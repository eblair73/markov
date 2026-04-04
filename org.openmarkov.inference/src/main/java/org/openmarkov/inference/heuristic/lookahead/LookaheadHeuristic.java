/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.lookahead;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base for n-step lookahead elimination heuristics.
 * <p>
 * At each real elimination step, instead of greedily picking the variable with
 * the lowest immediate cost, it evaluates:
 * <pre>
 *   score(G, X, depth) = immediateCost(G, active, X)
 *                      + min_Y score(G \ X, active \ X, Y, depth - 1)
 * </pre>
 * where the minimum over Y is restricted to the top {@code beamWidth} candidates
 * by immediate cost (beam search). The lookahead runs on a lightweight
 * {@code boolean[][]} adjacency matrix — no {@code ProbNet} copies are created
 * during the search.
 * <p>
 * Subclasses only need to implement {@link #immediateCost}.
 *
 * @see LookaheadMinFill
 * @see LookaheadMinCliqueSize
 */
public abstract class LookaheadHeuristic extends EliminationHeuristic {

    public static final int DEFAULT_DEPTH = 2;
    public static final int DEFAULT_BEAM  = 3;

    private static final Comparator<String> NAME_ORDER =
            Comparator.nullsLast(Comparator.naturalOrder());

    protected final int depth;
    protected final int beamWidth;

    /** Real graph copy maintained across actual eliminations. */
    private final ProbNet graphCopy;

    /** Maps each remaining variable to its node in {@code graphCopy}. */
    private final HashMap<Variable, Node> variablesNodes;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected LookaheadHeuristic(ProbNet probNet, List<List<Variable>> variablesToEliminate,
                                 int depth, int beamWidth) {
        super(probNet, variablesToEliminate);
        this.depth     = depth;
        this.beamWidth = beamWidth;
        this.graphCopy = probNet.copy();
        this.variablesNodes = new HashMap<>();
        for (Node node : graphCopy.getNodes()) {
            variablesNodes.put(node.getVariable(), node);
        }
    }

    // -------------------------------------------------------------------------
    // Template method
    // -------------------------------------------------------------------------

    /**
     * Immediate cost of eliminating variable at index {@code x} in graph {@code adj}.
     * Lower cost = better candidate.
     *
     * @param adj          current adjacency matrix (not mutated)
     * @param active       which indices are still present in the graph
     * @param x            index of the variable to evaluate
     * @param cardinalities number of states per variable index
     */
    protected abstract double immediateCost(boolean[][] adj, boolean[] active,
                                            int x, int[] cardinalities);

    // -------------------------------------------------------------------------
    // EliminationHeuristic
    // -------------------------------------------------------------------------

    @Override
    public final Variable getVariableToDelete() {
        int listIndex = variablesToEliminate.size() - 1;
        while (listIndex >= 0 && variablesToEliminate.get(listIndex).isEmpty()) {
            --listIndex;
        }
        if (listIndex < 0) {
            return null;
        }
        List<Variable> candidates = variablesToEliminate.get(listIndex);

        List<Variable> allRemaining = new ArrayList<>(variablesNodes.keySet());
        int n = allRemaining.size();
        int[] cardinalities = new int[n];
        Map<Variable, Integer> varToIdx = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            Variable v = allRemaining.get(i);
            varToIdx.put(v, i);
            cardinalities[i] = Math.max(1, v.getNumStates());
        }
        boolean[][] adj    = buildAdj(n, varToIdx);
        boolean[]   active = new boolean[n];
        Arrays.fill(active, true);

        Variable best          = null;
        double   bestScore     = Double.MAX_VALUE;
        int      bestNeighbors = Integer.MAX_VALUE;

        for (Variable candidate : candidates) {
            Integer idx = varToIdx.get(candidate);
            if (idx == null) {
                continue;
            }
            double s           = score(adj, active, idx, depth, cardinalities);
            int    numNeighbors = countNeighbors(adj, idx, n);
            if (s < bestScore
                    || (s == bestScore && numNeighbors < bestNeighbors)
                    || (s == bestScore && numNeighbors == bestNeighbors
                        && NAME_ORDER.compare(candidate.getName(),
                                              best == null ? null : best.getName()) < 0)) {
                best          = candidate;
                bestScore     = s;
                bestNeighbors = numNeighbors;
            }
        }
        return best;
    }

    @Override
    public final void afterEditExecutes(PNEdit edit) {
        super.afterEditExecutes(edit);
        Variable variable = getEventVariable(edit);
        if (variable == null) {
            return;
        }
        Node nodeToRemove = variablesNodes.get(variable);
        List<Node> neighbors = new ArrayList<>(nodeToRemove.getNeighbors());
        int m = neighbors.size();
        for (int i = 0; i < m - 1; i++) {
            Node ni = neighbors.get(i);
            for (int j = i + 1; j < m; j++) {
                Node nj = neighbors.get(j);
                if (!ni.isNeighbor(nj)) {
                    graphCopy.addLink(ni, nj, false);
                }
            }
        }
        graphCopy.removeNode(nodeToRemove);
        variablesNodes.remove(variable);
    }

    // -------------------------------------------------------------------------
    // Lookahead
    // -------------------------------------------------------------------------

    private double score(boolean[][] adj, boolean[] active, int x, int remainingDepth,
                         int[] cardinalities) {
        double c0 = immediateCost(adj, active, x, cardinalities);
        if (remainingDepth == 0) {
            return c0;
        }

        boolean[][] adj2    = eliminate(adj, x);
        boolean[]   active2 = active.clone();
        active2[x] = false;

        int      n           = adj2.length;
        double[] futureCosts = new double[n];
        int      numActive   = 0;
        for (int y = 0; y < n; y++) {
            if (active2[y]) {
                futureCosts[y] = immediateCost(adj2, active2, y, cardinalities);
                numActive++;
            } else {
                futureCosts[y] = Double.MAX_VALUE;
            }
        }
        if (numActive == 0) {
            return c0;
        }

        int[]  topK      = topKIndices(futureCosts, n, Math.min(beamWidth, numActive));
        double minFuture = Double.MAX_VALUE;
        for (int y : topK) {
            double s = score(adj2, active2, y, remainingDepth - 1, cardinalities);
            if (s < minFuture) {
                minFuture = s;
            }
        }
        return c0 + minFuture;
    }

    // -------------------------------------------------------------------------
    // Lightweight graph operations (available to subclasses)
    // -------------------------------------------------------------------------

    private boolean[][] buildAdj(int n, Map<Variable, Integer> varToIdx) {
        boolean[][] adj = new boolean[n][n];
        for (Map.Entry<Variable, Node> entry : variablesNodes.entrySet()) {
            int i = varToIdx.get(entry.getKey());
            for (Node neighbor : entry.getValue().getNeighbors()) {
                Integer j = varToIdx.get(neighbor.getVariable());
                if (j != null) {
                    adj[i][j] = true;
                    adj[j][i] = true;
                }
            }
        }
        return adj;
    }

    /**
     * Returns a new adjacency matrix where {@code x} has been eliminated:
     * fill-in edges added between all neighbor pairs of {@code x},
     * then row/column {@code x} cleared.
     */
    protected static boolean[][] eliminate(boolean[][] adj, int x) {
        int         n    = adj.length;
        boolean[][] next = new boolean[n][];
        for (int i = 0; i < n; i++) {
            next[i] = adj[i].clone();
        }
        for (int i = 0; i < n; i++) {
            if (!next[x][i]) continue;
            for (int j = i + 1; j < n; j++) {
                if (next[x][j]) {
                    next[i][j] = true;
                    next[j][i] = true;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            next[x][i] = false;
            next[i][x] = false;
        }
        return next;
    }

    /** Counts active neighbors of {@code x} in {@code adj}. */
    protected static int countNeighbors(boolean[][] adj, int x, int n) {
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (adj[x][i]) count++;
        }
        return count;
    }

    private static int[] topKIndices(double[] costs, int n, int k) {
        int[]    indices = new int[k];
        boolean[] used   = new boolean[n];
        for (int r = 0; r < k; r++) {
            double minVal = Double.MAX_VALUE;
            int    minIdx = -1;
            for (int i = 0; i < n; i++) {
                if (!used[i] && costs[i] < minVal) {
                    minVal = costs[i];
                    minIdx = i;
                }
            }
            if (minIdx == -1) {
                return Arrays.copyOf(indices, r);
            }
            indices[r]   = minIdx;
            used[minIdx] = true;
        }
        return indices;
    }
}

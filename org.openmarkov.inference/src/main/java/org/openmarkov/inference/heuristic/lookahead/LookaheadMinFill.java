/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.lookahead;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Lookahead heuristic using Weighted Min-Fill as the immediate cost function.
 * <p>
 * Immediate cost of eliminating X:
 * <pre>
 *   cost(X) = sum of card(Y) * card(Z)
 *             for each missing edge (Y, Z) among neighbors of X
 * </pre>
 *
 * @see LookaheadHeuristic
 */
public class LookaheadMinFill extends LookaheadHeuristic {

    public LookaheadMinFill(ProbNet probNet, List<List<Variable>> variablesToEliminate) {
        super(probNet, variablesToEliminate, DEFAULT_DEPTH, DEFAULT_BEAM);
    }

    public LookaheadMinFill(ProbNet probNet, List<List<Variable>> variablesToEliminate,
                            int depth, int beamWidth) {
        super(probNet, variablesToEliminate, depth, beamWidth);
    }

    @Override
    protected double immediateCost(boolean[][] adj, boolean[] active,
                                   int x, int[] cardinalities) {
        int  n    = adj.length;
        long cost = 0;
        for (int i = 0; i < n - 1; i++) {
            if (!adj[x][i]) continue;
            long cardI = cardinalities[i];
            for (int j = i + 1; j < n; j++) {
                if (adj[x][j] && !adj[i][j]) {
                    cost += cardI * cardinalities[j];
                }
            }
        }
        return (double) cost;
    }
}

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
 * Lookahead heuristic using Min-Clique-Size as the immediate cost function.
 * <p>
 * Immediate cost of eliminating X:
 * <pre>
 *   cost(X) = |neighbors(X)| + 1
 * </pre>
 * i.e. the number of variables in the clique that X would form with its neighbors.
 * This mirrors the primary criterion of {@code minimalCliqueSize}.
 *
 * @see LookaheadHeuristic
 */
public class LookaheadMinCliqueSize extends LookaheadHeuristic {

    public LookaheadMinCliqueSize(ProbNet probNet, List<List<Variable>> variablesToEliminate) {
        super(probNet, variablesToEliminate, DEFAULT_DEPTH, DEFAULT_BEAM);
    }

    public LookaheadMinCliqueSize(ProbNet probNet, List<List<Variable>> variablesToEliminate,
                                  int depth, int beamWidth) {
        super(probNet, variablesToEliminate, depth, beamWidth);
    }

    @Override
    protected double immediateCost(boolean[][] adj, boolean[] active,
                                   int x, int[] cardinalities) {
        return countNeighbors(adj, x, adj.length) + 1.0;
    }
}

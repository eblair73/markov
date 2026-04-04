/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterOfVariables;
import org.openmarkov.inference.algorithm.huginPropagation.HuginForest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class applies several heuristics to one <code>ProbNet</code> to produce an array of scores.<p>
 * The score is defined, per each <code>ProbNet</code>, as 1/(size of network),
 * and <i>size of network</i> = summation of the size of the cliques of the network.
 *
 * @author Manuel Arias
 */
public class TrivialHeuristicScore extends Thread implements EliminationHeuristicScore {
    
    // Attributes
    private ProbNet probNet;
    
    private double[] scores;
    
    private int scoreIndex;
    
    private HuginForest forest = null;
    
    private EliminationHeuristic heuristic;
    
    // Constructors
    
    /**
     * Initializes the
     *
     * @param probNet
     */
    public TrivialHeuristicScore(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    /**
     * This constructor is to be used internally to evaluate networks in parallel.<p>
     * A single instance of this class applies one <code>EliminationHeuristic</code> to one <code>ProbNet</code> and
     * stores the result in <code>scores[scoreIndex]</code>
     *
     * @param scores     <code>double[]</code>
     * @param scoreIndex <code>double</code>
     * @param probNet    <code>ProbNet</code>
     * @param heuristic  <code>EliminationHeuristic</code>
     */
    private TrivialHeuristicScore(double[] scores, int scoreIndex, ProbNet probNet, EliminationHeuristic heuristic) {
        this.scores = scores;
        this.scoreIndex = scoreIndex;
        this.probNet = probNet;
        this.heuristic = heuristic;
        createHuginForest();
    }
    
    // Methods
    
    /**
     * Builds a HuginForest
     *
     */
    private void createHuginForest() {
        forest = new HuginForest(probNet, heuristic);
        int accumulatedSize = getSumClustersSize(forest);
        scores[scoreIndex] = 1 / (1 + (double) accumulatedSize);
    }
    
    /**
     * @param probNet
     */
    public void setProbNet(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    /**
     * @see EliminationHeuristicScore#getScores(org.openmarkov.core.model.network.ProbNet, java.lang.Class[])
     */
    @SuppressWarnings({"rawtypes", "unchecked"}) @Override public double[] getScores(ProbNet probNet,
                                                                                     Class[] heuristicsClasses) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        
        int numHeuristics = heuristicsClasses.length;
        double[] scores = new double[numHeuristics];
        TrivialHeuristicScore[] trivialScores = new TrivialHeuristicScore[numHeuristics];
        for (int scoreIndex = 0; scoreIndex < numHeuristics; scoreIndex++) {
                // Create instance of heuristic given its class
                List<List<Variable>> listOfListOfVariables = new ArrayList<List<Variable>>();
                listOfListOfVariables.add(probNet.getVariables());
                Constructor<?> heuristicConstructor = heuristicsClasses[scoreIndex]
                        .getConstructor(ProbNet.class, List.class);
                // Create a thread for each heuristic
                trivialScores[scoreIndex] = new TrivialHeuristicScore(scores, scoreIndex, probNet.copy(),
                                                                      (EliminationHeuristic) heuristicConstructor
                                                                              .newInstance(new Object[]{probNet, listOfListOfVariables}));

        }
        
        //		// Wait until all threads finish
        //		while (counter.getCount() < scoreIndex) {
        //			wait();
        //		}
        //
        return scores;
    }
    
    /**
     * @param forest <code>HuginForest</code>
     * @return Sum of the clusters sizes. <code>int</code>
     */
    public int getSumClustersSize(HuginForest forest) {
        int sumCliquesSize = 0;
        for (ClusterOfVariables cluster : forest.getNodes()) {
            sumCliquesSize += cluster.size();
        }
        return sumCliquesSize;
    }
    
}

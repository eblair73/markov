/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.integrationTests.inference.util.Util;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination;
import org.openmarkov.inference.heuristic.hybridElimination.HybridElimination;
import org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn;
import org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

/**
 * Ensures that in every Bayesian network in the repository:
 * <ol>
 * <li> CanoMoralElimination is always better or equal than {MinimalFillIn and HybridElimination},
 * that are always better or equal than SimpleElimination
 * </ol>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class HeuristicsTest {

	// TODO Add performance tests

	@SuppressWarnings("rawtypes") private Class[] heuristicsClasses = new Class[] { CanoMoralElimination.class,
			MinimalFillIn.class, HybridElimination.class, SimpleElimination.class };

	@BeforeEach public void setUp() {
	}
	
	@Tag(TestSpeed.SLOW)
	@Test
	/** This is a performance test. It checks that some heuristics are better than others.
	 * We assume that CanoAndMoral must be better than all the others "most" of the times, 
	 * the same with MinimalFillin and SimpleElimination */
    public void test3() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		List<ProbNet> probNetsDB = Util.readProbNetsDB(BayesianNetworkType.getUniqueInstance());
		probNetsDB = Util.filterNonPureTablePotentialProbNets(probNetsDB);
		@SuppressWarnings("rawtypes") Class[] heuristicsClasses = new Class[] { CanoMoralElimination.class,
				MinimalFillIn.class, HybridElimination.class, SimpleElimination.class };
		double[][] allNetworksScores = getAllScores(probNetsDB, heuristicsClasses);
		int numNetworks = probNetsDB.size();
		int numHeuristics = heuristicsClasses.length;

		// Bidimensional upper triangular matrix to compare heuristics
		int[][] comparisons = new int[numHeuristics][];
		for (int i = 0; i < numHeuristics; i++) {
			comparisons[i] = new int[numHeuristics];
		}
		for (int i = 0; i < numNetworks; i++) {
			for (int j = 0; j < numHeuristics - 1; j++) {
				for (int k = j + 1; k < numHeuristics; k++) {
					if (allNetworksScores[i][j] != allNetworksScores[i][k]) {
						if (allNetworksScores[i][j] > allNetworksScores[i][k]) {
							comparisons[j][k]++;
						} else {
							comparisons[j][k]--;
						}
					}
				}
			}
		}

		// Create a vector of scores
		int[] punctuation = new int[numHeuristics];
		boolean[] removed = new boolean[numHeuristics];
		for (int i = 0; i < numHeuristics; i++) {
			removed[i] = false;
			for (int j = 0; j < numHeuristics; j++) {
				if (i != j) {
					if (i < j) {
						punctuation[i] += comparisons[i][j];
					} else {
						punctuation[i] -= comparisons[j][i];
					}
				}
			}
		}

		// Display the heuristic by quality from better to worst
		int lastPunctuationHeuristic = Integer.MIN_VALUE;
		int bestIndexHeuristic = 0;
		for (int i = 0; i < numHeuristics; i++) {
			int bestPunctuationHeuristic = Integer.MIN_VALUE;
			for (int j = 0; j < numHeuristics; j++) {
				if (punctuation[j] > bestPunctuationHeuristic && !removed[j]) {
					bestPunctuationHeuristic = punctuation[j];
					bestIndexHeuristic = j;
				}
			}
			removed[bestIndexHeuristic] = true;
			if (i == 0) {
				System.out.println("Heuristics, from best to worst:");
				System.out.print(heuristicsClasses[bestIndexHeuristic].getSimpleName());
				lastPunctuationHeuristic = bestPunctuationHeuristic;
			} else {
				if (lastPunctuationHeuristic == bestPunctuationHeuristic) {
					System.out.print(" = ");
				} else {
					System.out.print(" > ");
				}
				System.out.print(heuristicsClasses[bestIndexHeuristic].getSimpleName());
			}
			lastPunctuationHeuristic = bestPunctuationHeuristic;
		}
		System.out.println();
	}

	@SuppressWarnings("rawtypes") private double[][] getAllScores(Collection<ProbNet> probNetsDB,
                                                                  Class[] heuristicsClasses) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		double[][] networkScores = new double[probNetsDB.size()][];
		int i = 0;
		for (ProbNet bayesianNetwork : probNetsDB) {
			TrivialHeuristicScore heuristicScore = new TrivialHeuristicScore(bayesianNetwork);
			double[] scores = heuristicScore.getScores(bayesianNetwork, heuristicsClasses);
			networkScores[i++] = scores;
		}
		return networkScores;
	}

}

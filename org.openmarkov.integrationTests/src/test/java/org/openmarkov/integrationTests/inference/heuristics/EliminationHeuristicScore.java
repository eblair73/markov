/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface to calculate the quality of an
 * <code>EliminationHeuristic</code> applied to a <code>ProbNet</code>.
 *
 * @author Manuel Arias
 */
public interface EliminationHeuristicScore {

	/**
	 * @param probNet           <code>ProbNet</code>
	 * @param heuristicsClasses Set of heuristics to test. <code>Class[]</code>
	 * @return A measure of the quality of each heuristic. The bigger score is better. <code>double[]</code>
     */
	@SuppressWarnings("rawtypes") double[] getScores(ProbNet probNet, Class[] heuristicsClasses)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

}

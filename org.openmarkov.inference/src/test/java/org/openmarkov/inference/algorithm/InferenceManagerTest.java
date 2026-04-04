/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VariableElimination;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class InferenceManagerTest {
    private final static String VariableEliminationName = "VariableElimination";
    private InferenceManager inferenceManager;
    private ProbNet probNet;
    
    @BeforeEach public void setUp() {
        inferenceManager = new InferenceManager();
        probNet = new ProbNet();
    }
    
    // TODO - Check InferenceManager Class and this test (remove it or change to tasks logic?)
    @Test
    @Disabled("VariableElimination is no a concrete algorithm, but an Abstract class, therefore no concrete algorithm is named VariableElimination")
    public void testGetInferenceAlgorithms() {
        List<String> algorithmNames = inferenceManager.getInferenceAlgorithmNames(probNet);
        assertTrue(algorithmNames.contains(VariableEliminationName));
    }
    
    @Disabled("VariableElimination is no a concrete algorithm, but an Abstract class, therefore no concrete algorithm is named VariableElimination")
    @Test
    public void testGetInferenceAlgorithmsByName() throws org.openmarkov.core.exception.NotEvaluableNetworkException, NoSuchMethodException {
        InferenceAlgorithm algorithm = inferenceManager.getInferenceAlgorithmByName(VariableEliminationName, probNet);
        assertNotNull(algorithm);
    }
    
    @Disabled("VariableElimination is not exported via annotation, and therefore, the InferenceManager cannot find any VariableElimination class")
    @Test
    public void testGetDefaultInferenceAlgorithm() throws org.openmarkov.core.exception.NotEvaluableNetworkException {
        InferenceAlgorithm algorithm = inferenceManager.getDefaultInferenceAlgorithm(probNet);
        assertNotNull(algorithm);
        assertEquals(algorithm.getClass(), VariableElimination.class);
    }
}

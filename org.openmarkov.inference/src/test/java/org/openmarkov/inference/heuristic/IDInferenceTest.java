/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic;

public class IDInferenceTest {
    
    //todo: Most code in here cannot even compile due to changes
    
    /*
    @Test
    public void testInfluenceDiagramsInference() throws Exception {
        NetsRepository netsRepository = new NetsRepository();
        List<URL> influenceDiagramURLs = netsRepository.getNetworks(InfluenceDiagramType.getUniqueInstance());
        PGMXReader_0_2 reader = new PGMXReader_0_2();
        for (URL influenceDiagramURL : influenceDiagramURLs) {
            ProbNet influenceDiagram = reader.loadProbNet(influenceDiagramURL.getFile(), influenceDiagramURL.openStream());
            List<Criterion> decisionCriteria = influenceDiagram.getDecisionCriteria();
            if (decisionCriteria.size() == 1) {
                //Convert to decision tree
                if (influenceDiagram.getNumNodes() < 10) {
                    DecisionTreeElement equivalentDT = DecisionTreeBuilder.buildDecisionTree(influenceDiagram);
                    assertNotNull(equivalentDT);
                }
                VariableElimination elimination = null;
                elimination = new VariableElimination(influenceDiagram);
                //Calculate expected utility with VariableElimination
                TablePotential expectedUtility = elimination.getGlobalUtility();
                assertNotNull(expectedUtility);
                //Calculate optimal strategy
                Intervention optimalStrategy = elimination.getOptimalIntervention();
                assertNotNull(optimalStrategy);
            } else if (decisionCriteria.size() > 1) {
                //Assume it is cost -effectiveness, since there is no good way to know it
                VariableEliminationCE varEliminationCE = new VariableEliminationCE(influenceDiagram, 0, Double.POSITIVE_INFINITY, influenceDiagram.getPNESupport());
                //Calculate cost -effectiveness partition
                Intervention intervention = varEliminationCE.getOptimalIntervention();
                assertNotNull(intervention);
            }
        }
    }
     */
    
}

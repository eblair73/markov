package org.openmarkov.integrationTests.integrationTests.networksTests;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.TemporalEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TemporalEvaluationTest {
    
    // Delta parameter for Assert.Equals methods
    private final double deltaEquals = Math.pow(10, -4);
    private ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    
    @BeforeEach public void setUp() throws Exception {
        // New cost-effectiveness networks
        String networkName = "networks/mid/MID-Chancellor.pgmx";
        InputStream file = new IntegrationTest().getClass().getClassLoader().getResourceAsStream(networkName);
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        ProbNetInfo probNetInfo = pgmxReader.loadProbNetInfo(networkName, file);
        probNet = probNetInfo.getProbNet();
        preResolutionEvidence = probNetInfo.getEvidence().isEmpty() ? new EvidenceCase() : probNetInfo.getEvidence()
                                                                                                      .get(0);
        List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        for (Node node : utilityNodes) {
            if (!"Cost lamivudine".equals(node.getVariable().getBaseName())) {
                probNet.removeNode(node);
            }
        }
    }
    
    @Test
    @Tag(TestSpeed.SLOW)
    public void temporalEvolutionTest() throws Exception {
        VECEAnalysis veceAnalysis = new VECEAnalysis(probNet);
        veceAnalysis.setPreResolutionEvidence(preResolutionEvidence);
        veceAnalysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
        GTablePotential resultVE = veceAnalysis.getUtility();
        
        TemporalEvaluation temporalEvaluation = new TemporalEvaluation(probNet);
        temporalEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        temporalEvaluation.setConditioningVariables(Arrays.asList(probNet.getVariable("Therapy type")));
        
        List<Potential> potentialsPerSlice = temporalEvaluation.getUtilityPotentialsPerSlice();

        for (Potential potential : potentialsPerSlice) {
            LogManager.getLogger().debug(potential.toString());
        }
    }
    
}

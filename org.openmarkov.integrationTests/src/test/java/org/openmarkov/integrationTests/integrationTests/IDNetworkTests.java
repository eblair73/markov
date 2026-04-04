package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class IDNetworkTests {
    
    // Delta parameter for Assert.Equals methods
    protected final double deltaEquals = Math.pow(10, -4);
    protected String networkName;
    protected ProbNet probNet;
    protected EvidenceCase preResolutionEvidence;
    
    @BeforeEach public void setUp() throws java.net.URISyntaxException, org.openmarkov.core.exception.ParserException, IOException {
        URL res = getClass().getClassLoader().getResource(networkName);
        File f = Paths.get(res.toURI()).toFile();
        String absolutePath = f.getAbsolutePath();
        // Load the network: ID-decide-test
        ProbNetReader pgmxReader = newPGMXReader();
        ProbNetInfo probNetInfo = pgmxReader.loadProbNetInfo(absolutePath, new FileInputStream(absolutePath));
        this.probNet = probNetInfo.getProbNet();
        if (!probNetInfo.getEvidence().isEmpty()) {
            this.preResolutionEvidence = probNetInfo.getEvidence().get(0);
        }
    }
    
    protected ProbNetReader newPGMXReader() {
        return new PGMXReader_0_2();
    }
    
    @Disabled
    @Test
    public void veSensAnTornadoSpiderTests() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, ConstraintViolatedException {
        List<UncertainParameter> uncertainParameterList = SystematicSampling.getUncertainParameters(this.probNet);
        AxisVariation axisVariation = new AxisVariation();
        axisVariation.setVariationType(DeterministicAxisVariationType.POPP);
        axisVariation.setVariationValue(0.8);
        VESensAnTornadoSpider veSensAnTornadoSpider = new VESensAnTornadoSpider(probNet, preResolutionEvidence,
                                                                                uncertainParameterList, axisVariation, 50);
        HashMap<UncertainParameter, TablePotential> uncertainParameterTablePotentialHashMap = veSensAnTornadoSpider
                .getUncertainParametersPotentials();
    }
    
}

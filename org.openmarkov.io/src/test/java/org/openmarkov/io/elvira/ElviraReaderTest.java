/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.*;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraReaderTest {

	private ElviraParser elviraParser;

	@BeforeEach
	/** Creates a small ProbNet */ public void setUp() {
		elviraParser = new ElviraParser();
	}

	@Test public void testAlarm() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("alarm.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(37, probNet.getNumNodes());
		Assertions.assertEquals(46, probNet.getLinks().size());
	}

	@Test public void testApples() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("apples.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}

	@Test public void testAzar() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("azar.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(7, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testBarley() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Barley.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(48, probNet.getNumNodes());
		Assertions.assertEquals(84, probNet.getLinks().size());
	}

	@Test public void testBoblo() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("boblo.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(23, probNet.getNumNodes());
		Assertions.assertEquals(24, probNet.getLinks().size());
	}

	@Test public void testBoerlage92() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("boerlage92.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(23, probNet.getNumNodes());
		Assertions.assertEquals(36, probNet.getLinks().size());
	}

	@Test public void testCancer() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("cancer.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(5, probNet.getNumNodes());
		Assertions.assertEquals(5, probNet.getLinks().size());
	}

	@Test public void testCarStarts() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("car-starts.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(18, probNet.getNumNodes());
		Assertions.assertEquals(17, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testDiabetes() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Diabetes.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(413, probNet.getNumNodes());
		Assertions.assertEquals(602, probNet.getLinks().size());
	}

	@Test public void testDogProblem() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("dog-problem.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(5, probNet.getNumNodes());
		Assertions.assertEquals(4, probNet.getLinks().size());
	}

	@Test public void testElimbel2() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("elimbel2.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(10, probNet.getNumNodes());
		Assertions.assertEquals(10, probNet.getLinks().size());
	}

	@Test public void testHailfinder() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("hailfinder.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(56, probNet.getNumNodes());
		Assertions.assertEquals(66, probNet.getLinks().size());
	}

	@Test public void testHeadache() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("headache.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(12, probNet.getNumNodes());
		Assertions.assertEquals(11, probNet.getLinks().size());
	}

	@Test public void testInfUrinarias() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("inf-unirarias.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(12, probNet.getNumNodes());
		Assertions.assertEquals(20, probNet.getLinks().size());
	}

	@Test public void testInsurance() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("insurance.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(27, probNet.getNumNodes());
		Assertions.assertEquals(52, probNet.getLinks().size());
	}

	@Test public void testJavier1() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("javier1.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}

	@Test public void testJensenDI() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("jensendi.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(10, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test public void testLink() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Link.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(724, probNet.getNumNodes());
		Assertions.assertEquals(1125, probNet.getLinks().size());
	}

	@Test public void testMediastinoBasico3() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("mediastino-basico-3.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(15, probNet.getNumNodes());
		Assertions.assertEquals(28, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test public void testMildew() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Mildew.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(35, probNet.getNumNodes());
		Assertions.assertEquals(46, probNet.getLinks().size());
	}

	@Test public void testOil() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("oil.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(6, probNet.getNumNodes());
		Assertions.assertEquals(6, probNet.getLinks().size());
	}

	@Test public void testPigs() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Pigs.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(441, probNet.getNumNodes());
		Assertions.assertEquals(592, probNet.getLinks().size());
	}

	@Test public void testProstanetE() throws ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("prostanetE.elv");
        String netName = url.getFile();
        ProbNet probNet = elviraParser.loadProbNet(netName, new FileInputStream(netName));
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(47, probNet.getNumNodes());
		Assertions.assertEquals(81, probNet.getLinks().size());
	}
}

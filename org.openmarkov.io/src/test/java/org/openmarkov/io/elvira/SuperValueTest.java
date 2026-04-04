/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;

/**
 * @author Manuel Arias
 */
public class SuperValueTest {

	private ProbNet probNet;

	@Test public void test1() throws ParserException, IOException {
			URL url = this.getClass().getClassLoader().getResource("IDE4-decide-test.elv");
        ProbNetReader probNetReader = new ElviraParser();
        String netName = url.getFile();
        probNet = probNetReader.loadProbNet(netName, new FileInputStream(netName));
		Node node = probNet.getNode("Global utility", NodeType.UTILITY);
		assertNotNull(node);
		List<Potential> potentials = node.getPotentials();
		assertEquals(1, potentials.size());
		assertTrue(potentials.get(0) instanceof SumPotential);
		//		Potential potential = potentials.get(0);

	}

	@Test public void test2() throws ParserException, IOException {
			URL url = this.getClass().getClassLoader().getResource("IDU2-rodilla.elv");
        ProbNetReader probNetReader = new ElviraParser();
        String netName = url.getFile();
        probNet = probNetReader.loadProbNet(netName, new FileInputStream(netName));
		assertNotNull(probNet);
	}

	@Test public void test3() throws ParserException, IOException {
			URL url = this.getClass().getClassLoader().getResource("IDU2-rodilla-ce.elv");
        ProbNetReader probNetReader = new ElviraParser();
        String netName = url.getFile();
        probNet = probNetReader.loadProbNet(netName, new FileInputStream(netName));
		assertNotNull(probNet);
	}
}



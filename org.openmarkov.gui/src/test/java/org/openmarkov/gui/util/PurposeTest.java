/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.core.localize.StringDatabase;


/**
 * This class tests the class {@link Purpose}.
 *
 * @author jmendoza
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PurposeTest {
	/**
	 * This method initializes the language to English.
	 */
	@BeforeEach public void setUp() {
		StringDatabase.getUniqueInstance().setLanguage("en");
	}

	/**
	 * This method obtains the language-dependent string of a purpose that is
	 * known in English. Also it gets the language-dependent string of an
	 * unknown purpose.
	 */
	@Test public final void testGetString() {
        String string = Purpose.getString("treatment");
		assertEquals("Treatment", string);
		string = Purpose.getString("unknown");
		assertEquals(string, ">>> purpose.unknown.Text <<<");
	}

	/**
	 * This method obtains the language-dependent strings of all the known
	 * purposes.
	 */
	@Test public final void testGetListStrings() {
		String[] strings;

		strings = Purpose.getListStrings(true);
		assertEquals(strings.length, 11);
		assertEquals(strings[0], "");
		assertEquals(strings[1], "cost");
		assertEquals(strings[2], "effectiveness");
		assertEquals(strings[3], "treatment");
		assertEquals(strings[4], "riskfactor");
		assertEquals(strings[5], "symptom");
		assertEquals(strings[6], "sign");
		assertEquals(strings[7], "test");
		assertEquals(strings[8], "diseaseanomaly");
		assertEquals(strings[9], "auxiliary");
		assertEquals(strings[10], "other");
	}

	/**
	 * This method gets all purposes by their indexes.
	 */
	@Test public final void testGetByIndex() {
		assertEquals(Purpose.getByIndex(0), "");
		assertEquals(Purpose.getByIndex(1), "cost");
		assertEquals(Purpose.getByIndex(2), "effectiveness");
		assertEquals(Purpose.getByIndex(3), "treatment");
		assertEquals(Purpose.getByIndex(4), "riskfactor");
		assertEquals(Purpose.getByIndex(5), "symptom");
		assertEquals(Purpose.getByIndex(6), "sign");
		assertEquals(Purpose.getByIndex(7), "test");
		assertEquals(Purpose.getByIndex(8), "diseaseanomaly");
		assertEquals(Purpose.getByIndex(9), "auxiliary");
		assertEquals(Purpose.getByIndex(10), "other");
	}

	/**
	 * This method checks that all the purposes correspond to their index.
	 */
	@Tag(TestSpeed.SLOW)
	@Test public final void testGetIndex() {
		assertEquals(Purpose.getIndex(""), 0);
		assertEquals(Purpose.getIndex("cost"), 1);
		assertEquals(Purpose.getIndex("effectiveness"), 2);
		assertEquals(Purpose.getIndex("treatment"), 3);
		assertEquals(Purpose.getIndex("riskfactor"), 4);
		assertEquals(Purpose.getIndex("symptom"), 5);
		assertEquals(Purpose.getIndex("sign"), 6);
		assertEquals(Purpose.getIndex("test"), 7);
		assertEquals(Purpose.getIndex("diseaseanomaly"), 8);
		assertEquals(Purpose.getIndex("auxiliary"), 9);
		assertEquals(Purpose.getIndex("other"), 10);
		assertEquals(Purpose.getIndex("unknown"), 10);
	}
}

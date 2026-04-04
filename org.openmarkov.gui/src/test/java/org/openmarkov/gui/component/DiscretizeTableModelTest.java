/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.common.KeyTable;


/**
 * This class tests the class {@link org.openmarkov.gui.component.DiscretizeTableModel}.
 *
 * @author jlgozalo
 * @version 1.0 Agosto/09
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DiscretizeTableModelTest {
	/**
	 * constant definition of the columns
	 */
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_INTERVAL_NAME = 1;
	private static final int COLUMN_LOWER_LIMIT_SYMBOL = 2;
	private static final int COLUMN_LOWER_LIMIT_VALUE = 3;
	private static final int COLUMN_SEPARATOR = 4;
	private static final int COLUMN_UPPER_LIMIT_VALUE = 5;
	private static final int COLUMN_UPPER_LIMIT_SYMBOL = 6;
	/**
	 * Common object for all tests.
	 */
	private DiscretizeTableModel discretizeTableModel = null;
	private KeyTable discretizeTable = null;

	/**
	 * Creates a new discretizeTableModel for all tests.
	 */
	@BeforeEach public void setUp() {
		Object[][] data = new Object[2][7];
		data[0][0] = "i0";
		data[0][1] = "first interval";
		data[0][2] = "(";
		data[0][3] = 1.0;
		data[0][4] = ",";
		data[0][5] = 1.1;
		data[0][6] = "]";
		data[1][0] = "i1";
		data[1][1] = "second interval";
		data[1][2] = "[";
		data[1][3] = 0.0;
		data[1][4] = ",";
		data[1][5] = 1.0;
		data[1][6] = "]";
		String[] columns = { "c0", "c1", "c2", "c3", "c4", "c5", "c6" };
		discretizeTableModel = new DiscretizeTableModel(data, columns);
		discretizeTable = new KeyTable(discretizeTableModel, true, true);
	}

	/**
	 * This method tests the method getColumnClass
	 */
	@Test public final void testGetColumnClass() {
		Class<?> value = null;

		value = this.discretizeTable.getColumnClass(COLUMN_ID);
		assertEquals(value, String.class);
		value = this.discretizeTable.getColumnClass(COLUMN_INTERVAL_NAME);
		assertEquals(value, String.class);
		value = this.discretizeTable.getColumnClass(COLUMN_LOWER_LIMIT_SYMBOL);
		assertEquals(value, String.class);
		value = this.discretizeTable.getColumnClass(COLUMN_LOWER_LIMIT_VALUE);
		assertEquals(value, Double.class);
		value = this.discretizeTable.getColumnClass(COLUMN_SEPARATOR);
		assertEquals(value, String.class);
		value = this.discretizeTable.getColumnClass(COLUMN_UPPER_LIMIT_VALUE);
		assertEquals(value, Double.class);
		value = this.discretizeTable.getColumnClass(COLUMN_UPPER_LIMIT_SYMBOL);
		assertEquals(value, String.class);
	}

	/**
	 * This method tests the method getColumnClass
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testIsCellEditable() {
		assertFalse(this.discretizeTable.isCellEditable(0, COLUMN_ID));
		assertTrue(this.discretizeTable.isCellEditable(0, COLUMN_INTERVAL_NAME));
		assertFalse(this.discretizeTable.isCellEditable(0, COLUMN_LOWER_LIMIT_SYMBOL));
		assertTrue(this.discretizeTable.isCellEditable(0, COLUMN_LOWER_LIMIT_VALUE));
		assertFalse(this.discretizeTable.isCellEditable(0, COLUMN_SEPARATOR));
		assertTrue(this.discretizeTable.isCellEditable(0, COLUMN_UPPER_LIMIT_VALUE));
		assertFalse(this.discretizeTable.isCellEditable(0, COLUMN_UPPER_LIMIT_SYMBOL));
	}

}

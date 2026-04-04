/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Edit that modifies the uncertain values and numeric values of a single column in a
 * node's potential table. Used for sensitivity analysis parameters.
 *
 * @author mluque
 * @version 1 23/06/11
 */
@SuppressWarnings("serial") public class UncertainValuesEdit extends PNEdit {
	private final List<Double> newValuesColumn;
	private final List<UncertainValue> newUncertainColumn;
	private final List<Double> oldValuesColumn;
	private final List<UncertainValue> oldUncertainColumn;
	private final int basePosition;
	private final Node node;
	private final boolean isChanceVariable;
	private final boolean wasNullOldUncertainValues;
	/**
	 * Selected column in the values table
	 */
	private final int selectedColumn;

	/**
	 * Creates a new {@code AddNodeEdit} with the network where the new
	 * new node will be added and basic information about it.
	 *
	 * @param node             the new node
	 * @param uncertainColumn Uncertain column
	 * @param valuesColumn Values column
	 * @param basePosition Base position
	 * @param selectedColumn Selected column
	 * @param isChanceVariable Is chance variable?
	 */
	public UncertainValuesEdit(Node node, List<UncertainValue> uncertainColumn, List<Double> valuesColumn,
			int basePosition, int selectedColumn, boolean isChanceVariable) {
		super(node.getProbNet());
		this.node = node;
		this.isChanceVariable = isChanceVariable;
		Variable variable = node.getVariable();
		newUncertainColumn = uncertainColumn;
		newValuesColumn = valuesColumn;
		this.basePosition = basePosition;
		UncertainValue[] oldUncertainValues = getPotential().getUncertainValues();
		wasNullOldUncertainValues = oldUncertainValues == null;
		oldUncertainColumn = wasNullOldUncertainValues ? null : getColumn(oldUncertainValues, variable, basePosition);
		oldValuesColumn = getColumn(getPotential().getValues(), variable, basePosition);
		this.selectedColumn = selectedColumn;
	}

	public int getBasePosition() {
		return basePosition;
	}

	public boolean isChanceVariable() {
		return isChanceVariable;
	}

	public Node getNode() {
		return node;
	}

	private List<Double> getColumn(double[] values, Variable variable, int basePosition) {
		List<Double> column = new ArrayList<>();
		int numElements = (isChanceVariable) ? variable.getNumStates() : 1;
		for (int i = 0; i < numElements; i++) {
			column.add(values[basePosition + i]);
		}
		return column;
	}

	private List<UncertainValue> getColumn(UncertainValue[] uncertainValues, Variable variable, int basePosition) {
        int numElements = (isChanceVariable) ? variable.getNumStates() : 1;
        List<UncertainValue> column = new ArrayList<>(Arrays.asList(uncertainValues)
                                                            .subList(basePosition, numElements + basePosition));
		return column;
	}

	public int getSelectedColumn() {
		return selectedColumn;
	}

	private TablePotential getPotential() {
		if (node.getPotentials().get(0) instanceof TablePotential) {
			return (TablePotential) (node.getPotentials().get(0));
        }
        if (node.getPotentials().get(0) instanceof ExactDistrPotential) {
            return ((ExactDistrPotential) (node.getPotentials().get(0))).getTablePotential();
        }
        return null;
    }

	public Variable getVariable() {
		return node.getVariable();
	}
	
	@Override protected void doEdit() {
		TablePotential potential = getPotential();
		potential.setUncertainValuesConsistently(newUncertainColumn, newValuesColumn, basePosition);
	}



	@Override public void undo() {
		super.undo();
		TablePotential potential = getPotential();
		if (wasNullOldUncertainValues) {
			potential.setUncertainValues(null);
			potential.setValues(oldValuesColumn.stream().mapToDouble(Double::doubleValue).toArray());
		} else {
			potential.setUncertainValuesConsistently(oldUncertainColumn, oldValuesColumn, basePosition);
		}
	}

}
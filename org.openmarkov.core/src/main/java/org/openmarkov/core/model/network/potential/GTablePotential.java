/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * A generalized {@code TablePotential} that contains an
 * {@code Objects} table of the same type: {@code Element}.
 */
public class GTablePotential<E> extends AbstractIndexedPotential {

	// Attributes
	/**
	 * The array buffer into which the elements of the
	 * {@code GeneralizedTablePotential} are stored. This attribute is
	 * public for the sake of efficiency.
	 */
	public final List<E> elementTable;

	// Constructors

	/**
	 * @param variables List of variables
	 */
	public GTablePotential(List<Variable> variables) {
		super(variables, null);
		elementTable = new ArrayList<>(tableSize);
	}

	/**
	 * @param variables List of variables
	 * @param role Potential role
	 */
	public GTablePotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
		elementTable = new ArrayList<>(tableSize);
	}

	/**
	 * @param variables List of variables
	 * @param role Potential role
	 * @param elementTable List of elements
	 */
	public GTablePotential(List<Variable> variables, PotentialRole role, List<E> elementTable) {
		super(variables, role);
		this.elementTable = elementTable;
	}

	// Methods
	public GTablePotential(Potential potential) {
		this(potential.getVariables(), potential.getPotentialRole());
	}

	@Override
	public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}

	@Override
	public Potential copy() {
		return new GTablePotential<>(variables, role, new ArrayList<>(elementTable));
	}

	/**
	 * Overrides {@code toString} method. Mainly for test purposes
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		int numVariables = (variables != null) ? variables.size() : 0;
		int numElementsTable = elementTable.size();
		if (numVariables > 0) {
			// writes each configuration and its value
			int[] configuration = null;
			if (numElementsTable == 0) {
				buffer.append("Empty potential.\n");
			} else {
				buffer.append("Number of elements : ");
				buffer.append(numElementsTable);
				buffer.append("\n");
			}
			for (int i = 0; i < numElementsTable; i++) {
				buffer.append("If ");
				if (dimensions != null) {
					configuration = getConfiguration(i);
				}
				for (int j = 0; configuration != null && j < configuration.length; j++) {
					Variable variable = variables.get(j);
					buffer.append(variable.getName());
					buffer.append(" = ");
					buffer.append(variable.getStateName(configuration[j]));
					buffer.append(", ");
					if (j == configuration.length - 1) {
						buffer.append("then\n");
					}
				}
				buffer.append(elementTable.get(i).toString());
			}
		} else {
			buffer.append("No variables.\nNumber of elements in table: ");
			buffer.append(numElementsTable);
			buffer.append("\n");
			for (int i = 0; i < numElementsTable; i++) {
				buffer.append(elementTable.get(i).toString());
			}
		}
		return buffer.toString();
	}

}

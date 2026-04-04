/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.VariableTypeConverter;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyFiniteStatesVariables;
import org.openmarkov.core.model.network.constraint.OnlyNumericVariables;
import org.openmarkov.core.action.base.PNEdit;

/**
 * @author Manuel Arias
 */
@SuppressWarnings("serial")
public class VariableTypeEdit extends PNEdit {
    // private ProbNet probNet;
    private final Node node;
    private final VariableType newType;
    private final VariableType currentType;
    private State[] currentStates;
    private PartitionedInterval currentPartitionedInterval;

    public VariableTypeEdit(Node node, VariableType newType) {
        super(node.getProbNet());
        this.node = node;
        this.newType = newType;
        this.currentType = node.getVariable().getVariableType();

    }

    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(OnlyDiscreteVariables.class) instanceof OnlyDiscreteVariables constraint) {
            if (this.newType != VariableType.DISCRETIZED) {
                constraintChecker.addException(
                        new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(constraint, this.getNode()
                                .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(
                OnlyFiniteStatesVariables.class) instanceof OnlyFiniteStatesVariables constraint) {
            if (!OnlyFiniteStatesVariables.nodeIsFinite(this.node.getNodeType(), this.newType)) {
                constraintChecker
                        .addException(new ConstraintViolatedException.OnlyFiniteStatesAllowed(constraint, this.getNode()
                                .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(OnlyNumericVariables.class) instanceof OnlyNumericVariables constraint) {
            if (this.newType != VariableType.NUMERIC) {
                constraintChecker.addException(
                        new ConstraintViolatedException.OnlyNumericVariablesAllowed(constraint, this.getNode()
                                .getVariable()));
            }
        }
    }

    @Override
    protected void doEdit() {
        // Save the current states and interval
        currentStates = node.getVariable().getStates();
        currentPartitionedInterval = node.getVariable().getPartitionedInterval();

        // Restore the states
        node.getVariable().setStates(currentStates.length == 1
                ? node.getProbNet().getDefaultStates()
                : currentStates);

        if (currentType != newType) {
            VariableTypeConverter.convertVariableType(node, newType);
        }

        VariableTypeConverter.resetLinks(node);

    }

    @Override
    public void undo() {
        node.getVariable().setVariableType(currentType);
        node.getVariable().setStates(currentStates);
        node.getVariable().setPartitionedInterval(currentPartitionedInterval);
    }

    public Node getNode() {

        return this.node;
    }

}

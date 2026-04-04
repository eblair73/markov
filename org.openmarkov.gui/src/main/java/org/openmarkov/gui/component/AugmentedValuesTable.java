/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Node Potentials (either in a general family or in a
 * canonical family potential). This table also shows the data in several ways,
 * depending upon the type of user selection:
 * <ul>
 * <li>Probabilities or states values</li>
 * <li>Probabilistic or Deterministic values allowed</li>
 * <li>All parameters or Only independent parameters</li>
 * <li>TPC or canonical parameters(for the Canonical families)</li>
 * <li>Net or Compound values (for the Canonical families)</li>
 * </ul>
 *
 * @author carmenyago
 * @version 1 Apr/2017
 */
public class AugmentedValuesTable extends ValuesTable implements PNEditListener {
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor
     *
     * @param node       - the node with the TablePotential or ExactDistrPotential
     * @param tableModel - the model of the TablePotential or ExactDistrPotential
     * @param modifiable - true if the table can be edited and modified
     */
    public AugmentedValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
        super(node, tableModel, modifiable);
        this.tablePotential = ((AugmentedProbTablePotential) potential).getAugmentedProbTable();
    }
    
    /**
     * @param node the node
     * @param tableModel the table model
     * @param AugmentedProbTable the augmented prob table
     * @param modifiable the modifiable
     */
    public AugmentedValuesTable(Node node, ValuesTableModel tableModel, AugmentedProbTable AugmentedProbTable,
                                final boolean modifiable) {
        super(node, tableModel, modifiable);
        this.tablePotential = AugmentedProbTable;
    }
    
    /**
     * Constructor for ValuesTable
     * revised--&gt;not changed
     */
    public AugmentedValuesTable(ValuesTableModel tableModel, final boolean modifiable) {
        super(tableModel, modifiable);
        
    }
    
    /**
     * Default display configuration for this table
     */
    @Override protected void defaultConfiguration() {
        super.defaultConfiguration();
        //CMI
        //CHANGED
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //CMF
    }
    
    /**
     * check the value to modify in the table and sets
     */
    @Override public void setValueAt(Object newValue, int row, int col) {
        Object oldValue = getValueAt(row, col);
        if (oldValue.equals(newValue)) {
            return;
        }
        VariableExpression expression = (VariableExpression) newValue;
        AugmentedPotentialValueEdit nodePotentialEdit = new AugmentedPotentialValueEdit(node, expression, row, col);
        try {
            nodePotentialEdit.executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * Check if newValue is a String or a Double
     *
     * @param newValue - new value to validate
     *
     * @author carmenyago
     */
    @Override protected boolean castValue(Object newValue) {
        return switch (newValue) {
            case String newValueAsString -> {
                try {
                    Double.parseDouble(newValueAsString);
                    yield true;
                } catch (NumberFormatException ex) {
                    yield false;
                }
            }
            case Double d -> true;
            default -> false;
        };
    }
    
    /**
     * print the NodePotentialTable
     * carmenyago only removed the println of the deterministic attribute
     *
     * @author carmenyago revised--&gt;minor changes
     */
    @Override public void printTable() {
        System.out.println("NodePotentialTable: ");
        if (getVariable() != null) {
            System.out.println("    variable = " + getVariable().getName());
        } else {
            System.out.println("    variable = not defined yet");
        }
        if (tableModel != null) {
            System.out.println("    tableModel.firstEditableRow = " + tableModel.getFirstEditableRow());
            System.out.println("    tableModel.rowCount = " + tableModel.getRowCount());
            System.out.println("    tableModel.columnCount = " + tableModel.getColumnCount());
        } else {
            System.out.println("    tableModel.firstEditableRow = not tableModel yet");
        }
        System.out.println("    lastEditableRow = " + lastEditableRow);
        System.out.println("    usingGeneralPotencial = " + isUsingGeneralPotential());
        System.out.println("    showingAllParameters = " + isShowingAllParameters());
        System.out.println("    showingProbabilitiesValues = " + isShowingProbabilitiesValues());
        System.out.println("    showingTPCvalues = " + isShowingTPCvalues());
    }
    
    /**
     * Updates the edited column
     */
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (edit instanceof AugmentedPotentialValueEdit) {
            augmentedPotentialValueEditHappened((AugmentedPotentialValueEdit) edit);
        }
    }
    
    /**
     * @param edit the edit
     */
    public void augmentedPotentialValueEditHappened(AugmentedPotentialValueEdit edit) {
        int position = edit.getIndexSelected();
        Potential editPotential = edit.getNewPotential();
        AugmentedProbTable editTable;
        if (editPotential instanceof AugmentedProbTablePotential) {
            editTable = ((AugmentedProbTablePotential) editPotential).getAugmentedProbTable();
            
        } else {
            editTable = ((UnivariateDistrPotential) editPotential).getAugmentedProbTable();
        }
        VariableExpression[] functionValues = editTable.getFunctionValues();
        if (position >= 0) {
            int rowPosition = edit.getRowPosition(position);
            int columnPosition = edit.getColumnPosition();
            if (editPotential instanceof AugmentedProbTablePotential) {
                for (int i = lastEditableRow; i <= lastEditableRow; i++) {
                    super.getModel().setValueAt("Complement", i, columnPosition);
                }
            }
            super.getModel().setValueAt(functionValues[position], rowPosition, columnPosition);
        }
    }
    
    /**
     * UNCLEAR--&gt;Priority list
     */
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof AugmentedPotentialValueEdit augmentedEdit) {
            Potential editPotential = augmentedEdit.getNewPotential();
            AugmentedProbTable editTable;
            if (editPotential instanceof AugmentedProbTablePotential) {
                editTable = ((AugmentedProbTablePotential) editPotential).getAugmentedProbTable();
            } else {
                editTable = ((UnivariateDistrPotential) editPotential).getAugmentedProbTable();
            }
            super.getModel()
                 .setValueAt(editTable.getFunctionValues()[augmentedEdit.getIndexSelected()], augmentedEdit.getRowPosition(),
                             augmentedEdit.getColumnPosition());
        }

    }
    
    /**
     * This method edits the cell at row, column.
     * If isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent the entire cell is selected
     * UNCLEAR isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent values never change
     * Overrided to provide Select All editing functionality
     *
     * @param row    - the row of the edited cell
     * @param column - the column of the edited cell
     * @param e      - event to pass into shouldSelectCell;
     *               revised--&gt; not changed
     */
    @Override public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        
        if (e instanceof MouseEvent)
            System.err.println("CLICK COUNT" + ((MouseEvent) e).getClickCount());
        
        return result;
        
    }
    
    /**
     * If the editor that is handling the editing session is not a JTextComponent, the method does nothing
     * If the editor is a JTextComponent then:
     * If e is and instance of KeyEvent, ActionEvent or MouseEvent, the method select all the text of the cell
     *
     * @param e event which provoked the edition and selection
     *          revised --&gt; not changed
     */
    private void selectAll(EventObject e) {
        // Returns the component that is handling the editing session.
        final Component editor = getEditorComponent();
        if (!(editor instanceof JTextComponent))
            return;
        switch (e) {
            case null -> ((JTextComponent) editor).selectAll();
            // Typing in the cell was used to activate the editor
            case KeyEvent keyEvent when isSelectAllForKeyEvent -> ((JTextComponent) editor).selectAll();
            // F2 was used to activate the editor
            case ActionEvent actionEvent when isSelectAllForActionEvent -> ((JTextComponent) editor).selectAll();
            // A mouse click was used to activate the editor.
            // Generally this is a double click and the second mouse click is
            // passed to the editor which would remove the text selection unless
            // we use the invokeLater()
            case MouseEvent mouseEvent when isSelectAllForMouseEvent ->
                    SwingUtilities.invokeLater(() -> ((JTextComponent) editor).selectAll());
            default -> {
            }
        }
    }
    
}
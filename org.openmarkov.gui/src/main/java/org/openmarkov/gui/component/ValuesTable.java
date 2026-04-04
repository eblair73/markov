/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.component;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.UncertainValuesEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.action.TablePotentialValueEdit;
import org.openmarkov.gui.dialog.common.KeyTable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.exception.MismatchedValueException;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Node Potentials (either in a general family or in a
 * canonical family potential). This table also shows the data in several ways,
 * depending upon the type of user selection:
 * <ul>
 * <Li>Probabilities or states values</li>
 * <li>Probabilistic or Deterministic values allowed</li>
 * <li>All parameters or Only independent parameters</li>
 * <li>TPC or canonical parameters(for the Canonical families)</li>
 * <li>Net or Compound values (for the Canonical families)</li>
 * </ul>
 *
 * @author jlgozalo
 * @author mpalacios
 * @version 3.0 - cmyago - May 2016 - eliminates the different treatment for the Utility nodes.
 * - eliminates the deterministic values
 */
public class ValuesTable extends KeyTable implements PNEditListener {
    /**
     * first editable Column
     */
    public static final int FIRST_EDITABLE_COLUMN = 1;
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    /**
     * number of decimals positions to be used for calculations and display
     */
    protected static int decimalPositions = 2;                                  // by
    /**
     * table model
     */
    protected ValuesTableModel tableModel;
    /**
     * Boolean array with the rows and columns of the tableModel.
     * Each cell of the array is true if the data has been modified
     * boolean data model (to know if a value has been changed)
     */
    protected boolean[][] dataModified = null;
    /**
     * Table Row Sorter/Filter
     */
    protected TableRowSorter<ValuesTableModel> tableRowSorter = null;
    /**
     * type of node for this variable
     */
    protected NodeType nodeType = null;
    // default;
    /**
     * last editable row. By default, it is zero until runtime initialisation
     */
    protected int lastEditableRow = 0;
    /**
     * define if the table is using General or Canonical Potentials
     * <ul>
     * <li>if index = 0 then Using General Potential</li>
     * <li>if index = 1,2,3 then Using Canonical Potential (family OR)</li>
     * <li>if index = 4,5,6 then Using Canonical Potential (famili AND)</li>
     * </ul>
     */
    protected final int indexPotential = 0;                                  // General
    /**
     * define if the table shows all parameters or only independent parameters
     */
    protected boolean showingAllParameters = false;
    // Potential
    // by
    // default
    /**
     * define if the table shows probabilities values or state name
     */
    
    protected final boolean showingProbabilitiesValues = false;
    /**
     * define if the table shows TPC values or canonical values
     */
    protected final boolean showingTPCvalues = false;
    /**
     * define if the table shows Optimal Decision
     */
    protected boolean showingOptimal = false;
    /**
     * String database
     */
    protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    protected Node node;
    /**
     * First potential of node
     */
    protected Potential potential = null;
    /**
     * True if the class of potential is ExactDistrPotential
     */
    protected boolean isExactDistrPotential = false;
    /**
     * if getExactDistrPotential tablePotential=potential.getTablePotential, if !getExactDistrPotential tablePotential= (tablePotential)potential
     */
    protected TablePotential tablePotential = null;
    protected ProbNet probNet;
    /**
     * Define the last column of the table that was modified
     */
    
    protected int lastCol = -1;
    /**
     * Define the priority list when potential values are edited
     */
    
    protected List<Integer> priorityList = new LinkedList<Integer>();
    protected final boolean isSelectAllForMouseEvent = true;
    protected final boolean isSelectAllForActionEvent = true;
    protected final boolean isSelectAllForKeyEvent = true;
    /**
     * first editable row. By default, it is zero until runtime initialisation
     */
    private int firstEditableRow = 0;
    
    
    /**
     * Default constructor
     *
     * @param node       - the node with the TablePotential or ExactDistrPotential
     * @param tableModel - the model of the TablePotential or ExactDistrPotential
     * @param modifiable - true if the table can be edited and modified
     */
    public ValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
        super(tableModel, modifiable, true, true);
        node.getProbNet().getPNESupport().addListener(this);
        this.tableModel = tableModel;
        this.node = node;
        this.probNet = node.getProbNet();
        this.potential = node.getPotentials().get(0);
        //Adding the initialisation of getExactDistrPotential
        
        this.isExactDistrPotential = (node.getPotentials().get(0) instanceof ExactDistrPotential);
        if (isExactDistrPotential) {
            tablePotential = ((ExactDistrPotential) (this.potential)).getTablePotential();
        } else if (potential instanceof TablePotential) {
            tablePotential = (TablePotential) this.potential;
        }
        //
        if (modifiable) {
            int numRowsModel = tableModel.getRowCount();
            int numColumsModel = tableModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            initializeDataModified(false);
        }
    }
    
    /**
     * Constructor for ValuesTable
     */
    public ValuesTable(ValuesTableModel tableModel, final boolean modifiable) {
        super(tableModel, modifiable, true, true);
        this.tableModel = tableModel;
        if (modifiable) {
            int numRowsModel = tableModel.getRowCount();
            int numColumsModel = tableModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            initializeDataModified(false);
        }
    }
    
    /**
     * Sets a default id for the columns (Excel format)
     *
     * @param howManyColumns - number of columns of the table
     */
    public static String[] getColumnsIdsSpreadSheetStyle(int howManyColumns) {
        String[] columnsId = new String[howManyColumns];
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int columnPosition = 0; columnPosition < howManyColumns; columnPosition++) {
            String columnId = "";
            int firstLetterPosition = columnPosition % 26;
            int secondLetterPosition = columnPosition / 26 - 1;
            if (columnPosition >= (26 * 27)) {
            } else if (columnPosition >= 26) {
                columnId = columnId + ALPHABET.charAt(secondLetterPosition) + ALPHABET
                        .charAt(firstLetterPosition);
            } else {
                columnId = columnId + ALPHABET.charAt(firstLetterPosition);
            }
            columnsId[columnPosition] = columnId;
        }
        return columnsId;
    }
    
    /**
     * @return the decimalPositions
     */
    protected static int getDecimalPositions() {
        return decimalPositions;
    }
    
    /**
     * @param newDecimalPositions the decimalPositions to set
     */
    protected static void setDecimalPositions(int newDecimalPositions) {
        decimalPositions = newDecimalPositions;
    }
    
    /**
     * This method returns the dataModified variable. If dataModified doesn't exist it is created
     *
     * @return dataModified
     *
     * @see #dataModified
     * revised--&gt; not changed
     */
    public boolean[][] getDataModified() {
        if (dataModified == null) {
            int numRowsModel = tableModel.getRowCount();
            int numColumsModel = tableModel.getColumnCount();
            dataModified = new boolean[numRowsModel][numColumsModel];
        }
        return dataModified;
    }
    
    /**
     * This method initialises all the cells of dataModified to the boolean value given by isModified
     *
     * @param isModified - initial value for the cells is dataModified
     *
     * @see #dataModified
     * revised--&gt; not changed
     */
    public void initializeDataModified(boolean isModified) {
        if (tableModel != null) {
            if (dataModified == null) {
                getDataModified();
            }
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    dataModified[i][j] = isModified;
                }
            }
        }
    }
    
    /**
     * Default display configuration for this table
     */
    @Override protected void defaultConfiguration() {
        super.defaultConfiguration();
        setFirstColumnHidden(false); // key prefix column is hidden
        setShowColumnHeader(false); // no column header here
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setGridColor(Color.DARK_GRAY);
        setDefaultRenderer(Double.class, new ValuesTableCellRenderer(0));
        setDefaultRenderer(String.class, new ValuesTableCellRenderer(0));
        // next two lines is a cool trick to enhance table performance
        ToolTipManager.sharedInstance().unregisterComponent(this);
        ToolTipManager.sharedInstance().unregisterComponent(getTableHeader());
    }
    
    /**
     * Resets the model in use
     * revised-&gt; not changed
     */
    public void resetModel() {
        tableModel = null;
        dataModified = null;
    }
    
    /**
     * Gets the tableModel attribute
     * revised--&gt;not changed
     */
    public ValuesTableModel getTableModel() {
        return this.tableModel;
    }
    
    /**
     * Sets the data model for this table to newModel and registers with it for
     * listener notifications from the new data model.
     *
     * @param newDataModel the new data source for this table.
     *
     * @throws IllegalArgumentException if newModel is null.
     *                                  <p>
     *                                  revised--&gt;not changed
     */
    public void setModel(ValuesTableModel newDataModel) throws IllegalArgumentException {
        super.setModel(newDataModel);
        this.tableModel = newDataModel;
        tableRowSorter = new TableRowSorter<ValuesTableModel>(((ValuesTableModel) getModel()));
        // not display the last row where the cells has states and not values
        // and it is only required when displaying states values
    }
    
    /**
     * @see javax.swing.JTable#changeSelection(int, int, boolean, boolean)
     * revised--&gt;not changed
     */
    @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        if (columnIndex < FIRST_EDITABLE_COLUMN) { // not selectable
            super.changeSelection(rowIndex, columnIndex + 1, toggle, extend);
        } else {
            super.changeSelection(rowIndex, columnIndex, toggle, extend);
        }
    }
    
    /**
     * Cancels the editing in any cell of the table, avoiding its new value is
     * recorded.
     * revised--&gt;not changed
     */
    public void cancelCellEditing() {
        TableCellEditor actualEditor = getCellEditor();
        if (actualEditor != null) {
            actualEditor.cancelCellEditing();
        }
    }
    
    /**
     * Stops the editing in any cell of the table, recording the new value.
     * revised--&gt;not changed
     */
    public void stopCellEditing() {
        TableCellEditor actualEditor = getCellEditor();
        if (actualEditor != null) {
            actualEditor.stopCellEditing();
        }
    }
    
    
    /**
     * check the value to modify in the table and sets
     */
    @Override public void setValueAt(Object newValue, int row, int col) {
        
        Object oldValue = getValueAt(row, col);
        // The new value has to be transformed to double
        if (!castValue(newValue)) {
            newValue = oldValue;
        }
        
        // Not clear if I have to use equals
        if (oldValue.equals(newValue)) {
            return;
        }
        // When is tablePotential, the value cannot be negative
        if (((Double) newValue) < 0 && !isExactDistrPotential) {
            throw new UnrecoverableException(new MismatchedValueException("a positive number", newValue));
        }
        //if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION)
        if (!isExactDistrPotential) {
            if (lastCol != col) {
                priorityList.clear();
                lastCol = col;
            }
            
        }
        
        // Chance, decision and utility
        
        try {
            TablePotentialValueEdit nodePotentialEdit = new TablePotentialValueEdit(node, (Double) newValue, row, col,
                                                                                    priorityList, getTableModel().getNotEditablePositions());
            
            nodePotentialEdit.executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        } catch (ThereIsNoPotentialsInNodeException e) {
            throw new UnreachableException(e);
        }
        
        // UNCLEAR Should it be here?
        // Sets the value in case of ExactDistrPotential
        if (isExactDistrPotential)
            super.setValueAt(newValue, row, col);
    }
    
    /**
     * Check if newValue is a String or a Double
     *
     * @param newValue - new value to validate
     */
    protected boolean castValue(Object newValue) {
        
        if (newValue instanceof String)
            try {
                Double.parseDouble((String) newValue);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        if (newValue instanceof Double)
            return true;
        return false;
    }
    
    /**
     * @return the variable
     */
    public Variable getVariable() {
        return node.getVariable();
    }
    
    /**
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
    
    /**
     * @return the lastEditableRow
     */
    public int getLastEditableRow() {
        return lastEditableRow;
    }
    
    /**
     * @param lastEditableRow the lastEditableRow to set
     */
    public void setLastEditableRow(int lastEditableRow) {
        this.lastEditableRow = lastEditableRow;
    }
    
    /**
     * @return the firstEditableRow
     */
    public int getFirstEditableRow() {
        return firstEditableRow;
    }
    
    /**
     * @param firstEditableRow the firstEditableRow to set
     */
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
    
    /**
     * @return the usingGeneralPotential
     */
    public boolean isUsingGeneralPotential() {
        return (indexPotential == 0);
    }
    
    /**
     * @return the showingAllParameters
     */
    public boolean isShowingAllParameters() {
        return showingAllParameters;
    }
    
    /**
     * Method to show/hide rows based on the showingAllParameters attribute
     * using a RowFilter mechanism.
     * <ul>
     * <li>If true, the table is shown completely with probabilities values
     * which means that there is no active row filter</li>
     * <li>If not, the row filter is set to show all rows except the one that
     * has the state name equals to the last state name.</li>
     * </ul>
     *
     * @param showingAllParameters if true, show all; if false, show only
     *                             independent parameters
     *                             <p>
     *                             revised--&gt;  minor changes
     */
    public void setShowingAllParameters(boolean showingAllParameters) {
        this.showingAllParameters = showingAllParameters;
        tableRowSorter = new TableRowSorter<ValuesTableModel>(((ValuesTableModel) getModel()));
        if (showingAllParameters) {
            // I suppose variable is != null and has a name
            
            //Bug #280
            /*
             * The commented code creates a RowFilter that filters the rows containing the name of the variable.
             * Is it necessary in some case?
             */
            
            /*
             * RowFilter.regexFilter ("^" + name+ "$", 0) --&gt; Returns a RowFilter that returns anything beginning with the name of the variable why?
             * RowFilter.notFilter --&gt; Returns a RowFilter that includes entries if the supplied filter does not include the entry. That is, returns
             * the entries which does not contain the variable name
             *
             */
            
            //    	if (!isExactDistrPotential)
            //        {
            //            String name = getVariable ().getName ();
            //            if (getVariable ().getTimeSlice () != Integer.MIN_VALUE)
            //            {
            //                //Inserts '\\' before '[' and ']'; name= namenode\\[number\\]
            //            	name = getRegExp (name);
            //            }
            //            if (name.contains ("(") || name.contains (")"))
            //            {
            //            	//Inserts '\\' before '(' and ')';
            //            	name = getRegExpParenthesis (name);
            //            }
            //            if (name.contains ("+"))
            //            {
            //                name = name.replace ("+", "\\+");
            //            }
            //            if (name.contains ("?"))
            //            {
            //                name = name.replace ("?", "\\?");
            //            }
            //
            //            tableRowSorter.setRowFilter (RowFilter.notFilter (RowFilter.regexFilter ("^" + name+ "$", 0)));
            //            this.setRowSorter (tableRowSorter);
            //            this.setRowSorter (null);
            //
            //         }
            //         else
            //         {
            //                this.setRowSorter (null);
            //         }
            this.setRowSorter(null);
        } else {
            int lastRow = getModel().getRowCount() - 1 - 1;
            lastRow = (Math.max(lastRow, 0));
            LinkedList<RowFilter<Object, Object>> list = new LinkedList<RowFilter<Object, Object>>();
            list.add(RowFilter.notFilter(RowFilter.regexFilter((String) getModel().getValueAt(lastRow, 0), 0)));
            list.add(RowFilter.notFilter(RowFilter.regexFilter(getVariable().getName(), 0)));
            tableRowSorter.setRowFilter(RowFilter.andFilter(list));
            this.setRowSorter(tableRowSorter);
        }
    }
    
    /**
     * Gets the regular expression for the temporal node
     *
     * @param name the name of the node
     *
     * @return the regular expression of the name of node. It returns namenode\\[number\\]
     * <p>
     * revised--&gt; not changed
     */
    protected static String getRegExp(String name) {
        int cont1 = name.indexOf('[');
        String s1 = name.substring(0, cont1);
        int cont2 = name.indexOf(']');
        String s2 = name.substring(cont1, cont2);
        String s3 = name.substring(cont2);
        return s1 + "\\" + s2 + "\\" + s3;
    }
    
    /**
     * Gets the regular expression for node names with parenthesis
     *
     * @param name the name of the node
     *
     * @return the regular expression of the name of node. This method returns
     * the same name but substituting '(' and ')' by '\\(' and '\\)'
     */
    protected static String getRegExpParenthesis(String name) {
        if (name.contains("(")) {
            name = name.replace("(", "\\(");
        }
        if (name.contains(")")) {
            name = name.replace(")", "\\)");
        }
        return name;
    }
    
    /**
     * @return the showingProbabilitiesValues
     */
    protected boolean isShowingProbabilitiesValues() {
        return showingProbabilitiesValues;
    }
    
    /**
     * @return the showingTPCvalues
     */
    protected boolean isShowingTPCvalues() {
        return showingTPCvalues;
    }
    
    /**
     * @return the showingOptimal
     */
    public boolean isShowingOptimal() {
        return showingOptimal;
    }
    
    /**
     * @param showingOptimal the showingOptimal to set
     */
    public void setShowingOptimal(boolean showingOptimal) {
        this.showingOptimal = showingOptimal;
    }
    
    /**
     * print the NodePotentialTable
     */
    public void printTable() {
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
    
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (edit instanceof TablePotentialValueEdit tpEdit) {
            tablePotentialValueEditHappened(tpEdit);
        } else if (edit instanceof UncertainValuesEdit uvEdit) {
            uncertainValuesEditHappened(uvEdit);
        }
    }
    
    /**
     * Updates the table if the edited column has uncertainty
     *
     * @param edit the edit
     */
    private void uncertainValuesEditHappened(UncertainValuesEdit edit) {
        int row;
        int positionInValues;
        boolean isChance = edit.isChanceVariable();
        List<Variable> varsPotential = tablePotential.getVariables();
        int numVarsPotential = varsPotential.size();
        int numParents = numVarsPotential - (isChance ? 1 : 0);
        int col = edit.getSelectedColumn();
        TableModel superModel = super.getModel();
        double[] values = tablePotential.getValues();
        int basePosition = edit.getBasePosition();
        if (isChance) {
            int numStates = varsPotential.get(0).getNumStates();
            int startRow = numParents + (numStates - 1);
            for (int i = 0; i < numStates; i++) {
                row = startRow - i;
                positionInValues = basePosition + i;
                superModel.setValueAt(values[positionInValues], row, col);
            }
        } else {
            row = numParents;
            //positionInValues = col - 1;
            positionInValues = basePosition;
            superModel.setValueAt(values[positionInValues], row, col);
        }
    }
    
    /**
     * Sets the values in the edited column
     *
     * @param edit - context for changing the
     */
    public void tablePotentialValueEditHappened(TablePotentialValueEdit edit) {
        int position;
        TablePotential editPotential = edit.getPotential();
        if (!edit.getExactDistrPotential()) {
            priorityList = edit.getPriorityList();
            ListIterator<Integer> listIterator = priorityList.listIterator();
            double[] values = editPotential.getValues();
            while (listIterator.hasNext()) {
                position = listIterator.next();
                int rowPosition = edit.getRowPosition(position);
                int columnPosition = edit.getColumnPosition();
                super.getModel().setValueAt(values[position], rowPosition, columnPosition);
            }
        } else {
            position = edit.getColumnPosition() - 1;
            super.getModel()
                 .setValueAt(editPotential.getValues()[position], edit.getRowPosition(), edit.getColumnPosition());
        }
    }
    
    /**
     *
     */
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof TablePotentialValueEdit tpEdit) {
            TablePotential editPotential = tpEdit.getPotential();
            if (!tpEdit.getExactDistrPotential()) {
                priorityList = tpEdit.getPriorityList();
                for (Integer position : priorityList) {
                    super.getModel().setValueAt(editPotential.getValues()[position], tpEdit.getRowPosition(position),
                                                tpEdit.getColumnPosition());
                }
            } else {
                int position = tpEdit.getColumnPosition() - 1;
                super.getModel()
                     .setValueAt(editPotential.getValues()[position], tpEdit.getRowPosition(), tpEdit.getColumnPosition());
            }
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
     */
    @Override public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        if (isSelectAllForMouseEvent || isSelectAllForActionEvent || isSelectAllForKeyEvent) {
            selectAll(e);
        }
        return result;
    }
    
    /**
     * If the editor that is handling the editing session is not a JTextComponent, the method does nothing
     * If the editor is a JTextComponent then:
     * If e is and instance of KeyEvent, ActionEvent or MouseEvent, the method select all the text of the cell
     *
     * @param e event which provoked the edition and selection
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
    
    /**
     * This method sets the variable probNet to the node probNet
     *
     * @param node the node whose potential is being displayed
     */
    public void setData(Node node) {
        if (this.probNet.getPNESupport() != node.getProbNet().getPNESupport()) {
            this.probNet.getPNESupport().removeListener(this);
            node.getProbNet().getPNESupport().addListener(this);
        }
        this.probNet = node.getProbNet();
    }
    
    /**
     * Closes this object and prepare it for disposal
     */
    public void close() {
        probNet.getPNESupport().removeListener(this);
    }
    
    /**
     * Adjusts columns width to its content
     */
    public void fitColumnsWidthToContent() {
        JTableHeader header = getTableHeader();
        
        TableCellRenderer headerRenderer = null;
        
        if (header != null) {
            headerRenderer = header.getDefaultRenderer();
        }
        
        TableColumnModel columns = getColumnModel();
        TableModel tableModel = getModel();
        int margin = columns.getColumnMargin();
        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        
        for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
            TableColumn column = columns.getColumn(columnIndex);
            column.setMinWidth(60);
            int width = -1;
            
            TableCellRenderer tableCellRenderer = column.getHeaderRenderer();
            
            if (tableCellRenderer == null) {
                tableCellRenderer = headerRenderer;
            }
            
            if (tableCellRenderer != null) {
                Component component = tableCellRenderer
                        .getTableCellRendererComponent(this, column.getHeaderValue(), false, false, -1, columnIndex);
                
                width = component.getPreferredSize().width;
            }
            
            for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
                TableCellRenderer cellRenderer = getCellRenderer(rowIndex, columnIndex);
                
                Component c = cellRenderer
                        .getTableCellRendererComponent(this, tableModel.getValueAt(rowIndex, columnIndex), false, false,
                                                       rowIndex, columnIndex);
                
                width = Math.max(width, c.getPreferredSize().width);
            }
            
            if (width >= 0) {
                column.setMinWidth(width + margin);
            }
        }
    }
}
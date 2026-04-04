/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.*;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * Dialog box to add a finding in a node. The result of using this class is
 * equivalent to a double-click on a node's state when the working mode is
 * 'Inference mode'
 *
 * @author asaez
 * @version 1.0
 */
public class AddFindingDialog extends OkCancelDialog {
    @Serial
    private static final long serialVersionUID = 5618641549380924577L;
    /**
     * Object where the finding will be set.
     */
    protected VisualNode visualNode;
    /**
     * Button group that holds the radio buttons that will be shown. There is a
     * radio button for each state of the node.
     */
    private ButtonGroup buttonGroup = null;
    
    private JSpinner evidenceSpinner;
    
    private final NetworkEditorPanel networkEditorPanel;
    
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    private final Finding finding;
    

    private Finding newFinding;
    private Finding previousFinding;
    
    /**
     * Constructor. initializes the instance.
     *
     * @param owner      window that owns the dialog.
     * @param visualNode the node to which this dialog is associated.
     * @param finding    the assigned finding
     * @param networkEditorPanel
     */
    public AddFindingDialog(Window owner, VisualNode visualNode, Finding finding, NetworkEditorPanel networkEditorPanel) {
        super(owner);
        this.visualNode = visualNode;
        this.finding = finding;
        this.networkEditorPanel = networkEditorPanel;
        initialize();
        setMinimumSize(new Dimension(260, getHeight()));
        int posX = owner.getX() + (owner.getWidth() - this.getWidth()) / 2;
        int posY = owner.getY() + (owner.getHeight() - this.getHeight()) / 2;
        this.setLocation(posX, posY);
        setModal(true);
        setIconImage(null);
        
    }
    
    public ChosenOption requestValues() {
        
        setVisible(true);
        
        return getSelectedOption();
    }
    
    protected void initialize() {
        visualNode.getNode().getProbNet().getPNESupport().setWithUndo(true);
        visualNode.getNode().getProbNet().getPNESupport().openNewSubEditHistory();
        setTitle(stringDatabase.getString("AddFindingDialog.Title"));
        configureComponentsPanel();
        
        
        pack();
        
    }
    
    
    private void configureComponentsPanel() {
        getComponentsPanel().add(getPrincipalPanel());
    }
    
    
    private JPanel getPrincipalPanel() {
        JPanel principalPanel = new JPanel();
        JPanel textPanel = new JPanel();
        this.buttonGroup = new ButtonGroup();
        JPanel radioButtonsPanel = new JPanel();
        
        principalPanel.setLayout(new BorderLayout());
        textPanel.setLayout(new GridLayout(3, 1));
        textPanel.add(new JLabel(""));
        textPanel.add(new JLabel(visualNode.getNode().getName(), SwingConstants.CENTER));
        textPanel.add(new JLabel(""));
        principalPanel.add(textPanel, BorderLayout.NORTH);
        
        Variable variable = visualNode.getNode().getVariable();
        
        if (variable.getVariableType() == VariableType.FINITE_STATES) {
            State[] states = variable.getStates();
            radioButtonsPanel.setLayout(new GridLayout(states.length, 1));
            for (int i = states.length - 1; i >= 0; i--) {
                String stateName = states[i].getName();
                JRadioButton jRadioButton = new JRadioButton(stateName);
                if (finding != null) {
                    jRadioButton.setSelected(finding.getState().equals(stateName));
                    previousFinding = new Finding(variable, new State(stateName));
                }
                radioButtonsPanel.add(jRadioButton);
                jRadioButton.setActionCommand(stateName);
                if (i == 0) {
                    jRadioButton.setSelected(true);
                    previousFinding = new Finding(variable, new State(stateName));
                }
                
                buttonGroup.add(jRadioButton);
                jRadioButton.addActionListener(evt -> newFinding =
                        new Finding(variable, variable.getState((String) getSelectedState())));
            }
            principalPanel.add(radioButtonsPanel, BorderLayout.CENTER);
        } else {
            PartitionedInterval variableDomain = variable.getPartitionedInterval();
            double minValue = (variableDomain.isLeftClosed()) ?
                    variableDomain.getMin() :
                    variableDomain.getMin() + variable.getPrecision();
            double maxValue = (variableDomain.isRightClosed()) ?
                    variableDomain.getMax() :
                    variableDomain.getMax() - variable.getPrecision();
            double defaultValue = (finding != null) ? finding.getNumericalValue() : minValue;
            SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue,
                                                              variable.getPrecision());
            evidenceSpinner = new JSpinner(model);
            evidenceSpinner.setPreferredSize(new Dimension(100, 20));
            JLabel valueLabel = new JLabel("Numeric value:");
            valueLabel.setLabelFor(evidenceSpinner);
            JPanel namelessPanel = new JPanel();
            namelessPanel.add(valueLabel);
            namelessPanel.add(evidenceSpinner);
            principalPanel.add(namelessPanel, BorderLayout.CENTER);
        }
        
        return principalPanel;
    }
    
    public Object getSelectedState() {
        
        VariableType variableType = visualNode.getNode().getVariable().getVariableType();
        Object selectedState;
        
        if (variableType == VariableType.FINITE_STATES) {
            selectedState = buttonGroup.getSelection().getActionCommand();
        } else {
            selectedState = evidenceSpinner.getValue();
        }
        
        
        return selectedState;
    }
    
    public double getEvidenceValue() {
        return Double.parseDouble(evidenceSpinner.getValue().toString());
    }
    
    @Override
    protected boolean doOkClickBeforeHide() throws Exception {
        Variable variable = visualNode.getNode().getVariable();
        if (variable.getVariableType() == VariableType.FINITE_STATES) {
            newFinding = new Finding(variable, variable.getState((String) getSelectedState()));
        } else {
            newFinding = new Finding(variable, (Double) getSelectedState());
        }
        if (!visualNode.isPreResolutionFinding()) {
            networkEditorPanel.getEvidenceManager().setNewFinding(visualNode, null, newFinding, false);
        } else {
            networkEditorPanel.getEvidenceManager().setNewFinding(visualNode, previousFinding, newFinding, false);
        }
        
        
        visualNode.getNode().getProbNet().getPNESupport().closeSubEditHistory();
        
        return super.doOkClickBeforeHide();
    }
    
    @Override protected void doCancelClickBeforeHide() {
        visualNode.getNode().getProbNet().getPNESupport().cancelLastSubEditHistory();
    }
    
}

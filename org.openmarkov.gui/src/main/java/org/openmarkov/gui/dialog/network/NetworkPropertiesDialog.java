/*
 *  Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.util.PropertyNames;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog box to set the options of a network.
 *
 * @author jmendoza
 * @version 1.2 jlgozalo new Group layout and semantic errors fixed
 */
public class NetworkPropertiesDialog extends OkCancelDialog implements PropertyNames {
    private static final long serialVersionUID = -8734100506781534551L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private ProbNet probNet = null;
    /**
     * Panel to tab the different options.
     */
    private JTabbedPane tabbedPane = null;
    /**
     * Panel that contains the panel where definition fields are. It is used to
     * place the fields at the top of the panel.
     */
    private NetworkDefinitionPanel networkDefinitionPanel = null;
    /**
     * Panel that contains the panel where variables definition fields are. It
     * is used to place the fields at the top of the panel.
     */
    private NetworkVariablesPanel networkVariablesPanel = null;
    /**
     * Panel that contains the panel where a set of other additionalProperties
     * are. It is used to place the fields at the top of the panel.
     */
    private NetworkOtherPropertiesPanel networkOtherPropertiesPanel = null;
    /**
     * Panel containing 'Decision Criteria' only for probNet with chance nodes??????????.
     * It is used to place the fields at the top of the panel.
     */
    private NetworkDecisionCriteriaPanel networkDecisionCriteriaPanel = null;
    /**
     * Panel containing 'Agents' for a probNet.
     * It is used to place the fields at the top of the panel.
     */
    private NetworkAgentsPanel networkAgentsPanel;
    /**
     * Panel containing 'temporal Options' for probNet contains the received
     * constraint type. It is used to place the fields at the top of the panel.
     */
    private NetworkTemporalOptionsPanel networkTemporalOptionsPanel;
    
    /**
     * Specifies if the network whose additionalProperties are edited is new.
     */
    private boolean newNetwork = false;
    
    /**
     * This method initialises this instance.
     *
     * @param owner window that owns the dialog.
     */
    public NetworkPropertiesDialog(Window owner) {
        super(owner);
        newNetwork = true;
        initialize();
        setName("NetworkPropertiesDialog");
        setLocationRelativeTo(owner);
    }
    
    /**
     * This method initialises this instance.
     *
     * @param owner   window that owns the dialog.
     * @param probNet network
     */
    public NetworkPropertiesDialog(Window owner, ProbNet probNet) {
        super(owner);
        if (probNet != null) {
            probNet.getPNESupport().setWithUndo(true);
            probNet.getPNESupport().openNewSubEditHistory();
            this.probNet = probNet;
            newNetwork = false;
            initialize();
            setName("NetworkPropertiesDialog");
            setLocationRelativeTo(owner);
        }
        // SsetOnlineHelp("Network Properties Dialog");
    }
    
    /**
     * This method configures the dialog box.
     */
    private void initialize() {
        String title = stringDatabase.getString("NetworkPropertiesDialog.Title");
        if (probNet != null) {
            title += ": " + probNet.getName();
        }
        setTitle(title);
        configureComponentsPanel();
        pack();
    }
    
    /**
     * Sets up the panel where all components, except the buttons of the buttons
     * panel, will be appear.
     */
    private void configureComponentsPanel() {
        getComponentsPanel().add(getTabbedPane());
    }
    
    /**
     * This method initialises tabbedPane.
     *
     * @return a new tabbed pane.
     */
    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            //tabbedPane.addTab("", null, getNetworkDefinitionPanel(), null);
            tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.DefinitionTab"), null, getNetworkDefinitionPanel(), null);
            //tabbedPane.setTitleAt(0, stringDatabase.getString("NetworkPropertiesDialog.DefinitionTab));
            tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.VariablesTab"), null, getNetworkVariablesPanel(), null);
            if (!newNetwork) {
                tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.DecisionCriteriaTab"), null,
                                  getNetworkDecisionCriteriaPanel(), null);
                tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.AgentsTab"), null,
                                  getNetworkAgentsPanel(), null);
                tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.TemporalOptionsTab"), null,
                                  getNetworkTemporalOptionsPanel(), null);
                tabbedPane.addTab(stringDatabase.getString("NetworkPropertiesDialog.OtherPropertiesTab"), null,
                                  getNetworkOtherPropertiesPanel(), null);
            }
            tabbedPane.setName("tabbedPane");
        }
        return tabbedPane;
    }
    
    /**
     * Initialising NetworkDecisionCriteriaPanel.
     *
     * @return a new Decision criteria panel.
     */
    private NetworkDecisionCriteriaPanel getNetworkDecisionCriteriaPanel() {
        if (networkDecisionCriteriaPanel == null) {
            networkDecisionCriteriaPanel = new NetworkDecisionCriteriaPanel(this, probNet);
            networkDecisionCriteriaPanel.setName("networkDecisionCriteriaPanel");
        }
        return networkDecisionCriteriaPanel;
    }
    
    /**
     * Initialising NetworkAgentsPanel.
     *
     * @return a new Agents panel.
     */
    private NetworkAgentsPanel getNetworkAgentsPanel() {
        if (networkAgentsPanel == null) {
            networkAgentsPanel = new NetworkAgentsPanel(probNet);
            networkAgentsPanel.setName("networkAgentsPanel");
        }
        return networkAgentsPanel;
    }
    
    /**
     * Initialising NetworkTemporalOptionsPanel.
     *
     * @return a new Temporal options panel.
     */
    private NetworkTemporalOptionsPanel getNetworkTemporalOptionsPanel() {
        if (networkTemporalOptionsPanel == null) {
            networkTemporalOptionsPanel = new NetworkTemporalOptionsPanel(probNet);
            networkTemporalOptionsPanel.setName("networkTemporalOptionsPanel");
        }
        return networkTemporalOptionsPanel;
    }
    
    /**
     * This method initialises networkDefinitionPanel.
     *
     * @return a new definition panel.
     */
    private NetworkDefinitionPanel getNetworkDefinitionPanel() {
        if (networkDefinitionPanel == null) {
            networkDefinitionPanel = new NetworkDefinitionPanel(this, probNet);
            networkDefinitionPanel.setName("networkDefinitionPanel");
        }
        return networkDefinitionPanel;
    }
    
    /**
     * This method initialises networkVariablesPanel.
     *
     * @return a new variables definition panel.
     */
    private NetworkVariablesPanel getNetworkVariablesPanel() {
        if (networkVariablesPanel == null) {
            networkVariablesPanel = new NetworkVariablesPanel(probNet);
            networkVariablesPanel.setName("networkVariablesPanel");
        }
        return networkVariablesPanel;
    }
    
    /**
     * This method initialises networkOtherPropertiesPanel.
     *
     * @return a new other additionalProperties panel.
     */
    private NetworkOtherPropertiesPanel getNetworkOtherPropertiesPanel() {
        if (networkOtherPropertiesPanel == null) {
            networkOtherPropertiesPanel = new NetworkOtherPropertiesPanel(newNetwork);
            networkOtherPropertiesPanel.setName("networkOtherPropertiesPanel");
            if (probNet != null) {
                networkOtherPropertiesPanel.setProbNetProperties(probNet);
            }
        }
        return networkOtherPropertiesPanel;
    }
    
    /**
     * This method carries out the actions when the user press the Ok button
     * before hide the dialog.
     *
     * @return true always
     */
    @Override protected boolean doOkClickBeforeHide() {
        if (newNetwork) {
            // TODO Create probNet instance
            NetworkType networkType = getNetworkDefinitionPanel().getNetworkType();
            probNet = new ProbNet(networkType);
            probNet.setComment(getNetworkDefinitionPanel().getNetworkComment());
            probNet.setShowCommentWhenOpening(getNetworkDefinitionPanel().getShowComment());
            probNet.setDefaultStates(getNetworkVariablesPanel().getDefaultStates());
        } else {
            probNet.setShowCommentWhenOpening(getNetworkDefinitionPanel().getShowComment());
            probNet.getPNESupport().closeSubEditHistory();
        }
        return NetworkDefinitionPanel.checkName();
    }
    
    // ESCA-JAVA0025: allows an empty method to override another one
    
    /**
     * This method carries out the actions when the user press the Cancel button
     * before hide the dialog.
     */
    @Override protected void doCancelClickBeforeHide() {
        if (!newNetwork) {
            probNet.getPNESupport().cancelLastSubEditHistory();
            //Should cancel instead
        }
    }
    
    /**
     * This method shows the dialog and requests the user the network
     * additionalProperties.
     *
     * @return OK_BUTTON if the user has pressed the 'OK' button or
     * CANCEL_BUTTON if the user has pressed the 'Cancel' button.
     */
    public ChosenOption showProperties() {
        setVisible(true);
        return getSelectedOption();
    }
    
    /**
     * Returns the probNet.
     *
     * @return the probNet.
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    /**
     * Updates the features of a probNet when it´s set in the definition panel
     *
     * @param probNet the prob net
     */
    public void update(ProbNet probNet) {
        getNetworkDecisionCriteriaPanel().update(probNet);
        getNetworkAgentsPanel().update(probNet);
        getNetworkTemporalOptionsPanel().update(probNet);
    }
    
}
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.openmarkov.core.action.core.*;
import org.openmarkov.core.exception.*;

import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.temporalevolution.TemporalEvolutionDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.dialog.node.*;
import org.openmarkov.gui.exception.*;
import org.openmarkov.gui.graphic.*;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.NetworkPanel;
import org.openmarkov.gui.window.edition.ZoomManager;
import org.openmarkov.gui.window.edition.mode.EditionMode;
import org.openmarkov.gui.window.edition.mode.EditionModeManager;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEExpectedUtilityDecision;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.List;

/**
 * This class implements the behaviour of a panel where a network will be
 * edited.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.3 - asaez - Functionality added: - Explanation capabilities, -
 * Management of working modes (edition/inference), - Expansion and
 * contraction of nodes, - Introduction and elimination of evidence -
 * Management of multiple evidence cases.
 */
public final class NetworkEditorPanel extends JPanel {
    /**
     * Static field for serializable class.
     */
    @Serial
    private static final long serialVersionUID = 2789011585460326400L;
    /**
     * Maximum width of the panel.
     */
    private static final double MAX_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
    /**
     * Maximum height of the panel.
     */
    private static final double MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
    /**
     * Constant that indicates the value of the Expansion Threshold by default.
     */
    // This should be in a future a configuration option that should be read on
    // start
    private static final int DEFAULT_THRESHOLD_VALUE = 5;
    
    
    private final EditorInputHandler editorInputHandler;
    private final EvidenceManager evidenceManager;
    private final InferencePresenter inferencePresenter;
    
    /**
     * Object to convert coordinates of the screen to the panel and vice versa.
     */
    private final ZoomManager zoomManager;
    /**
     * Visual representation of the network
     */
    private final VisualNetwork visualNetwork;
    /**
     * Maximum width of the panel.
     */
    private double currentWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
    /**
     * Maximum height of the panel.
     */
    private double currentHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
    /**
     * Current edition mode.
     */
    private EditionMode editionMode = null;
    /**
     * This variable indicates which is the expansion threshold of the network
     */
    private double currentExpansionThreshold = NetworkEditorPanel.DEFAULT_THRESHOLD_VALUE;
    /**
     * Network panel associated to this editor panel
     */
    private final NetworkPanel networkPanel;

    /**
     * This variable indicates if the propagation mode is automatic or manual.
     */
    private boolean automaticPropagation;
    /**
     * This variable indicates if propagation should be done right now (if being
     * in Inference Mode).
     */
    private boolean propagationActive;
    
    
    private final EditionModeManager editionModeManager;
    
    /**
     * Constructor that creates the instance.
     *
     * @param networkPanel network that will be edited.
     */
    public NetworkEditorPanel(NetworkPanel networkPanel, VisualNetwork visualNetwork) {
        this.zoomManager = new ZoomManager();
        this.networkPanel = networkPanel;
        this.visualNetwork = visualNetwork;
        this.evidenceManager = new EvidenceManager(this);
        this.visualNetwork.getProbNet().getPNESupport().addListener(new PNEditEventHandler(this));
        this.automaticPropagation = true;
        this.propagationActive = true;
        this.editorInputHandler = new EditorInputHandler(this);
        this.addMouseListener(this.editorInputHandler);
        this.addMouseMotionListener(this.editorInputHandler);
        this.addKeyListener(this.editorInputHandler);
        this.setZoomToFitNetwork();
        this.editionModeManager = new EditionModeManager(this, this.visualNetwork.getProbNet());
        this.editionMode = this.editionModeManager.getDefaultEditionMode();
        this.inferencePresenter = new InferencePresenter(this);
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        switch (LocalPreferences.PREFERRED_THEME.get()) {
            case SYSTEM, LIGHT -> this.setBackground(Color.WHITE);
            case DARK -> {
                //setBackground(Color.GRAY);
            }
        }
    }
    
    public double getCurrentWidth() {
        return this.currentWidth;
    }
    
    public double getCurrentHeight() {
        return this.currentHeight;
    }
    
    /**
     * Changes the presentation mode of the text of the nodes.
     *
     * @param value new value of the presentation mode of the text of the nodes.
     */
    public void setByTitle(boolean value) {
        this.visualNetwork.setByTitle(value);
        readjustAndRepaint();
    }
    
    /**
     * Overwrite 'paint' method to avoid to call it explicitly.
     *
     * @param g the graphics context in which to paint.
     */
    @Override public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        super.paint(g);
        g2D.scale(this.zoomManager.getZoom(), this.zoomManager.getZoom());
        this.visualNetwork.paint(g2D);
    }
    
    /**
     * Changes the state of the edition and carries out the necessary actions in
     * each case.
     *
     * @param newEditionModeName new edition mode state.
     */
    public void setEditionMode(String newEditionModeName) {
        EditionMode newEditionMode = this.editionModeManager.getEditionMode(newEditionModeName);
        if (!this.editionMode.equals(newEditionMode)) {
            this.setCursor(this.editionModeManager.getCursor(newEditionModeName));
            this.visualNetwork.setSelectedAllObjects(false);
            this.editionMode = newEditionMode;
            this.repaint();
        }
    }
    
    /**
     * Selects all nodes and links.
     */
    public void selectAllObjects() {
        this.visualNetwork.setSelectedAllObjects(true);
        this.repaint();
    }
    
    
    /**
     * Return the height of the panel after applying the zoomManager.
     *
     * @return height of the panel after applying the zoomManager.
     */
    private double getNewHeight() {
        return this.zoomManager.panelToScreen(this.currentHeight);
    }
    
    /**
     * Return the width of the panel after applying the zoomManager.
     *
     * @return width of the panel after applying the zoomManager.
     */
    private double getNewWidth() {
        return this.zoomManager.panelToScreen(this.currentWidth);
    }
    
    /**
     * Returns the value of the zoomManager.
     *
     * @return actual value of zoomManager.
     */
    public double getZoom() {
        return this.zoomManager.getZoom();
    }
    
    /**
     * Changes the value of the zoomManager.
     *
     * @param value new zoomManager.
     */
    public void setZoom(double value) {
        if (Double.compare(this.zoomManager.getZoom(), value) != 0) { // jlgozalo. 24/08 fix condition to !=
            this.zoomManager.setZoom(value);
            Dimension newDimension = new Dimension((int) Math.round(this.getNewWidth()), (int) Math.round(this.getNewHeight()));
            this.setPreferredSize(newDimension);
            this.setSize(newDimension);
            readjustAndRepaint();
        }
    }
    
    /**
     * Sets a new contextual menu factory.
     *
     * @param newContextualMenuFactory contextual menu factory to be set.
     */
    public void setContextualMenuFactory(ContextualMenuFactory newContextualMenuFactory) {
        this.editorInputHandler.setContextualMenuFactory(newContextualMenuFactory);
    }
    
    
    public Node getSelectedNode() {
        List<VisualNode> selectedNodes = this.visualNetwork.getSelectedNodes();
        if (selectedNodes.size() != 1) { // This never happens
            throw new UnreachableException(new NoSelectedNodeException(this.visualNetwork));
        }
        return selectedNodes.getFirst().getNode();
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of a node.
     * If some property has changed, insert a new undo point into the network
     * undo manager.
     *
     * @param selectedNode the selected node
     * @param newNode the new node
     *
     * @return the result
     */
    boolean changeNodeProperties(VisualNode selectedNode, boolean newNode) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        boolean userAcceptedChanges = NetworkEditorPanel.requestNodePropertiesToUser2(GUIUtils.getOwner(this), selectedNode.getNode(), newNode);
        if (userAcceptedChanges) {
            this.adjustPanelDimension();
            selectedNode.update(this.evidenceManager.getPostResolutionEvidence().size());
            this.repaint();
            this.evidenceManager.removeNodeEvidenceInAllCases(selectedNode.getNode());
        }
        return userAcceptedChanges;
    }
    
    public void changeNodeProperties() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        List<VisualNode> selectedNodes = this.visualNetwork.getSelectedNodes();
        if (selectedNodes.size() == 1) {
            this.changeNodeProperties(selectedNodes.getFirst(), false);
        }
    }
    
    public void showPotentialDialog(boolean readOnly) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, ConstraintViolatedException {
        List<VisualNode> selectedNodes = this.visualNetwork.getSelectedNodes();
        Node node = selectedNodes.getFirst().getNode();
        if (this.requestPotentialValues(GUIUtils.getOwner(this), node, readOnly)) {
            // if the user has selected the ok button when closing the dialog
            readjustAndRepaint();
            this.evidenceManager.removeNodeEvidenceInAllCases(node);
        }
    }
    
    /**
     * This method requests to the user the additionalProperties of a node.
     *
     * @param owner   owner window that shows the dialog box.
     * @param node    object that contains the additionalProperties of the node
     *                and where changes will be saved.
     * @param newNode specifies if the node whose additionalProperties are going
     *                to be edited is new.
     *
     * @return true, if the user save the changes on node; otherwise, false.
     */
    private static boolean requestNodePropertiesToUser2(Window owner, Node node, boolean newNode) {
        NodePropertiesDialog nodePropertiesDialog = new CommonNodePropertiesDialog(owner, node, newNode);
        if (owner instanceof MainGUI gui) {
            gui.freeze();
        }
        boolean result = nodePropertiesDialog.requestProperties() == OkCancelDialog.ChosenOption.Ok;
        if (owner instanceof MainGUI gui) {
            gui.unfreeze();
        }
        return result;
    }
    
    private boolean requestPotentialValues(Window owner, Node node, boolean readOnly) {
        /**
         * Object Dialog for potentials edition
         */
        PotentialEditDialog potentialsDialog = new PotentialEditDialog(owner, node, readOnly);
        this.visualNetwork.cancelLinkCreation(this);
        return (
                potentialsDialog.requestValues()// to know if the user has
                        // selected the ok button when
                        // closing the dialog
                        == OkCancelDialog.ChosenOption.Ok
        );
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of a link.
     * If some property has changed, insert a new undo point into the network
     * undo manager.
     *
     * @param link the link
     */
    void changeLinkProperties(VisualLink link) {
        /*
         * This method must be implemented to activate the possibility of
         * editing the additionalProperties of a link in future versions.
         */
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of the
     * network. If some property has changed, insert a new undo point into the
     * network undo manager.
     */
    public void changeNetworkProperties() {
        // TODO be careful with local pNESupport and extern pNESupport
        Window owner = GUIUtils.getOwner(this);
        NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(owner, this.visualNetwork.getProbNet());
        dialogProperties.showProperties();
    }
    
    /**
     * This method imposes a policy in a decision node.
     */
    public void imposePolicyInNode() {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualDecisionNode visualNode = (VisualDecisionNode) selectedNode.getFirst();
            NetworkEditorPanel.requestImposePolicyValues(GUIUtils.getOwner(this), visualNode);
        }
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method edits an imposed policy of a decision node.
     */
    public void editNodePolicy() {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualDecisionNode visualNode = (VisualDecisionNode) selectedNode.getFirst();
            if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
                // TODO manage other kind of policy types from the interface
                NetworkEditorPanel.requestImposePolicyValues(GUIUtils.getOwner(this), visualNode);
            }
        }
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method removes an imposed policy from a decision node.
     */
    public void removePolicyFromNode() throws DoEditException {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualNode visualNode = selectedNode.getFirst();
            if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
                try {
                    new RemovePolicyEdit(visualNode.getNode()).executeEdit();
                } catch (ConstraintViolatedException e) {
                    throw new UnreachableException(e);
                }
            }
        }
        //setNetworkChangedWithOutEdit(true);
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    private static void requestImposePolicyValues(Window owner, VisualDecisionNode visualNode) {
        PotentialEditDialog imposePolicyDialog = new ImposePolicyDialog(owner, visualNode);
        imposePolicyDialog.setTitle("ImposePolicydialog.Title");
        imposePolicyDialog.requestValues();
    }
    
    
    /**
     * This method shows the expected utility of a decision node.
     */
    public void showExpectedUtilityOfNode() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, ConstraintViolatedException {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualNode visualNode = selectedNode.getFirst();
            Node node = visualNode.getNode();
            VEExpectedUtilityDecision veExpectedUtilityDecision
                    = new VEExpectedUtilityDecision(this.visualNetwork.getProbNet(), node.getVariable());
            Potential expectedUtility = veExpectedUtilityDecision.getExpectedUtility();
            Node dummyNode = new Node(new ProbNet(), node.getVariable(), node.getNodeType());
            dummyNode.setPotential(expectedUtility);
            PotentialEditDialog expectedUtilityDialog = new PotentialEditDialog(GUIUtils.getOwner(this), dummyNode, true);
            expectedUtilityDialog.setTitle("ExpectedUtilityDialog.Title");
            expectedUtilityDialog.requestValues();
        }
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method shows the optimal policy for a decision node.
     */
    public void showOptimalPolicyOfNode() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, ConstraintViolatedException {
        List<VisualNode> selectedNodes = this.visualNetwork.getSelectedNodes();
        if (selectedNodes.size() == 1) {
            VisualNode visualNode = selectedNodes.getFirst();
            ProbNet dummyProbNet = new ProbNet();
            OptimalPolicies veOptimalPolicy = new VEEvaluation(this.visualNetwork.getProbNet());
            Potential optimalPolicy = veOptimalPolicy.getOptimalPolicy(visualNode.getNode().getVariable());
            dummyProbNet.addPotential(optimalPolicy);
            Variable conditionedVariable = optimalPolicy.getVariable(0);
            Node dummyNode = dummyProbNet.getNode(conditionedVariable);
            dummyNode.setNodeType(NodeType.DECISION);
            dummyNode.setPolicyType(PolicyType.OPTIMAL);
            for (Variable variable : optimalPolicy.getVariables()) {
                if (variable.equals(conditionedVariable)) {
                    continue;
                }
                dummyProbNet.addLink(variable, conditionedVariable, true);
            }
            PotentialEditDialog optimalPolicyDialog =
                    new PotentialEditDialog(GUIUtils.getOwner(this), dummyNode, true);
            optimalPolicyDialog.setTitle("OptimalPolicyDialog.Title");
            optimalPolicyDialog.requestValues();
        }
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    
    /**
     * This method returns true if propagation type currently set is automatic;
     * false if manual.
     *
     * @return true if the current propagation type is automatic.
     */
    public boolean isAutomaticPropagation() {
        return this.automaticPropagation;
    }
    
    /**
     * This method sets the current propagation type.
     *
     * @param automaticPropagation new value of the propagation type.
     */
    public void setAutomaticPropagation(boolean automaticPropagation) {
        this.automaticPropagation = automaticPropagation;
    }
    
    /**
     * This method returns the propagation status: true if propagation should be
     * done right now; false otherwise.
     *
     * @return true if propagation should be done right now.
     */
    public boolean isPropagationActive() {
        return this.propagationActive;
    }
    
    /**
     * This method sets the propagation status.
     *
     * @param propagationActive new value of the propagation status.
     */
    public void setPropagationActive(boolean propagationActive) {
        this.propagationActive = propagationActive;
        this.visualNetwork.setPropagationActive(propagationActive);
    }
    
    /**
     * This method returns the associated network panel.
     *
     * @return the associated network panel.
     */
    public NetworkPanel getNetworkPanel() {
        return this.networkPanel;
    }
    
    /**
     * This method returns the current expansion threshold.
     *
     * @return the value of the current expansion threshold.
     */
    public double getExpansionThreshold() {
        return this.currentExpansionThreshold;
    }
    
    /**
     * This method changes the current expansion threshold.
     *
     * @param expansionThreshold new value of the expansion threshold.
     */
    public void setExpansionThreshold(double expansionThreshold) {
        this.currentExpansionThreshold = expansionThreshold;
    }
    
    /**
     * This method updates the expansion state (expanded/contracted) of the
     * nodes. It is used in transitions from edition to inference mode and vice
     * versa, and also when the user modifies the current expansion threshold in
     * the Inference tool bar
     *
     * @param newWorkingMode new value of the working mode.
     */
    public void updateNodesExpansionState(NetworkPanel.WorkingMode newWorkingMode) {
        switch (newWorkingMode) {
            case EDITION -> {
                List<VisualNode> allNodes = this.visualNetwork.getAllNodes();
                if (!allNodes.isEmpty()) {
                    for (VisualNode visualNode : allNodes) {
                        if (visualNode.isExpanded()) {
                            visualNode.setExpanded(false);
                        }
                    }
                    this.repaint();
                }
            }
            case INFERENCE -> {
                List<VisualNode> allNodes = this.visualNetwork.getAllNodes();
                if (!allNodes.isEmpty()) {
                    for (VisualNode visualNode : allNodes) {
                        visualNode.setExpanded(visualNode.getNode().getRelevance() >= this.currentExpansionThreshold);
                    }
                    this.repaint();
                }
            }
        }
    }
    
    public void temporalEvolution() {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualNode node = selectedNode.getFirst();
            new TemporalEvolutionDialog(GUIUtils.getOwner(this), node.getNode(), this.evidenceManager.getPreResolutionEvidence());
            this.visualNetwork.setSelectedAllNodes(false);
            this.repaint();
            // TODO - Change code
        } else if (selectedNode.isEmpty()) {
            new TemporalEvolutionDialog(GUIUtils.getOwner(this), this.networkPanel
                    .getProbNet(), this.evidenceManager.getPreResolutionEvidence());
        }
    }
    
    
    /**
     * This method updates all visual states of all visual nodes when it is
     * needed for a navigation operation among the existing evidence cases, a
     * creation of a new case or when all cases are cleared out.
     *
     * @param option the specific operation to be done over the visual states.
     */
    public void updateAllVisualStates(String option, int caseNumber) {
        List<VisualNode> allVisualNodes = this.visualNetwork.getAllNodes();
        for (VisualNode visualNode : allVisualNodes) {
            InnerBox innerBox = visualNode.getInnerBox();
            VisualState visualState = null;
            for (int i = 0; i < innerBox.getNumStates(); i++) {
                if (innerBox instanceof FSVariableBox) {
                    visualState = ((FSVariableBox) innerBox).getVisualState(i);
                } else if (innerBox instanceof NumericVariableBox) {
                    visualState = ((NumericVariableBox) innerBox).getVisualState();
                }
                if ("new".equals(option)) {
                    visualState.createNewStateValue();
                } else if ("clear".equals(option)) {
                    visualState.clearAllStateValues();
                }
                visualState.setCurrentStateValue(caseNumber);
            }
        }
        this.repaint();
    }
    
    /**
     * Returns the visualNetwork.
     *
     * @return the visualNetwork.
     */
    public VisualNetwork getVisualNetwork() {
        return this.visualNetwork;
    }
    
    /**
     * Sets workingMode
     *
     * @param newWorkingMode the new working mode
     */
    public void setWorkingMode(NetworkPanel.WorkingMode newWorkingMode) {
        this.visualNetwork.setWorkingMode(newWorkingMode);
        if (newWorkingMode == NetworkPanel.WorkingMode.INFERENCE) {
            this.editionMode = this.editionModeManager.getDefaultEditionMode();
            this.setCursor(this.editionModeManager.getDefaultCursor());
        }
    }
    
    /**
     * If the dimensions of the network are greater than the dimensions of the
     * panel, changes the dimensions of the panel in order to accommodate the
     * whole network.
     */
    public void adjustPanelDimension() {
        double[] bounds = this.visualNetwork.getNetworkBounds((Graphics2D) this.getGraphics());
        this.currentWidth = Math.min(NetworkEditorPanel.MAX_WIDTH, bounds[1]);
        this.currentHeight = Math.min(NetworkEditorPanel.MAX_HEIGHT, bounds[3]);
        Dimension newDimension = new Dimension((int) Math.round(this.getNewWidth()), (int) Math.round(this.getNewHeight()));
        this.setPreferredSize(newDimension);
        this.setSize(newDimension);
    }
    
    /**
     * Sets the zoomManager so the displayed network fits in the panel.
     */
    private void setZoomToFitNetwork() {
        double[] networkBounds = this.visualNetwork.getNetworkBounds((Graphics2D) this.getGraphics());
        Dimension panelBounds = this.networkPanel.getMainPanel().getNetworksTabPanel().getSize();
        double zoom = 1;
        
        while (((networkBounds[1] * zoom) > panelBounds.getWidth())
                || ((networkBounds[3] * zoom) > panelBounds.getHeight()) && zoom > 0.1) {
            zoom -= 0.1;
        }
        this.setZoom(zoom);
    }
    
    // The key listener needs a focusable object to listen
    @Override
    public boolean isFocusable() {
        return true;
    }
    
    public EvidenceManager getEvidenceManager() {
        return this.evidenceManager;
    }
    
    public void readjustAndRepaint() {
        this.adjustPanelDimension();
        this.revalidate();
        this.repaint();
    }
    
    public InferencePresenter getInferencePresenter() {
        return inferencePresenter;
    }
    
    public ZoomManager getZoomManager() {
        return this.zoomManager;
    }
    
    public EditionMode getEditionMode() {
        return this.editionMode;
    }
    
    public void updateName(String baseName) {
        setName("NetworkEditorOf"+baseName);
    }
}

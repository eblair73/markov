/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

import org.openmarkov.core.action.base.*;
import org.openmarkov.core.action.core.AbsorbNodeEdit;
import org.openmarkov.core.action.core.AbsorbParentsEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.exception.PreResolutionNodeInInferenceException;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.MainPanelMenuAssistant;
import org.openmarkov.gui.window.ZoomableContentPanel;
import org.openmarkov.gui.window.decisiontree.DecisionTreeWindow;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// ESCA-JAVA0136: allows more than 30 methods in the class

/**
 * This class implements a panel where is added a scroll panel and into this one
 * a network panel.
 *
 * @author jmendoza
 * @version 1.4 - jrico: Now it also has a reference to its DecisionTreeWindow (if shown).
 */
public class NetworkPanel extends ZoomableContentPanel implements PNEditListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 6804988702698858496L;
    /**
     * Network that is edited.
     */
    public final ProbNet probNet;
    /**
     * Panel where the network is painted.
     */
    private final NetworkEditorPanel networkEditorPanel;
    /**
     * Application main
     */
    private final MainPanel mainPanel;
    /**
     * Name of the file where the network is saved (updated or not).
     */
    private String networkFile = null;
    
    
    private JScrollPane editorScrollPanel;
    
    /**
     * Indicates if the network has been modified.
     */
    private boolean modified = false;
    /**
     * This variable indicates in which mode is the network currently working It
     * is initially set to Edition Mode
     */
    private WorkingMode workingMode = WorkingMode.EDITION;
    
    private final ArrayList<DecisionTreeWindow> decisionTreeWindows;
    
    public enum WorkingMode {
        EDITION, INFERENCE
    }
    
    private final List<Consumer<NetworkPanel>> onModificationListener;
    
    /**
     * Constructor that creates the instance.
     *
     * @param probNet   network that will be edited.
     * @param mainPanel application main panel.
     */
    public NetworkPanel(ProbNet probNet, MainPanel mainPanel) {
        this.probNet = probNet;
        this.mainPanel = mainPanel;
        this.onModificationListener = new ArrayList<>();
        probNet.getPNESupport().addListener(this);
        this.networkEditorPanel = new NetworkEditorPanel(this, new VisualNetwork(probNet, this.mainPanel.mainGUI));
        initialize();
        decisionTreeWindows = new ArrayList<>();
    }
    
    /**
     * This method initialises this instance.
     */
    private void initialize() {
        setLayout(new BorderLayout());
        // JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // getNetworkScrollPanel().setSize(new Dimension (300,300));
        // splitPane.setTopComponent(getNetworkScrollPanel());
        // splitPane.setBottomComponent(getPropertiesScrollPanel());
        
        this.editorScrollPanel = new JScrollPane();
        editorScrollPanel.setViewportView(networkEditorPanel);
        editorScrollPanel.getVerticalScrollBar().setUnitIncrement(25);
        add(editorScrollPanel);
    }
    
    /**
     * This method initializes networkEditorPanel.
     *
     * @return a new editor panel.
     */
    public NetworkEditorPanel getEditorPanel() {
        return networkEditorPanel;
    }
    
    /**
     * Returns the network which is edited.
     *
     * @return network which is edited.
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    /**
     * Returns the application main panel.
     *
     * @return the application main panel.
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Returns the modification state of the network.
     *
     * @return true if the network has been modified; otherwise, false.
     */
    public boolean getModified() {
        return modified;
    }
    
    public JScrollPane getEditorScrollPanel() {
        return this.editorScrollPanel;
    }
    
    public void addOnModification(Consumer<NetworkPanel> action) {
        this.onModificationListener.add(action);
    }
    
    /**
     * Sets the modification state of the network to a new value.
     *
     * @param value new value of the modification state of the network.
     */
    public void setModified(boolean value) {
        modified = value;
        for (Consumer<NetworkPanel> onModification : this.onModificationListener) {
            onModification.accept(this);
        }
    }
    
    /**
     * Returns the name of the file where the network is saved.
     *
     * @return a string that contains the name of the file.
     */
    public String getNetworkFile() {
        return networkFile;
    }
    
    
    /**
     * Sets the name of the file where the network is saved.
     *
     * @param name name of the file.
     */
    public void setNetworkFile(String name) {
        networkFile = name;
    }
    
    /**
     * Changes the state of the edition and carries out the necessary actions in
     * each case.
     *
     * @param newEditionMode new edition state.
     */
    public void setEditionMode(String newEditionMode) {
        networkEditorPanel.setEditionMode(newEditionMode);
    }
    
    /**
     * Returns the current working mode.
     *
     * @return the value of the current working mode (Edition or Inference).
     */
    public WorkingMode getWorkingMode() {
        return workingMode;
    }
    
    /**
     * Changes the current working mode.
     *
     * @param workingMode new value of the working mode.
     */
    public void setWorkingMode(WorkingMode workingMode) {
        this.workingMode = workingMode;
        networkEditorPanel.setWorkingMode(workingMode);
    }
    
    /**
     * Returns the current expansion threshold.
     *
     * @return the value of the current expansion threshold.
     */
    public double getExpansionThreshold() {
        return networkEditorPanel.getExpansionThreshold();
    }
    
    /**
     * Changes the current expansion threshold.
     *
     * @param expansionThreshold new value of the expansion threshold.
     */
    public void setExpansionThreshold(double expansionThreshold) {
        networkEditorPanel.setExpansionThreshold(expansionThreshold);
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of the
     * network. If some property has changed, insert a new undo point into the
     * network undo manager.
     */
    public void changeNetworkProperties() {
        networkEditorPanel.changeNetworkProperties();
    }
    
    /**
     * This method absorbs a node into the rest of the net arc-reversal style. This means updating the only utility
     * child it might have and removing it next.
     */
    public void absorbNode() throws DoEditException {
        Node node = networkEditorPanel.getSelectedNode();
        new AbsorbNodeEdit(networkEditorPanel.getVisualNetwork().getProbNet(), node.getVariable()).executeEdit();
    }
    
    /**
     * This method absorbs intermediate utility nodes.
     */
    public void absorbParents() throws DoEditException {
        Node node = networkEditorPanel.getSelectedNode();
        new AbsorbParentsEdit(networkEditorPanel.getVisualNetwork().getProbNet(), node).executeEdit();
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of a node.
     * If some property has changed, insert a new undo point into the network
     * undo manager.
     */
    public void changeNodeProperties() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.changeNodeProperties();
    }
    
    /**
     * This method has been created for testing.
     */
    public void changePotential() throws IncompatibleEvidenceException, ThereIsNoPotentialsInNodeException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, ConstraintViolatedException {
        networkEditorPanel.showPotentialDialog(workingMode != WorkingMode.EDITION);
    }
    
    /**
     * This method manage the temporal evolution of a variable.
     */
    public void temporalEvolution() {
        networkEditorPanel.temporalEvolution();
    }
    
    /**
     * This method imposes a policy in a decision node.
     */
    public void imposePolicyInNode() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException, NotEnoughMemoryException {
        networkEditorPanel.imposePolicyInNode();
    }
    
    /**
     * This method edits an imposed policy of a decision node.
     */
    public void editNodePolicy() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException, NotEnoughMemoryException {
        networkEditorPanel.editNodePolicy();
    }
    
    /**
     * This method removes an imposed policy from a decision node.
     */
    public void removePolicyFromNode() throws DoEditException {
        networkEditorPanel.removePolicyFromNode();
    }
    
    /**
     * This method shows the expected utility of a decision node.
     */
    public void showExpectedUtilityOfNode()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther,
            NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork,
            NotEvaluableNetworkException.UnsatisfiedConstraints, ThereIsNoPotentialsInNodeException, NotEnoughMemoryException, ConstraintViolatedException {
        networkEditorPanel.showExpectedUtilityOfNode();
    }
    
    /**
     * This method shows the optimal policy for a decision node.
     */
    public void showOptimalPolicyOfNode()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther,
            NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork,
            NotEvaluableNetworkException.UnsatisfiedConstraints,
            ThereIsNoPotentialsInNodeException, NotEnoughMemoryException, ConstraintViolatedException {
        networkEditorPanel.showOptimalPolicyOfNode();
    }
    
    /**
     * This method adds a finding in a node.
     */
    public void addFinding() {
        networkEditorPanel.getEvidenceManager().addFinding();
    }
    
    /**
     * This method removes findings from selected nodes.
     */
    public void removeFinding() throws PreResolutionNodeInInferenceException, DoEditException {
        networkEditorPanel.getEvidenceManager().removeFinding();
    }
    
    /**
     * This method updates the expansion state (expanded/contracted) of the
     * nodes. It is used in transitions from edition to inference mode and vice
     * versa, and also when the user modifies the current expansion threshold in
     * the Inference tool bar
     */
    public void updateNodesExpansionState(WorkingMode newWorkingMode) {
        networkEditorPanel.updateNodesExpansionState(newWorkingMode);
    }
    
    /**
     * This method updates the value of each state for each node in the network
     * with the current individual probabilities.
     */
    public void updateIndividualProbabilitiesAndUtilities() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().updateIndividualProbabilitiesAndUtilities();
    }
    
    /**
     * This method removes all the findings established in the current evidence
     * case.
     */
    public void removeAllFindings() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().removeAllFindings();
    }
    
    /**
     * This method returns true if there are any finding in the current evidence
     * case.
     *
     * @return true if the current evidence case has at least one finding.
     */
    public boolean areThereFindingsInCase() {
        return networkEditorPanel.getEvidenceManager().areThereFindingsInCase();
    }
    
    /**
     * This method copies the selected nodes to the clipboard.
     *
     * @param cut if true, the nodes copied to the clipboard are also removed.
     */
    public void exportToClipboard(boolean cut) {
        networkEditorPanel.getVisualNetwork().exportToClipboard(cut);
    }
    
    /**
     * This method imports various nodes from the clipboard and creates them in
     * the network.
     */
    public void pasteFromClipboard() throws DoEditException {
        networkEditorPanel.getVisualNetwork().pasteFromClipboard();
    }
    
    /**
     * This method says if there is data stored in the clipboard.
     *
     * @return true if there is data stored in the clipboard; otherwise, false.
     */
    public boolean isThereDataStored() {
        return networkEditorPanel.getVisualNetwork().getClipboardAssistant().isThereDataStored();
    }
    
    /**
     * This method removes the selected objects. First removes the selected
     * links and then removes the selected nodes. Also notifies that there
     * aren't selected elements and creates a new undo point.
     */
    public void removeSelectedObjects() {
        networkEditorPanel.getVisualNetwork().removeSelectedObjects();
    }
    
    /**
     * Sets a new contextual menu factory.
     *
     * @param newContextualMenuFactory contextual menu factory to be set.
     */
    public void setContextualMenuFactory(ContextualMenuFactory newContextualMenuFactory) {
        networkEditorPanel.setContextualMenuFactory(newContextualMenuFactory);
    }
    
    /**
     * Selects all nodes and links.
     */
    public void selectAllObjects() {
        networkEditorPanel.selectAllObjects();
    }
    
    /**
     * Returns the presentation mode of the text of the nodes.
     *
     * @return true if the title of the nodes is the name or false if it is the
     * name.
     */
    public boolean getByTitle() {
        return networkEditorPanel.getVisualNetwork().getByTitle();
    }
    
    /**
     * Changes the presentation mode of the text of the nodes.
     *
     * @param value new value of the presentation mode of the text of the nodes.
     */
    public void setByTitle(boolean value) {
        networkEditorPanel.setByTitle(value);
    }
    
    /**
     * Returns the value of the zoomManager.
     *
     * @return actual value of zoomManager.
     */
    @Override public double getZoom() {
        return networkEditorPanel.getZoom();
    }
    
    /**
     * Changes the value of the zoomManager.
     *
     * @param value new zoomManager.
     */
    @Override public void setZoom(double value) {
        networkEditorPanel.setZoom(value);
    }
    
    /**
     * Selects or deselects all nodes of the network.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllNodes(boolean selected) {
        networkEditorPanel.getVisualNetwork().setSelectedAllNodes(selected);
    }
    
    /**
     * Selects or deselects all objects of the network.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllObjects(boolean selected) {
        networkEditorPanel.getVisualNetwork().setSelectedAllObjects(selected);
    }
    
    @Override public void afterEditExecutes(PNEdit arg0) {
        setModified(true);
    }
    
    @Override public void beforeEditExecutes(PNEdit edit) {
        repaint();
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        setModified(edit.getProbNet().getPNESupport().getCanUndo());
        repaint();
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        setModified(edit.getProbNet().getPNESupport().getCanUndo());
        repaint();
    }
    
    /**
     * This method returns the number of the current Evidence Case.
     *
     * @return the number of the current Evidence Case.
     */
    public int getCurrentCase() {
        return networkEditorPanel.getEvidenceManager().getCurrentCase();
    }
    
    /**
     * This method returns the number of Evidence Cases that the ArrayList is
     * currently holding .
     *
     * @return the number of Evidence Cases in the ArrayList.
     */
    public int getNumberOfCases() {
        return networkEditorPanel.getEvidenceManager().getNumberOfCases();
    }
    
    /**
     * This method creates a new evidence case
     */
    public void createNewEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().createNewEvidenceCase();
    }
    
    /**
     * This method makes the first evidence case to be the current
     */
    public void goToFirstEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().goToFirstEvidenceCase();
    }
    
    /**
     * This method makes the previous evidence case to be the current
     */
    public void goToPreviousEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().goToPreviousEvidenceCase();
    }
    
    /**
     * This method makes the next evidence case to be the current
     */
    public void goToNextEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().goToNextEvidenceCase();
    }
    
    /**
     * This method makes the last evidence case to be the current
     */
    public void goToLastEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().goToLastEvidenceCase();
    }
    
    /**
     * This method clears out all the evidence cases. It returns to an 'initial
     * state' in which there is only an initial evidence case with no findings
     * (corresponding to prior probabilities)
     */
    public void clearOutAllEvidenceCases() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().clearOutAllEvidenceCases();
    }
    
    /**
     * This method does the propagation of the evidence for all the evidence
     * cases in memory.
     *
     * @param mainPanelMenuAssistant the menu assistant associated to the main
     *                               panel.
     */
    public void propagateEvidence(MainPanelMenuAssistant mainPanelMenuAssistant) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        networkEditorPanel.getEvidenceManager().propagateEvidence(mainPanelMenuAssistant);
    }
    
    /**
     * This method returns true if propagation type currently set in the panel
     * is automatic; false if manual.
     *
     * @return true if the current propagation type is automatic.
     */
    public boolean isAutomaticPropagation() {
        return networkEditorPanel.isAutomaticPropagation();
    }
    
    /**
     * This method sets the current propagation type in the panel.
     *
     * @param automaticPropagation new value of the propagation type.
     */
    public void setAutomaticPropagation(boolean automaticPropagation) {
        networkEditorPanel.setAutomaticPropagation(automaticPropagation);
    }
    
    /**
     * This method returns the propagation status: true if propagation should be
     * done right now; false otherwise.
     *
     * @return true if propagation should be done right now.
     */
    public boolean isPropagationActive() {
        return networkEditorPanel.isPropagationActive();
    }
    
    /**
     * This method sets the propagation status.
     *
     * @param propagationActive new value of the propagation status.
     */
    public void setPropagationActive(boolean propagationActive) {
        networkEditorPanel.setPropagationActive(propagationActive);
    }
    
    @Override public boolean close() {
        try {
            if (!MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().networkCanBeClosed(this)) {
                return false;
            }
        } catch (WriterException e) {
            throw new UnrecoverableException(e);
        }
        new ArrayList<>(this.decisionTreeWindows).forEach(DecisionTreeWindow::close);
        return super.close();
    }
    
    // TODO OOPN end
    
    public void addDecisionTreeWindows(DecisionTreeWindow decisionTreeWindows) {
        this.decisionTreeWindows.add(decisionTreeWindows);
    }
    
    public void removeDecisionTreeWindows(DecisionTreeWindow decisionTreeWindows) {
        this.decisionTreeWindows.remove(decisionTreeWindows);
    }
    
}

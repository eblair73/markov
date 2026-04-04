/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.core.action.base.linkEdits.InvertLinkAndUpdatePotentialsEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.potential.StrategyCarrier;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.gui.action.RemoveLinkRestrictionEdit;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.*;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.configuration.PreferencesDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.dialog.io.*;
import org.openmarkov.gui.dialog.link.LinkRestrictionEditDialog;
import org.openmarkov.gui.dialog.link.RevelationArcEditDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.dialog.network.OptimalStrategyDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.exception.*;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.util.PropertyNames;
import org.openmarkov.gui.window.decisiontree.DecisionTreeWindow;
import org.openmarkov.gui.window.edition.NetworkPanel;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEOptimalIntervention;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
//TODO: remove just because reference to cost-effectiveness was removed
//import org.openmarkov.costeffectiveness.id.inference.VariableEliminationCE;

/**
 * This class receives the main events of the application and helps the class
 * MainMenu to carry out this task.
 *
 * @author jmendoza
 * @version 1.6.2 -cmyago -26/02/2023 re-added behaviour for "Expand network" (fixing regression)
 */
public class MainPanelListenerAssistant extends WindowAdapter
        implements ActionListener, PropertyNames, ComponentListener {
    /**
     * Value for the ZoomManager increment/decrement
     */
    private static final double zoomChangeValue = 0.2;
    /**
     * Counter incremented each time a network frame is created.
     */
    private final static int frameIndex = 1;
    /**
     * Main panel which this object helps.
     */
    private final MainPanel mainPanel;
    /**
     * Messages string resource.
     */
    // private StringResource stringResource;
    private final List<NetworkPanel> networkPanels;
    private final StringDatabase stringDatabase;
    
    /**
     * Constructor that save the references to the objects that this class
     * needs.
     *
     * @param mainPanel - main panel which this listener helps.
     */
    public MainPanelListenerAssistant(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.mainPanel.setName(mainPanel.getName());
        // stringResource = StringResourceLoader.getUniqueInstance()
        // .getBundleMessages();
        this.networkPanels = new ArrayList<>();
        this.stringDatabase = StringDatabase.getUniqueInstance();
    }
    
    /**
     * commodity method to provide the path directory for the network file name
     *
     * @param fileName - name of the file to obtain the short name
     *
     * @return the directory of the file
     */
    private static String getDirectoryFileName(String fileName) {
        return (new File(fileName)).getAbsolutePath();
    }
    
    /**
     * Invoked when a window is in the process of being closed.
     *
     * @param e - event information.
     */
    @Override public void windowClosing(WindowEvent e) {
        try {
            closeApplication();
        } catch (WriterException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    /**
     * This method listens to the user actions on the main menu.
     *
     * @param e menu event information.
     */
    @Override public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        ActionCommands actionCommandConstant = ActionCommands.of(actionCommand);
        switch (actionCommandConstant) {
            case ActionCommands.NEW_NETWORK -> createNewNetwork();
            case ActionCommands.OPEN_NETWORK -> executeUIAction(this::openNetwork);
            case ActionCommands.OPEN_NETWORK_URL -> executeUIAction(this::openNetworkURL);
            case ActionCommands.SAVE_NETWORK -> executeUIAction(() -> saveNetwork(getCurrentNetworkPanel()));
            case ActionCommands.SAVE_OPEN_NETWORK -> executeUIAction(() -> saveOpenNetwork(getCurrentNetworkPanel()));
            case ActionCommands.SAVEAS_NETWORK -> executeUIAction(() -> saveNetworkAs(getCurrentNetworkPanel()));
            case ActionCommands.CLOSE_TAB -> executeUIAction(this::closeCurrentTab);
            case ActionCommands.LOAD_EVIDENCE -> executeUIAction(() -> loadEvidence(getCurrentNetworkPanel()));
            case ActionCommands.SAVE_EVIDENCE -> saveEvidence(getCurrentNetworkPanel());
            case ActionCommands.NETWORK_PROPERTIES -> getCurrentNetworkPanel().changeNetworkProperties();
            case ActionCommands.EXPAND_NETWORK -> executeUIAction(() ->
                                                                          expandNetwork(getCurrentNetworkPanel().getProbNet(), getCurrentNetworkPanel().getEditorPanel()
                                                                                                                                                       .getEvidenceManager()
                                                                                                                                                       .getPreResolutionEvidence()));
            case ActionCommands.TEMPORAL_EVOLUTION_BY_CRITERION, ActionCommands.TEMPORAL_EVOLUTION_ACTION ->
                    this.getCurrentNetworkPanel().temporalEvolution();
            //case ActionCommands.EXPAND_NETWORK_CE -> expandNetworkCE(getCurrentNetworkPanel().getProbNet(), getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence());
            case ActionCommands.EXIT_APPLICATION -> executeUIAction(this::closeApplication);
            case ActionCommands.CLIPBOARD_COPY -> {
                getCurrentNetworkPanel().exportToClipboard(false);
                mainPanel.getMainPanelMenuAssistant()
                         .setOptionEnabled(ActionCommands.CLIPBOARD_PASTE.getCommandName(), true);
            }
            case ActionCommands.CLIPBOARD_CUT -> {
                getCurrentNetworkPanel().exportToClipboard(true);
                mainPanel.getMainPanelMenuAssistant()
                         .setOptionEnabled(ActionCommands.CLIPBOARD_PASTE.getCommandName(), true);
            }
            case ActionCommands.CLIPBOARD_PASTE -> executeUIAction(() -> getCurrentNetworkPanel().pasteFromClipboard());
            case ActionCommands.UNDO -> undo();
            case ActionCommands.REDO -> redo();
            case ActionCommands.SELECT_ALL -> getCurrentNetworkPanel().selectAllObjects();
            case ActionCommands.OBJECT_REMOVAL -> getCurrentNetworkPanel().removeSelectedObjects();
            case ActionCommands.EDITION_MODE_PREFIX -> activateEditionMode(actionCommand);
            case ActionCommands.CHANGE_WORKING_MODE, ActionCommands.CHANGE_TO_INFERENCE_MODE,
                 ActionCommands.CHANGE_TO_EDITION_MODE -> {
                NetworkPanel.WorkingMode initialWorkingMode = getCurrentNetworkPanel().getWorkingMode();
                try {
                    toggleWorkingMode();
                } catch (NotEnoughMemoryException | IncompatibleEvidenceException |
                         ConstraintViolatedException | RuntimeException ex) {
                    //On fail, go back to the previous working mode.
                    try {
                        setWorkingMode(initialWorkingMode, initialWorkingMode);
                    } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                             NotEnoughMemoryException | IncompatibleEvidenceException |
                             CannotNormalizePotentialException | ConstraintViolatedException exc) {
                        throw new UnreachableException(exc);
                    }
                    throw new UnrecoverableException(ex);
                }
            }
            case ActionCommands.SET_NEW_EXPANSION_THRESHOLD -> setNewExpansionThreshold((Double) e.getSource());
            case ActionCommands.CREATE_NEW_EVIDENCE_CASE ->
                    executeUIAction(() -> evidenceCasesNavigationOption("CREATE_NEW_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_FIRST_EVIDENCE_CASE ->
                    executeUIAction(() -> evidenceCasesNavigationOption("GO_TO_FIRST_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE ->
                    executeUIAction(() -> evidenceCasesNavigationOption("GO_TO_PREVIOUS_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_NEXT_EVIDENCE_CASE ->
                    executeUIAction(() -> evidenceCasesNavigationOption("GO_TO_NEXT_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_LAST_EVIDENCE_CASE ->
                    executeUIAction(() -> evidenceCasesNavigationOption("GO_TO_LAST_EVIDENCE_CASE"));
            case ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES ->
                    executeUIAction(() -> evidenceCasesNavigationOption("CLEAR_OUT_ALL_EVIDENCE_CASES"));
            case ActionCommands.PROPAGATE_EVIDENCE ->
                    executeUIAction(() -> getCurrentNetworkPanel().propagateEvidence(mainPanel.getMainPanelMenuAssistant()));
            case ActionCommands.ABSORB_NODE -> executeUIAction(() -> this.getCurrentNetworkPanel().absorbNode());
            case ActionCommands.ABSORB_PARENTS -> executeUIAction(() -> this.getCurrentNetworkPanel().absorbParents());
            case ActionCommands.NODE_PROPERTIES ->
                    executeUIAction(() -> getCurrentNetworkPanel().changeNodeProperties());
            case ActionCommands.EDIT_POTENTIAL -> executeUIAction(() -> getCurrentNetworkPanel().changePotential());
            case ActionCommands.DECISION_IMPOSE_POLICY ->
                    executeUIAction(() -> getCurrentNetworkPanel().imposePolicyInNode());
            case ActionCommands.DECISION_EDIT_POLICY ->
                    executeUIAction(() -> getCurrentNetworkPanel().editNodePolicy());
            case ActionCommands.DECISION_REMOVE_POLICY ->
                    executeUIAction(() -> getCurrentNetworkPanel().removePolicyFromNode());
            case ActionCommands.DECISION_SHOW_EXPECTED_UTILITY ->
                    executeUIAction(() -> getCurrentNetworkPanel().showExpectedUtilityOfNode());
            case ActionCommands.DECISION_SHOW_OPTIMAL_POLICY ->
                    executeUIAction(() -> getCurrentNetworkPanel().showOptimalPolicyOfNode());
            case ActionCommands.NODE_EXPANSION -> {
                NetworkPanel networkPanel = getCurrentNetworkPanel();
                List<VisualNode> selectedNodes = networkPanel.getEditorPanel().getVisualNetwork().getSelectedNodes();
                if (!selectedNodes.isEmpty()) {
                    for (VisualNode visualNode : selectedNodes) {
                        if (!(visualNode.isExpanded())) {
                            visualNode.setExpanded(true);
                            networkPanel.getEditorPanel().getVisualNetwork().setSelectedNode(visualNode, false);
                        }
                    }
                }
                networkPanel.getEditorPanel().repaint();
            }
            case ActionCommands.NODE_CONTRACTION -> {
                NetworkPanel networkPanel = getCurrentNetworkPanel();
                List<VisualNode> selectedNodes = networkPanel.getEditorPanel().getVisualNetwork().getSelectedNodes();
                if (!selectedNodes.isEmpty()) {
                    for (VisualNode visualNode : selectedNodes) {
                        if (visualNode.isExpanded()) {
                            visualNode.setExpanded(false);
                            networkPanel.getEditorPanel().getVisualNetwork().setSelectedNode(visualNode, false);
                        }
                    }
                }
                networkPanel.getEditorPanel().repaint();
            }
            case ActionCommands.NODE_ADD_FINDING -> getCurrentNetworkPanel().addFinding();
            case ActionCommands.NODE_REMOVE_FINDING -> executeUIAction(() -> getCurrentNetworkPanel().removeFinding());
            case ActionCommands.NODE_REMOVE_ALL_FINDINGS ->
                    executeUIAction(() -> getCurrentNetworkPanel().removeAllFindings());
            case ActionCommands.BYTITLE_NODES -> activateByTitle(true);
            case ActionCommands.BYNAME_NODES -> activateByTitle(false);
            case ActionCommands.ZOOM_IN -> incrementZoom(getCurrentPanel());
            case ActionCommands.ZOOM_OUT -> decrementZoom(getCurrentPanel());
            case ActionCommands.CONFIGURATION -> executeUIAction(this::showUserConfigurationDialog);
            case ActionCommands.PROPAGATION_OPTIONS -> setPropagationOptions();
            case ActionCommands.INFERENCE_OPTIONS -> setInferenceOptions(getCurrentNetworkPanel());
            case ActionCommands.HELP_CHANGE_LANGUAGE -> showLanguageChangeDialog();
            case ActionCommands.HELP_SHORTCUTS -> showShortcuts();
            case ActionCommands.HELP_ABOUT -> showAbout();
            case ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS -> executeUIAction(() -> {
                NetworkPanel networkPanel = this.getCurrentNetworkPanel();
                List<VisualLink> links = networkPanel.getEditorPanel().getVisualNetwork().getSelectedLinks();
                if (!links.isEmpty()) {
                    Link<Node> link = links.getFirst().getLink();
                    Node node1 = link.getFrom();
                    Node node2 = link.getTo();
                    new InvertLinkAndUpdatePotentialsEdit(networkPanel.getEditorPanel()
                                                                      .getVisualNetwork()
                                                                      .getProbNet(), node1.getVariable(), node2.getVariable())
                            .executeEdit();
                }
            });
            case ActionCommands.LINK_RESTRICTION_ENABLE_PROPERTIES, ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES -> {
                NetworkPanel networkPanel = this.getCurrentNetworkPanel();
                List<VisualLink> links = networkPanel.getEditorPanel().getVisualNetwork().getSelectedLinks();
                if (!links.isEmpty()) {
                    Link<Node> link = links.getFirst().getLink();
                    if (!link.hasRestrictions()) {
                        link.initializesRestrictionsPotential();
                    }
                    new LinkRestrictionEditDialog(GUIUtils.getOwner(networkPanel.getEditorPanel()), link).requestValues();
                    link.tryResetRestrictionsPotential();
                    networkPanel.getEditorPanel().repaint();
                }
            }
            case ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES -> executeUIAction(() ->
                                                                                               new RemoveLinkRestrictionEdit(this.getCurrentNetworkPanel()
                                                                                                                                 .getEditorPanel()
                                                                                                                                 .getVisualNetwork()).executeEdit());
            case ActionCommands.LINK_REVELATIONARC_PROPERTIES -> {
                NetworkPanel networkPanel = this.getCurrentNetworkPanel();
                List<VisualLink> links = networkPanel.getEditorPanel().getVisualNetwork().getSelectedLinks();
                if (!links.isEmpty()) {
                    Link<Node> link = links.getFirst().getLink();
                    Window owner = GUIUtils.getOwner(networkPanel.getEditorPanel());
                    new RevelationArcEditDialog(owner, link).requestValues();
                }
            }
            case ActionCommands.DECISION_TREE -> executeUIAction(() -> showDecisionTree(this.getCurrentNetworkPanel()));
            case ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY ->
                    executeUIAction(() -> showOptimalStrategy(this.getCurrentNetworkPanel()));
            case ActionCommands.NEXT_SLICE_NODE -> executeUIAction(() -> {
                NetworkPanel networkPanel = this.getCurrentNetworkPanel();
                Node selectedNode = networkPanel.getEditorPanel()
                                                .getVisualNetwork()
                                                .getSelectedNodes()
                                                .getFirst()
                                                .getNode();
                Variable selectedVariable = selectedNode.getVariable();
                Variable newVariable = new Variable(selectedVariable);
                newVariable.setTimeSlice(selectedVariable.getTimeSlice() + 1);
                Point2D.Double position = new Point2D.Double(selectedNode.getCoordinateX() + 200,
                                                             selectedNode.getCoordinateY());
                new AddNodeEdit(networkPanel.getEditorPanel()
                                            .getVisualNetwork()
                                            .getProbNet(), newVariable, selectedNode.getNodeType(), position).executeEdit();
            });
            case ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC -> {
                try {
                    Method ceMethod = Class.forName("org.openmarkov.costEffectiveness.CostEffectivenessPlugin")
                                           .getDeclaredMethod("onClick");
                    ceMethod.setAccessible(true);
                    ceMethod.invoke(null);
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException ex) {
                    throw new UnreachableException(ex);
                } catch (InvocationTargetException ex) {
                    switch (ex.getCause()){
                        case RuntimeException exc -> throw exc;
                        case Exception exc -> throw new UnrecoverableException(exc);
                        case null, default -> throw new UnreachableException(ex);
                    }
                }
            }
            case ActionCommands.CHANCE_CREATION, ActionCommands.UNCERTAINTY_REMOVE, ActionCommands.UNCERTAINTY_EDIT,
                 ActionCommands.UNCERTAINTY_ASSIGN, ActionCommands.TEMPORAL_OPTIONS,
                 ActionCommands.SENSITIVITY_ANALYSIS, ActionCommands.SENSITIVITY_ANALYSIS_PROBABILISTIC,
                 ActionCommands.SENSITIVITY_ANALYSIS_DETERMINISTIC, ActionCommands.COST_EFFECTIVENESS_SENSITIVITY,
                 ActionCommands.LEARNING,
                 ActionCommands.VIEW_TOOLBARS, ActionCommands.LINK_PROPERTIES,
                 ActionCommands.TREE_SAVE_GRAPHVIZ, ActionCommands.TREE_SHOW_CEP, ActionCommands.TREE_OPEN_NETWORK,
                 ActionCommands.TREE_EXPAND_ALL, ActionCommands.TREE_EXPAND_NEXT,
                 ActionCommands.LINK_CREATION, ActionCommands.UTILITY_CREATION,
                 ActionCommands.DECISION_CREATION, ActionCommands.LOG, ActionCommands.CHANGE_ACTIVE_CLASS,
                 ActionCommands.ZOOM_PREFIX, ActionCommands.NODES, ActionCommands.ZOOM,
                 ActionCommands.OBJECT_SELECTION,
                 // Handled before this switch by the openRecentFileIndex() guard:
                 ActionCommands.OPEN_LAST_1_FILE, ActionCommands.OPEN_LAST_2_FILE,
                 ActionCommands.OPEN_LAST_3_FILE, ActionCommands.OPEN_LAST_4_FILE,
                 ActionCommands.OPEN_LAST_5_FILE, ActionCommands.OPEN_LAST_6_FILE,
                 ActionCommands.OPEN_LAST_7_FILE, ActionCommands.OPEN_LAST_8_FILE,
                 ActionCommands.OPEN_LAST_9_FILE -> this.defaultActionOnCommand(e, actionCommand, actionCommandConstant);
            case null -> this.defaultActionOnCommand(e, actionCommand, actionCommandConstant);
        }
    }
    
    @FunctionalInterface
    private interface UIAction {
        void execute() throws Exception;
    }
    
    private void executeUIAction(UIAction action) {
        try {
            action.execute();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    private void closeCurrentTab() throws WriterException {
        var selectedComponent = this.mainPanel.getNetworksTabPanel().getSelectedComponent();
        switch (selectedComponent) {
            case NetworkPanel networkPanel -> closeCurrentNetwork();
            case null -> {
            }
            default -> this.mainPanel.getNetworksTabPanel().remove(selectedComponent);
        }
    }
    
    private void defaultActionOnCommand(ActionEvent e, String actionCommand, ActionCommands actionCommandConstant) {
        
        if (actionCommand.startsWith(ActionCommands.EDITION_MODE_PREFIX.getCommandName())) {
            activateEditionMode(actionCommand);
        } else if (actionCommand.startsWith(ActionCommands.VIEW_TOOLBARS.getCommandName())) {
            MainGUI.INSTANCE.mainPanel.getToolbarManager()
                                      .addToolbar(actionCommand.replace(ActionCommands.VIEW_TOOLBARS.getCommandName() + ".", ""));
        } else if (ActionCommands.isZoomActionCommand(actionCommand)) {
            setZoom(getCurrentPanel(), ActionCommands.getValueZoomActionCommand(actionCommand));
        } else if (e.getSource() instanceof JButton source) {
            var listeners = source.getActionListeners();
            //
            //throw new UnrecoverableException(new InvalidArgumentException(actionCommand, "it has not tied action"));
        } else if (actionCommandConstant != null
                && actionCommandConstant.openRecentFileIndex().orElse(null) instanceof Integer recentFileIndex) {
            executeUIAction(() -> openNetwork(LastOpenFiles.getFilePathAt(recentFileIndex)));
        }
        
    }
    
    /**
     * Create a Frame for a Change Language dialog
     */
    private void showLanguageChangeDialog() {
        LanguageDialog.getUniqueInstance(mainPanel.getMainFrame()).setVisible(true);
    }
    
    /**
     * Create a Frame for the User Configuration dialog
     *
     */
    private void showUserConfigurationDialog() throws BackingStoreException {
        new PreferencesDialog(mainPanel.getMainFrame()).setVisible(true);
    }
    
    /**
     * Create a Frame for shortcuts information
     *
     * @return a JDialog (shortcutsBox with shortcut information
     */
    private ShortcutsBox showShortcuts() {
        return new ShortcutsBox(mainPanel.getMainFrame());
    }
    
    /**
     * Create a Frame for About information
     *
     * @return aboutBox the AboutBox dialog
     */
    private AboutBox showAbout() {
        return new AboutBox(mainPanel.getMainFrame());
    }
    
    /**
     * Returns the current network panel of the current frame.
     *
     * @return the current network panel.
     */
    public NetworkPanel getCurrentNetworkPanel() {
        return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkPanel();
    }
    
    /**
     * Returns the current network panel of the current frame.
     *
     * @return the current network panel.
     */
    public ZoomableContentPanel getCurrentPanel() {
        return (ZoomableContentPanel) mainPanel.getNetworksTabPanel().getSelectedComponent();
    }
    
    /**
     * Returns a value indicating if the network can be closed. If the network
     * has not been saved, this method offers to the users the possibility of
     * save it. If the user answers 'yes', the network is saved and can be
     * closed. If the user answers 'no', the network isn't saved and can be
     * closed. If the user answers 'cancel', the network can't be closed.
     *
     * @param networkPanel network panel to be checked.
     *
     * @return true, if the network can be closed; otherwise, false.
     */
    public boolean networkCanBeClosed(NetworkPanel networkPanel) throws WriterException {
        int response;
        boolean canClose = !networkPanel.getModified();
        if (networkPanel.getModified()) {
            String title = StringDatabase.getUniqueInstance()
                                         .getFormattedString("NetworkNotSaved.Title", networkPanel.probNet.getName());
            String message = StringDatabase.getUniqueInstance()
                                           .getFormattedString("NetworkNotSaved.Text", networkPanel.probNet.getName());
            response = JOptionPane
                    .showConfirmDialog(GUIUtils.getOwner(mainPanel), message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                                       JOptionPane.WARNING_MESSAGE);
            canClose = switch (response) {
                case JOptionPane.YES_OPTION -> saveNetwork(networkPanel);
                case JOptionPane.NO_OPTION -> true;
                default -> false;
            };
        }
        if (canClose) {
            networkPanels.remove(networkPanel);
        }
        return canClose;
    }
    
    //    /**
    //     * Saves a network in a file and makes the rest of actions in the
    //     * environment (menus, messages, etc.).
    //     *
    //     * @param networkPanel
    //     *            network panel which contains the network to be saved.
    //     * @param fileName
    //     *            file where save the network.
    //     * @param saveOptions
    //     * @return true if the network could be saved; otherwise, false.
    //     */
    //    private boolean saveNetworkActions(NetworkPanel networkPanel,
    //            String fileName,
    //            SaveOptions saveOptions) {
    //        boolean result = false;
    //        mainPanel.getMessageWindow().getNormalMessageStream().println(stringDatabase.getValuesInAString("SavingNetwork.Text)
    //                + " "
    //                + fileName);
    //        try {
    //            if (saveOptions != null && saveOptions.isSavePlainNetwork()) {
    //                networkPanel.showPlainNetwork();
    //            }
    //            if (saveOptions != null
    //                    && saveOptions.isSaveClassesInFile()
    //                    && networkPanel.getProbNet() instanceof OOPNet) {
    //                ((OOPNet) networkPanel.getProbNet()).fillClassList();
    //            }
    //
    //            NetsIO.saveNetworkFile(networkPanel.getProbNet(),
    //                    networkPanel.getEditorPanel().getEvidence(),
    //                    fileName);
    //            // networkPanel.getNetwork().backupProbNet.saveToFile( fileName );
    //            networkPanel.setModified(false);
    //            networkPanel.setNetworkFile(fileName);
    //            mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
    //            lastOpenFiles.setLastFileName(fileName);
    //            OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY,
    //                    getDirectoryFileName(fileName),
    //                    OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
    //            mainPanel.getMessageWindow().getNormalMessageStream().println(stringDatabase.getValuesInAString("NetworkSaved.Text));
    //            mainPanel.getMainMenu().rechargeLastOpenFiles();
    //            result = true;
    //        } catch (NotRecognisedNetworkFileExtensionException e) {
    //            JOptionPane.showMessageDialog(GUIUtils.getOwner(mainPanel),
    //                    stringDatabase.getValuesInAString("CanNotRecognisedFileExtension.Text),
    //                    stringDatabase.getValuesInAString("ErrorWindow.Title),
    //                    JOptionPane.ERROR_MESSAGE);
    //        } catch (CanNotWriteNetworkToFileException e) {
    //            JOptionPane.showMessageDialog(GUIUtils.getOwner(mainPanel),
    //                    stringDatabase.getValuesInAString("ErrorSavingNetwork.Text) + ": " + e.getMessage(),
    //                    stringDatabase.getValuesInAString("ErrorWindow.Title),
    //                    JOptionPane.ERROR_MESSAGE);
    //        } catch (Exception e) {
    //            JOptionPane.showMessageDialog(GUIUtils.getOwner(mainPanel),
    //                    stringDatabase.getValuesInAString("Generic I/O error"),
    //                    stringDatabase.getValuesInAString("ErrorWindow.Title),
    //                    JOptionPane.ERROR_MESSAGE);
    //        }
    //        return result;
    //    }
    
    
    /**
     * Saves a network in a file considering the file format chosen. Also it makes the rest of actions in the
     * environment (menus, messages, etc.).
     *
     * @param networkPanel network panel which contains the network to be saved.
     * @param fileName     file where save the network.
     *
     * @return true if the network could be saved; otherwise, false.
     */
    private boolean saveNetworkActions(NetworkPanel networkPanel, String fileName, String fileFormat) throws WriterException {
        System.out.println(stringDatabase.getString("SavingNetwork.Text") + " " + fileName);
        NetsIO.saveNetworkFile(networkPanel.getProbNet(), networkPanel.getEditorPanel()
                                                                      .getEvidenceManager()
                                                                      .getEvidence(), fileName);
        
        // networkPanel.getNetwork().backupProbNet.saveToFile( fileName );
        networkPanel.setModified(false);
        networkPanel.setNetworkFile(fileName);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
        LastOpenFiles.setLastFileName(fileName);
        LocalPreferences.LATEST_SAVED_DIRECTORY.set(new File(fileName).getAbsoluteFile());
        System.out.println(stringDatabase.getString("NetworkSaved.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        return true;
    }
    
    /**
     * Saves a network in the file given by
     *
     * @param networkPanel the network panel
     * @param fileName     the file where the network is stored
     *
     * @return true iff the network could be saved
     */
    
    private boolean saveNetworkActions(NetworkPanel networkPanel, String fileName) throws WriterException {
        String fileFormat = LocalPreferences.LATEST_NETWORK_FORMAT.get();
        return saveNetworkActions(networkPanel, fileName, fileFormat);
    }
    
    /**
     * Save a network. First it requests the file in which save the network and
     * then saves the network.
     *
     * @param networkPanel network panel that contains the network to be saved.
     *
     * @return true if the network has been saved; otherwise, false.
     */
    public boolean saveNetwork(NetworkPanel networkPanel) throws WriterException {
        String fileName = networkPanel.getNetworkFile();
        if (fileName != null) {
            createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        return (fileName != null && networkPanel.probNet.getWriter() != null) ? saveNetworkActions(networkPanel, fileName) : saveNetworkAs(networkPanel);
    }
    
    /**
     * Save a network. First it requests the file in which save the network and
     * then saves the network.
     *
     * @param networkPanel network panel that contains the network to be saved.
     */
    private void saveOpenNetwork(NetworkPanel networkPanel) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile, WriterException {
        String fileName = networkPanel.getNetworkFile();
        if (fileName != null) {
            createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        saveNetwork(networkPanel);
        fileName = networkPanel.getNetworkFile();
        closeCurrentNetwork();
        openNetwork(fileName);
    }
    
    private void createBackUpNetworkFile(String fileName, String newFileName) {
        File inFile = new File(fileName);
        File outFile = new File(newFileName);
        try (
                FileInputStream in = new FileInputStream(inFile);
                FileOutputStream out = new FileOutputStream(outFile);
        ) {
            while (true) {
                int c = in.read();
                if (c == -1) break;
                out.write(c);
            }
        } catch (IOException e) {
            System.out.println(stringDatabase.getString("NetworkBackupError.Text"));
        }
        System.out.println(stringDatabase.getString("NetworkBackup.Text"));
    }
    
    private static String toBakExtension(String nameFile) {
        String newName;
        int index = nameFile.lastIndexOf('.');
        if (index > 0) {
            newName = nameFile.substring(0, index);
        } else
            newName = nameFile;
        return newName + ".bak";
    }
    
    /**
     * Save a network in a different file. First it requests the file in which
     * save the network and then saves the network.
     *
     * @param networkPanel network panel that contains the network to be saved.
     *
     * @return true if the network has been saved; otherwise, false.
     */
    public boolean saveNetworkAs(NetworkPanel networkPanel) throws WriterException {
        String fileName = networkPanel.getNetworkFile();
        /*
        fileName = requestNetworkFileToSave((fileName != null) ? fileName
                : networkPanel.getProbNet().getName());
        */
        String suggestedName = (fileName != null) ? fileName : new File(networkPanel.getProbNet().getName()).getName();
        ArrayList<Object> fileNameAndFormat = requestNetworkFileAndFormatToSave(suggestedName);
        fileName = (String) fileNameAndFormat.get(0);
        if (fileName == null) {
            return false;
        }
        String fileFormat = (String) fileNameAndFormat.get(1);
        networkPanel.setNetworkFile(fileName);
        networkPanel.getProbNet().setName(new File(fileName).getName());
        var formatInfo = FormatManager.info((Class<?>) fileNameAndFormat.get(2));
        networkPanel.getProbNet().setWriter(
                FormatManager.writersInstances()
                             .filter(probNetWriter -> FormatManager.formatEquals(formatInfo, FormatManager.info(probNetWriter)))
                             .findFirst()
                             .orElse(null));
        networkPanel.getProbNet().setReader(
                FormatManager.readersInstances()
                             .filter(probNetReader -> FormatManager.formatEquals(formatInfo, FormatManager.info(probNetReader)))
                             .findFirst()
                             .orElse(null));
        return saveNetworkActions(networkPanel, fileName, fileFormat);
    }
    
    
    /**
     * It asks the user to choose a file by means of a save-file dialog box.
     *
     * @param suggestedFileName name of the file where the net can be saved as default.
     *
     * @return complete path of the file, or null if the user selects cancel.
     */
    private String requestNetworkFileToSave(String suggestedFileName) {
        NetworkOMFileChooser fileChooser = new NetworkOMFileChooser(false, false);
        String title = stringDatabase.getString("SaveNetwork.Title");
        fileChooser.setDialogTitle(title);
        fileChooser.setSelectedFile(new File(suggestedFileName));
        String filename = null;
        if (fileChooser.showSaveDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            filename = fileChooser.getSelectedFile().getAbsolutePath();
            String chosenFilterExtension = ((FileFilterBasic) fileChooser.getFileFilter()).getFilterExtension();
            if (!filename.toLowerCase().endsWith("." + chosenFilterExtension.toLowerCase())) {
                filename += "." + chosenFilterExtension.toLowerCase();
            }
        }
        return filename;
    }
    
    /**
     * @param suggestedFileName the suggested file name
     *
     * @return a list with the absolute path of of the chosen filename, the file format chosen, and the writer class.
     */
    private ArrayList<Object> requestNetworkFileAndFormatToSave(String suggestedFileName) {
        NetworkOMFileChooser fileChooser = new NetworkOMFileChooser(false, false);
        String title = stringDatabase.getString("SaveNetwork.Title");
        fileChooser.setDialogTitle(title);
        fileChooser.setCurrentDirectory(LocalPreferences.LATEST_SAVED_DIRECTORY.get());
        fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), new File(suggestedFileName).getName()));
        ArrayList<Object> fileNameAndFormat = new ArrayList<>();
        String filename = null;
        FileFilterAll<?> fileFormat = null;
        if (fileChooser.showSaveDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            filename = fileChooser.getSelectedFile().getAbsolutePath();
            String chosenFilterExtension = ((FileFilterBasic) fileChooser.getFileFilter()).getFilterExtension();
            if (!filename.toLowerCase().endsWith("." + chosenFilterExtension.toLowerCase())) {
                filename += "." + chosenFilterExtension.toLowerCase();
                File selectedFile = new File(filename);
                if (selectedFile.exists()) {
                    int response = JOptionPane.showConfirmDialog(this.getCurrentPanel(), "The file " + selectedFile.getName()
                                                                         + " already exists. The file will be renamed to " + selectedFile.getName() + " (1)." + chosenFilterExtension.toLowerCase(), "Network renamed",
                                                                 JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    filename = fileChooser.getSelectedFile()
                                          .getAbsolutePath() + " (1)." + chosenFilterExtension.toLowerCase();
                }
                
            }
            fileFormat = (FileFilterAll<?>) fileChooser.getFileFilter();
        }
        fileNameAndFormat.add(filename);
        fileNameAndFormat.add(fileFormat == null ? null : fileFormat.getFileDescription());
        fileNameAndFormat.add(fileFormat == null ? null : fileFormat.getFormatInfo());
        return fileNameAndFormat;
    }
    
    /**
     * Creates a new network in the workspace. First, it requests the
     * additionalProperties of the new network and, if the user accepts the
     * dialog box, a new network is created.
     */
    private void createNewNetwork() {
        NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(GUIUtils.getOwner(mainPanel));
        if (dialogProperties.showProperties() == OkCancelDialog.ChosenOption.Ok) {
            ProbNet probNet = dialogProperties.getProbNet();
            
            // If the probNet has not the OnlyChanceNodes constraint and not has any criterion, we
            // create the default criterion.
            if (!probNet.hasConstraintOfClass(OnlyChanceNodes.class) && (
                    probNet.getDecisionCriteria() == null || probNet.getDecisionCriteria().isEmpty()
            )) {
                List<Criterion> criteria = new ArrayList<>();
                criteria.add(new Criterion());
                probNet.setDecisionCriteria(criteria);
            }
            String networkName = stringDatabase.getString("InternalFrame.Title");
            probNet.setName(networkName);
            probNet.getPNESupport().setWithUndo(true);
            networkPanels.add(createNewFrame(probNet));
            // mainPanelMenuAssistant is added as listener to probNet
            // for menus updated purposes.
            probNet.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        }
    }
    
    /**
     * Creates a new frame in the workspace, suppling the network to be painted
     * into the frame.
     *
     * @param probNet network to be painted into the frame
     *
     * @return the network panel that is created.
     */
    public NetworkPanel createNewFrame(ProbNet probNet) {
        NetworkPanel networkPanel = new NetworkPanel(probNet, mainPanel);
        probNet.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.addCloseableTab(probNet.getName(), networkPanel);
        mainPanel.getNetworksTabPanel().setSelectedComponent(networkPanel);
        networkPanel.setContextualMenuFactory(mainPanel.getContextualMenuFactory());
        networkPanel.getEditorPanel().getVisualNetwork().addSelectionListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.getMainPanelMenuAssistant().updateOptionsNewNetworkOpen();
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(networkPanel);
        // mainPanel.getMainPanelMenuAssistant().updateNetworkAgents(networkPanel);
        mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(networkPanel.getCurrentCase());
        return networkPanel;
    }
    
    /**
     * Open a network.
     */
    private void openNetwork() throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        openNetwork("");
    }
    
    /**
     * Open a existing network in a new network frame. If it is not a recently
     * closed network (registered in the menu), it requests the file which
     * contains the network and then opens a new network frame.
     *
     * @param fileName - for the network
     */
    public void openNetwork(String fileName) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        if (fileName.isEmpty()) {
            fileName = requestNetworkFileToOpen();
        }
        if (fileName == null) return;
        System.out.println(stringDatabase.getString("LoadingNetwork.Text") + " " + fileName);
        //TODO Performance issue here on first call
        ProbNetInfo probNetInfo = NetsIO.openNetworkFile(fileName);
        ProbNet netReadFromFile = probNetInfo.getProbNet();
        netReadFromFile.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        netReadFromFile.getPNESupport().setWithUndo(true);
        netReadFromFile.setName(new File(fileName).getName());
        //TODO Performance issue here on first call
        var now = Instant.now();
        NetworkPanel networkPanel = createNewFrame(netReadFromFile);
        System.out.println("Total: " + Duration.between(now, Instant.now()));
        networkPanel.setNetworkFile(fileName);
        List<EvidenceCase> evidence = probNetInfo.getEvidence();
        if (evidence != null && !evidence.isEmpty()) {
            EvidenceCase preResolutionEvidence = evidence.getFirst();
            evidence.removeFirst();
            networkPanel.getEditorPanel().getEvidenceManager().setEvidence(preResolutionEvidence, evidence);
        }
        networkPanels.add(networkPanel);
        LastOpenFiles.setLastFileName(fileName);
        getDirectoryFileName(fileName);
        LocalPreferences.LATEST_OPEN_DIRECTORY.set(new File(fileName).getAbsoluteFile());
        // If the file was opened from a URL, the 'save' and 'save and reopen' button are disabled,
        // but it is not longer the scenario
        //mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(false);
        System.out.println(stringDatabase.getString("NetworkLoaded.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        
        if (netReadFromFile.getShowCommentWhenOpening()) {
            CommentHTMLScrollPane commentHTMLScrollPaneNetworkComment = new CommentHTMLScrollPane();
            
            commentHTMLScrollPaneNetworkComment.setEditable(false);
            commentHTMLScrollPaneNetworkComment.setCommentHTMLTextPaneText(netReadFromFile.getComment());
            commentHTMLScrollPaneNetworkComment.setPreferredSize(new Dimension(500, 300));
            JOptionPane networkMessagePane = new JOptionPane(commentHTMLScrollPaneNetworkComment,
                                                             JOptionPane.INFORMATION_MESSAGE);
            JDialog networkMessageDialog = networkMessagePane.createDialog(GUIUtils.getOwner(mainPanel),
                                                                           stringDatabase.getString("NetworkCommentWindow.Title"));
            networkMessageDialog.setResizable(true);
            networkMessageDialog.setMinimumSize(new Dimension(500, 300));
            networkMessageDialog.setVisible(true);
        }
        
    }
    
    public void openNetwork(ProbNet probNet) {
        NetworkPanel newNetworkPanel = createNewFrame(probNet);
        networkPanels.add(newNetworkPanel);
    }
    
    /**
     * Open a network from a URL.
     */
    //TODO: generalize... It's almost the same as openNetwork...
    public void openNetworkURL() throws NoReaderForFileException, ParserException, IOException, CorruptNetworkFile {
        URL url = requestURLFileToOpen();
        if (url == null) {
            return;
        }
        String urlFile = url.getFile();
        System.out.println(stringDatabase.getString("LoadingNetworkURL.Text") + " " + url);
        ProbNetInfo probNetInfo = NetsIO.openNetworkURL(url);
        ProbNet netReadFromURL = probNetInfo.getProbNet();
        netReadFromURL.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        netReadFromURL.getPNESupport().setWithUndo(true);
        netReadFromURL.setName(new File(urlFile).getName());
        NetworkPanel networkPanel = createNewFrame(netReadFromURL);
        networkPanel.setNetworkFile(urlFile);
        List<EvidenceCase> evidence = probNetInfo.getEvidence();
        if (evidence != null && !evidence.isEmpty()) {
            EvidenceCase preResolutionEvidence = evidence.getFirst();
            evidence.removeFirst();
            networkPanel.getEditorPanel().getEvidenceManager().setEvidence(preResolutionEvidence, evidence);
        }
        networkPanels.add(networkPanel);
        LastOpenFiles.setLastFileName(urlFile);
        // If the file was opened from a URL, the 'save' and 'save and reopen' buttons have to be disabled
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(true);
        System.out.println(stringDatabase.getString("NetworkLoaded.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        
        if (netReadFromURL.getShowCommentWhenOpening()) {
            CommentHTMLScrollPane commentHTMLScrollPaneNetworkComment = new CommentHTMLScrollPane();
            
            commentHTMLScrollPaneNetworkComment.setEditable(false);
            commentHTMLScrollPaneNetworkComment.setCommentHTMLTextPaneText(netReadFromURL.getComment());
            commentHTMLScrollPaneNetworkComment.setPreferredSize(new Dimension(500, 300));
            JOptionPane networkMessagePane = new JOptionPane(commentHTMLScrollPaneNetworkComment,
                                                             JOptionPane.INFORMATION_MESSAGE);
            JDialog networkMessageDialog = networkMessagePane.createDialog(GUIUtils.getOwner(mainPanel),
                                                                           stringDatabase.getString("NetworkCommentWindow.Title"));
            networkMessageDialog.setResizable(true);
            networkMessageDialog.setMinimumSize(new Dimension(500, 300));
            networkMessageDialog.setVisible(true);
        }
        
    }
    
    /**
     * It asks the user to choose a file by means of a open-file dialog box.
     *
     * @return complete path of the file, or null if the user selects cancel.
     */
    private String requestNetworkFileToOpen() {
        NetworkOMFileChooser fileChooser = new NetworkOMFileChooser();
        fileChooser.setDialogTitle(stringDatabase.getString("OpenNetwork.Title"));
        String fileName = null;
        if (fileChooser.showOpenDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            fileName = fileChooser.getSelectedFile().getAbsolutePath();
        }
        return fileName;
    }
    
    /**
     * It asks the user to choose a file by means of a open-file dialog box.
     *
     * @return complete path of the file, or null if the user selects cancel.
     */
    private URL requestURLFileToOpen() {
        URLNetworkChooserDialog urlNetworkChooserDialog = new URLNetworkChooserDialog(GUIUtils.getOwner(mainPanel));
        if (urlNetworkChooserDialog.requestNetworkURL() == OkCancelDialog.ChosenOption.Ok) {
            return urlNetworkChooserDialog.getNetworkURL();
        }
        return null;
        
    }
    
    /**
     * Closes the current network frame.
     *
     * @return true if the network has been closed; otherwise, false.
     */
    private boolean closeCurrentNetwork() throws WriterException {
        return closeNetwork(getCurrentNetworkPanel());
    }
    
    /**
     * Closes the selected network frame.
     *
     * @return true if the network has been closed; otherwise, false.
     */
    private boolean closeNetwork(NetworkPanel currentNetworkPanel) throws WriterException {
        if (currentNetworkPanel == null) {
            return true;
        }
        boolean canClose = networkCanBeClosed(currentNetworkPanel);
        if (canClose) {
            mainPanel.getNetworksTabPanel().remove(currentNetworkPanel);
            if (networkPanels.isEmpty()) {
                mainPanel.setToolBarPanel(NetworkPanel.WorkingMode.EDITION);
                mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
            }
        }
        return canClose;
    }
    
    /**
     * Closes the selected network frame.
     *
     * @return true if the network has been closed; otherwise, false.
     */
    public boolean closePanel(ZoomableContentPanel panel) {
        return panel.close();
    }
    
    /**
     * Process that executes when the user is trying to close the application.
     */
    private void closeApplication() throws WriterException {
        boolean allClosed = true;
        while (allClosed && !networkPanels.isEmpty()) {
            allClosed = closeCurrentNetwork();
        }
        if (allClosed) {
            System.exit(0);
        }
    }
    
    
    /**
     * Creates an expanded network from current network
     *
     * @param probNet               the network to be expanded
     * @param preResolutionEvidence evidence to be added and propagated in the expanded network
     */
    private void expandNetwork(ProbNet probNet, EvidenceCase preResolutionEvidence) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        NetworkPanel networkPanelMID = getCurrentNetworkPanel();
        String path = (new File(networkPanelMID.getNetworkFile())).getParent();
        InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
                                                                                    GUIUtils.getOwner(mainPanel), null);
        costEffectivenessDialog.getMulticriteriaPanel().setEnabled(false);
        if (costEffectivenessDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Cancel) {
            return;
        }
//		ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNet);
//		String fileName = probNet.getName() + "_expanded";
//
//		expandedNetwork.setName(fileName);
        String networkName = probNet.getName();
        //Expanded ID has midname_extended.pgmx
        String fileName = networkName.substring(0, networkName.lastIndexOf('.'));
        
        fileName = fileName + stringDatabase.getString("CostEffectiveness.ExpandNetwork.FileName") + ".pgmx";
        
        ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNet, preResolutionEvidence, fileName);
        
        NetworkPanel networkPanel = createNewFrame(expandedNetwork);
        //If enabled "save" tries to create the .bak file and throws an exception
        mainPanel.getMainPanelMenuAssistant()
                 .setOptionEnabled(ActionCommands.SAVE_OPEN_NETWORK.getCommandName(), false);

//		networkPanel.setNetworkFile(fileName );
        
        //Stores the full path
        networkPanel.setNetworkFile(path + File.separator + fileName);
        networkPanel.getEditorPanel()
                    .getEvidenceManager()
                    .setEvidence(preResolutionEvidence, new ArrayList<>());
        networkPanels.add(networkPanel);
    }

//	/**
//	 * expand the network like it would be done in CE analysis to show it in the
//	 * GUI
//	 */
//	private void expandNetworkCE(ProbNet probNet, EvidenceCase preResolutionEvidence) {
//		if (!getCurrentNetworkPanel().getProbNet().getInferenceOptions().getMultiCriteriaOptions()
//				.isCeOptionsShowed()) {
//			InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
//					GUIUtils.getOwner(mainPanel), MulticriteriaOptions.Type.COST_EFFECTIVENESS);
//			if (costEffectivenessDialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
//				return;
//			}
//		}
//
//		EvidenceCase evidence = new EvidenceCase(preResolutionEvidence);
//
//		ProbNet probNetCopy = probNet.deepCopy();
//
//		EvidenceCase evidenceCase = new EvidenceCase();
//		for (Finding finding : evidence.getFindings()) {
//			String baseName = finding.getVariable().getBaseName();
//			int slice = finding.getVariable().getTimeSlice();
//			Variable variable = null;
//			try {
//				variable = probNetCopy.getVariable(baseName, slice);
//				if (variable.getVariableType().equals(VariableType.NUMERIC)) {
//					Finding findingCopy = new Finding(variable, finding.getNumericalValue());
//					findingCopy.setStateIndex(finding.getStateIndex());
//					evidenceCase.addFinding(findingCopy);
//
//				} else if (variable.getVariableType().equals(VariableType.DISCRETIZED) || variable.getVariableType()
//						.equals(VariableType.FINITE_STATES)) {
//					Finding findingCopy = new Finding(variable, variable.getState(finding.getState()));
//					evidenceCase.addFinding(findingCopy);
//				}
//			} catch (NodeNotFoundException e) {
//				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//						"NodeNotFoundException", "Variable " + baseName + " not found."), null);
//				localizedException.showException();
//			} catch (IncompatibleEvidenceException e) {
//				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//						"IncompatibleEvidenceException", "Conflict in evidence variables."), null);
//				localizedException.showException();
//			} catch (InvalidStateException e) {
//				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//						"InvalidStateException", variable.getName(), finding.getState()), null);
//				localizedException.showException();
//			}
//		}
//		double maxX = 0.0;
//		for (Node node : probNetCopy.getNodes()) {
//			if (node.getCoordinateX() > maxX) {
//				maxX = node.getCoordinateX();
//			}
//		}
//		ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNetCopy);
//		try {
//			evidenceCase.extendEvidence(expandedNetwork);
//		} catch (IncompatibleEvidenceException e) {
//			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//					"IncompatibleEvidenceException", "Conflict in evidence variables."), null);
//			localizedException.showException();
//		} catch (InvalidStateException e) {
//			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//					"InvalidStateException"), null);
//			localizedException.showException();
//		} catch (WrongCriterionException e) {
//			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
//					"WrongCriterionException", e.getCause()), null);
//			localizedException.showException();
//		}
//		//            expandedNetwork = CostEffectivenessAnalysis.adaptMIDforCE(expandedNetwork, evidenceCase);
//
//		// TODO apply changes for transitions at cycle start, end or half cycle.
//		TemporalNetOperations.applyDiscountToUtilityNodes(expandedNetwork);
//		TemporalNetOperations.transformToID(expandedNetwork);
//
//		String fileName = probNetCopy.getName() + "_expandedCE";
//		expandedNetwork.setName(fileName);
//		NetworkPanel networkPanel = createNewFrame(expandedNetwork);
//		networkPanel.setNetworkFile(fileName);
//		networkPanel.getEditorPanel().setEvidence(evidenceCase, new ArrayList<EvidenceCase>());
//		networkPanels.add(networkPanel);
//	}
    
    /**
     * This method saves the evidence of the current network to a file
     *
     * @param currentNetworkPanel the current network panel
     */
    private void saveEvidence(NetworkPanel currentNetworkPanel) {
        // TODO Implement
        List<EvidenceCase> evidence = currentNetworkPanel.getEditorPanel().getEvidenceManager().getEvidence();
        evidence.add(0, currentNetworkPanel.getEditorPanel().getEvidenceManager().getPreResolutionEvidence());
        OMFileChooser omFileChooser = new OMFileChooser();
        
        
        File currentDirectory = LocalPreferences.LATEST_OPEN_DIRECTORY.get();
        omFileChooser.setCurrentDirectory(currentDirectory);
        String suggestedFileName = currentNetworkPanel.probNet.getName();
        omFileChooser.setSelectedFile(new File(suggestedFileName));
        omFileChooser.setAcceptAllFileFilterUsed(false);
        if (omFileChooser.showSaveDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            // save the selected file
            System.out.println("Save evidence file " + omFileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    /**
     * This method tries to load evidence into the current network
     *
     * @param currentNetworkPanel the current network panel
     */
    private void loadEvidence(NetworkPanel currentNetworkPanel) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ParsingSourceException, IOException, EmptyDatabaseException, ConstraintViolatedException {
        OMFileChooser evidenceOMFileChooser = new DBReaderOMFileChooser(false);
        evidenceOMFileChooser.setDialogTitle(stringDatabase.getString("LoadEvidence.Title"));
        // Set last used evidence format as default
        String lastFileFilter = LocalPreferences.LATEST_LOADED_EVIDENCE_FORMAT.get();
        evidenceOMFileChooser.setFileFilter(lastFileFilter);
        if ((evidenceOMFileChooser.showOpenDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION)) {
            // load the selected file
            System.out.println("Load evidence file " + evidenceOMFileChooser.getSelectedFile().getAbsolutePath());
            CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
            CaseDatabaseReader caseDbReader;
            try {
                caseDbReader = caseDbManager
                        .getReader(FilenameUtils.getExtension(evidenceOMFileChooser.getSelectedFile().getName()));
            } catch (NoWriterForExtensionException e) {
                throw new UnrecoverableException(e);
            }
            ProbNet currentNet = currentNetworkPanel.getProbNet();
            CaseDatabase caseDatabase = caseDbReader.load(evidenceOMFileChooser.getSelectedFile());
            List<Variable> variables = caseDatabase.getVariables();
            int[][] cases = caseDatabase.getCases();
            for (int i = 0; i < cases.length; ++i) {
                EvidenceCase newEvidenceCase = new EvidenceCase();
                for (int j = 0; j < cases[i].length; ++j) {
                    // Ignore missing values
                    if (!variables.get(j).getStateName(cases[i][j]).isEmpty()
                            && !variables.get(j).getStateName(cases[i][j]).equals("?")) {
                        Variable variable = currentNet.getVariable(variables.get(j).getName());
                        int stateIndex = variable.getStateIndex(variables.get(j)
                                                                         .getStateName(cases[i][j]));
                        if (stateIndex == -1) continue;
                        newEvidenceCase.addFinding(new Finding(variable, stateIndex));
                    }
                    
                }
                currentNetworkPanel.getEditorPanel().getEvidenceManager().addNewEvidenceCase(newEvidenceCase);
            }
            // save format extension in preferences
            LocalPreferences.LATEST_LOADED_EVIDENCE_FORMAT.set(((FileFilterBasic) evidenceOMFileChooser.getFileFilter()).getFilterExtension());
            LocalPreferences.LATEST_OPEN_DIRECTORY.set(evidenceOMFileChooser.getSelectedFile());
            
        }
    }
    
    /**
     * This method undoes the last operation on the actual network.
     */
    private void undo() {
        try {
            undoRedo(true);
        } catch (CannotUndoException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * This method re-does the last undone operation on the actual network.
     */
    private void redo() {
        try {
            undoRedo(false);
        } catch (CannotRedoException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * This method undoes or re-does an operation on the actual network.
     *
     * @param undoOperation - if true, an undo must be performed; if false, a redo will be
     *                      performed.
     *
     * @throws CannotUndoException - if undo can't be performed.
     * @throws CannotRedoException - if redo can't be performed.
     */
    private void undoRedo(boolean undoOperation) throws CannotUndoException, CannotRedoException {
        NetworkPanel networkPanel = getCurrentNetworkPanel();
        networkPanel.getEditorPanel().getVisualNetwork().setSelectedAllObjects(false);
        if (undoOperation) {
            networkPanel.getEditorPanel().getVisualNetwork().getProbNet().getPNESupport().undo();
        } else {
            networkPanel.getEditorPanel().getVisualNetwork().getProbNet().getPNESupport().redo();
        }
    }
    
    /**
     * This method activates an edition option for the current network.
     *
     * @param newEditionMode new edition mode to set.
     */
    private void activateEditionMode(String newEditionMode) {
        NetworkPanel networkPanel = getCurrentNetworkPanel();
        networkPanel.setEditionMode(newEditionMode);
        mainPanel.getMainPanelMenuAssistant().setEditionOption(newEditionMode, networkPanel.isThereDataStored());
    }
    
    /**
     * This method establishes the network working mode (edition or inference),
     * by setting the opposite to the current one.
     */
    private void toggleWorkingMode() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        NetworkPanel.WorkingMode currentWorkingMode = getCurrentNetworkPanel().getWorkingMode();
        NetworkPanel.WorkingMode newWorkingMode = switch (currentWorkingMode) {
            case EDITION -> NetworkPanel.WorkingMode.INFERENCE;
            case INFERENCE -> NetworkPanel.WorkingMode.EDITION;
        };
        setWorkingMode(currentWorkingMode, newWorkingMode);
    }
    
    private void setWorkingMode(NetworkPanel.WorkingMode currentWorkingMode, NetworkPanel.WorkingMode newWorkingMode) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        boolean performInference = true;
        boolean isTemporal;
        boolean isMulticriteria = false;
        
        ProbNet probNet = getCurrentNetworkPanel().getProbNet();
        
        isTemporal = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
        if (probNet.getDecisionCriteria() != null && probNet.getDecisionCriteria().size() > 1) {
            isMulticriteria = true;
        }
        boolean requiredInfereceOptions = false;
        if (isTemporal) {
            requiredInfereceOptions = true;
        }
        
        if (isMulticriteria) {
            requiredInfereceOptions = true;
        }
        
        if (currentWorkingMode == NetworkPanel.WorkingMode.EDITION && requiredInfereceOptions) {
            // Show multicriteria dialog if the probnet has at least two criteria and have utility nodes
            InferenceOptionsDialog dialog = new InferenceOptionsDialog(probNet, GUIUtils.getOwner(mainPanel), MulticriteriaOptions.Type.UNICRITERION);
            
            if (dialog.getSelectedOption() == OkCancelDialog.ChosenOption.Cancel) {
                newWorkingMode = NetworkPanel.WorkingMode.EDITION;
                performInference = false;
            }
            // Set as launched
            //getCurrentNetworkPanel().getProbNet().getInferenceOptions().setLaunchedBefore(performInference);
        }
        
        mainPanel.setToolBarPanel(newWorkingMode);
        mainPanel.changeWorkingModeButton(newWorkingMode);
        if (!getNetworkPanels().isEmpty()) {
            getCurrentNetworkPanel().setWorkingMode(newWorkingMode);
        }
        getCurrentNetworkPanel().setSelectedAllObjects(false);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(getCurrentNetworkPanel());
        
        try {
            if (performInference) {
                switch (newWorkingMode) {
                    case EDITION -> {
                        // getCurrentNetworkPanel().removeAllFindings(); //Suppressed the elimination of findings on returning to Edition Mode
                        //TODO: has the following piece of code sense with the task scenario?
                        //TODO: review inferenceAlgorithm variable in NetworkEditorPanel, specially in removeNodeEvidenceInAllCases
                        //if (getCurrentNetworkPanel().getInferenceAlgorithm() != null) {
                        //    getCurrentNetworkPanel().setInferenceAlgorithm(null);
                        //}
                    }
                    case INFERENCE -> {
                        getCurrentNetworkPanel().updateIndividualProbabilitiesAndUtilities();
                        mainPanel.getInferenceToolBar()
                                 .setCurrentEvidenceCaseName(getCurrentNetworkPanel().getCurrentCase());
                    }
                }
            }
        } finally {
            getCurrentNetworkPanel().updateNodesExpansionState(newWorkingMode);
            mainPanel.adaptToolBarSize();
        }
    }
    
    /**
     * This method establishes the new expansion threshold of the network.
     *
     * @param newValue new value for expansion threshold
     */
    private void setNewExpansionThreshold(Double newValue) {
        getCurrentNetworkPanel().setExpansionThreshold(newValue);
        getCurrentNetworkPanel().setSelectedAllNodes(false);
        mainPanel.getMainPanelMenuAssistant()
                 .updateOptionsNewWorkingMode(NetworkPanel.WorkingMode.INFERENCE, getCurrentNetworkPanel());
        getCurrentNetworkPanel().updateNodesExpansionState(NetworkPanel.WorkingMode.INFERENCE);
    }
    
    /**
     * This method responds to the navigation among the evidence cases option
     * selected by the user.
     *
     * @param command the Action Command corresponding to the selected option
     */
    private void evidenceCasesNavigationOption(String command) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException {
        switch (command) {
            case "CREATE_NEW_EVIDENCE_CASE" -> getCurrentNetworkPanel().createNewEvidenceCase();
            case "GO_TO_FIRST_EVIDENCE_CASE" -> getCurrentNetworkPanel().goToFirstEvidenceCase();
            case "GO_TO_PREVIOUS_EVIDENCE_CASE" -> getCurrentNetworkPanel().goToPreviousEvidenceCase();
            case "GO_TO_NEXT_EVIDENCE_CASE" -> getCurrentNetworkPanel().goToNextEvidenceCase();
            case "GO_TO_LAST_EVIDENCE_CASE" -> getCurrentNetworkPanel().goToLastEvidenceCase();
            case "CLEAR_OUT_ALL_EVIDENCE_CASES" -> getCurrentNetworkPanel().clearOutAllEvidenceCases();
        }
        mainPanel.getMainPanelMenuAssistant().updateOptionsEvidenceCasesNavigation(getCurrentNetworkPanel());
        mainPanel.getMainPanelMenuAssistant().updateOptionsPropagationTypeDependent(getCurrentNetworkPanel());
    }
    
    /**
     * This method sets the inference options.
     */
    private void setPropagationOptions() {
        NetworkPanel networkPanel = getCurrentNetworkPanel();
        new PropagationOptionsDialog(GUIUtils.getOwner(networkPanel.getEditorPanel()), networkPanel.getEditorPanel(),
                                     networkPanel.getMainPanel().getInferenceToolBar())
                .setVisible(true);
        mainPanel.getMainPanelMenuAssistant().updatePropagateEvidenceButton();
    }
    
    /**
     * This method sets the multicriteria options
     *
     * @param networkPanel the network panel
     */
    private void setInferenceOptions(NetworkPanel networkPanel) {
        InferenceOptionsDialog dialog = new InferenceOptionsDialog(networkPanel.getProbNet(),
                                                                   GUIUtils.getOwner(mainPanel), null);
        //MulticriteriaDialog dialog = new MulticriteriaDialog(networkPanel.getProbNet(), GUIUtils.getOwner(mainPanel));
    }
    
    /**
     * Sets the mode of painting the nodes.
     *
     * @param byTitle if true, then the texts that appear into the nodes will be
     *                their titles; if false, these texts will be their name.
     */
    private void activateByTitle(boolean byTitle) {
        NetworkPanel actualNetwork = getCurrentNetworkPanel();
        if (actualNetwork.getByTitle() != byTitle) {
            actualNetwork.setByTitle(byTitle);
            mainPanel.getMainPanelMenuAssistant().setByTitle(byTitle);
        }
    }
    
    /**
     * This method increments the zoomManager of the current panel.
     *
     * @param zoomableContentPanel network whose zoomManager will be changed.
     */
    private void incrementZoom(ZoomableContentPanel zoomableContentPanel) {
        setZoom(zoomableContentPanel, zoomableContentPanel.getZoom() + zoomChangeValue);
    }
    
    /**
     * This method decrements the zoomManager of the current panel.
     *
     * @param zoomableContentPanel network whose zoomManager will be changed.
     */
    private void decrementZoom(ZoomableContentPanel zoomableContentPanel) {
        setZoom(zoomableContentPanel, zoomableContentPanel.getZoom() - zoomChangeValue);
    }
    
    /**
     * Sets the zoomManager of the current panel and updates the menu and the toolbar.
     *
     * @param zoomableContentPanel network whose zoomManager will be changed.
     * @param value                new zoomManager value.
     */
    private void setZoom(ZoomableContentPanel zoomableContentPanel, double value) {
        zoomableContentPanel.setZoom(value);
        double newZoom = zoomableContentPanel.getZoom();
        mainPanel.getMainPanelMenuAssistant().setZoom(newZoom);
    }
    
    /**
     * Returns current list of opened network panels
     *
     * @return current list of opened network panels
     */
    public List<NetworkPanel> getNetworkPanels() {
        return networkPanels;
    }
    
    private void showDecisionTree(NetworkPanel networkPanel) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates, NotEnoughMemoryException {
        try {
            DecisionTreeWindow decisionTree = new DecisionTreeWindow(networkPanel);
            mainPanel.addCloseableTab("Decision tree for " + networkPanel.probNet.getName(), decisionTree);
            mainPanel.getMainPanelMenuAssistant().updateOptionsDecisionTree(decisionTree);
            mainPanel.getNetworksTabPanel().setSelectedComponent(decisionTree);
        } catch (OutOfMemoryError e) {
            throw new NotEnoughMemoryException(e);
        }
    }
    
    private void showOptimalStrategy(NetworkPanel networkPanel) throws IncompatibleEvidenceException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        /*
        22/10/2014
        Solving issue 195
        https://bitbucket.org/cisiad/org.openmarkov.issues/issue/195/exception-after-deleting-dan-node-and
        When the strategy was calculated for a network, then, if the network was modified, the strategy was not updated.
        The reason is that the algorithm, VariableEliminationDAN, was in 'postresolution' mode. Thus, the new strategy
        was not being calculated. Now, if the network is modified, we set the algorithm to null, as when
        the mode is changed from inference to edition.
         */
        if (networkPanel.getModified()) {
            //TODO: revise this piece of code under the new task paradigm
            //networkPanel.setInferenceAlgorithm(null);
        }
        
        ProbNet probNet = networkPanel.getProbNet();
        
        if (networkPanel.getProbNet().getNetworkType().equals(DecisionAnalysisNetworkType.getUniqueInstance())) {
            DANEvaluation eval = new DANDecompositionIntoSymmetricDANsEvaluation(probNet, networkPanel.getEditorPanel()
                                                                                                      .getEvidenceManager()
                                                                                                      .getPreResolutionEvidence());
            StrategyTree strategyTree = ((StrategyCarrier) eval.getUtility()).getStrategyTrees()[0];
            
            //OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel), probNet, inferenceAlgorithm);
            strategyTree.pruneAndGraftNode("OD");
            OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel),
                                                                                    probNet, strategyTree);
            optimalStrategyDialog.setVisible(true);
            
            // MID or ID
        } else {
            
            VEOptimalIntervention veOptimalStrategy = null;
            try {
                veOptimalStrategy = new VEOptimalIntervention(probNet,
                                                              networkPanel.getEditorPanel()
                                                                          .getEvidenceManager()
                                                                          .getPreResolutionEvidence());
            } catch (NotEvaluableNetworkException.NotApplicableNetwork |
                     NotEvaluableNetworkException.UnsatisfiedConstraints | IncompatibleEvidenceException |
                     ConstraintViolatedException e) {
                throw new UnrecoverableException(e);
            }
            
            try {
                //OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel), probNet, inferenceAlgorithm);
                OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel),
                                                                                        probNet, veOptimalStrategy);
                optimalStrategyDialog.setVisible(true);
            } catch (NonProjectablePotentialException e) {
                throw new UnrecoverableException(e);
            }
        }
        
    }
    
    /**
     * @param buffer    {@code StringBuffer}
     * @param mainPanel {@code MainPanel}
     */
    private static void showTextWindow(StringBuilder buffer, MainPanel mainPanel) {
        JFrame frame = new JFrame("Cost-Effectiveness analysis");
        String text = buffer.toString();
        JTextArea textArea = new JTextArea(40, getMaxCharsInALine(text));
        frame.getContentPane().add(textArea, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(textArea);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
        textArea.setText(text);
        frame.setLocationRelativeTo(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * @param text {@code String}
     *
     * @return {@code int}
     */
    private static int getMaxCharsInALine(String text) {
        int maxLengthLine = 0;
        if (text != null) {
            int position = 0;
            int nextEndLine;
            int textLength = text.length();
            do {
                nextEndLine = text.indexOf('\n', position);
                if (nextEndLine > 0) {
                    int lengthLine = nextEndLine - position;
                    if (lengthLine > maxLengthLine) {
                        maxLengthLine = lengthLine;
                    }
                    position = nextEndLine + 1;
                }
            } while (nextEndLine != -1 && position < textLength);
        }
        return maxLengthLine;
    }
    
    @Override public void componentResized(ComponentEvent e) {
        mainPanel.adaptToolBarSize();
    }
    
    @Override public void componentMoved(ComponentEvent e) {
    
    }
    
    @Override public void componentShown(ComponentEvent e) {
    
    }
    
    @Override public void componentHidden(ComponentEvent e) {
    
    }
    
}

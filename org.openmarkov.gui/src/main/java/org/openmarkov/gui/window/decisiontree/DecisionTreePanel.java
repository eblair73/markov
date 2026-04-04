/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions.Type;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;

import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.menu.TreeContextualMenu;
import org.openmarkov.gui.util.TreeNodeToDot;
import org.openmarkov.core.model.decisiontree.operation.DecisionTreeManager;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.inference.decisiontree.operation.DecisionTreeManagerImpl;

import javax.swing.*;
import javax.swing.tree.TreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * A scrollable container that displays and manages a visual decision tree.
 * It handles tree generation, zoomManager levels, and dynamic expansion through
 * inference.
 */
@SuppressWarnings("serial")
public class DecisionTreePanel extends JScrollPane {

    /** The visual tree component. */
    protected VisualDecisionTree jTree;

    /** Factory for creating contextual menus based on node types. */
    private final ContextualMenuFactory contextualMenuFactory;

    /** Listener for mouse and action events. */
    private final TreePanelListener listener;

    private final DecisionTreeManager decisionTreeManager;

    public VisualDecisionTree getJTree() {
        return jTree;
    }

    /**
     * Constructs a panel and builds the decision tree for the given network.
     * 
     * @param probNet The probabilistic network to represent.
     * 
     * @throws NotEvaluableNetworkException if the network cannot be evaluated
     * @throws IncompatibleEvidenceException if the evidence is incompatible with the network
     * @throws NonProjectablePotentialException if the potential cannot be projected
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if different sizes in potentials and states occurs
     */
    public DecisionTreePanel(ProbNet probNet)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        listener = new TreePanelListener();
        contextualMenuFactory = new ContextualMenuFactory(listener);
        this.decisionTreeManager = new DecisionTreeManagerImpl();

        // DecisionTreeElement root = DecisionTreeBuilder.buildDecisionTree (probNet);
        DecisionTreeElement root = buildDecisionTree(probNet);
        updateVisualInformation(root);
    }

    /**
     * Rebuilds the visual model and refreshes the viewport.
     * 
     * @param root The root element of the decision tree.
     */
    private void updateVisualInformation(DecisionTreeElement root) {
        DecisionTreeModel model = new DecisionTreeModel(root);
        jTree = new VisualDecisionTree(model);
        jTree.addMouseListener(listener);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        setViewportView(jTree);
        setBackground(Color.white);

    }

    private static final int DEFAULT_DEPTH = 5;

    /**
     * Builds a decision tree from a ProbNet with a default depth.
     * 
     * @see #buildDecisionTree(ProbNet, int)
     */
    public DecisionTreeElement buildDecisionTree(ProbNet probNet)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return buildDecisionTree(probNet, DEFAULT_DEPTH);
    }

    /**
     * Builds a decision tree from a ProbNet up to a specified depth.
     * 
     * @param probNet The probabilistic network.
     * @param depth   The maximum depth of the decision tree.
     * @return The root element of the constructed decision tree.
     * 
     * @throws NotEvaluableNetworkException if the network cannot be evaluated
     * @throws IncompatibleEvidenceException if the evidence is incompatible with the network
     * @throws NonProjectablePotentialException if the potential cannot be projected
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if different sizes in potentials and states occurs
     */
    public DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return decisionTreeManager.buildDecisionTree(probNet, depth);
    }

    /**
     * Returns the zoomManager.
     *
     * @return the zoomManager.
     */
    protected double getZoom() {
        return jTree.getZoom();
    }

    /**
     * Sets the zoomManager.
     *
     * @param zoom the zoomManager to set.
     */
    protected void setZoom(Double zoom) {
        jTree.setZoom(zoom);
        repaint();
    }

    /**
     * Expands the tree by one additional level of inference.
     */
    public void inferenceExpandNextLevel()
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceExpandLevels(1);
    }

    public void inferenceExpandLevels(int n)
            throws NotEvaluableNetworkException, NonProjectablePotentialException, IncompatibleEvidenceException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        DecisionTreeModel auxModel = (DecisionTreeModel) jTree.getModel();
        DecisionTreeBranchPanel root = (DecisionTreeBranchPanel) auxModel.getRoot();
        decisionTreeManager.expandLevels(root.getTreeBranch(), n);
        updateVisualInformation(root.getTreeBranch());
    }

    public void inferenceExpandAllLevels()
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceExpandLevels(Integer.MAX_VALUE);
    }

    /**
     * Internal listener to handle GUI actions and mouse interactions.
     */
    private class TreePanelListener implements ActionListener, MouseListener {

        /**
         * Dispatches commands for expansion, opening networks, or saving to Graphviz.
         * 
         * @param e The action event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            switch (ActionCommands.of(actionCommand)) {
                case ActionCommands.TREE_EXPAND_NEXT:
                    System.out.println("Expanding some levels");
                    // Expand N levels
                    try {
                        inferenceExpandNextLevel();
                    } catch (NotEvaluableNetworkException | IncompatibleEvidenceException
                            | NonProjectablePotentialException
                            | PotentialOperationException.DifferentSizesInPotentialsAndStates
                            | NotSupportedOperationException ex) {
                        throw new UnrecoverableException(ex);
                    }
                    break;
                case ActionCommands.TREE_EXPAND_ALL:
                    System.out.println("Expanding all levels");
                    // Expand all levels
                    try {
                        inferenceExpandAllLevels();
                    } catch (NotEvaluableNetworkException | IncompatibleEvidenceException
                            | NonProjectablePotentialException
                            | PotentialOperationException.DifferentSizesInPotentialsAndStates
                            | NotSupportedOperationException ex) {
                        throw new UnrecoverableException(ex);
                    }
                    break;
                case ActionCommands.TREE_OPEN_NETWORK:
                    System.out.println("Opening associated network");
                    openAssociatedNetwork();
                    // Open tree
                    break;
                case ActionCommands.TREE_SHOW_CEP:
                    System.out.println("Opening associated CEP");
                    openAssociatedCEP();

                    break;
                case ActionCommands.TREE_SAVE_GRAPHVIZ:
                    System.out.println("Doing something wonderful");
                    // Show CEP or utility
                    TreeNodeToDot tree2dot = new TreeNodeToDot();
                    Object selectedComponent = jTree.getLastSelectedPathComponent();
                    if (selectedComponent instanceof DecisionTreeNodePanel treeNodePanel) {
                        DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
                        try {
                            tree2dot.paintDTNode(treeNode);
                        } catch (IOException ex) {
                            throw new UnrecoverableException(ex);
                        }
                    }
                    break;
                case null, default:
            }
        }

        private void openAssociatedCEP() {
            Object selectedComponent = jTree.getLastSelectedPathComponent();
            if (selectedComponent instanceof DecisionTreeNodePanel treeNodePanel) {
                DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
                CEPDialog cepDialog = new CEPDialog(null, (CEP) (treeNode.getUtility()), treeNode.getNetwork());
                cepDialog.setVisible(true);
            }
        }

        private void openAssociatedNetwork() {

            Object selectedComponent = jTree.getLastSelectedPathComponent();
            if (selectedComponent instanceof DecisionTreeNodePanel) {
                MainGUI.INSTANCE.mainPanel
                        .getMainPanelListenerAssistant()
                        .openNetwork(getNetwork(selectedComponent));
            }

        }

        public static ProbNet getNetwork(Object selectedComponent) {

            DecisionTreeNodePanel treeNodePanel = (DecisionTreeNodePanel) selectedComponent;
            DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
            return treeNode.getNetwork();
        }

        /* Listener methods */
        // Open tree contextual menu on right click
        /**
         * Handles right-click events to show contextual menus.
         * 
         * @param e The mouse event.
         */
        @Override
        public void mouseClicked(MouseEvent e) {

            if (SwingUtilities.isRightMouseButton(e)) {

                int row = jTree.getClosestRowForLocation(e.getX(), e.getY());
                jTree.setSelectionRow(row); // Select the right-clicked component

                /*
                 * Show menu only if the tree element:
                 * 1. is a node
                 * 2. is a chance or decision one
                 */
                Object selectedComponent = jTree.getLastSelectedPathComponent();
                if (selectedComponent instanceof DecisionTreeNodePanel) {
                    NodeType nodeType = ((DecisionTreeNodePanel) selectedComponent).getNodeType();
                    // if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION) {
                    if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION || nodeType == NodeType.UTILITY) {
                        // Get menu from the contextualMenuFactory
                        Type type = getNetwork(selectedComponent).getInferenceOptions()
                                .getMultiCriteriaOptions()
                                .getMulticriteriaType();
                        TreeContextualMenu treeMenu = (TreeContextualMenu) contextualMenuFactory
                                .getTreeContextualMenu(type == Type.COST_EFFECTIVENESS);
                        treeMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }
    }

    /**
     * Retrieves the root logical node of the tree.
     * 
     * @return The {@link DecisionTreeNode} at the top of the hierarchy.
     */
    public DecisionTreeNode getDecisionTreeNode() {
        VisualDecisionTree dt = getJTree();
        TreeModel model = dt.getModel();
        DecisionTreeBranchPanel branchPanel = (DecisionTreeBranchPanel) model.getRoot();
        DecisionTreeBranch root = branchPanel.getTreeBranch();
        return root.getChild();
    }
}

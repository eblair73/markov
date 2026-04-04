/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.dialog.treeadd.IconFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI representation of a logical decision tree node.
 * It manages node-specific icons and displays utility values for terminal nodes.
 * @author CISIAD, UNED
 * @version 2019
 */
@SuppressWarnings("serial") 
public class DecisionTreeNodePanel extends DecisionTreeElementPanel {
	
    /** Cache for chance node icons to improve performance. */
    private static final Map<String, Icon> chanceNodeIconPool = new HashMap<>();
    /** Cache for decision node icons. */
    private static final Map<String, Icon> decisionNodeIconPool = new HashMap<>();
    /** Cache for utility node icons. */
    private static final Map<String, Icon> utilityNodeIconPool = new HashMap<>();
    
    /** The underlying logical tree node. */
    private final DecisionTreeNode treeNode;
    
    /**
     * Constructs a panel for a specific decision tree node and initializes its icon.
     * @param treeNode The logical node to be represented.
     */
    public DecisionTreeNodePanel(DecisionTreeNode treeNode) {
        this.treeNode = treeNode;
        leftLabel.setIcon(createNodeIcon(treeNode.getVariable(), treeNode.getNodeType()));
    }
    
    /**
     * Gets the associated logical tree node.
     * @return The {@link DecisionTreeNode} linked to this panel.
     */
    public DecisionTreeNode getTreeNode() {
        return treeNode;
    }
    
    
    /**
     * Retrieves an existing icon from the pool or creates a new one via {@link IconFactory}.
     * @param variable The variable associated with the node.
     * @param nodeType The type of the node (CHANCE, DECISION, or UTILITY).
     * @return The generated or cached {@link Icon}.
     */
    protected static Icon createNodeIcon(Variable variable, NodeType nodeType) {
        Font textIconFont = new Font("Helvetica", Font.BOLD, 15);
        Icon icon = null;
        switch (nodeType) {
            case CHANCE: {
                icon = chanceNodeIconPool.get(variable.getName());
                if (icon == null) {
                    icon = IconFactory.createChanceIcon(variable.getName(), textIconFont);
                    chanceNodeIconPool.put(variable.getName(), icon);
                }
                break;
            }
            case DECISION: {
                icon = IconFactory.createDecisionIcon(variable.getName(), textIconFont);
                decisionNodeIconPool.put(variable.getName(), icon);
                break;
            }
            case UTILITY: {
                icon = IconFactory.createUtilityIcon(variable.getName(), textIconFont);
                utilityNodeIconPool.put(variable.getName(), icon);
                break;
            }
        }
        return icon;
    }
    
    /**
     * Updates the right label with formatted utility data if the node is a utility type.
     * {@inheritDoc}
     */
    @Override 
    public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (treeNode.getNodeType() == NodeType.UTILITY) {
            rightLabel.setText(treeNode.formatUtility(df, false));
        }
    }
    
    /**
     * Gets the type of the underlying node.
     * @return The {@link NodeType} of the associated tree node.
     */
    public NodeType getNodeType() {
        return treeNode.getNodeType();
    }
}

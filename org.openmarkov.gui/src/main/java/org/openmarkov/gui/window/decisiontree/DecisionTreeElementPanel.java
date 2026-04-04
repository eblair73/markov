/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base panel for representing decision tree elements in the GUI.
 * Provides the basic layout and structure for nodes and branches.
 */
@SuppressWarnings("serial") 
public abstract class DecisionTreeElementPanel extends JPanel {

	/** Container of SummaryBox' text or the variable's icon. */
	protected final JLabel leftLabel = new JLabel();
	
	/**Container for leaf specific data, such as potential descriptions or values. */
	protected final JLabel rightLabel = new JLabel();

    /** List of child panels in the tree hierarchy. */
	protected final List<DecisionTreeElementPanel> children;
	
    /** Formatter for displaying numerical values with four decimal places. */
    final DecimalFormat df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    /**
     * Initializes the panel with a BorderLayout and default white background.
     */
	public DecisionTreeElementPanel() {
		super(new BorderLayout());
		this.add(leftLabel, BorderLayout.WEST);
		this.add(rightLabel, BorderLayout.CENTER);
		setBackground(Color.white);

		children = new ArrayList<>();
	}

    /**
     * Updates the panel's visual state based on its current status in the JTree.
     * * @param selected True if the element is selected.
     * @param expanded True if the element's children are visible.
     * @param leaf True if the element has no children.
     * @param row The display row index.
     * @param hasFocus True if the element currently has keyboard focus.
     */
	public abstract void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus);

    /**
     * Returns the list of child panels.
     * @return A list containing the children of this panel.
     */
	public List<DecisionTreeElementPanel> getChildren() {
		return children;
	}

    /**
     * Adds a child panel to this element's hierarchy.
     * @param child The panel to be added as a child.
     */
	public void addChild(DecisionTreeElementPanel child) {
		children.add(child);
	}

}

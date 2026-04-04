/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Custom cell renderer for decision tree elements in a JTree.
 * It updates the visual representation of each tree cell based on its state.
 */
@SuppressWarnings("serial")
public class DecisionTreeCellRenderer implements TreeCellRenderer {

	@Override public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (object instanceof DecisionTreeElementPanel) {
			((DecisionTreeElementPanel) object).update(selected, expanded, leaf, row, hasFocus);
		}
		return (Component) object;
	}

}

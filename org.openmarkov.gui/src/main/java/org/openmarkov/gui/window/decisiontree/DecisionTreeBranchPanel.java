/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;

/**
 * GUI representation of a logical decision tree branch.
 * It displays branch-specific information such as variable states and probabilities.
 */
@SuppressWarnings("serial") 
public class DecisionTreeBranchPanel extends DecisionTreeElementPanel {
	
	/** The underlying logical tree branch. */
	private final DecisionTreeBranch treeBranch;

	/**
	 * Constructs a panel for a specific decision tree branch.
	 * @param treeBranch The logical branch to be represented.
	 */
	public DecisionTreeBranchPanel(DecisionTreeBranch treeBranch) {
		super();
		this.treeBranch = treeBranch;
	}
	
	public DecisionTreeBranch getTreeBranch() {
		return treeBranch;
	}

	/**
	 * Builds the text to be shown in the branch
	 * @return A HTML string with the branch description
	 */
	public String getBranchDescriptiontHTML() {
		StringBuilder txtLeft = new StringBuilder("<html><table border=1>");
		DecisionTreeNode parent = treeBranch.getParent();
		if (parent != null && parent.getNodeType() == NodeType.DECISION) {
			if (parent.isBestDecision(treeBranch)) {
				txtLeft.append("<td width=10px bgcolor=red border=0></td>");
			} else {
				txtLeft.append("<td width=10px border=0></td>");
			}
		}
		txtLeft.append("<td align=center border=0>");
		if (treeBranch.getBranchVariable() != null) {
			txtLeft.append(treeBranch.getBranchVariable().getName() + "=");
			txtLeft.append(treeBranch.getBranchState().getName());
		}
		if (parent != null && parent.getNodeType() == NodeType.CHANCE) {
			txtLeft.append(" / ");
			txtLeft.append(" P=" + df.format(treeBranch.getBranchProbability()));
		}
		
		txtLeft.append(treeBranch.getChild().formatUtility(df,parent != null));
		txtLeft.append("</td>");
		txtLeft.append("</table></html>");
		return txtLeft.toString();
	}

	/** {@inheritDoc} */
	@Override public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		leftLabel.setText(getBranchDescriptiontHTML());
	}
}

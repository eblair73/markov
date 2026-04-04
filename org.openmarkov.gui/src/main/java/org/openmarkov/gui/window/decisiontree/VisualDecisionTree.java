/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.gui.window.edition.ZoomManager;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

@SuppressWarnings("serial")
/**
 * Visual representation of a decision tree using a JTree component.
 */
public class VisualDecisionTree extends JTree{

	/**
	 * Object to convert coordinates of the screen to the panel and vice versa.
	 */
	protected final ZoomManager zoomManager;
	
	/**
	 * Constructs a VisualDecisionTree with the specified DecisionTreeModel.
	 *
	 * @param model the decision tree model to be displayed.
	 */
	public VisualDecisionTree(DecisionTreeModel model) {
		super(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Allows JTree nodes to accept CR/LF codes
		setShowsRootHandles(true);
		setRowHeight(0);
		setCellRenderer(new DecisionTreeCellRenderer());
		setUI(new DecisionTreeUI());
		zoomManager = new ZoomManager();
	}

	/**
	 * Overwrite 'paint' method to avoid to call it explicitly.
	 *
	 * @param g the graphics context in which to paint.
	 */
	@Override public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2D.scale(zoomManager.getZoom(), zoomManager.getZoom());
		super.paint(g2D);
	}

	public double getZoom() {
		return zoomManager.getZoom();
	}

	/**
	 * Sets the zoomManager.
	 *
	 * @param zoom the zoomManager to set.
	 */
	protected void setZoom(Double zoom) {
		this.zoomManager.setZoom(zoom);
	}


}

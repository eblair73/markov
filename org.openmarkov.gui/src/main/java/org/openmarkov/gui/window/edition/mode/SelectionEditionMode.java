/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@EditionState(name = "Edit.Mode.Selection", icon = "selection.gif") public class SelectionEditionMode
        extends EditionMode {
    /**
     * Indicates if a node has been moved.
     */
    private boolean nodeMoved = false;
    
    /**
     * Information of the movement of the nodes.
     */
    private List<VisualNode> movedNodes;
    /**
     * Current selection state.
     */
    private SelectionState selectionState = SelectionState.DEFAULT;
    
    public SelectionEditionMode(NetworkEditorPanel networkEditorPanel, ProbNet probNet) {
        super(networkEditorPanel, probNet);
        movedNodes = new ArrayList<>();
    }
    
    @Override public void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (e.isControlDown() || e.isShiftDown()) {
            visualNetwork.addToSelection(position, g);
            return;
        }
        VisualElement selectedElement = visualNetwork.selectElementInPosition(position, g);
        setSelectionState((selectedElement != null) ? SelectionState.MOVING : SelectionState.SELECTING);
        if (selectedElement == null) {
            visualNetwork.startSelectionRectangle(position);
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e, Point2D.Double position, Graphics2D g) throws DoEditException {
        switch (selectionState) {
            case SelectionState.DEFAULT -> {
            }
            case SelectionState.MOVING -> {
                if (!nodeMoved) {
                    break;
                }
                movedNodes = visualNetwork.fillVisualNodesSelected();
                if (!movedNodes.isEmpty()) {
                    MoveNodeEdit moveNodeEdit = new MoveNodeEdit(movedNodes);
                    moveNodeEdit.executeEdit();
                }
                nodeMoved = false;
                networkEditorPanel.adjustPanelDimension();
            }
            case SelectionState.SELECTING -> {
                visualNetwork.finishSelectionRectangle(position);
            }
        }
        setSelectionState(SelectionState.DEFAULT);
        networkEditorPanel.repaint();
    }
    
    @Override public void mouseDragged(MouseEvent e, Point2D.Double position, double diffX, double diffY,
                                       Graphics2D g) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (selectionState == SelectionState.MOVING) {
            visualNetwork.moveSelectedElements(diffX, diffY);
            nodeMoved = true;
        } else if (selectionState == SelectionState.SELECTING) {
            visualNetwork.updateSelectionRectangle(diffX, diffY);
        }
        networkEditorPanel.repaint();
    }
    
    /**
     * Changes the state of the selection and carries out the necessary actions
     * in each case.
     *
     * @param newState new mouse state.
     */
    private void setSelectionState(SelectionState newState) {
        
        networkEditorPanel.setCursor(newState.getCursor());
        selectionState = newState;
        
    }
    
}

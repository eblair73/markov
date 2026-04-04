/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.graphic.VisualNode;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code MoveNodeEdi} is a simple edit that allows to modify the position
 * of a group of nodes
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
public class MoveNodeEdit extends PNEdit {
    
    private static final long serialVersionUID = 7578733825996342882L;
    
    private final List<Point2D.Double> lastPositions = new ArrayList<Point2D.Double>();
    private final List<Point2D.Double> newPositions = new ArrayList<Point2D.Double>();
    private final List<String> namesNode = new ArrayList<String>();
    
    /**
     * Creates a new {@code MoveNodeEdit} with the nodes, and new X, Y
     * coordinates.
     *
     * @param movedNodes the nodes that will be edited, with their new
     *                   positions.
     */
    public MoveNodeEdit(List<VisualNode> movedNodes) {
        super(movedNodes.get(0).getNode().getProbNet());
        for (VisualNode visualNode : movedNodes) {
            lastPositions.add(visualNode.getPosition().clone());
            newPositions.add(visualNode.getTemporalPosition().clone());
            namesNode.add(visualNode.getNode().getName());
        }
    }
    
    @Override protected void doEdit() {
        probNet.moveNode(namesNode,newPositions);
    }
    
    @Override public void undo() {
        super.undo();
        probNet.moveNode(namesNode,lastPositions);
    }
}

/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Compound edit that pastes previously copied nodes and links into a network.
 * Duplicate variable names are resolved by appending apostrophes. Potentials
 * are copied and variable references are updated to point to the new nodes.
 */
@SuppressWarnings("serial") public class PasteEdit extends PNEdit {
    private final SelectedContent clipboardContent;
    private SelectedContent pastedContent;
    ArrayList<PNEdit> edits;

    /**
     * Creates a new paste edit.
     *
     * @param probNet          the target network to paste into
     * @param clipboardContent the nodes and links to paste
     */
    public PasteEdit(ProbNet probNet, SelectedContent clipboardContent) {
        super(probNet);
        this.clipboardContent = clipboardContent;
        this.pastedContent = null;
        edits = new ArrayList<>();
    }
    
    @Override protected void doEdit() throws DoEditException {
        newVariables = new HashMap<>();
        edits = new ArrayList<>();
        // Gather new node creation edits
        for (Node node : clipboardContent.nodes()) {
            String oldName = node.getName();
            String newName = oldName;
            while (probNet.containsVariable(newName)) {
                newName += "'";
            }
            Variable variable = new Variable(node.getVariable());
            variable.setName(newName);
            newVariables.put(oldName, newName);
            Point2D.Double position = new Point2D.Double(node.getCoordinateX() + 3.0, node.getCoordinateY());
            AddNodeEdit addNodeEdit = new AddNodeEdit(probNet, variable, node.getNodeType(), position);
            addNodeEdit.executeEdit();
            edits.add(addNodeEdit);
        }
        //Gather link creation edits
        for (Link<Node> link : clipboardContent.links()) {
            String originalSourceNodeName = link.getFrom().getName();
            String originalDestinationNodeName = link.getTo().getName();
            AddLinkEdit addLinkEdit = new AddLinkEdit(probNet, probNet.getVariable(newVariables.get(originalSourceNodeName)),
                                                      probNet.getVariable(newVariables.get(originalDestinationNodeName)), link.isDirected());
            addLinkEdit.executeEdit();
            edits.add(addLinkEdit);
        }
        
        PNEdit finalizer = new PNEdit(this.probNet) {
            @Override protected void doEdit() {
                // Apply node generation edits
                ArrayList<Node> pastedNodes = new ArrayList<>();
                //Apply link creation edits
                List<Link<Node>> pastedLinks = new ArrayList<>();
                for (PNEdit edit : edits) {
                    switch (edit) {
                        case AddNodeEdit addNodeEdit -> pastedNodes.add(addNodeEdit.getNode());
                        case AddLinkEdit linkEdit -> pastedLinks.add(linkEdit.getLink());
                        default -> {
                        }
                    }
                }
                pastedContent = new SelectedContent(pastedNodes, pastedLinks);
                //Replace potentials to already created nodes with copies of copied nodes
                for (Node originalNode : clipboardContent.nodes()) {
                    ArrayList<Potential> newPotentials = new ArrayList<>();
                    Node newNode = probNet.getNode(newVariables.get(originalNode.getName()));
                    for (Potential originalPotential : originalNode.getPotentials()) {
                        Potential potential = originalPotential.copy();
                        for (int i = 0; i < potential.getNumVariables(); ++i) {
                            String variableName = potential.getVariable(i).getName();
                            if (newVariables.containsKey(variableName)) {
                                Variable variable = probNet.getVariable(newVariables.get(variableName));
                                potential.replaceVariable(i, variable);
                            }
                        }
                        if (potential instanceof ExactDistrPotential) {
                            Variable child = ((ExactDistrPotential) potential).getChildVariable();
                            if (newVariables.containsKey(child.getName())) {
                                ((ExactDistrPotential) potential)
                                        .setChildVariable(probNet.getVariable(newVariables.get(child.getName())));
                            }
                        }
                        newPotentials.add(potential);
                    }
                    newNode.setPotentials(newPotentials);
                    // Copy comment too!
                    newNode.setComment(originalNode.getComment());
                    newNode.setRelevance(originalNode.getRelevance());
                    newNode.setPurpose(originalNode.getPurpose());
                    newNode.setAdditionalProperties(originalNode.getAdditionalProperties());
                }
            }
        };
        finalizer.executeEdit();
        edits.add(finalizer);
    }
    
    @Override public void redo() {
        edits.forEach(PNEdit::redo);
        setTypicalRedo(false);
        super.redo();
    }
    
    @Override public void undo() {
        IntStream.range(0, edits.size())
                 .mapToObj(i -> {
                     int realIndex = edits.size() - 1 - i;
                     return edits.get(realIndex);
                 })
                 .forEach(PNEdit::undo);
        super.undo();
    }
    
    /**
     * Returns the pasted content.
     *
     * @return the pastedContent.
     */
    public SelectedContent getPastedContent() {
        return pastedContent;
    }
    
    private HashMap<String, String> newVariables;
}

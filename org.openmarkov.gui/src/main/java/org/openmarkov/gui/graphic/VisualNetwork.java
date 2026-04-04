/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.action.RemoveSelectedEdit;
import org.openmarkov.gui.util.MovedNodeInfo;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;
import org.openmarkov.gui.window.edition.SelectedContent;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.NetworkPanel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * This class implements the visual representation of a network.
 *
 * @author jmendoza 1.0
 * @author jlgozalo 1.1
 * @version 1.2 asaez - modified the constructor, the creation of
 * visual nodes and the order of painting nodes
 */
public class VisualNetwork implements PNEditListener {
    
    /**
     * Network whose visual representation is managed by this object.
     */
    private final ProbNet probNet;
    
    private final MainGUI mainGUI;
    
    /**
     * This variable indicates if nodes must be drawn by title.
     */
    // TODO este valor debe asignarse usando las preferencias de usuario de
    // visualización de redes
    private boolean byTitle = false;
    
    /**
     * List of visual nodes.
     */
    private List<VisualNode> visualNodes = new ArrayList<VisualNode>();
    
    /**
     * List of visual links.
     */
    private final List<VisualLink> visualLinks = new ArrayList<VisualLink>();
    
    /**
     * Set of selected nodes.
     */
    private final Set<VisualNode> selectedNodes = new HashSet<VisualNode>();
    
    /**
     * Set of selected links.
     */
    private final Set<VisualLink> selectedLinks = new HashSet<VisualLink>();
    
    /**
     * This object represents the arrow that is painted when a new link is being
     * created.
     */
    private VisualArrow newLink = null;
    
    /**
     * This object represents the source node of a new link.
     */
    private VisualNode newLinkSource = null;
    
    /**
     * Rectangle used to select various nodes.
     */
    protected SelectionRectangle selection = null;
    
    private boolean isPropagationActive = true;
    
    private NetworkPanel.WorkingMode workingMode = NetworkPanel.WorkingMode.EDITION;
    
    /**
     * Listener to the selection.
     */
    private final Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();
    
    protected Graphics2D g2;
    
    /**
     * Object that assists this panel in the operations with the clipboard.
     */
    private final EditorPanelClipboardAssistant clipboardAssistant;
    
    //private LinkWrapper linkWrapper;
    /**
     * Position of the mouse cursor when it is pressed.
     */
    
    /**
     * Creates a new visual network.
     *
     * @param probNet object that has the information of the network.
     * @param mainGUI the main gui
     */
    public VisualNetwork(ProbNet probNet, MainGUI mainGUI) {
        this.probNet = probNet;
        this.mainGUI = mainGUI;
        this.probNet.getPNESupport().addListener(this);
        this.clipboardAssistant = new EditorPanelClipboardAssistant();
        
        //network.addNetworkChangeListener(this);
        //changed by mpalacios
        constructVisualInfo();
    }
    
    public ProbNet getProbNet() {
        return this.probNet;
    }
    
    /**
     * Calculates the width and height of the panel according to the position of
     * the left-most and bottom-most nodes.
     *
     * @param g graphics where the network is painted.
     *
     * @return an array that contains the lowest and highest X coordinate and
     * the lowest and highest Y coordinate.
     */
    public double[] getNetworkBounds(Graphics2D g) {
        
        double[] networkBounds = {Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE};
        
        for (VisualNode node : visualNodes) {
            Rectangle2D nodeBounds = node.getShape(g).getBounds2D();
            networkBounds[0] = Math.min(nodeBounds.getMinX(), networkBounds[0]);
            networkBounds[1] = Math.max(nodeBounds.getMaxX(), networkBounds[1]);
            networkBounds[2] = Math.min(nodeBounds.getMinY(), networkBounds[2]);
            networkBounds[3] = Math.max(nodeBounds.getMaxY(), networkBounds[3]);
        }
        networkBounds[0] -= 2;
        networkBounds[1] += 2;
        networkBounds[2] -= 2;
        networkBounds[3] += 2;
        
        return networkBounds;
        
    }
    
    @FunctionalInterface
    interface NodeIsToKeep {
        boolean nodeIsToKeep(VisualNode visualNode, List<Node> currentNodesToAdd);
    }
    
    /**
     * This method constructs the lists of the visual nodes and visual links. It
     * only creates visual information for the new nodes and links and delete
     * the visual representation of the nodes and links that don't exist.
     */
    private void constructVisualInfo() {
        reconstructVisualInfo((visualNode, currentNodesToAdd) ->
                currentNodesToAdd.contains(visualNode.getNode())
                        && visualNode.getTemporalPosition().getX()
                        == currentNodesToAdd.get(currentNodesToAdd.indexOf(visualNode.getNode())).getCoordinateX()
                        && visualNode.getTemporalPosition().getX()
                        == currentNodesToAdd.get(currentNodesToAdd.indexOf(visualNode.getNode())).getCoordinateX());
    }
    
    private void reconstructVisualInfo(NodeIsToKeep nodeIsToKeep) {
        List<VisualNode> vNodesToDelete = new ArrayList<VisualNode>();
        List<Node> nodesToAdd = probNet.getNodes();
        for (VisualNode vNode : visualNodes) {
            if (nodeIsToKeep.nodeIsToKeep(vNode, Collections.unmodifiableList(nodesToAdd))) {
                nodesToAdd.remove(vNode.getNode());
            } else {
                vNodesToDelete.add(vNode);
            }
        }
        visualNodes.removeAll(vNodesToDelete);
        for (Node node : nodesToAdd) {
            VisualNode vNode1 = createVisualNode(node);
            visualNodes.add(vNode1);
            vNode1.setByTitle(byTitle);
        }
        List<Link<Node>> links = probNet.getLinks();
        List<VisualLink> vLinksToDelete = new ArrayList<VisualLink>();
        for (VisualLink vLink : visualLinks) {
            Link<Node> linkToCheck = vLink.getLink();
            boolean containsLink = links.contains(linkToCheck);
            boolean containsNodeToDelete = containsNodeToDelete(linkToCheck, vNodesToDelete);
            if (containsLink && !containsNodeToDelete) {
                links.remove(linkToCheck);
            } else {
                vLinksToDelete.add(vLink);
            }
        }
        visualLinks.removeAll(vLinksToDelete);
        int visualNodesCount = visualNodes.size();
        for (Link<Node> link : links) {
            VisualNode vNode1 = null;
            VisualNode vNode2 = null;
            int i = 0;
            while ((i < visualNodesCount) && ((vNode1 == null) || (vNode2 == null))) {
                if (vNode1 == null) {
                    if (link.getFrom().equals(visualNodes.get(i).getNode())) {
                        vNode1 = visualNodes.get(i);
                    }
                }
                if (vNode2 == null) {
                    if (link.getTo().equals(visualNodes.get(i).getNode())) {
                        vNode2 = visualNodes.get(i);
                    }
                }
                i++;
            }
            if ((vNode1 != null) && (vNode2 != null)) {
                visualLinks.add(new VisualLink(link, vNode1, vNode2));
            }
        }
    }
    
    /**
     * Returns whether the link contains nodes to delete
     *
     * @param linkToCheck the link to check
     * @param vNodesToDelete the v nodes to delete
     *
     * @return True iff the link contains the node to delete
     */
    private static boolean containsNodeToDelete(Link<Node> linkToCheck, List<VisualNode> vNodesToDelete) {
        
        for (VisualNode vNode : vNodesToDelete)
            if (linkToCheck.contains(vNode.getNode()))
                return true;
        
        return false;
    }
    
    /**
     * Returns the presentation mode of the text of the nodes.
     *
     * @return true if the title of the nodes is the name or false if it is the
     * name.
     */
    public boolean getByTitle() {
        return byTitle;
    }
    
    /**
     * Changes the presentation mode of the text of the nodes.
     *
     * @param value new value of the presentation mode of the text of the nodes.
     */
    public void setByTitle(boolean value) {
        
        if (byTitle != value) {
            byTitle = value;
        }
        for (VisualNode node : visualNodes) {
            node.setByTitle(value);
        }
        
    }
    
    /**
     * Creates a new list of visual nodes reordering them following
     * this criteria:
     * - first criteria: selection state -&gt; the selected nodes are in
     * the first places of the array.
     * - second criteria: relevance -&gt; the higher the relevance
     * the nearer to the start of the array.
     *
     * @return a new ordered array (first, selected nodes, and last,
     * non selected nodes; each group is ordered in
     * descending relevance criteria).
     */
    private ArrayList<VisualNode> reorderVisualNodes() {
        ArrayList<VisualNode> nodesSelected = new ArrayList<VisualNode>();
        ArrayList<VisualNode> nodesUnselected = new ArrayList<VisualNode>();
        
        for (VisualNode node : visualNodes) {
            if (node.isSelected()) {
                nodesSelected.add(node);
            } else {
                nodesUnselected.add(node);
            }
        }
        
        int selected = nodesSelected.size();
        int counter1 = 0;
        ArrayList<VisualNode> newList = new ArrayList<VisualNode>();
        int selPos = 0;
        while (counter1 < selected) {
            VisualNode candidate = null;
            double highestRelevance = -1;
            for (int i = 0; i < nodesSelected.size(); i++) {
                double relevance = nodesSelected.get(i).getNode().getRelevance();
                if (relevance > highestRelevance) {
                    highestRelevance = relevance;
                    candidate = nodesSelected.get(i);
                }
            }
            newList.add(selPos, candidate);
            selPos++;
            nodesSelected.remove(candidate);
            counter1++;
        }
        
        int unselected = nodesUnselected.size();
        int counter2 = 0;
        while (counter2 < unselected) {
            VisualNode candidate = null;
            double highestRelevance = -1;
            for (int i = 0; i < nodesUnselected.size(); i++) {
                double relevance = nodesUnselected.get(i).getNode().getRelevance();
                if (relevance > highestRelevance) {
                    highestRelevance = relevance;
                    candidate = nodesUnselected.get(i);
                }
            }
            newList.add(selPos, candidate);
            selPos++;
            nodesUnselected.remove(candidate);
            counter2++;
        }
        
        return newList;
    }
    
    /**
     * Paints the nodes. The nodes are painted in reverse order of its
     * position in the array. It means that the selected nodes are
     * always shown at first plane; and the nodes with higher relevance
     * are shown ahead of those with lower if they have the same selection
     * state
     *
     * @param g the graphics context in which to paint.
     */
    private void paintNodes(Graphics2D g) {
        visualNodes = reorderVisualNodes();
        for (int i = (visualNodes.size() - 1); i >= 0; i--) {
            if (visualNodes.get(i).isVisible()) {
                visualNodes.get(i).paint(g);
            }
        }
    }
    
    /**
     * Paints the links.
     *
     * @param g the graphics context in which to paint.
     */
    private void paintLinks(Graphics2D g) {
        
        for (VisualLink visualLink : visualLinks) {
            visualLink.paint(g);
        }
        
    }
    
    /**
     * Overwrited 'paint' method to avoid to call it explicitly.
     *
     * @param g the graphics context in which to paint.
     */
    public void paint(Graphics2D g) {
        this.g2 = g;
        paintLinks(g);
        paintNodes(g);
        if (newLink != null) {
            newLink.paint(g);
        }
        if (selection != null) {
            selection.paint(g);
        }
    }
    
    /**
     * Checks if is there a node in a position. You must specify if the node
     * must be selected or not.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a (selected or not) node in the position, returns it,
     * else, returns null.
     */
    public VisualNode whatNodeInPosition(Point2D.Double position, Graphics2D g) {
        
        VisualNode nodeFound = null;
        int index = 0, length = visualNodes.size();
        
        while ((nodeFound == null) && (index < length)) {
            VisualNode node = visualNodes.get(index++);
            if (node.pointInsideShape(position, g)) {
                nodeFound = node;
            }
        }
        
        return nodeFound;
        
    }
    
    /**
     * Checks if is there a inner box in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a inner box in the position, returns it,
     * else, returns null.
     */
    public InnerBox whatInnerBoxInPosition(Point2D.Double position, Graphics2D g) {
        
        InnerBox innerBoxFound = null;
        int index = 0;
        int nodesLength = visualNodes.size();
        while ((innerBoxFound == null) && (index < nodesLength)) {
            VisualNode node = visualNodes.get(index++);
            if (node.pointInsideShape(position, g)) {
                InnerBox innerBox = node.getInnerBox();
                if (innerBox.pointInsideShape(position, g)) {
                    innerBoxFound = innerBox;
                }
            }
        }
        return innerBoxFound;
    }
    
    /**
     * Checks if is there a visual state in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a visual state in the position, returns it,
     * else, returns null.
     */
    public VisualState whatStateInPosition(Point2D.Double position, Graphics2D g) {
        
        VisualState stateFound = null;
        int index = 0;
        int nodesLength = visualNodes.size();
        while ((stateFound == null) && (index < nodesLength)) {
            VisualNode node = visualNodes.get(index++);
            if (node.pointInsideShape(position, g)) {
                if (node.getInnerBox() instanceof FSVariableBox) {
                    int numStates = node.getInnerBox().getNumStates();
                    for (int i = 0; i < numStates; i++) {
                        VisualState state = ((FSVariableBox) node.getInnerBox()).getVisualState(i);
                        if (state.pointInsideShape(position, g)) {
                            stateFound = state;
                        }
                    }
                }
            }
        }
        return stateFound;
    }
    
    /**
     * Checks if is there a link in a position. You must specify if the link
     * must be selected or not.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a (selected or not) link in the position, returns it,
     * else, returns null.
     */
    public @Nullable VisualLink whatLinkInPosition(Point2D.Double position, Graphics2D g) {
        int index = 0;
        int length = visualLinks.size();
        while (index < length) {
            VisualLink link = visualLinks.get(index++);
            if (link.pointInsideShape(position, g)) {
                return link;
            }
        }
        return null;
        
    }
    
    /**
     * Checks if is there any selected element in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if a selected element is there in the position, returns the
     * element, else returns null.
     */
    public VisualElement getElementInPosition(Point2D.Double position, Graphics2D g) {
        VisualElement elementSelected;
        if ((elementSelected = whatNodeInPosition(position, g)) == null) {
            elementSelected = whatLinkInPosition(position, g);
        }
        return elementSelected;
    }
    
    /**
     * Sets the selection state of an element.
     *
     * @param element  element to be selected/deselected.
     * @param selected new selection state.
     */
    private void setSelectedElement(VisualElement element, boolean selected) {
        if (selected == element.isSelected()) {
            return;
        }
        switch (element) {
            case VisualNode selectedNode -> {
                if (selected) {
                    selectedNodes.add(selectedNode);
                } else {
                    selectedNodes.remove(element);
                }
            }
            case VisualLink selectedLink -> {
                if (selected) {
                    selectedLinks.add(selectedLink);
                } else {
                    selectedLinks.remove(element);
                }
            }
            default -> {
            }
        }
        notifyObjectsSelected();
        element.setSelected(selected);
    }
    
    /**
     * Sets the selection state of a node.
     *
     * @param node     node to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedNode(VisualNode node, boolean selected) {
        
        setSelectedElement(node, selected);
        
    }
    
    /**
     * Sets the selection state of a node identified by its name.
     *
     * @param name     name of the node to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedNode(String name, boolean selected) {
        
        boolean found = false;
        int i = 0, l = visualNodes.size();
        
        while (!found && (i < l)) {
            if (visualNodes.get(i).getNode().getName().equals(name)) {
                setSelectedElement(visualNodes.get(i), selected);
                found = true;
            } else {
                i++;
            }
        }
        
    }
    
    /**
     * Sets the selection state of a link.
     *
     * @param link     link to be selected/deselected.
     * @param selected new selection state.
     */
    private void setSelectedLink(VisualLink link, boolean selected) {
        
        setSelectedElement(link, selected);
        
    }
    
    /**
     * Sets the selection state of a link.
     *
     * @param link     link to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedLink(Link<Node> link, boolean selected) {
        int i = 0;
        VisualLink visualLink = null;
        while (visualLink == null && i < visualLinks.size()) {
            if (visualLinks.get(i).getLink().equals(link)) {
                visualLink = visualLinks.get(i);
            }
            ++i;
        }
        if (visualLink != null) {
            setSelectedElement(visualLink, selected);
        }
    }
    
    /**
     * Selects all nodes.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllNodes(boolean selected) {
        
        for (VisualNode node : visualNodes) {
            setSelectedElement(node, selected);
        }
        
        if (!selected) {
            selectedNodes.clear();
        }
        
    }
    
    /**
     * Selects all links.
     *
     * @param selected new selection state.
     */
    private void setSelectedAllLinks(boolean selected) {
        
        for (VisualLink link : visualLinks) {
            setSelectedElement(link, selected);
        }
        
        if (!selected) {
            selectedLinks.clear();
        }
        
    }
    
    /**
     * Selects all nodes and links.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllObjects(boolean selected) {
        
        setSelectedAllNodes(selected);
        setSelectedAllLinks(selected);
    }
    
    /**
     * Move some nodes an amount in both axis. The parameter 'selected'
     * indicates if the nodes must be selected or it doesn't mind.
     *
     * @param diffX    X-axis movement.
     * @param diffY    Y-axis movement.
     * @param selected if true, only the selected nodes are moved; if false, all
     *                 nodes are moved.
     */
    private void moveNodes(double diffX, double diffY, boolean selected) {
        for (VisualNode node : visualNodes) {
            if (!selected || (node.isSelected())) {
                Point2D.Double originalPosition = node.getTemporalPosition();
                double newPosX = originalPosition.getX() + diffX;
                double newPosY = originalPosition.getY() + diffY;
                boolean isValidPlace = newPosX >= 0 && newPosY >= 0;
                if (isValidPlace) {
                    node.setTemporalPosition(new Point2D.Double(newPosX, newPosY));
                    if (g2 != null) {
                        node.paint(g2);
                    }
                }
            }
        }
        
    }
    
    /**
     * Move the selected elements an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    public void moveSelectedElements(double diffX, double diffY) {
        moveSelectedNodes(diffX, diffY);
    }
    
    /**
     * Move the selected nodes an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    private void moveSelectedNodes(double diffX, double diffY) {
        
        moveNodes(diffX, diffY, true);
        
    }
    
    /**
     * Move all the nodes an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    protected void moveAllNodes(double diffX, double diffY) {
        
        moveNodes(diffX, diffY, false);
        
    }
    
    /**
     * Selects the nodes and links that are inside the selection rectangle and deselects
     * the ones that are outside.
     *
     * @param selection object that manages the selection.
     */
    private void selectElementsInsideSelection(SelectionRectangle selection) {
        
        setSelectedAllNodes(false);
        setSelectedAllLinks(false);
        // Select nodes
        ArrayList<VisualNode> selectedVisualNodes = new ArrayList<VisualNode>();
        for (VisualNode node : visualNodes) {
            if (selection.containsNode(node)) {
                setSelectedElement(node, true);
                selectedVisualNodes.add(node);
            }
        }
        // Select links
        for (VisualLink selectedLink : getLinksOfNodes(selectedVisualNodes, true)) {
            setSelectedElement(selectedLink, true);
        }
    }
    
    /**
     * Fills the array of information of the selected nodes and their actual
     * state.
     *
     * @return list where are the moved nodes information.
     */
    public List<MovedNodeInfo> fillActualNodesMovedInfo() {
        
        List<MovedNodeInfo> movedNodes = new ArrayList<MovedNodeInfo>();
        
        for (VisualNode node : visualNodes) {
            if (node.isSelected()) {
                movedNodes.add(new MovedNodeInfo(node.getNode(), node.getPosition()));
            }
        }
        
        return movedNodes;
        
    }
    
    /**
     * Fills the array of information of the selected nodes and the differences
     * of their state.
     *
     * @param movedNodes list where is saved the moved nodes information.
     */
    public static void fillDifferencesNodesMovedInfo(List<MovedNodeInfo> movedNodes) {
        
        for (MovedNodeInfo movedNode : movedNodes) {
            Node node = movedNode.getNode();
            movedNode.setDiffPosition(new Point2D.Double(node.getCoordinateX() - movedNode.getDiffPosition().getX(),
                                                         node.getCoordinateY() - movedNode.getDiffPosition().getY()));
        }
        
    }
    
    /**
     * Fills the array of information of the selected nodes and their actual
     * state.
     *
     * @return list where are the moved nodes information.
     */
    public List<VisualNode> fillVisualNodesSelected() {
        
        List<VisualNode> movedNodes = new ArrayList<>();
        
        for (VisualNode node : visualNodes) {
            if (node.isSelected()) {
                movedNodes.add(node);
            }
        }
        
        return movedNodes;
        
    }
    
    /**
     * This method returns a list containing all the nodes in the network.
     *
     * @return a list containing all the nodes in the network.
     */
    public List<VisualNode> getAllNodes() {
        return visualNodes;
    }
    
    /**
     * This method returns a list that contains all the links that leave of or
     * arrive in one node of the list of nodes passed as parameter.
     *
     * @param nodes        list of nodes whose links are returned.
     * @param onlyBothEnds returns only those links whose two ends are selected
     *
     * @return a list of links related to the nodes.
     */
    private List<VisualLink> getLinksOfNodes(List<VisualNode> nodes, boolean onlyBothEnds) {
        ArrayList<VisualLink> links = new ArrayList<VisualLink>();
        int l = nodes.size();
        for (VisualLink visualLink : visualLinks) {
            boolean found = false;
            boolean foundSource = false;
            boolean foundDestination = false;
            int i = 0;
            while (!found && (i < l)) {
                foundSource |= visualLink.getSourceNode().equals(nodes.get(i));
                foundDestination |= visualLink.getDestinationNode().equals(nodes.get(i));
                found = (onlyBothEnds) ? foundSource && foundDestination : foundSource || foundDestination;
                if (found) {
                    links.add(visualLink);
                } else {
                    i++;
                }
            }
        }
        return links;
    }
    
    public List<VisualLink> getVisualLinks() {
        return this.visualLinks;
    }
    
    /**
     * This method returns a list that contains all the links that leave of or
     * arrive in one node of the list of nodes passed as parameter.
     *
     * @param nodes list of nodes whose links are returned.
     *
     * @return a list of links related to the nodes.
     */
    public List<VisualLink> getLinksOfNodes(List<VisualNode> nodes) {
        return getLinksOfNodes(nodes, false);
    }
    
    /**
     * Sets a new selection listener.
     *
     * @param listener listener to be set.
     */
    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }
    
    /**
     * This method returns a list containing the selected nodes.
     *
     * @return a list containing the selected nodes.
     */
    public List<VisualNode> getSelectedNodes() {
        return new ArrayList<VisualNode>(selectedNodes);
    }
    
    /**
     * This method returns a list containing the selected links.
     *
     * @return a list containing the selected links.
     */
    public List<VisualLink> getSelectedLinks() {
        return new ArrayList<VisualLink>(selectedLinks);
    }
    
    /**
     * Returns the number of selected nodes.
     *
     * @return number of selected nodes.
     */
    public int getSelectedNodesNumber() {
        
        return selectedNodes.size();
        
    }
    
    /**
     * Returns the number of selected links.
     *
     * @return number of selected links.
     */
    public int getSelectedLinksNumber() {
        
        return selectedLinks.size();
        
    }
    
    /**
     * Notifies to the registered selection listener how many nodes and links
     * are selected, and which are the especific selected nodes.
     * Also notifies this situation to the menu assistant.
     */
    private void notifyObjectsSelected() {
        
        for (SelectionListener listener : selectionListeners) {
            listener.objectsSelected(getSelectedNodes(), getSelectedLinks());
        }
    }
    
    /**
     * Returns the network that is painted by this object.
     *
     * @return network which is painted.
     */
    public ProbNet getNetwork() {
        return probNet;
    }
    
    public PNESupport getpNESupport() {
        //review method
        return probNet.getPNESupport();
        
    }
    
    @Override public void afterEditExecutes(PNEdit edit) {
        constructVisualInfo();
        if (getWorkingMode() != NetworkPanel.WorkingMode.INFERENCE) {
            visualDecisionNodeRefresh();
        }
        mainGUI.mainPanel.getEditionToolBar().getUndoButton().setEnabled(this.probNet.getPNESupport().getCanUndo());
        mainGUI.mainPanel.getEditionToolBar().getRedoButton().setEnabled(this.probNet.getPNESupport().getCanRedo());
    }
    
    private void visualDecisionNodeRefresh() {
        reconstructVisualInfo((visualNode, currentNodesToAdd)
                -> visualNode.getNode().getNodeType() != NodeType.DECISION);
    }
    
    /**
     * Returns different types of visual nodes according to the supplied node.
     *
     * @param node node whose visual representation is going to be returned.
     *
     * @return the visual representation of the node.
     */
    private VisualNode createVisualNode(Node node) {
        
        VisualNode visualNode = switch (node.getNodeType()) {
            case CHANCE -> new VisualChanceNode(node, this);
            case DECISION -> new VisualDecisionNode(node, this);
            case UTILITY -> new VisualUtilityNode(node, this);
            default -> null;
        };
        return visualNode;
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        refreshUI();
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        refreshUI();
    }
    
    private void refreshUI() {
        constructVisualInfo();
        if (getWorkingMode() != NetworkPanel.WorkingMode.INFERENCE) {
            visualDecisionNodeRefresh();
        }
        boolean canUndo = this.probNet.getPNESupport().getCanUndo();
        mainGUI.mainPanel.getEditionToolBar().getUndoButton().setEnabled(canUndo);
        boolean canRedo = this.probNet.getPNESupport().getCanRedo();
        mainGUI.mainPanel.getEditionToolBar().getRedoButton().setEnabled(canRedo);
    }
    
    /**
     * Adds whatever is in that position to the selection
     *
     * @param cursorPosition the cursor position
     * @param g the g
     */
    public void addToSelection(Point2D.Double cursorPosition, Graphics2D g) {
        VisualNode node;
        VisualLink link;
        
        if ((node = whatNodeInPosition(cursorPosition, g)) != null) {
            setSelectedNode(node, !node.isSelected());
        } else if ((link = whatLinkInPosition(cursorPosition, g)) != null) {
            setSelectedLink(link, !link.isSelected());
        }
    }
    
    /**
     * Cleans selection and sets it to whatever is in the cursorPosition
     *
     * @param cursorPosition the cursor position
     * @param g the g
     *
     * @return true if there is an element in the position
     */
    public VisualElement selectElementInPosition(Point2D.Double cursorPosition, Graphics2D g) {
        VisualNode node;
        VisualLink link;
        VisualElement selectedElement = null;
        if ((node = whatNodeInPosition(cursorPosition, g)) != null) {
            if (!node.isSelected()) {
                setSelectedAllObjects(false);
                setSelectedNode(node, true);
            }
            selectedElement = node;
        } else if ((link = whatLinkInPosition(cursorPosition, g)) != null) {
            if (!link.isSelected()) {
                setSelectedAllObjects(false);
                setSelectedLink(link, true);
            }
            selectedElement = link;
        } else {
            setSelectedAllObjects(false);
        }
        return selectedElement;
    }
    
    /**
     * Starts link creation
     *
     * @param cursorPosition the cursor position
     * @param g the g
     */
    public void startLinkCreation(Point2D.Double cursorPosition, Graphics2D g) {
        VisualNode node;
        
        if ((node = whatNodeInPosition(cursorPosition, g)) != null) {
            newLink = new VisualArrow(node.getPosition(), cursorPosition);
            newLinkSource = node;
        }
    }
    
    public void updateLinkCreation(Point2D.Double position) {
        if (newLink != null) {
            newLink.setEndPoint(position);
        }
    }
    
    /**
     * Finishes link creation and returns edit for the new link
     *
     * @param point the point
     * @param g the g
     *
     * @return The edit for the new link created
     */
    public PNEdit finishLinkCreation(Point2D.Double point, Graphics2D g) {
        PNEdit linkEdit = null;
        if (newLink != null) {
            newLink = null;
            if (newLinkSource != null) {
                VisualNode newLinkDestination;
                if ((newLinkDestination = whatNodeInPosition(point, g)) != null) {
                    if (!newLinkSource.equals(newLinkDestination)) {
                        linkEdit = new AddLinkEdit(probNet, probNet.getVariable(newLinkSource.getNode().getName()),
                                                   probNet.getVariable(newLinkDestination.getNode().getName()), true);
                    }
                }
                newLinkSource = null;
            }
        }
        return linkEdit;
    }
    
    public void cancelLinkCreation(NetworkEditorPanel networkEditorPanel) {
        newLink = null;
        newLinkSource = null;
        networkEditorPanel.repaint();
    }
    
    public void startSelectionRectangle(Point2D.Double position) {
        selection = new SelectionRectangle();
        selection.initSelection(position, 0, 0);
    }
    
    public void finishSelectionRectangle(Point2D.Double position) {
        selection.clearSelectionSquare();
    }
    
    public void updateSelectionRectangle(double diffX, double diffY) {
        selection.setSize(selection.getWidth() + diffX, selection.getHeight() + diffY);
        selectElementsInsideSelection(selection);
    }
    
    /**
     * Returns the isPropagationActive.
     *
     * @return the isPropagationActive.
     */
    boolean isPropagationActive() {
        return isPropagationActive;
    }
    
    /**
     * Sets the isPropagationActive.
     *
     * @param isPropagationActive the isPropagationActive to set.
     */
    public void setPropagationActive(boolean isPropagationActive) {
        this.isPropagationActive = isPropagationActive;
    }
    
    public NetworkPanel.WorkingMode getWorkingMode() {
        return workingMode;
    }
    
    public void setWorkingMode(NetworkPanel.WorkingMode workingMode) {
        this.workingMode = workingMode;
    }
    
    //TODO OOPN end
    
    public void selectElement(VisualElement selectedElement) {
        setSelectedAllObjects(false);
        setSelectedElement(selectedElement, true);
    }
    
    /**
     * This method copies the selected nodes to the clipboard.
     *
     * @param cut if true, the nodes copied to the clipboard are also removed.
     */
    public void exportToClipboard(boolean cut) {
        List<Node> selectedNodes = this
                .getSelectedNodes().stream().map(VisualNode::getNode).toList();
        List<Link<Node>> selectedLinks = this
                .getSelectedLinks().stream().map(VisualLink::getLink).toList();
        SelectedContent copiedContent = new SelectedContent(selectedNodes, selectedLinks);
        if (!copiedContent.isEmpty()) {
            this.clipboardAssistant.copyToClipboard(copiedContent);
            if (cut) {
                this.removeSelectedObjects();
            }
        }
    }
    
    /**
     * Removes selected objects
     */
    public void removeSelectedObjects() {
        try {
            new RemoveSelectedEdit(this).executeEdit();
            this.setSelectedAllObjects(false);
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * This method imports the content from the clipboard and creates it in the
     * network.
     */
    public void pasteFromClipboard() throws DoEditException {
        if (!this.clipboardAssistant.isThereDataStored()) {
            return;
        }
        new PasteEdit(this.getProbNet(), this.clipboardAssistant.paste()).executeEdit();
    }
    
    public EditorPanelClipboardAssistant getClipboardAssistant() {
        return this.clipboardAssistant;
    }
}

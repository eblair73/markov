package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.exception.PreResolutionNodeInInferenceException;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.edition.NetworkPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

/**
 * Handles all mouse and keyboard input for the {@link NetworkEditorPanel},
 * delegating to the current {@link EditionMode} and managing contextual menus.
 */
class EditorInputHandler implements MouseListener, MouseMotionListener, KeyListener {
    
    private final NetworkEditorPanel networkEditorPanel;
    
    EditorInputHandler(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
    }
    
    /**
     * Invoked when a mouse button has been clicked (pressed and released) on
     * the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseClicked(MouseEvent e) {
        this.networkEditorPanel.requestFocus();
    }
    
    private int lastClickCount = 0;
    private boolean lastLeftClickProducedANode = false;
    
    /**
     * Invoked when a mouse button has been pressed on the component.
     *
     * @param e mouse event information.
     */
    @Override public void mousePressed(MouseEvent e) {
        // requestFocusInWindow(); Activate if nodes can't be moved by arrows.
        
        if (e.getClickCount() <= (this.lastClickCount + 1)) {
            this.lastLeftClickProducedANode = false;
            this.lastClickCount = Math.max(e.getClickCount() - 1, 0);
        } else {
            this.lastClickCount += 1;
        }
        // requestFocusInWindow(); Activate if nodes can't be moved by arrows.
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        this.cursorPosition.setLocation(this.networkEditorPanel.getZoomManager()
                                                               .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                .screenToPanel(e.getY()));
        // Specific functionality depending on the edition mode;
        try {
            var oldNodesCount = this.networkEditorPanel.getNetworkPanel().getProbNet().getNodes().size();
            this.networkEditorPanel.getEditionMode().mousePressed(e, this.cursorPosition, g);
            if (e.getClickCount() == 1) {
                int newNodesCount = this.networkEditorPanel.getNetworkPanel().getProbNet().getNodes().size();
                this.lastLeftClickProducedANode = oldNodesCount < newNodesCount;
            }
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
        // Generic functionality regardless of the edition mode
        if (SwingUtilities.isRightMouseButton(e)) {
            this.showContextualMenu(e, g);
            this.networkEditorPanel.repaint();
            return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
            this.networkEditorPanel.repaint();
            return;
        }
        VisualNode node;
        if (e.isAltDown() && e.getClickCount() != 2) {
            node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
            if (node != null) {
                if (!node.isSelected()) {
                    this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                    this.networkEditorPanel.getVisualNetwork().setSelectedNode(node, true);
                }
                try {
                    this.networkEditorPanel.showPotentialDialog(this.networkEditorPanel.getNetworkPanel()
                                                                                       .getWorkingMode() != NetworkPanel.WorkingMode.EDITION);
                } finally {
                    this.networkEditorPanel.repaint();
                    return;
                }
            }
        }
        if (!(e.getClickCount() == 2 && GUIUtils.noMouseModifiers(e))) {
            this.networkEditorPanel.repaint();
            return;
        }
        if (this.networkEditorPanel.getNetworkPanel().getWorkingMode() == NetworkPanel.WorkingMode.EDITION) {
            // If we are in Edition Mode a double click must open
            // the corresponding properties dialog (for node, link
            // or network)
            node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
            if (node != null) {
                try {
                    boolean userAcceptedChanges = this.networkEditorPanel.changeNodeProperties(node, this.lastLeftClickProducedANode);
                    if (!userAcceptedChanges && this.lastLeftClickProducedANode) {
                        while (true) {
                            if (this.networkEditorPanel.getNetworkPanel().getProbNet().getPNESupport()
                                                       .undo()
                                                       .stream()
                                                       .anyMatch(edit -> edit instanceof AddNodeEdit)) {
                                break;
                            }
                        }
                        this.networkEditorPanel.getNetworkPanel().getProbNet().getPNESupport().removeUndoneEdits();
                    }
                } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                         IncompatibleEvidenceException | CannotNormalizePotentialException |
                         ConstraintViolatedException | NotSupportedOperationException ex) {
                    this.networkEditorPanel.repaint();
                    throw new UnrecoverableException(ex);
                }
            } else {
                VisualLink link = this.networkEditorPanel.getVisualNetwork().whatLinkInPosition(this.cursorPosition, g);
                if (link != null) {
                    this.networkEditorPanel.changeLinkProperties(link);
                } else {
                    this.networkEditorPanel.changeNetworkProperties();
                }
            }
            this.networkEditorPanel.repaint();
            return;
        }
        
        if (this.networkEditorPanel.getVisualNetwork().whatStateInPosition(this.cursorPosition, g) == null) {
            if ((this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g) != null) && (
                    this.networkEditorPanel.getVisualNetwork().whatInnerBoxInPosition(this.cursorPosition, g) == null
            )) {
                try {
                    this.networkEditorPanel.changeNodeProperties();
                } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                         IncompatibleEvidenceException | CannotNormalizePotentialException |
                         ConstraintViolatedException | NotSupportedOperationException ex) {
                    throw new UnrecoverableException(ex);
                } finally {
                    this.networkEditorPanel.repaint();
                }
            }
            this.networkEditorPanel.repaint();
            return;
        }
        
        // If we are in Inference Mode a double click inside a
        // visual state of a node without pre-resolution finding
        // must introduce evidence in that node.
        // If the double click is inside a node but outside its
        // inner box (in its 'expanded external shape'), its
        // properties dialog should be open
        
        VisualNode visualNode = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
        if (visualNode.isPreResolutionFinding()) {
            throw new UnrecoverableException(new PreResolutionNodeInInferenceException(visualNode));
        }
        VisualState visualState = this.networkEditorPanel.getVisualNetwork().whatStateInPosition(this.cursorPosition, g);
        try {
            this.networkEditorPanel.getEvidenceManager().toggleFinding(visualNode, visualState);
        } catch (IncompatibleEvidenceException | NotEvaluableNetworkException | NonProjectablePotentialException |
                 NotEnoughMemoryException | CannotNormalizePotentialException | DoEditException ex) {
            throw new UnreachableException(ex);
        }
        
        
    }
    
    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param e mouse event information.
     */
    @Override public void mouseDragged(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double point = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                         .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                          .screenToPanel(e.getY()));
        double diffX = point.getX() - this.cursorPosition.getX();
        double diffY = point.getY() - this.cursorPosition.getY();
        this.cursorPosition.setLocation(point);
        this.networkEditorPanel.getEditionMode().mouseDragged(e, point, diffX, diffY, g);
    }
    
    /**
     * Invoked when a mouse button has been released on the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseReleased(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                            .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                             .screenToPanel(e.getY()));
        try {
            this.networkEditorPanel.getEditionMode().mouseReleased(e, position, g);
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    /**
     * Invoked when the mouse button enters the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseEntered(MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse button exits the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseExited(MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param e mouse event information.
     */
    @Override public void mouseMoved(MouseEvent e) {
    }
    
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_UP -> this.networkEditorPanel.getVisualNetwork().moveSelectedElements(0, -2);
            case KeyEvent.VK_RIGHT -> this.networkEditorPanel.getVisualNetwork().moveSelectedElements(2, 0);
            case KeyEvent.VK_DOWN -> this.networkEditorPanel.getVisualNetwork().moveSelectedElements(0, 2);
            case KeyEvent.VK_LEFT -> this.networkEditorPanel.getVisualNetwork().moveSelectedElements(-2, 0);
        }
        this.networkEditorPanel.repaint();
    }
    
    @Override
    public void keyReleased(KeyEvent keyEvent) {
    
    }
    
    
    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }
    
    /**
     * Position of the mouse cursor when it is pressed.
     */
    private final Point2D.Double cursorPosition = new Point2D.Double();
    
    /**
     * Shows contextual menu
     *
     * @param e MouseEvent
     * @param g Graphics2D
     */
    private void showContextualMenu(MouseEvent e, Graphics2D g) {
        VisualElement selectedElement = this.networkEditorPanel.getVisualNetwork()
                                                               .getElementInPosition(this.cursorPosition, g);
        ContextualMenu contextualMenu;
        if (selectedElement != null) {
            contextualMenu = this.getContextualMenu(selectedElement, this.networkEditorPanel);
            this.networkEditorPanel.getVisualNetwork().selectElement(selectedElement);
        } else {
            boolean canBeExpanded = this.networkEditorPanel.getNetworkPanel().getProbNet().thereAreTemporalNodes();
            contextualMenu = this.contextualMenuFactory.getNetworkContextualMenu(canBeExpanded);
        }
        contextualMenu.show(this.networkEditorPanel, e.getX(), e.getY());
    }
    
    /**
     * Object that creates the contextual menus.
     */
    private ContextualMenuFactory contextualMenuFactory = null;
    
    
    void setContextualMenuFactory(ContextualMenuFactory contextualMenuFactory) {
        this.contextualMenuFactory = contextualMenuFactory;
    }
    
    /**
     * Retrieves the contextual menu that corresponds to the selectedElement.
     *
     * @return the contextual menu corresponding the the parameter.
     */
    private @Nullable ContextualMenu getContextualMenu(VisualElement selectedElement, NetworkEditorPanel panel) {
        return Optional.ofNullable(this.contextualMenuFactory)
                       .map(menuFactory -> menuFactory.getContextualMenu(selectedElement, panel))
                       .orElse(null);
    }
}

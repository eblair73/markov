/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.core.exception.*;
import org.openmarkov.gui.window.edition.NetworkPanel;
import org.openmarkov.gui.window.ZoomableContentPanel;

import java.awt.*;


/**
 * A zoomable window container that hosts a {@link DecisionTreePanel}.
 * It acts as a bridge between the decision tree visualization and the main network panel.
 */
@SuppressWarnings("serial") 
public class DecisionTreeWindow extends ZoomableContentPanel {
    
	/** The panel containing the visual representation of the decision tree. */
    private final DecisionTreePanel decisionTreePanel;
    
    /** The source network panel associated with this tree window. */
    private final NetworkPanel networkPanel;
    
    /**
     * Creates a new window to display the decision tree derived from a network panel.
     * @param networkPanel The panel containing the probabilistic network.
     * @throws IncompatibleEvidenceException if the evidence is incompatible with the network
     * @throws NotEvaluableNetworkException If
     * @throws NonProjectablePotentialException if the potential cannot be projected
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if different sizes in potentials and states occurs
     */
    public DecisionTreeWindow(NetworkPanel networkPanel) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        this.setLayout(new BorderLayout());
        this.networkPanel = networkPanel;
        this.decisionTreePanel = new DecisionTreePanel(networkPanel.probNet);
        this.networkPanel.addDecisionTreeWindows(this);
        this.add(decisionTreePanel, BorderLayout.CENTER);
        this.setBackground(Color.blue);
    }
    
    /**
     * Handles the window closure by unregistering itself from the parent network panel.
     * @return True if the window closed successfully.
     */
    @Override public boolean close() {
        this.networkPanel.removeDecisionTreeWindows(null);
        return super.close();
    }
    
    /** {@inheritDoc} */
    @Override public double getZoom() {
        return decisionTreePanel.getZoom();
    }
    
    /** {@inheritDoc} */
    @Override public void setZoom(double zoom) {
        decisionTreePanel.setZoom(zoom);
    }
    
}

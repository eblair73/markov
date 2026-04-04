/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.gui.window.edition.NetworkPanel;

import javax.swing.*;

/**
 * This class represents the content panel of a tab.
 *
 * @author jmendoza
 * @version 1.1 jrico - Removed most of the methods, as these were related to MDI. OpenMarkov now uses tabs.
 * this class has been reduced to a simple JPanel that can be zoomed in and out, and has a custom action for closing.
 */
public abstract class ZoomableContentPanel extends JPanel {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 6808692603537287168L;
    
    /**
     * Prepares the frame for closing
     */
    public boolean close() {
        MainGUI.INSTANCE.mainPanel.getNetworksTabPanel().remove(this);
        if (MainGUI.INSTANCE.mainPanel.getNetworksTabPanel().getTabCount() == 0) {
            MainGUI.INSTANCE.mainPanel.setToolBarPanel(NetworkPanel.WorkingMode.EDITION);
            MainGUI.INSTANCE.mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
        }
        return true;
    }
    
    public abstract double getZoom();
    
    public abstract void setZoom(double zoom);
    
}

package org.openmarkov.stochasticPropagationOutput;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;

public class StochasticPropagationOutputPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.EXPORT;
    }
    
    @Override public int priorityInGroup() {
        return 1;
    }
    
    public boolean enabled() {
        return MainPanel.getCurrentProbNet() != null;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(
                StringDatabase.getUniqueInstance()
                              .getString("stochasticPropagationOutput", "StochasticPropagationOutput"))
                .enabled(MainPanel.getCurrentProbNet() != null)
                .onClick(() -> {
                    StochasticPropagationOutputFrame frame = new StochasticPropagationOutputFrame(MainGUI.INSTANCE.mainPanel.getMainFrame());
                    Exception error = frame.boundError();
                    if (error != null) {
                        throw error;
                    }
                    frame.setVisible(true);
                })
                .build();
    }
}

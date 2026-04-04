package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.toolplugin.ToolPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;

public class HoverLoggerPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Hover GUI logger (Developers' tool)")
                .asCheckbox()
                .selected(LocalPreferences.HOVER_LOGGER_ENABLED.get())
                .onItemEvent(e -> LocalPreferences.HOVER_LOGGER_ENABLED.set(e.getStateChange() == ItemEvent.SELECTED))
                .build();
    }
    
    static {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if(!LocalPreferences.HOVER_LOGGER_ENABLED.get()){
                return;
            }
            if (!(event instanceof MouseEvent mouseEvent)) {
                return;
            }
            if (mouseEvent.getID() != MouseEvent.MOUSE_MOVED) {
                return;
            }
            if (!(mouseEvent.getSource() instanceof Component hoveredComponent)) {
                return;
            }
            System.out.println("Hovering a " + hoveredComponent.getClass().getName() + " with name " + hoveredComponent.getName());
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        
    }
}

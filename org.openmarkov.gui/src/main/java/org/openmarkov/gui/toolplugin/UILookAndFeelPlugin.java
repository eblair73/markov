package org.openmarkov.gui.toolplugin;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class UILookAndFeelPlugin implements ToolPlugin {
    
    //!com.jthemedetecor.OsThemeDetector.getDetector().isDark();
    /*
        The user's theme can be detected using com.jthemedetecor.OsThemeDetector.getDetector().isDark(). But this
        requires the dependency:
        <dependency>
            <groupId>org.openani.jsystemthemedetector</groupId>
            <artifactId>jSystemThemeDetector</artifactId>
            <version>3.8</version>
        </dependency>
     */
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.USER_EXPERIENCE;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Change UI Look and Feel")
                .withItems(Arrays.stream(Theme.values()).map(UILookAndFeelPlugin::themeToButton))
                .build();
    }
    
    private static JMenuItem themeToButton(Theme theme) {
        boolean selected = LocalPreferences.PREFERRED_THEME.get() == theme;
        return new JMenuItemBuilder(theme.toUIString())
                .asRadio()
                .selected(selected)
                .onClick(() -> {
                    LocalPreferences.PREFERRED_THEME.set(theme);
                    UILookAndFeelPlugin.updateInterfaceToLook(MainGUI.INSTANCE.mainPanel.getMainFrame());
                    MainGUI.INSTANCE.mainPanel.getMainMenu().reInitialize();
                })
                .build();
    }
    
    public static void updateInterfaceToLook(@Nullable Container parent) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        LocalPreferences.PREFERRED_THEME.get().setlookAndFeel();
        if (parent != null) {
            while (true) {
                if (parent.getParent() == null) break;
                parent = parent.getParent();
            }
            SwingUtilities.updateComponentTreeUI(parent);
        }
    }
    
    public enum Theme {
        SYSTEM,
        DARK,
        LIGHT;
        
        public String toUIString() {
            return switch (this) {
                case SYSTEM -> "System";
                case DARK -> "Dark (Beta)";
                case LIGHT -> "Light (Beta)";
            };
        }
        
        public void setlookAndFeel() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            switch (this) {
                case SYSTEM -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                case DARK -> UIManager.setLookAndFeel(new FlatDarculaLaf());
                case LIGHT -> UIManager.setLookAndFeel(new FlatLightLaf());
            }
        }
    }
}

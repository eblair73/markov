package org.openmarkov.gui.componentBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class JMenuItemBuilder {
    
    private @NotNull String title;
    private @Nullable Character mnemonic;
    private @Nullable Boolean enabled;
    private final @NotNull ArrayList<ThrowingConsumer<ActionEvent, ? extends Exception>> onClick;
    private final @NotNull ArrayList<ThrowingConsumer<ItemEvent, ? extends Exception>> onItemEvent;
    private final @NotNull ArrayList<Component> items;
    private @NotNull SpecificKind specificKind;
    private @Nullable Boolean selected;
    
    enum SpecificKind {
        Radio, Checkbox, Unspecified
    }
    
    public JMenuItemBuilder(@NotNull String title) {
        this.title = title;
        this.items = new ArrayList<>();
        this.onClick = new ArrayList<>();
        this.onItemEvent = new ArrayList<>();
        this.specificKind = SpecificKind.Unspecified;
    }
    
    public JMenuItemBuilder withTitle(@NotNull String title) {
        this.title = title;
        return this;
    }
    
    public JMenuItemBuilder withMnemonic(Character mnemonic) {
        this.mnemonic = mnemonic;
        return this;
    }
    
    public JMenuItemBuilder enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public JMenuItemBuilder onClick(@NotNull ThrowingRunnable<? extends Exception> onClick) {
        this.onClick.add((ignored) -> onClick.run());
        return this;
    }
    
    public JMenuItemBuilder onClick(@NotNull ThrowingConsumer<ActionEvent, ? extends Exception> onClick) {
        this.onClick.add(onClick);
        return this;
    }
    
    public JMenuItemBuilder onItemEvent(@NotNull ThrowingConsumer<ItemEvent, ? extends Exception> onItemEvent) {
        this.onItemEvent.add(onItemEvent);
        return this;
    }
    
    public JMenuItemBuilder withItem(@NotNull Component item) {
        this.items.add(item);
        return this;
    }
    
    public JMenuItemBuilder withItems(@NotNull Collection<? extends @NotNull Component> items) {
        this.items.addAll(items);
        return this;
    }
    
    public JMenuItemBuilder withItems(@NotNull Stream<? extends @NotNull Component> items) {
        this.items.addAll(items.toList());
        return this;
    }
    
    public JMenuItemBuilder asRadio() {
        this.specificKind = SpecificKind.Radio;
        return this;
    }
    
    public JMenuItemBuilder asCheckbox() {
        this.specificKind = SpecificKind.Checkbox;
        return this;
    }
    
    public JMenuItemBuilder selected(Boolean selected) {
        this.selected = selected;
        return this;
    }
    
    @SuppressWarnings("ExtractMethodRecommender")
    public JMenuItem build() {
        JMenuItem jMenuItem = switch (this.specificKind) {
            case Radio -> new JRadioButtonMenuItem(this.title);
            case Checkbox -> new JCheckBoxMenuItem(this.title);
            case Unspecified -> {
                if (this.items.isEmpty()) {
                    yield new JMenuItem(this.title);
                }
                yield new JMenu(this.title);
            }
        };
        if (this.selected != null) {
            jMenuItem.setSelected(this.selected);
        }
        if (this.enabled != null) {
            jMenuItem.setEnabled(this.enabled);
        }
        if (this.mnemonic != null) {
            jMenuItem.setMnemonic(this.mnemonic);
        }
        for (var onClick : this.onClick) {
            jMenuItem.addActionListener(e -> {
                try {
                    onClick.accept(e);
                } catch (UnrecoverableException | UnreachableException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new UnrecoverableException(ex);
                }
            });
        }
        for (var onItemEvent : this.onItemEvent) {
            jMenuItem.addItemListener(e -> {
                try {
                    onItemEvent.accept(e);
                } catch (UnrecoverableException | UnreachableException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new UnrecoverableException(ex);
                }
            });
        }
        
        
        for (Component component : this.items) {
            jMenuItem.add(component);
        }
        return jMenuItem;
    }
    
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
    }
    
    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }
    
    
}

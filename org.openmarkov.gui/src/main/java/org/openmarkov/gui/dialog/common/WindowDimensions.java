package org.openmarkov.gui.dialog.common;

import java.awt.*;
import java.io.Serializable;

/**
 * Immutable snapshot of a window's location, size, and extended state (e.g. maximized),
 * used for persisting and restoring window geometry.
 */
public record WindowDimensions(Point location, Dimension size, int extendedState) implements Serializable {
    
}

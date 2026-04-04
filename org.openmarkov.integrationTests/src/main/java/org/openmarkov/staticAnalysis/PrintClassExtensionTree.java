package org.openmarkov.staticAnalysis;

import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.ExtensionTree;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.util.Comparator;

public class PrintClassExtensionTree {
    
    public static void main(String[] args) {
        PluginSearch.full()
                    .extending(JMenuItem.class)
                    .extensionTree()
                    .print();
    }
    
}

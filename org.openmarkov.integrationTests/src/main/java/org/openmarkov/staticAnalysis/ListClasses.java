package org.openmarkov.staticAnalysis;

import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.common.BottomPanelButtonDialog;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.common.PotentialPanelPlugin;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class ListClasses {
    
    public static void main(String[] args) {
        AtomicInteger index = new AtomicInteger();
        
        PluginSearch.init()
                    .extending(PNConstraint.class)
                    .stream()
                    .sorted(Comparator.comparing(Class::getName))
                    .forEach(classToPrint -> System.out.println(index.incrementAndGet() + " - " + classToPrint.getName()));
    }
    
}

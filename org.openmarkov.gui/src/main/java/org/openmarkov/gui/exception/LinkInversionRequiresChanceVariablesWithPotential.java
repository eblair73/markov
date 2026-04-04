package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;

public class LinkInversionRequiresChanceVariablesWithPotential extends DoEditException {
    
    public final Node wrongNode;
    
    public LinkInversionRequiresChanceVariablesWithPotential(Node wrongNode) {
        this.wrongNode = wrongNode;
    }
}

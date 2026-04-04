package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;

public class CannotInvertUndirectedLinks extends DoEditException {
    
    public final Link<Node> link;
    
    public CannotInvertUndirectedLinks(Link<Node> link) {
        this.link = link;
    }
}

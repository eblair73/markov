/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.OrientLinkEdit;

import java.util.ArrayList;

/**
 * Compound edit that orients (directs) multiple undirected links at once.
 */
@SuppressWarnings("serial") public class COrientLinksEdit extends CompoundPNEdit {
    
    public COrientLinksEdit(ProbNet probNet, ArrayList<OrientLinkEdit> edits) {
        super(probNet);
        this.orientLinkEdits = edits;
    }
    
    private final ArrayList<OrientLinkEdit> orientLinkEdits;
    
    // Methods
    @Override public ArrayList<PNEdit> generateEdits() {
        return (ArrayList<PNEdit>) (ArrayList) orientLinkEdits;
    }
    
    public String toString() {
        StringBuilder buffer = new StringBuilder("Orient links: ");
        for (PNEdit edit : getEdits()) {
            OrientLinkEdit orientLinkEdit = (OrientLinkEdit) edit;
            buffer.append(orientLinkEdit.getVariableFrom().getName());
            if (orientLinkEdit.isDirected()) {
                buffer.append(" --> ");
            } else {
                buffer.append(" --- ");
            }
            buffer.append(orientLinkEdit.getVariableTo().getName());
            buffer.append(", ");
        }
        buffer.delete(buffer.lastIndexOf(","), buffer.length());
        return buffer.toString();
    }
    
    @Override public boolean equals(Object arg0) {
        boolean sameInformation = true;
        if (arg0 instanceof COrientLinksEdit editToCompare) {
            for (PNEdit edit : editToCompare.getEdits()) {
                sameInformation &= getEdits().contains(edit);
            }
            for (PNEdit edit : getEdits()) {
                sameInformation &= editToCompare.getEdits().contains(edit);
            }
        } else {
            sameInformation = false;
        }
        return sameInformation;
    }
    
}

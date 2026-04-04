/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
@SuppressWarnings("serial") public class ListPNEdit extends CompoundPNEdit {
    
    private final ArrayList<PNEdit> edits;
    
    public ListPNEdit(ProbNet probNet, Collection<PNEdit> doneEdits) {
        super(probNet);
        this.edits = new ArrayList<>(doneEdits);
    }
    
    @Override protected ArrayList<PNEdit> generateEdits() {
        return this.edits;
    }
    
    
}

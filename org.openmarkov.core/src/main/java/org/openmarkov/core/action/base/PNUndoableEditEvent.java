/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

/**
 * An event that wraps a {@link PNEdit}. Unlike {@code UndoableEditEvent},
 * this event is associated with a {@link org.openmarkov.core.model.network.ProbNet}.
 */
@SuppressWarnings("serial")
public class PNUndoableEditEvent {
    
    // Attributes
    private final PNEdit edit;
    
    // Constructor
    
    /**
     * @param edit An {@code UndoableEdit} object.
     */
    public PNUndoableEditEvent(PNEdit edit) {
        this.edit = edit;
    }
    
    /**
     * Returns the edit value.
     *
     * @return the UndoableEdit object encapsulating the edit
     */
    public PNEdit getEdit() {
        return this.edit;
    }
    
}

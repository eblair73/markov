/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
public abstract class CompoundPNEdit extends PNEdit {

    // Attribute
    private boolean generatedEdits;
    
    // Constructor
    private ArrayList<PNEdit> edits;
    

    public CompoundPNEdit(ProbNet probNet) {
        super(probNet);
        this.generatedEdits = false;
    }
    
    // Methods
    
    /**
     * Generate edits and does them
     *
     * @throws DoEditException DoEditException
     */
    @Override protected void doEdit() throws DoEditException {
        ArrayList<PNEdit> doneEdits = new ArrayList<>(this.getEdits().size());
        try {
            for (PNEdit edit : this.getEdits()) {
                edit.executeEdit();
                doneEdits.add(edit);
            }
        } catch (DoEditException e) {
            for (PNEdit editToUndo : doneEdits.reversed()) {
                editToUndo.undo();
            }
            throw e;
        }
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        for (PNEdit pnEdit : this.getEdits()) {
            pnEdit.checkConstraintsWillBeMet(constraintChecker);
        }
    }
    
    /**
     * Generates the list of sub-edits that compose this compound edit.
     * Called lazily on first access via {@link #getEdits()}.
     *
     * @return the list of sub-edits
     */
    protected abstract ArrayList<PNEdit> generateEdits();
    
    /**
     * Returns the sub-edits, generating them on first call. All sub-edits are
     * marked as belonging to this compound edit.
     *
     * @return the list of sub-edits
     */
    public ArrayList<PNEdit> getEdits() {
        if (!this.generatedEdits) {
            this.edits = this.generateEdits();
            this.edits.forEach(PNEdit::markItBelongsToACompoundEdit);
            this.generatedEdits = true;
        }
        return this.edits;
    }

    @Override public void redo() {
        this.getEdits().forEach(PNEdit::redo);
        this.setTypicalRedo(false);
        super.redo();
    }
    
    @Override public void undo() {
        this.getEdits().reversed().forEach(PNEdit::undo);
        super.undo();
    }
    
}

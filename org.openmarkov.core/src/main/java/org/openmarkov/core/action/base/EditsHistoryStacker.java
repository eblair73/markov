/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a stack of {@link EditsHistory} instances to support nested
 * (transactional) edit groups. Sub-histories can be committed into the
 * parent or discarded/undone via {@link CloseEditStackOptions}.
 *
 * @author jrico
 */
public class EditsHistoryStacker {
    
    private final EditsHistory mainEditsHistory;
    private final ArrayDeque<EditsHistory> uncommitedHistories;
    
    public EditsHistoryStacker() {
        this.mainEditsHistory = new EditsHistory();
        this.uncommitedHistories = new ArrayDeque<>();
    }
    
    /**
     * Pushes a new empty {@link EditsHistory} onto the stack, starting a nested edit group.
     */
    public void openNewSubEditHistory() {
        this.uncommitedHistories.addLast(new EditsHistory());
    }
    
    /**
     * Pops the topmost sub-history and applies the given close operations
     * (e.g., forget, undo). If not forgotten, the edits are merged into
     * the parent history as a single compound edit.
     *
     * @param closeOperations options controlling how the sub-history is closed
     */
    public void closeSubEditHistory(List<CloseEditStackOptions> closeOperations) {
        if (this.uncommitedHistories.isEmpty()) {
            return;
        }
        ;
        var lastUsedStack = this.uncommitedHistories.removeLast();
        ArrayList<PNEdit> doneEdits = lastUsedStack.getDoneEdits();
        PNEdit stackAsEdit = switch (doneEdits.size()) {
            case 0 -> null;
            case 1 -> doneEdits.getFirst();
            default -> new ListPNEdit(doneEdits.getFirst().getProbNet(), doneEdits);
        };
        if (stackAsEdit == null) {
            return;
        }
        if (!closeOperations.contains(CloseEditStackOptions.FORGET)) {
            var nextLastStack = this.uncommitedHistories.isEmpty() ? this.mainEditsHistory : this.uncommitedHistories.getLast();
            nextLastStack.addEdit(stackAsEdit);
        }
        if (closeOperations.contains(CloseEditStackOptions.UNDO)) {
            stackAsEdit.undo();
        }
    }
    
    /**
     * Returns the currently active {@link EditsHistory}: the topmost sub-history
     * if any are open, otherwise the main history.
     *
     * @return the current edit history
     */
    public EditsHistory getCurrentUndoManager() {
        if (!this.uncommitedHistories.isEmpty()) {
            return this.uncommitedHistories.getLast();
        }
        return this.mainEditsHistory;
    }
    
    
}

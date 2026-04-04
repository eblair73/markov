/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.model.network.ProbNet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used over a {@code openmarkov.inference.ProbNet} where
 * changes can be undone and redone. One edition has two parts:
 * <ol>
 * <li>Inform the listeners and
 * <li>If the action is not vetoed (with a {@code Exception}) for a
 * listener it does the edition.
 * </ol>
 *
 * @author Manuel Arias
 */
public class PNESupport /*extends UndoableEditSupport*/ {

    // Attributes
    /**
     * If {@code true} stores editions in
     * {@code openmarkov.undo#EditsHistory} for undo/redo.
     */
    private boolean withUndo;
    
    /**
     * List of undoable edits.
     *
     * @see javax.swing.undo.UndoManager
     */
    public final EditsHistoryStacker editsHistoryStacker;

    /*
    private boolean              significantEdits = true;

    private boolean              editsExecuted    = false;
    private int                  editCount;
    */

    
    // Constructor
    /**
     *
     */
    public PNESupport(ProbNet probNet) {
        super();
        this.probNet = probNet;
        this.withUndo = false;
        this.editsHistoryStacker = new EditsHistoryStacker();
    }

    // Methods

    /**
     * Set of listeners notified on edit events (undo, redo, execute).
     * Uses a concurrent set to allow safe addition and removal of listeners
     * from any thread while {@link #redo()}, {@link #undo()} or
     * {@link PNEdit#executeEdit()} iterate over it.
     */
    private final Set<PNEditListener> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Returns an unmodifiable view of the current listener set.
     * Use {@link #addListener(PNEditListener)} and
     * {@link #removeListener(PNEditListener)} to mutate it.
     *
     * @return unmodifiable view of registered {@link PNEditListener}s
     */
    public Set<PNEditListener> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }

    /**
     * Replaces the entire listener set with the given collection.
     * The replacement is performed atomically relative to other
     * {@code addListener}/{@code removeListener} calls.
     *
     * @param listeners new set of listeners; must not be {@code null}
     */
    public void setListeners(Collection<? extends PNEditListener> listeners) {
        this.listeners.clear();
        this.listeners.addAll(listeners);
    }
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public ArrayList<PNEdit> redo() {
        var redoneEdit = editsHistoryStacker.getCurrentUndoManager().redo();
        ArrayList<PNEdit> redoneEdits = flattenEdit(redoneEdit);
        for (PNEdit subRedoneEdit : redoneEdits) {
            for (PNEditListener listener : listeners) {
                listener.afterRedoingEdit(subRedoneEdit);
            }
        }
        return redoneEdits;
    }
    
    private ArrayList<PNEdit> flattenEdit(@Nullable PNEdit redoneEdit) {
        if (redoneEdit == null) {
            return new ArrayList<>();
        }
        var editsToVisit = new ArrayDeque<PNEdit>();
        editsToVisit.addFirst(redoneEdit);
        var flattenedEdits = new ArrayList<PNEdit>();
        while (!editsToVisit.isEmpty()) {
            var edit = editsToVisit.removeFirst();
            flattenedEdits.add(edit);
            if (edit instanceof CompoundPNEdit compoundEdit) {
                compoundEdit.getEdits().forEach(editsToVisit::addLast);
            }
        }
        return flattenedEdits;
    }
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public ArrayList<PNEdit> undo() {
        var undoneEdit = editsHistoryStacker.getCurrentUndoManager().undo();
        ArrayList<PNEdit> undoneEdits = flattenEdit(undoneEdit);
        for (PNEdit subUndoneEdit : undoneEdits) {
            for (PNEditListener listener : listeners) {
                listener.afterUndoingEdit(subUndoneEdit);
            }
        }
        return undoneEdits;
    }
    
    /**
     * Removes all undone edits from the current history, making redo unavailable.
     */
    public void removeUndoneEdits() {
        editsHistoryStacker.getCurrentUndoManager().removeUndoneEdits();
    }
    
    /**
     * Returns the currently active {@link EditsHistory} (may be a sub-history).
     *
     * @return the current edit history
     */
    public EditsHistory getCurrentEditHistory() {
        return editsHistoryStacker.getCurrentUndoManager();
    }
    
    /**
     * @return {@code true} if there are edits that can be undone
     */
    public boolean getCanUndo() {
        return editsHistoryStacker.getCurrentUndoManager().canUndo();
    }
    
    /**
     * @return {@code true} if there are edits that can be redone
     */
    public boolean getCanRedo() {
        return editsHistoryStacker.getCurrentUndoManager().canRedo();
    }
    
    /**
     * Add a {@code OpenParenthesisEdit} edit instance to
     * {@code basicUndoManager} and increases the parenthesis deph.
     */
    public void openNewSubEditHistory() {
        if (withUndo) {
            editsHistoryStacker.openNewSubEditHistory();
        }
    }
    
    /**
     * Add a {@code CloseParenthesisEdit} edit instance to
     * {@code basicUndoManager} and decreases the parenthesis deph.
     */
    public void closeSubEditHistory(CloseEditStackOptions... closeOperations) {
        if (withUndo) {
            editsHistoryStacker.closeSubEditHistory(List.of(closeOperations));
        }
    }
    
    /**
     * Cancels the current sub-edit history by undoing all its edits and discarding them.
     */
    public void cancelLastSubEditHistory() {
        closeSubEditHistory(CloseEditStackOptions.FORGET, CloseEditStackOptions.UNDO);
    }
    
    /**
     * @return withUndo {@code boolean}.
     */
    public boolean isWithUndo() {
        return withUndo;
    }
    
    /**
     * @param withUndo {@code boolean}.
     */
    public void setWithUndo(boolean withUndo) {
        this.withUndo = withUndo;
    }
    
    /**
     * @return probNet {@code ProbNet}.
     */
    /*
     * public ProbNet getProbNet() { return (ProbNet)realSource; }
     */
    public String toString() {
        String out = "PNESupport. probNet: " + probNet;
        if (listeners != null) {
            out += " Number of listeners: " + listeners.size() + '.';
        } else {
            out += " Number of listeners: 0.";
        }
        if (withUndo) {
            out += " With undo.";
        } else {
            out += " Without undo.";
        }
        return out;
    }
    
    private final ProbNet probNet;
    
    /**
     * Registers a listener to be notified of edit events on this network.
     * Safe to call from any thread.
     *
     * @param listener the listener to add; no-op if already registered
     */
    public void addListener(PNEditListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Unregisters a previously added listener.
     * Safe to call from any thread, including from within a listener callback.
     *
     * @param listener the listener to remove; no-op if not registered
     */
    public void removeListener(PNEditListener listener) {
        this.listeners.remove(listener);
    }
}

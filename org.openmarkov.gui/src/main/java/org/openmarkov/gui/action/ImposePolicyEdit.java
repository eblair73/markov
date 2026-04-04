package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.graphic.VisualDecisionNode;

/**
 * Edit that imposes or replaces a policy on a decision node, supporting undo and redo.
 */
public class ImposePolicyEdit extends PNEdit {

    private final VisualDecisionNode visualDecisionNode;
    private final Potential newPolicy;
    private final Potential lastPolicy;

    /**
     * Creates a new edit that imposes a policy on a decision node.
     *
     * @param visualDecisionNode the visual decision node to modify
     * @param lastPolicy         the previous policy (used for undo)
     * @param newPolicy          the new policy to impose
     */
    public ImposePolicyEdit(VisualDecisionNode visualDecisionNode,Potential lastPolicy, Potential newPolicy) {
        super(visualDecisionNode.getNode().getProbNet());
        this.visualDecisionNode = visualDecisionNode;
        this.lastPolicy = lastPolicy;
        this.newPolicy = newPolicy;
    }
    /**
     * Creates a new edit that imposes a policy, using the node's current potential as the previous policy.
     *
     * @param visualDecisionNode the visual decision node to modify
     * @param newPolicy          the new policy to impose
     */
    public ImposePolicyEdit(VisualDecisionNode visualDecisionNode, Potential newPolicy) {
        super(visualDecisionNode.getNode().getProbNet());
        this.visualDecisionNode = visualDecisionNode;
        this.lastPolicy = visualDecisionNode.getNode().getPotential();
        this.newPolicy = newPolicy;
    }

    @Override
    protected void doEdit() throws DoEditException {
        visualDecisionNode.setPolicy(newPolicy);
    }


    @Override
    public void undo() {
        if(lastPolicy != null) {
            visualDecisionNode.setPolicy(lastPolicy);
        }else{
            visualDecisionNode.removePolicy();
        }
    }

    @Override
    public void redo() {
        visualDecisionNode.setPolicy(newPolicy);
    }
}

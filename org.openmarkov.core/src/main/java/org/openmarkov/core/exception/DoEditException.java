/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

public abstract class DoEditException extends OpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    //TODO: This is a wrapper, meaning either design is poor, or the exceptions it encloses
    // could be turned into RuntimeExceptions.
    public static final class CannotDoEditException extends DoEditException {
        public CannotDoEditException(IOpenMarkovException originException) {
            initCause((Exception) originException);
            this.originException = originException;
        }

        public final IOpenMarkovException originException;
    }
    
    //TODO: Used just in OOP Nets, which are deprecated
	public static final class InstanceAlreadyExists extends DoEditException {
		public InstanceAlreadyExists(String instanceName) {
            this.instanceName = instanceName;
        }
        
        public final String instanceName;
    }
    
    //TODO: Used by RemoveNodeEdit in case a node isn't selected, but... Can that really happen? It is likely this
    // can be removed
	public static final class NodeIsNull extends DoEditException {
		public NodeIsNull(ProbNet probNet) {
            this.probNet = probNet;
        }
        
        public final ProbNet probNet;
    }
    
    //TODO: It is caught and ignored in almost every catch block, probably leading to unexpected
    // bugs.
    // Perhaps it could be turned into a RuntimeException.
	public static final class CannotRemovePotential extends DoEditException {
		public CannotRemovePotential(ProbNet probNet, Potential oldPotential) {
            this.probNet = probNet;
            this.oldPotential = oldPotential;
        }
        
        public final ProbNet probNet;
        public final Potential oldPotential;
    }
	
	public static final class CannotInvertLink extends DoEditException {
		public CannotInvertLink(Node from, Node to, ProbNet probNet, List<PNConstraint> unsatisfiedConstraint) {
            this.from = from;
            this.to = to;
            this.probNet = probNet;
            this.unsatisfiedConstraint = unsatisfiedConstraint;
        }
        
        public final Node from;
        public final Node to;
        public final ProbNet probNet;
        public final List<PNConstraint> unsatisfiedConstraint;
    }
	
}

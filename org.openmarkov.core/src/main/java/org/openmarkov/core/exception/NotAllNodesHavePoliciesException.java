/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.Node;

import java.util.List;
import java.util.stream.Collectors;

//TODO: It is caught and then thrown as UnreachableException. This might be a RuntimeException.
public class NotAllNodesHavePoliciesException extends OpenMarkovException {
    
    private final Node conditioningDecision;
    private final List<Node> nodesWithoutPolicy;
    
    public NotAllNodesHavePoliciesException(Node conditioningDecision, List<Node> nodesWithoutPolicy) {
        this.conditioningDecision = conditioningDecision;
        this.nodesWithoutPolicy = nodesWithoutPolicy;
    }
    
    @Override @Nullable public String getExceptionMessage() {
        var nodesNames = this.nodesWithoutPolicy.stream().map(Node::getName)
                                                .map(name -> "\t- " + name)
                                                .collect(Collectors.joining("\n"));
        return "There are " + this.nodesWithoutPolicy.size() + " node(s) without policy other than " + conditioningDecision.getName()
                + ":\n" + nodesNames;
    }
    
    @Override @Nullable public String getExceptionTitle() {
        return "Not all nodes have policies";
    }
    
    @Override public String toString() {
        return IOpenMarkovException.toString(this);
    }
}


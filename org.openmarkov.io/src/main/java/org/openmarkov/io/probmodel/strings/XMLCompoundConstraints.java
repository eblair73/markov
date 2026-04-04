/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.strings;

import org.openmarkov.java.enumUtils.EnumUtils;

/** A compound constraint defines a type of ProbNet and it contains a bundle of basic constraints. */
public enum XMLCompoundConstraints {
    BAYESIAN_NETWORK,
    DINAMIC_BAYESIAN_NETWORK,
    INFLUENCE_DIAGRAM,
    MDP,
    POMDP;
    
    public String toString() {
        return switch (this) {
            case MDP -> "MDP";
            case POMDP -> "POMDP";
            default -> EnumUtils.toPascalCase(this);
        };
	}
 
}

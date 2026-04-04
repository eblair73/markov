/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.strings;

import org.openmarkov.java.enumUtils.EnumUtils;

/** Constraints applicable to a ProbNet */
public enum XMLBasicConstraints {
    DISTINCT_VARIABLE_NAMES,
    MAX_NUM_PARENTS,
    NO_BACKWARD_LINKS,
    NO_CLOSED_PATH,
    NO_CYCLES,
    NO_EMPTY_NAME,
    NO_MIXED_PARENTS,
    NO_SELF_LOOPS,
    NO_SUPER_VALUE_NODES,
    NO_UTILITY_PARENTS,
    NOT_EQUAL_LINKS,
    NOT_MULTIPLE_LINKS,
    ONLY_CHANCE_NODES,
    ONLY_DIRECTED_LINKS,
    ONLY_FINITE_STATE_VARIABLES,
    ONLY_NUMERIC_VARIABLES,
    ONLY_ONE_UTILITY_NODE,
    ONLY_TEMPORAL_NODES,
    ONLY_UNDIRECTED_LINKS,
    UNLABELED_LINKS;
    
    
    @Override public String toString() {
        return EnumUtils.toPascalCase(this);
    }
}

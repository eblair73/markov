/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.java.enumUtils.EnumUtils;

/**
 * Enumerates the roles a potential can play in a probabilistic graphical model:
 * conditional probability, joint probability, policy, link restriction, or unspecified.
 *
 * @author Manuel Arias
 * @version 1.0
 */
public enum PotentialRole implements Localizable {
    
    CONDITIONAL_PROBABILITY,
    JOINT_PROBABILITY,
    POLICY,
    LINK_RESTRICTION,
    UNSPECIFIED,
    // DECISION,
    // UTILITY,
    // TODO Remove
    // INTERVENTION,
    // UTIL_2
	;
    
    public String toString() {
        return EnumUtils.toCamelCase(this);
	}
    
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.toString();
    }
}

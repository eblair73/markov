/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for constraint package.
 */
public class UtilConstraints {
    
    public static <TargetEdit extends PNEdit> List<TargetEdit> getSimpleEditsByType(PNEdit edit, Class<TargetEdit> typeEditClass) {
        List<TargetEdit> edits = new ArrayList<>();
        if (typeEditClass.isInstance(edit)) {
            edits.add(typeEditClass.cast(edit));
        }
        // Check compound edits
        if (edit instanceof CompoundPNEdit compoundPNEdit) {
            for (PNEdit simpleEdit : compoundPNEdit.getEdits()) {
                edits.addAll(UtilConstraints.getSimpleEditsByType(simpleEdit, typeEditClass));
            }
        }
        return edits;
    }
    
}

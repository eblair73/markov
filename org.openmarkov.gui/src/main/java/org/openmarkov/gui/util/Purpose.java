/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

/**
 * This class is used to encapsulate the purpose of the nodes and their
 * dependent-language strings.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - fix initial values for fields
 */
public class Purpose {
    /**
     * Internal names of the different purposes.
     */
    private static final List<String> LIST = Arrays.asList("", "cost", "effectiveness", "treatment", "riskfactor",
                                                           "symptom", "sign", "test", "diseaseanomaly",
                                                           "auxiliary", "other");
    
    /**
     * It retrieves the dependent-language string of the desired purpose. If the
     * purpose hasn't a dependent-language string (because this purpose isn't a
     * registered one), the returned string is the purpose itself.
     *
     * @param element name of the purpose.
     *
     * @return a string that represents the purpose in the actual language.
     */
    public static String getString(String element) {
        if (element.isEmpty()) {
            return element;
        }
        try {
            return StringDatabase.getUniqueInstance().getString("purpose." + element + ".Text");
        } catch (MissingResourceException e) {
            return element;
        }
    }
    
    /**
     * This method returns an array of strings, each one has the
     * dependent-language string of each purpose.
     *
     * @return an array that contains a list of string that contains the
     * different purposes.
     */
    public static String[] getListStrings(boolean original) {
        if (!original) {
            return Purpose.LIST.stream().map(Purpose::getString).toArray(String[]::new);
        }
        String[] strings = new String[Purpose.LIST.size()];
        Purpose.LIST.toArray(strings);
        return strings;
    }
    
    /**
     * This method returns the purpose sited in the specified index in the list.
     * If the index is out of range (index &lt; 0 || index &gt; list size) the null is
     * returned.
     *
     * @param index element of the list of purposes.
     *
     * @return a string that contains the name of the specified purpose.
     */
    public static @Nullable String getByIndex(int index) {
        try {
            return Purpose.LIST.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    /**
     * This method returns the index in the list of the purpose passed as
     * parameter. If the parameter doesn't match any element of the list, then
     * the last index is returned.
     *
     * @param element name of the purpose to search.
     *
     * @return the index in the list of the purpose.
     */
    public static int getIndex(String element) {
        int index = Purpose.LIST.indexOf(element);
        if (index == -1) {
            return Purpose.LIST.size() - 1;
        }
        return index;
    }
}

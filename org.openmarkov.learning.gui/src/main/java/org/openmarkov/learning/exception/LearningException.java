package org.openmarkov.learning.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;

public abstract sealed class LearningException extends OpenMarkovException {
    
    public static final class NoDatabasePresent extends LearningException {
    }
    
    public static final class NoChosenVariables extends LearningException {
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}

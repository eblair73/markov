package org.openmarkov.stochasticPropagationOutput.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;

public class EmptyCaseDatabaseException extends OpenMarkovException {
    public EmptyCaseDatabaseException(StochasticPropagation algorithm) {
        this.algorithm = algorithm;
    }
    
    private final StochasticPropagation algorithm;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}

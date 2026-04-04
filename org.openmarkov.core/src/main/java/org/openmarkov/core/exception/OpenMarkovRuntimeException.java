/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * Base class for all unchecked domain exceptions in OpenMarkov.
 * <p>
 * Catching this type allows callers to handle any expected domain-level failure
 * (e.g. non-projectable potential, non-evaluable network, incompatible evidence)
 * without needing to enumerate every concrete subtype.
 * <p>
 * Infrastructure exceptions ({@link UnreachableException}, {@link UnrecoverableException})
 * do NOT extend this class — they represent programming errors or fatal system
 * conditions, not domain errors.
 */
public abstract class OpenMarkovRuntimeException extends RuntimeException implements IBundledOpenMarkovException {

    protected OpenMarkovRuntimeException() {
    }

    protected OpenMarkovRuntimeException(Throwable cause) {
        super(cause);
    }
}

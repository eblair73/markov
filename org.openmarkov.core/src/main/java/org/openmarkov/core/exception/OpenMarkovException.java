/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * Base class for all checked domain exceptions in OpenMarkov.
 * <p>
 * Catching this type allows callers to handle any expected domain-level failure
 * (e.g. parser errors, edit failures, incompatible evidence) without needing to
 * enumerate every concrete subtype.
 * <p>
 * The i18n support (localised title and message) is provided automatically by
 * {@link IBundledOpenMarkovException}: subclasses only need to register a
 * {@code .properties} entry for their simple class name.
 * <p>
 * Infrastructure exceptions ({@link UnreachableException}, {@link UnrecoverableException})
 * do NOT extend this class — they represent programming errors or fatal system
 * conditions, not domain errors.
 * <p>
 * When a checked exception must be propagated as unchecked (e.g. inside Swing's
 * Event Dispatch Thread), wrap it with {@link UnrecoverableException} rather than
 * converting the concrete class to extend {@link OpenMarkovRuntimeException}.
 *
 * @see OpenMarkovRuntimeException
 * @see IBundledOpenMarkovException
 */
public abstract class OpenMarkovException extends Exception implements IBundledOpenMarkovException {

    protected OpenMarkovException() {
    }

    protected OpenMarkovException(Throwable cause) {
        super(cause);
    }

    protected OpenMarkovException(String message) {
        super(message);
    }

    protected OpenMarkovException(String message, Throwable cause) {
        super(message, cause);
    }
}

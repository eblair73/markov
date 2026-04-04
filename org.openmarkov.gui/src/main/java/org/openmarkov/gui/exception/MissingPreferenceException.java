package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

//TODO: This exception always show the exception and ignore it, and while that should be intented
// for this specific exception, it should have a way to stop showing, as if there is a problem saving
// a value, it is likely there will be many more, showing them all could be displeasing to the user.
// Also, the use of this exception is wrong, as every place where it happens is a place where a default
// value for a missing preference should be used when missing, but instead, it returns null/empty values.
public class MissingPreferenceException extends OpenMarkovException {
    
    public final String key;
    
    public MissingPreferenceException(String key) {
        this.key = key;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}

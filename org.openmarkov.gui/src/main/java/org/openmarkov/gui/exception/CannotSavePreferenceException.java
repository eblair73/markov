package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

//TODO: This exception always show the exception and ignore it, and while that should be intented
// for this specific exception, it should have a way to stop showing, as if there is a problem saving
// a value, it is likely there will be many more, showing them all could be displeasing to the user.
public class CannotSavePreferenceException extends OpenMarkovException {
    
    public CannotSavePreferenceException(String key, Object value) {
        this.key = key;
        this.value = value;
        this.valueAsString = value.toString();
    }
    
    public final String key;
    public final Object value;
    public final String valueAsString;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}

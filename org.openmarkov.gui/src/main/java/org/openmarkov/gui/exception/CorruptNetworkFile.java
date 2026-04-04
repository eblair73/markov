package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

import java.net.URL;

public class CorruptNetworkFile extends OpenMarkovException {
    
    public CorruptNetworkFile(URL fileToRead, Exception ex) {
        super(ex);
        this.fileToRead = fileToRead;
    }
    
    private final URL fileToRead;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}

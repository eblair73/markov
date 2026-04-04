open module org.openmarkov.io {
    requires org.openmarkov.core;
    
    requires org.jdom2;
    requires org.apache.commons.io;
    requires java.desktop;
    requires jeval;
    requires org.jetbrains.annotations;
    requires org.openmarkov.annotation_processing;
    requires org.openmarkov.inference;
    
    uses javax.swing.event.UndoableEditListener;
    uses org.xml.sax.InputSource;
    
    exports org.openmarkov.io.probmodel.exception;
    exports org.openmarkov.io.probmodel.reader;
    exports org.openmarkov.io.probmodel.writer;
    exports org.openmarkov.io.probmodel.strings;
    
    exports org.openmarkov.io.xmlbif.strings;
    exports org.openmarkov.io.xmlbif;
}

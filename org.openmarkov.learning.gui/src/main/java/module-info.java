open module org.openmarkov.learning.gui {
	requires org.openmarkov.core;
	requires org.openmarkov.gui;
	requires org.openmarkov.learning.core;
	requires org.openmarkov.learning.algorithm;
	requires org.openmarkov.learning.metric;
	requires org.apache.commons.io;
	requires swing.layout;
	requires java.logging;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires org.openmarkov.annotation_processing;
    requires org.apache.poi.poi;
    
    exports org.openmarkov.learning.gui;
	exports org.openmarkov.learning.gui.localize;
	exports org.openmarkov.learning.gui.interactive;
}

package org.openmarkov.gui.loader.element;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.exception.ResourceNotFoundException;

import javax.swing.*;
import java.net.URL;

/**
 * Safe bindings to icons files.
 *
 * @author jrico
 */
public enum IconBind {
    
    NEW_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "new.gif"),
    OPEN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "open.gif"),
    OPEN_URL_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "openURL.gif"),
    SAVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "save.gif"),
    CLOSE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "close.gif"),
    UNDO_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "undo.gif"),
    REDO_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "redo.gif"),
    ACCEPT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "green_ok.gif"),
    APPLY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "green_apply.gif"),
    SELECTION_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "selection.gif"),
    CHANCE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "chance.gif"),
    DECISION_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "decision.gif"),
    UTILITY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "utility.gif"),
    LINK_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "link.gif"),
    ZOOM_IN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "zoomin.gif"),
    ZOOM_OUT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "zoomout.gif"),
    CUT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "cut.gif"),
    COPY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "copy.gif"),
    PASTE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "paste.gif"),
    REMOVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "remove.gif"),
    ARROW_UP_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "arrowUp.gif"),
    ARROW_DOWN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "arrowDown.gif"),
    PLUS_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "plus.gif"),
    MINUS_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "minus.gif"),
    INFINITE_POSITIVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "positiveInfinite.gif"),
    INFINITE_NEGATIVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "negativeInfinite.gif"),
    OPENMARKOV_LOGO_16(Locations.STANDARD_RESOURCE_ICONS_PATH + "OM_16p4.png"),
    EDITION_MODE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "edition_mode.png"),
    INFERENCE_MODE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "inference_mode.png"),
    CREATE_NEW_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "createNewCase.png"),
    GO_TO_FIRST_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goFirst.png"),
    GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goPrevious.png"),
    GO_TO_NEXT_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goNext.png"),
    GO_TO_LAST_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goLast.png"),
    CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "clearOutAllCases.png"),
    PROPAGATE_EVIDENCE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "propagate_evidence.png"),
    UNCERTAINTY(Locations.STANDARD_RESOURCE_ICONS_PATH + "uncertainty2.png"),
    DECISION_TREE(Locations.STANDARD_RESOURCE_ICONS_PATH + "dectree.gif"),
    OPTIMAL_STRATEGY(Locations.STANDARD_RESOURCE_ICONS_PATH + "optimalStrategy.gif"),
    COST_EFFECTIVENESS(Locations.STANDARD_RESOURCE_ICONS_PATH + "costEffectiveness.gif"),
    SENS_ANALYSIS(Locations.STANDARD_RESOURCE_ICONS_PATH + "sensAnalysis.gif");
    
    public final String fileName;
    
    IconBind(String fileName) {
        this.fileName = fileName;
    }
    
    public ImageIcon icon() {
        URL icon = IconBind.class.getResource(this.fileName);
        if (icon == null) {
            throw new UnreachableException(new ResourceNotFoundException(this.fileName));
        }
        return new ImageIcon(icon);
    }
    
    
}

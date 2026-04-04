/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class implements a file chooser dialog file to select OpenMarkov files.
 *
 * @author ibermejo
 */
@SuppressWarnings("serial")
public class NetworkOMFileChooser extends OMFileChooser {
    /**
     * Creates a new file chooser that starts in the current directory,
     * filtering the files with the file filters.
     *
     * @param isOpening Indicates if the file chooser is for opening a file
     *                  (isOpening=true) or for saving (isOpening=false)
     */
    public NetworkOMFileChooser(boolean acceptAllfile, boolean isOpening) {
        super();
        setName("NetworkOMFileChooser");
        setAcceptAllFileFilterUsed(acceptAllfile);
        rescanCurrentDirectory();
        var parsers = isOpening ? FormatManager.readersClasses() : FormatManager.writersClasses();
        parsers
                .sorted(Comparator.comparing(readerClass -> FormatManager.info(readerClass).description()))
                .forEach(readerClass -> {
                    String description = FormatManager.info(readerClass).description();
                    addChoosableFileFilter(new FileFilterAll<>(readerClass, FormatManager.info(readerClass)
                            .extension(), description));
                });

        File currentDirectory = null;
        /*
         * setFileFilter (OpenMarkovPreferences.get
         * (OpenMarkovPreferences.LAST_OPENED_FORMAT,
         * OpenMarkovPreferences.OPENMARKOV_FORMATS, "pgmx"));
         */
        // UNCLEAR Where is set pgmx? By default LAST_OPENED_FORMAT=pgmx

        if (isOpening) {
            currentDirectory = LocalPreferences.LATEST_OPEN_DIRECTORY.get();
            setFileFilter("OpenMarkov");
        } else {
            setFileFilter(LocalPreferences.LATEST_SAVED_NETWORK_FORMAT.get());
        }

        setCurrentDirectory(currentDirectory);
    }

    public NetworkOMFileChooser() {
        this(false, true);
    }

    @Override
    public int showOpenDialog(Component parent) {
        int result = super.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            LocalPreferences.LATEST_OPEN_DIRECTORY.set(getSelectedFile());
            /*
             * OpenMarkovPreferences.set (OpenMarkovPreferences.LAST_OPENED_FORMAT,
             * ((FileFilterBasic) getFileFilter ()).getFilterExtension (),
             * OpenMarkovPreferences.OPENMARKOV_FORMATS);
             */
            try {
                LocalPreferences.LATEST_NETWORK_FORMAT.set(getPgmxFileFormat());
            } catch (SAXException | IOException e) {
                throw new UnrecoverableException(e);
            }
        }
        return result;
    }

    @Override
    public int showSaveDialog(Component parent) {
        int result = super.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            LocalPreferences.LATEST_OPEN_DIRECTORY.set(getSelectedFile());
            /*
             * OpenMarkovPreferences.set (OpenMarkovPreferences.LAST_OPENED_FORMAT,
             * ((FileFilterBasic) getFileFilter ()).getFilterExtension (),
             * OpenMarkovPreferences.OPENMARKOV_FORMATS);
             */
            if (getFileFilter() instanceof FileFilterAll) {
                LocalPreferences.LATEST_SAVED_NETWORK_FORMAT
                        .set(((FileFilterAll) getFileFilter()).getFileDescription());
            }
        }
        return result;
    }

    @Override
    public void approveSelection() {
        if (getDialogType() == SAVE_DIALOG) {
            File selectedFile = getSelectedFile();
            if ((selectedFile != null) && selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName()
                        + " already exists. Do you want to replace the existing file?", "Ovewrite file",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response != JOptionPane.YES_OPTION)
                    return;
            }
        }
        super.approveSelection();
    }

    /**
     * Extracts the version of a pgmx file and concatenate it to the String
     * "OpenMarkov" for having the description of the file
     *
     * @return the format OpenMarkov.version of a pgmx file
     *
     * @throws IOException if an I/O error occurs
     * @throws SAXException if an XML parsing error occurs
     */

    public String getPgmxFileFormat() throws SAXException, IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getSelectedFile());
            String version = doc.getDocumentElement().getAttribute("formatVersion");
            // Removing the last digit of the version
            version = version.substring(0, version.lastIndexOf('.'));
            return "OpenMarkov." + version;
        } catch (ParserConfigurationException e) {
            throw new UnreachableException(e);
        }

    }
}

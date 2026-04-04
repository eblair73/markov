/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.localize.StringDatabase;

import java.io.File;

/**
 * This class implements the base code for all the file filters of the
 * application. By default, it accepts all the directories and initialises the
 * string resource.
 *
 * @author jmendoza
 * @version 1.0
 */
public class FileFilterAll<T> extends FileFilterBasic {

	/**
	 * Extension of the files that match this filter.
	 */
    private final String formatExtension;

	/**
	 * Description of the files that match this filter.
	 */
	private String fileDescription = "OpenMarkov";

	private final T formatInfo;
	
	public T getFormatInfo() {
		return this.formatInfo;
	}
	
	/**
	 * Create a new instance and create a new string resource.
	 */
	public FileFilterAll(T formatInfo, String extension, String description) {
		this.formatInfo = formatInfo;
		formatExtension = extension;
		setFileDescription(description);
	}

	/**
	 * Accepts all the directories (by default in OpenMarkovtFileFilter) and files
	 * whose extension is 'pgmx'.
	 *
	 * @return true if the file is a directory; false otherwise
	 */
	@Override public boolean accept(File file) {

		boolean result = super.accept(file);
        String fileExtension;

		if (!result) {
			fileExtension = getExtension(file);
			return (fileExtension.equals(formatExtension));
		}

		return true;

	}

	/**
	 * Returns the description of the OpenMarkov files
	 *
	 * @return a string representing the description of the files type
	 */
	@Override public String getDescription() {

		return StringDatabase.getUniqueInstance().getString("FileExtension." + getFileDescription() + ".Description")
				+ " (*." + formatExtension + ")";

	}
	//CMI

	/**
	 * @return the fileDescription used to match the filter with the proper Reader/Writer
	 */
	public String getFileDescription() {
		return fileDescription;
	}

	/**
	 * Sets the fileDescripion used to match the filter with the proper Reader/Writer
	 *
	 * @param fileDescription the fileDescription used to match the filter with the proper Reader/Writer
	 */
	public void setFileDescription(String fileDescription) {
		this.fileDescription = fileDescription;
	}

	//CMF

	/**
	 * Returns the extension of the files that match this filter.
	 *
	 * @return accepted extension by the filter.
	 */
	@Override public String getFilterExtension() {

		return formatExtension;

	}

}

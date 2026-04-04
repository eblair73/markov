/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmarkov.learning.gui;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;

import javax.swing.*;
import java.util.ArrayList;

/**
 * This abstract class represents the dialog that shows the user the options
 * and parameters of each learning algorithm.
 *
 * @author joliva
 * @author ibermejo
 * @author Manuel Arias
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({JFrame.class, boolean.class}))
public abstract class AlgorithmParametersDialog extends javax.swing.JDialog {

	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	/**
	 * Alpha (Laplace-like correction) parameter, shared by all algorithm dialogs.
	 * Default value is 0.5.
	 */
	protected String alphaParameter = "0.5";

	/**
	 * Text field for the alpha parameter. Subclasses must instantiate this field
	 * in their {@code initComponents()} method and include it in their layout.
	 */
	protected JTextField alphaText;

	/**
	 * Dialog that shows the user the options and parameters of each learning
	 * algorithm.
	 *
	 * @param parent the parent
	 * @param modal the modal
	 */
	public AlgorithmParametersDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
	}

	/**
	 * Validates the content of {@code alphaText}, and if valid, stores the
	 * value in {@code alphaParameter}. Throws {@link InvalidArgumentException}
	 * if the text cannot be parsed as a number in [0, 1].
	 */
	protected void applyAlpha() {
		@Nullable Double alpha;
		try {
			alpha = Double.parseDouble(alphaText.getText());
		} catch (NumberFormatException e) {
			alpha = null;
		}
		if (alpha == null || alpha < 0 || alpha > 1) {
			throw new InvalidArgumentException(alpha, "alpha", "must be between 0 and 1");
		}
		alphaParameter = alphaText.getText();
	}

	public String getAlphaParameter() {
		return alphaParameter;
	}

	public abstract String getDescription();

	public abstract LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database);

	public abstract ArrayList<Object> getOptions();

}

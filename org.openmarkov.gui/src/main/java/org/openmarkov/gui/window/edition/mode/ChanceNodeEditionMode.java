/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

/**
 * Edition mode that creates a new chance node on mouse click.
 */
@EditionState(name = "Edit.Mode.Chance", icon = "chance.gif", cursor = "chance.gif") public class ChanceNodeEditionMode
		extends NodeEditionMode {

	public ChanceNodeEditionMode(NetworkEditorPanel networkEditorPanel, ProbNet probNet) {
		super(networkEditorPanel, probNet, NodeType.CHANCE);
	}
}

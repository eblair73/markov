/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.action.base.PNEdit;

@SuppressWarnings("serial")

/*
  {@code PurposeEdit} is a simple edit that allows modify
  the node purpose property.

  @version 1.0 21/12/10
 * @author Miguel Palacios
 */
public class PurposeEdit extends PNEdit {
	/**
	 * The last purpose before the edition
	 */
	private final String lastPurpose;
	/**
	 * The new purpose after the edition
	 */
	private final String newPurpose;
	/**
	 * The edited node
	 */
    private final Node node;

	/**
	 * Creates a new {@code PurposeEdit} with the node and its new purpose.
	 *
	 * @param node       the edited node
	 * @param newPurpose the new purpose
	 */
	public PurposeEdit(Node node, String newPurpose) {
		super(node.getProbNet());
		this.lastPurpose = node.getPurpose();
		this.newPurpose = newPurpose;
		this.node = node;
	}
	
	@Override protected void doEdit() {
		node.setPurpose(newPurpose);
	}
    
    @Override public void undo() {
		super.undo();
		node.setPurpose(lastPurpose);
	}

	/**
	 * Gets the new purpose after the edition
	 *
	 * @return the new purpose
	 */
	public String getNewPurpose() {
		return newPurpose;
	}

	/**
	 * Gets the last purpose before the edition
	 *
	 * @return the last purpose
	 */
	public String getLastPurpose() {
		return lastPurpose;
	}
}


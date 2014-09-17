/*******************************************************************************
 * Copyright (c) 2014 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.compare.git.pgm;

import org.eclipse.equinox.app.IApplication;

/**
 * List of all code that {@link org.eclipse.emf.compare.git.pgm.LogicalApp} can return.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public enum Returns {
	/**
	 * Action terminated normally.
	 */
	COMPLETE(IApplication.EXIT_OK),
	/**
	 * The action has not finished completely.
	 */
	ABORTED(Integer.valueOf(1)),
	/**
	 * An error has occurred.
	 */
	ERROR(Integer.valueOf(128));

	private final Integer code;

	private Returns(Integer code) {
		this.code = code;
	}

	public final Integer code() {
		return code;
	}

	public static Returns valueOf(int code) {
		for (Returns r : Returns.values()) {
			if (r.code().equals(Integer.valueOf(code))) {
				return r;
			}
		}
		System.err.println(code + " is not a valid return code");
		return ERROR;
	}
}

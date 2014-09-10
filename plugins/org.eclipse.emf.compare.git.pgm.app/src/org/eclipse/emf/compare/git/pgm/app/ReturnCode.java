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
package org.eclipse.emf.compare.git.pgm.app;

import org.eclipse.equinox.app.IApplication;

/**
 * List of all code that {@link org.eclipse.emf.compare.git.pgm.app.LogicalApp} can return.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public final class ReturnCode {

	/**
	 * Action terminated normally.
	 */
	public static final Integer COMPLETE = IApplication.EXIT_OK;

	/**
	 * The action has not finished completely.
	 */
	public static final Integer ABORTED = Integer.valueOf(1);

	/**
	 * An error has occurred.
	 */
	public static final Integer ERROR = Integer.valueOf(128);

	/**
	 * Private constructor.
	 */
	private ReturnCode() {
	}

	/**
	 * Convert the int code to the corresponding ReturnCode.
	 * 
	 * @param code
	 *            the int code.
	 * @return the corresponding ReturnCode.
	 */
	public static Integer convert(int code) {
		Integer returnCode;
		switch (code) {
			case 0:
				returnCode = ReturnCode.COMPLETE;
				break;
			case 1:
				returnCode = ReturnCode.ABORTED;
				break;
			default:
				returnCode = ReturnCode.ERROR;
				break;
		}
		return returnCode;
	}
}

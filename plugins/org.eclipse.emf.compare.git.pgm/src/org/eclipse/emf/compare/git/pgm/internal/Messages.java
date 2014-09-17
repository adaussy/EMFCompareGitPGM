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
package org.eclipse.emf.compare.git.pgm.internal;

/**
 * Class holding all messages.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public final class Messages {
	/**
	 * Message displayed when an uncatch IO error is raised.
	 */
	public static final String UNKOWN_IO_ERROR_MESSAGE = "Unkown IO error";

	/**
	 * Message displayed when the repository can no be found.
	 */
	public static final String CAN_T_FIND_GIT_REPOSITORY_MESSAGE = "Can't find git repository";

	/**
	 * Constructor.
	 */
	private Messages() {
	}

}

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
package org.eclipse.emf.compare.git.pgm.internal.exception;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * This exception is raised when a argument on the command line failed the validation process.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class ArgumentValidationError extends CmdLineException {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = -8121393950063566490L;

	/**
	 * Constructor.
	 * 
	 * @param parser
	 *            {@link CmdLineException#CmdLineException(CmdLineParser, String)}
	 * @param message
	 *            {@link CmdLineException#CmdLineException(CmdLineParser, String)}
	 */
	public ArgumentValidationError(CmdLineParser parser, String message) {
		super(parser, message);
	}

	/**
	 * Constructor.
	 * 
	 * @param parser
	 *            {@link CmdLineException#CmdLineException(CmdLineParser, Throwable)}
	 * @param cause
	 *            {@link CmdLineException#CmdLineException(CmdLineParser, Throwable)}
	 */
	public ArgumentValidationError(CmdLineParser parser, Throwable cause) {
		super(parser, cause);
	}

}

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
package org.eclipse.emf.compare.git.pgm.internal.args;

import java.io.File;

import org.eclipse.emf.compare.git.pgm.internal.exception.ArgumentValidationError;
import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.FileOptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link FileOptionHandler} used to parse the setup argument.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
// TODO: Load the Oomph configuration here
public class SetupFileOptionHandler extends FileOptionHandler {
	/**
	 * Constructor.
	 * 
	 * @param parser
	 *            {@link org.kohsuke.args4j.spi.OptionHandler.OptionHandler#owner}
	 * @param option
	 *            {@link org.kohsuke.args4j.spi.OptionHandler.OptionHandler#option}
	 * @param setter
	 *            {@link org.kohsuke.args4j.spi.OptionHandler.OptionHandler#setter}
	 */
	public SetupFileOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super File> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		File setupFile = EMFCompareGitPGMUtil.toFileWithAbsolutePath(params.getParameter(0));
		if (!setupFile.exists()) {
			throw new ArgumentValidationError(owner, setupFile + " setup file does not exist");
		}
		setter.addValue(setupFile);
		return 1;
	}

}

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

import com.google.common.base.Preconditions;

import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.StringOptionHandler;

/**
 * OptionHandler that parses a git directory from a command line and set the gitdir value of the
 * CmdLineParserRepositoryBuilder.
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
public class GitDirHandler extends StringOptionHandler {

	/**
	 * Constructor.
	 * 
	 * @param parser
	 *            {@link OptionHandler#owner}
	 * @param option
	 *            {@link OptionHandler#option}
	 * @param setter
	 *            {@link OptionHandler#setter}
	 */
	public GitDirHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
		super(parser, option, setter);
		Preconditions.checkArgument(parser instanceof CmdLineParserRepositoryBuilder);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.kohsuke.args4j.spi.OptionHandler#parseArguments(org.kohsuke.args4j.spi.Parameters)
	 */
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String dir = params.getParameter(0);
		String absoluteDir = EMFCompareGitPGMUtil.toFileWithAbsolutePath(dir).toString();
		((CmdLineParserRepositoryBuilder)owner).setGitDir(absoluteDir);
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
	 */
	@Override
	public String getDefaultMetaVariable() {
		return "gitdir"; //$NON-NLS-1$
	}

}

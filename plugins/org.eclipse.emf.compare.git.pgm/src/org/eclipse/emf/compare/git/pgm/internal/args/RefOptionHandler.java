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

import org.eclipse.emf.compare.git.pgm.internal.exception.ArgumentValidationError;
import org.eclipse.jgit.lib.ObjectId;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * OptionHandler that parses a commit reference from a command line and checks that it can be resolved in the
 * git repository of the logical command.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class RefOptionHandler extends OptionHandler<ObjectId> {

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
	public RefOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super ObjectId> setter) {
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
		String ref = params.getParameter(0);

		ObjectId objectID;
		try {
			objectID = ((CmdLineParserRepositoryBuilder)owner).getRepo().resolve(ref);
			setter.addValue(objectID);
		} catch (Exception e) {
			throw new ArgumentValidationError(owner, e);
		}
		if (objectID == null) {
			throw new ArgumentValidationError(owner, ref + " - not a valid git reference.");
		}
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
	 */
	@Override
	public String getDefaultMetaVariable() {
		return "commit"; //$NON-NLS-1$
	}

}

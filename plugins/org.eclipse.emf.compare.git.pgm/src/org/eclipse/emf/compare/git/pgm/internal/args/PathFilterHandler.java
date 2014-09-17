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

import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Consumes all arguments left and handles them as they were path.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class PathFilterHandler extends OptionHandler<PathFilter> {

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
	public PathFilterHandler(CmdLineParser parser, OptionDef option, Setter<? super PathFilter> setter) {
		super(parser, option, setter);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.kohsuke.args4j.spi.OptionHandler#parseArguments(org.kohsuke.args4j.spi.Parameters)
	 */
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String path = params.getParameter(0);
		setter.addValue(PathFilter.create(path));
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
	 */
	@Override
	public String getDefaultMetaVariable() {
		return "<path...>";
	}

}

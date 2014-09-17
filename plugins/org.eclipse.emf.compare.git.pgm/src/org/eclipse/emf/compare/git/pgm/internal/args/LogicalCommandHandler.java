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

import org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand;
import org.eclipse.emf.compare.git.pgm.internal.cmd.CommandFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link OptionHandler} used to parse logical commands.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class LogicalCommandHandler extends OptionHandler<AbstractLogicalCommand> {

	/**
	 * Meta var used to display the print usage message of the command.
	 */
	private static final String CMD_META_VAR = "cmd"; //$NON-NLS-1$

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
	public LogicalCommandHandler(CmdLineParser parser, OptionDef option,
			Setter<? super AbstractLogicalCommand> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(final Parameters params) throws CmdLineException {

		final String name = params.getParameter(0);
		// Do not parse other argument since they will be parsed by subcommands.
		owner.stopOptionParsing();
		AbstractLogicalCommand cmd = CommandFactory.getInstance().createCommand(name);
		if (cmd != null) {
			setter.addValue(cmd);
		} else {
			throw new CmdLineException(owner, "Not a logical command " + name);
		}
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return CMD_META_VAR;
	}

}

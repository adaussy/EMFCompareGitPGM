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
package org.eclipse.emf.compare.git.pgm.internal.cmd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Commands factory.
 * <p>
 * Here is where the logical command should be built.
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public final class CommandFactory {
	/**
	 * Registry of commands.
	 */
	private final Map<String, Class<? extends AbstractLogicalCommand>> cmds;

	/**
	 * Constructor.
	 */
	private CommandFactory() {
		cmds = new HashMap<String, Class<? extends AbstractLogicalCommand>>(3);
		cmds.put(LogicalMergeCommand.LOGICAL_MERGE_CMD_NAME, LogicalMergeCommand.class);
		cmds.put(LogicalMergeToolCommand.LOGICAL_MERGE_TOOL_CMD_NAME, LogicalMergeToolCommand.class);
		cmds.put(LogicalDiffCommand.LOGICAL_DIFF_CMD_NAME, LogicalDiffCommand.class);
	}

	/**
	 * Singleton pattern.
	 * 
	 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
	 */
	private static final class SingletonHolder {
		/**
		 * Unique instance.
		 */
		private static final CommandFactory INSTANCE = new CommandFactory();
	}

	public static CommandFactory getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public Collection<String> getAvailableCmd() {
		return cmds.keySet();
	}

	/**
	 * Creates a command using its name.
	 * 
	 * @param cmdName
	 *            Name of the command.
	 * @return the newly created command or <code>null</code> if any error or if the command does not exist.
	 */
	public AbstractLogicalCommand createCommand(String cmdName) {
		AbstractLogicalCommand cmdIntance = null;
		if (cmdName != null) {
			Class<? extends AbstractLogicalCommand> cmdClass = cmds.get(cmdName);
			if (cmdClass != null) {
				try {
					cmdIntance = cmdClass.newInstance();
					cmdIntance.setCommandName(cmdName);
				} catch (InstantiationException e) {
					// If error return null
				} catch (IllegalAccessException e) {
					// If error return null
				}
			}
		}
		return cmdIntance;
	}

}

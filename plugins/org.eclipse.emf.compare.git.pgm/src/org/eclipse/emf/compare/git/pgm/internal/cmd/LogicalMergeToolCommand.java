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

import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.SEP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DeathType;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DiesOn;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.oomph.setup.util.OS;

/**
 * Logical merge tool command. <h3>Name</h3>
 * <p>
 * logicalmergetool - Git Logical Merge Tool
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicalmergetool [--show-stack-trace] &lt;setup&gt;
 * </p>
 * <h4>Description</h4>
 * <p>
 * The logical merge tool command is used to open a merge tool using logical model if the current repository
 * is in conflict state.
 * </p>
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("restriction")
public class LogicalMergeToolCommand extends AbstractLogicalCommand {

	/**
	 * Command name.
	 */
	static final String LOGICAL_MERGE_TOOL_CMD_NAME = "logicalmergetool"; //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand#internalRun()
	 */
	@Override
	protected Integer internalRun() throws Die {

		// Checks that the repository is in conflict state
		if (getRepository().getRepositoryState() != RepositoryState.MERGING) {
			throw new DiesOn(DeathType.FATAL).displaying("No conflict to merge").ready();
		}

		OS os = getPerformer().getOS();

		if (!os.isCurrent()) {
			return Returns.ERROR.code();
		}

		try {
			out().println("Launching the installed product...");
		} catch (IOException e) {
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

		String eclipseDir = os.getEclipseDir();
		String eclipseExecutable = os.getEclipseExecutable();
		String eclipsePath = new File(getPerformer().getInstallationLocation(), eclipseDir + SEP
				+ eclipseExecutable).getAbsolutePath();

		List<String> command = new ArrayList<String>();
		command.add(eclipsePath);

		if (getPerformer().getWorkspaceLocation() != null) {
			command.add("-data"); //$NON-NLS-1$
			command.add(getPerformer().getWorkspaceLocation().toString());
		}

		command.add("-vmargs"); //$NON-NLS-1$
		command.add("-D" + PROP_SETUP_CONFIRM_SKIP + "=true"); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_OFFLINE_STARTUP + "=" + false); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_MIRRORS_STARTUP + "=" + true); //$NON-NLS-1$ //$NON-NLS-2$

		ProcessBuilder builder = new ProcessBuilder(command);
		Process process;
		try {
			process = builder.start();
		} catch (IOException e) {
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

		// output both stdout and stderr data from proc to stdout of this
		// process
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
		new Thread(errorGobbler).start();
		new Thread(outputGobbler).start();

		int returnValue;
		try {
			returnValue = process.waitFor();
		} catch (InterruptedException e) {
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

		return Returns.valueOf(returnValue).code();
	}
}

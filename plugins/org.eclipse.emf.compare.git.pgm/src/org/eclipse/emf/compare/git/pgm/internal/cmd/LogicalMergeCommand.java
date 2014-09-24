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

import static org.eclipse.emf.compare.git.pgm.internal.Options.SHOW_STACK_TRACE_OPT;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.args.RefOptionHandler;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DeathType;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DiesOn;
import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.oomph.setup.util.OS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Logical merge command. <h3>Name</h3>
 * <p>
 * logicalmerge - Git Logical Merge
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicalmerge [--show-stack-trace] [--git-dir &lt;gitDirectory&gt;] &lt;setup&gt; &lt;commit&gt;
 * </p>
 * <h4>Description</h4>
 * <p>
 * The logical merge command is used to merge logical models. Instead of merging each file one by one like git
 * would do, it uses a set of semantically interconnected files. It avoids semantical breakage of models.
 * </p>
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("restriction")
public class LogicalMergeCommand extends AbstractLogicalCommand {

	/**
	 * Command name.
	 */
	static final String LOGICAL_MERGE_CMD_NAME = "logicalmerge"; //$NON-NLS-1$

	/**
	 * Holds a ObjectId that need to be merged.
	 */
	@Argument(index = 1, required = true, metaVar = "<commit>", usage = "Commit ID or branch name to merge.", handler = RefOptionHandler.class)
	private ObjectId commit;

	/**
	 * Optional message used for the merge commit.
	 */
	@Option(name = "-m", metaVar = "message", required = false, usage = "Set the commit message to be used for the merge commit (in case one is created).")
	private String message;

	@Option(name = "--debug", usage = "Launched the provisonned eclipse in debug mode.", aliases = {"-d" })
	private boolean debug;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand#run()
	 */
	@Override
	protected Integer internalRun() throws Die {
		// Checks we are not already in a conflict state
		// Checks that the repository is in conflict state
		if (getRepository().getRepositoryState() == RepositoryState.MERGING) {
			StringBuilder message = new StringBuilder(
					"error: 'merge' is not possible because you have unmerged files.").append(EOL);
			message.append("hint: Use the logicalmergetool command to fix them up un the work tree").append(
					EOL);
			message.append("hint: and then use the 'git add/rm <file>' as").append(EOL);
			message.append("hint: appropriate to mark resolution").append(EOL);
			System.out.println(message);
			throw new DiesOn(DeathType.FATAL).displaying("Exiting because of an unresolved conflict.")
					.ready();
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

		String setupFileAbsolutePath = this.getSetupFile().getAbsolutePath();
		String setupFileBasePath = Paths.get(setupFileAbsolutePath).getParent().toString();

		String eclipseDir = os.getEclipseDir();
		String eclipseExecutable = os.getEclipseExecutable();
		File eclipseFile = EMFCompareGitPGMUtil
				.toFileWithAbsolutePath(setupFileBasePath, Paths.get(
						getPerformer().getInstallationLocation().getPath(), eclipseDir, eclipseExecutable)
						.toString());

		List<String> command = new ArrayList<String>();
		command.add(eclipseFile.toString());
		command.add("-nosplash"); //$NON-NLS-1$
		command.add("--launcher.suppressErrors"); //$NON-NLS-1$
		command.add("-application"); //$NON-NLS-1$
		command.add("emf.compare.git.logicalmerge"); //$NON-NLS-1$

		// Propagates the show stack trace option to the application.
		if (isShowStackTrace()) {
			command.add(SHOW_STACK_TRACE_OPT);
		}

		command.add(getRepository().getDirectory().getAbsolutePath());

		command.add(setupFileAbsolutePath);

		if (commit != null) {
			command.add(commit.name());
		} else {
			command.add("HEAD"); //$NON-NLS-1$
		}

		if (message != null) {
			command.add("-m"); //$NON-NLS-1$
			command.add(message);
		}

		if (getPerformer().getWorkspaceLocation() != null) {
			command.add("-data"); //$NON-NLS-1$
			command.add(getPerformer().getWorkspaceLocation().toString());
		}

		command.add("-vmargs"); //$NON-NLS-1$
		command.add("-D" + PROP_SETUP_CONFIRM_SKIP + "=true"); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_OFFLINE_STARTUP + "=" + false); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_MIRRORS_STARTUP + "=" + true); //$NON-NLS-1$ //$NON-NLS-2$
		if (debug) {
			command.add("-Xdebug"); //$NON-NLS-1$
			command.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8123"); //$NON-NLS-1$
		}

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

	// For testing purpose.
	String getMessage() {
		return message;
	}

	// For testing purpose.
	ObjectId getCommit() {
		return commit;
	}
}

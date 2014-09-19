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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.args.PathFilterHandler;
import org.eclipse.emf.compare.git.pgm.internal.args.RefOptionHandler;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DeathType;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DiesOn;
import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.oomph.setup.util.OS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Logical diff command. <h3>Name</h3>
 * <p>
 * logicaldiff - Git Logical Diff
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicaldiff &lt;setup&gt; [--show-stack-trace] [--git-dir &lt;gitDirectory&gt;] &lt;commit&gt;
 * [&lt;compareWithCommit&gt;] [ -- &lt;paths...&gt;]
 * </p>
 * <h4>Description</h4>
 * <p>
 * The logical diff is used to display differences using logical model.
 * </p>
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("restriction")
public class LogicalDiffCommand extends AbstractLogicalCommand {

	/**
	 * Command name.
	 */
	static final String LOGICAL_DIFF_CMD_NAME = "logicaldiff"; //$NON-NLS-1$

	/**
	 * Holds the reference from which the differences should be displayed.
	 */
	@Argument(index = 1, multiValued = false, required = true, metaVar = "<commit>", usage = "Commit ID or branch name.", handler = RefOptionHandler.class)
	private ObjectId commit;

	/**
	 * Optional reference used to view the differences between {@link #commit} and {@link #commitWith}.
	 */
	@Argument(index = 2, multiValued = false, required = false, metaVar = "<compareWithCommit>", usage = "Commit ID or branch name. This is to view the changes between <commit> and <compareWithCommit> or HEAD if not specified.", handler = RefOptionHandler.class)
	private ObjectId commitWith;

	/**
	 * {@link TreeFilter} use to filter file on which differences should be shown.
	 */
	@Option(name = "--", metaVar = "<path...>", multiValued = false, handler = PathFilterHandler.class, usage = "This is used to limit the diff to the named paths (you can give directory names and get diff for all files under them).")
	private PathFilter treeFilter;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @see org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand#internalRun()
	 */
	@Override
	protected Integer internalRun() throws Die {

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
		command.add("emf.compare.git.logicaldiff"); //$NON-NLS-1$

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
		if (commitWith != null) {
			command.add(commitWith.name());
		} else {
			command.add("HEAD"); //$NON-NLS-1$
		}
		if (treeFilter != null) {
			command.add("--"); //$NON-NLS-1$
			command.add(treeFilter.getPath());
		}

		if (getPerformer().getWorkspaceLocation() != null) {
			command.add("-data"); //$NON-NLS-1$
			command.add(getPerformer().getWorkspaceLocation().toString());
		}

		command.add("-vmargs"); //$NON-NLS-1$
		command.add("-D" + PROP_SETUP_CONFIRM_SKIP + "=true"); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_OFFLINE_STARTUP + "=" + false); //$NON-NLS-1$ //$NON-NLS-2$
		command.add("-D" + PROP_SETUP_MIRRORS_STARTUP + "=" + true); //$NON-NLS-1$ //$NON-NLS-2$
		//command.add("-Xdebug"); //$NON-NLS-1$
		//command.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8123"); //$NON-NLS-1$

		ProcessBuilder builder = new ProcessBuilder(command);

		Process process;
		try {
			process = builder.start();
		} catch (IOException e) {
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

		// output both stdout and stderr data from proc to stdout of this
		// process
		// StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
		// new Thread(errorGobbler).start();
		new Thread(outputGobbler).start();

		int returnValue;
		try {
			returnValue = process.waitFor();
		} catch (InterruptedException e) {
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

		return Returns.valueOf(returnValue).code();
	}

	// For testing purpose
	ObjectId getCommit() {
		return commit;
	}

	// For testing purpose
	ObjectId getOptionalCommit() {
		return commitWith;
	}

	// For testing purpose
	TreeFilter getPathFilter() {
		return treeFilter;
	}

}

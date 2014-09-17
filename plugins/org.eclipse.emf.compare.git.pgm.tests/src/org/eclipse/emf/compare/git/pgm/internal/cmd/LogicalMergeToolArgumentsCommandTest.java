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

import static org.eclipse.emf.compare.git.pgm.internal.cmd.LogicalMergeToolCommand.LOGICAL_MERGE_TOOL_CMD_NAME;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.junit.Test;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalMergeToolArgumentsCommandTest extends AbstractLogicalCommandTest {

	@Override
	protected String getCommandName() {
		return LOGICAL_MERGE_TOOL_CMD_NAME;
	}

	@Override
	protected String getExpectedUsage() {
		//@formatter:off
		return EOL //
				+ "logicalmergetool <setup> [--git-dir gitFolderPath] [--help (-h)] [--show-stack-trace]" + EOL 
				+ EOL 
				+ " <setup>                 : Path to the setup file. The setup file is a Oomph" + EOL 
				+ "                           model." + EOL 
				+ " --git-dir gitFolderPath : Path to the .git folder of your repository." + EOL 
				+ " --help (-h)             : Dispays help for this command." + EOL 
				+ " --show-stack-trace      : Use this option to display java stack trace in" + EOL 
				+ "                           console on error."+ EOL
				+ EOL ; //
		//@formatter:on
	}

	@Test
	public void tooManyArgTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(oomphFolderPath.toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// Tests referencing a commit using the name of a branch
		getContext().addArg(getCommandName(), newSetupFile.getAbsolutePath(), "extraArg");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: Too many arguments: extraArg in:" + EOL//
				+ getExpectedUsage() //
				+ EOL; //
		assertOutput(expectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void isNotAGitRepoTest() throws Exception {
		Path myTmpDir = Files.createTempDirectory(getTestTmpFolder(), "NotARepo", new FileAttribute<?>[] {});
		// Launches command from directory that is not contained by a git repository
		setCmdLocation(myTmpDir.toString());

		File setupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		getContext().addArg(getCommandName(), setupFile.getAbsolutePath());

		Object result = getApp().start(getContext());
		assertOutput("fatal: Can't find git repository" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

}

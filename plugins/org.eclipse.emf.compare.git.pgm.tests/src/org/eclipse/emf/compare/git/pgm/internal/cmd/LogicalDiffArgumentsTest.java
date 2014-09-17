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
 * Test of {@link LogicalDiffCommand}.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalDiffArgumentsTest extends AbstractLogicalCommandTest {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommandTest#getCommandName()
	 */
	@Override
	protected String getCommandName() {
		return LogicalDiffCommand.LOGICAL_DIFF_CMD_NAME;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommandTest#getExpectedUsage()
	 */
	@Override
	protected String getExpectedUsage() {
		//@formatter:off
		return EOL
				+ "logicaldiff <setup> <commit> [<compareWithCommit>] [-- <path...>] [--git-dir gitFolderPath] [--help (-h)] [--show-stack-trace]" + EOL 
				+ EOL 
				+ " <setup>                 : Path to the setup file. The setup file is a Oomph" + EOL 
				+ "                           model." + EOL 
				+ " <commit>                : Commit ID or branch name." + EOL 
				+ " <compareWithCommit>     : Commit ID or branch name. This is to view the" + EOL 
				+ "                           changes between <commit> and <compareWithCommit> or" + EOL 
				+ "                           HEAD if not specified." + EOL 
				+ " -- <path...>            : This is used to limit the diff to the named paths" + EOL 
				+ "                           (you can give directory names and get diff for all" + EOL 
				+ "                           files under them)." + EOL 
				+ " --git-dir gitFolderPath : Path to the .git folder of your repository." + EOL 
				+ " --help (-h)             : Dispays help for this command." + EOL 
				+ " --show-stack-trace      : Use this option to display java stack trace in" + EOL 
				+ "                           console on error." + EOL 
				+ EOL ; 
		//@formatter:on
	}

	@Test
	public void emptyRepoTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		File userSetupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Tests the command on an empty repo (not commit, no branch)
		getContext().addArg(getCommandName(), userSetupFile.getAbsolutePath(), "master");
		Object result = getApp().start(getContext());
		assertOutput("fatal: master - not a valid git reference." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void noCommitIDTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		File userSetupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// No reference
		getContext().addArg(getCommandName(), userSetupFile.getAbsolutePath());
		Object result = getApp().start(getContext());
		String expectedMessage = "fatal: Argument \"<commit>\" is required in:" + EOL //
				+ getExpectedUsage() + EOL;
		assertOutput(expectedMessage);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void incorrectIDTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		File setupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Gives an incorrect ref
		getContext().addArg(getCommandName(), setupFile.getAbsolutePath(), "incorrectId");
		Object result = getApp().start(getContext());
		assertOutput("fatal: incorrectId - not a valid git reference." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void incorrectCompareWithCommitIDTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		File setupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());
		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// Gives an incorrect ref
		getContext().addArg(getCommandName(), setupFile.getAbsolutePath(), "master", "incorrectId");
		Object result = getApp().start(getContext());
		assertOutput("fatal: incorrectId - not a valid git reference." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void tooManyArgsTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		File setupFile = new OomphUserModelBuilder()//
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// Tests referencing a commit using the name of a branch
		getContext().addArg(getCommandName(), setupFile.getAbsolutePath(), "master", "master", "extraArg");
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

		getContext().addArg(getCommandName(), setupFile.getAbsolutePath(), "master");

		Object result = getApp().start(getContext());
		assertOutput("fatal: Can't find git repository" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	// TODO
	// @Test
	// public void correctPathTest() throws Exception {
	// setCmdLocation(repositoryPath.toString());
	// File newSetupFile = createOomphModel("setup.setup");
	// CommittedFile aFile = createAndCommitAFile("aFile.txt", "Some content", "First contribution");
	// CommittedFile newFile = createAndCommitAFile("test.txt", "Some content", "New commit");
	// // Tests referencing a commit using the name of a branch
	// context.addArg(getCommandName(), newSetupFile.getAbsolutePath(), "master", "--", aFile.getFile()
	// .getAbsolutePath(), newFile.getFile().getAbsolutePath());
	// Object result = getApp().start(context);
	// assertOutputs("", "");
	// assertEquals(Returns.COMPLETE, result);
	// assertTrue(getLogicalCommand() instanceof LogicalDiffCommand);
	// LogicalDiffCommand diffCmd = (LogicalDiffCommand)getLogicalCommand();
	// assertNotNull(diffCmd.getPathFilter());
	// assertNotNull(diffCmd.getCommit());
	// assertNull(diffCmd.getOptionalCommit());
	// }

}

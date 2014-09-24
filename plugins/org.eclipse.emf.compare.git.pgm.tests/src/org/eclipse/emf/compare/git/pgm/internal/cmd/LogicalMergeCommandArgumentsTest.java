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

import static org.eclipse.emf.compare.git.pgm.internal.cmd.LogicalMergeCommand.LOGICAL_MERGE_CMD_NAME;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.junit.Test;

/**
 * Test of {@link LogicalMergeCommand}.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalMergeCommandArgumentsTest extends AbstractLogicalCommandTest {

	@Override
	protected String getCommandName() {
		return LOGICAL_MERGE_CMD_NAME;
	}

	@Override
	protected String getExpectedUsage() {
		//@formatter:off
		return EOL //
				+ "logicalmerge <setup> <commit> [--debug (-d)] [--git-dir gitFolderPath] [--help (-h)] [--show-stack-trace] [-m message]" + EOL 
				+ EOL 
				+ " <setup>                 : Path to the setup file. The setup file is a Oomph" + EOL 
				+ "                           model." +EOL
				+ " <commit>                : Commit ID or branch name to merge." + EOL 
				+ " --debug (-d)            : Launched the provisonned eclipse in debug mode."+ EOL
				+ " --git-dir gitFolderPath : Path to the .git folder of your repository."+ EOL
				+ " --help (-h)             : Dispays help for this command." + EOL 
				+ " --show-stack-trace      : Use this option to display java stack trace in" + EOL
				+ "                           console on error." + EOL
				+ " -m message              : Set the commit message to be used for the merge" + EOL 
				+ "                           commit (in case one is created)." + EOL 
				+ EOL; 
		//@formatter:on
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

	@Test
	public void emptyRepoTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		// Creates a Oomph setup file
		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File setupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(oomphFolderPath.toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Tests commant on an empty repo (not commit, no branch)
		getContext().addArg(getCommandName(), setupFile.getAbsolutePath(), "master");
		Object result = getApp().start(getContext());
		assertOutput("fatal: master - not a valid git reference." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void corruptSetupModelTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		// Creates some content for the first commit.
		File project = new ProjectBuilder(this) //
				.addNewFileContent("aFile.txt", "Some content") //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		Path corruptedSetupFilePath = project.toPath().resolve("aFile.txt");
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, corruptedSetupFilePath.toString(), "master");
		Object result = getApp().start(getContext());
		assertOutput("fatal: " + corruptedSetupFilePath.toString() + " is not a valid setup file" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void validRepoTestIncorrectGitDirArg() throws Exception {
		// Sets context to be sure were are not in a git repository
		setCmdLocation(getTestTmpFolder().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(oomphFolderPath.toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// Provides the repository using parameter
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, "--git-dir", "x/x/x/incorrectPathToGitDir",
				newSetupFile.getAbsolutePath(), "master");

		Object result = getApp().start(getContext());
		assertOutput("fatal: Can't find git repository" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void validRepoNoCommitIDTest() throws Exception {
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

		// No reference
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, newSetupFile.getAbsolutePath());
		Object result = getApp().start(getContext());
		//@formatter:off
		String expectedMessage = "fatal: Argument \"<commit>\" is required in:" + EOL 
				+ getExpectedUsage()
				+ EOL;
		//@formatter:on
		assertOutput(expectedMessage);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void validRepoIncorrectIDTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(oomphFolderPath.toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		// Gives an incorrect ref
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, newSetupFile.getAbsolutePath(), "incorrectId");
		Object result = getApp().start(getContext());
		assertOutput("fatal: incorrectId - not a valid git reference." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void tooManyArgsTest() throws Exception {
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
		getContext().addArg(getCommandName(), newSetupFile.getAbsolutePath(), "master", "extraArg");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: Too many arguments: extraArg in:" + EOL//
				+ getExpectedUsage() //
				+ EOL; //
		assertOutput(expectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void incorrectMessageTest() throws Exception {
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
		getContext().addArg(getCommandName(), newSetupFile.getAbsolutePath(), "master", "-m");
		getApp().start(getContext());
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
		String extectedOutput = "fatal: Option \"-m\" takes an operand in:" + EOL + getExpectedUsage() + EOL;
		assertOutput(extectedOutput);
		assertEmptyErrorMessage();
	}

	@Test
	public void messageTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder().setInstallationTaskLocation(
				oomphFolderPath.toString()).setWorkspaceLocation(oomphFolderPath.resolve("ws").toString())
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));
		addAllAndCommit("First commit");

		// Tests referencing a commit using the name of a branch
		getContext().addArg(getCommandName(), newSetupFile.getAbsolutePath(), "master", "-m", "My message",
				"--help");
		getApp().start(getContext());
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
		assertEquals("My message", ((LogicalMergeCommand)getLogicalCommand()).getMessage());

	}
}

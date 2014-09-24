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
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.git.pgm.AbstractLogicalAppTest;
import org.eclipse.emf.compare.git.pgm.LogicalApp;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.Options;
import org.eclipse.emf.compare.git.pgm.suite.AllIntegrationTests;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

/**
 * Should only be called from the tycho build since it used the emfcompare-git-pgm update to create the
 * provided platform.
 * <p>
 * If you need to run it locally please set the system variable "emfcompare-git-pgm--updasite" to the location
 * of update holding emfcompare-git-pgm plugins.
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalMergeCommandIntegrationTest extends AbstractLogicalAppTest {

	@Override
	protected IApplication buildApp() {
		return new LogicalApp(URI.createURI(
				"platform:/fragment/org.eclipse.emf.compare.git.pgm.tests/model/lunaIntegrationTest.setup",
				false));
	}

	@Test
	public void alreadyUpToDateTest() throws Exception {
		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		setCmdLocation(getRepositoryPath().toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));
		addAllAndCommit("First commit");

		// Tests referencing a commit using the name of a branch
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, newSetupFile.getAbsolutePath(), "master");
		Object result = getApp().start(getContext());

		printOut();
		printErr();

		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
	}

	@Test
	public void alreadyUpToDateWithCommitIdTest() throws Exception {

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File setupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		setCmdLocation(getRepositoryPath().toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));
		RevCommit rev = addAllAndCommit("First commit");

		// Tests referencing a commit using its id.
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, setupFile.getAbsolutePath(), rev.getId().name());
		Object result = getApp().start(getContext());
		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
	}

	@Test
	public void alreadyUpToDateWithGitDirTest() throws Exception {
		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));
		addAllAndCommit("First commit");

		// Sets context to be sure were are not in a git repository
		setCmdLocation(getTestTmpFolder().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File setupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Provides the repository using parameter
		getContext().addArg(LOGICAL_MERGE_CMD_NAME, Options.GIT_DIR_OPT, getGitFolderPath().toString(),
				setupFile.getAbsolutePath(), "master");
		Object result = getApp().start(getContext());

		printOut();
		printErr();

		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
	}

	@Test
	public void alreadyUpToDateOnRepoSubFolderTest() throws Exception {
		// Creates some content for the first commit.
		File project = new ProjectBuilder(this) //
				.addNewFileContent("newFoler/newFile.txt", "some content") //
				.create(getRepositoryPath().resolve("EmptyProject"));
		addAllAndCommit("First commit");
		// Launches command from subfolder of the git repository
		setCmdLocation(project.toPath().resolve("newFolder").toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File setupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		getContext().addArg(LOGICAL_MERGE_CMD_NAME, setupFile.getAbsolutePath(), "master");
		Object result = getApp().start(getContext());

		printOut();
		printErr();

		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
	}

	@Test
	public void alreadyUpToDateWithMessageTest() throws Exception {
		// Creates some content for the first commit.
		File project = new ProjectBuilder(this) //
				.addNewFileContent("newFoler/newFile.txt", "some content") //
				.create(getRepositoryPath().resolve("EmptyProject"));
		addAllAndCommit("First commit");
		// Launches command from subfolder of the git repository
		setCmdLocation(project.toPath().resolve("newFolder").toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File setupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		getContext()
				.addArg(LOGICAL_MERGE_CMD_NAME, setupFile.getAbsolutePath(), "master", "-m", "My message");
		Object result = getApp().start(getContext());

		printOut();
		printErr();

		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
		assertTrue(getLogicalCommand() instanceof LogicalMergeCommand);
		LogicalMergeCommand mergeCmd = (LogicalMergeCommand)getLogicalCommand();
		assertNotNull(mergeCmd.getCommit());
		assertEquals("My message", mergeCmd.getMessage());

	}
}

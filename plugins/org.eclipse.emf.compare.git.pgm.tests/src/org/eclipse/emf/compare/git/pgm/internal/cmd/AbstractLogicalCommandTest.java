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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.emf.compare.git.pgm.AbstractLogicalAppTest;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;

/**
 * Abstract class for logical command tests.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public abstract class AbstractLogicalCommandTest extends AbstractLogicalAppTest {

	/**
	 * @return the name of the command under test.
	 */
	protected abstract String getCommandName();

	/**
	 * @return the expected usage message.
	 */
	protected abstract String getExpectedUsage();

	@Test
	public void helpLogicalCommandTest() throws Exception {
		// Asks command help
		getContext().addArg(getCommandName(), "--help");
		Object result = getApp().start(getContext());
		assertOutput(getExpectedUsage());
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
	}

	@Test
	public void incorrectSetupFileTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());
		// Gives an incorrect path for the setup file
		String incorrectSetupFilePath = getTestTmpFolder().resolve("wrongfile.setup").toString();
		getContext().addArg(getCommandName(), incorrectSetupFilePath, "master");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: " + incorrectSetupFilePath + " setup file does not exist" + EOL;
		assertOutput(expectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void missingSetupFileTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());
		// Does not give a setup file
		String missingSetupFilePath = getRepositoryPath().resolve("master").toString();
		getContext().addArg(getCommandName(), "master");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: " + missingSetupFilePath + " setup file does not exist" + EOL; //
		assertOutput(expectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void useOfGitDir() throws Exception {
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

		// Provides the repository using the --git-dir parameter
		// Use the --help option to avoid crashing the application and being able to test its parameters
		String gitDirPath = getRepositoryPath().resolve(".git").toString();
		getContext().addArg(getCommandName(), "--git-dir", gitDirPath, newSetupFile.getAbsolutePath(),
				"--help");

		Object result = getApp().start(getContext());
		Repository repo = getLogicalCommand().getRepository();
		assertNotNull(repo);
		assertEquals(gitDirPath, repo.getDirectory().getAbsolutePath());
		assertEquals(Returns.COMPLETE.code(), result);
	}

	@Test
	public void useOfShowStackTrace() throws Exception {
		// Sets context to be sure were are not in a git repository
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

		// Set the show stack trace option
		// Use the --help option to avoid crashing the application and being able to test its parameters
		getContext().addArg(getCommandName(), "--show-stack-trace", newSetupFile.getAbsolutePath(), "--help");

		Object result = getApp().start(getContext());
		assertTrue(getLogicalCommand().isShowStackTrace());
		assertEquals(Returns.COMPLETE.code(), result);
	}
}

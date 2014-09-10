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
package org.eclipse.emf.compare.git.pgm.app.internal.cmd;

import static org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.eclipse.emf.compare.git.pgm.app.AbstractLogicalAppTest;
import org.eclipse.emf.compare.git.pgm.app.ReturnCode;
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
		getContext().addArg("--show-stack-trace", getCommandName(), "--help");
		Object result = getApp().start(getContext());
		assertOutputs(getExpectedUsage(), "");
		assertEquals(ReturnCode.COMPLETE, result);
	}

	@Test
	public void isNotAGitRepoTest() throws Exception {
		Path myTmpDir = Files.createTempDirectory(getTestTmpFolder(), "NotARepo", new FileAttribute<?>[] {});
		getContext().addArg(getCommandName());
		// Launches command from directory that is not contained by a git repository
		setCmdLocation(myTmpDir.toString());
		Object result = getApp().start(getContext());
		assertOutputs("fatal: Can't find git repository" + EOL, "");
		assertEquals(ReturnCode.ERROR, result);
	}

	@Test
	public void incorrectSetupFileTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());
		// Gives an incorrect path for the setup file
		String incorrectSetupFilePath = getTestTmpFolder().resolve("wrongfile.setup").toString();
		getContext().addArg(getCommandName(), incorrectSetupFilePath, "master");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: " + incorrectSetupFilePath + " setup file does not exist" + EOL;
		assertOutputs(expectedOut, "");
		assertEquals(ReturnCode.ERROR, result);
	}

	@Test
	public void missingSetupFilTest() throws Exception {
		setCmdLocation(getRepositoryPath().toString());
		// Does not give a setup file
		getContext().addArg(getCommandName(), "master");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: master setup file does not exist" + EOL; //
		assertOutputs(expectedOut, "");
		assertEquals(ReturnCode.ERROR, result);
	}

}

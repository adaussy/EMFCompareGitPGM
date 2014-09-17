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

import org.eclipse.emf.compare.git.pgm.AbstractLogicalAppTest;
import org.eclipse.emf.compare.git.pgm.Returns;
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
		getContext().addArg(getCommandName(), "--show-stack-trace", "--help");
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
		String missingSetupFilePath = getTestTmpFolder().resolve("master").toString();
		getContext().addArg(getCommandName(), "master");
		Object result = getApp().start(getContext());
		String expectedOut = "fatal: " + missingSetupFilePath + " setup file does not exist" + EOL; //
		assertOutput(expectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

}

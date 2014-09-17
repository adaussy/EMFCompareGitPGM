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
package org.eclipse.emf.compare.git.pgm;

import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for the main application.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalAppTest extends AbstractLogicalAppTest {

	private String getExpectedAvailableCommandUsage() {
		//@formatter:off
		return EOL 
				+ "Available commands are:" + EOL
				+ "logicaldiff" + EOL
				+ "logicalmerge" + EOL
				+ "logicalmergetool" + EOL;
		//@formatter:on
	}

	private String getExpectedUsage() {
		//@formatter:off
		return "logicalApp --help (-h) command [ARG ...]" + EOL
				+ EOL //
				+ " --help (-h) : Displays help for this command." + EOL;
		//@formatter:on
	}

	@Test
	public void helpTest() throws Exception {
		getContext().addArg("--help");
		Object result = getApp().start(getContext());
		assertEquals(Returns.COMPLETE, result);
		String expectMessage = getExpectedUsage() + getExpectedAvailableCommandUsage(); //
		assertOutput(expectMessage);
		assertEmptyErrorMessage();
	}

	@Test
	public void noArgumentTest() throws Exception {
		Object result = getApp().start(getContext());
		String extectedOut = "fatal: logicalApp --help (-h) command [ARG ...]" + EOL//
				+ getExpectedAvailableCommandUsage() //
				+ EOL;
		assertOutput(extectedOut);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);

	}

	@Test
	public void wrongOptTest() throws Exception {
		getContext().addArg("-c");
		Object result = getApp().start(getContext());
		assertOutput("fatal: \"-c\" is not a valid option" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

	@Test
	public void wrongCmdTest() throws Exception {
		getContext().addArg("wrongCmd");
		Object result = getApp().start(getContext());
		assertOutput("fatal: Not a logical command wrongCmd" + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.ERROR.code(), result);
	}

}

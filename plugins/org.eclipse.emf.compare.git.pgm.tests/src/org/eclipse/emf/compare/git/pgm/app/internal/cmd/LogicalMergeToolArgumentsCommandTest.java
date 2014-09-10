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

import static org.eclipse.emf.compare.git.pgm.app.internal.cmd.LogicalMergeToolCommand.LOGICAL_MERGE_TOOL_CMD_NAME;
import static org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.emf.compare.git.pgm.app.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.app.ReturnCode;
import org.eclipse.emf.compare.git.pgm.app.util.ProjectBuilder;
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
				+ "logicalmergetool <setup> [--help (-h)] [--show-stack-trace] [--silent-oomph (-so)]" + EOL //
				+ EOL //
				+ " <setup>              : Path to the setup file. The setup file is a Oomph model." + EOL //
				+ " --help (-h)          : Dispays help for this command." + EOL //
				+ " --show-stack-trace   : Use this option to display java stack trace in console" + EOL
				+ "                        on error." + EOL
				+ " --silent-oomph (-so) : Use this to hide the log from Oomph. In this case the" + EOL
				+ "                        log from Oomph will be located in a file in the eclipse" + EOL
				+ "                        instalation folder." + EOL
				+ EOL; //
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
		assertOutputs(expectedOut, "");
		assertEquals(ReturnCode.ERROR, result);
	}

}

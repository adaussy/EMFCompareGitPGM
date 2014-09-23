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
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.git.pgm.AbstractLogicalAppTest;
import org.eclipse.emf.compare.git.pgm.LogicalApp;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.suite.AllIntegrationTests;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.eclipse.equinox.app.IApplication;
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
public class LogicalDiffIntegrationTest extends AbstractLogicalAppTest {

	@Override
	protected IApplication buildApp() {
		return new LogicalApp(URI.createURI(
				"platform:/fragment/org.eclipse.emf.compare.git.pgm.tests/model/lunaIntegrationTest.setup",
				false));
	}

	@Test
	public void nothingToDo() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(AllIntegrationTests.getProvidedPlatformLocation().toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// No reference
		getContext().addArg(LogicalDiffCommand.LOGICAL_DIFF_CMD_NAME, newSetupFile.getAbsolutePath(),
				"master", "master");
		Object result = getApp().start(getContext());

		printOut();
		printErr();

		assertOutputMessageEnd("No difference to display." + EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
	}
}

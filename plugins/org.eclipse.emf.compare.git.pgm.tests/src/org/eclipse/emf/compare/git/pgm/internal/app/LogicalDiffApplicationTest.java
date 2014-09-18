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
package org.eclipse.emf.compare.git.pgm.internal.app;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.emf.compare.git.pgm.AbstractApplicationTest;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.emf.compare.git.pgm.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.util.ProjectBuilder;
import org.eclipse.equinox.app.IApplication;
import org.junit.Test;

/**
 * Tests the logical diff application.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class LogicalDiffApplicationTest extends AbstractApplicationTest {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.AbstractApplicationTest#buildApp()
	 */
	@Override
	protected IApplication buildApp() {
		return new LogicalDiffApplication();
	}

	/**
	 * Test if there is no difference to display then the software display the "No differende to display"
	 * message.
	 * 
	 * @throws Exception
	 */
	@Test
	public void nothingToDo() throws Exception {
		setCmdLocation(getRepositoryPath().toString());

		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File newSetupFile = new OomphUserModelBuilder() //
				.setInstallationTaskLocation(getTestTmpFolder().resolve("oomphFolder").toString()) //
				.setWorkspaceLocation(oomphFolderPath.resolve("ws").toString()) //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());

		// Creates some content for the first commit.
		new ProjectBuilder(this) //
				.create(getRepositoryPath().resolve("EmptyProject"));

		addAllAndCommit("First commit");

		// No reference
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), newSetupFile.getAbsolutePath(),
				"master", "master");
		Object result = getApp().start(getContext());
		assertOutputMessageEnd("No difference to display." + EMFCompareGitPGMUtil.EOL);
		assertEmptyErrorMessage();
		assertEquals(Returns.COMPLETE.code(), result);
	}

}

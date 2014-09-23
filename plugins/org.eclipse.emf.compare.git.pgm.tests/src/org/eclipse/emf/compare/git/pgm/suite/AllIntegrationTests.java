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
package org.eclipse.emf.compare.git.pgm.suite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.textui.TestRunner;

import org.eclipse.emf.compare.git.pgm.internal.cmd.LogicalDiffIntegrationTest;
import org.eclipse.emf.compare.git.pgm.internal.cmd.LogicalMergeCommandIntegrationTest;
import org.eclipse.emf.compare.git.pgm.internal.cmd.LogicalMergeToolIntegrationTest;
import org.eclipse.jgit.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
@RunWith(Suite.class)
@SuiteClasses({LogicalMergeCommandIntegrationTest.class, LogicalMergeToolIntegrationTest.class,
		LogicalDiffIntegrationTest.class })
public class AllIntegrationTests {

	/**
	 * System property to set to the emfcompare-git-pgm update site location.
	 */
	private static final String EMFCOMPARE_GIT_PGM_UPDASITE_SYS_PROP = "emfcompare-git-pgm--updasite"; //$NON-NLS-1$

	private static final String TMP_DIRECTORY_PREFIX = "emfcompare-git-pgm"; //$NON-NLS-1$

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		return new JUnit4TestAdapter(AllIntegrationTests.class);
	}

	private static Path providedEclipsePlatformPath;

	/**
	 * Creates a unique location where the provided platform will be located.
	 * 
	 * @throws IOException
	 */
	@BeforeClass
	public static void provideLocationForProvidedPlatform() throws IOException {
		providedEclipsePlatformPath = Files.createTempDirectory(TMP_DIRECTORY_PREFIX
				+ "_providedEclipsePlatform", new FileAttribute<?>[] {}); //$NON-NLS-1$
		String updateSiteLocation = System.getProperty(EMFCOMPARE_GIT_PGM_UPDASITE_SYS_PROP);
		if (updateSiteLocation == null) {
			throw new AssertionError("The variable " + EMFCOMPARE_GIT_PGM_UPDASITE_SYS_PROP
					+ " should be defined in the system properties in order to run this test suite.");
		}
	}

	@AfterClass
	public static void deleteProvidedPlatform() throws IOException {
		if (providedEclipsePlatformPath != null) {
			FileUtils.delete(providedEclipsePlatformPath.toFile(), FileUtils.RECURSIVE | FileUtils.RETRY);
		}
	}

	/**
	 * A unique location where test can share a provided platform.
	 * <p>
	 * All integration tests that do not specifically want to test the providing mechanism can use this shared
	 * location for their provided platform. Using this location will assure that the platform is provided
	 * only once.
	 * </p>
	 * 
	 * @return
	 */
	public static Path getProvidedPlatformLocation() {
		if (providedEclipsePlatformPath == null) {
			throw new AssertionError("The integration tests needs to be launched using this suite.");
		}
		return providedEclipsePlatformPath;
	}

}

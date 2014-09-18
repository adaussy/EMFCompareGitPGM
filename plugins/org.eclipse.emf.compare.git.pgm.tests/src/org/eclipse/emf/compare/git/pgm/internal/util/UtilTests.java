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
package org.eclipse.emf.compare.git.pgm.internal.util;

import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.CURRENT;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.PARENT;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.SEP;
import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.toFileWithAbsolutePath;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Util tests.
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
@SuppressWarnings("nls")
public class UtilTests {

	@Test
	public void testRelativePath() throws IOException {
		String systemTmpDir = System.getProperty("java.io.tmpdir");
		Path systemTmpDirPath = Paths.get(systemTmpDir);
		Path d = systemTmpDirPath.resolve("a").resolve("b").resolve("c").resolve("d");
		File file = toFileWithAbsolutePath(d.toString(), PARENT + SEP + PARENT + SEP + "c");
		assertEquals(systemTmpDirPath.resolve("a").resolve("b").resolve("c").toString(), file.toString());
	}

	@Test
	public void testRelativePath2() throws IOException {
		String systemTmpDir = System.getProperty("java.io.tmpdir");
		Path systemTmpDirPath = Paths.get(systemTmpDir);
		Path d = systemTmpDirPath.resolve("a").resolve("b").resolve("c").resolve("d");
		File file = toFileWithAbsolutePath(d.toString(), CURRENT + SEP + PARENT + SEP + "d");
		assertEquals(systemTmpDirPath.resolve("a").resolve("b").resolve("c").resolve("d").toString(), file
				.toString());
	}
}

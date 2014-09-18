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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.textui.TestRunner;

import org.eclipse.emf.compare.git.pgm.internal.app.LogicalDiffApplicationTest;
import org.eclipse.emf.compare.git.pgm.internal.app.LogicalMergeApplicationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@RunWith(Suite.class)
@SuiteClasses({LogicalMergeApplicationTest.class, LogicalDiffApplicationTest.class })
public class AllLogicalApplicationTests {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		return new JUnit4TestAdapter(AllLogicalApplicationTests.class);
	}

}

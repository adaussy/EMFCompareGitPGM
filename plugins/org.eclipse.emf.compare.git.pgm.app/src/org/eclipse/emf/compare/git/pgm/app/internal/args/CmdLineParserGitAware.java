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
package org.eclipse.emf.compare.git.pgm.app.internal.args;

import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.jgit.lib.Repository;
import org.kohsuke.args4j.CmdLineParser;

/**
 * CmdLineParser that is aware of a git repository. It's to pass it to
 * {@link org.kohsuke.args4j.spi.OptionHandler} that might need it for validation.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class CmdLineParserGitAware extends CmdLineParser {

	/**
	 * Git directory.
	 */
	private String gitDir = null;

	/**
	 * Git repository.
	 */
	private Repository repo = null;

	/**
	 * Constructor.
	 * 
	 * @param bean
	 *            {@link CmdLineParser#CmdLineParser(Object)}
	 * @param repo
	 *            Git repository for the curretn command.
	 */
	public CmdLineParserGitAware(Object bean, Repository repo) {
		super(bean);
		this.repo = repo;
	}

	public Repository getRepo() throws Die {
		if (repo == null) {
			repo = EMFCompareGitPGMUtil.buildRepository(gitDir);
		}
		return repo;
	}

	public void setGitDir(String gitDir) {
		this.gitDir = gitDir;
	}

}

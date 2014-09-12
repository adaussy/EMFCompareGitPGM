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

import com.google.common.base.Preconditions;

import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.jgit.lib.Repository;
import org.kohsuke.args4j.CmdLineParser;

/**
 * CmdLineParser that is aware of a git repository. Some {@link org.kohsuke.args4j.spi.OptionHandler}s might
 * need it for validation.
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

	public static CmdLineParserGitAware newGitRepoBuilderCmdParser(Object bean) {
		return new CmdLineParserGitAware(bean);
	}

	public static CmdLineParserGitAware newGitAwareCmdParser(Object bean, Repository repo) {
		return new CmdLineParserGitAware(bean, repo);
	}

	/**
	 * Constructor.
	 * <p>
	 * The git repository will be buit during argument parsing
	 * </p>
	 * 
	 * @param bean
	 */
	private CmdLineParserGitAware(Object bean) {
		super(bean);
	}

	/**
	 * Constructor.
	 * <p>
	 * The repository has already been built
	 * 
	 * @param bean
	 *            {@link CmdLineParser#CmdLineParser(Object)}
	 * @param repo
	 *            Git repository for the current command (can not be null)(if the repository is not .
	 */
	private CmdLineParserGitAware(Object bean, Repository repo) {
		super(bean);
		Preconditions.checkNotNull(repo);
		this.repo = repo;
	}

	public Repository getRepo() throws Die {
		if (repo == null) {
			repo = EMFCompareGitPGMUtil.buildRepository(gitDir);
		}
		return repo;
	}

	/**
	 * Set the path to the git dir repository.
	 * <p>
	 * This method does not need to be called if the command has been run inside the git repository. However
	 * if it's used this method needs to be called before the first call of the {@link #getRepo()} method.
	 * </p>
	 * 
	 * @param gitDir
	 */
	public void setGitDir(String gitDir) {
		// The gitDir argument should be provided before the repository is built
		Preconditions.checkArgument(repo == null);
		this.gitDir = gitDir;
	}

}

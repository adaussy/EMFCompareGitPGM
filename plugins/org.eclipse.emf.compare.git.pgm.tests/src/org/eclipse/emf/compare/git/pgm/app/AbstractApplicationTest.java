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
package org.eclipse.emf.compare.git.pgm.app;

import static org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.compare.git.pgm.app.mock.MockedApplicationContext;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public abstract class AbstractApplicationTest {
	private static final String TMP_DIRECTORY_PREFIX = "logicalAppTestFolder"; //$NON-NLS-1$

	private static final String REPO_PREFIX = "Repo_"; //$NON-NLS-1$

	private Path testTmpFolder;

	private IApplication app;

	private MockedApplicationContext context;

	private Path repositoryPath;

	private File gitFolderPath;

	private ByteArrayOutputStream outputStream;

	private ByteArrayOutputStream errStream;

	private Git git;

	private String uderDir;

	private PrintStream sysout;

	private PrintStream syserr;

	/**
	 * Internal data structure.
	 * 
	 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
	 */
	protected static class CommittedFile {
		private final File file;

		private final RevCommit rev;

		public CommittedFile(File file, RevCommit rev) {
			super();
			this.file = file;
			this.rev = rev;
		}

		public File getFile() {
			return file;
		}

		public RevCommit getRev() {
			return rev;
		}
	}

	@Before
	public void before() throws Exception {
		// Creates a local git repository for test purpose
		testTmpFolder = Files.createTempDirectory(TMP_DIRECTORY_PREFIX, new FileAttribute<?>[] {});
		outputStream = new ByteArrayOutputStream();
		errStream = new ByteArrayOutputStream();
		sysout = System.out;
		syserr = System.err;
		// Redirects out and err in order to test outputs.
		System.setOut(new PrintStream(outputStream));
		System.setErr(new PrintStream(errStream));
		// Use a specific environment for testing to be able to reference the update site of the application.
		app = buildApp();
		setContext(new MockedApplicationContext());

		setRepositoryPath(Files.createTempDirectory(testTmpFolder, REPO_PREFIX + "repo",
				new FileAttribute<?>[] {}));
		setGitFolderPath(new File(getRepositoryPath().toFile(), Constants.DOT_GIT));
		git = Git.init().setDirectory(getRepositoryPath().toFile()).call();
		// Saves the user.dire property to be able to restore it.( some tests can modify it)
		uderDir = System.getProperty("user.dir"); //$NON-NLS-1$

	}

	protected File getWorkspaceLocation() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		return root.getLocation().toFile();
	}

	protected abstract IApplication buildApp();

	@After
	public void tearDown() throws Exception {
		// repository.dispose();
		git.close();
		// Restores system properties
		setCmdLocation(uderDir);

		File tmpFolder = testTmpFolder.toFile();
		if (tmpFolder.exists()) {
			// FileUtils.delete(tmpFolder, FileUtils.RECURSIVE | FileUtils.RETRY);
		}

		System.setOut(sysout);
		outputStream.close();

		System.setErr(syserr);
		errStream.close();
	}

	protected void setCmdLocation(String path) {
		System.setProperty("user.dir", path); //$NON-NLS-1$
	}

	protected void assertOutputs(String outExpectedMessage, String errExpectedMessage) {
		assertEquals(errExpectedMessage, errStream.toString());
		assertEquals(outExpectedMessage, outputStream.toString());
	}

	protected void assertOutputMessageEnd(String expected) {
		String outputStreamContent = outputStream.toString();
		assertTrue("The output message should end with: " + EOL + expected + EOL + "but was: " + EOL
				+ outputStreamContent, outputStreamContent.endsWith(expected));
	}

	protected Path getTestTmpFolder() {
		return testTmpFolder;
	}

	protected IApplication getApp() {
		return app;
	}

	protected RevCommit addAllAndCommit(String commitMessage) throws GitAPIException, NoFilepatternException,
			NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException,
			WrongRepositoryStateException {
		git.add().addFilepattern(".").call();
		RevCommit revCommit = git.commit().setAuthor("Logical test author", "logicaltest@obeo.fr")
				.setCommitter("Logical test author", "logicaltest@obeo.fr").setMessage(commitMessage).call();
		return revCommit;
	}

	protected Ref createBranch(String branchName, String startingPoint) throws RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException, GitAPIException {
		return getGit().branchCreate().setName(branchName).setStartPoint(startingPoint).call();
	}

	protected Ref createBranchAndCheckout(String ref, String startingPoint) throws RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
		return getGit().checkout().setName(ref).setStartPoint(startingPoint).setCreateBranch(true).call();
	}

	protected Git getGit() {
		return git;
	}

	protected void printOut() {
		sysout.println(outputStream.toString());
	}

	protected void printErr() {
		syserr.println(errStream.toString());
	}

	public MockedApplicationContext getContext() {
		return context;
	}

	public void setContext(MockedApplicationContext context) {
		this.context = context;
	}

	public Path getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(Path repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public File getGitFolderPath() {
		return gitFolderPath;
	}

	public void setGitFolderPath(File gitFolderPath) {
		this.gitFolderPath = gitFolderPath;
	}

}

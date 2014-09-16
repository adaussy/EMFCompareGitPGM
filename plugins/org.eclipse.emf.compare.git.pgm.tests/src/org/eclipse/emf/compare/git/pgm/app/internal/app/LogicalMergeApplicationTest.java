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
package org.eclipse.emf.compare.git.pgm.app.internal.app;

import static org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.git.pgm.app.AbstractApplicationTest;
import org.eclipse.emf.compare.git.pgm.app.Returns;
import org.eclipse.emf.compare.git.pgm.app.util.OomphUserModelBuilder;
import org.eclipse.emf.compare.git.pgm.app.util.ProjectBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class LogicalMergeApplicationTest extends AbstractApplicationTest {

	/**
	 * <h3>Test the logical merge application on the current branch</h3>
	 * <p>
	 * This use case aims to produce a "Already up to date message".
	 * </p>
	 * <h3>History:</h3>
	 * 
	 * <pre>
	 * Initial commit (PapyrusProject3) [Master]
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void alreadyUpToDate0() throws Exception {
		Path projectPath = getRepositoryPath().resolve("PapyrusModel");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/automerging/MER003/branch_a/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_a/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_a/model.notation") //
				.create(projectPath);
		RevCommit rev = addAllAndCommit("Initial commit [PapyrusProject3]");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				"master");

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		// printOut();
		// printErr();

		assertTrue(getGit().status().call().isClean());
		assertEquals(getGit().getRepository().resolve("HEAD").getName(), rev.getId().getName());
		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEquals(Returns.COMPLETE.code(), result);

	}

	/**
	 * <h3>Test the logical application on previous commit of the current branch</h3> <h3>History:</h3>
	 * 
	 * <pre>
	 * * Adds Class 1 [branch_b]
	 * |    
	 * |  
	 * Initial commit (PapyrusProject3) [branch_a]
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void alreadyUpToDate1() throws Exception {
		Path projectPath = getRepositoryPath().resolve("PapyrusModel");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/automerging/MER003/branch_a/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_a/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_a/model.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit [PapyrusProject3]");
		createBranch(branchA, "master");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/automerging/MER003/branch_b/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_b/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_b/model.notation") //
				.create(projectPath);

		RevCommit commitB = addAllAndCommit("Adds class 1");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is launched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchA);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		assertOutputMessageEnd("Already up to date." + EOL + EOL);
		assertEquals(Returns.COMPLETE.code(), result);

		assertTrue(getGit().status().call().isClean());
		assertEquals(getGit().getRepository().resolve("HEAD").getName(), commitB.getId().getName());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.app.AbstractApplicationTest#buildApp()
	 */
	@Override
	protected IApplication buildApp() {
		return new LogicalMergeApplication();
	}

	/**
	 * Create a Oomph setup file be bing able to handle the merge of a Papyrus model.
	 * 
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private File createPapyrusUserOomphModel(File... project) throws IOException {
		OomphUserModelBuilder userModelBuilder = new OomphUserModelBuilder();
		Path oomphFolderPath = getTestTmpFolder().resolve("oomphFolder");
		File userSetupFile = userModelBuilder.setInstallationTaskLocation(oomphFolderPath.toString()) //
				.setWorkspaceLocation(getWorkspaceLocation().getAbsolutePath()) //
				.setProjectPaths(Arrays.stream(project).map(p -> p.getAbsolutePath()).toArray(String[]::new)) //
				.setRepositories("http://download.eclipse.org/releases/luna/201406250900",
						"http://download.eclipse.org/modeling/emf/compare/updates/nightly/latest/") //
				.setRequirements("org.eclipse.uml2.feature.group",
						"org.eclipse.papyrus.sdk.feature.feature.group",
						"org.eclipse.emf.compare.rcp.ui.feature.group",
						"org.eclipse.emf.compare.uml2.feature.group",
						"org.eclipse.emf.compare.diagram.gmf.feature.group",
						"org.eclipse.emf.compare.diagram.papyrus.feature.group") //
				.saveTo(getTestTmpFolder().resolve("setup.setup").toString());
		return userSetupFile;
	}

	/**
	 * Assert that there is no conflict marker in the file (( <<<<<<<<<< or ========= or >>>>>>>>>>>). In fact
	 * this test try to load the resource.
	 * 
	 * @param paths
	 * @throws IOException
	 * @throws AssertionError
	 */
	private void assertNoConflitMarker(Path... paths) throws AssertionError, IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		for (Path p : paths) {
			try {
				Resource resource = resourceSet.getResource(URI.createFileURI(p.toString()), true);
				assertNotNull(resource);
			} catch (Exception e) {
				throw new AssertionError("Error wile parsing resource " + p.toString() + EOL
						+ getConfigurationMessage(), e);
			}
		}
	}

	private void assertExistInResource(Path resourcePath, String... fragments) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.getResource(URI.createFileURI(resourcePath.toString()), true);
		assertNotNull(resource);
		for (String fragment : fragments) {
			EObject eObject = resource.getEObject(fragment);
			assertNotNull("Element with framgment " + fragment + " does not exist" + EOL
					+ getConfigurationMessage(), eObject);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.app.AbstractApplicationTest#getApp()
	 */
	@Override
	protected LogicalMergeApplication getApp() {
		return (LogicalMergeApplication)super.getApp();
	}

	// @Test
	// public void incorrectOomphSetupFile_NoExistingProject() throws Exception {
	// Path projectPath = getRepositoryPath().resolve("AProject");
	// File project = new ProjectBuilder(this) //
	// .addNewFileContent("ATextFile.txt", "SomeContent").create(projectPath);
	// String branchA = "branch_a";
	// addAllAndCommit("Initial commit");
	// createBranch(branchA, "master");
	//
	// getGit().close();
	//
	// // Creates Oomph model
	// File userSetupFile = createPapyrusUserOomphModel(getRepositoryPath().resolve("NonExistingProject")
	// .toFile());
	//
	// // Mocks that the commands is lauched from the git repository folder.
	// setCmdLocation(getRepositoryPath().toString());
	//
	// // Sets args
	// getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
	// branchA);
	//
	// // Runs command
	// Object result = getApp().start(getContext());
	//
	// // Uncomments to displays output
	// printOut();
	// printErr();
	//
	// // IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	// // assertEquals(1, projectInWorkspace.length);
	// //
	// // assertEquals(Returns.ABORTED.code(), result);
	// //
	// // StringBuilder expectedOut = new StringBuilder();
	// // expectedOut.append("Auto-merging failed in ").append("MER001/model.notation").append(EOL);
	// // expectedOut.append("Auto-merging failed in ").append("MER001/model.uml").append(EOL);
	// // expectedOut.append("Automatic merge failed; fix conflicts and then commit the result.").append(EOL)
	// // .append(EOL);
	// // assertOutputMessageEnd(expectedOut.toString());
	// //
	// // assertNoConflitMarker(projectPath.resolve("model.uml"), projectPath.resolve("model.notation"));
	// //
	// // Set<String> expectedConflictingFilePath = Sets
	// // .newHashSet("MER001/model.uml", "MER001/model.notation");
	// // assertEquals(expectedConflictingFilePath, getGit().status().call().getConflicting());
	// }

	/**
	 * <h3>Use case MER001</h3>
	 * <p>
	 * This use case is used to achive a conflicting merge
	 * </p>
	 * 
	 * <pre>
	 * * Delete Class1 [branch_c]
	 * |
	 * | * Add Attribute1 under Class1 [branch_b]
	 * |/  
	 * |   
	 * Initial Commit [branch_a]
	 *  -Add project PapyrusProject1
	 *  -Add ClassDiagram1
	 *  -Add Class1
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMER001() throws Exception {

		Path projectPath = getRepositoryPath().resolve("MER001");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/conflicts/MER001/branch_a/model.di")//
				.addContentToCopy("data/conflicts/MER001/branch_a/model.uml") //
				.addContentToCopy("data/conflicts/MER001/branch_a/model.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit [PapyrusProject3]");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER001/branch_c/model.di")//
				.addContentToCopy("data/conflicts/MER001/branch_c/model.uml") //
				.addContentToCopy("data/conflicts/MER001/branch_c/model.notation") //
				.create(projectPath);

		addAllAndCommit("Deletes Class1");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER001/branch_b/model.di")//
				.addContentToCopy("data/conflicts/MER001/branch_b/model.uml") //
				.addContentToCopy("data/conflicts/MER001/branch_b/model.notation") //
				.create(projectPath);

		addAllAndCommit("Adds attribute1 under Class1");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchC);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertEquals(Returns.ABORTED.code(), result);

		StringBuilder expectedOut = new StringBuilder();
		expectedOut.append("Auto-merging failed in ").append("MER001/model.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER001/model.uml").append(EOL);
		expectedOut.append("Automatic merge failed; fix conflicts and then commit the result.").append(EOL)
				.append(EOL);
		assertOutputMessageEnd(expectedOut.toString());

		assertNoConflitMarker(projectPath.resolve("model.uml"), projectPath.resolve("model.notation"));

		Set<String> expectedConflictingFilePath = Sets
				.newHashSet("MER001/model.uml", "MER001/model.notation");
		assertEquals(expectedConflictingFilePath, getGit().status().call().getConflicting());
	}

	/**
	 * <h3>Test use case MER002</h3>
	 * <p>
	 * This aims to test a merge between two branches with more than commit between them (see history)
	 * </p>
	 * <h3>History</h3>
	 * 
	 * <pre>
	 * * Delete Class2 [branch_d]
	 * | 
	 * |  
	 * Delete Class1 [branc_c]
	 * |     
	 * |    
	 * | * Add attribute1 under Class1, attribute2 under Class2 [branch_b]
	 * |/  
	 * |   
	 * |  
	 * Initial Commit [branch_a]
	 *  -Add project PapyrusProject2
	 *  -Add ClassDiagram1
	 *  -Add Class1, Class2
	 * 
	 * </pre>
	 */
	@Test
	public void testMER002() throws Exception {
		Path projectPath = getRepositoryPath().resolve("MER002");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/conflicts/MER002/branch_a/model.di")//
				.addContentToCopy("data/conflicts/MER002/branch_a/model.uml") //
				.addContentToCopy("data/conflicts/MER002/branch_a/model.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit [PapyrusProject3]");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER002/branch_c/model.di")//
				.addContentToCopy("data/conflicts/MER002/branch_c/model.uml") //
				.addContentToCopy("data/conflicts/MER002/branch_c/model.notation") //
				.create(projectPath);

		addAllAndCommit("Deletes Class1");

		// Creates branch d
		String branchD = "branch_d";
		createBranchAndCheckout(branchD, branchC);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER002/branch_c/model.di")//
				.addContentToCopy("data/conflicts/MER002/branch_c/model.uml") //
				.addContentToCopy("data/conflicts/MER002/branch_c/model.notation") //
				.create(projectPath);

		addAllAndCommit("Delete Class2");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER002/branch_b/model.di")//
				.addContentToCopy("data/conflicts/MER002/branch_b/model.uml") //
				.addContentToCopy("data/conflicts/MER002/branch_b/model.notation") //
				.create(projectPath);

		addAllAndCommit("Add attribute1 under Class1, attribute2 under Class2");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchD);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertEquals(Returns.ABORTED.code(), result);

		StringBuilder expectedOut = new StringBuilder();
		expectedOut.append("Auto-merging failed in ").append("MER002/model.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER002/model.uml").append(EOL);
		expectedOut.append("Automatic merge failed; fix conflicts and then commit the result.").append(EOL)
				.append(EOL);
		assertOutputMessageEnd(expectedOut.toString());

		assertNoConflitMarker(projectPath.resolve("model.uml"), projectPath.resolve("model.notation"));

		Set<String> expectedConflictingFilePath = Sets
				.newHashSet("MER002/model.uml", "MER002/model.notation");
		assertEquals(expectedConflictingFilePath, getGit().status().call().getConflicting());

	}

	/**
	 * <h3>Test with a setup file that references incorrect projects.</h3>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIncorrectProjectToImport() throws Exception {
		Path existinProjectPath = getRepositoryPath().resolve("MER003");
		File existingProject = new ProjectBuilder(this) //
				.addContentToCopy("data/automerging/MER003/branch_a/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_a/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_a/model.notation") //
				.create(existinProjectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit [PapyrusProject3]");
		createBranch(branchA, "master");

		File notExistinProject = getRepositoryPath().resolve("GhostProject").toFile();
		File notAProject = getRepositoryPath().resolve("Empty folder").toFile();
		notAProject.mkdirs();

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(existingProject, notExistinProject, notAProject);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchA);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		StringBuilder expectedOut = new StringBuilder();
		expectedOut
				.append("fatal: Could not import all required projects in the workspace. Here is a list projects that were no imported in the workspace:")
				.append(EOL);
		expectedOut.append(
				String.join(EOL, notExistinProject.getAbsolutePath(), notAProject.getAbsolutePath())).append(
				EOL);
		expectedOut.append("Here is a list to actual project in the workspace:").append(EOL);
		expectedOut.append(existingProject.getAbsolutePath()).append(EOL);
		expectedOut.append(EOL);

		assertOutputMessageEnd(expectedOut.toString());
		assertEquals(Returns.ERROR.code(), result);

	}

	/**
	 * <h3>Test the use case MER003</h3>
	 * <p>
	 * This use case aims to test a logical merge on a model with no conflict (Auto merging should succeed).
	 * </p>
	 * <h3>History:</h3>
	 * 
	 * <pre>
	 * * Adds Class 1 [branch_b]
	 * |    
	 * | * Adds Class 2 [branch_c]
	 * |/ 
	 * |  
	 * Initial commit (PapyrusProject3) [branch_a]
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMER003() throws Exception {
		Path projectPath = getRepositoryPath().resolve("MER003");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/automerging/MER003/branch_a/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_a/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_a/model.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit [PapyrusProject3]");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/automerging/MER003/branch_c/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_c/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_c/model.notation") //
				.create(projectPath);

		addAllAndCommit("Adds class 2");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/automerging/MER003/branch_b/model.di")//
				.addContentToCopy("data/automerging/MER003/branch_b/model.uml") //
				.addContentToCopy("data/automerging/MER003/branch_b/model.notation") //
				.create(projectPath);

		addAllAndCommit("Adds class 1");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchC);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertOutputMessageEnd("Merge made by 'recursive' strategy." + EOL + EOL);
		assertEquals(Returns.COMPLETE.code(), result);

		final String class1URIFragment = "_bB2fYC3HEeSN_5D5iyrZGQ";
		final String class2URIFragment = "_hfIr4C3HEeSN_5D5iyrZGQ";
		assertExistInResource(project.toPath().resolve("model.uml"), class1URIFragment, class2URIFragment);

		final String class2ShapeURIFragment = "_hfJS8C3HEeSN_5D5iyrZGQ";
		final String class1ShapeURIFragment = "_bB3tgC3HEeSN_5D5iyrZGQ";
		assertExistInResource(project.toPath().resolve("model.notation"), class1ShapeURIFragment,
				class2ShapeURIFragment);

	}

	/**
	 * <pre>
	 *  * [branch_c]
	 *  |     Add Class3 under Model1
	 *  |     Add Class4 under Model2
	 *  |    
	 *  | * [branch_b)]
	 *  |/  Add Class1 under Model1
	 *  |   Add Class2 under Model2
	 *  |  
	 * [branch_a]
	 *  Initial commit
	 *   - A project with 2 models, 2 diagrams
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMER004() throws Exception {
		Path projectPath = getRepositoryPath().resolve("MER004");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/automerging/MER004/branch_a/model.di")//
				.addContentToCopy("data/automerging/MER004/branch_a/model.uml") //
				.addContentToCopy("data/automerging/MER004/branch_a/model.notation") //
				.addContentToCopy("data/automerging/MER004/branch_a/model2.di")//
				.addContentToCopy("data/automerging/MER004/branch_a/model2.uml") //
				.addContentToCopy("data/automerging/MER004/branch_a/model2.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit" + EOL + "- A project with 2 models, 2 diagrams");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/automerging/MER004/branch_c/model.di")//
				.addContentToCopy("data/automerging/MER004/branch_c/model.uml") //
				.addContentToCopy("data/automerging/MER004/branch_c/model.notation") //
				.addContentToCopy("data/automerging/MER004/branch_c/model2.di")//
				.addContentToCopy("data/automerging/MER004/branch_c/model2.uml") //
				.addContentToCopy("data/automerging/MER004/branch_c/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Add Class3 under Model1" + EOL + "Add Class4 under Model2");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/automerging/MER004/branch_b/model.di")//
				.addContentToCopy("data/automerging/MER004/branch_b/model.uml") //
				.addContentToCopy("data/automerging/MER004/branch_b/model.notation") //
				.addContentToCopy("data/automerging/MER004/branch_b/model2.di")//
				.addContentToCopy("data/automerging/MER004/branch_b/model2.uml") //
				.addContentToCopy("data/automerging/MER004/branch_b/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Add Class1 under Model1" + EOL + "Add Class2 under Model2");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchC);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertOutputMessageEnd("Merge made by 'recursive' strategy." + EOL + EOL);
		assertEquals(Returns.COMPLETE.code(), result);

		final String class1URIFragment = "_adib0C9QEeShUolneTgohg";
		final String class3URIFragment = "_lztC0C9QEeShUolneTgohg";
		assertExistInResource(project.toPath().resolve("model.uml"), class1URIFragment, class3URIFragment);

		final String class2URIFragment = "_a7N2UC9QEeShUolneTgohg";
		final String class4URIFragment = "_m3mv0C9QEeShUolneTgohg";
		assertExistInResource(project.toPath().resolve("model2.uml"), class2URIFragment, class4URIFragment);

		final String class1ShapeURIFragment = "_adjp8C9QEeShUolneTgohg";
		final String class3ShapeURIFragment = "_lzuQ8C9QEeShUolneTgohg";
		assertExistInResource(project.toPath().resolve("model.notation"), class1ShapeURIFragment,
				class3ShapeURIFragment);

		final String class2ShapeURIFragment = "_a7PEcC9QEeShUolneTgohg";
		final String class4ShapeURIFragment = "_m3nW4C9QEeShUolneTgohg";
		assertExistInResource(project.toPath().resolve("model2.notation"), class2ShapeURIFragment,
				class4ShapeURIFragment);

	}

	/**
	 * <pre>
	 * * [HEAD] [branch_c]
	 * |     Delete Class1 and Class2
	 * |    
	 * | * [branch_b]
	 * |/  Add attribute1 under Class1
	 * |   Add attribute2 under Class2
	 * |  
	 * [branch_a]
	 * Initial commit
	 *  - 1 project with 2 models, 2 diagrams
	 *  - add Class1 under Model1, Class2 under Model2
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMER005() throws Exception {
		Path projectPath = getRepositoryPath().resolve("MER005");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/conflicts/MER005/branch_a/model.di")//
				.addContentToCopy("data/conflicts/MER005/branch_a/model.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_a/model.notation") //
				.addContentToCopy("data/conflicts/MER005/branch_a/model2.di")//
				.addContentToCopy("data/conflicts/MER005/branch_a/model2.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_a/model2.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit" + EOL + " - 1 project with 2 models, 2 diagrams" + EOL
				+ " - add Class1 under Model1, Class2 under Model2");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER005/branch_c/model.di")//
				.addContentToCopy("data/conflicts/MER005/branch_c/model.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_c/model.notation") //
				.addContentToCopy("data/conflicts/MER005/branch_c/model2.di")//
				.addContentToCopy("data/conflicts/MER005/branch_c/model2.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_c/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Delete Class1 and Class2");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER005/branch_b/model.di")//
				.addContentToCopy("data/conflicts/MER005/branch_b/model.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_b/model.notation") //
				.addContentToCopy("data/conflicts/MER005/branch_b/model2.di")//
				.addContentToCopy("data/conflicts/MER005/branch_b/model2.uml") //
				.addContentToCopy("data/conflicts/MER005/branch_b/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Add attribute1 under Class1" + EOL + "Add attribute2 under Class2");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchC);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertNoConflitMarker(projectPath.resolve("model.uml"), projectPath.resolve("model.notation"),
				projectPath.resolve("model2.uml"), projectPath.resolve("model2.notation"));

		assertEquals(Returns.ABORTED.code(), result);

		StringBuilder expectedOut = new StringBuilder();
		expectedOut.append("Auto-merging failed in ").append("MER005/model.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER005/model.uml").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER005/model2.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER005/model2.uml").append(EOL);
		expectedOut.append("Automatic merge failed; fix conflicts and then commit the result.").append(EOL)
				.append(EOL);
		assertOutputMessageEnd(expectedOut.toString());

		Set<String> expectedConflictingFilePath = Sets.newHashSet("MER005/model.uml",
				"MER005/model.notation", "MER005/model2.uml", "MER005/model2.notation");
		assertEquals(expectedConflictingFilePath, getGit().status().call().getConflicting());

	}

	/**
	 * <h3>Test MER006</h3>
	 * <p>
	 * Successives conflicts on multiple models in multiple files (one file per model)
	 * </p>
	 * 
	 * <pre>
	 *  * [branch_d]
	 *  |     Delete Class2
	 *  |  
	 *  [branch_c] Delete Class1
	 *  |    
	 *  | *  [branch_b]
	 *  |/   Add attribute1 under Class1
	 *  |    Add attribute2 under Class2
	 *  |   
	 *  [branch_a]
	 * Initial commit
	 *   - 1 project with 2 models, 2 diagrams
	 *   - add Class1 under Model1, Class2 under Model2
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMER006() throws Exception {
		Path projectPath = getRepositoryPath().resolve("MER006");
		File project = new ProjectBuilder(this) //
				.addContentToCopy("data/conflicts/MER006/branch_a/model.di")//
				.addContentToCopy("data/conflicts/MER006/branch_a/model.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_a/model.notation") //
				.addContentToCopy("data/conflicts/MER006/branch_a/model2.di")//
				.addContentToCopy("data/conflicts/MER006/branch_a/model2.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_a/model2.notation") //
				.create(projectPath);
		String branchA = "branch_a";
		addAllAndCommit("Initial commit" + EOL + "   - 1 project with 2 models, 2 diagrams" + EOL
				+ "   - add Class1 under Model1, Class2 under Model2");
		createBranch(branchA, "master");

		// Creates branch c
		String branchC = "branch_c";
		createBranchAndCheckout(branchC, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER006/branch_c/model.di")//
				.addContentToCopy("data/conflicts/MER006/branch_c/model.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_c/model.notation") //
				.addContentToCopy("data/conflicts/MER006/branch_c/model2.di")//
				.addContentToCopy("data/conflicts/MER006/branch_c/model2.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_c/model2.notation") //
				.create(projectPath);

		addAllAndCommit(" Delete Class1");

		// Creates branch d
		String branchD = "branch_d";
		createBranchAndCheckout(branchD, branchC);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER006/branch_d/model.di")//
				.addContentToCopy("data/conflicts/MER006/branch_d/model.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_d/model.notation") //
				.addContentToCopy("data/conflicts/MER006/branch_d/model2.di")//
				.addContentToCopy("data/conflicts/MER006/branch_d/model2.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_d/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Delete Class2");

		// Creates branch b
		String branchB = "branch_b";
		createBranchAndCheckout(branchB, branchA);

		project = new ProjectBuilder(this) //
				.clean(true) //
				.addContentToCopy("data/conflicts/MER006/branch_b/model.di")//
				.addContentToCopy("data/conflicts/MER006/branch_b/model.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_b/model.notation") //
				.addContentToCopy("data/conflicts/MER006/branch_b/model2.di")//
				.addContentToCopy("data/conflicts/MER006/branch_b/model2.uml") //
				.addContentToCopy("data/conflicts/MER006/branch_b/model2.notation") //
				.create(projectPath);

		addAllAndCommit("Add attribute1 under Class1, attribute2 under Class2");

		getGit().close();

		// Creates Oomph model
		File userSetupFile = createPapyrusUserOomphModel(project);

		// Mocks that the commands is lauched from the git repository folder.
		setCmdLocation(getRepositoryPath().toString());

		// Sets args
		getContext().addArg(getRepositoryPath().resolve(".git").toString(), userSetupFile.getAbsolutePath(),
				branchD);

		// Runs command
		Object result = getApp().start(getContext());

		// Uncomments to displays output
		printOut();
		printErr();

		IProject[] projectInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projectInWorkspace.length);

		assertNoConflitMarker(projectPath.resolve("model.uml"), projectPath.resolve("model.notation"),
				projectPath.resolve("model2.uml"), projectPath.resolve("model2.notation"));

		assertEquals(Returns.ABORTED.code(), result);

		StringBuilder expectedOut = new StringBuilder();
		expectedOut.append("Auto-merging failed in ").append("MER006/model.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER006/model.uml").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER006/model2.notation").append(EOL);
		expectedOut.append("Auto-merging failed in ").append("MER006/model2.uml").append(EOL);
		expectedOut.append("Automatic merge failed; fix conflicts and then commit the result.").append(EOL)
				.append(EOL);
		assertOutputMessageEnd(expectedOut.toString());

		Set<String> expectedConflictingFilePath = Sets.newHashSet("MER006/model.uml",
				"MER006/model.notation", "MER006/model2.uml", "MER006/model2.notation");
		assertEquals(expectedConflictingFilePath, getGit().status().call().getConflicting());
	}

}
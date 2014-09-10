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

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.synchronize.GitResourceVariantTreeSubscriber;
import org.eclipse.egit.core.synchronize.GitSubscriberResourceMappingContext;
import org.eclipse.egit.core.synchronize.dto.GitSynchronizeData;
import org.eclipse.egit.core.synchronize.dto.GitSynchronizeDataSet;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.git.pgm.app.internal.ProgressPageLog;
import org.eclipse.emf.compare.git.pgm.app.internal.args.CmdLineParserGitAware;
import org.eclipse.emf.compare.git.pgm.app.internal.args.GitDirHandler;
import org.eclipse.emf.compare.git.pgm.app.internal.args.SetupFileOptionHandler;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die.DeathType;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die.DiesOn;
import org.eclipse.emf.compare.git.pgm.app.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.emf.compare.ide.ui.internal.logical.EMFModelProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.oomph.base.provider.BaseEditUtil;
import org.eclipse.oomph.internal.setup.SetupPrompter;
import org.eclipse.oomph.setup.Index;
import org.eclipse.oomph.setup.Project;
import org.eclipse.oomph.setup.ProjectCatalog;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.SetupTask;
import org.eclipse.oomph.setup.Trigger;
import org.eclipse.oomph.setup.internal.core.SetupContext;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.setup.internal.core.util.ECFURIHandlerImpl;
import org.eclipse.oomph.setup.internal.core.util.SetupUtil;
import org.eclipse.oomph.util.Confirmer;
import org.eclipse.oomph.util.IOUtil;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;

/**
 * Abstract class for any logical application.
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
@SuppressWarnings("restriction")
public abstract class AbstractLogicalApplication implements IApplication {

	/**
	 * Holds git directory location.
	 */
	@Argument(index = 0, metaVar = "gitFolderPath", usage = "Path to the .git folder of your repository.", handler = GitDirHandler.class)
	protected String gitdir;

	/**
	 * Holds the Oomph model setup file.
	 */
	@Argument(index = 1, metaVar = "<setup>", required = true, usage = "Path to the setup file. The setup file is a Oomph model.", handler = SetupFileOptionHandler.class)
	protected File setupFile;

	/**
	 * Logs any message from oomph.
	 */
	protected ProgressPageLog progressPageLog;

	/**
	 * Git repository for this command to be executed in.
	 */
	protected Repository repo;

	/**
	 * Performs the logical git command (diff or merge).
	 * 
	 * @return a {@link org.eclipse.emf.compare.git.pgm.app.ReturnCode}.
	 */
	protected abstract Integer performGitCommand();

	/**
	 * Creates and configure the setup task performer to execute the imports of projects referenced in the
	 * user setup model. Then call the {@link #performGitCommand()}.
	 * 
	 * @return a {@link org.eclipse.emf.compare.git.pgm.app.ReturnCode}.
	 * @throws IOException
	 * @throws Die
	 */
	protected Integer performStartup() throws Die {
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(BaseEditUtil
				.createAdapterFactory());

		ResourceSet rs = SetupUtil.createResourceSet();
		rs.eAdapters().add(
				new AdapterFactoryEditingDomain.EditingDomainProvider(new AdapterFactoryEditingDomain(
						adapterFactory, null, rs)));
		rs.getLoadOptions().put(ECFURIHandlerImpl.OPTION_CACHE_HANDLING,
				ECFURIHandlerImpl.CacheHandling.CACHE_WITHOUT_ETAG_CHECKING);

		URI startupSetupURI = URI.createFileURI(setupFile.getAbsolutePath());
		Resource startupSetupResource = rs.getResource(startupSetupURI, true);

		Index startupSetupIndex = (Index)EcoreUtil.getObjectByType(startupSetupResource.getContents(),
				SetupPackage.Literals.INDEX);

		SetupContext setupContext = SetupContext.createInstallationAndUser(rs);

		try {
			Trigger triggerStartup = Trigger.STARTUP;
			URIConverter uriConverter = rs.getURIConverter();
			SetupTaskPerformer performerStartup = SetupTaskPerformer.create(uriConverter,
					SetupPrompter.CANCEL, triggerStartup, setupContext, false);
			Confirmer confirmer = Confirmer.ACCEPT;
			performerStartup.put(ILicense.class, confirmer);
			performerStartup.put(Certificate.class, confirmer);

			progressPageLog = new ProgressPageLog(System.out);
			performerStartup.setProgress(progressPageLog);

			final IWorkspace workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IWorkspaceRoot root = workspace.getRoot();
					for (IProject project : root.getProjects()) {
						project.delete(true, null);
					}

					for (File file : root.getLocation().toFile().listFiles()) {
						if (file.isDirectory()) {
							// Hack waiting for a reponse on
							// https://www.eclipse.org/forums/index.php?t=rview&goto=1415112#msg_1415112
							if (".metadata".equals(file.getName())) {
								// Deletes the Oomph import-history.properties to force new import
								File importHistory = new File(
										file.getAbsolutePath()
												+ File.separator
												+ ".plugins/org.eclipse.oomph.setup.projects/import-history.properties");
								if (importHistory.exists()) {
									IOUtil.deleteBestEffort(importHistory);
									try {
										importHistory.createNewFile();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}, null);

			// Import Projects
			for (ProjectCatalog projectCatalog : startupSetupIndex.getProjectCatalogs()) {
				for (Project project : projectCatalog.getProjects()) {
					for (SetupTask setupTask : project.getSetupTasks()) {
						performerStartup.getTriggeredSetupTasks().add(setupTask);
					}
				}
			}

			performerStartup.perform();

			if (!performerStartup.hasSuccessfullyPerformed()) {
				throw new DiesOn(DeathType.FATAL).displaying("Error during Oomph operation").ready();
			}
		} catch (Exception e) {
			progressPageLog.log(e);
			throw new DiesOn(DeathType.FATAL).duedTo(e).displaying("Error during Oomph operation").ready();
		}

		Integer returnCode = performGitCommand();

		return returnCode;
	}

	/**
	 * {@inheritDoc}.
	 */
	public Object start(IApplicationContext context) throws Exception {
		// Prevents VM args if the application exits on somehting different that 0
		System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
		final Map args = context.getArguments();
		final String[] appArgs = (String[])args.get("application.args"); //$NON-NLS-1$

		final CmdLineParserGitAware clp = new CmdLineParserGitAware(this, null);
		try {
			clp.parseArgument(appArgs);
			repo = clp.getRepo();
		} catch (CmdLineException err) {
			err.printStackTrace();
			System.err.println(err.getMessage());
		}
		try {
			return performStartup();
		} catch (Die e) {
			return EMFCompareGitPGMUtil.handleDieError(e, true);
		}

	}

	/**
	 * {@inheritDoc}.
	 */
	public void stop() {
		// Nothing to do.
	}

	/**
	 * @see org.eclipse.egit.ui.internal.CompareUtils#canDirectlyOpenInCompare(IFile)
	 * @param file
	 *            the file to test.
	 * @return true if the file to test is EMFCompare compliant, false otherwise.
	 */
	protected boolean isEMFCompareCompliantFile(RemoteResourceMappingContext mergeContext, IFile file) {
		try {
			EMFModelProvider modelProvider = new EMFModelProvider();
			ResourceMapping[] modelMappings = modelProvider.getMappings(file, mergeContext,
					new NullProgressMonitor());
			if (modelMappings.length > 0) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets the tree iterator of the id located in the repository.
	 * 
	 * @param repository
	 *            the repository containing the id.
	 * @param id
	 *            the id for which we want the tree iterator.
	 * @return the tree iterator of the id located in the repository.
	 * @throws IOException
	 */
	protected AbstractTreeIterator getTreeIterator(Repository repository, ObjectId id) throws IOException {
		final CanonicalTreeParser p = new CanonicalTreeParser();
		final ObjectReader or = repository.newObjectReader();
		try {
			p.reset(or, new RevWalk(repository).parseTree(id));
			return p;
		} finally {
			or.release();
		}
	}

	/**
	 * Simulate a comparison between the two given references and returns back the subscriber that can provide
	 * all computed synchronization information.
	 * 
	 * @param sourceRef
	 *            Source reference (i.e. "left" side of the comparison).
	 * @param targetRef
	 *            Target reference (i.e. "right" side of the comparison).
	 * @param comparedFile
	 *            The file we are comparing (that would be the file right-clicked into the workspace).
	 * @return The created subscriber.
	 */
	protected RemoteResourceMappingContext createSubscriberForComparison(Repository repository,
			ObjectId sourceRef, ObjectId targetRef, IFile comparedFile) throws IOException {
		final GitSynchronizeData data = new GitSynchronizeData(repository, sourceRef.getName(), targetRef
				.getName(), false);
		final GitSynchronizeDataSet dataSet = new GitSynchronizeDataSet(data);
		GitResourceVariantTreeSubscriber subscriber = new GitResourceVariantTreeSubscriber(dataSet);
		subscriber.init(new NullProgressMonitor());
		return new GitSubscriberResourceMappingContext(subscriber, dataSet);
	}

	/**
	 * This will query all model providers for those that are enabled on the given file and list all mappings
	 * available for that file.
	 * 
	 * @param file
	 *            The file for which we need the associated resource mappings.
	 * @return All mappings available for that file.
	 */
	protected ResourceMapping[] getResourceMappings(RemoteResourceMappingContext mergeContext, IFile file) {
		final Set<ResourceMapping> mappings = new LinkedHashSet<ResourceMapping>();
		try {
			EMFModelProvider modelProvider = new EMFModelProvider();
			ResourceMapping[] modelMappings = modelProvider.getMappings(file, mergeContext,
					new NullProgressMonitor());
			for (ResourceMapping mapping : modelMappings) {
				mappings.add(mapping);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return mappings.toArray(new ResourceMapping[mappings.size()]);
	}
}

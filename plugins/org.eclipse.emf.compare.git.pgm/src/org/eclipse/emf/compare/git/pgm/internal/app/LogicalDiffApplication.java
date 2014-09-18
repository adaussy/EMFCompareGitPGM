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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.args.PathFilterHandler;
import org.eclipse.emf.compare.git.pgm.internal.args.RefOptionHandler;
import org.eclipse.emf.compare.ide.ui.internal.logical.ComparisonScopeBuilder;
import org.eclipse.emf.compare.ide.ui.internal.logical.EMFResourceMapping;
import org.eclipse.emf.compare.ide.ui.internal.logical.IdenticalResourceMinimizer;
import org.eclipse.emf.compare.ide.ui.logical.IModelMinimizer;
import org.eclipse.emf.compare.ide.ui.logical.SynchronizationModel;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Logical diff application. <h3>Name</h3>
 * <p>
 * logicaldiff - Git Logical Diff
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicaldiff &lt;setup&gt; &lt;commit&gt; [&lt;compareWithCommit&gt;] [ -- &lt;paths...&gt;]
 * </p>
 * <h4>Description</h4>
 * <p>
 * The logical diff is used to display differences using logical model.
 * </p>
 * </p>
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
@SuppressWarnings({"restriction" })
public class LogicalDiffApplication extends AbstractLogicalApplication {

	/**
	 * Holds the reference from which the differences should be displayed.
	 */
	@Argument(index = 2, multiValued = false, required = true, metaVar = "<commit>", usage = "Commit ID or branch name.", handler = RefOptionHandler.class)
	private ObjectId commit;

	/**
	 * Optional reference used to view the differences between {@link #commit} and {@link #commitWith}.
	 */
	@Argument(index = 3, multiValued = false, required = false, metaVar = "<compareWithCommit>", usage = "Commit ID or branch name. This is to view the changes between <commit> and <compareWithCommit> or HEAD if not specified.", handler = RefOptionHandler.class)
	private ObjectId commitWith;

	/**
	 * {@link TreeFilter} use to filter file on which differences should be shown.
	 */
	@Option(name = "--", metaVar = "<path...>", multiValued = false, handler = PathFilterHandler.class, usage = "This is used to limit the diff to the named paths (you can give directory names and get diff for all files under them).")
	private TreeFilter pathFilter;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected Integer performGitCommand() {
		try {
			IWorkspace ws = ResourcesPlugin.getWorkspace();

			// Call JGit diff to get the files involved
			OutputStream out = new ByteArrayOutputStream();
			DiffCommand diffCommand = Git.open(repo.getDirectory()).diff().setOutputStream(out);
			if (commit != null) {
				diffCommand = diffCommand.setOldTree(getTreeIterator(repo, commit));
			}
			if (commitWith != null) {
				diffCommand = diffCommand.setNewTree(getTreeIterator(repo, commitWith));
			}
			if (pathFilter != null) {
				diffCommand = diffCommand.setPathFilter(pathFilter);
			}
			Set<IFile> files = new HashSet<IFile>();
			List<DiffEntry> entries = diffCommand.call();

			for (DiffEntry diffEntry : entries) {
				String path = diffEntry.getOldPath();
				if (path != null) {
					IFile file = ws.getRoot().getFile(new Path(path));
					if (file != null) {
						files.add(file);
					}
				}
			}

			if (files.isEmpty()) {
				System.out.println("No difference to display.");
				return Returns.COMPLETE.code();
			}

			for (IFile file : files) {
				RemoteResourceMappingContext mergeContext = createSubscriberForComparison(repo, commit,
						commitWith, file);
				if (!isEMFCompareCompliantFile(mergeContext, file)) {
					diffCommand.setPathFilter(PathFilter.create(file.getProjectRelativePath().toString()));
					diffCommand.call();
					System.out.println(out.toString());
				} else {
					final ResourceMapping[] emfMappings = getResourceMappings(mergeContext, file);
					for (ResourceMapping mapping : emfMappings) {
						if (mapping instanceof EMFResourceMapping) {
							/*
							 * These two won't be used by the builder in our case since we retrieve the
							 * already created syncModel from the resource mapping.
							 */
							final IModelMinimizer minimizer = new IdenticalResourceMinimizer();
							final NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

							mapping.getTraversals(mergeContext, nullProgressMonitor);
							final SynchronizationModel syncModel = ((EMFResourceMapping)mapping)
									.getLatestModel();
							minimizer.minimize(syncModel, nullProgressMonitor);
							final IComparisonScope scope = ComparisonScopeBuilder.create(syncModel,
									nullProgressMonitor);

							final Comparison comparison = EMFCompare.builder().build().compare(scope,
									BasicMonitor.toMonitor(nullProgressMonitor));

							Resource resource = new XMIResourceImpl();
							Copier copier = new Copier(false);
							EObject comparisonCopy = copier.copy(comparison);
							copier.copyReferences();
							resource.getContents().add(comparisonCopy);

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							resource.save(baos, null);
							System.out.println(baos.toString("UTF-8"));//$NON-NLS-1$
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return Returns.COMPLETE.code();
	}

}

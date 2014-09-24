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

import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.op.MergeOperation;
import org.eclipse.emf.compare.git.pgm.Returns;
import org.eclipse.emf.compare.git.pgm.internal.args.RefOptionHandler;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DeathType;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DiesOn;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Logical merge application. <h3>Name</h3>
 * <p>
 * logicalmerge - Git Logical Merge
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicalmerge &lt;setup&gt; &lt;commit&gt;
 * </p>
 * <h4>Description</h4>
 * <p>
 * The logical merge command is used to merge logical models. Instead of merging each file one by one like git
 * would do, it uses a set of semantically interconnected files. It avoids semantical breakage of models.
 * </p>
 * </p>
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
@SuppressWarnings({"restriction" })
public class LogicalMergeApplication extends AbstractLogicalApplication {

	/**
	 * Length of a short commit id.
	 */
	private static final int SHORT_COMMIT_ID_LENGTH = 7;

	/**
	 * Holds a ObjectId that need to be merged.
	 */
	@Argument(index = 2, required = true, metaVar = "<commit>", usage = "Commit ID or branch name to merge.", handler = RefOptionHandler.class)
	private ObjectId commit;

	/**
	 * Optional message used for the merge commit.
	 */
	@Option(name = "-m", metaVar = "message", required = false, usage = " Set the commit message to be used for the merge commit (in case one is created).")
	private String message;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected Integer performGitCommand() throws Die {

		try {
			MergeOperation merge = new MergeOperation(repo, commit.getName());
			if (message != null) {
				merge.setMessage(message);
			}
			merge.execute(new NullProgressMonitor());
			MergeResult result = merge.getResult();
			Ref oldHead = repo.getRef(Constants.HEAD);

			return handleResult(result, oldHead, MergeStrategy.RECURSIVE);
		} catch (Exception e) {
			progressPageLog.log(e);
			throw new DiesOn(DeathType.FATAL).duedTo(e).ready();
		}

	}

	/**
	 * Handles the merge result. This method return the {@link Returns} depending of the merge status and
	 * display a message to the user.
	 * 
	 * @param mergeResult
	 *            The result of the merge.
	 * @param oldHead
	 *            the old HEAD reference before merge.
	 * @param strategy
	 *            The strategy used for the merge.
	 * @return a {@link Returns}.
	 * @throws Die
	 *             if the merge ends on error.
	 * @throws IOException
	 *             if a problem occurs while displaying a message to the user.
	 */
	private Integer handleResult(MergeResult mergeResult, Ref oldHead, MergeStrategy strategy) throws Die,
			IOException {
		final Integer returnCode;
		final String messageToPrint;
		switch (mergeResult.getMergeStatus()) {
			case MERGED:
				messageToPrint = new StringBuilder().append("Merge made by '").append(strategy.getName())
						.append("' strategy.").append(EOL).toString();
				returnCode = Returns.COMPLETE.code();
				break;
			case ALREADY_UP_TO_DATE:
				messageToPrint = new StringBuilder().append("Already up to date.").append(EOL).toString();
				returnCode = Returns.COMPLETE.code();
				break;
			case FAST_FORWARD:
				messageToPrint = buildFastForwardMessage(mergeResult, oldHead);
				returnCode = Returns.COMPLETE.code();
				break;
			case CONFLICTING:
				returnCode = Returns.ABORTED.code();
				messageToPrint = buildConflictingMessage(mergeResult);
				break;
			case FAILED:
				throw new DiesOn(DeathType.ERROR).displaying(getFailedMessage(mergeResult)).ready();
			case ABORTED:
				throw new DiesOn(DeathType.ERROR).displaying("There is no merge to abort").ready();
			case NOT_SUPPORTED:
			case MERGED_NOT_COMMITTED:
			case CHECKOUT_CONFLICT:
			case MERGED_SQUASHED:
			case FAST_FORWARD_SQUASHED:
			case MERGED_SQUASHED_NOT_COMMITTED:
			default:
				throw new DiesOn(DeathType.SOFTWARE_ERROR).displaying(getDefaultErrorMessage(mergeResult))
						.ready();
		}
		System.out.println(messageToPrint);
		return returnCode;

	}

	/**
	 * Builds the message to display to the user when the merge ends on a FAST_FORWARD state.
	 * 
	 * @param mergeResult
	 *            The merge result.
	 * @param oldHead
	 *            The previous head before merge.
	 * @return a message.
	 */
	private String buildFastForwardMessage(MergeResult mergeResult, Ref oldHead) {
		final StringBuilder messageBuilder = new StringBuilder();
		ObjectId oldHeadId = oldHead.getObjectId();
		messageBuilder.append("Updating ").append(oldHeadId.abbreviate(SHORT_COMMIT_ID_LENGTH).name())
				.append("..").append(mergeResult.getNewHead().abbreviate(SHORT_COMMIT_ID_LENGTH).name())
				.append(EOL);
		messageBuilder.append(mergeResult.getMergeStatus().toString());
		return messageBuilder.toString();
	}

	/**
	 * Builds the message to display to the user when merge ends on a conflicting state.
	 * 
	 * @param mergeResult
	 *            {@link MergeResult}.
	 * @return a message.
	 */
	private String buildConflictingMessage(MergeResult mergeResult) {
		final StringBuilder messageBuildder = new StringBuilder();
		try {
			// Should use mergeResult.getConflicting() however due to its random result we prefer using the
			// status of the git repository.
			final Status status = Git.wrap(repo).status().call();
			List<String> conflictingFile = Lists.newArrayList(status.getConflicting());
			// In order to have a determinist order.
			Collections.sort(conflictingFile);
			for (String conflicting : conflictingFile) {
				messageBuildder.append("Auto-merging failed in ").append(conflicting).append(EOL);

			}
		} catch (NoWorkTreeException e) {
			// Does nothing since this for console message
		} catch (GitAPIException e) {
			// Does nothing since this for console message
		}
		messageBuildder.append("Automatic merge failed; fix conflicts and then commit the result.").append(
				EOL);

		return messageBuildder.toString();
	}

	/**
	 * Builds the message to display to the user when the merge ends on a FAILED status.
	 * 
	 * @param mergeResult
	 *            The merge result.
	 * @return a message.
	 */
	private String getFailedMessage(MergeResult mergeResult) {
		final StringBuilder errorMessage = new StringBuilder();
		List<String> dirtyFiles = Lists.newArrayList();
		List<String> notDeletedFiles = Lists.newArrayList();
		for (Entry<String, MergeFailureReason> mergeFailure : mergeResult.getFailingPaths().entrySet()) {

			switch (mergeFailure.getValue()) {
				case DIRTY_INDEX:
				case DIRTY_WORKTREE:
					dirtyFiles.add(mergeFailure.getKey());
					break;
				case COULD_NOT_DELETE:
					notDeletedFiles.add(mergeFailure.getKey());
					break;
				default:
					break;
			}
		}
		if (!dirtyFiles.isEmpty()) {
			errorMessage.append("Your local changes to the following files would be overwritten by merge:"
					+ EOL);
			errorMessage.append(Joiner.on(EOL).join(dirtyFiles));
			errorMessage.append("Please, commit your changes or stash them before you can merge.");
		}
		if (!notDeletedFiles.isEmpty()) {
			errorMessage.append("Could not delete following files:" + EOL);
			errorMessage.append(Joiner.on(EOL).join(notDeletedFiles));
		}
		errorMessage.append("Aborting." + EOL);
		return errorMessage.toString();
	}

	/**
	 * Gets the default message error.
	 * 
	 * @param mergeResult
	 *            {@link MergeResult}.
	 * @return An error message.
	 */
	private String getDefaultErrorMessage(MergeResult mergeResult) {
		return new StringBuilder().append("Unsupported merge status '").append(
				mergeResult.getMergeStatus().toString()).append("'").append(EOL).toString();
	}

}

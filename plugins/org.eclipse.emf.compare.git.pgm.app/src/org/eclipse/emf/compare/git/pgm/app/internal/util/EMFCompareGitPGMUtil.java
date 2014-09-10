package org.eclipse.emf.compare.git.pgm.app.internal.util;

import static org.eclipse.emf.compare.git.pgm.app.internal.Messages.CAN_T_FIND_GIT_REPOSITORY_MESSAGE;
import static org.eclipse.emf.compare.git.pgm.app.internal.exception.Die.DeathType.FATAL;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.emf.compare.git.pgm.app.ReturnCode;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die.DiesOn;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class EMFCompareGitPGMUtil {

	/**
	 * File separtor
	 */
	public static final String FS = File.separator;

	/**
	 * End of line.
	 */
	public static final String EOL = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Build the git repository for this command.
	 * 
	 * @param aGitdir
	 *            a path to the git folder or null if the command is run inside the git repository.
	 * @return a git {@link Repository}.
	 * @throws Die
	 *             if the program exit prematurely.
	 */
	public static Repository buildRepository(String aGitdir) throws Die {
		final File gitDir;
		if (aGitdir == null) {
			gitDir = null;
		} else {
			gitDir = new File(aGitdir);
		}
		RepositoryBuilder rb = new RepositoryBuilder().setGitDir(gitDir).readEnvironment().setMustExist(true)
				.findGitDir();
		if (rb.getGitDir() == null) {
			throw new DiesOn(FATAL).displaying(CAN_T_FIND_GIT_REPOSITORY_MESSAGE).ready();
		}
		Repository repo;
		try {
			repo = rb.build();
		} catch (RepositoryNotFoundException e) {
			throw new DiesOn(FATAL).displaying(CAN_T_FIND_GIT_REPOSITORY_MESSAGE).ready();
		} catch (IOException e) {
			throw new DiesOn(FATAL).duedTo(e).displaying("Cannot build the git repository").ready();
		}
		return repo;
	}

	/**
	 * Displays the error message to the user and return matching {@link ReturnCode}.
	 * 
	 * @param error
	 *            Error to handle.
	 * @param showStackTrace
	 *            Set to <code>true</code> if the stack trace should be display in the console or
	 *            <code>false</code> otherwise.
	 * @return a {@link ReturnCode}
	 */
	public static Integer handleDieError(Die error, boolean showStackTrace) {
		final PrintStream stream;
		final Integer returnCode;
		final String prefix;
		switch (error.getType()) {
			case ERROR:
				prefix = "error: ";
				returnCode = ReturnCode.ERROR;
				stream = System.out;
				break;
			case FATAL:
				prefix = "fatal: ";
				returnCode = ReturnCode.ERROR;
				stream = System.out;
				break;
			case SOFTWARE_ERROR:
			default:
				prefix = "system: ";
				returnCode = ReturnCode.ERROR;
				stream = System.err;
				if (showStackTrace) {
					error.printStackTrace();
				}
				break;
		}
		if (showStackTrace && error.getCause() != null) {
			error.getCause().printStackTrace(stream);
		}
		if (error.getMessage() != null) {
			stream.println(prefix + error.getMessage());
		}

		return returnCode;
	}
}

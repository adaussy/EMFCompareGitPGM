package org.eclipse.emf.compare.git.pgm.app.internal.util;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.emf.compare.git.pgm.app.Returns;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;

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
	 * Empty string.
	 */
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Displays the error message to the user and return matching {@link Returns}.
	 * 
	 * @param error
	 *            Error to handle.
	 * @param showStackTrace
	 *            Set to <code>true</code> if the stack trace should be display in the console or
	 *            <code>false</code> otherwise.
	 * @return a {@link Returns}
	 */
	public static Integer handleDieError(Die error, boolean showStackTrace) {
		final PrintStream stream;
		final Integer returnCode = Returns.ERROR.code();
		final String prefix;
		switch (error.getType()) {
			case ERROR:
				prefix = "error: ";
				stream = System.out;
				break;
			case FATAL:
				prefix = "fatal: ";
				stream = System.out;
				break;
			case SOFTWARE_ERROR:
			default:
				prefix = "software error: ";
				stream = System.err;
				break;
		}
		if (error.getMessage() != null) {
			stream.println(prefix + error.getMessage());
		}
		if (showStackTrace && error.getCause() != null) {
			error.getCause().printStackTrace(stream);
		}

		return returnCode;
	}
}

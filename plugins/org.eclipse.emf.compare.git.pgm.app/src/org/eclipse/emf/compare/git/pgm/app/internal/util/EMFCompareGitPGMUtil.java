package org.eclipse.emf.compare.git.pgm.app.internal.util;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.eclipse.emf.compare.git.pgm.app.Returns;
import org.eclipse.emf.compare.git.pgm.app.internal.exception.Die;

public class EMFCompareGitPGMUtil {

	/**
	 * File separator.
	 */
	public static final String SEP = File.separator;

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

	/**
	 * Returns, from a relative path, the corresponding file with an absolute path. This absolute path is
	 * computed against 'user.dir' system property.
	 * 
	 * @param relativePath
	 *            the relative path for which we want the corresponding file.
	 * @return the corresponding file with an absolute path.
	 */
	public static File toFileWithAbsolutePath(String relativePath) {
		return toFileWithAbsolutePath(System.getProperty("user.dir"), relativePath); //$NON-NLS-1$
	}

	/**
	 * Returns, from a relative path, the corresponding file with an absolute path. This absolute path is
	 * computed against the given base path.
	 * 
	 * @param relativePath
	 *            the relative path for which we want the corresponding file.
	 * @return the corresponding file with an absolute path.
	 */
	public static File toFileWithAbsolutePath(String basePath, String relativePath) {
		File file = new File(relativePath);
		if (!file.isAbsolute()) {
			Path base = FileSystems.getDefault().getPath(basePath);
			Path resolvedPath = base.getParent().resolve(file.toPath());
			Path absolutePath = resolvedPath.normalize();
			file = absolutePath.toFile();
		}
		return file;
	}

}

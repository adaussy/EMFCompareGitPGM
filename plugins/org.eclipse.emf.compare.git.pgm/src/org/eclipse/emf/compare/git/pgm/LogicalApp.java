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
package org.eclipse.emf.compare.git.pgm;

import static org.eclipse.emf.compare.git.pgm.Returns.COMPLETE;
import static org.eclipse.emf.compare.git.pgm.Returns.ERROR;
import static org.eclipse.emf.compare.git.pgm.internal.Options.HELP_OPT;
import static org.eclipse.emf.compare.git.pgm.internal.exception.Die.DeathType.FATAL;

import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.git.pgm.internal.args.LogicalCommandHandler;
import org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand;
import org.eclipse.emf.compare.git.pgm.internal.cmd.CommandFactory;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die;
import org.eclipse.emf.compare.git.pgm.internal.exception.Die.DiesOn;
import org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

/**
 * <h4>Name</h4>
 * <p>
 * logicalApp - Logical Application
 * </p>
 * <h4>Synopsis</h4>
 * <p>
 * logicalapp --help &lt;command&gt; &lt;commandArgs...&gt;
 * <h4>Description</h4>
 * <p>
 * This application is a command line tool handling logical commands. See {@link CommandFactory} for a
 * detailed list of handled commands.
 * </p>
 * <p>
 * This class is the main entrance for the this application.
 * </p>
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class LogicalApp implements IApplication {

	/**
	 * Holds true if the user has requested help for this app.
	 */
	@Option(name = HELP_OPT, usage = "Displays help for this command.", aliases = {"-h" })
	private boolean help;

	/**
	 * Holds the logical command to be run.
	 */
	@Argument(index = 0, metaVar = "cmd", required = true, handler = LogicalCommandHandler.class)
	private AbstractLogicalCommand logicalCommand;

	/**
	 * Other arguments used in logical commands.
	 */
	@Argument(index = 1, metaVar = "args")
	private List<String> arguments = new ArrayList<String>();

	private final URI environmentSetupURI;

	public LogicalApp() {
		this(URI.createPlatformPluginURI("/org.eclipse.emf.compare.git.pgm/model/luna.setup", true)); //$NON-NLS-1$
	}

	public LogicalApp(URI environmentURI) {
		environmentSetupURI = environmentURI;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		// Prevents vm args if the application exits on something different that 0
		System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
		@SuppressWarnings("rawtypes")
		final Map args = context.getArguments();
		String[] appArg = (String[])args.get("application.args"); //$NON-NLS-1$
		if (appArg == null) {
			appArg = new String[] {};
		}
		Object returnCode;
		try {
			returnCode = execute(appArg);
		} catch (Die error) {
			final boolean showStackTrace;
			if (logicalCommand != null) {
				showStackTrace = logicalCommand.isShowStackTrace();
			} else {
				showStackTrace = false;
			}
			return EMFCompareGitPGMUtil.handleDieError(error, showStackTrace);
		}

		if (System.out.checkError() || System.err.checkError()) {
			// TODO improve this
			System.out.println("Unknown error");
			returnCode = ERROR;
		}
		return returnCode;
	}

	/**
	 * Builds and executes the logical command.
	 * 
	 * @param argv
	 *            application arguments.
	 * @return {@link Returns}
	 * @throws Die
	 * @throws IOException
	 */
	private Object execute(final String[] argv) throws Die, IOException {
		final CmdLineParser clp = new CmdLineParser(this);
		try {
			clp.parseArgument(argv);
		} catch (CmdLineException err) {
			// Incorrect arguments
			if (argv.length > 0 && !help) {
				throw new DiesOn(FATAL).displaying(err.getMessage()).ready();
			}
		}
		// User is requiring help.
		if (help) {
			printHelp(clp, new PrintWriter(System.out));
			return COMPLETE;
		}
		// If the user has provided no argument, display usage and and return error code.
		if (argv.length == 0) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter printWritter = new PrintWriter(out);
			printHelp(clp, printWritter);
			printWritter.close();
			throw new DiesOn(FATAL).displaying(out.toString()).ready();
		}

		logicalCommand.build(arguments, environmentSetupURI);

		Object returnCode;
		try {
			// Do not catch exception but flush what was currently writing the command
			returnCode = logicalCommand.execute();
		} finally {
			logicalCommand.flushOutW();
		}
		return returnCode;
	}

	/**
	 * Print the help message to the user.
	 * 
	 * @param clp
	 *            parser
	 * @param printWritter
	 *            {@link PrintWriter}.
	 */
	private void printHelp(CmdLineParser clp, PrintWriter printWritter) {
		final String ex = clp.printExample(ExampleMode.ALL);
		printWritter.println("logicalApp" + ex + " command [ARG ...]"); //$NON-NLS-1$ //$NON-NLS-2$
		if (help) {
			printWritter.println();
			clp.printUsage(printWritter, null);
		}
		if (logicalCommand == null) {
			printWritter.println();
			printWritter.println("Available commands are:");
			List<String> availableCommands = Lists.newArrayList(CommandFactory.getInstance()
					.getAvailableCmd());
			Collections.sort(availableCommands);
			for (String cmdName : availableCommands) {
				printWritter.println(cmdName);
			}
		}
		printWritter.flush();
	}

	// For testing purpose.
	AbstractLogicalCommand getLogicalCommand() {
		return logicalCommand;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}

}

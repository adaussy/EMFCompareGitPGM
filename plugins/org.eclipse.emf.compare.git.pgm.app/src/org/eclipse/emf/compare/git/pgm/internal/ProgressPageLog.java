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
package org.eclipse.emf.compare.git.pgm.internal;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.oomph.setup.SetupTask;
import org.eclipse.oomph.setup.log.ProgressLog;
import org.eclipse.oomph.util.OomphPlugin;

/**
 * Specific {@link ProgressLog}.
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
public class ProgressPageLog implements ProgressLog {

	private final PrintStream out;

	public ProgressPageLog(PrintStream out) {
		super();
		this.out = out;
	}

	/**
	 * {@inheritDoc}.
	 */
	public boolean isCanceled() {
		return false;
	}

	/**
	 * {@inheritDoc}.
	 */
	public void log(String line) {
		out.println(line);
	}

	/**
	 * {@inheritDoc}.
	 */
	public void log(String line, boolean filter) {
		/*
		 * No documentation is available on the filter parameter. However empirical tests show that filter is
		 * set to false when logging IStatus or exceptions. In our case we do not want to show that kind of
		 * information on the progress page log. It will be displayed later on the application is the
		 * --show-stack-trace option is set to true.
		 */
		if (filter) {
			out.println(line);
		}
	}

	/**
	 * {@inheritDoc}.
	 */
	public void log(IStatus status) {
		String string = OomphPlugin.toString(status);
		log(string, false);
	}

	/**
	 * {@inheritDoc}.
	 */
	public void log(Throwable t) {
		String string = OomphPlugin.toString(t);
		log(string, false);
	}

	/**
	 * {@inheritDoc}.
	 */
	public void task(SetupTask setupTask) {
	}

	public void setTerminating() {

	}

}

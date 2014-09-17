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
package org.eclipse.emf.compare.git.pgm.util;

import com.google.common.collect.ObjectArrays;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

/**
 * Mock an {@link IApplicationContext}.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public class MockedApplicationContext implements IApplicationContext {

	private static final String ARGS_KEY = "application.args"; //$NON-NLS-1$

	private Map<String, Object> arguments = new HashMap<String, Object>();

	public Map getArguments() {
		return arguments;
	}

	public void addArg(String... args) {
		Object commandLineArgs = arguments.get(ARGS_KEY);
		if (commandLineArgs == null) {
			arguments.put(ARGS_KEY, args);
		} else {
			arguments.put(ARGS_KEY, ObjectArrays.concat((String[])commandLineArgs, args, String.class));
		}

	}

	public void applicationRunning() {
	}

	public String getBrandingApplication() {
		return null;
	}

	public String getBrandingName() {
		return null;
	}

	public String getBrandingDescription() {
		return null;
	}

	public String getBrandingId() {
		return null;
	}

	public String getBrandingProperty(String key) {
		return null;
	}

	public Bundle getBrandingBundle() {
		return null;
	}

	public void setResult(Object result, IApplication application) {

	}

}

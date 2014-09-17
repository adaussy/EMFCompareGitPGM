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

import org.eclipse.emf.compare.git.pgm.internal.cmd.AbstractLogicalCommand;
import org.eclipse.equinox.app.IApplication;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public abstract class AbstractLogicalAppTest extends AbstractApplicationTest {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.AbstractApplicationTest#buildApp()
	 */
	@Override
	protected IApplication buildApp() {
		return new LogicalApp();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.git.pgm.AbstractApplicationTest#getApp()
	 */
	@Override
	protected LogicalApp getApp() {
		return (LogicalApp)super.getApp();
	}

	protected AbstractLogicalCommand getLogicalCommand() {
		return getApp().getLogicalCommand();
	}
}

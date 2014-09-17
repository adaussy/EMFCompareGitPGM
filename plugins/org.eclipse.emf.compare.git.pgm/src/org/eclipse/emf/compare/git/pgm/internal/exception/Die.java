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
package org.eclipse.emf.compare.git.pgm.internal.exception;

/**
 * Exception to be raised on error in this application.
 * 
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
public final class Die extends Exception {
	/**
	 * .
	 */
	private static final long serialVersionUID = 1907077670315070835L;

	/**
	 * Type of death of the application.
	 * 
	 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
	 */
	public enum DeathType {
		/**
		 * Major error the software stopped somewhere it should not.
		 */
		SOFTWARE_ERROR,
		/**
		 * Program terminated on a error.
		 */
		ERROR,
		/**
		 * Program terminated on a error which is prefixed with fatal.
		 */
		FATAL,
	}

	/**
	 * Type of death.
	 * 
	 * @see DeathType
	 */
	private final DeathType type;

	/**
	 * Private constructor.
	 * 
	 * @param type
	 *            Type of death
	 * @param message
	 *            Message displayed to the user.
	 * @param cause
	 *            Cause of the death.
	 * @see DiesOn
	 */
	private Die(DeathType type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	/**
	 * Private constructor.
	 * 
	 * @param type
	 *            Type of death
	 * @param cause
	 *            Cause of the death.
	 * @see DiesOn
	 */
	private Die(DeathType type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	/**
	 * Get the type of death.
	 * 
	 * @return {@link DeathType}
	 */
	public DeathType getType() {
		return type;
	}

	/**
	 * Facilities used to create understandable exceptions.
	 * 
	 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
	 */
	public static class DiesOn {

		/**
		 * Type of the death.
		 */
		private final DeathType type;

		/**
		 * Message to display to the user.
		 */
		private String message;

		/**
		 * Cause of the death.
		 */
		private Throwable cause;

		/**
		 * Constructor.
		 * 
		 * @param deathType
		 *            type of the death.
		 */
		public DiesOn(DeathType deathType) {
			this.type = deathType;
		}

		/**
		 * Sets the cause of the death.
		 * 
		 * @param aCause
		 *            .
		 * @return this.
		 */
		public DiesOn duedTo(Throwable aCause) {
			this.cause = aCause;
			return this;
		}

		/**
		 * Sets the message to be displayed to the user.
		 * 
		 * @param aMessage
		 *            message to display.
		 * @return this.
		 */
		public DiesOn displaying(String aMessage) {
			this.message = aMessage;
			return this;
		}

		/**
		 * Declares this exception ready to be thrown.
		 * 
		 * @return a newly created {@link Die}.
		 */
		public Die ready() {
			if (message == null) {
				return new Die(type, cause);
			} else {
				return new Die(type, message, cause);
			}
		}

	}

}

/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.exceptions;

import org.culturegraph.mf.morph.MorphBuilder;


/**
 * Thrown by {@link MorphBuilder} if the definition is syntactically incorrect or components for dynamic loading are not found.
 *
 * @author Markus Michael Geipel
 *
 */
public final class MorphDefException extends MetafactureException {

	private static final long serialVersionUID = -3130648074493084946L;

	/**
	 * @param message
	 */
	public MorphDefException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MorphDefException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MorphDefException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

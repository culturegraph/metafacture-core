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
package org.culturegraph.mf.stream.converter.bib;

import org.culturegraph.mf.exceptions.MetafactureException;

/**
 * Thrown by {@link Reader}s if a record has no ID
 *
 * @author Markus Michael Geipel
 *
 */
public final class MissingIdException extends MetafactureException {

	private static final long serialVersionUID = 2048460214057525724L;

	public MissingIdException(final String message) {
		super(message);
	}

	public MissingIdException(final Throwable cause) {
		super(cause);
	}

	public MissingIdException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

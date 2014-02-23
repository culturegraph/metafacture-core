/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.converter;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * Serialises an object as JSON. Records and entities are represented
 * as objects unless their name ends with []. If the name ends with [],
 * an array is created.
 * 
 * @author Christoph Böhme
 *
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(String.class)
public final class JsonEncoder extends
		DefaultStreamPipe<ObjectReceiver<String>> {

	public static final String ARRAY_MARKER = "[]";

	private final JsonGenerator jsonGenerator;
	private final StringWriter writer = new StringWriter();
	
	public JsonEncoder() {
		try {
			jsonGenerator = new JsonFactory().createGenerator(writer);
			jsonGenerator.setRootValueSeparator(null);
		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}
	
	@Override
	public void startRecord(final String id) {
		final StringBuffer buffer = writer.getBuffer();
		buffer.delete(0, buffer.length());
		startGroup(id);
	}
	
	@Override
	public void endRecord() {
		endGroup();
		try {
			jsonGenerator.flush();
		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
		getReceiver().process(writer.toString());
	}
	
	@Override
	public void startEntity(final String name) {
		startGroup(name);
	}
	
	@Override
	public void endEntity() {
		endGroup();
	}
	
	@Override
	public void literal(final String name, final String value) {
		try {
			final JsonStreamContext ctx = jsonGenerator.getOutputContext();
			if (ctx.inObject()) {
				jsonGenerator.writeFieldName(name);
			}
			if (value == null) {
				jsonGenerator.writeNull();
			} else {
				jsonGenerator.writeString(value);
			}
		} catch (final JsonGenerationException e) {
			throw new MetafactureException(e);
		}
		catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}
	
	/**
	 * Enabling a pretty print JSON output.
	 */
	public void usePrettyPrint() {
		jsonGenerator.useDefaultPrettyPrinter();
	}

	/**
	 * All JSON output does only have escaping where it is <li>really</li> necessary.
	 * This is recommended in the most cases. Nevertheless it can be useful to
	 * get even more escaping. With this method it is possible to use
	 * {@link StringEscapeUtils#escapeJavaScript(String)} for escaping instead.
	 * 
	 * @param escapeCharacters
	 *            This is an array which defines which characters should be
	 *            escaped and how it will be done. See {@link CharacterEscapes}.
	 *            In most cases this should be null. Usage: <pre>{@code
	 * 	int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
	 * 	// and force escaping of a few others:
	 * 	esc['\''] = CharacterEscapes.ESCAPE_STANDARD;
	 * JsonEncoder.useEscapeJavaScript(esc);
	 * }</pre>
	 */
	public void useEscapeJavaScript(final int[] escapeCharacters) {
		CharacterEscapes ce = new CharacterEscapes() {

			private static final long serialVersionUID = 1L;

			@Override
			public int[] getEscapeCodesForAscii() {
				if (escapeCharacters == null) {
					return CharacterEscapes.standardAsciiEscapesForJSON();
				}

				return escapeCharacters;
			}

			@Override
			public SerializableString getEscapeSequence(int ch) {
				String c = Character.toString((char) ch);
				String s = StringEscapeUtils.escapeJavaScript(c);
				return new SerializedString(s);
			}

		};
		jsonGenerator.setCharacterEscapes(ce);
	}
	
	private void startGroup(final String name) {
		try {
			final JsonStreamContext ctx = jsonGenerator.getOutputContext();
			if (name.endsWith(ARRAY_MARKER)) {
				if (ctx.inObject()) {
					jsonGenerator.writeFieldName(name.substring(0, name.length() - ARRAY_MARKER.length()));
				}
				jsonGenerator.writeStartArray();
			} else {
				if (ctx.inObject()) {
					jsonGenerator.writeFieldName(name);
				}
				jsonGenerator.writeStartObject();
			}
		} catch (final JsonGenerationException e) {
			throw new MetafactureException(e);
		}
		catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}
	
	private void endGroup() {
		try {
			final JsonStreamContext ctx = jsonGenerator.getOutputContext();
			if (ctx.inObject()) {
				jsonGenerator.writeEndObject();
			} else if (ctx.inArray()) {
				jsonGenerator.writeEndArray();
			}
		} catch (final JsonGenerationException e) {
			throw new MetafactureException(e);
		}
		catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}
	
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.formeta.formatter.ConciseFormatter;
import org.culturegraph.mf.formeta.formatter.Formatter;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Triple;
import org.culturegraph.mf.types.Triple.ObjectType;
import org.culturegraph.mf.util.StreamConstants;

/**
 * 
 * @author Markus Michael Geipel
 * 
 */
@Description("Takes literals from a stream and emits them as triples such "
		+ "that the name and value become predicate and object and the record id the subject. "
		+ "If 'redirect' is true, use '_id' to change the id, or '{to:ID}NAME' to change the id of a single literal. "
		+ "Set 'recordPredicate' to encode a complete record in one triple. The value of 'recordPredicate' is used "
		+ "as the predicate of the triple. If 'recordPredicate' is set, no {to:ID}NAME-style redirects are possible.")
@In(StreamReceiver.class)
@Out(Triple.class)
public final class StreamToTriples extends DefaultStreamPipe<ObjectReceiver<Triple>> {

	private static final Pattern REDIRECT_PATTERN = Pattern.compile("^\\{to:(.+)}(.+)$");

	private final List<String> nameBuffer = new ArrayList<String>();
	private final List<String> valueBuffer = new ArrayList<String>();
	private final Formatter formatter = new ConciseFormatter();

	private boolean redirect;
	private String recordPredicate;
	
	private int nestingLevel;
	private int encodeLevel;
	private String predicateName;
	private String currentId;

	public boolean isRedirect() {
		return redirect;
	}
	
	public void setRedirect(final boolean redirect) {
		this.redirect = redirect;
	}
	
	public String getRecordPredicate() {
		return recordPredicate;
	}
	
	public void setRecordPredicate(final String recordPredicate) {
		this.recordPredicate = recordPredicate;
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		
		currentId = identifier;
		
		if (recordPredicate != null) {
			encodeLevel = 0;
			startEncode(recordPredicate);
		} else {
			encodeLevel = 1;
		}
		
		nestingLevel = 1;
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		
		nestingLevel = 0;
		
		if (nestingLevel == encodeLevel) {
			endEncode();
		}
		
		if (redirect) {
			for (int i = 0; i < nameBuffer.size(); ++i) {
				getReceiver().process(new Triple(currentId, nameBuffer.get(i), valueBuffer.get(i)));
			}
			nameBuffer.clear();
			valueBuffer.clear();
		}
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		
		if (nestingLevel > encodeLevel) {
			formatter.startGroup(name);
		} else {
			startEncode(name);
		}
		++nestingLevel;
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		
		--nestingLevel;
		if (nestingLevel == encodeLevel) {
			endEncode();
		} else {
			formatter.endGroup();
		}
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		
		if (nestingLevel > encodeLevel) {
			if (nestingLevel == 1 && redirect && StreamConstants.ID.equals(name)) {
				currentId = value;
			} else {
				formatter.literal(name, value);
			}
		} else {
			dispatch(name, value, ObjectType.STRING);
		}
	}
	
	private void startEncode(final String predicate) {
		predicateName = predicate;
		formatter.reset();
		formatter.startGroup("");		
	}
	
	private void endEncode() {
		formatter.endGroup();
		dispatch(predicateName, formatter.toString(), ObjectType.ENTITY);
	}

	private void dispatch(final String name, final String value, final ObjectType type) {
		if (redirect) {
			if (StreamConstants.ID.equals(name)) {
				currentId = value;
			} else {
				final Matcher matcher = REDIRECT_PATTERN.matcher(name);
				if (matcher.find()) {
					getReceiver().process(new Triple(matcher.group(1), matcher.group(2), value, type));
				} else {
					nameBuffer.add(name);
					valueBuffer.add(value);
				}
			}
		} else {
			getReceiver().process(new Triple(currentId, name, value, type));
		}
	}

}

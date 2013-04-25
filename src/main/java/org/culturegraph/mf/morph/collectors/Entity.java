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
package org.culturegraph.mf.morph.collectors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.NamedValueReceiver;
import org.culturegraph.mf.morph.NamedValueSource;
import org.culturegraph.mf.stream.pipe.StreamBuffer;
import org.culturegraph.mf.util.StringUtil;



/**
 * Corresponds to the <code>&lt;entity&gt;</code> tag.
 * 
 * @author Markus Michael Geipel
 */
public final class Entity extends AbstractCollect {
	// private static final Logger LOG = LoggerFactory.getLogger(Entity.class);

	// public static final String ENTITY_NAME = "_entity_name";

	private final List<NamedValueSource> sourceList = new ArrayList<NamedValueSource>();
	private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();
	private final StreamBuffer buffer = new StreamBuffer();
	
	private NamedValueSource nameSource;
	private String currentName;

	public Entity(final Metamorph metamorph) {
		super(metamorph);
	}

	public void setNameSource(final NamedValueSource nameSource) {
		this.nameSource = nameSource;
	}

	@Override
	protected void emit() {
		final NamedValueReceiver namedValueReceiver = getNamedValueReceiver();
		if (namedValueReceiver instanceof Entity) {
			final Entity parent = (Entity) namedValueReceiver;
			parent.receive(null, null, this, getRecordCount(), getEntityCount());
		} else {
			write(getMetamorph().getStreamReceiver());
		}
	}

	private void write(final StreamReceiver receiver) {
		if (!buffer.isEmpty()) {
			
			receiver.startEntity(StringUtil.fallback(currentName, getName()));
			buffer.setReceiver(receiver);
			buffer.replay();
			receiver.endEntity();
			
			buffer.clear();
		}
		
	}

	@Override
	protected void receive(final String name, final String value, final NamedValueSource source) {
		if (source == nameSource) {
			currentName = value;
		} else if (source instanceof Entity) {
			final Entity child = (Entity) source;
			child.write(buffer);
		}else{
			buffer.literal(name, value);
		}
		sourcesLeft.remove(source);
	}

	@Override
	protected boolean isComplete() {
		return sourcesLeft.isEmpty();
	}

	@Override
	protected void clear() {
		sourcesLeft.addAll(sourceList);
		buffer.clear();
		currentName = null;
	}

	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
		sourceList.add(namedValueSource);
		sourcesLeft.add(namedValueSource);
	}
	
}

/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.morph.functions;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.morph.AbstractNamedValuePipe;
import org.culturegraph.mf.types.MultiMap;

/**
 * Base class for functions.
 *
 * @author Markus Michael Geipel
 */
public abstract class AbstractFunction extends AbstractNamedValuePipe
		implements Function {

	private MultiMap multiMap;
	private Map<String, String> localMap;
	private String mapName;

	protected final String getValue(final String mapName, final String key) {
		return multiMap.getValue(mapName, key);
	}

	protected final String getLocalValue(final String key) {
		if (localMap != null) {
			return localMap.get(key);
		}
		return null;
	}

	public final String getMapName() {
		return mapName;
	}

	public final Map<String, String> getMap() {
		if (localMap == null) {
			return multiMap.getMap(mapName);
		}
		return localMap;
	}

	public final void setMap(final String mapName) {
		this.mapName = mapName;
	}

	@Override
	public final void putValue(final String key, final String value) {
		if (localMap == null) {
			localMap = new HashMap<String, String>();
		}
		localMap.put(key, value);
	}

	@Override
	public final void setMultiMap(final MultiMap multiMapProvider) {
		this.multiMap = multiMapProvider;
	}

	@Override
	public void flush(final int recordCount, final int entityCount) {
		// Does nothing by default
	}

}

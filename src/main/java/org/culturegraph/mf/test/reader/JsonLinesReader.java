/*
 * Copyright 2017 hbz
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
package org.culturegraph.mf.test.reader;

import org.culturegraph.mf.io.LineReader;
import org.culturegraph.mf.json.JsonDecoder;

/**
 * Reads and decodes a <i>JSON Lines</i> data stream.
 *
 * @author Jens Wille
 *
 */
public class JsonLinesReader extends ReaderBase {

	public JsonLinesReader() {
		super(new LineReader(), new JsonDecoder());
	}

}

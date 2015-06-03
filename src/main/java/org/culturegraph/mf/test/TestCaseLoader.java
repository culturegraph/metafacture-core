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
package org.culturegraph.mf.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public final class TestCaseLoader {

	/**
	 *
	 */
	private static final String FILE_NOT_FOUND = "Could not find test case file: ";

	private static final String SCHEMA_FILE = "schemata/metamorph-test.xsd";

	private static final String TEST_CASE_TAG = "test-case";

	private TestCaseLoader() {
		// No instances allowed
	}

	public static List<TestCase> load(final URL testDef) {
		final InputSource inputSource = new InputSource(testDef.toExternalForm());
		return load(inputSource);
	}

	//TODO: seems to be unused
	public static List<TestCase> load(final String testDef) {
		final InputSource inputSource = new InputSource(testDef);
		return load(inputSource);
	}

	//TODO seems to be unused
	public static List<TestCase> load(final File testDefFile) {
		try {
			final InputSource inputSource =
					new InputSource(testDefFile.toURI().toURL().toExternalForm());
			return load(inputSource);
		} catch (final MalformedURLException e) {
			throw new TestConfigurationException(FILE_NOT_FOUND, e);
		}
	}

	//TODO seems to be unused
	public static List<TestCase> load(final InputStream inputStream) {
		return load(new InputSource(inputStream));
	}

	public static List<TestCase> load(final InputSource inputSource) {

		try {
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(SCHEMA_FILE);
			final Schema schema = schemaFactory.newSchema(schemaUrl);

			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setIgnoringElementContentWhitespace(true);
			builderFactory.setIgnoringComments(true);
			builderFactory.setNamespaceAware(true);
			builderFactory.setCoalescing(true);
			builderFactory.setSchema(schema);

			final DocumentBuilder builder = builderFactory.newDocumentBuilder();
			final Document doc = builder.parse(inputSource);

			final List<TestCase> testCases = new ArrayList<TestCase>();
			final NodeList testCaseNodes = doc.getElementsByTagName(TEST_CASE_TAG);
			for(int i=0; i < testCaseNodes.getLength(); ++i) {
				final Element testCaseElement = (Element) testCaseNodes.item(i);
				testCases.add(new TestCase(testCaseElement));
			}

			return testCases;

		} catch (final ParserConfigurationException e) {
			throw new TestConfigurationException("Parser configuration failed", e);
		} catch (final SAXException e) {
			throw new TestConfigurationException("Parser error", e);
		} catch (final IOException e) {
			throw new TestConfigurationException("Error while reading file", e);
		}
	}
}


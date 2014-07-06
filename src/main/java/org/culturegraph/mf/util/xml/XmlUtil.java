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
package org.culturegraph.mf.util.xml;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.culturegraph.mf.exceptions.ShouldNeverHappenException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public final class XmlUtil {
	
	private static final String APPLICATION_XML_MIME_TYPE = "application/xml";
	private static final String TEXT_XML_MIME_TYPE = "text/xml";
	private static final String XML_BASE_MIME_TYPE = "+xml";
	
	private XmlUtil() {
		// No instances allowed
	}
	
	public static String nodeToString(final Node node) {
		return nodeToString(node, false);
	}
	
	public static String nodeToString(final Node node, final boolean omitXMLDecl) {
		final StringWriter writer = new StringWriter();
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerException e) {
			throw new ShouldNeverHappenException(e);
		}
		
		if (omitXMLDecl) {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} else {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");			
		}
		
		try {
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new ShouldNeverHappenException(e);
		}
		
		return writer.toString();
	}

	public static String nodeListToString(final NodeList nodes) {
		final StringBuilder builder = new StringBuilder();
		
		for (int i=0; i < nodes.getLength(); ++i) {
			builder.append(nodeToString(nodes.item(i), i != 0));
		}
		
		return builder.toString();
	}
	
	public static boolean isXmlMimeType(final String mimeType) {
		if (mimeType == null) {
			return false;
		}
		return APPLICATION_XML_MIME_TYPE.equals(mimeType) || 
				TEXT_XML_MIME_TYPE.equals(mimeType) || 
				mimeType.endsWith(XML_BASE_MIME_TYPE);
	}
}

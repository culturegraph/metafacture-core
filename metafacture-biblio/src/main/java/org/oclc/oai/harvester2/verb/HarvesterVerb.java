/**
 * Copyright 2006 OCLC, Online Computer Library Center Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.oclc.oai.harvester2.verb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

//import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb {

    private static final Logger log = LoggerFactory.getLogger(HarvesterVerb.class);

    /* Primary OAI namespaces */
    public static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    public static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
    public static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";
    private Document doc = null;
    private String schemaLocation = null;
    private String requestURL = null;

    private static final ThreadLocal<DocumentBuilderFactory> builderFactory = new ThreadLocal<DocumentBuilderFactory>() {
        @Override
        public DocumentBuilderFactory initialValue() {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory;
        }
    };
    private static final ThreadLocal<TransformerFactory> transformerFactory = new ThreadLocal<TransformerFactory>() {
        @Override
        public TransformerFactory initialValue() {
            return TransformerFactory.newInstance();
        }
    };

    private static final ThreadLocal<XPath> xpath = new ThreadLocal<XPath>() {
        @Override
        public XPath initialValue() {
            XPathFactory fucktory = XPathFactory.newInstance();
            XPath xpath = fucktory.newXPath();
            try {
                xpath.setNamespaceContext(new OAINamespaceContext(builderFactory.get()));
            } catch (ParserConfigurationException e) {
                log.error("Cannot set namespace context", e);
            }
            return xpath;
        }
    };

    /**
     * Get the OAI response as a DOM object
     *
     * @return the DOM for the OAI response
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Get the xsi:schemaLocation for the OAI response
     *
     * @return the xsi:schemaLocation value
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * Get the OAI errors
     *
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     * @throws XPathExpressionException
     */
    public NodeList getErrors() throws XPathExpressionException {
        if (SCHEMA_LOCATION_V2_0.equals(getSchemaLocation())) {
            return getNodeList("/oai20:OAI-PMH/oai20:error");
        } else {
            return null;
        }
    }

    /**
     * Get the OAI request URL for this response
     *
     * @return the OAI request URL as a String
     */
    public String getRequestURL() {
        return requestURL;
    }

    /**
     * Mock object creator (for unit testing purposes)
     */
    public HarvesterVerb() {
    }

    /**
     * Performs the OAI request
     *
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public HarvesterVerb(String requestURL) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException {
        harvest(requestURL);
    }

    /**
     * Preforms the OAI request
     *
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    private void harvest(String requestURL) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException {
        this.requestURL = requestURL;
        log.debug("requestURL=" + requestURL);
        InputStream in;
        URL url = new URL(requestURL);
        HttpURLConnection con = null;
        int responseCode;
        do {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "OAIHarvester/2.0");
            con.setRequestProperty("Accept-Encoding",
                    "compress, gzip, identify");
            try {
                responseCode = con.getResponseCode();
                log.debug("responseCode=" + responseCode);
            } catch (FileNotFoundException e) {
                // assume it's a 503 response
                log.info(requestURL, e);
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
            }

            if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                long retrySeconds = con.getHeaderFieldInt("Retry-After", -1);
                if (retrySeconds == -1) {
                    long now = (new Date()).getTime();
                    long retryDate = con.getHeaderFieldDate("Retry-After", now);
                    retrySeconds = retryDate - now;
                }
                if (retrySeconds == 0) { // Apparently, it's a bad URL
                    throw new FileNotFoundException("Bad URL?");
                }
                System.err.println("Server response: Retry-After=" + retrySeconds);
                if (retrySeconds > 0) {
                    try {
                        Thread.sleep(retrySeconds * 1000);
                    } catch (InterruptedException ex) {
                        log.error("Error while sleeping", ex);
                    }
                }
            }
        } while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE);
        String contentEncoding = con.getHeaderField("Content-Encoding");
        log.debug("contentEncoding=" + contentEncoding);
        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(con.getInputStream());
            zis.getNextEntry();
            in = zis;
        } else if ("gzip".equals(contentEncoding)) {
            in = new GZIPInputStream(con.getInputStream());
        } else if ("deflate".equals(contentEncoding)) {
            in = new InflaterInputStream(con.getInputStream());
        } else {
            in = con.getInputStream();
        }

        InputSource data = new InputSource(in);

        doc = builderFactory.get().newDocumentBuilder().parse(data);

        // The URIs in xsi:schemaLocation are separated by (any kind
        // of) white space. Normalize it to a single space.
        String schemaLoc = getSingleString("/*/@xsi:schemaLocation");
        this.schemaLocation = schemaLoc.trim().replaceAll("\\s+", " ");
    }

    /**
     * Get the String value for the given XPath location in the response DOM
     *
     * @param xpath
     * @return a String containing the value of the XPath location.
     * @throws XPathExpressionException
     */
    public String getSingleString(String xpath) throws XPathExpressionException {
        return getSingleString(getDocument(), xpath);
//        return XPathAPI.eval(getDocument(), xpath, namespaceElement).str();
//      String str = null;
//      Node node = XPathAPI.selectSingleNode(getDocument(), xpath,
//      namespaceElement);
//      if (node != null) {
//      XObject xObject = XPathAPI.eval(node, "string()");
//      str = xObject.str();
//      }
//      return str;
    }

    public String getSingleString(Node node, String expression) throws XPathExpressionException {
        return xpath.get().evaluate(expression, node);
    }

    /**
     * Get a NodeList containing the nodes in the response DOM for the specified xpath
     *
     * @param expression
     * @return the NodeList for the xpath into the response DOM
     * @throws XPathExpressionException
     */
    public NodeList getNodeList(String expression) throws XPathExpressionException {
        return (NodeList) xpath.get().evaluate(expression, getDocument(), XPathConstants.NODESET);
    }

    @Override
    public String toString() {
        // Element docEl = getDocument().getDocumentElement();
        // return docEl.toString();
        Source input = new DOMSource(getDocument());
        StringWriter sw = new StringWriter();
        Result output = new StreamResult(sw);
        try {
            Transformer idTransformer = transformerFactory.get().newTransformer();
            idTransformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes");
            idTransformer.transform(input, output);
            return sw.toString();
        } catch (TransformerException e) {
            return e.getMessage();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oclc.oai.harvester2.verb;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Michal Hlavac (michal_hlavac@datalan.sk)
 */
public class OAINamespaceContext implements NamespaceContext {

    private final Element namespaceElement;

    public OAINamespaceContext(DocumentBuilderFactory factory) throws ParserConfigurationException {
        this.namespaceElement = buildNamespaceElement(factory);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaceElement.lookupNamespaceURI(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }

    private Element buildNamespaceElement(DocumentBuilderFactory factory) throws ParserConfigurationException {
        DOMImplementation impl = factory.newDocumentBuilder().getDOMImplementation();
        Document namespaceHolder = impl.createDocument(
                "http://www.oclc.org/research/software/oai/harvester",
                "harvester:namespaceHolder", null);

        Element el = namespaceHolder.getDocumentElement();
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:harvester", "http://www.oclc.org/research/software/oai/harvester");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai20", "http://www.openarchives.org/OAI/2.0/");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_GetRecord", "http://www.openarchives.org/OAI/1.1/OAI_GetRecord");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_Identify", "http://www.openarchives.org/OAI/1.1/OAI_Identify");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_ListIdentifiers", "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_ListMetadataFormats", "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_ListRecords", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords");
        el.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oai11_ListSets", "http://www.openarchives.org/OAI/1.1/OAI_ListSets");
        return el;
    }
}

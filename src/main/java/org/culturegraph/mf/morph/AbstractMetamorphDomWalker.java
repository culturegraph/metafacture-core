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
package org.culturegraph.mf.morph;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.morph.collectors.CollectFactory;
import org.culturegraph.mf.morph.functions.FunctionFactory;
import org.culturegraph.mf.morph.maps.MapFactory;
import org.culturegraph.mf.types.ScopedHashMap;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * Builds a {@link Metamorph} from an xml description
 * 
 * @author Markus Michael Geipel
 */
public abstract class AbstractMetamorphDomWalker {

	/**
	 * XML tags
	 */
	public static enum MMTAG {
		META, FUNCTIONS, RULES, MACROS, MACRO, MAPS, ENTITY, MAP, ENTRY, TEXT, VARS
	}

	/**
	 * XML attributes
	 */
	public static enum ATTRITBUTE {
		VERSION("version"), 
		SOURCE("source"), 
		VALUE("value"), 
		NAME("name"),
		CLASS("class"), 
		DEFAULT("default"), 
		ENTITY_MARKER("entityMarker"), 
		FLUSH_WITH("flushWith");

		private final String string;

		private ATTRITBUTE(final String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}
	}

	private static final String DATA = "data";
	private static final String MAP = "map";
	private static final String MACRO = "call-macro";
	private static final String POSTPROCESS = "postprocess";
	private static final String ENTITY_NAME = "entity-name";
	private static final String SCHEMA_FILE = "schemata/metamorph.xsd";
	private static final int LOWEST_COMPATIBLE_VERSION = 1;
	private static final int CURRENT_VERSION = 1;

	private FunctionFactory functionFactory;
	private CollectFactory collectFactory;
	private MapFactory mapFactory;

	private final Map<String, Node> macros = new HashMap<String, Node>();
	private ScopedHashMap<String, String> vars = new ScopedHashMap<String, String>();
	private boolean ignoreMissingVars;

	protected final FunctionFactory getFunctionFactory() {
		return functionFactory;
	}

	protected final CollectFactory getCollectFactory() {
		return collectFactory;
	}

	protected final MapFactory getMapFactory() {
		return mapFactory;
	}

	public final void walk(final String morphDef) {
		try {
			walk(ResourceUtil.getStream(morphDef));
		} catch (FileNotFoundException e) {
			throw new MorphDefException(e);
		}
	}

	public final void walk(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("'inputStream' must not be null");
		}
		walk(DomLoader.parse(SCHEMA_FILE, new InputSource(inputStream)));
	}

	public final void walk(final Reader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("'reader' must not be null");
		}
		walk(DomLoader.parse(SCHEMA_FILE, new InputSource(reader)));
	}
	
	public final void walk(final Reader morphDefReader, final Map<String, String> vars) {
		this.vars.putAll(vars);
		walk(morphDefReader);
	}


	public final void walk(final String morphDef, final Map<String, String> vars) {
		this.vars.putAll(vars);
		walk(morphDef);
	}
	
	public final void walk(final InputStream inputStream, final Map<String, String> vars) {
		this.vars.putAll(vars);
		walk(inputStream);
	}

	private static MMTAG tagOf(final Node child) {
		return MMTAG.valueOf(child.getLocalName().toUpperCase());
	}

	protected static String attribute(final Node node, final ATTRITBUTE attr) {
		final Node attribute = node.getAttributes().getNamedItem(attr.getString());
		if (attribute != null) {
			return attribute.getNodeValue();
		}
		return null;
	}

	protected static Map<String, String> attributeMap(final Node node) {
		final Map<String, String> attributes = new HashMap<String, String>();
		final NamedNodeMap attrNode = node.getAttributes();

		for (int i = 0; i < attrNode.getLength(); ++i) {
			final Node itemNode = attrNode.item(i);
			attributes.put(itemNode.getLocalName(), itemNode.getNodeValue());
		}
		return attributes;
	}
	
	protected  final String resolveVars(final String string){
		return StringUtil.format(string, Metamorph.VAR_START, Metamorph.VAR_END, ignoreMissingVars, vars);
	}
	
	protected final void setIgnoreMissingVars(final boolean ignoreMissingVars) {
		this.ignoreMissingVars = ignoreMissingVars;
	}
	
	protected final String resolvedAttribute(final Node node, final ATTRITBUTE attr){
		final String value = attribute(node, attr);
		if(null==value){
			return null;
		}
		return resolveVars(value);
		
	}
	
	protected  final Map<String, String> resolvedAttributeMap(final Node node){
		final Map<String, String> attributes = new HashMap<String, String>();
		final NamedNodeMap attrNode = node.getAttributes();

		for (int i = 0; i < attrNode.getLength(); ++i) {
			final Node itemNode = attrNode.item(i);
			attributes.put(itemNode.getLocalName(), resolveVars(itemNode.getNodeValue()));
		}
		return attributes;
	}
	
	private void handleVars(final Node varsNode) {
		for (Node varNode = varsNode.getFirstChild(); varNode != null; varNode = varNode.getNextSibling()) {
			final String varName = attribute(varNode, ATTRITBUTE.NAME);
			final String varValue = attribute(varNode, ATTRITBUTE.VALUE);
			vars.put(varName, varValue);
		}
		vars = new ScopedHashMap<String, String>(vars);
	}

	protected final void walk(final Document doc) {
		functionFactory = new FunctionFactory();
		collectFactory = new CollectFactory();
		mapFactory = new MapFactory();

		init();

		final Element root = doc.getDocumentElement();
		final int version = Integer.parseInt(attribute(root, ATTRITBUTE.VERSION));
		checkVersionCompatibility(version);

		setEntityMarker(attribute(root, ATTRITBUTE.ENTITY_MARKER));

		for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {

			switch (tagOf(node)) {
			case META:
				handleMeta(node);
				break;
			case FUNCTIONS:
				handleFunctionDefinitions(node);
				break;
			case RULES:
				handleRules(node);
				break;
			case MAPS:
				handleMaps(node);
				break;
			case VARS:
				handleVars(node);
				break;
			case MACROS:
				handleMacros(node);
				break;
			default:
				illegalChild(node);
			}
		}
		finish();
	}

	private void handleMacros(final Node node) {
		for (Node macroNode = node.getFirstChild(); macroNode != null; macroNode = macroNode.getNextSibling()) {
			final String name = attribute(macroNode, ATTRITBUTE.NAME);
			macros.put(name, macroNode);
		}
	}

	protected abstract void init();

	protected abstract void finish();

	protected abstract void setEntityMarker(final String entityMarker);

	protected abstract void handleInternalMap(final Node mapNode);

	protected abstract void handleMapClass(final Node mapNode);

	protected abstract void handleMetaEntry(final String name, final String value);

	protected abstract void handleFunctionDefinition(final Node functionDefNode);

	protected abstract void enterData(Node node);

	protected abstract void exitData(Node node);

	protected abstract void enterCollect(Node node);

	protected abstract void exitCollect(Node node);

	protected abstract void enterName(Node node);	
	
	protected abstract void exitName(Node node);

	protected abstract void handleFunction(Node functionNode);

	private void handleRule(final Node node) {
		final String nodeName = node.getLocalName();
		if (getCollectFactory().containsKey(nodeName)) {
			enterCollect(node);
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (POSTPROCESS.equals(child.getLocalName())) {
					handlePostprocess(child);
				} else if (ENTITY_NAME.equals(child.getLocalName())) {
					enterName(child);
					handleRule(child.getFirstChild());
					exitName(child);
				} else {
					handleRule(child);
				}
			}
			exitCollect(node);
		} else if (DATA.equals(nodeName)) {
			enterData(node);
			handlePostprocess(node);
			exitData(node);
		} else if (MACRO.equals(nodeName)){
			final String macroName = attribute(node, ATTRITBUTE.NAME);
			final Node macroNode = macros.get(macroName);
			if (macroNode==null){
				throw new MorphDefException("Macro '" + macroName + "' undefined!");
			}
			vars = new ScopedHashMap<String, String>(vars);
			vars.putAll(attributeMap(node));
			handleRules(macroNode);
			vars = vars.getOuterScope();
		}else {
			illegalChild(node);
		}
	}

	private void handlePostprocess(final Node node) {
		for (Node functionNode = node.getFirstChild(); functionNode != null; functionNode = functionNode
				.getNextSibling()) {
			handleFunction(functionNode);
		}
	}

	private void handleMaps(final Node node) {
		for (Node mapNode = node.getFirstChild(); mapNode != null; mapNode = mapNode.getNextSibling()) {
			if (MAP.equals(mapNode.getLocalName())) {
				handleInternalMap(mapNode);
			} else {
				handleMapClass(mapNode);
			}
		}
	}

	private void handleRules(final Node node) {
		for (Node ruleNode = node.getFirstChild(); ruleNode != null; ruleNode = ruleNode.getNextSibling()) {
			handleRule(ruleNode);
		}
	}

	private void checkVersionCompatibility(final int version) {
		if (version < LOWEST_COMPATIBLE_VERSION || version > CURRENT_VERSION) {
			throw new MorphDefException("Version " + version
					+ " of definition file not supported by metamorph version " + CURRENT_VERSION);
		}
	}

	protected final void illegalChild(final Node child) {
		throw new MorphDefException("Schema mismatch: illegal tag " + child.getLocalName() + " in node "
				+ child.getParentNode().getLocalName());
	}

	private void handleFunctionDefinitions(final Node node) {
		for (Node functionDefNode = node.getFirstChild(); functionDefNode != null; functionDefNode = functionDefNode
				.getNextSibling()) {
			handleFunctionDefinition(functionDefNode);
		}
	}

	private void handleMeta(final Node node) {
		for (Node metaEntryNode = node.getFirstChild(); metaEntryNode != null; metaEntryNode = metaEntryNode
				.getNextSibling()) {

			handleMetaEntry(metaEntryNode.getLocalName(), metaEntryNode.getTextContent());
		}
	}
}

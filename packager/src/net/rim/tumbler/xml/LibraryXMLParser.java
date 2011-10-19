/*
* Copyright 2010-2011 Research In Motion Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.rim.tumbler.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.file.Library;
import net.rim.tumbler.file.Library.Configuration;
import net.rim.tumbler.file.Library.Extension;
import net.rim.tumbler.file.Library.Jar;
import net.rim.tumbler.file.Library.Platform;
import net.rim.tumbler.file.Library.Src;
import net.rim.tumbler.file.Library.Target;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LibraryXMLParser {
	private static final String NODE_LIBRARY = "library";
	private static final String NODE_EXTENSION = "extension";
	private static final String NODE_ENTRYCLASS = "entryClass";
	private static final String NODE_DEPENDENCIES = "dependencies";
	private static final String NODE_JAR = "jar";
	private static final String NODE_PLATFORMS = "platforms";
	private static final String NODE_PLATFORM = "platform";
	private static final String NODE_TARGET = "target";
	private static final String NODE_CONFIGURATIONS = "configurations";
	private static final String NODE_CONFIGURATION = "configuration";
	private static final String NODE_SRC = "src";
	private static final String NODE_FEATURES = "features";
	private static final String NODE_FEATURE = "feature";
	
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_VERSION = "version";
	private static final String ATTR_CONFIG = "config";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_PATH = "path";
	private static final String ATTR_ID = "id";
	private static final String ATTR_REQUIRED = "required";
	
	private Library _library = null;

	public LibraryXMLParser() {
		_library = new Library();
	}
	
	public Library parseXML(String libraryXMLPath) throws Exception {
		// TODO need to log anything?
		
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(libraryXMLPath));
			doc.getDocumentElement().normalize();
			
			return parseDocument(doc);
		} catch (SAXException e) {
			// TODO might not need to re-throw?
			Logger.logMessage(LogType.WARNING, "EXCEPTION_INVALID_LBRARY_XML",
					new String[] { libraryXMLPath });
		}
		
		return null;
	}
	
	private String processText(String text) {
		if (text == null) {
			return null;
		}

		return text.replaceAll("\t", "").replaceAll("\n", "").trim();
	}
	
	private String getTextValue(Node node) {
        if (node == null)
            return "";

        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return processText(node.getNodeValue());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node chilNode = list.item(i);
            if (chilNode.getNodeType() == Node.TEXT_NODE) {
                return processText(chilNode.getNodeValue());
            }
        }
        return "";
    }	

	private Library parseDocument(Document doc) {
		Node root = (Node) doc.getElementsByTagName(NODE_LIBRARY).item(0);
		
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals(NODE_EXTENSION)) {
					processExtensionNode(node);
				} else if (node.getNodeName().equals(NODE_PLATFORMS)) {
					processPlatformsNode(node);
				} else if (node.getNodeName().equals(NODE_CONFIGURATIONS)) {
					processConfigurationsNode(node);
				} else if (node.getNodeName().equals(NODE_FEATURES)) {
					processFeaturesNode(node);					
				}
			}
		}
		
		return _library;		
	}
	
	private void processExtensionNode(Node extensionNode) {
		if (extensionNode instanceof Element) {
			NamedNodeMap attrs = extensionNode.getAttributes();
			Node idAttr = attrs.getNamedItem(ATTR_ID);

			// id attribute does not exist in extension that comes as a JAR
			// even if it's not there, it is not necessarily an error
			if (idAttr != null) {
				Extension extension = new Extension(idAttr.getNodeValue());
				_library.setExtension(extension);
			}
				
			NodeList childNodes = extensionNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);

				if (node.getNodeName().equals(NODE_ENTRYCLASS)) {
					_library.setEntryClass(getTextValue(node).trim());
				} else if (node.getNodeName().equals(NODE_DEPENDENCIES)) {
					processDependenciesNode(node);
				}
			}
		}
	}
	
	private void processDependenciesNode(Node dependenciesNode) {
		if (dependenciesNode instanceof Element) {
			NodeList extensionNodes = ((Element) dependenciesNode).getElementsByTagName(NODE_EXTENSION);
			
			for (int i = 0; i < extensionNodes.getLength(); i++) {
				Node node = extensionNodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				
				if (attrs != null) {
					Node idAttr = attrs.getNamedItem(ATTR_ID);
					
					if (idAttr != null) {
						Extension dependency = new Extension(idAttr.getNodeValue());
						_library.addDependency(dependency);
					}
				}
			}
			
			NodeList jarNodes = ((Element) dependenciesNode).getElementsByTagName(NODE_JAR);
			
			for (int i = 0; i < jarNodes.getLength(); i++) {
				Node node = jarNodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				
				if (attrs != null) {
					Node pathAttr = attrs.getNamedItem(ATTR_PATH);
					
					if (pathAttr != null) {
						Jar jar = new Jar(pathAttr.getNodeValue());
						_library.addJarDependency(jar);
					}
				}
			}
		}
	}
	
	private void processPlatformsNode(Node platformsNode) {
		NodeList childNodes = platformsNode.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Platform p = processPlatformNode(childNodes.item(i));
			
			if (p != null) {
				_library.addPlatform(p);
			}
		}
	}
	
	private Platform processPlatformNode(Node platformNode) {
		if (platformNode.getNodeName().equals(NODE_PLATFORM)) {
			NamedNodeMap attrs = platformNode.getAttributes();
			
			if (attrs != null) {
				Node valueAttr = attrs.getNamedItem(ATTR_VALUE);

				if (valueAttr != null) {
					Platform platform = new Platform(valueAttr.getNodeValue());
					NodeList childNodes = platformNode.getChildNodes();

					for (int i = 0; i < childNodes.getLength(); i++) {
						Target target = processTargetNode(childNodes.item(i));

						if (target != null) {
							platform.addTarget(target);
						}
					}

					return platform;
				}
			}
		}

		return null;
	}
	
	private Target processTargetNode(Node targetNode) {
		if (targetNode.getNodeName().equals(NODE_TARGET)) {
			NamedNodeMap attrs = targetNode.getAttributes();

			if (attrs != null) {
				Node versionAttr = attrs.getNamedItem(ATTR_VERSION);
				Node configAttr = attrs.getNamedItem(ATTR_CONFIG);

				if (versionAttr != null && configAttr != null) {
					Target target = new Target(versionAttr.getNodeValue(),
							configAttr.getNodeValue());
					return target;
				}
			}
		}

		return null;
	}
	
	private void processConfigurationsNode(Node configurationsNode) {
		NodeList childNodes = configurationsNode.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Configuration config = processConfigurationNode(childNodes.item(i));
			
			if (config != null) {
				_library.addConfiguration(config);
			}
		}
		
	}
	
	private Configuration processConfigurationNode(Node configurationNode) {
		if (configurationNode.getNodeName().equals(NODE_CONFIGURATION)) {
			NamedNodeMap attrs = configurationNode.getAttributes();
			
			if (attrs != null) {
				Node nameAttr = attrs.getNamedItem(ATTR_NAME);
				Node versionAttr = attrs.getNamedItem(ATTR_VERSION);
				
				if (nameAttr != null && versionAttr != null) {
					Configuration config = new Configuration(nameAttr.getNodeValue(), versionAttr.getNodeValue());
					
					NodeList childNodes = configurationNode.getChildNodes();
					for (int i = 0; i < childNodes.getLength(); i++) {
						Src src = processSrcNode(childNodes.item(i));
						
						if (src != null) {
							config.addSrc(src);
						}
					}
					
					return config;
				}
			}
		}
		
		return null;
	}
	
	private Src processSrcNode(Node srcNode) {
		if (srcNode.getNodeName().equals(NODE_SRC)) {
			NamedNodeMap attrs = srcNode.getAttributes();
			
			if (attrs != null) {
				Node typeAttr = attrs.getNamedItem(ATTR_TYPE);
				Node pathAttr = attrs.getNamedItem(ATTR_PATH);
				
				if (typeAttr != null && pathAttr != null) {
					Src src = new Src(typeAttr.getNodeValue(), pathAttr.getNodeValue());
					return src;
				}
			}
		}
		
		return null;
	}
	
	private void processFeaturesNode(Node featuresNode) {
		NodeList childNodes = featuresNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			WidgetFeature feature = processFeatureNode(childNodes.item(i));
			
			if (feature != null) {
				_library.addFeature(feature);
			}
		}
		
	}
	
	private WidgetFeature processFeatureNode(Node featureNode) {
		if (featureNode.getNodeName().equals(NODE_FEATURE)) {
			NamedNodeMap attrs = featureNode.getAttributes();

			if (attrs != null) {
				Node idAttr = attrs.getNamedItem(ATTR_ID);
				Node versionAttr = attrs.getNamedItem(ATTR_VERSION);
				Node requiredAttr = attrs.getNamedItem(ATTR_REQUIRED);

				if (idAttr != null && versionAttr != null) {
					boolean isRequired = true;

					if (requiredAttr != null) {
						isRequired = !requiredAttr.getNodeValue()
								.equalsIgnoreCase(Boolean.FALSE.toString());
					}

					WidgetFeature feature = new WidgetFeature(idAttr
							.getNodeValue(), isRequired, versionAttr
							.getNodeValue(), null);
					return feature;
				}
			}
		}

		return null;
	}
}

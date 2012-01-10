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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.rim.tumbler.WidgetArchive;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.session.SessionManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ConfigXMLParser implements XMLParser {

    private WidgetConfig _widgetConfig;
    
    public ConfigXMLParser() {
        _widgetConfig = new WidgetConfig();
    }
    
    @Override
    public WidgetConfig parseXML(WidgetArchive archive) throws Exception {
        // Parse the xml file
        boolean verbose = SessionManager.getInstance().isVerbose();
        if (verbose) {
            Logger.logMessage(LogType.INFO, "PROGRESS_VALIDATING_CONFIG_XML");
        }
        try {
            // Create DOM
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
            Document doc = builder.parse(new ByteArrayInputStream(archive.getConfigXML()));
            doc.getDocumentElement().normalize();
            
            // Parse DOM
            return parseDocument(doc, archive);
            
        } catch (SAXException saxEx) {
            throw new PackageException("EXCEPTION_CONFIGXML_BADXML", saxEx);
        }
    }

    private WidgetConfig parseDocument(Document dom, WidgetArchive archive) throws Exception {
        
        // <widget> node
        Node root = (Node) dom.getElementsByTagName("widget").item(0);
        processWidgetNode(root);

        // Child nodes
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                if (node.getNodeName().equals("icon")) {
                    processIconNode(node);
                } else if (node.getNodeName().equals("author")) {
                    processAuthorNode(node);
                } else if (node.getNodeName().equals("license")) {
                    processLicenseNode(node);
                } else if (node.getNodeName().equals("content")) {
                    processContentNode(node);
                } else if (node.getNodeName().equals("rim:loadingScreen")) {
                    processLoadingScreenNode(node);
                } else if (node.getNodeName().equals("rim:connection")) {
                    processConnectionNode(node);
                } else if (node.getNodeName().equals("rim:navigation")) {
                    processNavigationNode(node);
                } else if (node.getNodeName().equals("rim:cache")) {
                    processCacheNode(node);
				} else if (node.getNodeName().equals("rim:orientation")) {
                    processOrientationNode(node);
                } else if (node.getNodeName().equals("name")) {
                    _widgetConfig.setName(getTextValue(node));
                } else if (node.getNodeName().equals("description")) {
                    _widgetConfig.setDescription(getTextValue(node));
                }
            }
        }
        
        // a bit more validation - put this elsewhere?  serializer?
        
        // Check for verbose mode
		if (SessionManager.getInstance().isVerbose()) {
			Logger.logMessage(LogType.INFO, "PROGRESS_VALIDATING_CONFIG_XML_WIDGET_NAME");
			Logger.logMessage(LogType.INFO,	"PROGRESS_VALIDATING_CONFIG_XML_WIDGET_AUTHOR");
		}
		
        // Validate that an application name/author was specified
        if (_widgetConfig.getName() == null) {
        	throw new ValidationException("EXCEPTION_CONFIGXML_MISSING_WIDGET_NAME");
        }
        
        // Validation via logging
        if (_widgetConfig.getAuthor() == null) {
        	// Just log warning
        	Logger.logMessage(LogType.WARNING, "VALIDATION_CONFIGXML_MISSING_AUTHOR");
        }     

        if (_widgetConfig.getContent() == null || _widgetConfig.getContent().isEmpty()) {
            // No content page found in config.xml, how about in zip?
            if (archive.getIndexFile() == null) {
                Logger.logMessage(LogType.WARNING, "VALIDATION_MISSING_STARTUP_PAGE");
            } else {
                _widgetConfig.setContent(archive.getIndexFile());
            }
        }
        // No icon files found, how about in zip?
        if (_widgetConfig.getIconSrc().size() == 0 && archive.getIconFile() != null) {
            _widgetConfig.addIcon(archive.getIconFile());
        }
        
        // Invalid Configurations...
        
        // If both source attributes are empty the developer did something wrong.
        if ((_widgetConfig.getForegroundSource() == null || _widgetConfig.getForegroundSource().isEmpty()) 
                && _widgetConfig.getBackgroundSource() == null) {
            throw new PackageException( "EXCEPTION_CONFIGXML_INVALID_CONTENT",
                    "Invalid source or the source is not specified." );
        }

        if (!_widgetConfig.isStartupEnabled()
                && !_widgetConfig.getForegroundSource().isEmpty()
                && _widgetConfig.getBackgroundSource() != null) {
            throw new PackageException( "EXCEPTION_CONFIGXML_INVALID_CONTENT",
                    "Invalid source or the source is not specified." );
        }
        
        return _widgetConfig;
    }

    private void processWidgetNode(Node widgetNode) throws Exception {
        NodeList list = widgetNode.getChildNodes();
        NamedNodeMap attrs = widgetNode.getAttributes();

        // version        
        Node versionAttr = attrs.getNamedItem("version");
		if (versionAttr == null) {
			throw new ValidationException("VALIDATION_CONFIGXML_MISSING_VERSION");
		}
        _widgetConfig.setVersion(getTextValue(versionAttr));

        // id
        Node idAttr = attrs.getNamedItem("id");
        if (idAttr != null) {
            _widgetConfig.setID(getTextValue(idAttr));
        }

        // rim:header
        Node headerAttr = attrs.getNamedItem("rim:header");
        if (headerAttr != null) {
            String header = getTextValue(headerAttr);
            int index = header.indexOf(':');

            if (index > 0) {
                _widgetConfig.addHeader(
                        header.substring(0, index), header.substring(index + 1, header.length()));
            }
        }
        
        // rim:backButton
        Node backButtonAttr = attrs.getNamedItem("rim:backButton");
        if (backButtonAttr != null) {
            _widgetConfig.setBackButtonBehaviour(getTextValue(backButtonAttr));
        }

        // Parsing access nodes and feature nodes
        Hashtable<WidgetAccess, Vector<WidgetFeature>> accessTable = new Hashtable<WidgetAccess, Vector<WidgetFeature>>();

        // Populate "LOCAL" access list
        String localpath = "WidgetConfig.WIDGET_LOCAL_DOMAIN";
        boolean hasFeatures = false;
        WidgetAccess localAccess = new WidgetAccess(localpath, true);
        Vector<WidgetFeature> featureList = getFeatureListFromNode(widgetNode);
        if (featureList.size() > 0) {
            hasFeatures = true;
        }
        accessTable.put(localAccess, featureList);

        // Populate all "access" nodes access lists
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equalsIgnoreCase("access")
                    && node.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap nodeAttributes = node.getAttributes();

                // uri information
                Node uriNode = nodeAttributes.getNamedItem("uri");
                String uri = "";
                if (uriNode != null) {
                    uri = uriNode.getNodeValue();
                }

                // subdomains information
                Node subdomainsNode = nodeAttributes.getNamedItem("subdomains");
                boolean subdomains = false;
                if (subdomainsNode != null) {
                    if (subdomainsNode.getNodeValue().equalsIgnoreCase("true")) {
                        subdomains = true;
                    }
                }

                if (!uri.trim().equals("*")) {
                    WidgetAccess access = new WidgetAccess(uri, subdomains);
                    
                    // Find all sub-feature nodes
                    if (uri.length() > 0) {
                        featureList = getFeatureListFromNode(node);
                        if (featureList.size() > 0) {
                            hasFeatures = true;
                        }
                        accessTable.put(access, featureList);
                    }
                } else {
                    _widgetConfig.setMultiAccess(true);
                    // no features allowed for *
                    if (getFeatureListFromNode(node).size() > 0) {
                        throw new ValidationException("EXCEPTION_CONFIGXML_FEATURES_NOT_ALLOWED");
                    }
                }
            }
        }
        if (!hasFeatures) {
            Logger.logMessage(LogType.WARNING, "VALIDATION_CONFIGXML_NO_FEATURES");
        }
        
        _widgetConfig.setAccessTable(accessTable);
    }

    private final Vector<WidgetFeature> getFeatureListFromNode(Node parentNode)
            throws Exception {
        Vector<WidgetFeature> featureList = new Vector<WidgetFeature>();

        // Find all <feature> child nodes
        NodeList list = parentNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().equalsIgnoreCase("feature")
                    && node.getNodeType() == Node.ELEMENT_NODE) {
                String name = "";
                boolean isRequired = true;
                String version = "";

                NamedNodeMap nodeAttributes = node.getAttributes();

                // id
                Node id = nodeAttributes.getNamedItem("id");
                if (id != null) {
                    name = id.getNodeValue();
                    
                    // required
                    Node required = nodeAttributes.getNamedItem("required");
                    if (required != null) {
                        String value = required.getNodeValue();
                        if (value.equalsIgnoreCase("false")) {
                            isRequired = false;
                        }
                    }

                    // version
                    Node ver = nodeAttributes.getNamedItem("version");
                    if (ver != null) {
                        version = ver.getNodeValue();
                    }

                    if (name.length() > 0) {
                        WidgetFeature wf = new WidgetFeature(name, isRequired, version, null);
                        featureList.addElement(wf);
                    }
                } else {
                    throw new ValidationException("VALIDATION_MISSING_FEATURE_ID");
                }
            }
        }

        return featureList;
    }

    // <content>
    private void processContentNode(Node contentNode) throws Exception {
        NamedNodeMap attrs = contentNode.getAttributes();
        NodeList list = contentNode.getChildNodes();
        int listLength = list.getLength();
        
        Node srcAttr = attrs.getNamedItem("src");
        _widgetConfig.setContent(getURIValue(srcAttr));
        _widgetConfig.setForegroundSource(getURIValue(srcAttr));

        Node typeAttr = attrs.getNamedItem("type");
        if (typeAttr != null) {
            _widgetConfig.setContentType(getTextValue(typeAttr));            
        }

        Node charsetAttr = attrs.getNamedItem("charset");
        if (charsetAttr != null) {
            _widgetConfig.setContentCharSet(getTextValue(typeAttr));
        }
        
        Node invokeParamAttr = attrs.getNamedItem("rim:allowInvokeParams");
        if (invokeParamAttr != null && invokeParamAttr.getNodeValue().equalsIgnoreCase("true")) {
        	_widgetConfig.setAllowInvokeParams(true);
        }
        
        //Process Child "background"      
        for(int i = 0; i < listLength; i++) {
        	Node startupNode = list.item(i);
        	String nodeName = startupNode.getNodeName(); 
        	if(nodeName.equalsIgnoreCase("rim:background")) {
        		_widgetConfig.setStartup(true);
        		NamedNodeMap startupAttrs = startupNode.getAttributes();
        		Node srcNode = startupAttrs.getNamedItem("src");
        		Node runOnStartup = startupAttrs.getNamedItem("runOnStartup");
        		if (srcNode != null) {
        			if (runOnStartup != null && runOnStartup.getNodeValue().equalsIgnoreCase("false")) {
        				_widgetConfig.setStartup(false);
        			}
        			_widgetConfig.setBackgroundSource(getURIValue(srcNode));
        		}
        	}
        }
    }
    
    // <license>
    private void processLicenseNode(Node licenseNode) throws Exception {
        // license
        _widgetConfig.setLicense(getTextValue(licenseNode).trim());
        
        //license URL
        NamedNodeMap attrs = licenseNode.getAttributes();
        Node hrefAttr = attrs.getNamedItem("href");
        if (hrefAttr != null) {
            _widgetConfig.setLicenseURL(getURIValue(hrefAttr));
        }
    }

    // <author>
    private void processAuthorNode(Node authorNode) throws Exception {
        // author
        _widgetConfig.setAuthor(getTextValue(authorNode).trim());

        // author URL
        NamedNodeMap attrs = authorNode.getAttributes();
        Node hrefAttr = attrs.getNamedItem("href");
        if (hrefAttr != null) {
            _widgetConfig.setAuthorURL(getURIValue(hrefAttr));
        }

        // copyright
        Node copyrightAttr = attrs.getNamedItem("rim:copyright");
        if (copyrightAttr != null) {
            _widgetConfig.setCopyright(getTextValue(copyrightAttr));
        }

        // email
        Node emailAttr = attrs.getNamedItem("email");
        if (emailAttr != null) {
            _widgetConfig.setAuthorEmail(getTextValue(emailAttr));
        }
    }

    // <icon>
    private void processIconNode(Node iconNode) throws Exception {
        // get icon
        NamedNodeMap attrs = iconNode.getAttributes();
        Node src = attrs.getNamedItem("src");
        if (src == null) {
            throw new PackageException("EXCEPTION_CONFIGXML_INVALID_ICON");
        }
        String iconSrc = getURIValue(src);
        
        // check hover
        Node hoverAttr = attrs.getNamedItem("rim:hover");
        if (hoverAttr != null && hoverAttr.getNodeValue().equals("true")) {
            if (_widgetConfig.getHoverIconSrc().size() == 0) {
                _widgetConfig.addHoverIcon(iconSrc);
            }
        } else if (hoverAttr == null || hoverAttr.getNodeValue().equals("false")) {
            if (_widgetConfig.getIconSrc().size() == 0) {
                _widgetConfig.addIcon(iconSrc);
            }
        }
    }
    
    // <rim:navigation>
    private void processNavigationNode(Node navigationNode) throws Exception {
        // get icon
        NamedNodeMap attrs = navigationNode.getAttributes();
        Node mode = attrs.getNamedItem("mode");
        if (mode != null && mode.getNodeValue().equals("focus")) {
            _widgetConfig.setNavigationMode(true);
        } else {
        	_widgetConfig.setNavigationMode(false);
        }
    }    
    
    /*
     * Processes the loading screen node and sets Loading Screen configurations
     */
    private void processLoadingScreenNode(Node loadingScreenNode) throws Exception {
        if (loadingScreenNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new PackageException("EXCEPTION_CONFIGXML_INVALID_LOADINGSCREEN_ELEMENT");
        }     	
    	
        NamedNodeMap attrs = loadingScreenNode.getAttributes();
        Node attr;
        
        attr = attrs.getNamedItem("backgroundColor");
        if (attr != null) {
            _widgetConfig.setLoadingScreenColour(getTextValue(attr));
        }

        attr = attrs.getNamedItem("backgroundImage");
        if (attr != null) {
            _widgetConfig.setBackgroundImage(getTextValue(attr).replace('\\', '/').trim());
        }
        
        attr = attrs.getNamedItem("foregroundImage");
        if (attr != null) {
            _widgetConfig.setForegroundImage(getTextValue(attr).replace('\\', '/').trim());
        }

        attr = attrs.getNamedItem("onFirstLaunch");
        if (attr != null && attr.getNodeValue().equalsIgnoreCase("true")) {
            _widgetConfig.setFirstPageLoad(true);
        }
    
        attr = attrs.getNamedItem("onRemotePageLoad");
        if (attr != null && attr.getNodeValue().equalsIgnoreCase("true")) {
            _widgetConfig.setRemotePageLoad(true);
        }

        attr = attrs.getNamedItem("onLocalPageLoad");
        if (attr != null && attr.getNodeValue().equalsIgnoreCase("true")) {
            _widgetConfig.setLocalPageLoad(true);
        }
        
        Element loadingScreenElement = (Element) loadingScreenNode;

        // Process nested <rim:transitionEffect> elements
        NodeList transitionEffectList = loadingScreenElement.getElementsByTagName("rim:transitionEffect");
        if (transitionEffectList.getLength() > 1) {
        	throw new PackageException("EXCEPTION_CONFIGXML_INVALID_LOADINGSCREEN_ELEMENT");
        }

        if (transitionEffectList.getLength() > 0) {
        	Node transitionEffectNode = transitionEffectList.item(0);
        	processTransitionEffectNode(transitionEffectNode);
        }
    }

    /*
     * Processes the transition effect node and sets transition configurations
     */    
    private void processTransitionEffectNode(Node transitionNode) throws Exception {
        NamedNodeMap attrs = transitionNode.getAttributes();
        Node attr;
        
        attr = attrs.getNamedItem("type");
        if (attr != null) {
        	String transitionType = null;
        	if (attr.getNodeValue().equalsIgnoreCase("slidePush")) {
        		transitionType = "TransitionConstants.TRANSITION_SLIDEPUSH";
        	} else if (attr.getNodeValue().equalsIgnoreCase("slideOver")) {
        		transitionType = "TransitionConstants.TRANSITION_SLIDEOVER";
        	} else if (attr.getNodeValue().equalsIgnoreCase("fadeIn")) {
        		transitionType = "TransitionConstants.TRANSITION_FADEIN";
        	} else if (attr.getNodeValue().equalsIgnoreCase("fadeOut")) {
        		transitionType = "TransitionConstants.TRANSITION_FADEOUT";
        	} else if (attr.getNodeValue().equalsIgnoreCase("wipeIn")) {
        		transitionType = "TransitionConstants.TRANSITION_WIPEIN";
        	} else if (attr.getNodeValue().equalsIgnoreCase("wipeOut")) {
        		transitionType = "TransitionConstants.TRANSITION_WIPEOUT";
        	} else if (attr.getNodeValue().equalsIgnoreCase("zoomIn")) {
        		transitionType = "TransitionConstants.TRANSITION_ZOOMIN";
        	} else if (attr.getNodeValue().equalsIgnoreCase("zoomOut")) {
        		transitionType = "TransitionConstants.TRANSITION_ZOOMOUT";
        	}
        	
        	if (transitionType != null) {
        		_widgetConfig.setTransitionType(transitionType);
        	}
        }

        attr = attrs.getNamedItem("duration");
        if (attr != null) {
            // Check if the duration value is valid
            int duration = -1;
            try {
                duration = Integer.parseInt(getTextValue(attr).trim());
            } catch (NumberFormatException ignore) {
                // duration remains -1;
            }
            if (duration >= 0) {
                if (duration < 250 ) {
                    duration = 250;
                } else if(duration > 1000) {
                    duration = 1000;
                }
                _widgetConfig.setTransitionDuration( duration );
            }
        }    
        
        attr = attrs.getNamedItem("direction");
        if (attr != null) {
        	String transitionDirection = null;
        	if (attr.getNodeValue().equalsIgnoreCase("left")) {
        		transitionDirection = "TransitionConstants.DIRECTION_LEFT";
        	} else if (attr.getNodeValue().equalsIgnoreCase("right")) {
        		transitionDirection = "TransitionConstants.DIRECTION_RIGHT";
        	} else if (attr.getNodeValue().equalsIgnoreCase("up")) {
        		transitionDirection = "TransitionConstants.DIRECTION_UP";
        	} else if (attr.getNodeValue().equalsIgnoreCase("down")) {
        		transitionDirection = "TransitionConstants.DIRECTION_DOWN";
        	}
        	
        	if (transitionDirection != null) {
        		_widgetConfig.setTransitionDirection(transitionDirection);
        	}
        }
    }
    
    /*
     * <rim:connection>
     * Processes the connection element to determine the preferred transport
     * order
     */
    private void processConnectionNode(Node connectionNode) throws Exception {

        // This should be an element node
        if (connectionNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new PackageException("EXCEPTION_CONFIGXML_INVALID_CONNECTION_ELEMENT");
        }
        Element connElement = (Element) connectionNode;

        // Process timeout attribute
        NamedNodeMap attrs = connElement.getAttributes();
        Node timeoutAttr = attrs.getNamedItem("timeout");
        if (timeoutAttr != null) {
            // Check if the timeout value is valid
            int timeoutValue = -1;
            try {
                timeoutValue = Integer.parseInt(getTextValue(timeoutAttr));
            } catch (NumberFormatException ignore) {
                // timeoutValue remains -1
            }
            if (timeoutValue >= 0) {
                _widgetConfig.setTransportTimeout(timeoutValue);
            }
        }

        // Process nested <id> elements
        NodeList transportList = connElement.getElementsByTagName("id");
        if (transportList.getLength() > 0) {
            // Build the lookup table
            Hashtable<String, String> referenceLookup = new Hashtable<String, String>();
            referenceLookup.put("TCP_WIFI", "TransportInfo.TRANSPORT_TCP_WIFI");
            referenceLookup.put("MDS", "TransportInfo.TRANSPORT_MDS");
            referenceLookup.put("BIS-B", "TransportInfo.TRANSPORT_BIS_B");
            referenceLookup.put("TCP_CELLULAR", "TransportInfo.TRANSPORT_TCP_CELLULAR");
            referenceLookup.put("WAP", "TransportInfo.TRANSPORT_WAP");
            referenceLookup.put("WAP2", "TransportInfo.TRANSPORT_WAP2");

            // Go through the transport list
            String[] transportArray = new String[transportList.getLength()];
            for (int i = 0; i < transportList.getLength(); i++) {
                Node currentTransport = transportList.item(i);
                transportArray[i] = referenceLookup.get(getTextValue(currentTransport).toUpperCase());
            }
            _widgetConfig.setTransportOrder(transportArray);
        }
    }
	
	
   // Processing for the <rim:orientation> node
    private void processOrientationNode(Node orientationNode) throws Exception {
        
    	// get mode
        NamedNodeMap attrs = orientationNode.getAttributes();
        Node modeAttrNode = attrs.getNamedItem("mode");        
        if (modeAttrNode != null) {        	
        	try{
				int orientation = -1;
				if (modeAttrNode.getNodeValue().equalsIgnoreCase("portrait")) {
					orientation = 0;
				} else if (modeAttrNode.getNodeValue().equalsIgnoreCase("landscape")) {
					orientation = 1;
				} 
				// Only set the orientation if it was properly specified
				if (orientation != -1) {
					_widgetConfig.setOrientation(orientation);
				}
        	}catch(Exception e){
        		// Default values are used if an error happens        		
        	}
        } 
    }    
	
   // Processing for the <rim:cache> node
    private void processCacheNode(Node cacheNode) throws Exception {
        
    	// get disableAllCache
        NamedNodeMap attrs = cacheNode.getAttributes();
        Node disableCacheAttrNode = attrs.getNamedItem("disableAllCache");        
        if (disableCacheAttrNode != null) {        	
        	try{
	        	// Obtain value
	        	boolean disabledCache = 
	        		Boolean.parseBoolean(disableCacheAttrNode.getNodeValue());
	        	
	        	// Flip the value before we set it
	            _widgetConfig.setCacheEnabled(!disabledCache);
        	}catch(Exception e){
        		// Default values are used if an error happens        		
        	}
        } 
        
        // get aggressiveCacheAge        
        Node aCacheAgeAttrNode = attrs.getNamedItem("aggressiveCacheAge");        
        if (aCacheAgeAttrNode != null) {
        	try{
        		int aCacheAgeValue = Integer.parseInt(aCacheAgeAttrNode.getNodeValue());
        		_widgetConfig.setAggressiveCacheAge(aCacheAgeValue);
        	} catch (Exception e){
        		// Default values are used if an error happens        		
        	}
        }
        
        // Note: It is important to set the total BEFORE the individual item size
        // since there are some constraints that depend on it
        
        // get maxCacheSizeTotal         
        Node maxCacheTotalAttrNode = attrs.getNamedItem("maxCacheSizeTotal");        
        if (maxCacheTotalAttrNode != null) {
        	try{
        		int maxCacheTotalValue = Integer.parseInt(maxCacheTotalAttrNode.getNodeValue());
        		// Convert from kilobytes to bytes
        		_widgetConfig.setMaxCacheSize(maxCacheTotalValue * 1024);
        	}catch(Exception e){
        		// Default values are used if an error happens        		
        	}
        }
        
        // get maxCacheSizeItem        
        Node maxCacheItemAttrNode = attrs.getNamedItem("maxCacheSizeItem");        
        if (maxCacheItemAttrNode != null) {
        	try{
        		int maxCacheItemValue = Integer.parseInt(maxCacheItemAttrNode.getNodeValue());
        		// Convert from kilobytes to bytes
        		if (maxCacheItemValue != -1) {
        			_widgetConfig.setMaxCacheItemSize(maxCacheItemValue * 1024);
        		} else {
        			_widgetConfig.setMaxCacheItemSize(maxCacheItemValue);
        		}
        	} catch (Exception e){
        		// Default values are used if an error happens
        	}
        }
    }    
    
    private String processText(String text) {
    	if (text == null) {
    	    return "";        
    	}
        return text.replaceAll("\t", "").replaceAll("\n", "").trim();                    
    }

    /** Returns an empty string upon a null node argument or null node value */
    private String getTextValue(Node node) {
        if (node == null) {
            return "";
        }
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return processText(node.getNodeValue());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                return processText(childNode.getNodeValue());
            }
        }
        return "";
    }

    private String getURIValue(Node node) {
        if (node == null)
            return "";

        String result = null;
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            result = node.getNodeValue();
        }
        else {
	        NodeList list = node.getChildNodes();
	        for (int i = 0; i < list.getLength(); i++) {
	            Node chilNode = list.item(i);
	            if (chilNode.getNodeType() == Node.TEXT_NODE) {
	                result = chilNode.getNodeValue();
	            }
	        }
        }
        
        if (result != null) {
        	return result.replace('\\', '/').trim();
        }
        return "";
    }
    
    static class MyErrorHandler implements ErrorHandler {
        @Override
        public void error(SAXParseException arg0) throws SAXException {
            printErrorMessage();
        }

        @Override
        public void fatalError(SAXParseException arg0) throws SAXException {
            printErrorMessage();
        }

        @Override
        public void warning(SAXParseException arg0) throws SAXException {
        }

        private void printErrorMessage() {
        }
    }
}

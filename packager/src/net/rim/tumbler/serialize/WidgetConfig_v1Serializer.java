/*
* Copyright 2010 Research In Motion Limited.
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
package net.rim.tumbler.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.file.TemplateFile;

public class WidgetConfig_v1Serializer implements WidgetConfigSerializer {
    
    private static final String         EOL = System.getProperty("line.separator");
    private static final String         TAB = "    ";
    private static final String         AUTOGEN_HEADER =
        "///" +
        "/// AUTO-GENERATED CLASS FROM WEB COMPONENT PACK - edit with caution" + EOL +      
        "///" + EOL + EOL +
        "/*" + EOL +
        " * WidgetConfigAutoGen.java" + EOL +
        " *" + EOL +
        " * © Research In Motion Limited, 2009" + EOL +
        " * Confidential and proprietary." + EOL +
        " */" + EOL +
        "package blackberry.web.widget.autogen;" + EOL +
        "import blackberry.web.widget.impl.WidgetConfigImpl;" + EOL +
        "import blackberry.web.widget.loadingScreen.TransitionConstants;" + EOL +
        "import net.rim.device.api.web.WidgetAccess;" + EOL +
        "import net.rim.device.api.web.WidgetConfig;" + EOL +
        "import net.rim.device.api.web.WidgetFeature;" + EOL +
        "import net.rim.device.api.io.transport.TransportInfo;" + EOL +
        "import java.util.Hashtable;" + EOL +
        "/*" + EOL +
        " * see blackberry.web.widget.WidgetConfig" + EOL +
        " */" + EOL +
        "public final class WidgetConfigAutoGen extends WidgetConfigImpl {" + EOL +
        "    public WidgetConfigAutoGen() {" + EOL;

    private StringBuffer        _buffer;
    private Map<String, String> _memberMap;
    private WidgetConfig        _widgetConfig;
    
    public WidgetConfig_v1Serializer(WidgetConfig widgetConfig) {
        _buffer = new StringBuffer();
        _memberMap = new HashMap<String, String>();
		
        // Populate the basic members
        // Special characters for Java source file: escape /, ", ' for Java source 
        _memberMap.put("_id",                       widgetConfig.getID());
        _memberMap.put("_name",                     widgetConfig.getName());
        _memberMap.put("_description",              widgetConfig.getDescription());
        _memberMap.put("_content",                  widgetConfig.getContent());
        _memberMap.put("_configXML",                widgetConfig.getConfigXML());
        _memberMap.put("_backButtonBehaviour",      widgetConfig.getBackButtonBehaviour());
        _memberMap.put("_contentType",              widgetConfig.getContentType());
        _memberMap.put("_contentCharset",           widgetConfig.getContentCharSet());
        _memberMap.put("_license",                  widgetConfig.getLicense());
        _memberMap.put("_licenseURL",               widgetConfig.getLicenseURL());
        _memberMap.put("_author",                   widgetConfig.getAuthor());
        _memberMap.put("_copyright",                widgetConfig.getCopyright());
        _memberMap.put("_authorEmail",              widgetConfig.getAuthorEmail());
        _memberMap.put("_loadingScreenColor",       widgetConfig.getLoadingScreenColour());
        _memberMap.put("_backgroundImage",       	widgetConfig.getBackgroundImage());
        _memberMap.put("_foregroundImage",       	widgetConfig.getForegroundImage());
        _memberMap.put("_authorURL",                widgetConfig.getAuthorURL());
        _memberMap.put("_backgroundSource",         widgetConfig.getBackgroundSource());
        _memberMap.put("_foregroundSource",         widgetConfig.getForegroundSource());

        
        _widgetConfig = widgetConfig;
    }
    
    public byte[] serialize() {
        _buffer.append(TemplateFile.refactor(AUTOGEN_HEADER));
        Set<String> members = _memberMap.keySet();
        
        // Iterate memberMap
        for( String member : members ) {
            String value = _memberMap.get(member);
            if (value != null) {
                _buffer.append(makeLine(
                        member + " = \"" + escapeSpecialCharacterForJavaSource(value) + "\";", 0));
            }
        }
        
        // * present
        if (_widgetConfig.allowMultiAccess()) {
            _buffer.append(makeLine("_hasMultiAccess = true;", 0));
        }
       
        // Add icons
        if (_widgetConfig.getIconSrc().size() > 0) {
            _buffer.append(makeLine(
                     "_icon = \"" + _widgetConfig.getIconSrc().firstElement() + "\";", 0));
            if (_widgetConfig.getHoverIconSrc().size() > 0) {
                _buffer.append(makeLine(
                        "_iconHover = \"" + _widgetConfig.getHoverIconSrc().firstElement() + "\";", 0));                
            }
        }
        
        // Add custom headers
        for( String key : _widgetConfig.getCustomHeaders().keySet()) {
            _buffer.append(makeLine("_customHeaders.addProperty(", 0));
            _buffer.append(makeLine("\"" + key + "\",", 1));
            _buffer.append(makeLine("\"" + escapeSpecialCharacterForJavaSource(_widgetConfig.getCustomHeaders().get(key)) + "\");", 1));
        }
        
        // Set navigation mode
        if (_widgetConfig.getNavigationMode()) {
            _buffer.append(makeLine("_widgetNavigationMode = true;", 0));
        }

        // Add LoadingScreen configuration
        if (_widgetConfig.getFirstPageLoad()) {
            _buffer.append(makeLine("_firstPageLoad = true;", 0));
        }        
        if (_widgetConfig.getRemotePageLoad()) {
            _buffer.append(makeLine("_remotePageLoad = true;", 0));
        }     
        if (_widgetConfig.getLocalPageLoad()) {
            _buffer.append(makeLine("_localPageLoad = true;", 0));
        }     
        
        // Add TransitionEffect configuration
        if (_widgetConfig.getTransitionType() != null) {
            _buffer.append(makeLine(
                    "_transitionType = " + _widgetConfig.getTransitionType() + ";", 0));
            
            if (_widgetConfig.getTransitionDuration() >= 0) {
                _buffer.append(makeLine(
                        "_transitionDuration = " + _widgetConfig.getTransitionDuration() + ";", 0));
            }
            
            if (_widgetConfig.getTransitionDirection() != null) {
                _buffer.append(makeLine(
                        "_transitionDirection = " + _widgetConfig.getTransitionDirection() + ";", 0));
            }            
        }
        
        // Add cache options
        if (_widgetConfig.isCacheEnabled() != null) {
            _buffer.append(makeLine("_cacheEnabled = " + _widgetConfig.isCacheEnabled() + ";", 0));
        }
        if (_widgetConfig.getAggressiveCacheAge() != null) {
        	// Enable aggressive caching if applicable
        	if(_widgetConfig.isAggressiveCacheEnabled()!=null) {
	        	_buffer.append(makeLine("_aggressivelyCaching = " 
	            		+ _widgetConfig.isAggressiveCacheEnabled() + ";", 0));
        	}
            _buffer.append(makeLine("_aggressiveCacheAge = " 
            		+ _widgetConfig.getAggressiveCacheAge() + ";", 0));
        }        
        if (_widgetConfig.getMaxCacheSize() != null) {
            _buffer.append(makeLine("_maxCacheSize = " 
            		+ _widgetConfig.getMaxCacheSize() + ";", 0));
        }
        if (_widgetConfig.getMaxCacheItemSize() != null) {
            _buffer.append(makeLine("_maxCacheable = " 
            		+ _widgetConfig.getMaxCacheItemSize() + ";", 0));
        }
        // Debug issue fix ?
        if(_widgetConfig.isDebugEnabled()) {
        	_buffer.append(makeLine("_debugEnabled = true;", 0));
        }
        
        // Auto-Startup options
        if(_widgetConfig.allowInvokeParams()) {
        	_buffer.append(makeLine("_allowInvokeParams = " 
            		+ _widgetConfig.allowInvokeParams() + ";", 0));
        }
        
        if(_widgetConfig.isStartupEnabled()) {
        	_buffer.append(makeLine("_runOnStartup = " 
            		+ _widgetConfig.isStartupEnabled() + ";", 0));
        }
        
        // Add 3rd party extensions
        for (int j = 0; j < _widgetConfig.getExtensionClasses().size(); j++) {
        	String extensionClass = _widgetConfig.getExtensionClasses().elementAt(j);
        	_buffer.append(makeLine(
        			"_widgetExtensions.addElement(new " + extensionClass + "());", 0));
        	
        }
        
        // Add transport
        if (_widgetConfig.getTransportTimeout() >= 0) {
            _buffer.append(makeLine(
                    "_transportTimeout = new Integer(" + _widgetConfig.getTransportTimeout() + ");", 0));
        }
        if (_widgetConfig.getTransportOrder() != null) {
            _buffer.append(makeLine(
                    "_preferredTransports = new int[]{",0));
            for(int i=0; i<_widgetConfig.getTransportOrder().length; i++) {
                String transport = _widgetConfig.getTransportOrder()[i];
                if (i+1 != _widgetConfig.getTransportOrder().length) {
                    transport += ",";
                }
                _buffer.append(makeLine(transport, 1));
            }
            _buffer.append(makeLine("};", 0));
        }
        
        // Add access/features
        if (_widgetConfig.getAccessTable().size() > 0) {
            String line;
            _buffer.append(makeLine("try {", 0));
            for (WidgetAccess key : _widgetConfig.getAccessTable().keySet()) {
                line = "_accessList.put(";
                _buffer.append(makeLine(line, 1));
                String uri = key.getURI().toString();
                if (uri.equals("WidgetConfig.WIDGET_LOCAL_DOMAIN")) {
                    line = uri + ",";
                } else {
                    line = "\"" + uri + "\"" + ",";
                }
                _buffer.append(makeLine(line, 2));
                _buffer.append(makeLine("new WidgetAccess(", 2));
                _buffer.append(makeLine(line, 3));
                line = (new Boolean(key.allowSubDomain())).toString() + ",";
                _buffer.append(makeLine(line, 3));
                _buffer.append(makeLine("new WidgetFeature[] {", 3));

                Vector<?> wfList = (Vector<?>)_widgetConfig.getAccessTable().get(key);
                for (int j = 0; j < wfList.size(); j++) {
                    WidgetFeature wf = (WidgetFeature) wfList.get(j);
                    _buffer.append(makeLine("new WidgetFeature(", 4));
                    line = "\"" + wf.getID() + "\"" + ",";
                    _buffer.append(makeLine(line, 5));
                    line = (new Boolean(wf.isRequired())).toString() + ",";
                    _buffer.append(makeLine(line, 5));
                    line = "\"" + wf.getVersion() + "\"" + ",";
                    _buffer.append(makeLine(line, 5));
                    line = "null)";
                    if (j+1 != wfList.size()) {
                        line += ",";
                    }
                    _buffer.append(makeLine(line, 5));
                }
                _buffer.append(makeLine("}", 3));
                _buffer.append(makeLine(")", 2));
                _buffer.append(makeLine(");", 1));
            }
            _buffer.append(makeLine("} catch (Exception e) {", 0));
            _buffer.append(makeLine("// ignore this element - invalid URI", 1));
            _buffer.append(makeLine("}", 0));
        }        
        _buffer.append(EOL + TAB + "}" + EOL + "}");
        return _buffer.toString().getBytes();
    }
    
    private String makeLine(String toAdd, int level) {
        String result = EOL + TAB + TAB;
        for(int i=0; i<level; i++) {
            result += TAB;
        }
        return result + toAdd;
    }
    
	private String escapeSpecialCharacterForJavaSource(String s) {
		// process escaped characters
		// " -> \\\\\"
		// ' -> \\\\\'
		// \ -> \\\\\\\\
		// NOTE: \\\\ (4 SLASHES) stand for 1 \ (SLASH)
		
		if (s == null) return null;
		String ret = 
			s.replaceAll(Pattern.quote("\\"), "\\\\\\\\")
			 .replaceAll(Pattern.quote("\""), "\\\\\"")
			 .replaceAll(Pattern.quote("\'"), "\\\\\'");
		return ret;
	}    
}

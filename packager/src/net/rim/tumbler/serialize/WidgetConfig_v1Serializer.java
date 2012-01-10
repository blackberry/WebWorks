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
package net.rim.tumbler.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.file.TemplateFile;

public class WidgetConfig_v1Serializer implements WidgetConfigSerializer {
    
    private static final String NL = System.getProperty("line.separator");
    private static final String TAB = "    ";
    private static final String NL_LVL_0 = NL + TAB + TAB;
    private static final String NL_LVL_1 = NL + TAB + TAB + TAB;
    private static final String NL_LVL_2 = NL + TAB + TAB + TAB + TAB;
    private static final String NL_LVL_3 = NL + TAB + TAB + TAB + TAB + TAB;
    private static final String NL_LVL_4 = NL + TAB + TAB + TAB + TAB + TAB + TAB;
    private static final String NL_LVL_5 = NL + TAB + TAB + TAB + TAB + TAB + TAB + TAB;
    private static final String AUTOGEN_HEADER =
        "///" +
        "/// AUTO-GENERATED CLASS FROM WEB COMPONENT PACK - edit with caution" + NL +      
        "///" + NL + NL +
        "/*" + NL +
        " * WidgetConfigAutoGen.java" + NL +
        " *" + NL +
        " * © Research In Motion Limited, 2009-2011" + NL +
        " * Confidential and proprietary." + NL +
        " */" + NL +
        "package blackberry.web.widget.autogen;" + NL +
        "import blackberry.web.widget.impl.WidgetConfigImpl;" + NL +
        "import blackberry.web.widget.loadingScreen.TransitionConstants;" + NL +
        "import net.rim.device.api.web.WidgetAccess;" + NL +
        "import net.rim.device.api.web.WidgetConfig;" + NL +
        "import net.rim.device.api.web.WidgetFeature;" + NL +
        "import net.rim.device.api.io.transport.TransportInfo;" + NL +
        "import java.util.Hashtable;" + NL +
        "/*" + NL +
        " * see blackberry.web.widget.WidgetConfig" + NL +
        " */" + NL +
        "public final class WidgetConfigAutoGen extends WidgetConfigImpl {" + NL +
        "    public WidgetConfigAutoGen() {" + NL;

    private WidgetConfig _widgetConfig;
    
    public WidgetConfig_v1Serializer(WidgetConfig widgetConfig) {
        _widgetConfig = widgetConfig;
    }
    
    public byte[] serialize() {
        
        StringBuilder buffer = new StringBuilder();
        Map<String, String> memberMap = new HashMap<String, String>();
        
        // Populate the basic members
        // Special characters for Java source file: escape /, ", ' for Java source 
        memberMap.put("_id",                  _widgetConfig.getID());
        memberMap.put("_name",                _widgetConfig.getName());
        memberMap.put("_description",         _widgetConfig.getDescription());
        memberMap.put("_content",             _widgetConfig.getContent());
        memberMap.put("_configXML",           _widgetConfig.getConfigXML());
        memberMap.put("_backButtonBehaviour", _widgetConfig.getBackButtonBehaviour());
        memberMap.put("_contentType",         _widgetConfig.getContentType());
        memberMap.put("_contentCharset",      _widgetConfig.getContentCharSet());
        memberMap.put("_license",             _widgetConfig.getLicense());
        memberMap.put("_licenseURL",          _widgetConfig.getLicenseURL());
        memberMap.put("_author",              _widgetConfig.getAuthor());
        memberMap.put("_copyright",           _widgetConfig.getCopyright());
        memberMap.put("_authorEmail",         _widgetConfig.getAuthorEmail());
        memberMap.put("_loadingScreenColor",  _widgetConfig.getLoadingScreenColour());
        memberMap.put("_backgroundImage",     _widgetConfig.getBackgroundImage());
        memberMap.put("_foregroundImage",     _widgetConfig.getForegroundImage());
        memberMap.put("_authorURL",           _widgetConfig.getAuthorURL());
        memberMap.put("_backgroundSource",    _widgetConfig.getBackgroundSource());
        memberMap.put("_foregroundSource",    _widgetConfig.getForegroundSource());
        
        buffer.append(TemplateFile.refactor(AUTOGEN_HEADER));
        Set<Entry<String, String>> entrySet = memberMap.entrySet();
        
        // Iterate memberMap
        for (Entry< String, String > entry : entrySet) {
            String value = entry.getValue();
            if (value != null) {
                buffer.append(entry.getKey()).append(" = \"");
                buffer.append(escapeSpecialCharacterForJavaSource(value));
                buffer.append("\";").append(NL_LVL_0); 
            }
        }
        
        // * present
        if (_widgetConfig.allowMultiAccess()) {
            buffer.append("_hasMultiAccess = true;").append(NL_LVL_0);
        }
       
        // Add icons
        if (_widgetConfig.getIconSrc().size() > 0) {
            buffer.append("_icon = \"").append(_widgetConfig.getIconSrc().firstElement())
                  .append("\";").append(NL_LVL_0);
            if (_widgetConfig.getHoverIconSrc().size() > 0) {
                buffer.append("_iconHover = \"")
                      .append(_widgetConfig.getHoverIconSrc().firstElement())
                      .append("\";").append(NL_LVL_0);           
            }
        }
        
        // Add custom headers
        for( String key : _widgetConfig.getCustomHeaders().keySet()) {
            buffer.append("_customHeaders.addProperty(").append(NL_LVL_0);
            buffer.append("\"").append(key).append("\"," + NL_LVL_1);
            buffer.append("\"").append(escapeSpecialCharacterForJavaSource(_widgetConfig.getCustomHeaders().get(key)))
                  .append("\");").append(NL_LVL_1);
        }
        
        // Set navigation mode
        if (_widgetConfig.getNavigationMode()) {
            buffer.append("_widgetNavigationMode = true;").append(NL_LVL_0);
        }
		
		// Set orientation
        if (_widgetConfig.getOrientationDefined()) {
            buffer.append("_orientationDefined = true;").append(NL_LVL_0);
			buffer.append("_orientation = " + Integer.toString(_widgetConfig.getOrientation()) + ";").append(NL_LVL_0);
        }

        // Add LoadingScreen configuration
        if (_widgetConfig.getFirstPageLoad()) {
            buffer.append("_firstPageLoad = true;").append(NL_LVL_0);
        }        
        if (_widgetConfig.getRemotePageLoad()) {
            buffer.append("_remotePageLoad = true;").append(NL_LVL_0);
        }     
        if (_widgetConfig.getLocalPageLoad()) {
            buffer.append("_localPageLoad = true;").append(NL_LVL_0);
        }     
        
        // Add TransitionEffect configuration
        if (_widgetConfig.getTransitionType() != null) {
            buffer.append("_transitionType = ").append(_widgetConfig.getTransitionType())
                  .append(";").append(NL_LVL_0);
            
            if (_widgetConfig.getTransitionDuration() >= 0) {
                buffer.append("_transitionDuration = ")
                      .append(_widgetConfig.getTransitionDuration())
                      .append(";").append(NL_LVL_0);
            }
            
            if (_widgetConfig.getTransitionDirection() != null) {
                buffer.append("_transitionDirection = ")
                      .append(_widgetConfig.getTransitionDirection())
                      .append(";").append(NL_LVL_0);
            }            
        }
        
        // Add cache options
        if (_widgetConfig.isCacheEnabled() != null) {
            buffer.append("_cacheEnabled = ").append(_widgetConfig.isCacheEnabled())
                  .append(";").append(NL_LVL_0);
        }
        if (_widgetConfig.getAggressiveCacheAge() != null) {
        	// Enable aggressive caching if applicable
        	if(_widgetConfig.isAggressiveCacheEnabled()!=null) {
	        	buffer.append("_aggressivelyCaching = ")
	        	      .append(_widgetConfig.isAggressiveCacheEnabled()).append(";")
	        	      .append(NL_LVL_0);
        	}
            buffer.append("_aggressiveCacheAge = ")
                  .append(_widgetConfig.getAggressiveCacheAge())
                  .append(";").append(NL_LVL_0);
        }        
        if (_widgetConfig.getMaxCacheSize() != null) {
            buffer.append("_maxCacheSize = ").append(_widgetConfig.getMaxCacheSize())
                  .append(";").append(NL_LVL_0);
        }
        if (_widgetConfig.getMaxCacheItemSize() != null) {
            buffer.append("_maxCacheable = ").append(_widgetConfig.getMaxCacheItemSize())
                  .append(";").append(NL_LVL_0);
        }
        // Debug issue fix ?
        if(_widgetConfig.isDebugEnabled()) {
        	buffer.append("_debugEnabled = true;").append(NL_LVL_0);
        }
        
        // Auto-Startup options
        if(_widgetConfig.allowInvokeParams()) {
        	buffer.append("_allowInvokeParams = ").append(_widgetConfig.allowInvokeParams())
        	      .append(";").append(NL_LVL_0);
        }
        
        if(_widgetConfig.isStartupEnabled()) {
        	buffer.append("_runOnStartup = ").append(_widgetConfig.isStartupEnabled())
        	      .append(";").append(NL_LVL_0);
        }
        
        // Add 3rd party extensions
        for (int j = 0; j < _widgetConfig.getExtensionClasses().size(); j++) {
        	String extensionClass = _widgetConfig.getExtensionClasses().elementAt(j);
        	buffer.append("_widgetExtensions.addElement(new ")
        	      .append(extensionClass).append("());").append(NL_LVL_0);
        	
        }

        // Add extension JS files
        for (int j = 0; j < _widgetConfig.getExtensionJSFiles().size(); j++) {
            String extensionJSFile = _widgetConfig.getExtensionJSFiles().elementAt(j);
            buffer.append("_jsInjectionPaths.addElement(\"").append(extensionJSFile).append("\");").append(NL_LVL_0);
        }
        
        // Add shared global JS files
        for (int j = 0; j < _widgetConfig.getSharedGlobalJSFiles().size(); j++) {
            String sharedGlobalJSFile = _widgetConfig.getSharedGlobalJSFiles().elementAt(j);
            buffer.append("_sharedGlobalJSInjectionPaths.addElement(\"").append(sharedGlobalJSFile).append("\");").append(NL_LVL_0);
        }
        
        // Add transport
        if (_widgetConfig.getTransportTimeout() >= 0) {
            buffer.append("_transportTimeout = new Integer(")
                  .append(_widgetConfig.getTransportTimeout()).append(");").append(NL_LVL_0);
        }
        if (_widgetConfig.getTransportOrder() != null) {
            buffer.append("_preferredTransports = new int[]{").append(NL_LVL_0);
            for(int i=0; i<_widgetConfig.getTransportOrder().length; i++) {
                String transport = _widgetConfig.getTransportOrder()[i];
                if (i+1 != _widgetConfig.getTransportOrder().length) {
                    transport += ",";
                }
                buffer.append(transport).append(NL_LVL_1);
            }
            buffer.append("};").append(NL_LVL_0);
        }
        
        // Add access/features
        if (_widgetConfig.getAccessTable().size() > 0) {
            String line;
            buffer.append("try {").append(NL_LVL_0);
            for (WidgetAccess key : _widgetConfig.getAccessTable().keySet()) {
                line = "_accessList.put(";
                buffer.append(line).append(NL_LVL_1);
                String uri = key.getURI().toString();
                if (uri.equals("WidgetConfig.WIDGET_LOCAL_DOMAIN")) {
                    line = uri + ",";
                } else {
                    line = "\"" + uri + "\"" + ",";
                }
                buffer.append(line).append(NL_LVL_2);
                buffer.append("new WidgetAccess(").append(NL_LVL_2);
                buffer.append(line).append(NL_LVL_3);
                line = (Boolean.valueOf(key.allowSubDomain())).toString() + ",";
                buffer.append(line).append(NL_LVL_3);
                buffer.append("new WidgetFeature[] {").append(NL_LVL_3);

                Vector<?> wfList = (Vector<?>)_widgetConfig.getAccessTable().get(key);
                for (int j = 0; j < wfList.size(); j++) {
                    WidgetFeature wf = (WidgetFeature) wfList.get(j);
                    buffer.append("new WidgetFeature(").append(NL_LVL_4);
                    line = "\"" + wf.getID() + "\"" + ",";
                    buffer.append(line).append(NL_LVL_5);
                    line = (Boolean.valueOf(wf.isRequired())).toString() + ",";
                    buffer.append(line).append(NL_LVL_5);
                    line = "\"" + wf.getVersion() + "\"" + ",";
                    buffer.append(line).append(NL_LVL_5);
                    line = "null)";
                    if (j+1 != wfList.size()) {
                        line += ",";
                    }
                    buffer.append(line).append(NL_LVL_5);
                }
                buffer.append("}").append(NL_LVL_3);
                buffer.append(")").append(NL_LVL_2);
                buffer.append(");").append(NL_LVL_1);
            }
            buffer.append("} catch (Exception e) {").append(NL_LVL_0);
            buffer.append("// ignore this element - invalid URI").append(NL_LVL_1);
            buffer.append("}").append(NL_LVL_0);
        }        
        buffer.append(NL).append(TAB).append("}").append(NL).append("}");
        return buffer.toString().getBytes();
    }
    
	private String escapeSpecialCharacterForJavaSource(String s) {
		// process escaped characters
		// " -> \\\\\"
		// ' -> \\\\\'
		// \ -> \\\\\\\\
		// NOTE: \\\\ (4 SLASHES) stand for 1 \ (SLASH)
		
		if (s == null)
		    return null;
		
		String ret = 
			s.replaceAll(Pattern.quote("\\"), "\\\\\\\\")
			 .replaceAll(Pattern.quote("\""), "\\\\\"")
			 .replaceAll(Pattern.quote("\'"), "\\\\\'");
		return ret;
	}    
}

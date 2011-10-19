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
package blackberry.core;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetException;


/**
 * Webworks extension developers should implement this interface in order for the framework to call their third party extension.
 * 
 * @author nbhasin
 */
public interface IJSExtension {
    /**
     * Invoke will be called when an XMLHttpRequest is made from JavaScript.
     * 
     * @param request
     *            - Request Object that came in.
     * @param response
     *            - Response object that must be filled out.
     * @param methodCalled
     *            - String method name that is being invoked.
     * @param arguments
     *            - Array of arguments.
     *
     * @throws WidgetException
     */
    void invoke( JSExtensionRequest request, JSExtensionResponse response ) throws WidgetException;

    /**
     * Method is called to get a list of the features that this extension should be called for.
     * 
     * @return - String[]
     */
    String[] getFeatureList();
    
    /**
     * Load JavaScript into scriptEngine and detect page change.
     * 
     * @param feature
     *            - Feature ID.
     * @param version
     *            - Version of the feature.
     * @param document
     *            - Document being loaded.
     * @param scriptEngine
     *            - JavaScript Engine.
     * @param jsInjectionPaths
     *            - List of all extension JS files, the extension will only load its own JS files, not all of them.
     */
    void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine, SimpleSortingVector jsInjectionPaths );

    /**
     * Registers the extension - information about the widget is passed to the extension.
     *
     * @param widgetConfig
     * @param browserField
     */
    void register( WidgetConfig widgetConfig, BrowserField browserField );
    
    /**
     * Cleans up the extension, reset states in Java side
     */
    void unloadFeatures();
}

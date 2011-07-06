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
package blackberry.utils;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

/**
 * Utils extension
 */
public class UtilsExtension implements WidgetExtension {

    private static final String FEATURE_BLACKBERRY_UTILS = "blackberry.utils";
    private static ScriptEngine _scriptEngine;

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { FEATURE_BLACKBERRY_UTILS };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(String, String, Document, ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {
        Object obj = null;
        _scriptEngine = scriptEngine;
        if( feature.equals( FEATURE_BLACKBERRY_UTILS ) ) {
            obj = new UtilsNamespace();
        }
        if( obj != null ) {
            scriptEngine.addExtension( feature, obj );
        }
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
    }

    /**
     * Get the current script engine.
     * 
     * @return the current script engine
     */
    public static ScriptEngine getScriptEngine() {
        return _scriptEngine;
    }
}

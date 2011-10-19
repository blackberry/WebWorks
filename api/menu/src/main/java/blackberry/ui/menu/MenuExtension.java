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
package blackberry.ui.menu;

import org.w3c.dom.Document;

import java.lang.ref.WeakReference;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

/**
 * UI Menu extension
 */
public final class MenuExtension implements WidgetExtension {

    public static final String NAME = "blackberry.ui.menu";
    private static WeakReference _browserField;
        
    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { NAME };
    }
    
    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(Strng, String, Document, ScriptEngine)
     */
    public void loadFeature( final String feature, final String version, final Document doc, final  ScriptEngine scriptEngine ) throws Exception {
        Object obj = null;

        if (feature.equals(NAME)) {
            obj = MenuNamespace.getInstance();
        }
        if (obj != null) {
            scriptEngine.addExtension(feature, obj);
        }
    }
    
    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( final WidgetConfig widgetConfig, final  BrowserField browserField ) {
        _browserField = new WeakReference( browserField );
    }
    
    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
        // do nothing
    }
    
    /**
     * Get the current BrowserFiled. 
     * 
     * @return the current BrowserFiled
     */
    public static BrowserField getBrowserField() {
        return ( BrowserField ) _browserField.get();
    }

}


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
package blackberry.identity;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

/**
 * This is a main entry class implements WidgetExtension
 * 
 * @author sgolod
 * 
 */
public class IdentityExtension implements WidgetExtension {

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { IdentityNamespace.NAME };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(java.lang.String, java.lang.String, org.w3c.dom.Document,
     *      net.rim.device.api.script.ScriptEngine)
     */
    public void loadFeature( final String feature, final String version, final Document doc, final ScriptEngine scriptEngine )
            throws Exception {
        Object obj = null;

        if( feature.equals( IdentityNamespace.NAME ) ) {
            obj = new IdentityNamespace();
        }

        if( obj != null ) {
            scriptEngine.addExtension( feature, obj );
        }
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(net.rim.device.api.web.WidgetConfig,
     *      net.rim.device.api.browser.field2.BrowserField)
     */
    public void register( final WidgetConfig widgetConfig, final BrowserField browserField ) {
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(org.w3c.dom.Document)
     */
    public void unloadFeatures( final Document doc ) {
    }

}

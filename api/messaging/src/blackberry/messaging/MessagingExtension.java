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
package blackberry.messaging;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

/**
 * Implementation of blackberry.message extension
 */
public class MessagingExtension implements WidgetExtension {

    public static final String FEATURE_NAME = "blackberry.message";
    public static WidgetConfig _widgetConfig = null;
    private static Scriptable _smsNamespace = null;

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { FEATURE_NAME };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(String, String, Document, ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {
        WidgetExtension _smsEX = null;
        if( _widgetConfig != null ) {
            _smsEX = _widgetConfig.getExtensionForFeature( FEATURE_NAME + ".sms" );
        }

        if( _smsEX != null ) {
            Class cl = Class.forName( "blackberry.message.sms.SMSNamespace" );
            _smsNamespace = (Scriptable)cl.newInstance();
        }
        Object obj = null;
        if( feature.equals( FEATURE_NAME ) ) {
            obj = new MessagingNamespace();
        }
        if( obj != null ) {
            scriptEngine.addExtension( feature, obj );
        }

    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
        MessagingExtension._widgetConfig = widgetConfig;
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
        // do nothing
    }

    public static Scriptable getSmsNamespace() {
        return _smsNamespace;
    }

}

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
package blackberry.phone;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

import blackberry.phone.call.CallNamespace;
import blackberry.phone.calllog.CallLogNamespace;
import blackberry.phone.find.FindNamespace;

/**
 * 
 */
public class PhoneExtension implements WidgetExtension {

    /* @Override */
    public String[] getFeatureList() {
        return new String[] { CallNamespace.NAME, CallLogNamespace.NAME, FindNamespace.NAME };
    }

    /* @Override */
    public void loadFeature(final String feature, final String version, final Document doc, final ScriptEngine scriptEngine) throws Exception {
        if (feature.equals(CallNamespace.NAME)) {
            scriptEngine.addExtension(feature, CallNamespace.getInstance());
        }
        else if (feature.equals(CallLogNamespace.NAME)) {
            scriptEngine.addExtension(feature, CallLogNamespace.getInstance());
        }
        else if (feature.equals(FindNamespace.NAME)) {
            scriptEngine.addExtension(feature, FindNamespace.getInstance());
        }
    }

    /* @Override */
    public void register(final WidgetConfig widgetConfig, final BrowserField browserField) {
        // TODO Auto-generated method stub
    }

    /* @Override */
    public void unloadFeatures(final Document doc) {
        // TODO Auto-generated method stub
    }
}

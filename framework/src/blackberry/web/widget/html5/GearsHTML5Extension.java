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
package blackberry.web.widget.html5;

import org.w3c.dom.Document;

import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.io.IOUtilities;

import java.io.InputStream;
import java.io.IOException;

/**
 * Class to load and execute the HTML5 to gears shim
 */
public final class GearsHTML5Extension implements WidgetExtension {

    private String shimURL = "/js/html5_init.js";
    private String shimContent;

    /*
     * Default constructor for the class.
     */
    public GearsHTML5Extension() {
        loadShim();
    }

    public String[] getFeatureList() {
        return null;
    }
    
    /*
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(java.lang.String, java.lang.String, org.w3c.dom.Document, net.rim.device.api.script.ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {
        // Load shim content into script engine and execute
        Object compiledScript = scriptEngine.compileScript( shimContent );
        scriptEngine.executeCompiledScript( compiledScript, null );
    }

    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
        return;
    }

    public void unloadFeatures( Document doc ) {
        return;
    }

    /* 
     * Method that loads contents of shimURL variable to be executed by the 
     * Script Engine 
     */
    private void loadShim() {
        InputStream input = null;
        try {
            input = this.getClass().getResourceAsStream( shimURL );
            byte[] data = IOUtilities.streamToBytes( input );
            shimContent = new String( data );
        } catch( IOException e ) {
            // Should not happen
            // Can only be thrown if I/O error converting input stream
            // into byte array
        } finally {
            try {
                if( input != null ) {
                    input.close();
                }
            } catch( IOException e ) {
                // Should not happen
            }
        }
    }
}

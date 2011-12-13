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
package blackberry.web.widget.bf.navigationcontroller;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

/**
 * Extension for injecting navmode.js into the script engine
 */
public class NavigationExtension implements WidgetExtension {

    private String navmodeURL = "/js/navmode.js";
    private String navmodeContent;    
    
    public NavigationExtension() {
        loadJS();
    }
    
    public String[] getFeatureList() {
        return null;
    }

    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {
        // Load navmode content into script engine and execute
        Object compiledScript = scriptEngine.compileScript( navmodeContent );
        scriptEngine.executeCompiledScript( compiledScript, null );
    }


    public void register( WidgetConfig arg0, BrowserField arg1 ) {
        return;
    }

    public void unloadFeatures( Document arg0 ) {
        return;
    }

    /* 
     * Method that loads contents of navmode JS file to be executed by the 
     * Script Engine 
     */
    private void loadJS() {
        InputStream input = null;
        try {
            input = this.getClass().getResourceAsStream( navmodeURL );
            byte[] data = IOUtilities.streamToBytes( input );
            navmodeContent = new String( data );
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

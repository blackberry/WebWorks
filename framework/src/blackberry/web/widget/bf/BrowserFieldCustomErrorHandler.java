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
package blackberry.web.widget.bf;

import blackberry.web.widget.Widget;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.impl.WidgetError;
import blackberry.web.widget.impl.WidgetException;
import blackberry.web.widget.device.DeviceInfo;
import blackberry.web.widget.exception.MediaHandledException;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldErrorHandler;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.web.WidgetConfig;

import javax.microedition.io.InputConnection;

/**
 * Custom error handler class for WebWorks.
 */
public class BrowserFieldCustomErrorHandler extends BrowserFieldErrorHandler {
    private WidgetConfig    _config;
    
    /* Constructor */
    BrowserFieldCustomErrorHandler(BrowserField browserField, WidgetConfig widgetConfig) {
           super(browserField);
           _config = widgetConfig;
    }
    
     /*Override*/ public void displayContentError(String url, InputConnection connection, Throwable t) {        
		if(url == null){
			invokeError(t, null);
		}
		else{
			invokeError(t, url);
		}       
    }
    
    /*Override*/ public void requestContentError(BrowserFieldRequest request, Throwable t) {
        invokeError(t, request.getURL());
    } 
    
    /*Override*/ public void navigationRequestError(BrowserFieldRequest request, Throwable t) {
                
                // Do not display an error message if our custom exception is found.
                if(t instanceof MediaHandledException){
                        return;
                }
        invokeError(t, request.getURL());
    }
    
    private void invokeError(Throwable t, String url) {
        
        WidgetConfigImpl cfg = (WidgetConfigImpl) _config;
        
        if(cfg.getForegroundSource().length() == 0 
                && cfg.getBackgroundSource() != null 
                || ApplicationManager.getApplicationManager().inStartup()) {
            EventLogger.logEvent(Widget.WIDGET_GUID, t.getMessage().getBytes());
            return;
        }
        
        WidgetError we = null;
        try {
            canConnect(url);
            we = new WidgetError(t, url);
        }
        catch (Exception e) {
            we = new WidgetError(e, url);
        }
        Application.getApplication().invokeAndWait( we );        
       
    }
    
    /**
     * This method checks for some more specific errors than BF2 - radio off, out of coverage
     * which allows for a better detailed message.
     * @param url The URL that was being retrieved when the error occured.
     */
    private void canConnect(String url) throws Exception {
        // Null urls not checked.
        if (url == null) {
            return;
        }
        
        // If this is a 'local' resource, then no need to check radio/data coverage.
        url = url.trim().toLowerCase();
        if (url.startsWith("file:") || url.startsWith("cod:") || url.startsWith("local:")) {
            return;
        }
                
        // Use network API to determine availability.
        boolean isAvailable = false;
        int[] transports = ((WidgetConfigImpl)_config).getPreferredTransports();
        if (transports == null) {
            transports = BrowserFieldScreen.PREFERRED_TRANSPORTS;
        }
        int numTransports = transports.length;
        int transport;
        for (int i=0; i<numTransports; i++) {
            transport = transports[i];
            if (TransportInfo.isTransportTypeAvailable(transport)) {
                isAvailable = true;
                break;
            }
        }
        if (!isAvailable) {
            throw new WidgetException( WidgetException.ERROR_NETWORK_NOT_AVAILABLE, url );
        }
        
        // Use network API to determine coverage.
        boolean hasCoverage = false;
        for (int i=0; i<numTransports; i++) {
            transport = transports[i];
            if (TransportInfo.hasSufficientCoverage(transport)) {
                hasCoverage = true;
                break;
            }
        }        
        if (!hasCoverage) {
            throw new WidgetException( WidgetException.ERROR_INSUFFICIENT_COVERAGE, url );
        }
    }
}



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
 
package blackberry.system.event;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;

import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetException;
import blackberry.common.util.JSUtilities;

import blackberry.core.IJSExtension;
import blackberry.core.JSExtensionRequest;
import blackberry.core.JSExtensionResponse;

import blackberry.system.event.SystemEventManager;
import blackberry.system.event.SystemEventReturnValue;

/**
 * JavaScript extension for blackberry.system.event<br>
 * 
 * Uses long-polling to handle callbacks.<br>
 * 
 * When a callback is registered in JavaScript, the following sequence of events takes place:
 * <ol>
 * <li>A "register" call is made with the event name.</li>
 * <li>If the event is recognized and successfully registered with the BlackBerry API, OK is returned.</li>
 * <li>When the system event occurs, it is queued in an event queue for consumption by the "poll" call.</li>
 * <li>The JavaScript layer makes a "poll" call as long as we are listening to at least one event. It waits on the event queue until an event occurs, at which point it returns it.</li>
 * <li>Client side immediately issues a new request to listen for the event.<li>
 * <li>Cycle continues until user unregisters the last callback and a stop listening event is sent back instead to release the last open polling connection.</li>
 * </ol>
 * @author ababut
 */
public class SystemEventExtension implements IJSExtension {
    private static final String FUNCTION_REGISTER = "register";
    private static final String FUNCTION_UNREGISTER = "unregister";
    private static final String FUNCTION_POLL = "poll";
    
    private static String[] JS_FILES = { "system_event_dispatcher.js", "system_event_ns.js" };    

    private SystemEventManager _eventManager = new SystemEventManager();

    /**
     * Implements invoke() of interface IJSExtension. Responsible for parsing request method and its optional argument.
     * The operation is one of (register/unregister/poll).
     * The event argument is one of (onCoverageChange/onHardwareKey).
     * The arg argument is optional and applies only to the hardware key event. It contains a numeric constant representing the key to listen for.     
     *
     * @throws WidgetException 
     */
    public void invoke( JSExtensionRequest request, JSExtensionResponse response ) throws WidgetException {
        String op = request.getMethodName();
        Object[] args = request.getArgs();
        
        //Event and optional argument we'll be operating on, default to empty string to avoid NPEs
        String event = (args != null && args.length > 0) ? (String) request.getArgumentByName( "event" ) : "";
        String eventArg = (args != null && args.length > 1) ? (String) request.getArgumentByName( "arg" ) : "";
        
        SystemEventReturnValue returnValue = null;
        
        //Dispatch the function call or complain that we don't recognize it
        try {
            if(FUNCTION_REGISTER.equals(op)) {
                _eventManager.listenFor(event, eventArg);
                returnValue = SystemEventReturnValue.getSuccessForOp(FUNCTION_REGISTER, event);
            } else if(FUNCTION_UNREGISTER.equals(op)) {
                _eventManager.stopListeningFor(event, eventArg);
                returnValue = SystemEventReturnValue.getSuccessForOp(FUNCTION_UNREGISTER, event);
            } else if(FUNCTION_POLL.equals(op)) {
                returnValue = _eventManager.getNextWaitingEvent();
            } else {
                returnValue = SystemEventReturnValue.INVALID_METHOD;
            }
        } catch (RuntimeException e) {
            returnValue = SystemEventReturnValue.getErrorForOp(op, event);
        }
        
        response.setPostData( returnValue.getJSExtensionReturnValue().getReturnValue().toString().getBytes() );
    }
    
    /**
     * @see blackberry.core.IJSExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { "blackberry.system.event" };
    }

    /**
     * @see blackberry.core.IJSExtension#loadFeature(String, String, Document, ScriptEngine, jsInjectionPaths)
     */
    public void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine,
            SimpleSortingVector jsInjectionPaths ) {
        JSUtilities.loadJS( scriptEngine, JS_FILES, jsInjectionPaths );
    }

    /**
     * @see blackberry.core.IJSExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
    }

    /**
     * @see blackberry.core.IJSExtension#unloadFeatures()
     */
    public void unloadFeatures() {
        // Clear event listeners when page is done        
        reset();
    }
    
    private void reset() {
       _eventManager.shutDown();
    }
}

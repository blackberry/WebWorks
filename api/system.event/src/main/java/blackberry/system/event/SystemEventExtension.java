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

import java.util.Vector;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.CoverageStatusListener;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.util.IntEnumeration;
import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetException;
import blackberry.common.util.JSUtilities;
import blackberry.common.util.json4j.JSONObject;
import blackberry.core.IJSExtension;
import blackberry.core.JSExtensionRequest;
import blackberry.core.JSExtensionResponse;
import blackberry.core.JSExtensionReturnValue;

/**
 * JavaScript extension for blackberry.system.event<br>
 * 
 * Uses long-polling to handle callbacks.<br>
 * 
 * When user starts listening for an event (by specifying a callback function), the following happens:
 * <ol>
 * <li>The current thread blocks till the desired event occurs.</li>
 * <li>The thread is notified causing the HTTP request to return.</li>
 * <li>The callback function gets invoked on the client side.</li>
 * <li>Client side immediately issues a new request to listen for the event. (cycle continues until user unregisters the callback)</li>
 * </ol>
 * @author rtse
 */
public class SystemEventExtension implements IJSExtension, ISystemEventExtensionConstants, KeyListener {
    private static Vector SUPPORTED_METHODS;

    static {
        SUPPORTED_METHODS = new Vector();
        SUPPORTED_METHODS.addElement( REQ_FUNCTION_ON_COVERAGE_CHANGE );
        SUPPORTED_METHODS.addElement( REQ_FUNCTION_ON_HARDWARE_KEY );
    }
    
    private static String[] JS_FILES = { "system_event_dispatcher.js", "system_event_ns.js" };    

    private CoverageMonitor _currentCoverageMonitor;
    private IntHashtable _keyRegistry = new IntHashtable();

    public String[] getFeatureList() {
        String[] featureList;
        featureList = new String[ 1 ];
        featureList[ 0 ] = FEATURE_ID;
        return featureList;
    }

    private static boolean parseBoolean( String str ) {
        return ( str != null && str.equals( Boolean.TRUE.toString() ) );
    }

    private void reset() {
        if( _currentCoverageMonitor != null && _currentCoverageMonitor.isListening() ) {
            _currentCoverageMonitor.stop();
        }
        _currentCoverageMonitor = null;

        if( !_keyRegistry.isEmpty() ) {
            IntEnumeration keys = _keyRegistry.keys();

            while( keys.hasMoreElements() ) {
                Object tmp = _keyRegistry.remove( keys.nextElement() );

                // even though the key is no longer being monitored, we still need to release any thread that is waiting on its
                // lock
                if( tmp != null ) {
                    synchronized( tmp ) {
                        tmp.notify();
                    }
                }
            }
        }
    }

    /**
     * Implements invoke() of interface IJSExtension. Methods of extension will be called here.
     * @throws WidgetException 
     */
    public void invoke( JSExtensionRequest request, JSExtensionResponse response ) throws WidgetException {
        String method = request.getMethodName();
        Object[] args = request.getArgs();
        String msg = "";
        int code = JSExtensionReturnValue.SUCCESS;
        JSONObject data = new JSONObject();
        JSONObject returnValue;

        if( !SUPPORTED_METHODS.contains( method ) ) {
            throw new WidgetException( "Undefined method: " + method );
        }

        try {
            if( method.equals( REQ_FUNCTION_ON_COVERAGE_CHANGE ) ) {
                if( args != null && args.length > 0 ) {
                    String monitor = (String) request.getArgumentByName( ARG_MONITOR );

                    data.put( ARG_MONITOR, monitor );

                    listenToCoverageChange( parseBoolean( monitor ) );
                }
            } else if( method.equals( REQ_FUNCTION_ON_HARDWARE_KEY ) ) {
                if( args != null && args.length > 1 ) {
                    int key = Integer.parseInt( (String) request.getArgumentByName( ARG_KEY ) );
                    String monitor = (String) request.getArgumentByName( ARG_MONITOR );

                    data.put( ARG_MONITOR, monitor );
                    data.put( ARG_KEY, key );

                    listenToKey( parseBoolean( monitor ), key );
                }
            }
        } catch( Exception e ) {
            msg = e.getMessage();
            code = JSExtensionReturnValue.FAIL;
        }

        returnValue = new JSExtensionReturnValue( msg, code, data ).getReturnValue();
        response.setPostData( returnValue.toString().getBytes() );
    }

    private void listenToCoverageChange( boolean register ) throws Exception {
        if( register ) {
            if( _currentCoverageMonitor == null ) {
                _currentCoverageMonitor = new CoverageMonitor();
            }

            // blocks
            _currentCoverageMonitor.run();

            // this block is hit when the thread gets notified by the de-registration
            // throw exception to indicate to JS client that callback should not be invoked
            if( !_currentCoverageMonitor.isListening() ) {
                _currentCoverageMonitor = null;
                throw new Exception( "Coverage change is no longer monitored" );
            }
        } else {
            if( _currentCoverageMonitor != null && _currentCoverageMonitor.isListening() ) {
                _currentCoverageMonitor.stop();
            }
        }
    }
    
    /**
     * Helper class to implement CoverageStatusListener
     */
    private class CoverageMonitor implements CoverageStatusListener {
        private Object _lock;
        private boolean _listening;

        public CoverageMonitor() {
            _lock = new Object();
            _listening = true;
            CoverageInfo.addListener( this );
        }

        public void coverageStatusChanged( int newCoverage ) {
            synchronized( _lock ) {
                _lock.notify();
            }
        }

        public void run() throws InterruptedException {
            synchronized( _lock ) {
                _lock.wait();
            }
        }

        public void stop() {
            CoverageInfo.removeListener( this );
            _listening = false;
            synchronized( _lock ) {
                _lock.notify();
            }
        }

        public boolean isListening() {
            return _listening;
        }
    }

    public boolean keyChar( char arg0, int arg1, int arg2 ) {
        return false;
    }

    private void listenToKey( boolean register, int key ) throws Exception {
        Object lock = new Object();

        if( register ) {
            synchronized( lock ) {
                _keyRegistry.put( key, lock );

                if( !_keyRegistry.isEmpty() ) {
                    // wait on the lock for the specific key
                    lock.wait();
                }
            }

            // this block is hit when the thread gets notified by the de-registration
            // throw exception to indicate to JS client that callback should not be invoked
            if( !_keyRegistry.containsKey( key ) ) {
                throw new Exception( "Key " + key + " is no longer monitored" );
            }
        } else {
            Object tmp = _keyRegistry.remove( key );

            // even though the key is no longer being monitored, we still need to release any thread that is waiting on its
            // lock
            if( tmp != null ) {
                synchronized( tmp ) {
                    tmp.notify();
                }
            }
        }
    }

    /**
     * @see net.rim.device.api.system.KeyListener#keyDown(int, int)
     */
    public boolean keyDown( int keycode, int time ) {
        int keyPressed = Keypad.key( keycode );
        int event;

        switch( keyPressed ) {
            case Keypad.KEY_CONVENIENCE_1:
                event = IKEY_CONVENIENCE_1;
                break;
            case Keypad.KEY_CONVENIENCE_2:
                event = IKEY_CONVENIENCE_2;
                break;
            case Keypad.KEY_MENU:
                event = IKEY_MENU;
                break;
            case Keypad.KEY_SEND:
                event = IKEY_STARTCALL;
                break;
            case Keypad.KEY_END:
                event = IKEY_ENDCALL;
                break;
            case Keypad.KEY_ESCAPE:
                event = IKEY_BACK;
                break;
            case Keypad.KEY_VOLUME_DOWN:
                event = IKEY_VOLUME_DOWN;
                break;
            case Keypad.KEY_VOLUME_UP:
                event = IKEY_VOLUME_UP;
                break;
            default:
                return false;
        }

        if( _keyRegistry.containsKey( event ) ) {
            Object lock = _keyRegistry.get( event );
            synchronized( lock ) {
                // notify the thread waiting for that specific key's lock
                lock.notify();
            }

            return true;
        }

        return false;
    }

    public boolean keyRepeat( int arg0, int arg1 ) {
        return false;
    }

    public boolean keyStatus( int arg0, int arg1 ) {
        return false;
    }

    public boolean keyUp( int arg0, int arg1 ) {
        return false;
    }

    public void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine,
            SimpleSortingVector jsInjectionPaths ) {
        JSUtilities.loadJS( scriptEngine, JS_FILES, jsInjectionPaths );
    }

    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {

    }

    public void unloadFeatures() {
        // Clear states when page is done        
        reset();
    }
}

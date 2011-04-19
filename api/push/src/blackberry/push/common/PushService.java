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
package blackberry.push.common;

import java.util.Enumeration;

import blackberry.core.ApplicationEventHandler;

import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.util.IntHashtable;
import blackberry.core.EventService;

/**
 * Service that handles all the active push listeners in the application.
 */
public class PushService {
    
    public static final int CHUNK_SIZE = 256;
    private IntHashtable    _pushListeners;
    private OnExitHandler   _onExitHandler;

    /**
     * Retrieves an instance of this object.
     * 
     * @return an instance of the PushService object.
     */
    public static PushService getInstance() {
        // Use 'Initialization on demand holder' idiom
        return InstanceHolder.INSTANCE;
    }
    
    private static class InstanceHolder {
        private static final PushService INSTANCE = new PushService();
    }
 
    private PushService() {
        _pushListeners = new IntHashtable();
        _onExitHandler = new OnExitHandler();
    }

    /**
     * Creates a new push listener to listen on a specified port.
     * 
     * @param port
     *            the port to listen to for push messages.
     * @param callback
     *            scriptable function to invoke when a push message is received.
     * @param maxQueueCount
     *            the maximum number of push messages to queue before oldest messages are discarded.
     */
    public void openPushChannel( int port, ScriptableFunction callback, int maxQueueCount ) throws Exception {
        // check port
        if( isValidPort( port ) ) {
            PushListener listener = (PushListener) _pushListeners.get( port );

            // new listener
            if( listener == null ) {
                listener = new PushListener( port, callback, maxQueueCount );
                _pushListeners.put( port, listener );

                // new listener - create onExitHandler
                if( _pushListeners.size() == 1 ) {
                    setOnExitListener( true );
                }
                listener.start();
            }
            // update existing listener
            else {
                listener.updateCallback( callback );
            }
        } else {
            throw new IllegalArgumentException( "Reserved port" );
        }
    }

    /**
     * Closes a push channel.
     * 
     * @param port
     *            the port the push listener was opened on.
     */
    public void closePushChannel( int port ) {
        PushListener listener = (PushListener) _pushListeners.get( port );
        if( listener != null ) {
            listener.stop();
            _pushListeners.remove( port );

            // stop onExitListener
            if( _pushListeners.size() == 0 ) {
                setOnExitListener( false );
            }
        }
    }

    private void setOnExitListener( boolean activate ) {
        if( activate ) {
            EventService.getInstance().addHandler( ApplicationEventHandler.EVT_APP_EXIT, _onExitHandler );
        } else {
            EventService.getInstance().removeHandler( ApplicationEventHandler.EVT_APP_EXIT, _onExitHandler );
        }
    }

    /**
     * Validates the port is not reserved or in use.
     * 
     * @param port
     *            the port to validate.
     * @return <code>true</code> if the port is valid; <code>false</code> otherwise.
     */
    private boolean isValidPort( int port ) {
        switch( port ) {
            case 80:
            case 443:
            case 7874:
            case 8080:
                return false;
            default:
                return true;
        }
    }

    final class OnExitHandler implements ApplicationEventHandler {

        /**
         * @see blackberry.core.ApplicationEventHandler#handleEvent(int, java.lang.Object[])
         */
        public void handleEvent( int eventID, Object[] args ) {
            // close all push ports
            Enumeration listenerEnum = _pushListeners.elements();
            PushListener listener;
            while( listenerEnum.hasMoreElements() ) {
                listener = (PushListener) listenerEnum.nextElement();
                listener.stop();
            }
            _pushListeners.clear();
            setOnExitListener( false );
        }

        /**
         * @see blackberry.core.ApplicationEventHandler#handlePreEvent(int, java.lang.Object[])
         */
        public boolean handlePreEvent( int eventID, Object[] args ) {
            // do nothing
            return false;
        }
    }
}

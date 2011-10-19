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
package blackberry.push;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.push.PushApplicationDescriptor;
import net.rim.blackberry.api.push.PushApplicationRegistry;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.util.IntHashtable;
import blackberry.common.push.PushDaemon;
import blackberry.common.push.PushPersistentStore;
import blackberry.core.ApplicationEventHandler;
import blackberry.core.EventService;
import blackberry.core.WidgetProperties;

/**
 * Service that handles all the active push listeners in the application.
 */
public class PushService {

    public static final int BES_PUSH = 0;
    public static final int BIS_PUSH = 1;
    private IntHashtable _pushListeners;
    private PushListener2 _pushListener2;
    private OnExitHandler _onExitHandler;
    public static final long PUSHSERVICE_GUID = Long.parseLong( PushService.class.getName().hashCode() + "", 16 );

    private PushDaemon.DaemonStore _daemonStore;

    /**
     * Retrieves the singleton instance of this object.
     * 
     * @return Singleton instance of the PushService object.
     */
    public synchronized static PushService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final PushService INSTANCE = new PushService();
    }

    /*
     * private static class InstanceHolderPush
     */
    private PushService() {
        // store the application descriptor and push in RuntimeStore so that it can be accessed from daemon.
        _daemonStore = PushDaemon.DaemonStore.loadFromStore();
        // persistent the application descriptor
        PushPersistentStore.setAppDescArgs( ApplicationDescriptor.currentApplicationDescriptor().getArgs() );

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
    }

    /**
     * Register PushApplication on the PPG server and create new push listener to listen on specific port
     * 
     * @param port
     *            The port on the device to listen for pushes on.
     * @param appId
     *            The id provided to you for your push application after signing up to use the BlackBerry Push Service.
     * @param serverUrl
     *            The URL for the PPG. Examples of this URL include: http://pushapi.eval.blackberry.com if using the eval
     *            environment of the BlackBerry Push Service or http://pushapi.na.blackberry.com if using the production
     *            environment of the BlackBerry Push Service.
     * @param entryPage
     *            The page that will be shown when application closes and a push data arrives.
     * @param maxQueueCap
     *            The maximum number of pushes to queue before the oldest pushes are discarded.
     * @param onData
     *            The ScriptableFunction that is invoked when a new push has been received.
     * @param onRegister
     *            The ScriptableFunction that is invoked when the result of the registration performed during the opening of the
     *            push listener is received.
     * @param onSimChange
     *            The ScriptableFunction that is invoked on a SIM card change (since it has implications on the receiving of
     *            pushes).
     */
    public void openBISPushChannel( int port, String appId, String serverUrl, String entryPage, int maxQueueCap,
            ScriptableFunction onData, ScriptableFunction onRegister, ScriptableFunction onSimChange ) throws Exception {

        // check to see if a push is opened on this port using old push API
        PushListener listener = (PushListener) _pushListeners.get( port );
        if( listener != null ) {
            throw new IllegalArgumentException( "A push is opened on this port, please close it and try again." );
        }

        // check to see if a push is open on this port using new push API
        int currentType = PushPersistentStore.getLastKnownType();
        int currentPort = PushPersistentStore.getLastKnownPort();
            
        // cannot run BES and BIS at the same time
        if( currentType != -1 && currentType == BES_PUSH ) {
            throw new IllegalArgumentException( "A BES push is opened, please close it first." );
        }
        // only one port can be opened at a time
        if( currentPort != -1 && port != currentPort ) {
            throw new IllegalArgumentException( "A different port (" + currentPort
                    + ") is already opened, only one port can be opened at a time." );
        }

        EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "open BIS push listener".getBytes() );
        ApplicationDescriptor ad = new ApplicationDescriptor( ApplicationDescriptor.currentApplicationDescriptor(), new String[] {
                "PushDaemon", entryPage, "" + maxQueueCap } );
        PushApplicationDescriptor pad = new PushApplicationDescriptor( appId, port, serverUrl,
                PushApplicationDescriptor.SERVER_TYPE_BPAS, ad );
        int status = PushApplicationRegistry.getStatus( pad ).getStatus();
        if( status != PushApplicationStatus.STATUS_ACTIVE ) {
            EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "Register push application".getBytes() );
            PushApplicationRegistry.registerApplication( pad );
        }

        // new listener
        if( _pushListener2 == null ) {
            _pushListener2 = new PushListener2( port, _daemonStore.getMessageQueue(), onData, onRegister, onSimChange );
            setOnExitListener( true );
            _pushListener2.start();
        } else {
            _pushListener2.updateCallback( onData, onRegister, onSimChange );
        }
    }

    /**
     * Creates a new push listener to listen on a specified port.
     * 
     * @param port
     *            The port on the device to listen for pushes on.
     * @param entryPage
     *            The page that will be shown when application closes and a push data arrives.
     * @param maxQueueCap
     *            The maximum number of pushes to queue before the oldest pushes are discarded.
     * @param onData
     *            The ScriptableFunction that is invoked when a new push has been received.
     * @param onSimChange
     *            The ScriptableFunction that is invoked on a SIM card change (since it has implications on the receiving of
     *            pushes).
     */
    public void openBESPushChannel( int port, String entryPage, int maxQueueCap, ScriptableFunction onData,
            ScriptableFunction onSimChange ) throws Exception {

        // check to see if a push is opened on this port using old push API
        PushListener listener = (PushListener) _pushListeners.get( port );
        if( listener != null ) {
            throw new IllegalArgumentException( "A push is opened on this port, please close it and try again." );
        }

        // check to see if a push is open on this port using new push API
        int currentType = PushPersistentStore.getLastKnownType();
        int currentPort = PushPersistentStore.getLastKnownPort();
            
        // cannot run BES and BIS at the same time
        if( currentType != -1 && currentType == BIS_PUSH ) {
            throw new IllegalArgumentException( "A BIS push is opened, please close it and try again." );
        }
        // only one port can be opened at a time
        if( currentPort != -1 && port != currentPort ) {
            throw new IllegalArgumentException( "A different port (" + currentPort
                    + ") is already opened, only one port can be opened at a time." );
        }

        ApplicationDescriptor ad = new ApplicationDescriptor( ApplicationDescriptor.currentApplicationDescriptor(), new String[] {
                "PushDaemon", entryPage, "" + maxQueueCap } );
        PushApplicationDescriptor pad = new PushApplicationDescriptor( port, ad );
        int status = PushApplicationRegistry.getStatus( pad ).getStatus();
        if( status != PushApplicationStatus.STATUS_ACTIVE ) {
            PushApplicationRegistry.registerApplication( pad );
        }

        // new listener
        if( _pushListener2 == null ) {
            _pushListener2 = new PushListener2( port, _daemonStore.getMessageQueue(), onData, onSimChange );
            setOnExitListener( true );
            _pushListener2.start();
        } else {
            _pushListener2.updateCallback( onData, null, onSimChange );
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
        } else {
            if( PushPersistentStore.getLastKnownPort() == port ) {
                stopDaemon();
                
                if( _pushListener2 != null ) {
                    _pushListener2.stop();
                    _pushListener2 = null;
                } 

                EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "Unregister push application".getBytes() );
                PushApplicationRegistry.unregisterApplication();
                PushPersistentStore.setLastKnownType( -1 );
                PushPersistentStore.setLastKnownPort( -1 );
            }
        }
        if( ( _pushListeners.size() == 0 ) && ( _pushListener2 == null ) ) {
            setOnExitListener( false );
        }
    }

    /**
     * Closes all push channels.
     */
    public void closePushChannel() {
        for( Enumeration e = _pushListeners.elements(); e.hasMoreElements(); ) {
            PushListener listener = (PushListener) e.nextElement();
            if( listener != null ) {
                listener.stop();
            }
        }
        _pushListeners.clear();

        if( _pushListener2 != null ) {
            stopDaemon();
            _pushListener2.stop();
            _pushListener2 = null;
            EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "Unregister push application".getBytes() );
            PushApplicationRegistry.unregisterApplication();
            setOnExitListener( false );
        } else if( PushPersistentStore.getLastKnownPort() != -1 ) {
            stopDaemon();
            EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "Unregister push application".getBytes() );
            PushApplicationRegistry.unregisterApplication();
        }

        PushPersistentStore.setLastKnownType( -1 );
        PushPersistentStore.setLastKnownPort( -1 );

    }

    private void stopDaemon() {
        if( _daemonStore != null ) {
            Vector commandQueue = _daemonStore.getCommandQueue();
            synchronized( commandQueue ) {
                commandQueue.addElement( new Object() );
                commandQueue.notify();
            }
            _daemonStore.getMessageQueue().removeAllElements();
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
    public static boolean isValidPort( int port ) {
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
         * @see ApplicationEventHandler#handleEvent(int, Object[])
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
            if( _pushListener2 != null ) {
                _pushListener2.stop();
                _pushListener2 = null;
            }
            setOnExitListener( false );
        }

        /**
         * @see ApplicationEventHandler#handlePreEvent(int, Object[])
         */
        public boolean handlePreEvent( int eventID, Object[] args ) {
            // do nothing
            return false;
        }
    }

    public void resetListeners() {
        if( _pushListener2 != null ) {
            _pushListener2.updateCallback( null, null, null );
        }
    }
}
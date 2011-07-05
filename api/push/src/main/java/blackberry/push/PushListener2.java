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
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.io.http.PushInputStream;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import blackberry.common.push.PushDaemon.SimChangeData;
import blackberry.common.push.PushDaemon.StatusChangeData;
import blackberry.common.push.PushData;
import blackberry.common.settings.SettingsManager;
import blackberry.common.util.ID;
import blackberry.push.data.PushDataObject;

/**
 * 
 * The push listener is used to handle pushes opened through openBESPushListener or openBISPushLitener calls.
 * 
 */
public class PushListener2 {
    private static final long LASTKNOWN_ID;
    
    private boolean _stop;
    private ScriptableFunction _pushCallback;
    private ScriptableFunction _registerCallback;
    private ScriptableFunction _simChangeCallback;
    private Object _callBackLock;
    private Vector _messageQueue;

    static {
        LASTKNOWN_ID = ID.getUniqueID( "LASTKNOWN_ID" );
    } 
   
    /**
     * Constructor
     * 
     * @param port
     *            The port to listen on
     * @param queue
     *            The queue to store the push message
     * 
     * @param pushCallback
     *            The ScriptableFunction that is invoked when a new push has been received.
     */
    private PushListener2( int port, Vector queue, ScriptableFunction pushCallback ) {
        _messageQueue = queue;
        _pushCallback = pushCallback;
        _callBackLock = new Object();
        _stop = false;
        setLastKnownPort(port); // Keep track of some info between device restarts
    }

    /**
     * Constructor
     * 
     * @param port
     *            The port to listen on
     * @param queue
     *            The message queue
     * @param pushCallback
     *            The ScriptableFunction that is invoked when a new push has been received.
     * @param simChangeCallback
     *            The ScriptableFunction that is invoked on a SIM card change (since it has implications on the receiving of
     *            pushes).
     */
    public PushListener2( int port, Vector queue, ScriptableFunction pushCallback, ScriptableFunction simChangeCallback ) {
        this( port, queue, pushCallback );
        _simChangeCallback = simChangeCallback;
        _callBackLock = new Object();
        setLastKnownType(PushService.BES_PUSH); // Keep track of some info between device restarts
    }

    /**
     * Constructor
     * 
     * @param port
     *            The port to listen on
     * @param pushCallback
     *            The ScriptableFunction that is invoked when a new push has been received.
     * @param registerCallback
     *            The ScriptableFunction that is invoked when the result of the registration performed during the opening of the
     *            push listener is received.
     * @param simChangeCallback
     *            The ScriptableFunction that is invoked on a SIM card change (since it has implications on the receiving of
     *            pushes).
     */
    public PushListener2( int port, Vector queue, ScriptableFunction pushCallback, ScriptableFunction registerCallback,
            ScriptableFunction simChangeCallback ) {
        this( port, queue, pushCallback, simChangeCallback );
        _registerCallback = registerCallback;
        setLastKnownType(PushService.BIS_PUSH); // Keep track of some info between device restarts
    }

    /**
     * Updates this listener's callbacks to a new ones. Also, if the listener was previously suspended - it will resume.
     * 
     * @param pushCallback
     *            the new push callback
     * @param registerCallback
     *            The new register callback
     * @param simChangeCallback
     *            The new SIM change callback
     */
    public void updateCallback( ScriptableFunction pushCallback, ScriptableFunction registerCallback,
            ScriptableFunction simChangeCallback ) {
        synchronized( _callBackLock ) {
            _pushCallback = pushCallback;
            _registerCallback = registerCallback;
            _simChangeCallback = simChangeCallback;
        }
        _stop = false;
        synchronized( _messageQueue ) {
            _messageQueue.notify();
        }
    }

    /**
     * Stops this listener - all connections are closed, all threads are finished. The push listener cannot be restarted once it
     * is stopped - a new instance, listening on the same port, must be created.
     */
    public synchronized void stop() {
        _stop = true;

        // wake the message processor
        synchronized( _messageQueue ) {
            _messageQueue.notify();
        }
    }

    /**
     * Starts the message processing thread.
     */
    public void start() {
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.start();
    }

    
    /**
     * Get the last known push service type
     * 
     * @return push service type 
     */
    public static int getLastKnownType() {
        PersistentObject persistentObject = PersistentStore.getPersistentObject(LASTKNOWN_ID);
        Hashtable info = (Hashtable)persistentObject.getContents();
        if(info != null && info.containsKey("type")) {
            return ((Integer)info.get("type")).intValue();
        }
        
        return -1;
    }


    /**
     * Set the last known push service type
     * 
     * @param type
     *            push service type
     */
    public static void setLastKnownType(int type) {
        PersistentObject persistentObject = PersistentStore.getPersistentObject(LASTKNOWN_ID);
        Hashtable info = (Hashtable)persistentObject.getContents();
        if(info == null) {
            info = SettingsManager.createStorableObject();
            CodeSigningKey codeSigningKey = CodeSigningKey.get( info );
            persistentObject.setContents( new ControlledAccess( info, codeSigningKey ) );
        }
        info.put("type", new Integer(type));
        persistentObject.commit();
    }
    
    /**
     * Get the last known push service port
     * 
     * @return push service port 
     */
    public static int getLastKnownPort() {
        PersistentObject persistentObject = PersistentStore.getPersistentObject(LASTKNOWN_ID);
        Hashtable info = (Hashtable)persistentObject.getContents();
        if(info != null && info.containsKey("port")) {
            return ((Integer)info.get("port")).intValue();
        }
        
        return -1;
    }
    
    /**
     * Set the last known push service port
     * 
     * @param port
     *            push service port
     */
    public static void setLastKnownPort(int port) {
        PersistentObject persistentObject = PersistentStore.getPersistentObject(LASTKNOWN_ID);
        Hashtable info = (Hashtable)persistentObject.getContents();
        if(info == null) {
            info = SettingsManager.createStorableObject();
            CodeSigningKey codeSigningKey = CodeSigningKey.get( info );
            persistentObject.setContents( new ControlledAccess( info, codeSigningKey ) );
        }
        info.put("port", new Integer(port));
        persistentObject.commit();
    }

    /**
     * Private class that creates a thread to process push messages already received by listener thread.
     */
    private final class MessageProcessor extends Thread {
        public void run() {

            while( !_stop ) {

                // check for messages
                synchronized( _messageQueue ) {
                    if( _pushCallback == null || _messageQueue.size() == 0 ) {
                        try {
                            _messageQueue.wait();
                        } catch( Exception e ) {
                            // ignore - check loop
                        }
                    }
                    if( _pushCallback != null && _messageQueue.size() > 0 ) {
                        // process message - one at a time
                        Object message = _messageQueue.elementAt( 0 );
                        processMessage( message );
                        _messageQueue.removeElementAt( 0 );
                    }
                }
            }

            // clean-up any messages that were not processed
            synchronized( _messageQueue ) {
                Enumeration messagesList = _messageQueue.elements();
                PushData message;
                while( messagesList.hasMoreElements() ) {
                    message = (PushData) messagesList.nextElement();
                    message.discard();
                }
                _messageQueue.removeAllElements();
            }
        }

        private void processMessage( Object message ) {
            
            if( message instanceof PushData ) {
                processPushData( (PushData) message );
            } else if( message instanceof SimChangeData ) {
                processSimChange();
            } else if( message instanceof StatusChangeData ) {
                processStatusChange( ( (StatusChangeData) message ).getStatus() );
            }
        }

        /**
         * Processes a created blackberry.push.PushData message
         * 
         * @param message
         *            PushData message.
         */
        private void processPushData( PushData message ) {
            // Create PushDataObject Scriptable Object
            PushDataObject pushDataScriptable = new PushDataObject( message );

            try {
                // Invoke callback method
                Object result = null;
                synchronized( _callBackLock ) {
                    result = _pushCallback.invoke( null, new Object[] { pushDataScriptable } );
                }

                // Accept/Decline push message based on return from callback
                if( result instanceof Integer ) {
                    int callbackReply = ( (Integer) result ).intValue();
                    switch( callbackReply ) {
                        case PushDataObject.ACCEPT:
                            message.accept();
                            break;
                        case PushDataObject.DECLINE_USERDCR:
                            message.decline( PushInputStream.DECLINE_REASON_USERDCR );
                            break;
                        case PushDataObject.DECLINE_USERPND:
                            message.decline( PushInputStream.DECLINE_REASON_USERPND );
                            break;
                        case PushDataObject.DECLINE_USERREQ:
                            message.decline( PushInputStream.DECLINE_REASON_USERREQ );
                            break;
                        case PushDataObject.DECLINE_USERRFS:
                            message.decline( PushInputStream.DECLINE_REASON_USERRFS );
                            break;
                        case PushDataObject.DECLINE_USERDCU:
                        default:
                            // should not get here
                            message.decline( PushInputStream.DECLINE_REASON_USERDCU );
                            break;
                    }
                } else {
                    // By default, we'll decline the message
                    message.decline( PushInputStream.DECLINE_REASON_USERDCU );
                }
                message.discard();
            } catch( Exception e ) {
                // do thing
            }
        }

        private void processStatusChange( int result ) {
            if( _registerCallback != null ) {
                Object[] args = new Object[ 1 ];
                args[ 0 ] = new Integer( result );
                try {
                    _registerCallback.invoke( null, args );
                } catch( Exception e ) {
                    // do nothing
                }
            }
        }

        private void processSimChange() {
            try {
                _simChangeCallback.invoke( null, null );
            } catch( Exception e ) {
                // do nothing
            }
        }
    }
}

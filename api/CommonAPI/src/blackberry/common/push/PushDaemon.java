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
package blackberry.common.push;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.StreamConnection;

import net.rim.blackberry.api.push.PushApplication;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.io.http.PushInputStream;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.DataBuffer;
import blackberry.common.util.ID;
import blackberry.core.WidgetProperties;

/**
 * The push daemon is responsible for receiving push messages, store them in a message queue and notify the UI application. It
 * launches the UI application if it is not running.
 * 
 * 
 */
public class PushDaemon extends Application implements PushApplication {
    // Registration status
    public static final int SUCCESS = 0;
    public static final int NETWORK_ERROR = 1;
    public static final int REJECTED_BY_SERVER = 2;
    public static final int INVALID_PARAMETERS = 3;
    public static final int GENERAL_ERROR = -1;
    private static final int CHUNK_SIZE = 256;

    private int _maxQueueCap;
    private String _entryPage;
    private DaemonStore _daemonStore;
    private CommandListener _commandListener;

    /**
     * Data object used between the daemon and widget to communicate
     */
    final public static class DaemonStore {
        private static final long DAEMON_STORE_ID;
        
        private ApplicationDescriptor _uiAppDesc = null;
        private Vector _commandQueue;
        private Vector _messageQueue;

        static {
            DAEMON_STORE_ID = ID.getUniqueID( "DAEMON_STORE_ID" );
        }

        public static DaemonStore loadFromStore() {
            DaemonStore store = null;

            Object storeObject = RuntimeStore.getRuntimeStore().get( DAEMON_STORE_ID );
            if( storeObject != null ) {
                store = (DaemonStore) storeObject;
            } else {
                store = new DaemonStore();
                CodeSigningKey codeSigningKey = CodeSigningKey.get( store );
                RuntimeStore.getRuntimeStore().put( DAEMON_STORE_ID, new ControlledAccess( store, codeSigningKey ) );
            }

            return store;
        }

        private DaemonStore() {
            _commandQueue = new Vector();
            _messageQueue = new Vector();
        }

        /**
         * Get vector holding command messages to the daemon
         * 
         * @return command queue vector
         */
        public Vector getCommandQueue() {
            return _commandQueue;
        }

        /**
         * Get vector holding push messages
         * 
         * @return message queue object
         */
        public Vector getMessageQueue() {
            return _messageQueue;
        }

        /**
         * Get the app descriptor for the application expecting push notifications
         * 
         * @return app descriptor
         */
        public ApplicationDescriptor getUIAppDesc() {
            return _uiAppDesc;
        }

        /**
         * Set the app descriptor for the application expecting push notifications
         * 
         * @param uiAppDesc
         *            app descriptor
         */
        public void setUIAppDesc( ApplicationDescriptor uiAppDesc ) {
            _uiAppDesc = uiAppDesc;
        }

    }

    /**
     * Constructor.
     * 
     * @param entryPage
     *            The page of the UI application to be shown if it is not running
     * @param maxQueueCap
     *            The maximum number of message to store in the message queue
     */
    public PushDaemon( String entryPage, int maxQueueCap ) {
        _entryPage = entryPage;
        _maxQueueCap = maxQueueCap;

        _daemonStore = (DaemonStore) DaemonStore.loadFromStore();
        _daemonStore.getCommandQueue().removeAllElements(); // clear all commands

        _commandListener = new CommandListener();
        _commandListener.start();
        
        EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "PushDaemon is started.".getBytes() );
    }

    /**
     * onMessage handle inbound push notifications
     * 
     * @param stream
     *            inbound PushInputStream
     * 
     * @param conn
     *            inbound StreamConnection
     * 
     * @see PushApplication#.onMessage(PushInputStream, StreamConnection)
     */
    public void onMessage( PushInputStream inputStream, StreamConnection conn ) {
        handleMessage( inputStream, conn );
    }

    /**
     * onStatusChange Called when Push Status changes
     * 
     * @param status
     *            changed push state.
     * 
     * @see PushApplication#onStatusChange(PushApplicationStatus)
     */
    public void onStatusChange( PushApplicationStatus status ) {
        handleStatusChange( status );
    }

    private void launchUI() {
        // launch UI if it is not running
        ApplicationDescriptor uiAppDesc = _daemonStore.getUIAppDesc();
        if( uiAppDesc != null && !isAppRunning( uiAppDesc ) ) {
            ApplicationDescriptor newAppDesc = new ApplicationDescriptor( uiAppDesc, new String[] { _entryPage } );
            try {
                ApplicationManager.getApplicationManager().runApplication( (ApplicationDescriptor) newAppDesc, false );
            } catch( ApplicationManagerException e ) {
                throw new RuntimeException( "Failed to launch UI application, details: " + e.getMessage() );
            }
        }
    }

    private static boolean isAppRunning( ApplicationDescriptor desc ) {
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        return mgr.getProcessId( desc ) != -1;
    }

    private void handleMessage( PushInputStream inputStream, StreamConnection conn ) {

        EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), ( "handleMessage is called." ).getBytes() );
        
        DataBuffer db = null;
        try {
            /**
             * Does this need to be synch'd? This should be a single threaded instance that runs autonomously on a port.
             * 
             * Might want to call this in the the PushService class to catch IOPortAlreadyBoundException so we can properly throw
             * this back up via JavaScript so the developer realizes that the port they tried to open is unavailable.
             */

            Vector messageQueue = _daemonStore.getMessageQueue();

            // extract the data from the input stream
            db = new DataBuffer();
            byte[] data = new byte[ CHUNK_SIZE ];
            int chunk = 0;
            while( -1 != ( chunk = inputStream.read( data ) ) ) {
                db.write( data, 0, chunk );
            }

            // trim the array - the buffer in DataBuffer grows
            // past the size and fills with empty chars
            db.trim();

            // synchronize this block to avoid race conditions with the queue
            synchronized( messageQueue ) {
                // Create push object, add to queue for callback thread
                PushData pd = new PushData( conn, inputStream, db.getArray() );
                if( _maxQueueCap > 0 ) {
                    if( messageQueue.size() >= _maxQueueCap ) {
                        messageQueue.removeElementAt( 0 );
                    }
                }

                messageQueue.addElement( pd );
                messageQueue.notify();
            }
        } catch( IOException e1 ) {
            // a problem occurred with the input stream
            // however, the original StreamConnectionNotifier is still valid
            if( inputStream != null ) {
                try {
                    inputStream.close();
                } catch( IOException e2 ) {
                }
            }
            if( conn != null ) {
                try {
                    conn.close();
                } catch( IOException e2 ) {
                }
            }
        }

        launchUI();
    }

    private void handleStatusChange( PushApplicationStatus applicationStatus ) {
        int result = GENERAL_ERROR;
        boolean simChange = false;
        int status = applicationStatus.getStatus();

        EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), ( "handleStatusChange is called, status:" + status ).getBytes() );

        if( status == PushApplicationStatus.STATUS_ACTIVE ) {
            result = SUCCESS;
        } else if( status == PushApplicationStatus.STATUS_FAILED ) {
            if( applicationStatus.getReason() == PushApplicationStatus.REASON_NETWORK_ERROR ) {
                result = NETWORK_ERROR;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_REJECTED_BY_SERVER ) {
                result = REJECTED_BY_SERVER;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_INVALID_PARAMETERS ) {
                result = INVALID_PARAMETERS;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_SIM_CHANGE ) {
                simChange = true;
            }
        } else if( status == PushApplicationStatus.STATUS_NOT_REGISTERED ) {
            if( applicationStatus.getReason() == PushApplicationStatus.REASON_NETWORK_ERROR ) {
                result = NETWORK_ERROR;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_REJECTED_BY_SERVER ) {
                result = REJECTED_BY_SERVER;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_INVALID_PARAMETERS ) {
                result = INVALID_PARAMETERS;
            } else if( applicationStatus.getReason() == PushApplicationStatus.REASON_SIM_CHANGE ) {
                simChange = true;
            }
        }

        Vector messageQueue = _daemonStore.getMessageQueue();

        synchronized( messageQueue ) {
            Object data;
            if( simChange ) {
                data = new SimChangeData();
            } else {
                data = new StatusChangeData( result );
            }
            messageQueue.addElement( data );
            messageQueue.notify();
        }

        launchUI();
    }

    public static class StatusChangeData {
        private int _status;

        public StatusChangeData( int status ) {
            _status = status;
        }

        public int getStatus() {
            return _status;
        }
    }

    public static class SimChangeData {
    }

    public class CommandListener extends Thread {

        private boolean _stop;

        public CommandListener() {
            _stop = false;
        }

        /**
         * @see java.lang.Thread
         */
        public void run() {
            while( !_stop ) {

                Vector commandQueue = _daemonStore.getCommandQueue();

                synchronized( commandQueue ) {
                    if( commandQueue.size() == 0 ) {
                        try {
                            commandQueue.wait();
                        } catch( Exception e ) {
                            // ignore - check loop
                        }
                    }
                    if( commandQueue.size() > 0 ) {
                        _stop = true;
                    }
                }
            }
            EventLogger.logEvent( WidgetProperties.getInstance().getGuid(), "PushDaemon exits".getBytes() );
            System.exit( 0 );
        }
    }
}
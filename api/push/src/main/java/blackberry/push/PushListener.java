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

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import net.rim.device.api.io.http.PushInputStream;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.util.DataBuffer;
import blackberry.common.push.PushData;
import blackberry.push.data.PushDataObject;

/**
 * 
 * Separate push listening thread to handle all traffic for a particular port.
 * 
 */
public class PushListener extends Thread {

    private static final int CHUNK_SIZE = 256;
    private static final String URL_PREFIX = "http://:";
    private static final String MDS_SUFFIX = ";deviceside=false;ConnectionType=mds-public";

    private boolean _stop;
    private StreamConnectionNotifier _notify;
    private int _port;
    private ScriptableFunction _callback;
    private Object _callBackLock;
    private Vector _messageQueue;
    private boolean _isSuspended;
    private int _maxQueueCap;
    private Thread _listenThread;

    /**
     * Constructor
     * 
     * @param port
     *            the port that this object will listen on.
     * @param callback
     *            the scriptable callback function to invoke when a new push message is received.
     * @param maxQueue
     *            the max amount of pushed messages to queue
     */
    public PushListener( int port, ScriptableFunction callback, int maxQueue ) {
        _port = port;
        _callback = callback;
        _callBackLock = new Object();
        _maxQueueCap = maxQueue;
        if( _maxQueueCap > 0 ) {
            _messageQueue = new Vector( _maxQueueCap );
        } else {
            _messageQueue = new Vector();
        }
    }

    /**
     * 
     * Suspend the listener - preventing it from invoking callback functions.
     * 
     * Messages pushed to the port are still received and queued
     * 
     */
    public void suspend() {
        setSuspended( true );
    }

    /**
     * Updates this listener's callback to a new one. Also, if the listener was previously suspended - it will resume.
     * 
     * @param callback
     *            the new callback function to invoke.
     */
    public void updateCallback( ScriptableFunction callback ) {
        synchronized( _callBackLock ) {
            _callback = callback;
        }
        setSuspended( false );
    }

    /**
     * Stops this listener - all connections are closd, all threads are finished. The push listener cannot be restarted once it is
     * stopped - a new instance, listening on the same port, must be created.
     */
    public synchronized void stop() {
        _stop = true;

        // wake the message processor
        synchronized( _messageQueue ) {
            _messageQueue.notify();
        }

        try {
            if( _notify != null ) {
                _notify.close();
            }
        } catch( IOException e ) {
        }
    }

    /**
     * @see java.lang.Thread
     */
    public void run() {
        _listenThread = Thread.currentThread();

        // Create message processing thread, and run it
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.start();

        while( !_stop ) {
            StreamConnection stream = null;
            InputStream input = null;
            DataBuffer db = null;
            try {
                /**
                 * Does this need to be synch'd? This should be a single threaded instance that runs autonomously on a port.
                 * 
                 * Might want to call this in the the PushService class to catch IOPortAlreadyBoundException so we can properly
                 * throw this back up via JavaScript so the developer realizes that the port they tried to open is unavailable.
                 */
                synchronized( this ) {
                    _notify = (StreamConnectionNotifier) Connector.open( getURL() );
                }

                while( !_stop ) {
                    // block for data
                    stream = _notify.acceptAndOpen();
                    try {
                        // create input stream
                        input = stream.openInputStream();

                        // extract the data from the input stream
                        db = new DataBuffer();
                        byte[] data = new byte[ CHUNK_SIZE ];
                        int chunk = 0;
                        while( -1 != ( chunk = input.read( data ) ) ) {
                            db.write( data, 0, chunk );
                        }

                        // trim the array - the buffer in DataBuffer grows
                        // past the size and fills with empty chars
                        db.trim();

                        // synchronize this block to avoid race conditions with the queue
                        synchronized( _messageQueue ) {
                            // Create push object, add to queue for callback thread
                            PushData pd = new PushData( stream, input, db.getArray() );
                            _messageQueue.addElement( pd );
                            _messageQueue.notify();
                        }
                    } catch( IOException e1 ) {
                        // a problem occurred with the input stream
                        // however, the original StreamConnectionNotifier is still valid
                        if( input != null ) {
                            try {
                                input.close();
                            } catch( IOException e2 ) {
                            }
                        }
                        if( stream != null ) {
                            try {
                                stream.close();
                            } catch( IOException e2 ) {
                            }
                        }
                    }
                }

            } catch( ConnectionNotFoundException cnfe ) {
                // stop this listener, it's not going to run
                stop();
            } catch( IOException ioe ) {
                // likely the stream was closed
            } finally {
                if( _notify != null ) {
                    try {
                        _notify.close();
                        _notify = null;
                    } catch( IOException e ) {
                    }
                }
            }
        }
    }

    /*
     * When set to true, the listener is suspended - preventing it from invoking callback functions. Messages pushed to the port
     * are still received and queued. When set to false, the listener resumes normal functions - receiving push messages and
     * invoking callbacks.
     * 
     * isSuspended - flag indicating whether listener is suspended.
     */
    private void setSuspended( boolean isSuspended ) {
        synchronized( _messageQueue ) {
            _isSuspended = isSuspended;
            _messageQueue.notifyAll();
        }
    }

    /**
     * Private class that creates a thread to process push messages already received by listener thread.
     */
    private final class MessageProcessor extends Thread {
        public void run() {
            while( !_stop ) {
                // check for messages
                synchronized( _messageQueue ) {
                    if( _messageQueue.size() == 0 || _isSuspended ) {
                        try {
                            _messageQueue.wait();
                        } catch( Exception e ) {
                            // ignore - check loop
                        }
                        continue;
                    }
                }
                // process message - one at a time
                PushData message = (PushData) _messageQueue.elementAt( 0 );
                processMessage( message );

                // remove message from queue
                synchronized( _messageQueue ) {
                    _messageQueue.removeElementAt( 0 );
                }
            }

            // clean-up any messages that were not processed
            // sync up with listen thread to make sure all messages
            // are in the queue so everything is cleaned up
            try {
                _listenThread.join();
            } catch( Exception e ) {
                // ignore, proceed with clean-up
            }
            Enumeration messagesList = _messageQueue.elements();
            PushData message;
            while( messagesList.hasMoreElements() ) {
                message = (PushData) messagesList.nextElement();
                message.discard();
            }
        }

        /**
         * Processes a created net.rim.device.api.web.jse.toolkit.push.PushData message
         * 
         * @param message
         *            PushData message.
         */
        void processMessage( PushData message ) {
            // Create PushDataObject Scriptable Object
            PushDataObject pushDataScriptable = new PushDataObject( message );

            try {
                // Invoke callback method
                Object result = null;
                synchronized( _callBackLock ) {
                    result = _callback.invoke( null, new Object[] { pushDataScriptable } );
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
            } catch( Exception e ) {
                try {
                    // Decline message
                    message.decline( PushInputStream.DECLINE_REASON_USERDCU );
                } catch( Exception ex ) {
                }
            }
            // discard message?
        }
    }

    private String getURL() {
        String result = URL_PREFIX + _port + MDS_SUFFIX;
        return result;
    }
}

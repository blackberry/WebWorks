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
package blackberry.message.sms;

import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.io.DatagramConnectionBase;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.message.util.MessageUtil;

/**
 * Sends one SMS message or multiple messages
 * 
 * @author oel
 * 
 */
public class FunctionSendSMS extends ScriptableFunctionBase {

    public static final String NAME = "send";

    private static Vector _senders;
    private Connection _conn;

    /**
     * Constructor.
     */
    public FunctionSendSMS() {
        if( _senders == null ) {
            _senders = new Vector();
            new Thread() {
                public void run() {
                    while( true ) {
                        synchronized( _senders ) {
                            if( _senders.isEmpty() ) {
                                try {
                                    _senders.wait();
                                } catch( InterruptedException e ) {
                                    return;
                                }
                            }

                            if( !_senders.isEmpty() ) {
                                ( (Sender) _senders.elementAt( 0 ) ).start();
                                _senders.removeElementAt( 0 );
                            }
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures();
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature sendSMSSig = new FunctionSignature( 2 );
        sendSMSSig.addParam( String.class, true );
        sendSMSSig.addParam( String.class, true );
        return new FunctionSignature[] { sendSMSSig };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        String msgContent = args[ 0 ].toString();
        String msgAddress = args[ 1 ].toString();

        SMSMessage msg = new SMSMessage( msgContent, msgAddress );

        synchronized( _senders ) {
            _senders.addElement( new Sender( msg ) );
            _senders.notifyAll();
        }
        return UNDEFINED;
    }

    private class Sender extends Thread {
        SMSMessage message;

        public Sender( SMSMessage msg ) {
            message = msg;
        }

        public void run() {
            synchronized( _senders ) {
                try {
                    _conn = Connector.open( SMSMessage.PROTOCOL + message.getAddress() );

                    if( MessageUtil.isCDMA() ) {
                        DatagramConnectionBase dcb = (DatagramConnectionBase) _conn;
                        dcb.send( message.toDatagram( dcb ) );
                    } else {
                        MessageConnection mc = (MessageConnection) _conn;
                        mc.send( message.toMessage( mc ) );
                    }
                } catch( Exception e ) {
                    throw new RuntimeException( e.getMessage() );
                } finally {
                    try {
                        if( _conn != null ) {
                            _conn.close();
                        }
                    } catch( Exception e ) {
                    }
                }
            }
        }
    }
}
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
package blackberry.message.sms;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import blackberry.message.Receiver;

public class SMSReceiver extends Receiver {

    public SMSReceiver() {
        listenerRegistry = new SMSReceiveListenerRegistry();
    }

    public String getProtocol() {
        return SMSMessage.PROTOCOL;
    }
    
    protected void listenForMessages() throws Exception {
        _connection = openPort();
        DatagramConnection dCon = (DatagramConnection) _connection;
        while (true) {
            Datagram dgram = dCon.newDatagram(dCon.getMaximumLength());
            dCon.receive(dgram);
            if (isRunning() && listenerRegistry.listenerIsSet()) {
                new DatagramProcessor(dgram).start();
            }
            else {
                yield();
            }
        }
    }
    
    private class DatagramProcessor extends Thread{
        Datagram datagram;
        DatagramProcessor (Datagram dg) {
            datagram = dg;
        }
        
        public void run() {
            String body = new String(datagram.getData());
            String address = datagram.getAddress();
            MessageConnection mc = (MessageConnection)_connection;
            Message newMessage = new SMSMessage(body, address).toMessage(mc);
            listenerRegistry.queueIncomingMessage(newMessage);
        }
        
    }
    
    private DatagramConnection openPort() throws Exception {
        //use DatagramConnection because MessageConnection requires opening port 0,
        //which in most cases will be blocked by some 3rd part application
        return (DatagramConnection) Connector.open(getProtocol());
    }

}

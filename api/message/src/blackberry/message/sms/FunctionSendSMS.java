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

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;

import blackberry.message.util.MessageUtil;
import common.util.ArgumentValidationUtil;
import net.rim.device.api.io.DatagramConnectionBase;
import net.rim.device.api.script.ScriptableFunction;

/**
 * Sends one SMS message or multiple messages
 * 
 * @author oel
 * 
 */
public class FunctionSendSMS extends ScriptableFunction {

    public static final String NAME = "send";

    private Connection _conn;

    public Object invoke(Object thiz, Object[] args) {
        ArgumentValidationUtil.validateParameterNumber(new int[] { 2 }, args);
        String msgContent = args[0].toString();
        String msgAddress = args[1].toString();

        SMSMessage msg = new SMSMessage(msgContent, msgAddress);
        Sender sender = new Sender(msg);
        sender.start();

        return UNDEFINED;
    }

    private class Sender extends Thread {
        SMSMessage message;

        public Sender(SMSMessage msg) {
            message = msg;
        }

        public void run() {
            try {
                _conn = Connector.open(SMSMessage.PROTOCOL + message.getAddress());

                if (MessageUtil.isCDMA()) {
                    DatagramConnectionBase dcb = (DatagramConnectionBase) _conn;
                    dcb.send(message.toDatagram(dcb));
                }
                else {
                    MessageConnection mc = (MessageConnection) _conn;
                    mc.send(message.toMessage(mc));
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                try {
                    if (_conn != null) {
                        _conn.close();
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}
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

import java.util.Date;

import javax.wireless.messaging.TextMessage;

import blackberry.message.ReceiveListenerRegistry;
import blackberry.message.util.MessageUtil;

/**
 * SMS message receiver registry implmentation.
 * 
 * @author dmeng
 *
 */
public class SMSReceiveListenerRegistry extends ReceiveListenerRegistry {

    /**
     * @see ReceiveListenerRegistry#isTheRightTypeOfMessage( Object )
     */
    protected boolean isTheRightTypeOfMessage( Object msg ) {
        return msg instanceof TextMessage;
    }

    /**
     * @see ReceiveListenerRegistry#fireAll()
     */
    public void fireAll() {
        while( !messages.isEmpty() ) {
            final TextMessage msg = (TextMessage) messages.elementAt( 0 );
            uiApplication.invokeLater( new Runnable() {
                public void run() {
                    new callbackDispatchThread( msg ).start();
                }
            } );
            messages.removeElementAt( 0 );
        }
    }

    private class callbackDispatchThread extends Thread {
        private TextMessage _msg;

        callbackDispatchThread( TextMessage msg ) {
            _msg = msg;
        }

        public void run() {
            try {
                listener.invoke( null, getParameters() );
            } catch( Exception e1 ) {
                throw new RuntimeException( e1.getMessage() );
            }
        }

        private Object[] getParameters() {
            String address = MessageUtil.getFormattedAddress( SMSMessage.PROTOCOL, _msg.getAddress() );
            Date date = _msg.getTimestamp();
            if( date == null ) {
                date = new Date(); // Date is not set, so use the received date
            }
            return new Object[] { _msg.getPayloadText(), address, date };
        }
    }
}

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

import java.io.IOException;
import java.util.Date;

import javax.microedition.io.Datagram;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.DatagramBase;
import net.rim.device.api.io.DatagramConnectionBase;
import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.SMSPacketHeader;

/**
 * Wraps message body and address into an SMS message object.
 * 
 * @author oel
 * 
 */
public class SMSMessage {

    public static final String PROTOCOL = "sms://";
    // Message body
    private String _body;

    // Address received message from or where message sent to
    private String _phoneNumber;

    private Date _date;

    public SMSMessage(String body, String phoneNumber) {
        this(body, phoneNumber, new Date());
    }

    public SMSMessage(String body, String phoneNumber, Date date) {
        _body = body;
        _phoneNumber = phoneNumber;
        _date = date;
    }

    public SMSMessage() {
    }

    public String getContent() {
        return _body;
    }

    public String getAddress() {
        return _phoneNumber;
    }

    public Date getDate() {
        return _date;
    }
    /**
     * Returns a Message object representing this SMS message
     * 
     * @param mc
     *            The MessageConnection source with which to create the Message from
     * @return The Message object representing the SMS message
     */
    public Message toMessage(MessageConnection mc) {
        String addressString = "//" + _phoneNumber;
        TextMessage m = (TextMessage) mc.newMessage(MessageConnection.TEXT_MESSAGE, addressString);
        m.setPayloadText(_body);
        return m;
    }

    /**
     * Returns a Datagram object representing this SMS message
     * 
     * @param datagramConnectionBase
     *            The DatagramConnectionBase object with which to create the Datagram from
     * @return The Datagram object representing the SMS message
     */
    public Datagram toDatagram(DatagramConnectionBase datagramConnectionBase) throws IOException {
        DatagramBase datagram = null;
        byte[] data = _body.getBytes("ISO-8859-1");
        datagram = (DatagramBase) datagramConnectionBase.newDatagram();
        SmsAddress smsAddress = new SmsAddress("//" + _phoneNumber);
        SMSPacketHeader smsPacketHeader = smsAddress.getHeader();
        smsPacketHeader.setMessageCoding(SMSPacketHeader.MESSAGE_CODING_ISO8859_1);
        datagram.setAddressBase(smsAddress);
        datagram.write(data, 0, data.length);

        return datagram;
    }

}
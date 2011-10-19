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
package blackberry.bbm.platform.io;

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannelListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;

public class ChannelListener extends BBMPlatformChannelListener {

    private final ConnectionListenerImpl _connListener;
    
    public ChannelListener(ConnectionListenerImpl connListener) {
        _connListener = connListener;
    }
    
    public void contactDeclined(BBMPlatformConnection connection, BBMPlatformContact contact) {
        _connListener.contactDeclined(connection, contact);
    }

    public void contactLeft(BBMPlatformConnection connection, BBMPlatformContact contact) {
        _connListener.contactLeft(connection, contact);
    }

    public void contactsInvited(BBMPlatformConnection connection, BBMPlatformContactList contactList) {
        _connListener.contactsInvited(connection, contactList);
    }

    public void contactsJoined(BBMPlatformConnection connection, BBMPlatformContactList contactList, String cookie, int type) {
        _connListener.contactsJoined(connection, contactList, cookie, type);
    }

    public void dataReceived(BBMPlatformConnection connection, BBMPlatformContact contact, BBMPlatformData data) {
        _connListener.dataReceived(connection, contact, data);
    }

    public void joinRequestReceived(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, String cookie) {
        _connListener.joinRequestReceived(connection, request, cookie);
    }
    
    public void joinRequestCanceled(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, int reason) {
        _connListener.joinRequestCanceled(connection, request, reason);
    }
}

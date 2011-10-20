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

import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.util.Util;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSession;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSessionListener;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.device.api.script.ScriptableFunction;

public class SessionListener extends BBMPlatformSessionListener {

    private final ConnectionListenerImpl _connListenerImpl;
    
    public SessionListener(ConnectionListenerImpl connListener) {
        _connListenerImpl = connListener;
    }
    
    ///////////////////////////////////////////
    // BBMPlatformConnectionListener methods //
    ///////////////////////////////////////////
    
    public void contactDeclined(BBMPlatformConnection connection, BBMPlatformContact contact) {
        _connListenerImpl.contactDeclined(connection, contact);
    }

    public void contactLeft(BBMPlatformConnection connection, BBMPlatformContact contact) {
        _connListenerImpl.contactLeft(connection, contact);
    }

    public void contactsInvited(BBMPlatformConnection connection, BBMPlatformContactList contactList) {
        _connListenerImpl.contactsInvited(connection, contactList);
    }

    public void contactsJoined(BBMPlatformConnection connection, BBMPlatformContactList contactList, String cookie, int type) {
        _connListenerImpl.contactsJoined(connection, contactList, cookie, type);
    }

    public void dataReceived(BBMPlatformConnection connection, BBMPlatformContact contact, BBMPlatformData data) {
        _connListenerImpl.dataReceived(connection, contact, data);
    }
    
    public void joinRequestReceived(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, String cookie) {
        _connListenerImpl.joinRequestReceived(connection, request, cookie);
    }
    
    public void joinRequestCanceled(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, int reason) {
        _connListenerImpl.joinRequestCanceled(connection, request, reason);
    }

    ////////////////////////////////////////
    // BBMPlatformSessionListener methods //
    ////////////////////////////////////////
    
    public void broadcastDataReceived(BBMPlatformSession session, BBMPlatformContact sender, BBMPlatformData data) {
        _connListenerImpl.waitForConnDelivery();
        
        final SessionObject sessionObj = (SessionObject)_connListenerImpl.getConnectionObject();
        try {
            final ScriptableFunction callback = (ScriptableFunction) sessionObj.getField(SessionObject.EVENT_ON_BROADCAST_DATA);
            final Object[] args = new Object[] {
                new BBMPlatformUser(sender),
                data.getDataAsString(),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void contactsRemoved(BBMPlatformSession session, BBMPlatformContact removedBy, BBMPlatformContactList contactList) {
        _connListenerImpl.waitForConnDelivery();
        
        final SessionObject sessionObj = (SessionObject)_connListenerImpl.getConnectionObject();
        try {
            final ScriptableFunction callback = (ScriptableFunction) sessionObj.getField(SessionObject.EVENT_ON_USERS_REMOVED);
            final Object[] args = new Object[] {
                new BBMPlatformUser(removedBy),
                Util.contactListToArray(contactList),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }
    
}

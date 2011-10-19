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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnectionListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.util.ConstantsUtil;
import blackberry.bbm.platform.util.Util;

public class ConnectionListenerImpl implements BBMPlatformConnectionListener {
    
    private ConnectionObject _connObj;
    
    public void setConnectionObject(ConnectionObject connObj) {
        _connObj = connObj;
    }
    
    public ConnectionObject getConnectionObject() {
        return _connObj;
    }
    
    public void waitForConnDelivery() {
        synchronized(_connObj) {
            if(! _connObj.isDelivered()) {
                try {
                    _connObj.wait();
                } catch(Throwable t) {
                    // autolog
                }
            }
        }
    }

    ///////////////////////////////////
    // BBMPlatformConnectionListener //
    ///////////////////////////////////
    
    public void contactDeclined(BBMPlatformConnection connection, BBMPlatformContact contact) {
        this.waitForConnDelivery();
        
        try {
            final ScriptableFunction callback = (ScriptableFunction) _connObj.getField(ConnectionObject.EVENT_ON_USER_DECLINED);
            final Object[] args = new Object[] {
                new BBMPlatformUser(contact),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void contactLeft(BBMPlatformConnection connection, BBMPlatformContact contact) {
        this.waitForConnDelivery();
        
        try {
            final ScriptableFunction callback = (ScriptableFunction) _connObj.getField(ConnectionObject.EVENT_ON_USER_LEFT);
            final Object[] args = new Object[] {
                new BBMPlatformUser(contact),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void contactsInvited(BBMPlatformConnection connection, BBMPlatformContactList contactList) {
        this.waitForConnDelivery();
        
        try {
            final ScriptableFunction callback = (ScriptableFunction) _connObj.getField(ConnectionObject.EVENT_ON_USERS_INVITED);
            final Object[] args = new Object[] {
                Util.contactListToArray(contactList),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void contactsJoined(BBMPlatformConnection connection, BBMPlatformContactList contactList, String cookie, int type) {
        this.waitForConnDelivery();
        
        try {
            final ScriptableFunction callback = (ScriptableFunction) _connObj.getField(ConnectionObject.EVENT_ON_USERS_JOINED);
            final Object[] args = new Object[] {
                Util.contactListToArray(contactList),
                ConstantsUtil.getContactJoinedTypeStr(type),
                cookie,
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void dataReceived(BBMPlatformConnection connection, BBMPlatformContact contact, BBMPlatformData data) {
        this.waitForConnDelivery();
        
        try {
            final ScriptableFunction callback = (ScriptableFunction) _connObj.getField(ConnectionObject.EVENT_ON_DATA);
            final Object[] args = new Object[] {
                new BBMPlatformUser(contact),
                data.getDataAsString(),
            };
            Util.dispatchCallback(callback, args);
        } catch(Exception e) {
            // do nothing
        }
    }

    public void joinRequestReceived(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, String cookie) {
        this.waitForConnDelivery();
        final Object[] args = new Object[] {
            new ScriptableIncomingJoinRequest(request),
        };
        Util.dispatchCallback(IONamespace.getInstance()._onJoinReqReceived, args);
    }
    
    public void joinRequestCanceled(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, int reason) {
        this.waitForConnDelivery();
        final Object[] args = new Object[] {
            new ScriptableIncomingJoinRequest(request),
            ConstantsUtil.requestReasonToString(reason),
        };
        Util.dispatchCallback(IONamespace.getInstance()._onJoinReqCanceled, args);
    }
}

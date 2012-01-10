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

import java.util.Date;
import java.util.Hashtable;

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannel;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannelListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformOutgoingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSession;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSessionListener;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.service.MessagingServiceListener;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.users.UsersNamespace;
import blackberry.bbm.platform.util.ConstantsUtil;
import blackberry.bbm.platform.util.Util;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

public class MessagingServiceListenerImpl extends MessagingServiceListener {
    
    private static final String CONN_TYPE_CHANNEL = "channel";
    private static final String CONN_TYPE_SESSION = "session";
    
    private final Hashtable _connsHashConnObjs;
    private final IONamespace _ioNamespace; 
    
    private ScriptableFunction _onFileTransferFailed;
    
    public MessagingServiceListenerImpl(IONamespace ioNamespace) {
        _connsHashConnObjs = new Hashtable();
        _ioNamespace = ioNamespace;
    }
    
    public void setOnFileTransferFailed(ScriptableFunction function) {
        _onFileTransferFailed = function;
    }
    
    private void dispatchOnConnectionAccepted(BBMPlatformConnection conn) {
        final ScriptableFunction onConnAccepted;
        try {
            onConnAccepted = (ScriptableFunction) _ioNamespace.getField(IONamespace.EVENT_ON_CONN_ACCEPTED);
        } catch(Exception e) {
            return;
        }
     // Get connection type
        final String connType;
        if(conn instanceof BBMPlatformChannel) {
            connType = CONN_TYPE_CHANNEL;
        } else if(conn instanceof BBMPlatformSession) {
            connType = CONN_TYPE_SESSION;
        } else {
            return; // Unknown connection type
        }
        
        final ConnectionObject connObj = (ConnectionObject) _connsHashConnObjs.get(conn);
        
        // Dispatch
        final Object[] args = new Object[] {
            connType, connObj    
        };
        Util.dispatchCallback(onConnAccepted, args);
        Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
            protected void dispatch() {
                synchronized(connObj) {
                    connObj.setDelivered();
                    connObj.notify();
                }
            }
        });
    }
    
    //////////////////////////////////////
    // MessagingServiceListener methods //
    //////////////////////////////////////
    
    public void channelCreated(BBMPlatformChannel channel) {
        this.dispatchOnConnectionAccepted(channel);
    }

    public void channelCreated(BBMPlatformChannel channel, int menuItemId) {
        final ScriptableFunction onBBMMenuInvite;
        try {
            onBBMMenuInvite = (ScriptableFunction) _ioNamespace.getField(IONamespace.EVENT_ON_BBM_MENU_INVITE);
        } catch(Exception e) {
            return;
        }
        
        final ChannelObject channelObj = (ChannelObject) _connsHashConnObjs.get(channel);
        
        // Dispatch
        final Object[] args = new Object[] {
            channelObj, new Integer(menuItemId),    
        };
        Util.dispatchCallback(onBBMMenuInvite, args);
        Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
            protected void dispatch() {
                synchronized(channelObj) {
                    channelObj.setDelivered();
                    channelObj.notify();
                }
            }
        });
    }

    public BBMPlatformChannelListener getChannelListener(BBMPlatformChannel channel) {
        final ChannelObject channelObj = new ChannelObject(channel, false);
        _connsHashConnObjs.put(channel, channelObj);
        
        final ConnectionListenerImpl listenerImpl = new ConnectionListenerImpl();
        listenerImpl.setConnectionObject(channelObj);

        return new ChannelListener(listenerImpl);
    }

    public BBMPlatformSessionListener getSessionListener(BBMPlatformSession session) {
        final SessionObject sessionObj = new SessionObject(session, false);
        _connsHashConnObjs.put(session, sessionObj);
        
        final ConnectionListenerImpl listenerImpl =  new ConnectionListenerImpl();
        listenerImpl.setConnectionObject(sessionObj);
        
        return new SessionListener(listenerImpl);
    }

    public void sessionCreated(BBMPlatformSession session) {
        this.dispatchOnConnectionAccepted(session);
    }

    public void sessionEnded(BBMPlatformContact contact, BBMPlatformSession session) {
        final SessionObject sessionObj = (SessionObject)_connsHashConnObjs.get(session);
        try {
            final ScriptableFunction callback = (ScriptableFunction) sessionObj.getField(SessionObject.EVENT_ON_ENDED);
            final Object[] args = { new BBMPlatformUser(contact) };
            Util.dispatchCallback(callback, args);
        } catch( Exception e ) {
            // do nothing
        }
    }

    public void fileTransferFailed(String path, BBMPlatformContact contact, int reason) {
        if(_onFileTransferFailed == null) {
            return;
        }

        final String reasonStr = ConstantsUtil.fileTransferReasonToString(reason);
        Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
            protected void dispatch() {
                try {
                    _onFileTransferFailed.invoke(null, new Object[] { reasonStr });
                } catch (Exception e) {
                    // do nothing
                } finally {
                    _onFileTransferFailed = null;
                }
            }
        });
    }
    
    public void joinRequestAccepted(BBMPlatformOutgoingJoinRequest request, String cookie){
        final ScriptableOutgoingJoinRequest scriptRequest = _ioNamespace.removeOutgoingJoinReq(request);
        if(scriptRequest != null) {
            final Object[] args = {
                scriptRequest,
                cookie
            };
            Util.dispatchCallback(scriptRequest._acceptedCallback, args);
        }
    }
     
    public void joinRequestDeclined(BBMPlatformOutgoingJoinRequest request, int reasonCode) {
        final ScriptableOutgoingJoinRequest scriptRequest = _ioNamespace.removeOutgoingJoinReq(request);
        if(scriptRequest != null) {
            final Object[] args = {
                scriptRequest,
                ConstantsUtil.requestReasonToString(reasonCode),
            };
            Util.dispatchCallback(scriptRequest._declinedCallback, args);
        }
    }

    public void onContactReachable(BBMPlatformContact contact) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) _ioNamespace.getField(IONamespace.EVENT_ON_USER_REACHABLE);
        } catch(Exception e) {
            return;
        }
        final Object[] args = {
            new BBMPlatformUser(contact),
        };
        Util.dispatchCallback(callback, args);
    }

    public void onMessagesExpired(BBMPlatformContact contact, BBMPlatformData[] data) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) _ioNamespace.getField(IONamespace.EVENT_ON_DATA_EXPIRED);
        } catch(Exception e) {
            return;
        }
        
        // Get data a strings
        final int numData = data.length;
        final String[] dataStrs = new String[numData];
        for(int i = 0; i < numData; i++) {
            dataStrs[i] = data[i].getDataAsString();
        }
        
        // Invoke callback
        final Object[] args = {
            new BBMPlatformUser(contact),
            dataStrs,
        };
        Util.dispatchCallback(callback, args);
    }

    public void onShareContentReceived(BBMPlatformContact contact, String description, BBMPlatformData content, long timestamp) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) UsersNamespace.getInstance().getField(UsersNamespace.EVENT_ON_SHARE_CONTENT);
        } catch(Exception e) {
            return;
        }
        final Object[] args = {
            new BBMPlatformUser(contact),
            content.getDataAsString(),
            description,
            new Date(timestamp),
        };
        Util.dispatchCallback(callback, args);
    }
        
}

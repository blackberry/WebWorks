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

import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannel;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannelListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformOutgoingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSession;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSessionListener;
import net.rim.blackberry.api.bbm.platform.service.MessagingService;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.IntHashtable;
import blackberry.bbm.platform.BBMPlatformNamespace;
import blackberry.bbm.platform.util.ScriptableFieldManager;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

public class IONamespace extends Scriptable {
    
    public static final String NAME = "io";
    
    public static final String FUNC_CREATE_CONN =           "createConnection";
    public static final String EVENT_ON_CONN_ACCEPTED =     "onconnectionaccepted";
    public static final String EVENT_ON_BBM_MENU_INVITE =   "onbbmmenuinvite";
    
    // Hosting
    public static final String FUNC_HOST =                  "host";
    public static final String FIELD_HOSTED_CONN =          "hostedConnection";
    public static final String FIELD_HOST_REQS =            "hostRequests";
    public static final String FUNC_JOIN_HOST =             "joinHost";
    public static final String FIELD_JOIN_HOST_REQS =       "joinHostRequests";
    
    // Unreachable user
    public static final String EVENT_ON_USER_REACHABLE =    "onuserreachable";
    public static final String EVENT_ON_DATA_EXPIRED =      "ondataexpired";
    
    private static IONamespace _instance;
    
    private final ScriptableFieldManager _wFields;
    
    // Fields used in io.host() function
    private Object             _hostedConn;
    public ScriptableFunction _onJoinReqReceived;
    public ScriptableFunction _onJoinReqCanceled;
    
    private final IntHashtable _joinReq2ScriptReq;
    
    private IONamespace() {
        _wFields = new ScriptableFieldManager();
        _wFields.addField(EVENT_ON_CONN_ACCEPTED);
        _wFields.addField(EVENT_ON_BBM_MENU_INVITE);
        _wFields.addField(EVENT_ON_USER_REACHABLE);
        _wFields.addField(EVENT_ON_DATA_EXPIRED);
        _hostedConn = UNDEFINED;
        _joinReq2ScriptReq = new IntHashtable();
    }
    
    public static IONamespace getInstance() {
        if(_instance == null) {
            _instance = new IONamespace();
        }
        return _instance;
    }
    
    public Object getField(String name) throws Exception {       
        if(name.equals(FUNC_CREATE_CONN)) {
            return new CreateConnectionFunction();
        // Hosting
        } else if(name.equals(FUNC_HOST)) {
            return new HostFunction();
        } else if(name.equals(FUNC_JOIN_HOST)) {
            return new JoinHostFunction();
        } else if(name.equals(FIELD_HOSTED_CONN)) {
            return this.getHostedConn();
        } else if(name.equals(FIELD_JOIN_HOST_REQS)) {
            return this.getOutgoingJoinRequests();
        } else if(name.equals(FIELD_HOST_REQS)) {
            return this.getIncomingJoinRequests();
        } else if(_wFields.hasField(name)) {
            return _wFields.getField(name);
        } else {
            return super.getField(name);
        }
    }
    
    public boolean putField(String name, Object value) {
        return _wFields.putField(name, value);
    }
    
    private class CreateConnectionFunction extends ScriptableFunctionBase {
        
        public static final String TYPE_CHANNEL = "channel";
        public static final String TYPE_SESSION = "session";
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String type = (String) args[0];
            final MessagingService msgService = BBMPlatformNamespace.getInstance().getMessagingService();
            if(type.equals(TYPE_CHANNEL)) {
                final ConnectionListenerImpl connListenerImpl = new ConnectionListenerImpl();
                final BBMPlatformChannelListener channelListener = new ChannelListener(connListenerImpl);
                
                final BBMPlatformChannel channel = msgService.createChannel(channelListener);
                ChannelObject connObj = new ChannelObject(channel, true);
                connListenerImpl.setConnectionObject(connObj);
                
                return connObj;
            } else if(type.equals(TYPE_SESSION)) {
                final ConnectionListenerImpl connListenerImpl = new ConnectionListenerImpl();
                final BBMPlatformSessionListener sessionListener = new SessionListener(connListenerImpl);
                
                final BBMPlatformSession session = msgService.createSession(sessionListener);
                SessionObject connObj = new SessionObject(session, true);
                connListenerImpl.setConnectionObject(connObj);
                
                return connObj;
            } else {
                throw new IllegalArgumentException("Unknown type: " + type);
            }
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(String.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    }
    
    private class HostFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            // If no arguments, stop hosting on any hosted connection
            if(args.length == 0) {
                try {
                    ConnectionObject scriptHostedConn = (ConnectionObject) getHostedConn();
                    BBMPlatformConnection hostedConn = scriptHostedConn.getConnection();
                    hostedConn.setPrivate();
                } catch(Exception e) {
                    // do nothing
                }
            // Otherwise, begin hosting on the connection provided
            } else {
                // If already hosting a public connection, stop it
                Object hostedConnObj = getHostedConn();
                if(! hostedConnObj.equals(UNDEFINED)) {
                    BBMPlatformConnection hostedConn = ((ConnectionObject) hostedConnObj).getConnection();
                    hostedConn.setPrivate();
                }
                
                final ConnectionObject connection =     (ConnectionObject) args[0];
                final ScriptableFunction onComplete = (ScriptableFunction) args[1];
                _onJoinReqReceived =                  (ScriptableFunction) args[2];
                _onJoinReqCanceled =                  (ScriptableFunction) args[3];
                
                Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                    protected void dispatch() {
                        boolean hosting = connection.getConnection().setPublic();
                        if(hosting) {
                            _hostedConn = connection;
                        }
                        Util.dispatchCallback(onComplete, new Object[] { new Boolean(hosting) });
                    }
                });
            }
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(4);
            sig1.addParam(ConnectionObject.class,   true);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(ScriptableFunction.class, true);
            
            FunctionSignature sig2 = new FunctionSignature(0);
            
            return new FunctionSignature[] {
                sig1,
                sig2,
            };
        }
    } // HostFunction
    
    private class JoinHostFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final int hostPIN =              (int)(Long.parseLong((String) args[0], 16) & 0x0FFFFFFFFL);
            final String hostPPID =                               (String) args[1];
            final ScriptableFunction onComplete =     (ScriptableFunction) args[2];
            final ScriptableFunction onHostAccepted = (ScriptableFunction) args[3];
            final ScriptableFunction onHostDeclined = (ScriptableFunction) args[4];
            final String cookie;
            if(args.length >= 6) {
                cookie = (String) args[5];
            } else {
                cookie = null;
            }
            
            // Validate arguments
            if(hostPIN <= 0) {
                throw new IllegalArgumentException("Invalid host PIN: " + hostPIN);
            }
            final int ppidLength = 24;
            if(hostPPID == null || hostPPID.length() != ppidLength) {
                throw new IllegalArgumentException("Invalid host PPID: " + hostPPID);
            }
            final int maxCookieLength = 128;
            if(cookie != null && cookie.length() > maxCookieLength) {
                throw new IllegalArgumentException("cookie too long: " + cookie);
            }
            
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    Object[] args;
                    try {
                        final MessagingService msgService = BBMPlatformNamespace.getInstance().getMessagingService();
                        final BBMPlatformOutgoingJoinRequest request = msgService.sendJoinRequest(hostPIN, hostPPID, cookie);
                        final Object scriptRequest;
                        if(request == null) {
                            scriptRequest = UNDEFINED;
                        } else {
                            scriptRequest = new ScriptableOutgoingJoinRequest(request, onHostAccepted, onHostDeclined);
                            _joinReq2ScriptReq.put(request.getRequestId(), scriptRequest);
                        }
                        
                        args = new Object[] {
                            scriptRequest,
                        };
                    } catch(Exception e) {
                        Util.logError("MessagingService#sendJoinRequest() threw " + e);
                        args = null;
                    }
                    Util.dispatchCallback(onComplete, args);
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(6);
            sig1.addParam(String.class,              true);
            sig1.addParam(String.class,              true);
            sig1.addParam(ScriptableFunction.class,  true);
            sig1.addParam(ScriptableFunction.class,  true);
            sig1.addParam(ScriptableFunction.class,  true);
            sig1.addParam(String.class,              false);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // JoinHostFunction
    
    private ScriptableOutgoingJoinRequest[] getOutgoingJoinRequests() {
        final MessagingService msgService = BBMPlatformNamespace.getInstance().getMessagingService();
        final Enumeration reqs = msgService.getOutgoingJoinRequests();
        if(reqs == null) {
            return new ScriptableOutgoingJoinRequest[0];
        } else {
            final Vector vectorReqs = new Vector();
            while(reqs.hasMoreElements()) {
                vectorReqs.addElement(reqs.nextElement());
            }
            
            final ScriptableOutgoingJoinRequest[] scriptableReqs = new ScriptableOutgoingJoinRequest[vectorReqs.size()];
            for(int i = 0; i < scriptableReqs.length; i++) {
                scriptableReqs[i] = new ScriptableOutgoingJoinRequest((BBMPlatformOutgoingJoinRequest) vectorReqs.elementAt(i), null, null);
            }
            return scriptableReqs;
        }
    }
    
    private ScriptableIncomingJoinRequest[] getIncomingJoinRequests() {
        Object hostedConnObj = this.getHostedConn();
        if(hostedConnObj.equals(UNDEFINED)) {
            return new ScriptableIncomingJoinRequest[0];
        } else {
            BBMPlatformConnection hostedConn = ((ConnectionObject) hostedConnObj).getConnection();
            final Enumeration reqs = hostedConn.getIncomingJoinRequests();
            if(reqs == null) {
                return new ScriptableIncomingJoinRequest[0];
            } else {
                final Vector vectorReqs = new Vector();
                while(reqs.hasMoreElements()) {
                    vectorReqs.addElement(reqs.nextElement());
                }
                
                final ScriptableIncomingJoinRequest[] scriptableReqs = new ScriptableIncomingJoinRequest[vectorReqs.size()];
                for(int i = 0; i < scriptableReqs.length; i++) {
                    scriptableReqs[i] = new ScriptableIncomingJoinRequest((BBMPlatformIncomingJoinRequest) vectorReqs.elementAt(i));
                }
                return scriptableReqs;
            }
        }
    }
    
    private Object getHostedConn() {
        if(_hostedConn.equals(UNDEFINED)) {
            return UNDEFINED;
        } else {
            // If we have a connection and it is not hosted, correct it by setting as undefined
            ConnectionObject hostedConn = (ConnectionObject) _hostedConn;
            if(! hostedConn.getConnection().isPublic()) {
                _hostedConn = UNDEFINED;
            }
            
            return _hostedConn;
        }
    }
    
    public ScriptableOutgoingJoinRequest removeOutgoingJoinReq(BBMPlatformOutgoingJoinRequest request) {
        final int requestID = request.getRequestId();
        final ScriptableOutgoingJoinRequest scriptRequest = (ScriptableOutgoingJoinRequest) _joinReq2ScriptReq.remove(requestID);
        if(scriptRequest == null) {
            Util.logError("No script request found for request: " + requestID);
            return null;
        } else {
            return scriptRequest;
        }
    }
}

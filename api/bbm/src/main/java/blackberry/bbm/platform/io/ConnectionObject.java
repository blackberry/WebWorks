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
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.blackberry.api.bbm.platform.profile.ContactListProvider;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.ui.UiApplication;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.util.ScriptableFieldManager;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class ConnectionObject extends Scriptable {
    
    public static final String FUNC_INVITE_CONTACTS =       "inviteContacts";
    public static final String FUNC_ADD =                   "add";
    public static final String FUNC_SEND =                  "send";
    public static final String FUNC_REMOVE =                "remove";
    public static final String FUNC_REMOVE_ALL =            "removeAll";
    
    public static final String EVENT_ON_DATA =              "ondata";
    public static final String EVENT_ON_USER_DECLINED =     "onuserdeclined";
    public static final String EVENT_ON_USER_LEFT =         "onuserleft";
    public static final String EVENT_ON_USERS_JOINED =      "onusersjoined";
    public static final String EVENT_ON_USERS_INVITED =     "onusersinvited";
    
    public static final String FIELD_ID =                   "id";
    public static final String FIELD_JOINED_USERS =         "joinedUsers";
    public static final String FIELD_PENDING_USERS_COUNT =  "pendingUsersCount";
    
    public static final String CONST_MAX_USERS =            "MAX_USERS";
    public static final String CONST_MAX_COOKIE_LENGTH =    "MAX_COOKIE_LENGTH";
    public static final String CONST_MAX_INVITE_MSG_LENGTH ="MAX_INVITE_MSG_LENGTH";
    public static final String CONST_MAX_DATA_LENGTH =      "MAX_DATA_LENGTH";
    
    protected final BBMPlatformConnection _connection;
    protected final ScriptableFieldManager _wFields;
    
    private boolean _isDelivered;
    
    public ConnectionObject(BBMPlatformConnection connection, boolean isDelivered) {
        _connection = connection;
        _isDelivered = isDelivered;
        
        _wFields = new ScriptableFieldManager();
        _wFields.addField(EVENT_ON_DATA);
        _wFields.addField(EVENT_ON_USER_DECLINED);
        _wFields.addField(EVENT_ON_USER_LEFT);
        _wFields.addField(EVENT_ON_USERS_JOINED);
        _wFields.addField(EVENT_ON_USERS_INVITED);
    }
    
    public Object getField(String name) throws Exception {
        // Constants
        if(       name.equals(CONST_MAX_USERS)) {
            return new Integer(_connection.getMaxContacts());
        } else if(name.equals(CONST_MAX_COOKIE_LENGTH)) {
            return new Integer(_connection.getMaxCookieLength());
        } else if(name.equals(CONST_MAX_INVITE_MSG_LENGTH)) {
            return new Integer(_connection.getMaxInvitationMessageLength());
        } else if(name.equals(CONST_MAX_DATA_LENGTH)) {
            return new Integer(_connection.getMaxDataSize());
        // Properties
        } else if(name.equals(FIELD_ID)) {
            return new Integer(_connection.getId());
        } else if(name.equals(FIELD_JOINED_USERS)) {
            return Util.contactListToArray(_connection.getContactList());
        } else if(name.equals(FIELD_PENDING_USERS_COUNT)) {
            return new Integer(_connection.getPendingContactsCount());
        // Functions
        } else if(name.equals(FUNC_INVITE_CONTACTS)) {
            return new InviteContactsFunction();
        } else if(name.equals(FUNC_ADD)) {
            return new AddFunction();
        } else if(name.equals(FUNC_SEND)) {
            return new SendFunction();
        } else if(name.equals(FUNC_REMOVE)) {
            return new RemoveFunction();
        } else if(name.equals(FUNC_REMOVE_ALL)) {
            return new RemoveAllFunction();
        } else if(_wFields.hasField(name)) {
            return _wFields.getField(name);
        } else {
            return UNDEFINED;
        }
    }
    
    public boolean putField(String name, Object value) throws Exception {
        return _wFields.putField(name, value); 
    }
    
    public BBMPlatformConnection getConnection() {
        return _connection;
    }
    
    /**
     * Whether or not the connection has been delivered through io.onConnectionCreated.
     */
    public boolean isDelivered() {
        return _isDelivered;
    }
    
    public void setDelivered() {
        _isDelivered = true;
    }
    
    private class InviteContactsFunction extends ScriptableFunctionBase {
        
        public static final String OPTIONS_FIELD_EXPIRY_TIME =  "expiryTime";
        public static final String OPTIONS_FIELD_COOKIE =       "cookie";
        public static final String OPTIONS_FIELD_CONTACTS =     "contacts";
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String invitationMsg = (String) args[0];
            
            long expiryTime;
            String cookie;
            ContactListProvider contactList;
            
            if(args.length == 2) {
                final Scriptable options = (Scriptable) args[1];
                
                Object expiryTimeObj = options.getField(OPTIONS_FIELD_EXPIRY_TIME);
                if(expiryTimeObj.equals(UNDEFINED)) {
                    expiryTime = 0;
                } else {
                    try {
                        expiryTime = Long.parseLong(expiryTimeObj.toString());
                    } catch(Exception e) {
                        expiryTime = 0;
                    }
                }
                
                try {
                    cookie = (String) options.getField(OPTIONS_FIELD_COOKIE);
                } catch(Exception e) {
                    cookie = null;
                }
                
                try {
                    BBMPlatformUser[] contacts = (BBMPlatformUser[]) options.getField(OPTIONS_FIELD_CONTACTS);
                    contactList = new Util.SimpleContactListProvider(contacts);
                } catch(Exception e) {
                    contactList = null;
                }
            } else {
                expiryTime = 0;
                cookie = null;
                contactList = null;
            }
            
            // Validate args
            if(invitationMsg != null && invitationMsg.length() > _connection.getMaxInvitationMessageLength()) {
                throw new IllegalArgumentException("inviteMessage.length > " + _connection.getMaxInvitationMessageLength());
            }
            if(cookie != null && cookie.length() > _connection.getMaxCookieLength()) {
                throw new IllegalArgumentException("cookie.length > " + _connection.getMaxCookieLength());
            }
            
            final String finalCookie = cookie;
            final long finalExpTime = expiryTime;
            final ContactListProvider finalContactList = contactList;

            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    try {
                        _connection.sendInvitation(invitationMsg, finalCookie, finalExpTime, finalContactList);
                    } catch(Exception e) {
                        Util.logError("sendInvitation(" + invitationMsg + ", " +
                                                          finalCookie + ", " +
                                                          finalExpTime + ", " +
                                                          finalContactList + ") threw " + e);
                    }
                }
            });
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(String.class, true);
            sig1.addParam(Scriptable.class, false);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // InviteContactsFunction
    
    private class AddFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final BBMPlatformUser user = (BBMPlatformUser) args[0];
            final BBMPlatformContact contact = (BBMPlatformContact) user.getPresence();
            
            // Cookie is optional
            final String cookie;
            if(args.length >= 2) {
                cookie = (String) args[1];
            } else {
                cookie = null;
            }
            
            _connection.add(contact, cookie);
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(BBMPlatformUser.class, true);
            sig1.addParam(String.class,          false);
            return new FunctionSignature[] {
                sig1
            };
        }        
    } // AddFunction
    
    private class SendFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Object data = args[0];
            
            // Get user array
            BBMPlatformUser[] users = Util.scriptableUsersArrayToUserArray((Scriptable) args[1]);
            
            if(users == null) {
                _connection.sendData(new BBMPlatformData(data.toString()), (BBMPlatformContact) null);
            } else {
                _connection.sendData(new BBMPlatformData(data.toString()), Util.userArrayToContactList(users));
            }
            
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(Object.class, true);
            sig1.addParam(Scriptable.class, false);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // SendFunction
    
    private class RemoveFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            Object arg0 = args[0];
            if(arg0 instanceof BBMPlatformUser) {
                BBMPlatformUser user = (BBMPlatformUser) arg0;
                BBMPlatformContact contact = (BBMPlatformContact) user.getPresence();
                _connection.remove(contact);
            } else if(arg0 instanceof Scriptable) {
                BBMPlatformUser[] usersArray = Util.scriptableUsersArrayToUserArray((Scriptable) arg0);
                BBMPlatformContactList contactList = Util.userArrayToContactList(usersArray);
                _connection.remove(contactList);
            } else {
                throw new IllegalArgumentException("BBMPlatformUser or BBMPlatformUser[] must be provided");
            }
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(BBMPlatformUser.class, true);
            FunctionSignature sig2 = new FunctionSignature(1);
            sig2.addParam(Scriptable.class, true);
            return new FunctionSignature[] {
                sig1,
                sig2,
            };
        }
    } // RemoveFunction
    
    private class RemoveAllFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            _connection.removeAllContacts();
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // RemoveAllFunction
}

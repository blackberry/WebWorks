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
package blackberry.bbm.platform.users;

import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.IOErrorCode;
import net.rim.blackberry.api.bbm.platform.profile.BBMInvitationRequest;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.blackberry.api.bbm.platform.profile.ContactListProvider;
import net.rim.blackberry.api.bbm.platform.profile.PresenceListener;
import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.blackberry.api.bbm.platform.service.ContactListService;
import net.rim.blackberry.api.bbm.platform.service.MessagingService;
import net.rim.blackberry.api.bbm.platform.service.UIService;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;
import blackberry.bbm.platform.BBMPlatformNamespace;
import blackberry.bbm.platform.io.ConnectionObject;
import blackberry.bbm.platform.io.MessagingServiceListenerImpl;
import blackberry.bbm.platform.self.SelfNamespace;
import blackberry.bbm.platform.util.ScriptableFieldManager;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

public class UsersNamespace extends Scriptable {
    public static final String NAME = "users";
    
    private static final String FUNC_START_BBM_CHAT =       "startBBMChat";
    private static final String FUNC_PICK_USERS =           "pickUsers";
    private static final String FUNC_INVITE_TO_DOWNLOAD =   "inviteToDownload";
    private static final String FUNC_INVITE_TO_BBM =        "inviteToBBM";
    private static final String FUNC_INVITE_TO_BBM_CONN =   "inviteToBBMFromConnections";
    private static final String FUNC_SEND_FILE =            "sendFile";
    private static final String FIELD_CONTACTS_WITH_APP =   "contactsWithApp";
    private static final String EVENT_ON_UPDATE =           "onupdate";
    private static final String FUNC_SHARE_CONTENT =        "shareContent";
    public static final String EVENT_ON_SHARE_CONTENT =     "onsharecontent";
    
    private static UsersNamespace instance;

    private ContactListService _contactList;
    private UIService _uiService;
    private final ScriptableFieldManager _wFields;
    
    private UsersNamespace() {
        _wFields = new ScriptableFieldManager();
        _wFields.addField(EVENT_ON_UPDATE);
        _wFields.addField(EVENT_ON_SHARE_CONTENT);
    }
    
    public static UsersNamespace getInstance() {
        if(instance == null) {
            instance = new UsersNamespace();
        }
        
        return instance;
    }
    
    public void init() {
        BBMPlatformContext context = BBMPlatformNamespace.getInstance().getContext();
        _contactList = context.getContactListService();
        _contactList.setPresenceListener(new MyPresenceListener());
        _uiService =   context.getUIService();
    }
    
    public boolean putField(String name, Object value) throws Exception {
        return _wFields.putField(name, value);
    }

    public Object getField(String name) throws Exception {
        if(       name.equals(FIELD_CONTACTS_WITH_APP)) {
            return getContactsWithApp();
        } else if(name.equals(FUNC_PICK_USERS)) {
            return new PickUsersFunction(); 
        } else if(name.equals(FUNC_START_BBM_CHAT)) {
            return new StartBBMChatFunction();
        } else if(name.equals(FUNC_INVITE_TO_DOWNLOAD)) {
            return new InviteToDownloadFunction();
        } else if(name.equals(FUNC_INVITE_TO_BBM)) {
            return new InviteToBBMFunction();
        } else if(name.equals(FUNC_INVITE_TO_BBM_CONN)) {
            return new InviteToBBMConnFunction();
        } else if(name.equals(FUNC_SEND_FILE)) {
            return new SendFileFunction();
        } else if(name.equals(FUNC_SHARE_CONTENT)) {
            return new ShareContentFunction();
        } else if(_wFields.hasField(name)) {
            return _wFields.getField(name);
        } else {
            return super.getField(name);
        }
    }
    
    private class MyPresenceListener implements PresenceListener {

        public void contactUpdated(BBMPlatformContact contact, int eventType) {
            invokeOnUpdate(new BBMPlatformUser(contact), eventType);
        }

        public void userUpdated(UserProfile user, int eventType) {
            invokeOnUpdate(SelfNamespace.getInstance(),  eventType);
        }
        
        private void invokeOnUpdate(Scriptable user, int eventType) {
            final ScriptableFunction callback;
            try {
                callback = (ScriptableFunction) _wFields.getField(EVENT_ON_UPDATE);
            } catch(Exception e) {
                return;
            }
            final Object[] args = new Object[] {
                user,
                eventTypeToString(eventType),
            };
                
            Util.dispatchCallback(callback, args);
        }
    } // MyPresenceListener
    
    private class PickUsersFunction extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Scriptable options =                    (Scriptable) args[0];
            final ScriptableFunction onComplete = (ScriptableFunction) args[1];
            
            // title is optional. default = null
            final String title;
            final Object titleObj = options.getField("title");
            if(titleObj.equals(UNDEFINED)) {
                title = null;
            } else {
                title = (String) titleObj;
            }
           
            // multiSelect is optional. default = false
            final Object multiSelectObj = options.getField("multiSelect");
            final boolean multiSelect;
            if(multiSelectObj.equals(UNDEFINED)) {
                multiSelect = false;
            } else {
                multiSelect = ((Boolean) multiSelectObj).booleanValue();
            }
            
            // showSelectAll is optional. default = false
            final Object showSelectAllObj = options.getField("showSelectAll");
            final boolean showSelectAll;
            if(showSelectAllObj.equals(UNDEFINED)) {
                showSelectAll = false;
            } else {
                showSelectAll = ((Boolean) showSelectAllObj).booleanValue();
            }
            
            // type is optional. default = -1
            final Object type =  options.getField("type");
            final int groupTypeInt;
            if(type.equals(UNDEFINED)) {
                groupTypeInt = -1;
            } else {
                groupTypeInt = Util.groupTypeStrToInt((String) type);
            }
            
            // users is optional. default = null
            final Object users = options.getField("users");
            final ContactListProvider userList;
            if(users.equals(UNDEFINED)) {
                userList = null;
            } else {
                BBMPlatformUser[] usersArray = Util.scriptableUsersArrayToUserArray((Scriptable) users);
                userList = new Util.SimpleContactListProvider(usersArray);
            }
            
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    Object pickedUsersArray;
                    try {
                        BBMPlatformContactList pickedUsersList;
                        
                        if(users.equals(UNDEFINED)) { // users not provided, use type
                            try {
                                pickedUsersList = _uiService.showContactPicker(title, groupTypeInt,           multiSelect, showSelectAll);
                            } catch(Exception e) {
                                Util.logError("UIService#showContactPicker(String, int, boolean, boolean) threw " + e);
                                pickedUsersList = new BBMPlatformContactList();
                            }
                        } else if(type.equals(UNDEFINED)) { // type not provided, use users
                            try {
                                pickedUsersList = _uiService.showContactPicker(title, userList,               multiSelect, showSelectAll);
                            } catch(Exception e) {
                                Util.logError("UIService#showContactPicker(String, ContactListProvider, boolean, boolean) threw " + e);
                                pickedUsersList = new BBMPlatformContactList();
                            }
                        } else { // both users and type provided, use both
                            try {
                                pickedUsersList = _uiService.showContactPicker(title, userList, groupTypeInt, multiSelect, showSelectAll);
                            } catch(Exception e) {
                                Util.logError("UIService#showContactPicker(String, ContactListProvider, int, boolean, boolean) threw " + e);
                                pickedUsersList = new BBMPlatformContactList();
                            }
                        }
                        
                        pickedUsersArray = Util.contactListToArray(pickedUsersList);
                    } catch(Exception e) {
                        Util.logError("PickUsersFunction threw " + e);
                        pickedUsersArray = Util.wrapUserArrayIn50(new BBMPlatformUser[0]);
                    }
                    
                    // Invoke callback
                    Util.dispatchCallback(onComplete, new Object[] { pickedUsersArray });
                    
                }
            });

            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(Scriptable.class, true);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                sig1,
            };
        }
    } // PickUsersFunction
    
    private class StartBBMChatFunction extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];
            final String message =                            (String) args[1];
            
            if(args.length == 2) {
                // Need to launch on another thread since the Contact Picker dialog blocks
                UiApplication.getUiApplication().invokeLater(new Runnable() {
                    public void run() {
                        try {
                            _uiService.startBBMChat(message);
                        } catch(Exception e) {
                            Util.logError("UIService#startBBMChat(String) threw " + e);
                        }
                        Util.dispatchCallback(onComplete, null);
                    }
                });
            } else if(args.length >= 3) {
                final Scriptable users = (Scriptable) args[2];
                
                // Create contact list
                final BBMPlatformContactList contacts = new BBMPlatformContactList();
                final int numUsers = users.getElementCount();
                for(int i = 0; i < numUsers; i++) {
                    BBMPlatformUser user = (BBMPlatformUser) users.getElement(i);
                    contacts.add((BBMPlatformContact) user.getPresence());
                }
                
                _uiService.startBBMChat(contacts, message);
                Util.dispatchCallback(onComplete, null);
            }
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(3);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(String.class,             true);
            sig1.addParam(Scriptable.class,         false);
            
            return new FunctionSignature[] {
                sig1,
            };
        }
    } // StartBBMChatFunction
    
    private class InviteToBBMFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];        
            final Scriptable scriptInvitations =          (Scriptable) args[1];
            final BBMInvitationRequest[] invitations = this.scriptInvitesToJava(scriptInvitations);
            this.validateInvitations(invitations);
            
            Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                protected void dispatch() {
                    try {
                        _uiService.inviteToBBM(invitations);
                    } catch(Exception e) {
                        Util.logError("UIService#inviteToBBM(BBMInvitationRequest[]) threw " + e);
                    }
                    try {
                        onComplete.invoke(null, null);
                    } catch(Exception e) {
                        // do nothing
                    }
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(Scriptable.class,         true);
            return new FunctionSignature[] {
                sig1,
            };
        }
        
        private BBMInvitationRequest[] scriptInvitesToJava(Scriptable scriptInvites) throws Exception {
            final int numInvites = scriptInvites.getElementCount();
            final BBMInvitationRequest[] javaInvites = new BBMInvitationRequest[numInvites];
            final String fieldName = "name";
            final String fieldPin =  "pin";
            
            for(int i = 0; i < numInvites; i++) {
                Scriptable scriptInvite = (Scriptable) scriptInvites.getElement(i);
                String pinObj =  (String) scriptInvite.getField(fieldPin);
                String nameObj = (String) scriptInvite.getField(fieldName);
                
                javaInvites[i] = new BBMInvitationRequest(pinObj, nameObj);
            }
            return javaInvites;
        }
        
        private void validateInvitations(BBMInvitationRequest[] invitations) {
            final int numInvitations = invitations.length;
            if(numInvitations > 24) {
                throw new IllegalArgumentException("invitations.length > 24");
            }
            
            for(int i = 0; i < numInvitations; i++) {
                final BBMInvitationRequest invite = invitations[i];
                final String name = invite.getName();
                if(name == null || name.length() == 0) {
                    throw new IllegalArgumentException("Invalid name: " + name);
                }
                
                final String pin = invite.getId();
                if(! Util.isValidPIN(pin)) {
                    throw new IllegalArgumentException("Invalid PIN: " + pin);
                }
            }
        }
    } // InviteToBBMFunction
    
    private class InviteToDownloadFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];
            
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    MessagingService msgService = BBMPlatformNamespace.getInstance().getMessagingService();
                    final int result = msgService.sendDownloadInvitation();
                    
                    Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                        protected void dispatch() {
                            // Convert to WW result object
                            final Object resultObj;
                            if(result == IOErrorCode.DOWNLOAD_INVITATION_LIMIT_REACHED) {
                                resultObj = "limitreached";
                            } else {
                                resultObj = UNDEFINED;
                            }
                            
                            try {
                                onComplete.invoke(null, new Object[] { resultObj });
                            } catch(Exception e) {
                                // do nothing
                            }
                        }
                    });
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // InviteToDownloadFunction
    
    private class InviteToBBMConnFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];
            
            // Connection object is optional
            final ConnectionObject connObj;
            if(args.length >= 2) {
                connObj = (ConnectionObject) args[1];
            } else {
                connObj = null;
            }
            
            // Invite from all open connections
            if(connObj == null) {
                Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                    protected void dispatch() {
                        _uiService.inviteToBBM();
                        try {
                            onComplete.invoke(null, null);
                        } catch(Exception e) {
                            // do nothing
                        }
                    }
                });
            // Invite from a specific connection
            } else {
                final BBMPlatformConnection conn = ((ConnectionObject) connObj).getConnection();
                Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                    protected void dispatch() {
                        _uiService.inviteToBBM(conn);
                        try {
                            onComplete.invoke(null, null);
                        } catch(Exception e) {
                            // do nothing
                        }
                    }
                });
            }
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(ConnectionObject.class,   false);
            return new FunctionSignature[] {
                sig1,
            };
        }
    } // InviteToBBMConnFunction
    
    private class SendFileFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String fileURI =                           (String) args[0];
            final String comment =                           (String) args[1];
            final ScriptableFunction onFailure = (ScriptableFunction) args[2];
            
            final BBMPlatformNamespace bbmpNpsc = BBMPlatformNamespace.getInstance();
            final MessagingService msgService = bbmpNpsc.getMessagingService();
            final MessagingServiceListenerImpl msgServiceListener = bbmpNpsc.getMessagingServiceListener();
            
            if(args.length == 3) {
                msgServiceListener.setOnFileTransferFailed(onFailure);
                msgService.sendFile(fileURI, comment);
            } else if(args.length == 4) {
                BBMPlatformUser scriptContact = ((BBMPlatformUser) args[3]);
                BBMPlatformContact contact = (BBMPlatformContact) scriptContact.getPresence();
                
                msgServiceListener.setOnFileTransferFailed(onFailure);
                msgService.sendFile(contact, fileURI, comment);
            }
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(4);
            sig1.addParam(String.class,             true);
            sig1.addParam(String.class,             true);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(Scriptable.class,         false);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // SendFileFunction
    
    private static class ShareContentFunction extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String content =                            (String) args[0];
            final String description =                        (String) args[1];
            final ScriptableFunction onComplete = (ScriptableFunction) args[2];
            final Object options;
            if(args.length >= 4) {
                options = args[3];
            } else {
                options = null;
            }
            
            // Throw exceptions for too long parameters
            final int maxContentLength = 61124;
            if(content.length() > maxContentLength) {
                throw new IllegalArgumentException("content.length > " + maxContentLength);
            }
            final int maxDescLength = 128;
            if(description.length() > maxDescLength) {
                throw new IllegalArgumentException("description.length > " + maxDescLength);
            }
            
            // Parse options object for title and contacts
            final String title;
            final ContactListProvider contacts;
            if(options == null || options.equals(UNDEFINED)) {
                title = null;
                contacts = null;
            } else {
                // Title
                final Scriptable optionsObj = (Scriptable) options;
                final Object titleObj = optionsObj.getField("title");
                if(titleObj.equals(UNDEFINED)) {
                    title = null;
                } else {
                    title = (String) titleObj;
                }
                
                // Contacts
                Object contactsObj = optionsObj.getField("users");
                if(contactsObj.equals(UNDEFINED)) {
                    contacts = null;
                } else {
                    final BBMPlatformUser[] users = Util.scriptableUsersArrayToUserArray((Scriptable) contactsObj);
                    contacts = new Util.SimpleContactListProvider(users);
                }
            }
            
            // Call API
            final BBMPlatformNamespace bbmpNpsc = BBMPlatformNamespace.getInstance();
            final MessagingService msgService = bbmpNpsc.getMessagingService();
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    msgService.shareContent(description, new BBMPlatformData(content), contacts, title);
                    Util.dispatchCallback(onComplete, null);
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(4);
            sig1.addParam(String.class,             true);
            sig1.addParam(String.class,             true);
            sig1.addParam(ScriptableFunction.class, true);
            sig1.addParam(Scriptable.class,         false);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // ShareContentFunction

    private static String eventTypeToString(int eventType) {
        switch(eventType) {
            case PresenceListener.EVENT_TYPE_DISPLAY_PICTURE:       return "displaypicture";
            case PresenceListener.EVENT_TYPE_DISPLAY_NAME:          return "displayname";
            case PresenceListener.EVENT_TYPE_PERSONAL_MESSAGE:      return "personalmessage";
            case PresenceListener.EVENT_TYPE_STATUS:                return "status";
            case PresenceListener.EVENT_TYPE_INSTALL_APP:           return "install";
            case PresenceListener.EVENT_TYPE_UNINSTALL_APP:         return "uninstall";
            case PresenceListener.EVENT_TYPE_INVITATION_RECEIVED:   return "invited";
            default: throw new IllegalArgumentException("eventType " + eventType + " is unknown");
        }
    }
    
    private static Object getContactsWithApp() {
        final ContactListService contactService = BBMPlatformNamespace.getInstance().getContext().getContactListService();
        final BBMPlatformContactList contactList = contactService.getContactList();
        return Util.contactListToArray(contactList);
    }
}
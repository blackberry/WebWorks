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
package blackberry.bbm.platform.self;

import net.rim.blackberry.api.bbm.platform.profile.ProfileLocation;
import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.Bitmap;
import blackberry.bbm.platform.BBMPlatformNamespace;
import blackberry.bbm.platform.self.location.LocationNamespace;
import blackberry.bbm.platform.self.profilebox.ProfileBoxNamespace;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

public class SelfNamespace extends BBMPlatformUser {
    
    public static final String NAME = "self";
    
    public static final String FUNC_SET_PERSONAL_MSG =  "setPersonalMessage";
    public static final String FUNC_SET_STATUS =        "setStatus";
    public static final String FUNC_SET_DISPLAY_PIC =   "setDisplayPicture";
    
    private static SelfNamespace instance;
    
    private SelfNamespace() {
        super(null);
    }
    
    public static SelfNamespace getInstance() {
        if(instance == null) {
            instance = new SelfNamespace();
        }
        return instance;
    }
    
    public Object getField(String name) throws Exception {
        final UserProfile userProfile = BBMPlatformNamespace.getInstance().getContext().getUserProfile();
        super._presence = userProfile;
        
        if(name.equals(LocationNamespace.NAME)) {
            ProfileLocation location = userProfile.getProfileLocation();
            if(location == null) {
                return UNDEFINED;
            } else {
                return new LocationNamespace(location);
            }
        } else if(name.equals(ProfileBoxNamespace.NAME)) {
            return ProfileBoxNamespace.getInstance(userProfile);
        } else if(name.equals(SelfNamespace.FUNC_SET_PERSONAL_MSG)) {
            return new SetPersonalMessage();
        } else if(name.equals(SelfNamespace.FUNC_SET_STATUS)) {
            return new SetStatus();
        } else if(name.equals(SelfNamespace.FUNC_SET_DISPLAY_PIC)) {
            return new SetDisplayPic();
        }
        // Otherwise the field is shared by both UserProfile and BBMPlatformContact
        else {
            return super.getField(name);
        }
    }
    
    /**
     * Sets the user's personal message.
     */
    private class SetPersonalMessage extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String message =                            (String) args[0];
            final ScriptableFunction onComplete = (ScriptableFunction) args[1];
            
            Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                protected void dispatch() {
                    boolean result;
                    try {
                        result = ((UserProfile) _presence).setPersonalMessage(message);
                    } catch(Exception e) {
                        Util.logError("UserProfile#setPersonalMessage() threw " + e);
                        result = false;
                    }
                    try {
                        onComplete.invoke( null, new Object[] { new Boolean(result) });
                    } catch(Exception e) {
                        // do nothing
                    }
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addNullableParam(String.class,     true);
            sig1.addParam(ScriptableFunction.class, true);
            
            return new FunctionSignature[] {
                sig1
            };
        }
    }
    
    /**
     * Sets the user's status.
     */
    private class SetStatus extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String status =                             (String) args[0];
            final String statusMessage =                      (String) args[1];
            final ScriptableFunction onComplete = (ScriptableFunction) args[2];
            final int statusInt = BBMPlatformUser.statusToInt(status);
            
            Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                protected void dispatch() {
                    boolean result;
                    try {
                        result = ((UserProfile) _presence).setStatus(statusInt, statusMessage);
                    } catch(Exception e) {
                        Util.logError("UserProfile#setStatus() threw " + e);
                        result = false;
                    }
                    try {
                        onComplete.invoke( null, new Object[] { new Boolean(result) });
                    } catch(Exception e) {
                        // do nothing
                    }
                }
            });
            
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(3);
            sig1.addParam(String.class,             true);
            sig1.addNullableParam(String.class,     true);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    }
    
    /**
     * Sets the user's display picture.
     */
    private class SetDisplayPic extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final String displayPicURI = (String) args[0];
            final ScriptableFunction onComplete = (ScriptableFunction) args[1];
            final Bitmap displayPic = Util.requestBitmap(displayPicURI);
            
            Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                protected void dispatch() {
                    boolean result;
                    try { 
                        result = ((UserProfile) _presence).setDisplayPicture(displayPic);
                    } catch(Exception e) {
                        Util.logError("UserProfile#setDisplayPicture() threw " + e);
                        result = false;
                    }
                    try {
                        onComplete.invoke( null, new Object[] { new Boolean(result) });
                    } catch(Exception e) {
                        // do nothing
                    }
                }
            });
            
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(2);
            sig1.addParam(String.class,             true);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                    sig1
            };
        }
        
    }
}

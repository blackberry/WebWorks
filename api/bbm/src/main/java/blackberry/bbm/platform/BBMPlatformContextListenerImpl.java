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
package blackberry.bbm.platform;

import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContextListener;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.Presence;
import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBoxItem;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.bbm.platform.self.SelfNamespace;
import blackberry.bbm.platform.self.profilebox.ScriptableProfileBoxItem;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.bbm.platform.util.ConstantsUtil;
import blackberry.bbm.platform.util.Util;

public class BBMPlatformContextListenerImpl extends BBMPlatformContextListener {
    
    private final BBMPlatformNamespace _platform;
    
    public BBMPlatformContextListenerImpl(BBMPlatformNamespace platform) {
        _platform = platform;
    }

    public void accessChanged(boolean isAccessAllowed, int code) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) _platform.getField(BBMPlatformNamespace.EVENT_ON_ACCESS_CHANGED);
        } catch(Exception e1){
            return;
        }
        
        if(isAccessAllowed == true) {
            _platform.initBBMPlatformObjects();
        }
        
        final Object status;
        try {
            status = ConstantsUtil.accessCodeToString(code); 
        } catch(Exception e) {
            // If unknown access code, don't invoke
            Util.logWarning(e.toString());
            return;
        }
            
        final Object[] args = new Object[] {
            new Boolean(isAccessAllowed),
            status,
        };
        
        Util.dispatchCallback(callback, args);
    }

    public void appInvoked(int reason, Object param, Presence user) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) _platform.getField(BBMPlatformNamespace.EVENT_ON_APP_INVOKED);
        } catch(Exception e1){
            return;
        }
        
        // Get reason string and scriptable param
        final String reasonStr;
        final Object scriptableParam;
        switch(reason) {
            case BBMPlatformContext.INVOKE_PROFILE_BOX_ITEM:
                reasonStr = "profilebox";
                scriptableParam = new ScriptableProfileBoxItem(_platform.getProfileBox(), (UserProfileBoxItem) param);
                break;
            case BBMPlatformContext.INVOKE_PROFILE_BOX:
                reasonStr = "profileboxtitle";
                scriptableParam = Scriptable.UNDEFINED;
                break;
            case BBMPlatformContext.INVOKE_PERSONAL_MESSAGE:
                reasonStr = "personalmessage";
                scriptableParam = (String) param;
                break;
            case BBMPlatformContext.INVOKE_CHAT_MESSAGE:
                reasonStr = "chatmessage";
                scriptableParam = Scriptable.UNDEFINED;
                break;
            default:
                // Don't invoke if we don't know what to do with the reason
                return;
        }
        
        // Get scriptable user: either self namespace of BBMPlatformUser instance
        final Object scriptableUser;
        if(user instanceof UserProfile) {
            scriptableUser = SelfNamespace.getInstance();
        } else if(user instanceof BBMPlatformContact) {
            scriptableUser = new BBMPlatformUser(user);
        } else {
            scriptableUser = Scriptable.UNDEFINED;
        }
        
        final Object[] args = {
            reasonStr,
            scriptableParam,
            scriptableUser,
        };
        Util.dispatchCallback(callback, args);
    }
    
}

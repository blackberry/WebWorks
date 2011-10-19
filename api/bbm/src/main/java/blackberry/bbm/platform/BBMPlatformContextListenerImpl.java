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
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBoxItem;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.bbm.platform.self.profilebox.ScriptableProfileBoxItem;
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

    public void appInvoked(int reason, Object param) {
        final ScriptableFunction callback;
        try {
            callback = (ScriptableFunction) _platform.getField(BBMPlatformNamespace.EVENT_ON_APP_INVOKED);
        } catch(Exception e1){
            return;
        }
        
        final Object[] args;
        if(reason == BBMPlatformContext.INVOKE_PROFILE_BOX_ITEM) {
            final ScriptableProfileBoxItem scriptItem =
                new ScriptableProfileBoxItem(_platform.getProfileBox(), (UserProfileBoxItem) param);
            args = new Object[] { "profilebox", scriptItem };
        } else {
            return;
        }
        
        Util.dispatchCallback(callback, args);
    }
}

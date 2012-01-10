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

import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.Presence;
import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.device.api.script.Scriptable;
import blackberry.bbm.platform.util.Util;

public class BBMPlatformUser extends Scriptable {
    
    public static final String FIELD_DISPLAY_PIC =     "displayPicture";
    public static final String FIELD_DISPLAY_NAME =    "displayName";
    public static final String FIELD_PERSONAL_MSG =    "personalMessage";
    public static final String FIELD_STATUS =          "status";
    public static final String FIELD_STATUS_MSG =      "statusMessage";
    public static final String FIELD_HANDLE =          "handle";
    public static final String FIELD_PPID =            "ppid";
    public static final String FIELD_TYPE =            "type";
    public static final String FIELD_APP_VERSION =     "appVersion";
    public static final String FIELD_BBM_SDK_VERSION = "bbmsdkVersion";
    
    public static final String STATUS_STR_AVAILABLE = "available";
    public static final String STATUS_STR_BUSY =      "busy";
    
    protected Presence _presence;
    
    public BBMPlatformUser(Presence presence) {
        _presence = presence;
    }
    
    public Presence getPresence() {
        return _presence;
    }
    
    public Object getField(String name) throws Exception {
        // Fields
        if(name.equals(BBMPlatformUser.FIELD_DISPLAY_PIC)) {
            return Util.bitmapToBase64Str(_presence.getDisplayPicture());
        } else if(name.equals(BBMPlatformUser.FIELD_DISPLAY_NAME)) {
            return _presence.getDisplayName();
        } else if(name.equals(BBMPlatformUser.FIELD_PERSONAL_MSG)) {
            return _presence.getPersonalMessage();
        } else if(name.equals(BBMPlatformUser.FIELD_STATUS)) {
            return BBMPlatformUser.statusToString(_presence.getStatus());
        } else if(name.equals(BBMPlatformUser.FIELD_STATUS_MSG)) {
            return _presence.getStatusMessage();
        } else if(name.equals(BBMPlatformUser.FIELD_APP_VERSION)) {
            return _presence.getAppVersion();
        } else if(name.equals(BBMPlatformUser.FIELD_BBM_SDK_VERSION)) {
            return new Integer(_presence.getBBMSDKVersion());
        } else if(name.equals(BBMPlatformUser.FIELD_TYPE)) {
            return "BBM";
        } else if(name.equals(BBMPlatformUser.FIELD_HANDLE)) {
            return BBMPlatformUser.getHandle(_presence);
        } else if(name.equals(BBMPlatformUser.FIELD_PPID)) {
            return BBMPlatformUser.getPPID(_presence);
        } else {
            return super.getField(name);
        }
    }
    
    /**
     * Converts a status int to its string counterpart.
     * @param status The status int.
     * @return the status string.
     */
    protected static String statusToString(int status) {
        switch(status) {
            case Presence.STATUS_AVAILABLE: return BBMPlatformUser.STATUS_STR_AVAILABLE;
            case Presence.STATUS_BUSY:      return BBMPlatformUser.STATUS_STR_BUSY;
            default:
                throw new IllegalArgumentException("status " + status + " is unknown");
        }
    }
    
    /**
     * Converts a status string to its int counterpart.
     * @param status The status string.
     * @return the status int.
     */
    protected static int statusToInt(String status) {
        if(status.equals(BBMPlatformUser.STATUS_STR_AVAILABLE)) {
            return Presence.STATUS_AVAILABLE;
        } else if(status.equals(BBMPlatformUser.STATUS_STR_BUSY)) {
            return Presence.STATUS_BUSY;
        } else
            throw new IllegalArgumentException("status string " + status + " is unknown");
    }
    
    /**
     * Returns the handle of a presence.
     * @param presence The presence.
     * @return the handle of a presence.
     */
    public static String getHandle(Presence presence) {
        final String handle;
        if(presence instanceof BBMPlatformContact) {
            final BBMPlatformContact contact = (BBMPlatformContact) presence;
            handle = contact.getHandle();
        } else if(presence instanceof UserProfile) {
            final UserProfile currentUser = (UserProfile) presence;
            handle = currentUser.getHandle();
        } else {
            handle = null;
        }
        return handle;
    }
    
    /**
     * Returns the PPID of a presence.
     * @param presence The presence.
     * @return the PPID of a presence.
     */
    public static String getPPID(Presence presence) {
        final String ppid;
        if(presence instanceof BBMPlatformContact) {
            final BBMPlatformContact contact = (BBMPlatformContact) presence;
            ppid = contact.getPPID();
        } else if(presence instanceof UserProfile) {
            final UserProfile currentUser = (UserProfile) presence;
            ppid = currentUser.getPPID();
        } else {
            ppid = null;
        }
        return ppid;
    }
}

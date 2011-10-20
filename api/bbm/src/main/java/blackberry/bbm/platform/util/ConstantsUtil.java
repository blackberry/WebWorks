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
package blackberry.bbm.platform.util;

import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnectionListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSessionListener;
import net.rim.blackberry.api.bbm.platform.io.IOErrorCode;

public class ConstantsUtil {
	
    public static String accessCodeToString(int accessCode) {
        switch(accessCode) {
            case BBMPlatformContext.ACCESS_ALLOWED:                         return "allowed";
            case BBMPlatformContext.ACCESS_BLOCKED_BY_USER:                 return "user";
            case BBMPlatformContext.ACCESS_BLOCKED_BY_RIM:                  return "rim";
            case BBMPlatformContext.ACCESS_BLOCKED_RESET_REQUIRED:          return "resetrequired";
            case BBMPlatformContext.ACCESS_NO_DATA_COVERAGE:                return "nodata";
            case BBMPlatformContext.ACCESS_TEMPORARY_ERROR:                 return "temperror";
            case BBMPlatformContext.ACCESS_REGISTER_WITH_UI_APPLICATION:    return "nonuiapp";
            default: throw new IllegalArgumentException("Access status " + accessCode + " is unknown");
        }
    }
    
	public static String fileTransferReasonToString(int reason) {
        switch(reason) {
            case IOErrorCode.FILE_TRANSFER_FILE_NOT_FOUND: 	    return "filenotfound";
            case IOErrorCode.FILE_TRANSFER_FILE_SIZE_EXCEEDED:  return "filetoolarge";
            case IOErrorCode.FILE_TRANSFER_EMPTY_FILE:          return "fileempty";
            case IOErrorCode.FILE_TRANSFER_FILE_FORWARD_LOCKED: return "fileforwardlocked";
            case IOErrorCode.FILE_TRANSFER_BAD_FILE_TYPE:	    return "filebadtype";
            case IOErrorCode.FILE_TRANSFER_BAD_CONTACT:			return "noncontact";
            case IOErrorCode.FILE_TRANSFER_USER_CANCELED:		return "usercanceled";
            default: throw new IllegalArgumentException("Unknown file transfer failed reason: " + reason);
        }
	}

    public static String getContactJoinedTypeStr(int type) {
        switch(type) {
            case BBMPlatformConnectionListener.CONTACT_INVITED_BY_ME: return "invitedbyme";
            case BBMPlatformConnectionListener.CONTACT_INVITING_ME:   return "acceptedbyme";
            case BBMPlatformSessionListener.CONTACT_INVITED_BY_OTHER: return "invitedbyother";
            case BBMPlatformSessionListener.CONTACT_JOINED_BEFORE_ME: return "joinedbeforeme";
            default: return null;
        }
    }

    public static String requestStatusToString(int status) {
        switch(status) {
            case BBMPlatformJoinRequest.REQUEST_STATUS_PENDING: return  "pending";
            case BBMPlatformJoinRequest.REQUEST_STATUS_CANCELED: return "canceled";
            case BBMPlatformJoinRequest.REQUEST_STATUS_ACCEPTED: return "accepted";
            case BBMPlatformJoinRequest.REQUEST_STATUS_DECLINED: return "declined";
            default: throw new IllegalArgumentException("Unknown request status: " + status);
        }
    }

    public static String requestReasonToString(int reason) {
        switch(reason) {
            case IOErrorCode.JOIN_REQUEST_DECLINED_BY_HOST:                     return "hostdeclined";
            case IOErrorCode.JOIN_REQUEST_DECLINED_INVALID_HOST_PPID:           return "hostppidinvalid";
            case IOErrorCode.JOIN_REQUEST_DECLINED_APPLICATION_NOT_RUNNING:     return "appnotrunning";
            case IOErrorCode.JOIN_REQUEST_DECLINED_PUBLIC_CONNECTION_IS_FULL:   return "connectionfull";
            case IOErrorCode.JOIN_REQUEST_DECLINED_PUBLIC_CONNECTION_NOT_FOUND: return "connectionnotfound";
            case IOErrorCode.JOIN_REQUEST_CANCELED_BY_REQUESTER:                return "peercanceled";
            case IOErrorCode.JOIN_REQUEST_CANCELED_REQUESTER_LEFT:              return "peerleft";
            default: throw new IllegalArgumentException("Unknown request reason: " + reason);
        }
    }

    public static String appEnvToString(int appEnvironment) {
        switch(appEnvironment) {
            case BBMPlatformContext.APP_ENVIRONMENT_APPWORLD: return "appworld";
            case BBMPlatformContext.APP_ENVIRONMENT_TEST:     return "test";
            default: throw new IllegalArgumentException("Unknown app environment: " + appEnvironment);
        }
    }
}

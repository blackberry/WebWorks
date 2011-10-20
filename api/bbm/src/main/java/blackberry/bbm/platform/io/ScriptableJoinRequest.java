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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformJoinRequest;
import net.rim.device.api.script.Scriptable;
import blackberry.bbm.platform.util.ConstantsUtil;

public class ScriptableJoinRequest extends Scriptable {
    
    public static final String FIELD_ID =     "id";
    public static final String FIELD_STATUS = "status";
    
    protected final BBMPlatformJoinRequest _request;
    
    public ScriptableJoinRequest(BBMPlatformJoinRequest request) {
        _request = request;
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FIELD_ID)) {
            return new Integer(_request.getRequestId());
        } else if(name.equals(FIELD_STATUS)) {
            return ConstantsUtil.requestStatusToString(_request.getStatus());
        } else {
            return UNDEFINED;
        }
    }
}
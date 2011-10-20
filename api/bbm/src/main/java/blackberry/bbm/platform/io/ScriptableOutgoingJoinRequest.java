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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformOutgoingJoinRequest;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class ScriptableOutgoingJoinRequest extends ScriptableJoinRequest {

    private static final String FIELD_HOST =  "host";
    private static final String FUNC_CANCEL = "cancel";
    
    public final ScriptableFunction _acceptedCallback;
    public final ScriptableFunction _declinedCallback;
    
    public ScriptableOutgoingJoinRequest(BBMPlatformOutgoingJoinRequest request, ScriptableFunction acceptedCallback, ScriptableFunction declinedCallback) {
        super(request);
        _acceptedCallback = acceptedCallback;
        _declinedCallback = declinedCallback;
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FIELD_ID) || name.equals(FIELD_STATUS)) {
            return super.getField(name);
        } else if(name.equals(FIELD_HOST)) {
            return new BBMPlatformUser(getRequest().getHost());             
        } else if(name.equals(FUNC_CANCEL)) {
            return new CancelFunction();
        } else {
            return UNDEFINED;
        }
    }
    
    private BBMPlatformOutgoingJoinRequest getRequest() {
        return (BBMPlatformOutgoingJoinRequest) _request;
    }
    
    private class CancelFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            BBMPlatformOutgoingJoinRequest request = getRequest();
            request.cancel();
            IONamespace.getInstance().removeOutgoingJoinReq(request);
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // CancelFunction
}
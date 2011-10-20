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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class ScriptableIncomingJoinRequest extends ScriptableJoinRequest {

    private static final String FIELD_COOKIE =    "cookie";
    private static final String FIELD_PEER =      "peer";
    private static final String FUNC_ACCEPT =     "accept";
    private static final String FUNC_DECLINE =    "decline";
    
    public ScriptableIncomingJoinRequest(BBMPlatformIncomingJoinRequest request) {
        super(request);
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FIELD_ID) || name.equals(FIELD_STATUS)) {
            return super.getField(name);
        } else if(name.equals(FIELD_COOKIE)) {
            return getRequest().getCookie();             
        } else if(name.equals(FIELD_PEER)) {
            return new BBMPlatformUser(getRequest().getRequester());
        } else if(name.equals(FUNC_ACCEPT)) {
            return new AcceptFunction();
        } else if(name.equals(FUNC_DECLINE)) {
            return new DeclineFunction();
        } else {
            return UNDEFINED;
        }
    }
    
    private BBMPlatformIncomingJoinRequest getRequest() {
        return (BBMPlatformIncomingJoinRequest) _request;
    }
    
    private class AcceptFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            String cookie;
            try {
                cookie = (String) args[0];
            } catch(Exception e) {
                cookie = null;
            }
            getRequest().accept(cookie);
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(String.class, false);
            return new FunctionSignature[] {
                sig1,
            };
        }
    } // AcceptFunction
    
    private class DeclineFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            getRequest().decline();
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // DeclineFunction
}
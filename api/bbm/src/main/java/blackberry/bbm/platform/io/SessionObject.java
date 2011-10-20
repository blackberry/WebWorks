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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSession;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class SessionObject extends ConnectionObject {
    
    public static final String EVENT_ON_BROADCAST_DATA = "onbroadcastdata";
    public static final String EVENT_ON_USERS_REMOVED =  "onusersremoved";
    public static final String EVENT_ON_ENDED =          "onended";
    
    public static final String FUNC_BROADCAST =          "broadcast";
    public static final String FUNC_END =                "end";
    public static final String FUNC_LEAVE =              "leave";
    
    private final BBMPlatformSession _session;

    public SessionObject(BBMPlatformSession session, boolean isDelivered) {
        super(session, isDelivered);
        _session = session;
        _wFields.addField(EVENT_ON_BROADCAST_DATA);
        _wFields.addField(EVENT_ON_USERS_REMOVED);
        _wFields.addField(EVENT_ON_ENDED);
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FUNC_BROADCAST)) {
            return new BroadcastFunction();
        } else if(name.equals(FUNC_END)) {
            return new EndFunction();
        }  else if(name.equals(FUNC_LEAVE)) {
            return new LeaveFunction();
        } else {
            return super.getField(name);
        }
    }
    
    private class BroadcastFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Object data = args[0];
            
            _session.broadcastData(new BBMPlatformData(data.toString()));
            
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Object.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // BroadcastFunction
    
    private class EndFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            _session.end();
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // EndFunction

    private class LeaveFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            _session.leave();
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // LeaveFunction
    
}

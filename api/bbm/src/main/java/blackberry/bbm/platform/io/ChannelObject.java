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

import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannel;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class ChannelObject extends ConnectionObject {
    
    public static final String FUNC_LEAVE = "leave";

    public ChannelObject(BBMPlatformChannel channel, boolean isDelivered) {
        super(channel, isDelivered);
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FUNC_LEAVE)) {
            return new LeaveFunction();
        } else {
            return super.getField(name);
        }
    }
    
    private class LeaveFunction extends ScriptableFunctionBase {
        protected Object execute(Object thiz, Object[] args) throws Exception {
            ((BBMPlatformChannel) ChannelObject.this._connection).destroy();
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    } // LeaveFunction
}

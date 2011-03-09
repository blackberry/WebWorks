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
package blackberry.message;

import net.rim.device.api.script.Scriptable;

/**
 * Parent class for SMS namespace.
 *  
 * @author dmeng
 *
 */
public class MessageNamespace extends Scriptable {
    protected static Receiver _receiver;
    private static final String FIELD_RECEIVING = "isListeningForMessage";

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField(String name) throws Exception {
        if (name.equals(FIELD_RECEIVING)) {
            return new Boolean(_receiver.isRunning());
        }

        if (name.equals(ReceiveListenerRegistry.ADDER)) {
            return _receiver.addReceiveListener();
        }

        if (name.equals(ReceiveListenerRegistry.REMOVER)) {
            return _receiver.removeReceiveListener();
        }
        return super.getField(name);
    }

    /**
     * @see net.rim.device.api.script.Scriptable#putField(String, Object)
     */
    public boolean putField(String field, Object value) throws Exception {
        if (FIELD_RECEIVING.equals(field)) {
            boolean letRun = ((Boolean) value).booleanValue();
            _receiver.signal(letRun);
            return true;
        }
        return super.putField(field, value);
    }
}
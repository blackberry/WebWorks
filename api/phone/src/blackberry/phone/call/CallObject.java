/*
* Copyright 2010 Research In Motion Limited.
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
package blackberry.phone.call;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

public class CallObject extends Scriptable {

    private final String FIELD_CALL_OUTGOING = "outgoing";
    private final String FIELD_RECIPIENT_NAME = "recipientName";
    private final String FIELD_RECIPIENT_NUMBER = "recipientNumber";

    // Fields to be set in constructor.
    private final int _callid;
    private final boolean _outgoing;
    private final String _name;
    private final String _number;

    public CallObject(final int callid, final boolean outgoing, final String name, final String number) {
        this._callid = callid;
        this._outgoing = outgoing;
        this._name = name;
        this._number = number;
    }

    /* @Override */
    public Object getField(final String name) throws Exception {
        if (name.equals(FIELD_CALL_OUTGOING)) {
            return new Boolean(_outgoing);
        }
        else if (name.equals(FIELD_RECIPIENT_NAME)) {
            return _name;
        }
        else if (name.equals(FIELD_RECIPIENT_NUMBER)) {
            return _number;
        }
        else if (name.equals(IsOnHoldFunction.NAME)) {
            return new IsOnHoldFunction();
        }

        return super.getField(name);
    }

    private int getCallID() {
        return _callid;
    }

    // / <summary>
    // / Returns true if this call currently on hold.
    // / </summary>
    public class IsOnHoldFunction extends ScriptableFunction {
        public static final String NAME = "isOnHold";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {
            final PhoneCall phoneCall = Phone.getCall(getCallID());
            int callStatus = PhoneCall.STATUS_DISCONNECTED;

            if (phoneCall != null) {
                callStatus = phoneCall.getStatus();
                return new Boolean(callStatus == PhoneCall.STATUS_HELD ? true : false);
            }

            return UNDEFINED;
        }
    }

}

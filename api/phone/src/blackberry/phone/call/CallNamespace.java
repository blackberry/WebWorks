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
package blackberry.phone.call;

import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

/**
 * Implementation of blackberry.phone name space
 */
public final class CallNamespace extends Scriptable {

    public static final String NAME = "blackberry.phone.Phone";

    private Hashtable _activeCalls;

    private static CallNamespace _instance = null;

    public static synchronized CallNamespace getInstance() {
        if (_instance == null) {
            _instance = new CallNamespace();
            _instance.initialize();
        }

        return _instance;
    }

    private CallNamespace() {
    }

    private void initialize() {
        _instance._activeCalls = new Hashtable();
        // Registering for a PhoneListener to queue and return the array of
        // active calls when required.
        Phone.addPhoneListener(ConcretePhoneListener.getInstance(_instance));
    }

    /* @Override */
    public Object getField(final String name) throws Exception {
        if (name.equals(InActiveCallFunction.NAME)) {
            return new InActiveCallFunction();
        }
        else if (name.equals(ActiveCallsFunction.NAME)) {
            return new ActiveCallsFunction();
        }
        else if (name.equals(ConcretePhoneListener.NAME)) {
            return ConcretePhoneListener.getInstance(this);
        }

        // PhoneListener types
        else if (name.equals("CB_CALL_INITIATED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_INITIATED);
        }
        else if (name.equals("CB_CALL_WAITING")) {
            return new Integer(ConcretePhoneListener.CB_CALL_WAITING);
        }
        else if (name.equals("CB_CALL_INCOMING")) {
            return new Integer(ConcretePhoneListener.CB_CALL_INCOMING);
        }
        else if (name.equals("CB_CALL_ANSWERED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_ANSWERED);
        }
        else if (name.equals("CB_CALL_CONNECTED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_CONNECTED);
        }
        else if (name.equals("CB_CALL_CONFERENCECALL_ESTABLISHED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_CONFERENCECALL_ESTABLISHED);
        }
        else if (name.equals("CB_CONFERENCECALL_DISCONNECTED")) {
            return new Integer(ConcretePhoneListener.CB_CONFERENCECALL_DISCONNECTED);
        }
        else if (name.equals("CB_CALL_DISCONNECTED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_DISCONNECTED);
        }
        else if (name.equals("CB_CALL_DIRECTCONNECT_CONNECTED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_DIRECTCONNECT_CONNECTED);
        }
        else if (name.equals("CB_CALL_DIRECTCONNECT_DISCONNECTED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_DIRECTCONNECT_DISCONNECTED);
        }
        else if (name.equals("CB_CALL_ENDED_BYUSER")) {
            return new Integer(ConcretePhoneListener.CB_CALL_ENDED_BYUSER);
        }
        else if (name.equals("CB_CALL_FAILED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_FAILED);
        }
        else if (name.equals("CB_CALL_RESUMED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_RESUMED);
        }
        else if (name.equals("CB_CALL_HELD")) {
            return new Integer(ConcretePhoneListener.CB_CALL_HELD);
        }
        else if (name.equals("CB_CALL_ADDED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_ADDED);
        }
        else if (name.equals("CB_CALL_REMOVED")) {
            return new Integer(ConcretePhoneListener.CB_CALL_REMOVED);
        }
        else if (name.equals("CALL_ERROR_SUBSCRIBER_BUSY")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_SUBSCRIBER_BUSY);
        }
        else if (name.equals("CALL_ERROR_CONGESTION")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_CONGESTION);
        }
        else if (name.equals("CALL_ERROR_RADIO_PATH_UNAVAILABLE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_RADIO_PATH_UNAVAILABLE);
        }
        else if (name.equals("CALL_ERROR_NUMBER_UNOBTAINABLE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_NUMBER_UNOBTAINABLE);
        }
        else if (name.equals("CALL_ERROR_AUTHORIZATION_FAILURE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_AUTHORIZATION_FAILURE);
        }
        else if (name.equals("CALL_ERROR_EMERGENCY_CALLS_ONLY")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_EMERGENCY_CALLS_ONLY);
        }
        else if (name.equals("CALL_ERROR_HOLD_ERROR")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_HOLD_ERROR);
        }
        else if (name.equals("CALL_ERROR_OUTGOING_CALLS_BARRED")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_OUTGOING_CALLS_BARRED);
        }
        else if (name.equals("CALL_ERROR_GENERAL")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_GENERAL);
        }
        else if (name.equals("CALL_ERROR_MAINTENANCE_REQUIRED")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_MAINTENANCE_REQUIRED);
        }
        else if (name.equals("CALL_ERROR_SERVICE_NOT_AVAILABLE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_SERVICE_NOT_AVAILABLE);
        }
        else if (name.equals("CALL_ERROR_DUE_TO_FADING")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_DUE_TO_FADING);
        }
        else if (name.equals("CALL_ERROR_LOST_DUE_TO_FADING")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_LOST_DUE_TO_FADING);
        }
        else if (name.equals("CALL_ERROR_TRY_AGAIN")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_TRY_AGAIN);
        }
        else if (name.equals("CALL_ERROR_FDN_MISMATCH")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_FDN_MISMATCH);
        }
        else if (name.equals("CALL_ERROR_CONNECTION_DENIED_BY_NETWORK")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_CONNECTION_DENIED_BY_NETWORK);
        }
        else if (name.equals("CALL_ERROR_NUMBER_NOT_IN_SERVICE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_NUMBER_NOT_IN_SERVICE);
        }
        else if (name.equals("CALL_ERROR_PLEASE_TRY_LATER")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_PLEASE_TRY_LATER);
        }
        else if (name.equals("CALL_ERROR_SERVICE_CONFLICT")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_SERVICE_CONFLICT);
        }
        else if (name.equals("CALL_ERROR_SYSTEM_BUSY_TRY_LATER")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_SYSTEM_BUSY_TRY_LATER);
        }
        else if (name.equals("CALL_ERROR_USER_BUSY_IN_PRIVATE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_BUSY_IN_PRIVATE);
        }
        else if (name.equals("CALL_ERROR_USER_BUSY_IN_DATA")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_BUSY_IN_DATA);
        }
        else if (name.equals("CALL_ERROR_USER_NOT_AUTHORIZED")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_NOT_AUTHORIZED);
        }
        else if (name.equals("CALL_ERROR_USER_NOT_AVAILABLE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_NOT_AVAILABLE);
        }
        else if (name.equals("CALL_ERROR_USER_UNKNOWN")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_UNKNOWN);
        }
        else if (name.equals("CALL_ERROR_USER_NOT_REACHABLE")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_USER_NOT_REACHABLE);
        }
        else if (name.equals("CALL_ERROR_INCOMING_CALL_BARRED")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_INCOMING_CALL_BARRED);
        }
        else if (name.equals("CALL_ERROR_CALL_REPLACED_BY_STK")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_CALL_REPLACED_BY_STK);
        }
        else if (name.equals("CALL_ERROR_STK_CALL_NOT_ALLOWED")) {
            return new Integer(ConcretePhoneListener.CALL_ERROR_STK_CALL_NOT_ALLOWED);
        }

        return super.getField(name);
    }

    // / <summary>
    // / Returns true when an active call.
    // / ControlledAccessException - thrown if the user is not granted
    // / (i.e doesn't have PERMISSION_PHONE permission)to use the phone
    // / features.
    // / </summary>
    public static class InActiveCallFunction extends ScriptableFunction {
        public static final String NAME = "inActiveCall";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {
            final PhoneCall phoneCall = Phone.getActiveCall();

            if (phoneCall != null) {
                return new Boolean(true);
            }

            return new Boolean(false);
        }
    }

    // / <summary>
    // / Returns an array of current active calls.
    // / </summary>
    public class ActiveCallsFunction extends ScriptableFunction {
        public static final String NAME = "activeCalls";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {
            PhoneCall activeCall = null;

            // If active calls hash is empty and there is an active call
            // probably it happen when
            // the app wasn't running so add this call to the hash
            if (_activeCalls.isEmpty() && (activeCall = Phone.getActiveCall()) != null) {
                addActiveCall(activeCall.getCallId());
            }

            final Object[] activeCalls = new Object[_activeCalls.size()];
            final Enumeration e = _activeCalls.elements();

            for (int i = 0; e.hasMoreElements(); i++) {
                activeCalls[i] = e.nextElement();
            }

            return activeCalls;
        }
    }

    public void addActiveCall(final int callid) {
        final PhoneCall phoneCall = Phone.getCall(callid);
        if (phoneCall != null) {
            final CallObject call = new CallObject(callid, phoneCall.isOutgoing(), phoneCall.getDisplayPhoneNumber(), phoneCall.getPhoneNumber());
            _activeCalls.put(new Integer(callid), call);
        }
    }

    public void removeActiveCall(final int callid) {
        _activeCalls.remove(new Integer(callid));
    }

}

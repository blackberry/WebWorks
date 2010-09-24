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

import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;

public class ConcretePhoneListener extends ScriptableFunction implements PhoneListener {
    public static final String NAME = "addPhoneListener";

    private final UiApplication _uiAppliation = UiApplication.getUiApplication();

    private CallNamespace _owner = null;
    private final ScriptableFunction[] _callbacks = new ScriptableFunction[CB_PHONELISTENERS_SIZE];
    private final int NO_FAILURE = -1;
    private int _lastCallIdPassedToCallInitiated;

    public static final int CB_CALL_INITIATED = 0;
    public static final int CB_CALL_WAITING = 1;
    public static final int CB_CALL_INCOMING = 2;
    public static final int CB_CALL_ANSWERED = 3;
    public static final int CB_CALL_CONNECTED = 4;
    public static final int CB_CALL_CONFERENCECALL_ESTABLISHED = 5;
    public static final int CB_CONFERENCECALL_DISCONNECTED = 6;
    public static final int CB_CALL_DISCONNECTED = 7;
    public static final int CB_CALL_DIRECTCONNECT_CONNECTED = 8;
    public static final int CB_CALL_DIRECTCONNECT_DISCONNECTED = 9;
    public static final int CB_CALL_ENDED_BYUSER = 10;
    public static final int CB_CALL_FAILED = 11;
    public static final int CB_CALL_RESUMED = 12;
    public static final int CB_CALL_HELD = 13;
    public static final int CB_CALL_ADDED = 14;
    public static final int CB_CALL_REMOVED = 15;
    private static final int CB_PHONELISTENERS_SIZE = 16;

    public static final int CALL_ERROR_SUBSCRIBER_BUSY = PhoneListener.CALL_ERROR_SUBSCRIBER_BUSY;
    public static final int CALL_ERROR_CONGESTION = PhoneListener.CALL_ERROR_CONGESTION;
    public static final int CALL_ERROR_RADIO_PATH_UNAVAILABLE = PhoneListener.CALL_ERROR_RADIO_PATH_UNAVAILABLE;
    public static final int CALL_ERROR_NUMBER_UNOBTAINABLE = PhoneListener.CALL_ERROR_NUMBER_UNOBTAINABLE;
    public static final int CALL_ERROR_AUTHORIZATION_FAILURE = PhoneListener.CALL_ERROR_AUTHORIZATION_FAILURE;
    public static final int CALL_ERROR_EMERGENCY_CALLS_ONLY = PhoneListener.CALL_ERROR_EMERGENCY_CALLS_ONLY;
    public static final int CALL_ERROR_HOLD_ERROR = PhoneListener.CALL_ERROR_HOLD_ERROR;
    public static final int CALL_ERROR_OUTGOING_CALLS_BARRED = PhoneListener.CALL_ERROR_OUTGOING_CALLS_BARRED;
    public static final int CALL_ERROR_GENERAL = PhoneListener.CALL_ERROR_GENERAL;
    public static final int CALL_ERROR_MAINTENANCE_REQUIRED = PhoneListener.CALL_ERROR_MAINTENANCE_REQUIRED;
    public static final int CALL_ERROR_SERVICE_NOT_AVAILABLE = PhoneListener.CALL_ERROR_SERVICE_NOT_AVAILABLE;
    public static final int CALL_ERROR_DUE_TO_FADING = PhoneListener.CALL_ERROR_DUE_TO_FADING;
    public static final int CALL_ERROR_LOST_DUE_TO_FADING = PhoneListener.CALL_ERROR_LOST_DUE_TO_FADING;
    public static final int CALL_ERROR_TRY_AGAIN = PhoneListener.CALL_ERROR_TRY_AGAIN;
    public static final int CALL_ERROR_FDN_MISMATCH = PhoneListener.CALL_ERROR_FDN_MISMATCH;
    public static final int CALL_ERROR_CONNECTION_DENIED_BY_NETWORK = PhoneListener.CALL_ERROR_CONNECTION_DENIED_BY_NETWORK;
    public static final int CALL_ERROR_NUMBER_NOT_IN_SERVICE = PhoneListener.CALL_ERROR_NUMBER_NOT_IN_SERVICE;
    public static final int CALL_ERROR_PLEASE_TRY_LATER = PhoneListener.CALL_ERROR_PLEASE_TRY_LATER;
    public static final int CALL_ERROR_SERVICE_CONFLICT = PhoneListener.CALL_ERROR_SERVICE_CONFLICT;
    public static final int CALL_ERROR_SYSTEM_BUSY_TRY_LATER = PhoneListener.CALL_ERROR_SYSTEM_BUSY_TRY_LATER;
    public static final int CALL_ERROR_USER_BUSY_IN_PRIVATE = PhoneListener.CALL_ERROR_USER_BUSY_IN_PRIVATE;
    public static final int CALL_ERROR_USER_BUSY_IN_DATA = PhoneListener.CALL_ERROR_USER_BUSY_IN_DATA;
    public static final int CALL_ERROR_USER_NOT_AUTHORIZED = PhoneListener.CALL_ERROR_USER_NOT_AUTHORIZED;
    public static final int CALL_ERROR_USER_NOT_AVAILABLE = PhoneListener.CALL_ERROR_USER_NOT_AVAILABLE;
    public static final int CALL_ERROR_USER_UNKNOWN = PhoneListener.CALL_ERROR_USER_UNKNOWN;
    public static final int CALL_ERROR_USER_NOT_REACHABLE = PhoneListener.CALL_ERROR_USER_NOT_REACHABLE;
    public static final int CALL_ERROR_INCOMING_CALL_BARRED = PhoneListener.CALL_ERROR_INCOMING_CALL_BARRED;
    public static final int CALL_ERROR_CALL_REPLACED_BY_STK = PhoneListener.CALL_ERROR_CALL_REPLACED_BY_STK;
    public static final int CALL_ERROR_STK_CALL_NOT_ALLOWED = PhoneListener.CALL_ERROR_STK_CALL_NOT_ALLOWED;

    private static ConcretePhoneListener _instance = null;

    public static synchronized ConcretePhoneListener getInstance(final CallNamespace owner) {
        if (_instance == null) {
            _instance = new ConcretePhoneListener(owner);
        }

        return _instance;
    }

    private ConcretePhoneListener(final CallNamespace owner) {
        this._owner = owner;

        resetLastInitiatedCallID();
    }

    private int getLastInitiatedCallID() {
        return _lastCallIdPassedToCallInitiated;
    }

    private void setLastInitiatedCallID(final int callid) {
        _lastCallIdPassedToCallInitiated = callid;
    }

    private void resetLastInitiatedCallID() {
        _lastCallIdPassedToCallInitiated = -1;
    }

    /* override */
    public Object invoke(final Object thiz, final Object[] args) throws Exception {
        final int PARAMS_MAX = 2;

        // null is passed to remove all listeners.
        if (args == null || args.length == 1 && args[0] == null) {
            removeAllListeners();

            return new Boolean(true);
        }
        else if (args.length != PARAMS_MAX) {
            return UNDEFINED;
        }
        else {
            final ScriptableFunction sf = (ScriptableFunction) args[0];
            final int index = ((Integer) args[1]).intValue();

            if (isCBIndexValid(index)) {
                setListenerByIndex(sf, index);

                return new Boolean(true);
            }
        }

        return UNDEFINED;
    }

    private boolean isCBIndexValid(final int index) {
        if (index >= 0 && index < CB_PHONELISTENERS_SIZE) {
            return true;
        }

        return false;
    }

    public ScriptableFunction getListenerByIndex(final int index) {
        if (isCBIndexValid(index)) {
            return _callbacks[index];
        }

        return null;
    }

    public void setListenerByIndex(final ScriptableFunction sfCB, final int index) {
        if (isCBIndexValid(index)) {
            _callbacks[index] = sfCB;
        }
    }

    public void removeAllListeners() {
        for (int i = 0; i < CB_PHONELISTENERS_SIZE; i++) {
            setListenerByIndex(null, i);
        }
    }

    public int getNumberOfRegisteredCallbacks() {
        int cbNumber = 0;

        for (int i = 0; i < CB_PHONELISTENERS_SIZE; i++) {
            if (_callbacks[i] != null) {
                cbNumber++;
            }
        }

        return cbNumber;
    }

    /* overriding area */
    // //////////////////////////////////////////////////////////////////////////////
    // Invoked when a call gets added to a conference call
    public void callAdded(final int callid) {
        InvokeScriptableCallback(CB_CALL_ADDED, callid);
    }

    // Invoked when the user answers a call (user driven).
    public void callAnswered(final int callid) {
        _owner.addActiveCall(callid);

        InvokeScriptableCallback(CB_CALL_ANSWERED, callid);
    }

    // Invoked when a conference call has been established.
    public void callConferenceCallEstablished(final int callid) {
        InvokeScriptableCallback(CB_CALL_CONFERENCECALL_ESTABLISHED, callid);
    }

    // Invoked when the network indicates a connected event (network driven).
    public void callConnected(final int callid) {
        InvokeScriptableCallback(CB_CALL_CONNECTED, callid);
    }

    // Invoked when a direct-connect call is connected.
    public void callDirectConnectConnected(final int callid) {
        InvokeScriptableCallback(CB_CALL_DIRECTCONNECT_CONNECTED, callid);
    }

    // Invoked when a direct-connect call is disconnected.
    public void callDirectConnectDisconnected(final int callid) {
        InvokeScriptableCallback(CB_CALL_DIRECTCONNECT_DISCONNECTED, callid);
    }

    // Invoked when a call is disconnected.
    public void callDisconnected(final int callid) {
        _owner.removeActiveCall(callid);

        InvokeScriptableCallback(CB_CALL_DISCONNECTED, callid);
    }

    // Invoked when the user ends the call.
    public void callEndedByUser(final int callid) {
        InvokeScriptableCallback(CB_CALL_ENDED_BYUSER, callid);
    }

    // Invoked when a call fails.
    public void callFailed(final int callid, final int reason) {
        InvokeScriptableCallback(CB_CALL_FAILED, callid, reason);
    }

    // Invoked when a call goes into the 'held' state.
    public void callHeld(final int callid) {
        InvokeScriptableCallback(CB_CALL_HELD, callid);
    }

    // Invoked when a new call is arriving.
    public void callIncoming(final int callid) {
        InvokeScriptableCallback(CB_CALL_INCOMING, callid);
    }

    // Invoked when a call has been initiated by the device (outbound).
    // Known Issue: This method called twice, timestamp is applied to skip the second call.
    public void callInitiated(final int callid) {
        if (getLastInitiatedCallID() == callid) {
            resetLastInitiatedCallID();
            return;
        }

        setLastInitiatedCallID(callid);

        _owner.addActiveCall(callid);

        InvokeScriptableCallback(CB_CALL_INITIATED, callid);
    }

    // Invoked when a call gets removed from a conference call.
    public void callRemoved(final int callid) {
        InvokeScriptableCallback(CB_CALL_REMOVED, callid);
    }

    // Invoked when a call goes from 'held' to 'resumed' state.
    public void callResumed(final int callid) {
        InvokeScriptableCallback(CB_CALL_RESUMED, callid);
    }

    // Invoked when a call is waiting.
    public void callWaiting(final int callid) {
        _owner.addActiveCall(callid);

        InvokeScriptableCallback(CB_CALL_WAITING, callid);
    }

    // Invoked when a conference call is terminated (all members disconnected).
    public void conferenceCallDisconnected(final int callid) {
        InvokeScriptableCallback(CB_CONFERENCECALL_DISCONNECTED, callid);
    }

    private void InvokeScriptableCallback(final int callbackIndex, final int callid) {
        InvokeScriptableCallback(callbackIndex, callid, NO_FAILURE);
    }

    private void InvokeScriptableCallback(final int callbackIndex, final int callid, final int reason) {
        final ScriptableFunction callback = getListenerByIndex(callbackIndex);

        if (callback != null) {
            _uiAppliation.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    final Integer callidObj = new Integer(callid);
                                    callback.invoke(null, (reason == NO_FAILURE ? new Object[] { callidObj } : new Object[] { callidObj,
                                            new Integer(reason) }));
                                } catch (final Exception e) {
                                }
                            }
                        }).start();
                    } catch (final Exception e) {
                    }
                }
            });
        }
    }
}

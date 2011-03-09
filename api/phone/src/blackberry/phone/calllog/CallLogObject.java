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
package blackberry.phone.calllog;

import java.util.Date;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.device.api.script.Scriptable;

/**
 * LogEntry describes an entry in the call log
 * 
 * @author stu
 */
public class CallLogObject extends Scriptable {

    public static final String FIELD_CALL_DATE = "date";
    public static final String FIELD_CALL_DURATION = "duration";
    public static final String FIELD_CALL_NOTES = "notes";
    public static final String FIELD_CALL_STATUS = "status";
    public static final String FIELD_CALL_TYPE = "type";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ORIGINAL_NUMBER = "number";
    public static final String FIELD_ADDRESS_BOOK_NUMBER = "addressBookNumber";
    public static final String FIELD_ADDRESS_BOOK_TYPE = "addressBookType";

    // Call status
    public static final int STATUS_NORMAL = CallLog.STATUS_NORMAL; // Normal call status (no errors).
    public static final int STATUS_BUSY = CallLog.STATUS_BUSY; // Busy call status.
    public static final int STATUS_CONGESTION = CallLog.STATUS_CONGESTION; // Error due to congestion.
    public static final int STATUS_PATH_UNAVAILABLE = CallLog.STATUS_PATH_UNAVAILABLE; // Error due to path unavailability.
    public static final int STATUS_NUMBER_UNOBTAINABLE = CallLog.STATUS_NUMBER_UNOBTAINABLE; // Error due to number unobtainability.
    public static final int STATUS_AUTHENTICATION_FAILURE = CallLog.STATUS_AUTHENTICATION_FAILURE; // Error due to call authorization failure.
    public static final int STATUS_EMERGENCY_CALLS_ONLY = CallLog.STATUS_EMERGENCY_CALLS_ONLY; // Emergency calls only.
    public static final int STATUS_HOLD_ERROR = CallLog.STATUS_HOLD_ERROR; // Call hold error.
    public static final int STATUS_OUTGOING_CALLS_BARRED = CallLog.STATUS_OUTGOING_CALLS_BARRED; // Outgoing calls barred.
    public static final int STATUS_GENERAL_ERROR = CallLog.STATUS_GENERAL_ERROR; // General error occured.
    public static final int STATUS_MAINTENANCE_REQUIRED = CallLog.STATUS_MAINTENANCE_REQUIRED; // Maintenance required.
    public static final int STATUS_SERVICE_NOT_AVAILABLE = CallLog.STATUS_SERVICE_NOT_AVAILABLE; // Service not available.
    public static final int STATUS_CALL_FAIL_DUE_TO_FADING = CallLog.STATUS_CALL_FAIL_DUE_TO_FADING; // Call failed due to fading.
    public static final int STATUS_CALL_LOST_DUE_TO_FADING = CallLog.STATUS_CALL_LOST_DUE_TO_FADING; // Call lost due to fading.
    public static final int STATUS_CALL_FAILED_TRY_AGAIN = CallLog.STATUS_CALL_FAILED_TRY_AGAIN; // Call failed due to fading.
    public static final int STATUS_FDN_MISMATCH = CallLog.STATUS_FDN_MISMATCH; // An FDN mismatch occured.
    public static final int STATUS_CONNECTION_DENIED = CallLog.STATUS_CONNECTION_DENIED; // Call connection was denied.
    public static final int STATUS_INCOMING_CALL_BARRED = CallLog.STATUS_INCOMING_CALL_BARRED; // Incoming calls are barred.

    // call type of PhoneCallLog (ConferencePhoneCallLog has it's own final type)
    public static final int TYPE_RECEIVED_CALL = PhoneCallLog.TYPE_RECEIVED_CALL;
    public static final int TYPE_PLACED_CALL = PhoneCallLog.TYPE_PLACED_CALL;
    public static final int TYPE_MISSED_CALL_UNOPENED = PhoneCallLog.TYPE_MISSED_CALL_UNOPENED;
    public static final int TYPE_MISSED_CALL_OPENED = PhoneCallLog.TYPE_MISSED_CALL_OPENED;

    // member variables
    /**
     * Date of the call.
     * 
     * @category Hidden
     */
    protected Date _callDate;

    /**
     * Duration of call (in seconds).
     * 
     * @category Hidden
     */
    protected int _callDuration;

    /**
     * Status of call (from STATUS_* data members).
     * 
     * @category Hidden
     */
    protected int _callStatus;

    /**
     * Call log notes.
     * 
     * @category Hidden
     */
    protected String _callNotes;

    /**
     * Type of call. PhoneCallLog unlike ConferencePhoneCallLog has a type.
     * 
     * @category Hidden
     */
    protected int _callType;

    /**
     * name associated with the caller id.
     * 
     * @category Hidden
     */
    protected String _name;

    /**
     * Original number for this caller ID..
     * 
     * @category Hidden
     */
    protected String _number;

    /**
     * Contact list phone number for this caller ID..
     * 
     * @category Hidden
     */
    protected String _addressBookNumber;

    /**
     * Phone type, home, work etc., default is Phone
     * 
     * @category Hidden
     */
    protected String _addressBookType;

    public CallLogObject(final CallLog callLog) {
        _callDate = callLog.getDate();
        _callDuration = callLog.getDuration();
        _callNotes = callLog.getNotes();
        _callStatus = callLog.getStatus();

        // Process more fields only if the callLog is instance of PhoneCallLog class (it might be ConferencePhoneCallLog).
        if (callLog.getClass() == PhoneCallLog.class) {
            final PhoneCallLog phoneCallLog = (PhoneCallLog) callLog;
            _callType = phoneCallLog.getType();
            final PhoneCallLogID logID = phoneCallLog.getParticipant();
            if (logID != null) {
                _name = logID.getName();
                _number = logID.getNumber();
                _addressBookNumber = logID.getAddressBookFormattedNumber();
                _addressBookType = logID.getType();
            }
        }
    }

    /* override */
    public Object getField(final String name) throws Exception {
        if (name.equals(FIELD_CALL_DATE)) {
            return _callDate;
        }
        else if (name.equals(FIELD_CALL_DURATION)) {
            return new Integer(_callDuration);
        }
        else if (name.equals(FIELD_CALL_NOTES)) {
            return _callNotes;
        }
        else if (name.equals(FIELD_CALL_STATUS)) {
            return new Integer(_callStatus);
        }
        else if (name.equals(FIELD_CALL_TYPE)) {
            return new Integer(_callType);
        }
        else if (name.equals(FIELD_NAME)) {
            return _name;
        }
        else if (name.equals(FIELD_ORIGINAL_NUMBER)) {
            return _number;
        }
        else if (name.equals(FIELD_ADDRESS_BOOK_NUMBER)) {
            return _addressBookNumber;
        }
        else if (name.equals(FIELD_ADDRESS_BOOK_TYPE)) {
            return _addressBookType;
        }
        else if (name.equals("STATUS_NORMAL")) {
            return new Integer(STATUS_NORMAL);
        }
        else if (name.equals("STATUS_BUSY")) {
            return new Integer(STATUS_BUSY);
        }
        else if (name.equals("STATUS_CONGESTION")) {
            return new Integer(STATUS_CONGESTION);
        }
        else if (name.equals("STATUS_PATH_UNAVAILABLE")) {
            return new Integer(STATUS_PATH_UNAVAILABLE);
        }
        else if (name.equals("STATUS_NUMBER_UNOBTAINABLE")) {
            return new Integer(STATUS_NUMBER_UNOBTAINABLE);
        }
        else if (name.equals("STATUS_AUTHENTICATION_FAILURE")) {
            return new Integer(STATUS_AUTHENTICATION_FAILURE);
        }
        else if (name.equals("STATUS_EMERGENCY_CALLS_ONLY")) {
            return new Integer(STATUS_EMERGENCY_CALLS_ONLY);
        }
        else if (name.equals("STATUS_HOLD_ERROR")) {
            return new Integer(STATUS_HOLD_ERROR);
        }
        else if (name.equals("STATUS_OUTGOING_CALLS_BARRED")) {
            return new Integer(STATUS_OUTGOING_CALLS_BARRED);
        }
        else if (name.equals("STATUS_GENERAL_ERROR")) {
            return new Integer(STATUS_GENERAL_ERROR);
        }
        else if (name.equals("STATUS_MAINTENANCE_REQUIRED")) {
            return new Integer(STATUS_MAINTENANCE_REQUIRED);
        }
        else if (name.equals("STATUS_SERVICE_NOT_AVAILABLE")) {
            return new Integer(STATUS_SERVICE_NOT_AVAILABLE);
        }
        else if (name.equals("STATUS_CALL_FAIL_DUE_TO_FADING")) {
            return new Integer(STATUS_CALL_FAIL_DUE_TO_FADING);
        }
        else if (name.equals("STATUS_CALL_LOST_DUE_TO_FADING")) {
            return new Integer(STATUS_CALL_LOST_DUE_TO_FADING);
        }
        else if (name.equals("STATUS_CALL_FAILED_TRY_AGAIN")) {
            return new Integer(STATUS_CALL_FAILED_TRY_AGAIN);
        }
        else if (name.equals("STATUS_FDN_MISMATCH")) {
            return new Integer(STATUS_FDN_MISMATCH);
        }
        else if (name.equals("STATUS_CONNECTION_DENIED")) {
            return new Integer(STATUS_CONNECTION_DENIED);
        }
        else if (name.equals("STATUS_INCOMING_CALL_BARRED")) {
            return new Integer(STATUS_INCOMING_CALL_BARRED);
        }
        else if (name.equals("TYPE_RECEIVED_CALL")) {
            return new Integer(TYPE_RECEIVED_CALL);
        }
        else if (name.equals("TYPE_PLACED_CALL")) {
            return new Integer(TYPE_PLACED_CALL);
        }
        else if (name.equals("TYPE_MISSED_CALL_UNOPENED")) {
            return new Integer(TYPE_MISSED_CALL_UNOPENED);
        }
        else if (name.equals("TYPE_MISSED_CALL_OPENED")) {
            return new Integer(TYPE_MISSED_CALL_OPENED);
        }

        return super.getField(name);
    }
}

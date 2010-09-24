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
package blackberry.phone.calllog;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

public class CallLogNamespace extends Scriptable {

    public static final String NAME = "blackberry.phone.PhoneLogs";

    static final int FOLDER_MISSED_CALLS = 0;
    private static final int FOLDER_NORMAL_CALLS = 1;

    private static final String LABEL_FOLDER_MISSED_CALLS = "FOLDER_MISSED_CALLS";
    private static final String LABEL_FOLDER_NORMAL_CALLS = "FOLDER_NORMAL_CALLS";

    private static CallLogNamespace _instance = null;

    public static CallLogNamespace getInstance() {
        if (_instance == null) {
            _instance = new CallLogNamespace();
        }

        return _instance;
    }

    private CallLogNamespace() {
    }

    private static PhoneLogs phoneLogs = PhoneLogs.getInstance();

    /* @Override */
    public Object getField(final String name) throws Exception {
        if (name.equals(NumberOfCallsFunction.NAME)) {
            return new NumberOfCallsFunction();
        }
        else if (name.equals(DeleteCallAtFunction.NAME)) {
            return new DeleteCallAtFunction();
        }
        else if (name.equals(CallAtFunction.NAME)) {
            return new CallAtFunction();
        }
        else if (name.equals(ConcretePhoneLogListener.NAME)) {
            return ConcretePhoneLogListener.getInstance();
        }
        else if (name.equals(LABEL_FOLDER_MISSED_CALLS)) {
            return new Integer(FOLDER_MISSED_CALLS);
        }
        else if (name.equals(LABEL_FOLDER_NORMAL_CALLS)) {
            return new Integer(FOLDER_NORMAL_CALLS);
        }
        else if (name.equals(CallLogFindFunction.NAME)) {
            return new CallLogFindFunction();
        }

        return super.getField(name);
    }

    // Retrieves the number of calls in a given call log folder.
    public class CallAtFunction extends ScriptableFunction {
        public static final String NAME = "callAt";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {
            if (args != null) {
                final int len = args.length;
                if (len == 2) {
                    final int index = ((Integer) args[0]).intValue();
                    final CallLog callLog = callLogAt(index, ((Integer) args[1]).intValue());

                    if (callLog != null) {
                        return new CallLogObject(callLog);
                    }
                }
            }

            return UNDEFINED;
        }
    }

    // Retrieves the number of calls in a given call log folder.
    public class NumberOfCallsFunction extends ScriptableFunction {
        public static final String NAME = "numberOfCalls";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {

            if (args != null) {
                final int len = args.length;
                if (len == 1) {
                    return new Integer(numberOfLogsInFolder(((Integer) args[0]).intValue()));
                }
            }

            return UNDEFINED;
        }
    }

    // Deletes a call from the log in a given call log folder.
    public class DeleteCallAtFunction extends ScriptableFunction {
        public static final String NAME = "deleteCallAt";

        /* override */
        public Object invoke(final Object thiz, final Object[] args) throws Exception {

            if (args != null) {
                final int len = args.length;
                if (len == 2) {
                    final int index = ((Integer) args[0]).intValue();
                    final long folderID = mapIntegerFolderIdToLong(((Integer) args[1]).intValue());
                    phoneLogs.deleteCall(index, folderID);

                    return new Boolean(true);
                }
            }

            return UNDEFINED;
        }
    }

    static CallLog callLogAt(final int index, final int folderID) {
        final long mappedFolderID = mapIntegerFolderIdToLong(folderID);

        return phoneLogs.callAt(index, mappedFolderID);
    }

    static int numberOfLogsInFolder(final int folderID) {
        final long mappedFolderID = mapIntegerFolderIdToLong(folderID);

        return phoneLogs.numberOfCalls(mappedFolderID);
    }

    private static long mapIntegerFolderIdToLong(final int intFolderID) {
        switch (intFolderID) {
        case FOLDER_MISSED_CALLS:
            return PhoneLogs.FOLDER_MISSED_CALLS;
        case FOLDER_NORMAL_CALLS:
            return PhoneLogs.FOLDER_NORMAL_CALLS;
        default:
            return -1;
        }
    }
}

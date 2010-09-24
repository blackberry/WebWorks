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

import java.util.Vector;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.phone.find.FindNamespace;
import blackberry.phone.find.TestableScriptableObject;

public class CallLogFindFunction extends ScriptableFunction {
    static final String NAME = "find";

    private final int INDEX_TESTABLE = 0;
    private final int INDEX_FOLDER_ID = 1;
    private final int INDEX_ORDER_BY = 2;
    private final int INDEX_MAX_RETURN = 3;
    private final int INDEX_IS_ASCENDING = 4;

    /* override */
    public Object invoke(final Object thiz, final Object[] args) throws Exception {
        CallLogObject[] logsFound = new CallLogObject[0];

        TestableScriptableObject testable = null;
        int folderID = CallLogNamespace.FOLDER_MISSED_CALLS;
        String orderByField = "";
        int maxReturn = -1;
        boolean isAscending = true;

        if (!FindNamespace.isValidFindArguments(args)) {
            return logsFound;
        }

        if (args.length > INDEX_TESTABLE) {
            testable = (TestableScriptableObject) args[INDEX_TESTABLE];
        }

        if (args.length > INDEX_FOLDER_ID) {
            if (args[INDEX_FOLDER_ID] != null) {
                final Integer i = (Integer) args[INDEX_FOLDER_ID];
                folderID = i.intValue();
            }
        }

        if (args.length > INDEX_ORDER_BY) {
            if (args[INDEX_ORDER_BY] != null) {
                orderByField = (String) args[INDEX_ORDER_BY];
            }
        }

        if (args.length > INDEX_MAX_RETURN) {
            if (args[INDEX_MAX_RETURN] != null) {
                final Integer i = (Integer) args[INDEX_MAX_RETURN];
                maxReturn = i.intValue();
            }
        }

        if (args.length > INDEX_IS_ASCENDING) {
            if (args[INDEX_IS_ASCENDING] != null) {
                final Boolean b = (Boolean) args[INDEX_IS_ASCENDING];
                isAscending = b.booleanValue();
            }
        }

        final boolean isSorted = orderByField != null && orderByField.length() > 0 ? true : false;
        final int numberOfCalls = CallLogNamespace.numberOfLogsInFolder(folderID);
        final Vector found = new Vector();
        int iElement = 0;

        for (int i = 0; i < numberOfCalls; i++) {
            final CallLog l = CallLogNamespace.callLogAt(i, folderID);
            final CallLogObject log = new CallLogObject(l);
            if (testable != null) {
                if (testable.test(log)) {
                    FindNamespace.insertElementByOrder(found, log, orderByField, isAscending);
                    iElement++;
                }
            }
            else {
                FindNamespace.insertElementByOrder(found, log, orderByField, isAscending);
                iElement++;
            }

            if (!isSorted && iElement == maxReturn) {
                break;
            }
        }

        int size = found.size();
        if (maxReturn > 0 && size > maxReturn) {
            size = maxReturn;
        }
        logsFound = new CallLogObject[size];
        for (int i = 0; i < size; i++) {
            logsFound[i] = (CallLogObject) found.elementAt(i);
        }

        return logsFound;
    }

}

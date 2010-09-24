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
package blackberry.phone.find;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import common.util.StringTokenizer;

import net.rim.device.api.script.Scriptable;

/**
 *
 */
public class FindNamespace extends Scriptable {

    public static final String NAME = "blackberry.phone.Find";

    private static FindNamespace _instance = null;

    public static FindNamespace getInstance() {
        if (_instance == null) {
            _instance = new FindNamespace();
        }

        return _instance;
    }

    private FindNamespace() {
    }

    /* override */
    public Object getField(final String name) throws Exception {
        if (name.equals(FilterExpressionConstructor.NAME)) {
            return new FilterExpressionConstructor();
        }

        return super.getField(name);
    }

    public static final Object getScriptField(final Scriptable object, final String fieldName, final StringBuffer optionalProperty) {
        // clear optionalField
        optionalProperty.setLength(0);

        // Parse whether leftField contains subfield or attribute
        Scriptable o = object;
        String optional = "";
        String key = fieldName;
        if (key.indexOf('.') >= 0) {
            final StringTokenizer st = new StringTokenizer(key, '.');
            while (true) {
                key = st.nextToken();
                if (!st.hasMoreTokens()) {
                    break;
                }

                Object field;
                try {
                    field = o.getField(key);
                } catch (final Exception e) {
                    field = null;
                }

                if (field == null) {
                    return null;
                }

                if (field instanceof Scriptable) {
                    o = (Scriptable) field;
                    if (o == null) {
                        return null;
                    }
                }
                else if (field instanceof Date) {
                    optional = st.nextToken();
                    break;
                }
            }
        }

        Object fieldToTest;
        try {
            fieldToTest = o.getField(key);
        } catch (final Exception e) {
            fieldToTest = null;
        }

        optionalProperty.append(optional);

        return fieldToTest;
    }

    public static final boolean isValidFindArguments(final Object[] args) {
        int argsIndex = 0;

        // filterExpression object
        if (args.length > argsIndex) {
            if (args[argsIndex] != null && !(args[argsIndex] instanceof TestableScriptableObject)) {
                return false;
            }
        }
        argsIndex++;

        // folderID number
        if (args.length > argsIndex) {
            if (args[argsIndex] != null && !(args[argsIndex] instanceof Integer)) {
                return false;
            }
        }
        argsIndex++;

        // orderBy string
        if (args.length > argsIndex) {
            if (args[argsIndex] != null && !(args[argsIndex] instanceof String)) {
                return false;
            }
        }
        argsIndex++;

        // maxReturn number
        if (args.length > argsIndex) {
            if (args[argsIndex] != null && !(args[argsIndex] instanceof Integer)) {
                return false;
            }
        }
        argsIndex++;

        // ASC or DESC
        if (args.length > argsIndex) {
            if (args[argsIndex] != null && !(args[argsIndex] instanceof Boolean)) {
                return false;
            }
        }
        argsIndex++;

        return true;
    }

    public static final Object getScriptFieldValue(final Scriptable object, final String fieldName) throws Exception {
        if (object == null || fieldName == null || fieldName.length() == 0) {
            return null;
        }

        final StringBuffer optionalProperty = new StringBuffer();
        final Object field = FindNamespace.getScriptField(object, fieldName, optionalProperty);
        final String fieldOptional = optionalProperty.toString();

        if (field == null) {
            return null;
        }

        // find the right position to insert the new object based on the value
        // of the orderByField
        Object value = field;
        if (fieldOptional.length() > 0 && field instanceof Date && value != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime((Date) value);

            int dateField;
            if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_YEAR)) {
                dateField = cal.get(Calendar.YEAR);
            }
            else if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_MONTH)) {
                dateField = cal.get(Calendar.MONTH) + 1; // Month starts from 0
            }
            else if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_DAY)) {
                dateField = cal.get(Calendar.DAY_OF_MONTH);
            }
            else if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_HOUR)) {
                dateField = cal.get(Calendar.HOUR_OF_DAY);
            }
            else if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_MINUTE)) {
                dateField = cal.get(Calendar.MINUTE);
            }
            else if (fieldOptional.equals(FilterExpressionConstructor.DATE_FIELD_SECOND)) {
                dateField = cal.get(Calendar.SECOND);
            }
            else {
                dateField = -1;
            }

            value = new Integer(dateField);
        }

        return value;
    }

    // -1: orderByValue is after valueInVector
    // 0: orderByValue is equal to valueInVector
    // 1: orderByValue is before valueInVector
    private static final int compareObjects(final Object valueInVector, final Object orderByValue, final boolean isAscending) {
        int result1;
        int result2;

        if (isAscending) {
            result1 = -1;
            result2 = 1;
        }
        else { // Descening
            result1 = 1;
            result2 = -1;
        }

        if (valueInVector == null) {
            return result2;
        }

        if (valueInVector instanceof Integer && orderByValue instanceof Integer) {
            final int v = ((Integer) valueInVector).intValue();
            final int o = ((Integer) orderByValue).intValue();
            if (v < o) {
                return result1;
            }
            else if (v == o) {
                return 0;
            }
            else {
                return result2;
            }
        }
        else if (valueInVector instanceof Double && orderByValue instanceof Double) {
            final double v = ((Double) valueInVector).doubleValue();
            final double o = ((Double) orderByValue).doubleValue();
            if (v < o) {
                return result1;
            }
            else if (v == o) {
                return 0;
            }
            else {
                return result2;
            }
        }
        else if (valueInVector instanceof Date && orderByValue instanceof Date) {
            final long v = ((Date) valueInVector).getTime();
            final long o = ((Date) orderByValue).getTime();
            if (v < o) {
                return result1;
            }
            else if (v == o) {
                return 0;
            }
            else {
                return result2;
            }
        }
        else if (valueInVector instanceof Boolean && orderByValue instanceof Boolean) {
            if (valueInVector == Boolean.FALSE && orderByValue == Boolean.FALSE) {
                return 0;
            }
            else if (valueInVector == Boolean.TRUE && orderByValue == Boolean.TRUE) {
                return 0;
            }
            else if (valueInVector == Boolean.FALSE && orderByValue == Boolean.TRUE) {
                return result1;
            }
            else {
                return result2;
            }
        }
        else if (valueInVector instanceof String && orderByValue instanceof String) {
            final int result = ((String) valueInVector).compareTo((String) orderByValue);
            if (result < 0) {
                return result1;
            }
            else if (result == 0) {
                return 0;
            }
            else {
                return result2;
            }
        }

        return result1;
    }

    private static final void insertAtPosition(final Vector vector, final Scriptable object, final int index) {
        vector.insertElementAt(object, index);
    }

    private static final void insertAfterPosition(final Vector vector, final Scriptable object, final int index) {
        if (index < vector.size() - 1) {
            vector.insertElementAt(object, index + 1);
        }
        else {
            vector.addElement(object);
        }
    }

    public static final boolean insertElementByOrder(final Vector vector, final Scriptable object, final String orderByField,
            final boolean isAscending) throws Exception {
        final boolean ret = true;
        if (orderByField != null && orderByField.length() > 0) {
            final int size = vector.size();
            int start = 0;
            int end = size - 1;

            if (size == 0) {
                vector.addElement(object);
                return ret;
            }

            final Object orderByValue = getScriptFieldValue(object, orderByField);

            if (orderByValue == null) {
                vector.addElement(object);
                return ret;
            }

            Scriptable objectInVector;
            Object valueInVector;

            while (end - start >= 2) {
                final int insertPosition = start + (end - start) / 2;

                objectInVector = (Scriptable) vector.elementAt(insertPosition);
                valueInVector = getScriptFieldValue(objectInVector, orderByField);

                // if compareObjects returns true, insert the object in current
                // insertPosition
                final int result = compareObjects(valueInVector, orderByValue, isAscending);
                if (result < 0) {
                    // orderByValue is after the value of current vector item
                    start = insertPosition;
                }
                else if (result == 0) {
                    // orderByValue is equal to the value of current vector item
                    insertAtPosition(vector, object, insertPosition);
                    return ret;
                }
                else {
                    // orderByValue is before the value of current vector item
                    end = insertPosition;
                }
            }

            // end - start < 2 : end == start or end == start + 1
            objectInVector = (Scriptable) vector.elementAt(start);
            valueInVector = getScriptFieldValue(objectInVector, orderByField);

            if (compareObjects(valueInVector, orderByValue, isAscending) >= 0) {
                // orderByValue is equal to or before the value of start vector
                // item
                insertAtPosition(vector, object, start);
            }
            else {
                if (end != start) {
                    objectInVector = (Scriptable) vector.elementAt(end);
                    valueInVector = getScriptFieldValue(objectInVector, orderByField);
                    if (compareObjects(valueInVector, orderByValue, isAscending) >= 0) {
                        // orderByValue is equal to or before the value of end
                        // vector item
                        insertAtPosition(vector, object, end);
                    }
                    else {
                        // orderByValue is after the value of end vector item
                        insertAfterPosition(vector, object, end);
                    }
                }
                else { // if end == start and already compared by the start
                    // position
                    insertAfterPosition(vector, object, start);
                }
            }
        }
        else {
            vector.addElement(object);
        }

        return ret;
    }
}

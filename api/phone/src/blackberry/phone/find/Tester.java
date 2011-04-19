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
package blackberry.phone.find;

import java.util.Calendar;
import java.util.Date;

public class Tester {

    public static boolean test(final Object fieldToTest, final Object objective, final int operator, final String optional) {
        if (fieldToTest instanceof Integer) {
            return testByInteger((Integer) fieldToTest, (Integer) objective, operator);
        }
        else if (fieldToTest instanceof Boolean) {
            if (objective instanceof Boolean) {
                return testByBoolean((Boolean) fieldToTest, (Boolean) objective, operator);
            }
            else {
                return false;
            }
        }
        else if (fieldToTest instanceof String) {
            return testByString((String) fieldToTest, (String) objective, operator);
        }
        else if (fieldToTest instanceof Date) {
            return testByDate((Date) fieldToTest, objective, operator, optional);
        }
        else {
            // should not happen
            return false;
        }
    }

    private static boolean testByString(final String value, final String compare, final int operator) {

        switch (operator) {
        case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
            return !(value.compareTo(compare) == 0);
        case FilterExpressionConstructor.OPERATOR_EQUAL:
            return value.compareTo(compare) == 0;
        case FilterExpressionConstructor.OPERATOR_LESS_THAN:
            return value.compareTo(compare) < 0;
        case FilterExpressionConstructor.OPERATOR_GREATER_THAN:
            return value.compareTo(compare) > 0;
        case FilterExpressionConstructor.OPERATOR_LESS_OR_EQUAL:
            return value.compareTo(compare) <= 0;
        case FilterExpressionConstructor.OPERATOR_GREATER_OR_EQUAL:
            return value.compareTo(compare) >= 0;
        }

        return false;
    }

    private static boolean testByInteger(final Integer value, final Integer objective, final int operator) {

        switch (operator) {
        case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
            return objective.intValue() != value.intValue();
        case FilterExpressionConstructor.OPERATOR_EQUAL:
            return objective.intValue() == value.intValue();
        case FilterExpressionConstructor.OPERATOR_LESS_THAN:
            return value.intValue() < objective.intValue();
        case FilterExpressionConstructor.OPERATOR_GREATER_THAN:
            return value.intValue() > objective.intValue();
        case FilterExpressionConstructor.OPERATOR_LESS_OR_EQUAL:
            return value.intValue() <= objective.intValue();
        case FilterExpressionConstructor.OPERATOR_GREATER_OR_EQUAL:
            return value.intValue() >= objective.intValue();
        }

        return false;
    }

    private static boolean testByBoolean( final Boolean value, final Boolean objective,
            final int operator ) {
        
        boolean result;

        switch (operator) {
        case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
            result = !objective.equals(value);
            break;
        case FilterExpressionConstructor.OPERATOR_EQUAL:
            result = objective.equals(value);
            break;
        default:
            result = false;
        }

        return result;
    }

    private static boolean testByDate(final Date value, final Object objective, final int operator, final String optional) {

        if (value == null) {
            return false;
        }

        if (optional != null && optional.length() > 0) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(value);
            int i;

            if (optional.equals(FilterExpressionConstructor.DATE_FIELD_YEAR)) {
                i = cal.get(Calendar.YEAR);
            }
            else if (optional.equals(FilterExpressionConstructor.DATE_FIELD_MONTH)) {
                i = cal.get(Calendar.MONTH) + 1; // Month starts from 0
            }
            else if (optional.equals(FilterExpressionConstructor.DATE_FIELD_DAY)) {
                i = cal.get(Calendar.DAY_OF_MONTH);
            }
            else if (optional.equals(FilterExpressionConstructor.DATE_FIELD_HOUR)) {
                i = cal.get(Calendar.HOUR_OF_DAY);
            }
            else if (optional.equals(FilterExpressionConstructor.DATE_FIELD_MINUTE)) {
                i = cal.get(Calendar.MINUTE);
            }
            else if (optional.equals(FilterExpressionConstructor.DATE_FIELD_SECOND)) {
                i = cal.get(Calendar.SECOND);
            }
            else {
                // unsupported Date field
                return false;
            }

            if (objective instanceof String) {
                final String compare = (String) objective;
                final String v = new Integer(i).toString();
                switch (operator) {
                case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
                    return !(v.compareTo(compare) == 0);
                case FilterExpressionConstructor.OPERATOR_EQUAL:
                    return v.compareTo(compare) == 0;
                case FilterExpressionConstructor.OPERATOR_LESS_THAN:
                    return v.compareTo(compare) < 0;
                case FilterExpressionConstructor.OPERATOR_GREATER_THAN:
                    return v.compareTo(compare) > 0;
                case FilterExpressionConstructor.OPERATOR_LESS_OR_EQUAL:
                    return v.compareTo(compare) <= 0;
                case FilterExpressionConstructor.OPERATOR_GREATER_OR_EQUAL:
                    return v.compareTo(compare) >= 0;
                }

            }
            else if (objective instanceof Integer) {
                final int v = i;
                final int o = ((Integer) objective).intValue();
                switch (operator) {
                case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
                    return v != o;
                case FilterExpressionConstructor.OPERATOR_EQUAL:
                    return v == o;
                case FilterExpressionConstructor.OPERATOR_LESS_THAN:
                    return v < o;
                case FilterExpressionConstructor.OPERATOR_GREATER_THAN:
                    return v > o;
                case FilterExpressionConstructor.OPERATOR_LESS_OR_EQUAL:
                    return v <= o;
                case FilterExpressionConstructor.OPERATOR_GREATER_OR_EQUAL:
                    return v >= o;
                }
            }
            else {
                return false;
            }
        }
        else {
            if (objective instanceof Date) {
                final long v = value.getTime();
                final Date d = (Date) objective;
                final long o = d.getTime();
                switch (operator) {
                case FilterExpressionConstructor.OPERATOR_NOT_EQUAL:
                    return o != v;
                case FilterExpressionConstructor.OPERATOR_EQUAL:
                    return o == v;
                case FilterExpressionConstructor.OPERATOR_LESS_THAN:
                    return v < o;
                case FilterExpressionConstructor.OPERATOR_GREATER_THAN:
                    return v > o;
                case FilterExpressionConstructor.OPERATOR_LESS_OR_EQUAL:
                    return v <= o;
                case FilterExpressionConstructor.OPERATOR_GREATER_OR_EQUAL:
                    return v >= o;
                }
            }
            else {
                return false;
            }
        }

        return false;
    }

}

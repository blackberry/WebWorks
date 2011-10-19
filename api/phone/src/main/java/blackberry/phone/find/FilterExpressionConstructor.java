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

import net.rim.device.api.script.ScriptableFunction;

public class FilterExpressionConstructor extends ScriptableFunction {
    public static final String NAME = "FilterExpression";

    private final static String OPERATOR_NOT_EQUAL_STRING = "!=";
    private final static String OPERATOR_EQUAL_STRING = "==";
    private final static String OPERATOR_LESS_THAN_STRING = "<";
    private final static String OPERATOR_GREATER_THAN_STRING = ">";
    private final static String OPERATOR_LESS_OR_EQUAL_STRING = "<=";
    private final static String OPERATOR_GREATER_OR_EQUAL_STRING = ">=";
    private final static String OPERATOR_LOGICAL_AND_STRING = "AND";
    private final static String OPERATOR_LOGICAL_OR_STRING = "OR";
    private final static String OPERATOR_CONTAINS_STRING = "CONTAINS";

    private final static int OPERATOR_INVALID = -1;
    final static int OPERATOR_NOT_EQUAL = 0;
    final static int OPERATOR_EQUAL = 1;
    final static int OPERATOR_LESS_THAN = 2;
    final static int OPERATOR_LESS_OR_EQUAL = OPERATOR_EQUAL | OPERATOR_LESS_THAN; // 3
    final static int OPERATOR_GREATER_THAN = 4;
    final static int OPERATOR_GREATER_OR_EQUAL = OPERATOR_EQUAL | OPERATOR_GREATER_THAN; // 5
    final static int OPERATOR_LOGICAL_AND = 8;
    final static int OPERATOR_LOGICAL_OR = 16;
    final static int OPERATOR_CONTAINS = 32;

    final static String DATE_FIELD_YEAR = "Year";
    final static String DATE_FIELD_MONTH = "Month";
    final static String DATE_FIELD_DAY = "Day";
    final static String DATE_FIELD_HOUR = "Hour";
    final static String DATE_FIELD_MINUTE = "Minute";
    final static String DATE_FIELD_SECOND = "Second";

    /* @Override */
    public Object construct(final Object thiz, final Object[] args) throws Exception {
        Object leftField = null;
        Object rightField = null;
        int operator = OPERATOR_INVALID;
        boolean negateExpression = false;

        // additional validation
        validateArgs(args);

        // Populate "operator"
        if (args[1] instanceof Integer) {
            operator = ((Integer) args[1]).intValue();
        }
        else if (args[1] instanceof String) {
            operator = isValidOperator((String) args[1]);
        }

        // Populate "leftField" and "rightField"
        if (args[0] instanceof String) {
            if (operator >= OPERATOR_NOT_EQUAL && operator < OPERATOR_LOGICAL_AND || operator == OPERATOR_CONTAINS) {
                leftField = args[0];
                rightField = args[2];
            }
            else {
                throw new IllegalArgumentException("Invalid types for operator: leftField, rightField");
            }
        }
        else if (args[0] instanceof FilterExpressionObject) {
            if (operator >= OPERATOR_LOGICAL_AND && operator <= OPERATOR_LOGICAL_OR && args[2] instanceof FilterExpressionObject) {
                leftField = args[0];
                rightField = args[2];
            }
            else {
                throw new IllegalArgumentException("Invalid types for operator: leftField, rightField");
            }
        }

        // Populate the optional "negateExpression"
        if (args.length == 4) {
            final Boolean b = (Boolean) args[3];
            negateExpression = b.booleanValue();
        }

        return new FilterExpressionObject(leftField, operator, rightField, negateExpression);
    }

    private void validateArgs(final Object[] args) {
        // validate 'operator'
        if (!(args[1] instanceof Integer || args[1] instanceof String)) {
            throw new IllegalArgumentException("Invalid type - " + args[1].getClass().toString());
        }

        if (args[1] instanceof Integer && !isValidOperator((Integer) args[1])) {
            throw new IllegalArgumentException("'operator' is invalid");
        }
        else if (args[1] instanceof String && isValidOperator((String) args[1]) == OPERATOR_INVALID) {
            throw new IllegalArgumentException("'operator' is invalid");
        }

        // validate 'leftField'
        if (!(args[0] instanceof String || args[0] instanceof FilterExpressionObject)) {
            throw new IllegalArgumentException("Invalid type - " + args[0].getClass().toString());
        }

        // validate 'rightField'
        if (args[0] instanceof FilterExpressionObject && !(args[2] instanceof FilterExpressionObject)) {
            throw new IllegalArgumentException("Invalid type - " + args[2].getClass().toString());
        }
    }

    private boolean isValidOperator(final Integer op) {
        final int i = op.intValue();
        return !(i != OPERATOR_NOT_EQUAL && i != OPERATOR_EQUAL && i != OPERATOR_LESS_THAN && i != OPERATOR_GREATER_THAN
                && i != OPERATOR_LESS_OR_EQUAL && i != OPERATOR_GREATER_OR_EQUAL && i != OPERATOR_LOGICAL_AND && i != OPERATOR_LOGICAL_OR && i != OPERATOR_CONTAINS);
    }

    private int isValidOperator(final String op) {
        int operator;
        if (op.equals(OPERATOR_NOT_EQUAL_STRING)) {
            operator = OPERATOR_NOT_EQUAL;
        }
        else if (op.equals(OPERATOR_EQUAL_STRING)) {
            operator = OPERATOR_EQUAL;
        }
        else if (op.equals(OPERATOR_LESS_THAN_STRING)) {
            operator = OPERATOR_LESS_THAN;
        }
        else if (op.equals(OPERATOR_GREATER_THAN_STRING)) {
            operator = OPERATOR_GREATER_THAN;
        }
        else if (op.equals(OPERATOR_LESS_OR_EQUAL_STRING)) {
            operator = OPERATOR_LESS_OR_EQUAL;
        }
        else if (op.equals(OPERATOR_GREATER_OR_EQUAL_STRING)) {
            operator = OPERATOR_GREATER_OR_EQUAL;
        }
        else if (op.equals(OPERATOR_LOGICAL_AND_STRING)) {
            operator = OPERATOR_LOGICAL_AND;
        }
        else if (op.equals(OPERATOR_LOGICAL_OR_STRING)) {
            operator = OPERATOR_LOGICAL_OR;
        }
        else if (op.equals(OPERATOR_CONTAINS_STRING)) {
            operator = OPERATOR_CONTAINS;
        }
        else {
            operator = OPERATOR_INVALID;
        }
        return operator;
    }
}

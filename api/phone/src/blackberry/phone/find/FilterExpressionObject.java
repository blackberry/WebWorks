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

import net.rim.device.api.script.Scriptable;

public class FilterExpressionObject extends Scriptable implements TestableScriptableObject {

    public static final String FIELD_LEFTFIELD = "leftField";
    public static final String FIELD_OPERATOR = "operator";
    public static final String FIELD_RIGHTFIELD = "rightField";
    public static final String FIELD_NEGATEEXPRESSION = "negate";

    private final Object _leftField;
    private final Object _rightField;
    private final int _operator;
    private final boolean _isNegate;

    public FilterExpressionObject(final Object leftField, final int operator, final Object rightField, final boolean negateExpression) {
        this._leftField = leftField;
        this._rightField = rightField;
        this._operator = operator;
        this._isNegate = negateExpression;
    }

    /* @Override */
    public Object getField(final String name) throws Exception {
        if (name.equals(FIELD_LEFTFIELD)) {
            return getLeftField();
        }
        else if (name.equals(FIELD_OPERATOR)) {
            return new Integer(getOperator());
        }
        else if (name.equals(FIELD_RIGHTFIELD)) {
            return getRightField();
        }
        else if (name.equals(FIELD_NEGATEEXPRESSION)) {
            return new Boolean(isNegateExpression());
        }

        return super.getField(name);
    }

    public Object getLeftField() {
        return _leftField;
    }

    public Object getRightField() {
        return _rightField;
    }

    public int getOperator() {
        return _operator;
    }

    public boolean isNegateExpression() {
        return _isNegate;
    }

    /* @Override */
    public boolean test(final Scriptable object) {
        final Object leftField = getLeftField();
        final Object rightField = getRightField();

        if (leftField instanceof String) {
            final StringBuffer optionalProperty = new StringBuffer();
            final Object fieldToTest = FindNamespace.getScriptField(object, (String) leftField, optionalProperty);

            if (fieldToTest == null) {
                return isNegateExpression();
            }

            final String optional = optionalProperty.toString();
            return Tester.test(fieldToTest, rightField, getOperator(), optional) ? !isNegateExpression() : isNegateExpression();
        }
        else if (leftField instanceof FilterExpressionObject && rightField instanceof FilterExpressionObject) {
            final FilterExpressionObject feLeft = (FilterExpressionObject) leftField;
            final FilterExpressionObject feRight = (FilterExpressionObject) rightField;

            // Recursively "test" the leftField and then the rightField if
            // necessary
            final boolean leftTestResult = feLeft.test(object);

            switch (getOperator()) {
            case FilterExpressionConstructor.OPERATOR_LOGICAL_AND:
                if (!leftTestResult) {
                    return leftTestResult ? !isNegateExpression() : isNegateExpression();
                }
                else {
                    return feRight.test(object) ? !isNegateExpression() : isNegateExpression();
                }
            case FilterExpressionConstructor.OPERATOR_LOGICAL_OR:
                if (leftTestResult) {
                    return leftTestResult ? !isNegateExpression() : isNegateExpression();
                }
                else {
                    return feRight.test(object) ? !isNegateExpression() : isNegateExpression();
                }
            default:
                return isNegateExpression();
            }
        }

        return isNegateExpression();
    }
}

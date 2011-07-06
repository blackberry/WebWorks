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
package blackberry.find.filterExpression;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.find.ExpressionTester;
import blackberry.find.FindNamespace;
import blackberry.find.TestableScriptableObject;

/**
 * FilterExpressionObject represents the FilterExpression
 */
public class FilterExpressionObject extends ScriptableObjectBase implements TestableScriptableObject {

    public static final String FIELD_LEFTFIELD = "leftField";
    public static final String FIELD_OPERATOR = "operator";
    public static final String FIELD_RIGHTFIELD = "rightField";
    public static final String FIELD_NEGATEEXPRESSION = "negate";

    public final static int OPERATOR_INVALID = -1;
    public final static int OPERATOR_NOT_EQUAL = 0;
    public final static int OPERATOR_EQUAL = 1;
    public final static int OPERATOR_LESS_THAN = 2;
    public final static int OPERATOR_GREATER_THAN = 4;
    public final static int OPERATOR_REG_EXP = 8;
    public final static int OPERATOR_LESS_OR_EQUAL = OPERATOR_EQUAL | OPERATOR_LESS_THAN; // 3
    public final static int OPERATOR_GREATER_OR_EQUAL = OPERATOR_EQUAL | OPERATOR_GREATER_THAN; // 5
    public final static int OPERATOR_LOGICAL_AND = 16;
    public final static int OPERATOR_LOGICAL_OR = 32;
    public final static int OPERATOR_CONTAINS = 64;

    public final static String OPERATOR_NOT_EQUAL_STRING = "!=";
    public final static String OPERATOR_EQUAL_STRING = "==";
    public final static String OPERATOR_LESS_THAN_STRING = "<";
    public final static String OPERATOR_GREATER_THAN_STRING = ">";
    public final static String OPERATOR_REG_EXP_STRING = "REGEX";
    public final static String OPERATOR_LESS_OR_EQUAL_STRING = "<=";
    public final static String OPERATOR_GREATER_OR_EQUAL_STRING = ">=";
    public final static String OPERATOR_LOGICAL_AND_STRING = "AND";
    public final static String OPERATOR_LOGICAL_OR_STRING = "OR";
    public final static String OPERATOR_CONTAINS_STRING = "CONTAINS";

    private final Object _leftField;
    private final Object _rightField;
    private final int _operator;
    private final boolean _isNegate;

    /**
     * Construct a new FilterExpressionObject.
     * 
     * @param leftField
     *            a value to be compared or FilterExpression object to be combined.
     * @param operator
     *            the operator used for comparing or combining.
     * @param rightField
     *            a value to be compared or FilterExpression object to be combined.
     * @param negateExpression
     *            the flag that indicates whether the condition of the FilterExpression object should be evaluated negatively.
     */
    public FilterExpressionObject( final Object leftField, final int operator, final Object rightField,
            final boolean negateExpression ) {
        super();
        _leftField = leftField;
        _rightField = rightField;
        _operator = operator;
        _isNegate = negateExpression;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) {
        if( name.equals( FIELD_LEFTFIELD ) ) {
            return getLeftField();
        } else if( name.equals( FIELD_RIGHTFIELD ) ) {
            return getRightField();
        } else if( name.equals( FIELD_OPERATOR ) ) {
            return new Integer( getOperator() );
        } else if( name.equals( FIELD_NEGATEEXPRESSION ) ) {
            return new Boolean( isNegateExpression() );
        }

        return super.getField( name );
    }

    /**
     * Retrieve left field.
     * 
     * @return the value of the left field.
     */
    public Object getLeftField() {
        return _leftField;
    }

    /**
     * Retrieve right field.
     * 
     * @return the value of the right field.
     */
    public Object getRightField() {
        return _rightField;
    }

    /**
     * Retrieve operator.
     * 
     * @return the operator.
     */
    public int getOperator() {
        return _operator;
    }

    /**
     * Retrieve the negate indicator.
     * 
     * @return the boolean indicates if comparing conditions should be evaluated negatively.
     */
    public boolean isNegateExpression() {
        return _isNegate;
    }

    /**
     * @see blackberry.find.TestableScriptableObject#test(blackberry.core.ScriptableObjectBase)
     */
    public boolean test( final ScriptableObjectBase object ) {
        final Object leftField = getLeftField();
        final Object rightField = getRightField();

        if( leftField instanceof String ) {
            final StringBuffer optionalProperty = new StringBuffer();
            final ScriptField fieldToTest = FindNamespace.getScriptField( object, (String) leftField, optionalProperty );

            if( fieldToTest == null ) {
                return isNegateExpression();
            }

            final String optional = optionalProperty.toString();
            return ExpressionTester.test( fieldToTest, rightField, getOperator(), optional ) ? !isNegateExpression()
                    : isNegateExpression();
        } else if( leftField instanceof FilterExpressionObject && rightField instanceof FilterExpressionObject ) {
            final FilterExpressionObject feLeft = (FilterExpressionObject) leftField;
            final FilterExpressionObject feRight = (FilterExpressionObject) rightField;

            // Recursively "test" the leftField and then the rightField if necessary
            final boolean leftTestResult = feLeft.test( object );

            switch( getOperator() ) {
                case OPERATOR_LOGICAL_AND:
                    if( !leftTestResult ) {
                        return leftTestResult ? !isNegateExpression() : isNegateExpression();
                    } else {
                        return feRight.test( object ) ? !isNegateExpression() : isNegateExpression();
                    }
                case OPERATOR_LOGICAL_OR:
                    if( leftTestResult ) {
                        return leftTestResult ? !isNegateExpression() : isNegateExpression();
                    } else {
                        return feRight.test( object ) ? !isNegateExpression() : isNegateExpression();
                    }
                default:
                    return isNegateExpression();
            }
        }

        return isNegateExpression();
    }
}

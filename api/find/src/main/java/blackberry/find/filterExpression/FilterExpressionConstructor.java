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

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * FilterExpressionConstructor constructs the FilterExpressionObject.
 */
public class FilterExpressionConstructor extends ScriptableFunctionBase {
    public static final String NAME = "FilterExpression";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        Object leftField = null;
        Object rightField = null;
        int operator = FilterExpressionObject.OPERATOR_INVALID;
        boolean negateExpression = false;

        // Populate "operator"
        if( args[ 1 ] instanceof Integer ) {
            operator = ( (Integer) args[ 1 ] ).intValue();
        } else if( args[ 1 ] instanceof String ) {
            operator = getValidOperator( (String) args[ 1 ] );
        }

        // Populate "leftField" and "rightField"
        if( args[ 0 ] instanceof String ) {
            if( operator >= FilterExpressionObject.OPERATOR_NOT_EQUAL && operator <= FilterExpressionObject.OPERATOR_REG_EXP
                    || operator == FilterExpressionObject.OPERATOR_CONTAINS ) {
                leftField = args[ 0 ];
                rightField = args[ 2 ];
            } else {
                throw new IllegalArgumentException( "Invalid types for operator: leftField, rightField" );
            }
        } else if( args[ 0 ] instanceof FilterExpressionObject ) {
            if( operator >= FilterExpressionObject.OPERATOR_LOGICAL_AND && operator <= FilterExpressionObject.OPERATOR_LOGICAL_OR
                    && args[ 2 ] instanceof FilterExpressionObject ) {
                leftField = args[ 0 ];
                rightField = args[ 2 ];
            } else {
                throw new IllegalArgumentException( "Invalid types for operator: leftField, rightField" );
            }
        }

        // Populate the optional "negateExpression"
        if( args.length == 4 ) {
            final Boolean b = (Boolean) args[ 3 ];
            negateExpression = b.booleanValue();
        }

        return new FilterExpressionObject( leftField, operator, rightField, negateExpression );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 4 );
        fs.addParam( Object.class, true );
        fs.addParam( Object.class, true );
        fs.addNullableParam( Object.class, true );
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#validateArgs(java.lang.Object[])
     */
    protected void validateArgs( final Object[] args ) {
        super.validateArgs( args );

        // additional validation
        // validate 'operator'
        if( !( args[ 1 ] instanceof Integer || args[ 1 ] instanceof String ) ) {
            throw new IllegalArgumentException( "Invalid type - " + args[ 1 ].getClass().toString() );
        }

        if( args[ 1 ] instanceof Integer && !isValidOperator( (Integer) args[ 1 ] ) ) {
            throw new IllegalArgumentException( "'operator' is invalid" );
        } else if( args[ 1 ] instanceof String
                && getValidOperator( (String) args[ 1 ] ) == FilterExpressionObject.OPERATOR_INVALID ) {
            throw new IllegalArgumentException( "'operator' is invalid" );
        }

        // validate 'leftField'
        if( !( args[ 0 ] instanceof String || args[ 0 ] instanceof FilterExpressionObject ) ) {
            throw new IllegalArgumentException( "Invalid type - " + args[ 0 ].getClass().toString() );
        }

        // validate 'rightField'
        if( args[ 0 ] instanceof FilterExpressionObject && !( args[ 2 ] instanceof FilterExpressionObject ) ) {
            throw new IllegalArgumentException( "Invalid type - " + args[ 2 ].getClass().toString() );
        }
    }

    private boolean isValidOperator( final Integer op ) {
        final int i = op.intValue();
        return !( i != FilterExpressionObject.OPERATOR_NOT_EQUAL && i != FilterExpressionObject.OPERATOR_EQUAL
                && i != FilterExpressionObject.OPERATOR_LESS_THAN && i != FilterExpressionObject.OPERATOR_GREATER_THAN
                && i != FilterExpressionObject.OPERATOR_REG_EXP && i != FilterExpressionObject.OPERATOR_LESS_OR_EQUAL
                && i != FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL && i != FilterExpressionObject.OPERATOR_LOGICAL_AND
                && i != FilterExpressionObject.OPERATOR_LOGICAL_OR && i != FilterExpressionObject.OPERATOR_CONTAINS );
    }

    private int getValidOperator( final String op ) {
        int operator;
        if( op.equals( FilterExpressionObject.OPERATOR_NOT_EQUAL_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_NOT_EQUAL;
        } else if( op.equals( FilterExpressionObject.OPERATOR_EQUAL_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_EQUAL;
        } else if( op.equals( FilterExpressionObject.OPERATOR_LESS_THAN_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_LESS_THAN;
        } else if( op.equals( FilterExpressionObject.OPERATOR_GREATER_THAN_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_GREATER_THAN;
        } else if( op.equals( FilterExpressionObject.OPERATOR_REG_EXP_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_REG_EXP;
        } else if( op.equals( FilterExpressionObject.OPERATOR_LESS_OR_EQUAL_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_LESS_OR_EQUAL;
        } else if( op.equals( FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL;
        } else if( op.equals( FilterExpressionObject.OPERATOR_LOGICAL_AND_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_LOGICAL_AND;
        } else if( op.equals( FilterExpressionObject.OPERATOR_LOGICAL_OR_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_LOGICAL_OR;
        } else if( op.equals( FilterExpressionObject.OPERATOR_CONTAINS_STRING ) ) {
            operator = FilterExpressionObject.OPERATOR_CONTAINS;
        } else {
            operator = FilterExpressionObject.OPERATOR_INVALID;
        }
        return operator;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        // Constructor - not supported
        return UNDEFINED;
    }
}

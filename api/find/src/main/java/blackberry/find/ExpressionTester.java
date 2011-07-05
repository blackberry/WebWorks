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
package blackberry.find;

import java.util.Calendar;
import java.util.Date;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.common.util.PatternMatchingUtilities;
import blackberry.core.ScriptField;
import blackberry.find.filterExpression.FilterExpressionObject;

/**
 * Implements test method.
 */
public class ExpressionTester {
    /**
     * Perform the test on field.
     * 
     * @param fieldToTest
     *            the field to be tested.
     * @param objective
     *            the object to test against.
     * @param operator
     *            the testing operator.
     * @param optional
     *            the optional parameter (i.e. negate).
     * @return a boolean indicating whether a fieldToTest is matching testing condition provided.
     */
    public static boolean test( final ScriptField fieldToTest, final Object objective, final int operator, final String optional ) {
        final int type = fieldToTest.getType();

        switch( type ) {
            case ScriptField.TYPE_DOUBLE:
                return testByDouble( fieldToTest, (Double) parseDouble( objective ), operator );
            case ScriptField.TYPE_INT:
                return testByInteger( fieldToTest, (Integer) parseInteger( objective ), operator );
            case ScriptField.TYPE_BOOLEAN:
                if( objective instanceof Boolean ) {
                    return testByBoolean( fieldToTest, (Boolean) objective, operator );
                } else {
                    return false;
                }
            case ScriptField.TYPE_STRING:
                return testByString( fieldToTest, (String) parseString( objective ), operator );
            case ScriptField.TYPE_DATE:
                return testByDate( fieldToTest, objective, operator, optional );
            case ScriptField.TYPE_SCRIPTABLE:
                // supported only if _value is array and the operator is CONTAINS or ==
                return testByArray( fieldToTest, objective, operator );
            default:
                // should not happen
                return false;
        }
    }

    private static boolean testByString( final ScriptField sf, final String compare, final int operator ) {
        final String value = (String) sf.getValue();

        switch( operator ) {
            case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                return !( value.compareTo( compare ) == 0 );
            case FilterExpressionObject.OPERATOR_EQUAL:
                return value.compareTo( compare ) == 0;
            case FilterExpressionObject.OPERATOR_LESS_THAN:
                return value.compareTo( compare ) < 0;
            case FilterExpressionObject.OPERATOR_GREATER_THAN:
                return value.compareTo( compare ) > 0;
            case FilterExpressionObject.OPERATOR_REG_EXP: {
                final boolean m = PatternMatchingUtilities.isMatch( FindExtension.getScriptEngine(), value, compare );
                return m;
            }
            case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                return value.compareTo( compare ) <= 0;
            case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                return value.compareTo( compare ) >= 0;
        }

        return false;
    }

    private static boolean testByDouble( final ScriptField sf, final Double objective, final int operator ) {
        final Double value = (Double) sf.getValue();

        switch( operator ) {
            case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                return objective.doubleValue() != value.doubleValue();
            case FilterExpressionObject.OPERATOR_EQUAL:
                return objective.doubleValue() == value.doubleValue();
            case FilterExpressionObject.OPERATOR_LESS_THAN:
                return value.doubleValue() < objective.doubleValue();
            case FilterExpressionObject.OPERATOR_GREATER_THAN:
                return value.doubleValue() > objective.doubleValue();
            case FilterExpressionObject.OPERATOR_REG_EXP: {
                return false;
            }
            case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                return value.doubleValue() <= objective.doubleValue();
            case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                return value.doubleValue() >= objective.doubleValue();
        }

        return false;
    }

    private static boolean testByInteger( final ScriptField sf, final Integer objective, final int operator ) {
        final Integer value = (Integer) sf.getValue();

        switch( operator ) {
            case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                return objective.intValue() != value.intValue();
            case FilterExpressionObject.OPERATOR_EQUAL:
                return objective.intValue() == value.intValue();
            case FilterExpressionObject.OPERATOR_LESS_THAN:
                return value.intValue() < objective.intValue();
            case FilterExpressionObject.OPERATOR_GREATER_THAN:
                return value.intValue() > objective.intValue();
            case FilterExpressionObject.OPERATOR_REG_EXP: {
                return false;
            }
            case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                return value.intValue() <= objective.intValue();
            case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                return value.intValue() >= objective.intValue();
        }

        return false;
    }

    private static boolean testByBoolean( final ScriptField sf, final Boolean objective, final int operator ) {
        final Boolean value = (Boolean) sf.getValue();

        switch( operator ) {
            case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                return objective.booleanValue() != value.booleanValue();
            case FilterExpressionObject.OPERATOR_EQUAL:
                return objective.booleanValue() == value.booleanValue();
            case FilterExpressionObject.OPERATOR_LESS_THAN:
                return false;
            case FilterExpressionObject.OPERATOR_GREATER_THAN:
                return false;
            case FilterExpressionObject.OPERATOR_REG_EXP:
                return false;
            case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                return false;
            case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                return false;
        }

        return false;
    }

    private static boolean testByDate( final ScriptField sf, Object objective, final int operator, final String optional ) {
        final Date value = (Date) sf.getValue();

        if( value == null ) {
            return false;
        }

        if( optional != null && optional.length() > 0 ) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( value );
            int i;

            if( optional.equals( FindNamespace.DATE_FIELD_YEAR ) ) {
                i = cal.get( Calendar.YEAR );
            } else if( optional.equals( FindNamespace.DATE_FIELD_MONTH ) ) {
                i = cal.get( Calendar.MONTH ) + 1; // Month starts from 0
            } else if( optional.equals( FindNamespace.DATE_FIELD_DAY ) ) {
                i = cal.get( Calendar.DAY_OF_MONTH );
            } else if( optional.equals( FindNamespace.DATE_FIELD_HOUR ) ) {
                i = cal.get( Calendar.HOUR_OF_DAY );
            } else if( optional.equals( FindNamespace.DATE_FIELD_MINUTE ) ) {
                i = cal.get( Calendar.MINUTE );
            } else if( optional.equals( FindNamespace.DATE_FIELD_SECOND ) ) {
                i = cal.get( Calendar.SECOND );
            } else {
                // unsupported Date field
                return false;
            }

            if( objective instanceof String ) {
                final String compare = (String) objective;
                final String v = new Integer( i ).toString();
                switch( operator ) {
                    case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                        return !( v.compareTo( compare ) == 0 );
                    case FilterExpressionObject.OPERATOR_EQUAL:
                        return v.compareTo( compare ) == 0;
                    case FilterExpressionObject.OPERATOR_LESS_THAN:
                        return v.compareTo( compare ) < 0;
                    case FilterExpressionObject.OPERATOR_GREATER_THAN:
                        return v.compareTo( compare ) > 0;
                    case FilterExpressionObject.OPERATOR_REG_EXP: {
                        return PatternMatchingUtilities.isMatch( FindExtension.getScriptEngine(), v, compare );
                    }
                    case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                        return v.compareTo( compare ) <= 0;
                    case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                        return v.compareTo( compare ) >= 0;
                }

            } else if( objective instanceof Integer ) {
                final int v = i;
                final int o = ( (Integer) objective ).intValue();
                switch( operator ) {
                    case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                        return v != o;
                    case FilterExpressionObject.OPERATOR_EQUAL:
                        return v == o;
                    case FilterExpressionObject.OPERATOR_LESS_THAN:
                        return v < o;
                    case FilterExpressionObject.OPERATOR_GREATER_THAN:
                        return v > o;
                    case FilterExpressionObject.OPERATOR_REG_EXP: {
                        return false;
                    }
                    case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                        return v <= o;
                    case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                        return v >= o;
                }
            } else {
                return false;
            }
        } else {
            /**
             * In 6.0 - java.util.Date is not passed back, it is a Scriptable object representing the JavaScript Date object.
             * Retrieve the getTime() and create java.util.Date
             */
            if( objective instanceof Scriptable ) {
                try {
                    final Scriptable s = (Scriptable) objective;
                    final Object getTime = s.getField( "getTime" );
                    if( getTime instanceof ScriptableFunction ) {
                        final Double millis = (Double) ( (ScriptableFunction) getTime ).invoke( objective, new Object[] {} );
                        objective = new Date( millis.longValue() );
                    }
                } catch( final Exception e ) {
                    return false;
                }
            } else if( objective instanceof Double ) {
                objective = new Date( ( (Double) objective ).longValue() );
            } else if( objective instanceof Integer ) {
                objective = new Date( ( (Integer) objective ).longValue() );
            }

            if( objective instanceof Date ) {
                final long v = value.getTime();
                final Date d = (Date) objective;
                final long o = d.getTime();
                switch( operator ) {
                    case FilterExpressionObject.OPERATOR_NOT_EQUAL:
                        return o != v;
                    case FilterExpressionObject.OPERATOR_EQUAL:
                        return o == v;
                    case FilterExpressionObject.OPERATOR_LESS_THAN:
                        return v < o;
                    case FilterExpressionObject.OPERATOR_GREATER_THAN:
                        return v > o;
                    case FilterExpressionObject.OPERATOR_REG_EXP: {
                        return false;
                    }
                    case FilterExpressionObject.OPERATOR_LESS_OR_EQUAL:
                        return v <= o;
                    case FilterExpressionObject.OPERATOR_GREATER_OR_EQUAL:
                        return v >= o;
                }
            } else {
                return false;
            }
        }

        return false;
    }

    private static boolean testByArray( final ScriptField sf, final Object objective, final int operator ) {
        final Object value = sf.getValue();
        Object[] valueArr = null;

        try {
            if( value != null ) {
                if( value instanceof Scriptable ) {
                    final Scriptable valuesArray = (Scriptable) value;

                    if( valuesArray.getField( "length" ) != null ) {
                        final int length = ( (Integer) valuesArray.getField( "length" ) ).intValue();
                        valueArr = new Object[ length ];

                        for( int i = 0; i < length; i++ ) {
                            final Object obj = valuesArray.getElement( i );
                            valueArr[ i ] = obj;
                        }
                    }
                } else if( value instanceof Object[] ) {
                    valueArr = (Object[]) value;
                }
            }

            switch( operator ) {
                case FilterExpressionObject.OPERATOR_CONTAINS:
                    if( valueArr != null ) {
                        for( int i = 0; i < valueArr.length; i++ ) {
                            if( valueArr[ i ].equals( objective ) ) {
                                return true;
                            }
                        }
                    }

                    break;
                case FilterExpressionObject.OPERATOR_EQUAL:
                    // if operator is "==", only supports comparison with null
                    if( objective == null ) {
                        return valueArr == null || valueArr.length == 0;
                    }

                    break;
            }
        } catch( final Exception e ) {
            return false;
        }

        return false;
    }

    private static Object parseDouble( final Object value ) {
        if( value instanceof Double ) {
            return value;
        }

        double d = 0;
        try {
            d = Double.parseDouble( value.toString() );
        } catch( final NumberFormatException e ) {
        }

        return new Double( d );
    }

    private static Object parseInteger( final Object value ) {
        if( value instanceof Integer ) {
            return value;
        }

        int i = 0;
        try {
            i = Integer.parseInt( value.toString() );
        } catch( final NumberFormatException e ) {
        }

        return new Integer( i );
    }

    private static Object parseString( final Object value ) {
        return value.toString();
    }

}

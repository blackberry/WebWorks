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
import java.util.Vector;

import net.rim.device.api.script.Scriptable;
import blackberry.common.util.StringTokenizer;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.find.filterExpression.FilterExpressionConstructor;

/**
 * FindNamespace defines public properties for blackberry.find namespace.
 */
public class FindNamespace extends Scriptable {

    public static final String NAME = "blackberry.find";

    final static String DATE_FIELD_YEAR = "Year";
    final static String DATE_FIELD_MONTH = "Month";
    final static String DATE_FIELD_DAY = "Day";
    final static String DATE_FIELD_HOUR = "Hour";
    final static String DATE_FIELD_MINUTE = "Minute";
    final static String DATE_FIELD_SECOND = "Second";

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( FilterExpressionConstructor.NAME ) ) {
            return new FilterExpressionConstructor();
        }

        return super.getField( name );
    }

    /**
     * Searching for the fieldName provided.
     * 
     * @param object
     *            to perform the search on.
     * @param fieldName
     *            the name of the field to be found.
     * @param optionalProperty
     *            the optional property if passed.
     * @return the ScriptField found, <code>null</code> is returned otherwise.
     */
    public static final ScriptField getScriptField( final ScriptableObjectBase object, final String fieldName,
            final StringBuffer optionalProperty ) {
        // clear optionalField
        optionalProperty.setLength( 0 );

        // Parse whether leftField contains subfield or attribute
        ScriptableObjectBase o = object;
        String optional = "";
        String key = fieldName;
        if( key.indexOf( '.' ) >= 0 ) {
            final StringTokenizer st = new StringTokenizer( key, "." );
            while( true ) {
                key = st.nextToken();
                if( !st.hasMoreTokens() ) {
                    break;
                }

                final ScriptField field = o.getItem( key );
                if( field == null ) {
                    return null;
                }

                if( field.getType() == ScriptField.TYPE_SCRIPTABLE ) {
                    o = (ScriptableObjectBase) field.getValue();
                    if( o == null ) {
                        return null;
                    }
                } else {
                    // allow other types have sub-property???
                    optional = st.nextToken();
                    break;
                }
            }
        }

        ScriptField fieldToTest = null;
        try {
            fieldToTest = o.getItem( key );
        } catch( final Exception e ) {
        }
        optionalProperty.append( optional );

        return fieldToTest;
    }

    /**
     * Validate the find arguments.
     * 
     * @param args
     *            the arguments to be checked for validity.
     * @param hasService
     *            indicates if one of the arguments passed is a service.
     * @return the boolean value to be the result of arguments validation.
     */
    public static final boolean isValidFindArguments( final Object[] args, final boolean hasService ) {
        int argsIndex = 0;

        // filterExpression object
        if( args.length > argsIndex ) {
            if( args[ argsIndex ] != null && !( args[ argsIndex ] instanceof TestableScriptableObject ) ) {
                return false;
            }
        }
        argsIndex++;

        // orderBy string
        if( args.length > argsIndex ) {
            if( args[ argsIndex ] != null && !( args[ argsIndex ] instanceof String ) ) {
                return false;
            }
        }
        argsIndex++;

        // maxReturn number
        if( args.length > argsIndex ) {
            if( args[ argsIndex ] != null && !( args[ argsIndex ] instanceof Integer ) ) {
                return false;
            }
        }
        argsIndex++;

        if( hasService ) {
            // service object
            if( args.length > argsIndex ) {
                // To remove extension dependency compare strings instead of using instanceof ServiceObject
                if( args[ argsIndex ] != null && !args[ argsIndex ].getClass().getName().endsWith( "ServiceObject" ) ) {
                    return false;
                }
            }
            argsIndex++;
        }

        // ASC or DESC
        if( args.length > argsIndex ) {
            if( args[ argsIndex ] != null && !( args[ argsIndex ] instanceof Boolean ) ) {
                return false;
            }
        }
        argsIndex++;

        return true;
    }

    /**
     * Retrieve the Object.
     * 
     * @param object
     *            the Scriptable instance.
     * @param fieldName
     *            the fieldName the value for it would be retrieve.
     * @return the Object represents the value, or null when object equals null or fieldName is null or empty.
     */
    public static final Object getScriptFieldValue( final ScriptableObjectBase object, final String fieldName ) {
        if( object == null || fieldName == null || fieldName.length() == 0 ) {
            return null;
        }

        final StringBuffer optionalProperty = new StringBuffer();
        final ScriptField field = FindNamespace.getScriptField( object, fieldName, optionalProperty );
        final String fieldOptional = optionalProperty.toString();

        if( field == null ) {
            return null;
        }

        // find the right position to insert the new object based on the value of the orderByField
        Object value = field.getValue();
        if( fieldOptional.length() > 0 && field.getType() == ScriptField.TYPE_DATE && value != null ) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( (Date) value );

            int dateField;
            if( fieldOptional.equals( DATE_FIELD_YEAR ) ) {
                dateField = cal.get( Calendar.YEAR );
            } else if( fieldOptional.equals( DATE_FIELD_MONTH ) ) {
                dateField = cal.get( Calendar.MONTH ) + 1; // Month starts from 0
            } else if( fieldOptional.equals( DATE_FIELD_DAY ) ) {
                dateField = cal.get( Calendar.DAY_OF_MONTH );
            } else if( fieldOptional.equals( DATE_FIELD_HOUR ) ) {
                dateField = cal.get( Calendar.HOUR_OF_DAY );
            } else if( fieldOptional.equals( DATE_FIELD_MINUTE ) ) {
                dateField = cal.get( Calendar.MINUTE );
            } else if( fieldOptional.equals( DATE_FIELD_SECOND ) ) {
                dateField = cal.get( Calendar.SECOND );
            } else {
                dateField = -1;
            }

            value = new Integer( dateField );
        }

        return value;
    }

    /**
     * Compare the objects by criteria provided.
     * 
     * @param valueInVector
     *            is a value of a field under review.
     * @param orderByValue
     *            is a value of a field which is serves as an order criteria.
     * @param isAscending
     *            when true, the comparison result is made in an ascending order mode.
     * @return the result of a comparison. -1: orderByValue is after valueInVector 0: orderByValue is equal to valueInVector 1:
     *         orderByValue is before valueInVector
     */
    private static final int compareObjects( final Object valueInVector, final Object orderByValue, final boolean isAscending ) {
        int result1;
        int result2;

        if( isAscending ) {
            result1 = -1;
            result2 = 1;
        } else { // Descending
            result1 = 1;
            result2 = -1;
        }

        if( valueInVector == null ) {
            return result2;
        }

        if( valueInVector instanceof Integer && orderByValue instanceof Integer ) {
            final int v = ( (Integer) valueInVector ).intValue();
            final int o = ( (Integer) orderByValue ).intValue();
            if( v < o ) {
                return result1;
            } else if( v == o ) {
                return 0;
            } else {
                return result2;
            }
        } else if( valueInVector instanceof Double && orderByValue instanceof Double ) {
            final double v = ( (Double) valueInVector ).doubleValue();
            final double o = ( (Double) orderByValue ).doubleValue();
            if( v < o ) {
                return result1;
            } else if( v == o ) {
                return 0;
            } else {
                return result2;
            }
        } else if( valueInVector instanceof Date && orderByValue instanceof Date ) {
            final long v = ( (Date) valueInVector ).getTime();
            final long o = ( (Date) orderByValue ).getTime();
            if( v < o ) {
                return result1;
            } else if( v == o ) {
                return 0;
            } else {
                return result2;
            }
        } else if( valueInVector instanceof Boolean && orderByValue instanceof Boolean ) {
            if( ( (Boolean) valueInVector ).booleanValue() == false && ( (Boolean) orderByValue ).booleanValue() == false ) {
                return 0;
            } else if( ( (Boolean) valueInVector ).booleanValue() == true && ( (Boolean) orderByValue ).booleanValue() == true ) {
                return 0;
            } else if( ( (Boolean) valueInVector ).booleanValue() == false && ( (Boolean) orderByValue ).booleanValue() == true ) {
                return result1;
            } else {
                // ( ( (Boolean) valueInVector ).booleanValue() == true && ( (Boolean) orderByValue ).booleanValue() == false )
                return result2;
            }
        } else if( valueInVector instanceof String && orderByValue instanceof String ) {
            final int result = ( (String) valueInVector ).compareTo( (String) orderByValue );
            if( result < 0 ) {
                return result1;
            } else if( result == 0 ) {
                return 0;
            } else {
                return result2;
            }
        }

        return result1;
    }

    private static final void insertAtPosition( final Vector vector, final Scriptable object, final int index ) {
        vector.insertElementAt( object, index );
    }

    private static final void insertAfterPosition( final Vector vector, final Scriptable object, final int index ) {
        if( index < vector.size() - 1 ) {
            vector.insertElementAt( object, index + 1 );
        } else {
            vector.addElement( object );
        }
    }

    /**
     * The method insert elements by specified order in to the vector provide.
     * 
     * @param vector
     *            the vector where object to be inserted.
     * @param object
     *            the object to be added.
     * @param orderByField
     *            specify to what field orderBy should be applied.
     * @param isAscending
     *            specify if the order is ascending.
     */
    public static final void insertElementByOrder( final Vector vector, final ScriptableObjectBase object,
            final String orderByField, final boolean isAscending ) {
        if( orderByField != null && orderByField.length() > 0 ) {
            final int size = vector.size();
            int start = 0;
            int end = size - 1;

            if( size == 0 ) {
                vector.addElement( object );
                return;
            }

            final Object orderByValue = getScriptFieldValue( object, orderByField );

            if( orderByValue == null ) {
                vector.addElement( object );
                return;
            }

            ScriptableObjectBase objectInVector;
            Object valueInVector;

            while( end - start >= 2 ) {
                final int insertPosition = start + ( end - start ) / 2;

                objectInVector = (ScriptableObjectBase) vector.elementAt( insertPosition );
                valueInVector = getScriptFieldValue( objectInVector, orderByField );

                // if compareObjects returns 0, insert the object in current insertPosition
                final int result = compareObjects( valueInVector, orderByValue, isAscending );
                if( result < 0 ) {
                    // orderByValue is after the value of current vector item
                    start = insertPosition;
                } else if( result == 0 ) {
                    // orderByValue is equal to the value of current vector item
                    insertAtPosition( vector, object, insertPosition );
                    return;
                } else {
                    // orderByValue is before the value of current vector item
                    end = insertPosition;
                }
            }

            // end - start < 2 : end == start or end == start + 1
            objectInVector = (ScriptableObjectBase) vector.elementAt( start );
            valueInVector = getScriptFieldValue( objectInVector, orderByField );

            if( compareObjects( valueInVector, orderByValue, isAscending ) >= 0 ) {
                // orderByValue is equal to or before the value of start vector item
                insertAtPosition( vector, object, start );
            } else {
                if( end != start ) {
                    objectInVector = (ScriptableObjectBase) vector.elementAt( end );
                    valueInVector = getScriptFieldValue( objectInVector, orderByField );
                    if( compareObjects( valueInVector, orderByValue, isAscending ) >= 0 ) {
                        // orderByValue is equal to or before the value of end vector item
                        insertAtPosition( vector, object, end );
                    } else {
                        // orderByValue is after the value of end vector item
                        insertAfterPosition( vector, object, end );
                    }
                } else { // if end == start and already compared by the start position
                    insertAfterPosition( vector, object, start );
                }
            }
        } else {
            vector.addElement( object );
        }
    }

}

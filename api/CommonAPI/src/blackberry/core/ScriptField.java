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
package blackberry.core;

import java.util.Date;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

/**
 * This class is a wrapper to the fields and methods of a Scriptable object
 * 
 * @author dmateescu
 */
public class ScriptField {
    public static final int TYPE_DOUBLE = 0; // java.lang.Double
    public static final int TYPE_STRING = 1; // java.lang.String
    public static final int TYPE_INT = 2; // java.lang.Integer
    public static final int TYPE_SCRIPTABLE = 3; // net.rim.device.api.script.Scriptable
    public static final int TYPE_BOOLEAN = 4; // java.lang.Boolean
    public static final int TYPE_DATE = 5; // java.util.Date

    private String _name;
    private Object _value;
    private int _type;
    private boolean _readonly;
    private boolean _isMethod;

    /**
     * Constructs a ScriptField
     * 
     * @param name
     *            the name of the field or method
     * @param value
     *            the value of the field
     * @param type
     *            the type of the field
     * @param readonly
     *            describes whether the field is readonly or not
     * @param isMethod
     *            describes whether the member is a method
     */
    public ScriptField( String name, Object value, int type, boolean readonly, boolean isMethod ) {
        _name = name;
        _type = type;
        _readonly = readonly;
        _isMethod = isMethod;
        setValue( value );
    }

    /**
     * Returns true if the ScriptField object represents a read-only field; false otherwise.
     * 
     * @return true if the ScriptField object represents a read-only field; false otherwise.
     */
    public boolean isReadOnly() {
        return _readonly;
    }

    /**
     * Returns true if the ScriptField object represents a method; false otherwise.
     * 
     * @return true if the ScriptField object represents a method; false otherwise.
     */
    public boolean isMethod() {
        return _isMethod;
    }

    /**
     * Returns the name of the ScriptField.
     * 
     * @return returns the name of the ScriptField.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the type of the ScriptField object - constant TYPE_* value.
     * 
     * @return
     */
    public int getType() {
        return _type;
    }

    /**
     * Returns the value of the ScriptField object.
     * 
     * @return
     */
    public Object getValue() {
        return _value;
    }

    /**
     * Helper method to quickly return the value of this ScriptField as a java.lang.String
     * 
     * @return
     */
    public String getStringValue() {
        if( _type != TYPE_STRING )
            return null;
        return (String) getValue();
    }

    /**
     * <description>
     * 
     * @param value
     *            <description>
     */
    public void setValue( Object val ) {
        Object result;
        try {
            result = convertToType( val );
        } catch( Exception ex ) {
            result = null;
        }
        _value = result;
    }

    /**
     * Converts the newValue into the specific type value specified by this ScriptField
     * 
     * @param newValue
     *            <description>
     * @return The Object can be safely cast into the type specified by getType()
     */
    private Object convertToType( Object oldValue ) throws Exception {
        Object newValue = null;

        switch( _type ) {
            case TYPE_DOUBLE:
                newValue = parseDouble( oldValue );
                break;
            case TYPE_INT:
                newValue = parseInteger( oldValue );
                break;
            case TYPE_BOOLEAN:
                newValue = parseBoolean( oldValue );
                break;
            case TYPE_STRING:
                // always accept a string
                newValue = parseString( oldValue );
                break;
            case TYPE_DATE:
                newValue = parseDate( oldValue );
                break;
            case TYPE_SCRIPTABLE:
                newValue = parseScriptable( oldValue );
                break;
            default:
                // should not happen
                newValue = null;
                break;

        }
        return newValue;
    }

    private Object parseDouble( Object value ) {
        if( value instanceof Double ) {
            return value;
        }

        double d = 0;
        try {
            d = Double.parseDouble( value.toString() );
        } catch( NumberFormatException e ) {
        }

        return new Double( d );
    }

    private Object parseInteger( Object value ) {
        if( value instanceof Integer ) {
            return value;
        }

        int i = 0;
        try {
            i = Integer.parseInt( value.toString() );
        } catch( NumberFormatException e ) {
        }

        return new Integer( i );
    }

    private Object parseBoolean( Object value ) {
        if( value instanceof Boolean ) {
            return value;
        }

        String s = value.toString();
        if( s.equalsIgnoreCase( "true" ) )
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    private Object parseDate( Object value ) throws Exception {
        /**
         * In 6.0 - java.util.Date is not passed back, it is a Scriptable object representing the JavaScript Date object. Retrieve
         * the getTime() and create java.util.Date
         */
        if( value instanceof Scriptable ) {
            try {
                Scriptable s = (Scriptable) value;
                Object getTime = s.getField( "getTime" );
                if( getTime instanceof ScriptableFunction ) {
                    Double millis = (Double) ( (ScriptableFunction) getTime ).invoke( value, new Object[] {} );
                    return new Date( millis.longValue() );
                }
            } catch( Exception e ) {
                throw e;
            }
        }
        // just in case somebody 'fixes' this issue
        else if( value instanceof Date ) {
            return value;
        }
        else if (value instanceof Double) {
            Double millis = (Double)value;
            return new Date(millis.longValue());
        }
        else if (value instanceof Integer) {            
            return new Date(( (Integer) value ).longValue());
        }
        return null;
    }

    private Object parseString( Object value ) {
        return value.toString();
    }

    private Object parseScriptable( Object value ) {
        return value;
    }
}

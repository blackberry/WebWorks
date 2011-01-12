/*
 * ScriptField.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.jse.base;

import java.util.Date;

/**
 * The class that represents each field of a scriptable object
 */
public class ScriptField {
    public static final int         TYPE_DOUBLE = 0;        // java.lang.Double
    public static final int         TYPE_STRING = 1;        // java.lang.String
    public static final int         TYPE_INT = 2;           // java.lang.Integer
    public static final int         TYPE_SCRIPTABLE = 3;    // net.rim.device.api.script.Scriptable
    public static final int         TYPE_BOOLEAN = 4;       // java.lang.Boolean
    public static final int         TYPE_DATE = 5;          // java.util.Date

    private String      _name;
    private Object      _value;
    private int         _type;
    private boolean     _readonly;
    private boolean     _isMethod;

    public ScriptField(String name, Object value, int type, boolean readonly, boolean isMethod) {
        _name = name;
        _value = value;
        _type = type;
        _readonly = readonly;
        _isMethod = isMethod;
    }

    /**
     * Converts the newValue into the specific type value specified by this ScriptField
     * @param newValue <description>
     * @return The Object can be safely cast into the type specified by getType()
     */
    private Object convertToType(Object oldValue) throws Exception {
        Object newValue = null;

        switch (_type) {
            case TYPE_DOUBLE:
                newValue = parseDouble(oldValue);
                break;
            case TYPE_INT:
                newValue = parseInteger(oldValue);
                break;
            case TYPE_BOOLEAN:
                newValue = parseBoolean(oldValue);
                break;
            case TYPE_STRING:
                newValue = parseString(oldValue);
                break;
            case TYPE_DATE:
                newValue = parseDate(oldValue);
                break;
            case TYPE_SCRIPTABLE:
                newValue = parseScriptable(oldValue);                   
                break;
            default:
                // should not happen
                newValue = null;
                break;
        }

        return newValue;
    }

    private Object parseDouble(Object value) {
        if (value instanceof Double) {
            return value;
        }
        
        double d = 0;
        try {
            d = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
        }
        
        return new Double(d);
    }
        
    private Object parseInteger(Object value) {
        if (value instanceof Integer) {
            return value;
        }
        
        int i = 0;
        try {
            i = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
        }
        
        return new Integer(i);
    }

    private Object parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return value;
        }
        
        String s = value.toString();
        if (s.equalsIgnoreCase("true"))
            return Boolean.TRUE;

        return Boolean.FALSE;
    }

    private Object parseDate(Object value) {
        if (!(value instanceof Date))
            return null;

        return value;
    }

    private Object parseString(Object value) {
        return value.toString();
    }

    private Object parseScriptable(Object value) {
        return value;
    }
    
    public void setValue (Object value) {
        _value = value;
    }

    public String getName() { return _name; }

    public Object getValue() {
        Object val;

        try {
            val = convertToType(_value);
        } catch (Exception ex) {
            val = null;
        }

        return (Object) val;
    }

    public boolean isReadOnly() { return _readonly; }

    public boolean isMethod() { return _isMethod; }
}

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

/**
 * This class provides the base for the Scriptable classes who need to set fields. It caches the fields and methods of a
 * Scriptable, and provides a way of verifying the writable (no-read-only) fields of a Scriptable.
 * 
 * @author dmateescu
 * 
 */
public abstract class ScriptableObjectBase extends Scriptable {

    protected Hashtable _fields; // Script items that are actually available

    protected static final String SCRIPT_NAMESPACE_DELIM = ".";

    /**
     * Constructs a ScriptableObjectBase object
     */
    public ScriptableObjectBase() {
        _fields = new Hashtable();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getParent()
     */
    public Scriptable getParent() {
        return null;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#enumerateFields(java.util.Vector)
     */
    public void enumerateFields( Vector v ) {
        if( !_fields.isEmpty() ) {
            for( Enumeration e = _fields.keys(); e.hasMoreElements(); ) {
                v.addElement( e.nextElement() );
            }
        }
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) {
        Object field = _fields.get( name );
        if( field == null ) {
            return UNDEFINED;
        }
        return ( (ScriptField) field ).getValue();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#putField(java.lang.String, java.lang.Object)
     */
    public boolean putField( String name, Object value ) throws Exception {
        if( _fields.containsKey( name ) ) {
            // Retrieve the field from the lookup
            ScriptField field = (ScriptField) _fields.get( name );

            if( field.isReadOnly() ) {
                return false;
            }

            // Type check/verify by implementing object
            if( verify( field, value ) ) {
                // Set the new field
                field.setValue( value );
                return true;
            }
        }
        return super.putField( name, value );
    }

    protected void addItem( ScriptField field ) {
        _fields.put( field.getName(), field );
    }

    /**
     * Sets the value for the given field.
     * 
     * @param fieldName
     *            The field name
     * @param value
     *            The value
     */
    public void setValue( String fieldName, Object value ) {
        ScriptField field = (ScriptField) _fields.get( fieldName );
        if( field == null ) {
            throw new IllegalArgumentException( "Field " + fieldName + " does not exist." );
        }
        field.setValue( value );
    }
    
    /**
     * This method returns an item corresponding to a field
     * 
     * @param name
     *            the field name
     * @return
     */
    public ScriptField getItem( String name ) {
        return (ScriptField) _fields.get( name );
    }

    /**
     * This method returns whether setting a field is a valid operation. This default implementation assumes that the class
     * contains only read-only fields.
     * 
     * @param fieldName
     *            the field name
     * @param newValue
     *            the new value
     * @return false by default. It is up to the classes extending this class to override this method.
     * @throws Exception
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return false;
    }

    protected void addReadOnlyField( String name, Object value, int type ) {
        addItem( new ScriptField( name, value, type, true, false ) );
    }
}

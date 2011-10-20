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
import java.util.Enumeration;
import java.util.Vector;

import blackberry.common.util.json4j.JSONArray;
import blackberry.common.util.json4j.JSONException;
import blackberry.common.util.json4j.JSONObject;

import net.rim.device.api.script.Scriptable;

/**
 * The Class ObjectBase implements ScriptableObjectBase for underlying compatibility
 * and makes all unused inherited fields deprecated. 
 */
public class ObjectBase extends ScriptableObjectBase {

    private final String METHOD_NOT_SUPPORTED = "Method is not supported.";
    
    public static final Object UNDEFINED = null;
    
    /*
     * @see blackberry.core.ScriptableObjectBase#getParent()
     */
    public Scriptable getParent() {
        return null;
    }

    /*
     * @see blackberry.core.ScriptableObjectBase#enumerateFields(java.util.Vector)
     */
    public void enumerateFields( Vector vector ) {
    }

    /*
     * @see net.rim.device.api.script.Scriptable#getElementCount()
     */
    public int getElementCount() {
        return -1;
    }

    /*
     * @see net.rim.device.api.script.Scriptable#getElement(int)
     */
    public Object getElement( int i ) throws Exception {
        throw new Exception( METHOD_NOT_SUPPORTED );
    }

    /*
     * @see net.rim.device.api.script.Scriptable#putElement(int, java.lang.Object)
     */
    public boolean putElement( int i, Object obj ) throws Exception {
        throw new Exception( METHOD_NOT_SUPPORTED );
    }

    /*
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        throw new Exception( METHOD_NOT_SUPPORTED );
    }

    /*
     * @see blackberry.core.ScriptableObjectBase#addReadOnlyField(java.lang.String, java.lang.Object, int)
     */
    protected void addReadOnlyField( String name, Object value, int type ) {
    }
    
    /**
     * Clear all fields in hashtable
     */
    public void clearAllFields() {
        if( _fields != null ) {
            _fields.clear();
        }
    }

    /**
     * Get list of field names to EXclude in the JSON representation of this object.<br>
     * By default, no fields get excluded. Sub-class should override if it requires certain fields to be excluded.
     *
     * @return vector contains field names
     */
    protected Vector getJSONExcludedFields() {
        return null;
    }

    /**
     * Create a JSONObject with fields in the hashtable.<br>
     * Date will be stored as Long since JSONObject does not serialize Date.<br>
     * 
     * @return JSONObject
     * @throws JSONException
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();

        if( _fields != null && !_fields.isEmpty() ) {
            Enumeration keys = _fields.keys();
            Vector jsonExcludedFields = getJSONExcludedFields();

            while( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();

                if( jsonExcludedFields == null || ( jsonExcludedFields != null && !jsonExcludedFields.contains( key ) ) ) {
                    ScriptField field = getItem( key );
                    Object value = getField( key );
                    if( field.getType() == ScriptField.TYPE_DATE ) {
                        if( value != null ) {
                            value = new Long( ( (Date) value ).getTime() );
                        }
                    } else if( field.getType() == ScriptField.TYPE_SCRIPTABLE ) {
                        if( value != null ) {
                            if( value instanceof ObjectBase ) {
                                value = ( (ObjectBase) value ).getJSONObject();
                            } else if( value instanceof ObjectBase[] ) {
                                value = convertObjectArrayToJSONArray( (ObjectBase[]) value );
                            }

                        }
                    }

                    // Added checking for empty string array, json.put doesn't parse string array if it is null
                    if( value instanceof String[] ) {
                        String[] ss = (String[]) value;
                        jsonObj.put( key, ss );
                    } else {
                        jsonObj.put( key, value );
                    }
                }
            }
        }

        return jsonObj;
    }

    /**
     * Utility method to convert an ObjectBase array to JSON array
     *
     * @param arr
     * @return JSONArray
     * @throws JSONException
     */
    public static JSONArray convertObjectArrayToJSONArray( ObjectBase[] arr ) throws JSONException {
        JSONArray jsonArr = new JSONArray();

        if( arr != null ) {
            for( int i = 0; i < arr.length; i++ ) {
                if( arr[ i ] != null ) {
                    jsonArr.add( arr[ i ].getJSONObject() );
                }       
            }
        }

        return jsonArr;
    }
}


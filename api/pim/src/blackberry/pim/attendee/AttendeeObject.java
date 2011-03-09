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
package blackberry.pim.attendee;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents an Attendee
 * 
 * @author dmateescu
 */
public class AttendeeObject extends ScriptableObjectBase {

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_ADDRESS = "address";
    private static final String EMPTY_ADDRESS = "";

    /**
     * Constructs a new AttendeeObject
     */
    public AttendeeObject() {
        addItem( new ScriptField( FIELD_ADDRESS, EMPTY_ADDRESS, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_TYPE, UNDEFINED, ScriptField.TYPE_SCRIPTABLE, false, false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * Returns the type of an attendee
     * 
     * @return the type
     */
    public int getType() {
        return ( (Integer) getItem( FIELD_TYPE ).getValue() ).intValue();
    }

    /**
     * Returns the address of an attendee
     * 
     * @return the address
     */
    public String getAddress() {
        try {
            return (String) getField( FIELD_ADDRESS );
        } catch( Exception e ) {
            return EMPTY_ADDRESS;
        }
    }
}

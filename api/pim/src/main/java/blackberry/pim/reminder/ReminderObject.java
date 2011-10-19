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
package blackberry.pim.reminder;

import java.util.Date;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents a Reminder
 * 
 * @author dmateescu
 */
public class ReminderObject extends ScriptableObjectBase {

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_RELATIVE_HOURS = "relativeHours";
    public static final String FIELD_DATE = "date";

    /**
     * Constructs a new ReminderObject
     */
    public ReminderObject() {
        addItem( new ScriptField( FIELD_TYPE, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_RELATIVE_HOURS, new Double( 0 ), ScriptField.TYPE_DOUBLE, false, false ) );
        addItem( new ScriptField( FIELD_DATE, null, ScriptField.TYPE_DATE, false, false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        String fieldName = field.getName();
        // The field 'type' must be a value of either 0 or 1
        if( fieldName.equals( FIELD_TYPE ) ) {
            int i = ( (Integer) newValue ).intValue();
            if( i == 0 || i == 1 ) {
                return true;
            }
            return false;
        }
        // no need to worry about 'relativeHours' or 'date' - as long as they convert, they're fine
        return true;
    }

    /**
     * Returns the int value of the type
     * 
     * @return the type
     */
    public int getType() {
        Integer i = (Integer) getItem( FIELD_TYPE ).getValue();
        return i.intValue();
    }

    /**
     * Returns the relative hours as a double
     * 
     * @return the relative hours
     */
    public double getRelativeHours() {
        return Double.parseDouble( getItem( FIELD_RELATIVE_HOURS ).getValue().toString() );
    }

    /**
     * Returns the date
     * 
     * @return the date
     */
    public Date getDate() {
        return (Date) getItem( FIELD_DATE ).getValue();
    }
}

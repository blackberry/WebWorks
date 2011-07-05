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
package blackberry.pim.recurrence;

import javax.microedition.pim.RepeatRule;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents a Recurrence
 * 
 * @author dmateescu
 */
public class RecurrenceObject extends ScriptableObjectBase {

    public static final String FIELD_FREQUENCY = "frequency";
    public static final String FIELD_COUNT = "count";
    public static final String FIELD_INTERVAL = "interval";
    public static final String FIELD_END = "end";
    public static final String FIELD_MONTHINYEAR = "monthInYear";
    public static final String FIELD_WEEKINMONTH = "weekInMonth";
    public static final String FIELD_DAYINWEEK = "dayInWeek";
    public static final String FIELD_DAYINMONTH = "dayInMonth";
    public static final String FIELD_DAYINYEAR = "dayInYear";
    private static final int DEFAULT_FREQUENCY = 0;
    private static final int DAILY_FREQUENCY = 1;
    private static final int WEEKLY_FREQUENCY = 2;
    private static final int MONTHLY_FREQUENCY = 3;
    private static final int YEARLY_FREQUENCY = 4;

    /**
     * Constructs a new RecurenceObject
     */
    public RecurrenceObject() {
        addItem( new ScriptField( FIELD_FREQUENCY, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_COUNT, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_INTERVAL, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_END, null, ScriptField.TYPE_DATE, false, false ) );
        addItem( new ScriptField( FIELD_MONTHINYEAR, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_WEEKINMONTH, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_DAYINWEEK, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_DAYINMONTH, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
        addItem( new ScriptField( FIELD_DAYINYEAR, new Integer( 0 ), ScriptField.TYPE_INT, false, false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        String fieldName = field.getName();
        // The field 'frequency' must be a value from 0 to 4
        if( fieldName.equals( FIELD_FREQUENCY ) ) {
            int i = ( (Integer) newValue ).intValue();
            if( i >= 0 && i <= 4 ) {
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * This static method converts the frequency into repeat rule
     * 
     * @param frequency
     *            the frequency of the recurrence
     * @return the repeat rule
     */
    public static int frequencyToRepeatRule( int frequency ) {
        switch( frequency ) {
            case 0:
                return DEFAULT_FREQUENCY;
            case 1:
                return RepeatRule.DAILY;
            case 2:
                return RepeatRule.WEEKLY;
            case 3:
                return RepeatRule.MONTHLY;
            case 4:
                return RepeatRule.YEARLY;
            default:
                return DEFAULT_FREQUENCY;
        }
    }

    /**
     * This static method converts the repeat rule into frequency
     * 
     * @param repeatRule
     *            the repeat rule
     * @return the frequency of the recurrence
     */
    public static int repeatRuleToFrequency( int repeatRule ) {
        if( repeatRule == 0 )
            return DEFAULT_FREQUENCY;
        else if( repeatRule == RepeatRule.DAILY )
            return DAILY_FREQUENCY;
        else if( repeatRule == RepeatRule.WEEKLY )
            return WEEKLY_FREQUENCY;
        else if( repeatRule == RepeatRule.MONTHLY )
            return MONTHLY_FREQUENCY;
        else if( repeatRule == RepeatRule.YEARLY )
            return YEARLY_FREQUENCY;
        else
            return DEFAULT_FREQUENCY;
    }
}

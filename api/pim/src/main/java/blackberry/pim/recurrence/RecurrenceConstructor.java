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

import blackberry.core.ScriptableFunctionBase;

/**
 * This class represents the constructor of a Recurrence
 * 
 * @author dmateescu
 */
public class RecurrenceConstructor extends ScriptableFunctionBase {
    public static final String NAME = "blackberry.pim.Recurrence";

    public static final int NO_REPEAT = 0;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;
    public static final int MONTHLY = 3;
    public static final int YEARLY = 4;

    public static final String LABEL_NO_REPEAT = "NO_REPEAT";
    public static final String LABEL_DAILY = "DAILY";
    public static final String LABEL_WEEKLY = "WEEKLY";
    public static final String LABEL_MONTHLY = "MONTHLY";
    public static final String LABEL_YEARLY = "YEARLY";

    public static final String LABEL_JANUARY = "JANUARY";
    public static final String LABEL_FEBRUARY = "FEBRUARY";
    public static final String LABEL_MARCH = "MARCH";
    public static final String LABEL_APRIL = "APRIL";
    public static final String LABEL_MAY = "MAY";
    public static final String LABEL_JUNE = "JUNE";
    public static final String LABEL_JULY = "JULY";
    public static final String LABEL_AUGUST = "AUGUST";
    public static final String LABEL_SEPTEMBER = "SEPTEMBER";
    public static final String LABEL_OCTOBER = "OCTOBER";
    public static final String LABEL_NOVEMBER = "NOVEMBER";
    public static final String LABEL_DECEMBER = "DECEMBER";

    public static final String LABEL_FIRST = "FIRST";
    public static final String LABEL_SECOND = "SECOND";
    public static final String LABEL_THIRD = "THIRD";
    public static final String LABEL_FOURTH = "FOURTH";
    public static final String LABEL_FIFTH = "FIFTH";
    public static final String LABEL_LAST = "LAST";
    public static final String LABEL_SECONDLAST = "SECONDLAST";
    public static final String LABEL_THIRDLAST = "THIRDLAST";
    public static final String LABEL_FOURTHLAST = "FOURTHLAST";
    public static final String LABEL_FIFTHLAST = "FIFTHLAST";

    public static final String LABEL_SUNDAY = "SUNDAY";
    public static final String LABEL_MONDAY = "MONDAY";
    public static final String LABEL_TUESDAY = "TUESDAY";
    public static final String LABEL_WEDNESDAY = "WEDNESDAY";
    public static final String LABEL_THURSDAY = "THURSDAY";
    public static final String LABEL_FRIDAY = "FRIDAY";
    public static final String LABEL_SATURDAY = "SATURDAY";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        return new RecurrenceObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( LABEL_NO_REPEAT ) ) {
            return new Integer( NO_REPEAT );
        } else if( name.equals( LABEL_DAILY ) ) {
            return new Integer( DAILY );
        } else if( name.equals( LABEL_WEEKLY ) ) {
            return new Integer( WEEKLY );
        } else if( name.equals( LABEL_MONTHLY ) ) {
            return new Integer( MONTHLY );
        } else if( name.equals( LABEL_YEARLY ) ) {
            return new Integer( YEARLY );
        } else if( name.equals( LABEL_JANUARY ) ) {
            return new Integer( RepeatRule.JANUARY );
        } else if( name.equals( LABEL_FEBRUARY ) ) {
            return new Integer( RepeatRule.FEBRUARY );
        } else if( name.equals( LABEL_MARCH ) ) {
            return new Integer( RepeatRule.MARCH );
        } else if( name.equals( LABEL_APRIL ) ) {
            return new Integer( RepeatRule.APRIL );
        } else if( name.equals( LABEL_MAY ) ) {
            return new Integer( RepeatRule.MAY );
        } else if( name.equals( LABEL_JUNE ) ) {
            return new Integer( RepeatRule.JUNE );
        } else if( name.equals( LABEL_JULY ) ) {
            return new Integer( RepeatRule.JULY );
        } else if( name.equals( LABEL_AUGUST ) ) {
            return new Integer( RepeatRule.AUGUST );
        } else if( name.equals( LABEL_SEPTEMBER ) ) {
            return new Integer( RepeatRule.SEPTEMBER );
        } else if( name.equals( LABEL_OCTOBER ) ) {
            return new Integer( RepeatRule.OCTOBER );
        } else if( name.equals( LABEL_NOVEMBER ) ) {
            return new Integer( RepeatRule.NOVEMBER );
        } else if( name.equals( LABEL_DECEMBER ) ) {
            return new Integer( RepeatRule.DECEMBER );
        } else if( name.equals( LABEL_FIRST ) ) {
            return new Integer( RepeatRule.FIRST );
        } else if( name.equals( LABEL_SECOND ) ) {
            return new Integer( RepeatRule.SECOND );
        } else if( name.equals( LABEL_THIRD ) ) {
            return new Integer( RepeatRule.THIRD );
        } else if( name.equals( LABEL_FOURTH ) ) {
            return new Integer( RepeatRule.FOURTH );
        } else if( name.equals( LABEL_FIFTH ) ) {
            return new Integer( RepeatRule.FIFTH );
        } else if( name.equals( LABEL_LAST ) ) {
            return new Integer( RepeatRule.LAST );
        } else if( name.equals( LABEL_SECONDLAST ) ) {
            return new Integer( RepeatRule.SECONDLAST );
        } else if( name.equals( LABEL_THIRDLAST ) ) {
            return new Integer( RepeatRule.THIRDLAST );
        } else if( name.equals( LABEL_FOURTHLAST ) ) {
            return new Integer( RepeatRule.FOURTHLAST );
        } else if( name.equals( LABEL_FIFTHLAST ) ) {
            return new Integer( RepeatRule.FIFTHLAST );
        } else if( name.equals( LABEL_SUNDAY ) ) {
            return new Integer( RepeatRule.SUNDAY );
        } else if( name.equals( LABEL_MONDAY ) ) {
            return new Integer( RepeatRule.MONDAY );
        } else if( name.equals( LABEL_TUESDAY ) ) {
            return new Integer( RepeatRule.TUESDAY );
        } else if( name.equals( LABEL_WEDNESDAY ) ) {
            return new Integer( RepeatRule.WEDNESDAY );
        } else if( name.equals( LABEL_THURSDAY ) ) {
            return new Integer( RepeatRule.THURSDAY );
        } else if( name.equals( LABEL_FRIDAY ) ) {
            return new Integer( RepeatRule.FRIDAY );
        } else if( name.equals( LABEL_SATURDAY ) ) {
            return new Integer( RepeatRule.SATURDAY );
        }

        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        return UNDEFINED;
    }
}

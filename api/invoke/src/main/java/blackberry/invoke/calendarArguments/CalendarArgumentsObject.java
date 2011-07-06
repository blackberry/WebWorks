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
package blackberry.invoke.calendarArguments;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.pim.Event;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.pim.appointment.AppointmentObject;

/**
 * This class represents the CalendarArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class CalendarArgumentsObject extends ScriptableObjectBase {

    private final Date _date;
    private final AppointmentObject _appointment;

    public static final String FIELD_DATE = "date";
    public static final String FIELD_VIEW = "view";
    public static final String FIELD_APPOINTMENT = "appointment";

    /**
     * Default constructor, constructs a new CalendarArgumentsObject object.
     */
    public CalendarArgumentsObject() {
        _date = null;
        _appointment = null;
        initial();
    }

    /**
     * Constructs a new CalendarArgumentsObject object.
     * 
     * @param d
     *            Date to open into the Calendar View
     */
    public CalendarArgumentsObject( final Date d ) {
        _date = d;
        _appointment = null;
        initial();
    }

    /**
     * Constructs a new CalendarArgumentsObject object.
     * 
     * @param a
     *            Appointment to view in Calendar application.
     */
    public CalendarArgumentsObject( final AppointmentObject a ) {
        _date = null;
        _appointment = a;
        initial();
    }

    // Injects fields and methods
    private void initial() {
        addItem( new ScriptField( FIELD_DATE, _date, ScriptField.TYPE_DATE, true, false ) );
        addItem( new ScriptField( FIELD_VIEW, new Integer( CalendarArgumentsConstructor.VIEW_NEW ), ScriptField.TYPE_INT, false,
                false ) );
        addItem( new ScriptField( FIELD_APPOINTMENT, _appointment, ScriptField.TYPE_SCRIPTABLE, true, false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the CalendarArgumentsObject's underlying content.
     * 
     * @return the type of view when opening Calendar application.
     */
    public int getView() {
        final Integer i = (Integer) getItem( FIELD_VIEW ).getValue();
        final int view = i.intValue();
        return view;
    }

    /**
     * Internal helper method to get direct access to the CalendarArgumentsObject's underlying content.
     * 
     * @return the contained Calendar Object.
     */
    public Calendar getCalendar() {
        if( _date == null ) {
            return null;
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( _date );
        return calendar;
    }

    /**
     * Internal helper method to get direct access to the CalendarArgumentsObject's underlying content.
     * 
     * @return the reference of BlackBerry Event Object.
     */
    public Event getEvent() {
        if( _appointment == null ) {
            return null;
        }

        return _appointment.getEvent();
    }

    /**
     * Internal helper method to get direct access to the CalendarArgumentsObject's underlying content.
     * 
     * @return the reference of input Appointment Object.
     */
    public AppointmentObject getAppointmentObject() {
        return _appointment;
    }
}

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
package blackberry.pim.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.RepeatRule;

import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.pdap.BlackBerryPIM;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.DeviceInfo;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.service.ServiceObject;
import blackberry.pim.PIMUtils;
import blackberry.pim.attendee.AttendeeObject;
import blackberry.pim.recurrence.RecurrenceObject;
import blackberry.pim.reminder.ReminderConstructor;
import blackberry.pim.reminder.ReminderObject;

/**
 * This class represents an Appointment
 * 
 * @author dmateescu
 * 
 */
public class AppointmentObject extends ScriptableObjectBase {
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_SUMMARY = "summary";
    public static final String FIELD_NOTE = "note";
    public static final String FIELD_START = "start";
    public static final String FIELD_END = "end";
    public static final String FIELD_RECURRENCE = "recurrence";
    public static final String FIELD_REMINDER = "reminder";
    public static final String FIELD_ATTENDEES = "attendees";
    public static final String FIELD_FREEBUSY = "freeBusy";
    public static final String FIELD_ALLDAY = "allDay";
    public static final String FIELD_UID = "uid";
    public static final String METHOD_SAVE = "save";
    public static final String METHOD_REMOVE = "remove";
    public static final String METHOD_FIND = "find";

    private String _serviceName;
    private Event _event;

    private AppointmentSaveScriptableFunction _save;

    /**
     * Default constructor
     */
    public AppointmentObject() {
        _serviceName = "";
        _event = null;
        init();
    }

    /**
     * Constructs an AppointmentObject based on a ServiceObject
     * 
     * @param s
     *            the ServiceObject
     */
    public AppointmentObject( ServiceObject s ) {
        super();
        _event = null;
        if( s != null ) {
            _serviceName = s.getName();
        } else {
            _serviceName = "";
        }
        init();
    }

    /**
     * Constructs an AppointmentObject based on an Event
     * 
     * @param e
     *            the Event
     */
    public AppointmentObject( Event e ) {
        super();
        _event = e;
        _serviceName = "";
        init();
    }

    /**
     * Constructs an AppointmentObject based on an Event and a ServiceObject
     * 
     * @param e
     *            the Event
     * @param s
     *            the ServiceObject
     */
    public AppointmentObject( Event e, ServiceObject s ) {
        super();
        _event = e;
        if( s != null ) {
            _serviceName = s.getName();
        } else {
            _serviceName = "";
        }
        init();
    }

    private void init() {
        // Methods
        addItem( new ScriptField( METHOD_SAVE, createSaveMethod(), ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_REMOVE, createRemoveMethod(), ScriptField.TYPE_SCRIPTABLE, true, true ) );

        String location = "";
        String summary = "";
        String note = "";
        String uid = "";
        Date start = null;
        Date end = null;
        ReminderObject reminder = null;

        boolean allDay = false;
        AttendeeObject[] attendeeObjects = null;
        int freeBusy = AppointmentConstructor.CONST_VAL_BUSY.intValue();

        RepeatRule repeatRule = null;

        if( _event != null ) {
            if( _event.countValues( Event.LOCATION ) > 0 ) {
                location = _event.getString( Event.LOCATION, Event.ATTR_NONE );
            }
            if( _event.countValues( Event.SUMMARY ) > 0 ) {
                summary = _event.getString( Event.SUMMARY, Event.ATTR_NONE );
            }
            if( _event.countValues( Event.NOTE ) > 0 ) {
                note = _event.getString( Event.NOTE, Event.ATTR_NONE );
            }
            if( _event.countValues( Event.UID ) > 0 ) {
                uid = _event.getString( Event.UID, Event.ATTR_NONE );
            }
            if( _event.countValues( Event.START ) > 0 ) {
                start = new Date( _event.getDate( Event.START, Event.ATTR_NONE ) );
            }
            if( _event.countValues( Event.END ) > 0 ) {
                end = new Date( _event.getDate( Event.END, Event.ATTR_NONE ) );
            }
            if( _event.countValues( Event.ALARM ) > 0 ) {
                int relativeTime = _event.getInt( Event.ALARM, Event.ATTR_NONE );
                Double d = new Double( (double) relativeTime / 3600 );

                reminder = new ReminderObject();
                reminder.getItem( ReminderObject.FIELD_TYPE ).setValue( new Integer( ReminderConstructor.TYPE_RELATIVE ) );
                reminder.getItem( ReminderObject.FIELD_RELATIVE_HOURS ).setValue( d );
            }
            if( _event.countValues( BlackBerryEvent.ALLDAY ) > 0 ) {
                allDay = _event.getBoolean( BlackBerryEvent.ALLDAY, Event.ATTR_NONE );
            }
            int countAttendees = _event.countValues( BlackBerryEvent.ATTENDEES );
            attendeeObjects = new AttendeeObject[ countAttendees ];
            for( int i = 0; i < countAttendees; i++ ) {
                AttendeeObject attendeeObject = new AttendeeObject();
                String attendeeAddress = _event.getString( BlackBerryEvent.ATTENDEES, i );
                attendeeObject.getItem( AttendeeObject.FIELD_ADDRESS ).setValue( attendeeAddress );
                attendeeObjects[ i ] = attendeeObject;
            }

            if( _event.countValues( BlackBerryEvent.FREE_BUSY ) > 0 ) {
                freeBusy = _event.getInt( BlackBerryEvent.FREE_BUSY, BlackBerryEvent.ATTR_NONE );
            }
            repeatRule = _event.getRepeat();
        }

        // Properties
        addItem( new ScriptField( FIELD_LOCATION, location, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_NOTE, note, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_SUMMARY, summary, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_UID, uid, ScriptField.TYPE_STRING, true, false ) );

        addItem( new ScriptField( FIELD_START, start, ScriptField.TYPE_DATE, false, false ) );
        addItem( new ScriptField( FIELD_END, end, ScriptField.TYPE_DATE, false, false ) );

        addItem( new ScriptField( FIELD_REMINDER, reminder, ScriptField.TYPE_SCRIPTABLE, false, false ) );

        addItem( new ScriptField( FIELD_ALLDAY, new Boolean( allDay ), ScriptField.TYPE_BOOLEAN, false, false ) );
        addItem( new ScriptField( FIELD_FREEBUSY, new Integer( freeBusy ), ScriptField.TYPE_INT, false, false ) );

        addItem( new ScriptField( FIELD_ATTENDEES, attendeeObjects, ScriptField.TYPE_SCRIPTABLE, false, false ) );

        if( repeatRule != null ) {
            RecurrenceObject recurObject = new RecurrenceObject();

            int count = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.COUNT );
            recurObject.getItem( RecurrenceObject.FIELD_COUNT ).setValue( new Integer( count ) );

            int freq = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.FREQUENCY );
            recurObject.getItem( RecurrenceObject.FIELD_FREQUENCY ).setValue(
                    new Integer( RecurrenceObject.repeatRuleToFrequency( freq ) ) );

            int interval = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.INTERVAL );
            recurObject.getItem( RecurrenceObject.FIELD_INTERVAL ).setValue( new Integer( interval ) );

            long endTime = PIMUtils.getRepeatRuleDate( repeatRule, RepeatRule.END );
            recurObject.getItem( RecurrenceObject.FIELD_END ).setValue( new Date( endTime ) );

            int monthInYear = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.MONTH_IN_YEAR );
            recurObject.getItem( RecurrenceObject.FIELD_MONTHINYEAR ).setValue( new Integer( monthInYear ) );

            int weekInMonth = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.WEEK_IN_MONTH );
            recurObject.getItem( RecurrenceObject.FIELD_WEEKINMONTH ).setValue( new Integer( weekInMonth ) );

            int dayInWeek = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.DAY_IN_WEEK );
            recurObject.getItem( RecurrenceObject.FIELD_DAYINWEEK ).setValue( new Integer( dayInWeek ) );

            int dayInMonth = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.DAY_IN_MONTH );
            recurObject.getItem( RecurrenceObject.FIELD_DAYINMONTH ).setValue( new Integer( dayInMonth ) );

            int dayInYear = PIMUtils.getRepeatRuleInt( repeatRule, RepeatRule.DAY_IN_YEAR );
            recurObject.getItem( RecurrenceObject.FIELD_DAYINYEAR ).setValue( new Integer( dayInYear ) );

            addItem( new ScriptField( FIELD_RECURRENCE, recurObject, ScriptField.TYPE_SCRIPTABLE, false, false ) );
        } else {
            addItem( new ScriptField( FIELD_RECURRENCE, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
        }
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * Returns the appointment's location
     * 
     * @return the location
     */
    public String getLocation() {
        return (String) getItem( FIELD_LOCATION ).getValue();
    }

    /**
     * Returns the appointment's summary
     * 
     * @return the summary
     */
    public String getSummary() {
        return (String) getItem( FIELD_SUMMARY ).getValue();
    }

    /**
     * Returns the appointment's note
     * 
     * @return the note
     */
    public String getNote() {
        return (String) getItem( FIELD_NOTE ).getValue();
    }

    /**
     * Returns the appointment's UID
     * 
     * @return the uid
     */
    public String getUID() {
        return (String) getItem( FIELD_UID ).getValue();
    }

    /**
     * Returns the start date of an appointment
     * 
     * @return start date
     */
    public Date getStart() {
        return (Date) getItem( FIELD_START ).getValue();
    }

    /**
     * Returns the end date of an appointment
     * 
     * @return end date
     */
    public Date getEnd() {
        return (Date) getItem( FIELD_END ).getValue();
    }

    /**
     * Returns the recurrence of an appointment
     * 
     * @return recurrence
     */
    public RecurrenceObject getRecurrence() {
        return (RecurrenceObject) getItem( FIELD_RECURRENCE ).getValue();
    }

    /**
     * Returns the recurrence of an appointment
     * 
     * @return the recurrence
     */
    public ReminderObject getReminder() {
        return (ReminderObject) getItem( FIELD_REMINDER ).getValue();
    }

    /**
     * Returns the attendees of an appointment
     * 
     * @return the attendees
     */
    public AttendeeObject[] getAttendees() {
        AttendeeObject[] attendees = null;

        try {
            Object attendeesObject = getItem( FIELD_ATTENDEES ).getValue();

            if( attendeesObject instanceof Scriptable ) {
                Scriptable attendeesArray = (Scriptable) attendeesObject;
                int length = ( (Integer) attendeesArray.getField( "length" ) ).intValue();
                attendees = new AttendeeObject[ length ];

                for( int i = 0; i < length; i++ ) {
                    AttendeeObject attendee = (AttendeeObject) attendeesArray.getElement( i );
                    attendees[ i ] = attendee;
                }
            } else if( attendeesObject instanceof AttendeeObject[] ) {
                attendees = (AttendeeObject[]) attendeesObject;
            }
        } catch( Exception ex ) {
            return null;
        }

        return attendees;
    }

    /**
     * Returns whether to show the time as free or busy
     * 
     * @return free busy
     */
    public int getFreeBusy() {
        Integer i = (Integer) getItem( FIELD_FREEBUSY ).getValue();
        return i.intValue();
    }

    /**
     * Returns whether the appointment is an all-day event
     * 
     * @return all day
     */
    public boolean isAllDay() {
        Boolean b = (Boolean) getItem( FIELD_ALLDAY ).getValue();
        return b.booleanValue();
    }

    /**
     * Returns the event field of an appointment
     * 
     * @return the event
     */
    public Event getEvent() {
        return _event;
    }

    /**
     * This class implements the save function of an Appointment
     * 
     */
    public class AppointmentSaveScriptableFunction extends ScriptableFunctionBase {
        private final AppointmentObject _that;
        private static final int HOURS_OF_DAY = 24;
        private static final long ONE_DAY_IN_MILLISECS = 86400000;
        private static final int ONE_HOUR_IN_MILLISECS = 3600000;
        private static final String SOFTWARE_VERSION_SIX = "6";

        /**
         * Default constructor of the AppointmentSaveScriptableFunction
         */
        public AppointmentSaveScriptableFunction() {
            super();
            _that = AppointmentObject.this;
        }

        private void adjustDates( Date startDate, Date endDate ) {
            // this method compensates the start/end dates offset introduced by a Java API bug.
            // The method and its caller will be removed when the bug gets fix in the Java APIs.
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            Calendar rightnow = Calendar.getInstance( TimeZone.getDefault() );
            rightnow.setTime( startDate );

            int hourOfTheDay = rightnow.get( Calendar.HOUR_OF_DAY );

            int offsetDST = TimeZone.getDefault().getOffset( 1, rightnow.get( Calendar.YEAR ), rightnow.get( Calendar.MONTH ),
                    rightnow.get( Calendar.DAY_OF_MONTH ), rightnow.get( Calendar.DAY_OF_WEEK ),
                    rightnow.get( Calendar.MILLISECOND ) )
                    / ONE_HOUR_IN_MILLISECS;
            if( offsetDST < 0 ) {
                if( hourOfTheDay >= ( HOURS_OF_DAY + offsetDST ) ) {
                    startDate.setTime( startTime - ONE_DAY_IN_MILLISECS );
                    endDate.setTime( endTime - ONE_DAY_IN_MILLISECS );
                }
            } else if( offsetDST > 0 ) {
                if( hourOfTheDay <= offsetDST ) {
                    startDate.setTime( startTime + ONE_DAY_IN_MILLISECS );
                    endDate.setTime( endTime + ONE_DAY_IN_MILLISECS );
                }
            }
        }

        /**
         * Implements the update functionality
         * 
         * @throws Exception
         */
        public void update() throws Exception {
            String location = _that.getLocation();
            String summary = _that.getSummary();
            String note = _that.getNote();

            // Start
            Date start = _that.getStart();

            // End
            Date end = _that.getEnd();

            // Recurrence
            RecurrenceObject recurrenceObj = _that.getRecurrence();
            RepeatRule repeatRule = null;

            if( recurrenceObj != null ) {
                repeatRule = new RepeatRule();
                Integer i;
                Date d;

                // count
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_COUNT ).getValue();
                int count = i.intValue();
                if( count >= 0 ) {
                    repeatRule.setInt( RepeatRule.COUNT, count );
                }

                // frequency
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_FREQUENCY ).getValue();
                int freq = i.intValue();
                repeatRule.setInt( RepeatRule.FREQUENCY, RecurrenceObject.frequencyToRepeatRule( freq ) );

                // interval
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_INTERVAL ).getValue();
                int interval = i.intValue();
                if( interval > 0 ) {
                    repeatRule.setInt( RepeatRule.INTERVAL, interval );
                }

                // end
                d = (Date) recurrenceObj.getItem( RecurrenceObject.FIELD_END ).getValue();
                if( d != null ) {
                    long endTime = d.getTime();
                    repeatRule.setDate( RepeatRule.END, endTime );
                }

                // monthInYear
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_MONTHINYEAR ).getValue();
                int monthInYear = i.intValue();
                if( monthInYear > 0 ) {
                    repeatRule.setInt( RepeatRule.MONTH_IN_YEAR, monthInYear );
                }

                // weekInMonth
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_WEEKINMONTH ).getValue();
                int weekInMonth = i.intValue();
                if( weekInMonth > 0 ) {
                    repeatRule.setInt( RepeatRule.WEEK_IN_MONTH, weekInMonth );
                }

                // dayInWeek
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_DAYINWEEK ).getValue();
                int dayInWeek = i.intValue();
                if( dayInWeek > 0 ) {
                    repeatRule.setInt( RepeatRule.DAY_IN_WEEK, dayInWeek );
                }

                // dayInMonth
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_DAYINMONTH ).getValue();
                int dayInMonth = i.intValue();
                if( dayInMonth > 0 ) {
                    repeatRule.setInt( RepeatRule.DAY_IN_MONTH, dayInMonth );
                }

                // dayInYear
                i = (Integer) recurrenceObj.getItem( RecurrenceObject.FIELD_DAYINYEAR ).getValue();
                int dayInYear = i.intValue();
                if( dayInYear > 0 ) {
                    repeatRule.setInt( RepeatRule.DAY_IN_YEAR, dayInYear );
                }
            }

            // Reminder
            ReminderObject reminderObj = _that.getReminder();

            // Attendees
            AttendeeObject[] attendees = _that.getAttendees();

            // FreeBusy
            int freeBusy = _that.getFreeBusy();

            // AllDay
            boolean allday = _that.isAllDay();

            // Create a new appointment

            EventList eventList;

            if( _serviceName.length() > 0 ) {
                eventList = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE, _serviceName );
            } else {
                eventList = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE );
            }

            if( _event == null )
                _event = eventList.createEvent();

            if( _event.countValues( Event.LOCATION ) > 0 ) {
                if( location.length() > 0 ) {
                    _event.setString( Event.LOCATION, 0, Event.ATTR_NONE, location );
                } else {
                    _event.removeValue( Event.LOCATION, 0 );
                }
            } else {
                if( location.length() > 0 ) {
                    _event.addString( Event.LOCATION, Event.ATTR_NONE, location );
                }
            }

            if( _event.countValues( Event.SUMMARY ) > 0 ) {
                if( summary.length() > 0 ) {
                    _event.setString( Event.SUMMARY, 0, Event.ATTR_NONE, summary );
                } else {
                    _event.removeValue( Event.SUMMARY, 0 );
                }
            } else {
                if( summary.length() > 0 ) {
                    _event.addString( Event.SUMMARY, Event.ATTR_NONE, summary );
                }
            }

            if( _event.countValues( Event.NOTE ) > 0 ) {
                if( note.length() > 0 ) {
                    _event.setString( Event.NOTE, 0, Event.ATTR_NONE, note );
                } else {
                    _event.removeValue( Event.NOTE, 0 );
                }
            } else {
                if( note.length() > 0 ) {
                    _event.addString( Event.NOTE, Event.ATTR_NONE, note );
                }
            }

            // patch for compensating the one day offset in the calendar for all 6.0 simulator bundles, for all-day events and
            // WEEKLY and MONTHLY appointments
            String softwareVersion = DeviceInfo.getSoftwareVersion();
            if( softwareVersion.startsWith( SOFTWARE_VERSION_SIX ) ) {
                if( allday ) {
                    adjustDates( start, end );
                } else if( repeatRule != null ) {
                    int frequency = repeatRule.getInt( RepeatRule.FREQUENCY );
                    if( frequency == RepeatRule.WEEKLY || frequency == RepeatRule.MONTHLY ) {
                        adjustDates( start, end );
                    }
                }
            } // this temporary patch should be removed when the one-day offset bug gets fixed in the Java APIs

            if( _event.countValues( Event.START ) > 0 ) {
                if( start != null ) {
                    _event.setDate( Event.START, 0, Event.ATTR_NONE, start.getTime() );
                } else {
                    _event.removeValue( Event.START, 0 );
                }
            } else {
                if( start != null ) {
                    _event.addDate( Event.START, Event.ATTR_NONE, start.getTime() );
                }
            }

            if( _event.countValues( Event.END ) > 0 ) {
                if( end != null ) {
                    _event.setDate( Event.END, 0, Event.ATTR_NONE, end.getTime() );
                } else {
                    _event.removeValue( Event.END, 0 );
                }
            } else {
                if( end != null ) {
                    _event.addDate( Event.END, Event.ATTR_NONE, end.getTime() );
                }
            }

            // recurrence
            _event.setRepeat( repeatRule );

            // reminder
            if( reminderObj != null ) {
                int reminderType = reminderObj.getType();
                int reminderRelativeTime;

                if( reminderType != ReminderConstructor.TYPE_RELATIVE ) {
                    throw new IllegalArgumentException( "The type of reminder of appointment should be relative!" );
                    // reminderRelativeTime = (int) (reminderObj.getDate().getTime()-start.getTime());
                }

                reminderRelativeTime = (int) ( reminderObj.getRelativeHours() * 3600 ); // 1 hr = 3600 sec

                if( _event.countValues( Event.ALARM ) > 0 ) {
                    if( reminderRelativeTime > 0 ) {
                        _event.setInt( Event.ALARM, 0, Event.ATTR_NONE, reminderRelativeTime );
                    } else {
                        _event.removeValue( Event.ALARM, 0 );
                    }
                } else {
                    if( reminderRelativeTime > 0 ) {
                        _event.addInt( Event.ALARM, Event.ATTR_NONE, reminderRelativeTime );
                    }
                }
            } else {
                if( _event.countValues( Event.ALARM ) > 0 ) {
                    _event.removeValue( Event.ALARM, 0 );
                }
            }

            // attendees
            // remove all old attendees
            int countAttendees = _event.countValues( BlackBerryEvent.ATTENDEES );
            for( int j = 0; j < countAttendees; j++ ) {
                _event.removeValue( BlackBerryEvent.ATTENDEES, 0 );
            }

            if( attendees != null ) {
                for( int i = 0; i < attendees.length; i++ ) {
                    _event.addString( BlackBerryEvent.ATTENDEES, BlackBerryEvent.ATTR_NONE, attendees[ i ].getAddress() );
                }
            }

            if( _event.countValues( BlackBerryEvent.FREE_BUSY ) > 0 ) {
                _event.setInt( BlackBerryEvent.FREE_BUSY, 0, BlackBerryEvent.ATTR_NONE, freeBusy );
            } else {
                _event.addInt( BlackBerryEvent.FREE_BUSY, BlackBerryEvent.ATTR_NONE, freeBusy );
            }

            if( _event.countValues( BlackBerryEvent.ALLDAY ) > 0 ) {
                _event.setBoolean( BlackBerryEvent.ALLDAY, 0, BlackBerryEvent.ATTR_NONE, allday );
            } else {
                _event.addBoolean( BlackBerryEvent.ALLDAY, BlackBerryEvent.ATTR_NONE, allday );
            }
        }

        /**
         * Commits the changes
         * 
         * @throws Exception
         */
        public void commit() throws Exception {
            if( _event == null ) {
                return;
            }

            if( _event.isModified() ) {
                _event.commit();
            }

            // uid
            final String uid = _event.getString( Event.UID, 0 );
            _that.getItem( AppointmentObject.FIELD_UID ).setValue( uid );
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
            update();
            commit();
            return UNDEFINED;
        }
    }

    private ScriptableFunction createSaveMethod() {
        _save = new AppointmentSaveScriptableFunction();
        return _save;
    }

    private ScriptableFunction createRemoveMethod() {
        return new ScriptableFunctionBase() {
            public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
                if( _event == null ) {
                    throw new PIMException( "PIMItem not found." );
                }

                EventList eventList;
                if( _serviceName.length() == 0 ) {
                    eventList = (EventList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.EVENT_LIST,
                            BlackBerryPIM.WRITE_ONLY );
                } else {
                    eventList = (EventList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.EVENT_LIST,
                            BlackBerryPIM.WRITE_ONLY, _serviceName );
                }

                eventList.removeEvent( _event );
                _event = null;

                return UNDEFINED;
            };
        };
    }

    /**
     * The update for an AppointmentObject
     * 
     * @throws Exception
     */
    public void update() throws Exception {
        _save.update();
    }

    /**
     * The save for an AppointmentObject
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        _save.execute( null, null );
    }
}

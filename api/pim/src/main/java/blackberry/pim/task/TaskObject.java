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
package blackberry.pim.task;

import java.util.Date;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.RepeatRule;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;

import net.rim.blackberry.api.pdap.BlackBerryPIM;
import net.rim.blackberry.api.pdap.BlackBerryToDo;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.service.ServiceObject;
import blackberry.pim.PIMUtils;
import blackberry.pim.category.CategoryNamespace;
import blackberry.pim.recurrence.RecurrenceObject;
import blackberry.pim.reminder.ReminderConstructor;
import blackberry.pim.reminder.ReminderObject;

/**
 * This class represents a Task
 * 
 * @author dmateescu
 */
public class TaskObject extends ScriptableObjectBase {

    private ToDo _todo;
    private String _serviceName;

    private TaskSaveScriptableFunction _save;
    private TaskRemoveScriptableFunction _remove;

    public static final String FIELD_SUMMARY = "summary";
    public static final String FIELD_NOTE = "note";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_DUE = "due";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_RECURRENCE = "recurrence";
    public static final String FIELD_REMINDER = "reminder";
    public static final String FIELD_CATEGORIES = "categories";

    public static final String METHOD_SAVE = "save";
    public static final String METHOD_REMOVE = "remove";

    /**
     * Default constructor of a TaskObject
     */
    public TaskObject() {
        super();
        _todo = null;
        _serviceName = "";
        initialialize();
    }

    /**
     * Constructs a TaskObject based on a ToDo
     * 
     * @param t
     *            the ToDo
     */
    public TaskObject( ToDo t ) {
        super();
        _todo = t;
        _serviceName = "";
        initialialize();
    }

    /**
     * Constructs a TaskObject based on a ServiceObject
     * 
     * @param s
     *            the ServiceObject
     */
    public TaskObject( ServiceObject s ) {
        super();
        _todo = null;
        _serviceName = s.getName();
        initialialize();
    }

    /**
     * Constructs a TaskObject based on a ToDo and a ServiceObject
     * 
     * @param t
     *            the ToDo
     * @param s
     *            the ServiceObject
     */
    public TaskObject( ToDo t, ServiceObject s ) {
        super();
        _todo = t;
        _serviceName = s.getName();
        initialialize();
    }

    /**
     * The implementation of the save method which will save the changes made to the Task object.
     * 
     */
    public class TaskSaveScriptableFunction extends ScriptableFunctionBase {
        private final TaskObject _outer;

        /**
         * Default constructor of the save method
         */
        public TaskSaveScriptableFunction() {
            super();
            _outer = TaskObject.this;
        }

        /**
         * This method updates the ToDo object
         * 
         * @throws Exception
         */
        public void update() throws Exception {
            ToDoList todoList;
            if( _serviceName.length() == 0 ) {
                todoList = (ToDoList) BlackBerryPIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.READ_WRITE );
            } else {
                todoList = (ToDoList) BlackBerryPIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.READ_WRITE, _serviceName );
            }

            if( _todo == null ) {
                _todo = (ToDo) todoList.createToDo();
            }

            // summary
            String value;
            value = _outer.getItem( TaskObject.FIELD_SUMMARY ).getStringValue();
            if( _todo.countValues( ToDo.SUMMARY ) == 0 ) {
                if( value.length() > 0 ) {
                    _todo.addString( ToDo.SUMMARY, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _todo.setString( ToDo.SUMMARY, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _todo.removeValue( ToDo.SUMMARY, 0 );
                }
            }

            // note
            value = _outer.getItem( TaskObject.FIELD_NOTE ).getStringValue();
            if( _todo.countValues( ToDo.NOTE ) == 0 ) {
                if( value.length() > 0 ) {
                    _todo.addString( ToDo.NOTE, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _todo.setString( ToDo.NOTE, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _todo.removeValue( ToDo.NOTE, 0 );
                }
            }

            // status
            Integer i = (Integer) _outer.getItem( TaskObject.FIELD_STATUS ).getValue();
            int intValue = i.intValue();
            if( _todo.countValues( BlackBerryToDo.STATUS ) == 0 ) {
                _todo.addInt( BlackBerryToDo.STATUS, PIMItem.ATTR_NONE, intValue );
            } else {
                _todo.setInt( BlackBerryToDo.STATUS, 0, PIMItem.ATTR_NONE, intValue );
            }

            // due
            Date d = (Date) _outer.getItem( TaskObject.FIELD_DUE ).getValue();
            if( _todo.countValues( BlackBerryToDo.DUE ) == 0 ) {
                if( d != null ) {
                    _todo.addDate( BlackBerryToDo.DUE, PIMItem.ATTR_NONE, d.getTime() );
                }
            } else {
                if( d != null ) {
                    _todo.setDate( BlackBerryToDo.DUE, 0, PIMItem.ATTR_NONE, d.getTime() );
                } else {
                    _todo.removeValue( BlackBerryToDo.DUE, 0 );
                }
            }

            // priority
            i = (Integer) _outer.getItem( TaskObject.FIELD_PRIORITY ).getValue();
            intValue = TaskConstructor.taskPriorityToTodoPriority( i.intValue() );
            if( _todo.countValues( ToDo.PRIORITY ) == 0 ) {
                _todo.addInt( ToDo.PRIORITY, PIMItem.ATTR_NONE, intValue );
            } else {
                _todo.setInt( ToDo.PRIORITY, 0, PIMItem.ATTR_NONE, intValue );
            }

            // recurrence
            RecurrenceObject recurObject = (RecurrenceObject) _outer.getItem( TaskObject.FIELD_RECURRENCE ).getValue();
            if( recurObject != null ) {
                BlackBerryToDo bbTodo = (BlackBerryToDo) _todo;
                RepeatRule repeat = new RepeatRule();

                // count
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_COUNT ).getValue();
                int count = i.intValue();
                if( count >= 0 ) {
                    repeat.setInt( RepeatRule.COUNT, count );
                }

                // frequency
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_FREQUENCY ).getValue();
                int freq = i.intValue();
                repeat.setInt( RepeatRule.FREQUENCY, RecurrenceObject.frequencyToRepeatRule( freq ) );

                // interval
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_INTERVAL ).getValue();
                int interval = i.intValue();
                if( interval > 0 ) {
                    repeat.setInt( RepeatRule.INTERVAL, interval );
                }

                // end
                d = (Date) recurObject.getItem( RecurrenceObject.FIELD_END ).getValue();
                if( d != null ) {
                    long endTime = d.getTime();
                    repeat.setDate( RepeatRule.END, endTime );
                }

                // monthInYear
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_MONTHINYEAR ).getValue();
                int monthInYear = i.intValue();
                if( monthInYear > 0 ) {
                    repeat.setInt( RepeatRule.MONTH_IN_YEAR, monthInYear );
                }

                // weekInMonth
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_WEEKINMONTH ).getValue();
                int weekInMonth = i.intValue();
                if( weekInMonth > 0 ) {
                    repeat.setInt( RepeatRule.WEEK_IN_MONTH, weekInMonth );
                }

                // dayInWeek
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_DAYINWEEK ).getValue();
                int dayInWeek = i.intValue();
                if( dayInWeek > 0 ) {
                    repeat.setInt( RepeatRule.DAY_IN_WEEK, dayInWeek );
                }

                // dayInMonth
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_DAYINMONTH ).getValue();
                int dayInMonth = i.intValue();
                if( dayInMonth > 0 ) {
                    repeat.setInt( RepeatRule.DAY_IN_MONTH, dayInMonth );
                }

                // dayInYear
                i = (Integer) recurObject.getItem( RecurrenceObject.FIELD_DAYINYEAR ).getValue();
                int dayInYear = i.intValue();
                if( dayInYear > 0 ) {
                    repeat.setInt( RepeatRule.DAY_IN_YEAR, dayInYear );
                }

                bbTodo.setRepeat( repeat );
            }

            // reminder
            long reminderTime = -1L;
            boolean isAbsoluteReminder = true;
            if( _todo.countValues( BlackBerryToDo.REMINDER ) > 0 ) {
                reminderTime = _todo.getDate( BlackBerryToDo.REMINDER, 0 );
                if( reminderTime <= 1000 * 3600 * 24 * 7 * 2 ) // if reminderTime is less then 2 weeks time, it's a relative time
                {
                    isAbsoluteReminder = false;
                }
            }

            ReminderObject reminderObject = (ReminderObject) _outer.getItem( TaskObject.FIELD_REMINDER ).getValue();
            reminderTime = -1L;
            if( reminderObject != null ) {
                i = (Integer) reminderObject.getItem( ReminderObject.FIELD_TYPE ).getValue();
                int type = i.intValue();

                // Relative
                if( type == ReminderConstructor.TYPE_RELATIVE ) {
                    double dbl = reminderObject.getRelativeHours();
                    if( isAbsoluteReminder ) {
                        long relativeMilliSecond = (long) ( dbl * 3600 * 1000 );
                        Date due = (Date) _outer.getItem( TaskObject.FIELD_DUE ).getValue();
                        if( due != null ) {
                            reminderTime = due.getTime() - relativeMilliSecond;
                        }
                    } else {
                        reminderTime = (long) ( dbl * 3600 * 1000 );
                    }
                } else {
                    d = (Date) reminderObject.getItem( ReminderObject.FIELD_DATE ).getValue();
                    if( isAbsoluteReminder ) {
                        reminderTime = d.getTime();
                    } else {
                        Date due = (Date) _outer.getItem( TaskObject.FIELD_DUE ).getValue();
                        if( due != null ) {
                            reminderTime = due.getTime() - d.getTime();
                        }
                    }
                }
            }

            if( _todo.countValues( BlackBerryToDo.REMINDER ) == 0 ) {
                if( reminderTime >= 0 ) {
                    _todo.addDate( BlackBerryToDo.REMINDER, PIMItem.ATTR_NONE, reminderTime );
                }
            } else {
                if( reminderTime >= 0 ) {
                    _todo.setDate( BlackBerryToDo.REMINDER, 0, PIMItem.ATTR_NONE, reminderTime );
                } else {
                    _todo.removeValue( BlackBerryToDo.REMINDER, 0 );
                }
            }

            // categories
            CategoryNamespace.updateCategories( _todo, todoList, _outer );
        }

        /**
         * This method commits the changes while saving a Task object
         * 
         * @throws Exception
         */
        public void commit() throws Exception {
            if( _todo == null ) {
                return;
            }
            // commit the task
            _todo.commit();
            // uid
            final String uid = _todo.getString( ToDo.UID, 0 );
            _outer.getItem( TaskObject.FIELD_UID ).setValue( uid );
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

    /**
     * This method implements the remove function of a task, which will remove a Task from the PIM storage.
     * 
     */
    public class TaskRemoveScriptableFunction extends ScriptableFunctionBase {

        /**
         * Default constructor of the remove method
         */
        public TaskRemoveScriptableFunction() {
            super();
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
            // if the _todo is not in ToDoList, do nothing
            if( _todo == null ) {
                throw new PIMException( "PIMItem not found." );
            }

            // open the handheld todos database for remove
            ToDoList todoList;
            if( _serviceName.length() == 0 ) {
                todoList = (ToDoList) BlackBerryPIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.WRITE_ONLY );
            } else {
                todoList = (ToDoList) BlackBerryPIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.WRITE_ONLY, _serviceName );
            }
            todoList.removeToDo( _todo );
            _todo = null;
            return UNDEFINED;
        }
    }

    private void initialialize() {
        if( _todo != null ) {
            // summary
            if( _todo.countValues( ToDo.SUMMARY ) > 0 ) {
                addItem( new ScriptField( FIELD_SUMMARY, _todo.getString( ToDo.SUMMARY, 0 ), ScriptField.TYPE_STRING, false,
                        false ) );
            } else {
                addItem( new ScriptField( FIELD_SUMMARY, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // note
            if( _todo.countValues( ToDo.NOTE ) > 0 ) {
                addItem( new ScriptField( FIELD_NOTE, _todo.getString( ToDo.NOTE, 0 ), ScriptField.TYPE_STRING, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // status
            if( _todo.countValues( BlackBerryToDo.STATUS ) > 0 ) {
                addItem( new ScriptField( FIELD_STATUS, new Integer( _todo.getInt( BlackBerryToDo.STATUS, 0 ) ),
                        ScriptField.TYPE_INT, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_STATUS, new Integer( TaskConstructor.STATUS_VALUE_NOT_STARTED ),
                        ScriptField.TYPE_INT, false, false ) );
            }

            // due
            if( _todo.countValues( ToDo.DUE ) > 0 ) {
                addItem( new ScriptField( FIELD_DUE, new Date( _todo.getDate( ToDo.DUE, 0 ) ), ScriptField.TYPE_DATE, false,
                        false ) );
            } else {
                addItem( new ScriptField( FIELD_DUE, null, ScriptField.TYPE_DATE, false, false ) );
            }

            // priority
            if( _todo.countValues( ToDo.PRIORITY ) > 0 ) {
                addItem( new ScriptField( FIELD_PRIORITY, new Integer( TaskConstructor.todoPriorityToTaskPriority( _todo.getInt(
                        ToDo.PRIORITY, 0 ) ) ), ScriptField.TYPE_INT, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_PRIORITY, new Integer( TaskConstructor.VALUE_PRIORITY_NORMAL ),
                        ScriptField.TYPE_INT, false, false ) );
            }

            // uid
            if( _todo.countValues( ToDo.UID ) > 0 ) {
                addItem( new ScriptField( FIELD_UID, _todo.getString( ToDo.UID, 0 ), ScriptField.TYPE_STRING, true, false ) );
            } else {
                addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );
            }

            // recurrence
            BlackBerryToDo bbTodo = (BlackBerryToDo) _todo;
            RepeatRule repeat = bbTodo.getRepeat();
            if( repeat != null ) {
                RecurrenceObject recurObject = new RecurrenceObject();

                int count = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.COUNT );
                recurObject.getItem( RecurrenceObject.FIELD_COUNT ).setValue( new Integer( count ) );

                int freq = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.FREQUENCY );
                recurObject.getItem( RecurrenceObject.FIELD_FREQUENCY ).setValue(
                        new Integer( RecurrenceObject.repeatRuleToFrequency( freq ) ) );

                int interval = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.INTERVAL );
                recurObject.getItem( RecurrenceObject.FIELD_INTERVAL ).setValue( new Integer( interval ) );

                long endTime = PIMUtils.getRepeatRuleDate( repeat, RepeatRule.END );
                recurObject.getItem( RecurrenceObject.FIELD_END ).setValue( new Date( endTime ) );

                int monthInYear = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.MONTH_IN_YEAR );
                recurObject.getItem( RecurrenceObject.FIELD_MONTHINYEAR ).setValue( new Integer( monthInYear ) );

                int weekInMonth = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.WEEK_IN_MONTH );
                recurObject.getItem( RecurrenceObject.FIELD_WEEKINMONTH ).setValue( new Integer( weekInMonth ) );

                int dayInWeek = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.DAY_IN_WEEK );
                recurObject.getItem( RecurrenceObject.FIELD_DAYINWEEK ).setValue( new Integer( dayInWeek ) );

                int dayInMonth = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.DAY_IN_MONTH );
                recurObject.getItem( RecurrenceObject.FIELD_DAYINMONTH ).setValue( new Integer( dayInMonth ) );

                int dayInYear = PIMUtils.getRepeatRuleInt( repeat, RepeatRule.DAY_IN_YEAR );
                recurObject.getItem( RecurrenceObject.FIELD_DAYINYEAR ).setValue( new Integer( dayInYear ) );

                addItem( new ScriptField( FIELD_RECURRENCE, recurObject, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_RECURRENCE, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            }

            // reminder
            if( _todo.countValues( BlackBerryToDo.REMINDER ) > 0 ) {
                ReminderObject remindObject = new ReminderObject();

                long reminderTime = _todo.getDate( BlackBerryToDo.REMINDER, 0 );
                if( reminderTime <= 1000 * 3600 * 24 * 7 * 2 ) // if reminderTime is less then 2 weeks time, it's a relative time
                {
                    remindObject.getItem( ReminderObject.FIELD_TYPE ).setValue( new Integer( ReminderConstructor.TYPE_RELATIVE ) );
                    double relativeHours = (double) ( (double) reminderTime ) / 3600 / 1000;
                    remindObject.getItem( ReminderObject.FIELD_RELATIVE_HOURS ).setValue( new Double( relativeHours ) );
                } else { // it's a absolute time
                    remindObject.getItem( ReminderObject.FIELD_TYPE ).setValue( new Integer( ReminderConstructor.TYPE_DATE ) );
                    remindObject.getItem( ReminderObject.FIELD_DATE ).setValue( new Date( reminderTime ) );
                }

                addItem( new ScriptField( FIELD_REMINDER, remindObject, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_REMINDER, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            }

            // categories
            addItem( new ScriptField( FIELD_CATEGORIES, _todo.getCategories(), ScriptField.TYPE_SCRIPTABLE, false, false ) );
        } else {
            addItem( new ScriptField( FIELD_SUMMARY, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_STATUS, new Integer( TaskConstructor.STATUS_VALUE_NOT_STARTED ),
                    ScriptField.TYPE_INT, false, false ) );
            addItem( new ScriptField( FIELD_DUE, null, ScriptField.TYPE_DATE, false, false ) );
            addItem( new ScriptField( FIELD_PRIORITY, new Integer( TaskConstructor.VALUE_PRIORITY_NORMAL ), ScriptField.TYPE_INT,
                    false, false ) );
            addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );

            addItem( new ScriptField( FIELD_RECURRENCE, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            addItem( new ScriptField( FIELD_REMINDER, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            addItem( new ScriptField( FIELD_CATEGORIES, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
        }

        // Methods
        _save = new TaskSaveScriptableFunction();
        _remove = new TaskRemoveScriptableFunction();

        addItem( new ScriptField( METHOD_SAVE, _save, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_REMOVE, _remove, ScriptField.TYPE_SCRIPTABLE, true, true ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * This method returns the ToDoObject of a task
     * 
     * @return the todo
     */
    public ToDo getTodo() {
        return _todo;
    }

    /**
     * This method updates the changes made to a task
     * 
     * @throws Exception
     */
    public void update() throws Exception {
        _save.update();
    }

    /**
     * This method saves a task
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        _save.execute( null, null );
    }

    /**
     * This method returns the status of a task
     * 
     * @return the status
     */
    public int getStatus() {
        Integer i = (Integer) getItem( FIELD_STATUS ).getValue();
        return i.intValue();
    }

    /**
     * This method returns the summary of a task
     * 
     * @return the summary
     */
    public String getSummary() {
        return (String) getItem( FIELD_SUMMARY ).getValue();
    }

    /**
     * This method returns the note of a task
     * 
     * @return the note
     */
    public String getNote() {
        return (String) getItem( FIELD_NOTE ).getValue();
    }

    /**
     * This method returns the due date of a task
     * 
     * @return the due
     */
    public Date getDue() {
        return (Date) getItem( FIELD_DUE ).getValue();
    }

    /**
     * This method returns the uid of a task
     * 
     * @return the uid
     */
    public String getUID() {
        return (String) getItem( FIELD_UID ).getValue();
    }

    /**
     * This method returns the recurrence of a tasks
     * 
     * @return the recurrence
     */
    public RecurrenceObject getRecurrence() {
        return (RecurrenceObject) getItem( FIELD_RECURRENCE ).getValue();
    }

    /**
     * This method returns the reminder of a task
     * 
     * @return the reminder
     */
    public ReminderObject getReminder() {
        return (ReminderObject) getItem( FIELD_REMINDER ).getValue();
    }

    /**
     * This method returns the categories of a task
     * 
     * @return the categories
     * @throws Exception
     *             , when categories cannot be obtained
     */
    public String[] getCategories() throws Exception {
        return CategoryNamespace.getCategoriesFromScriptField( getItem( FIELD_CATEGORIES ) );
    }
}

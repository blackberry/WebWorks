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

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.service.ServiceObject;

/**
 * This class represents the constructor of a Task
 * 
 * @author dmateescu
 */
public class TaskConstructor extends ScriptableFunctionBase {
    public static final String NAME = "blackberry.pim.Task";

    public static final String STATUS_LABEL_NOT_STARTED = "NOT_STARTED";
    public static final String STATUS_LABEL_COMPLETED = "COMPLETED";
    public static final String STATUS_LABEL_DEFERRED = "DEFERRED";
    public static final String STATUS_LABEL_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_LABEL_WAITING = "WAITING";

    public static final int STATUS_VALUE_NOT_STARTED = 0;
    public static final int STATUS_VALUE_IN_PROGRESS = 1;
    public static final int STATUS_VALUE_COMPLETED = 2;
    public static final int STATUS_VALUE_WAITING = 3;
    public static final int STATUS_VALUE_DEFERRED = 4;

    public static final String LABEL_PRIORITY_HIGH = "PRIORITY_HIGH";
    public static final String LABEL_PRIORITY_NORMAL = "PRIORITY_NORMAL";
    public static final String LABEL_PRIORITY_LOW = "PRIORITY_LOW";

    public static final int VALUE_PRIORITY_HIGH = 0;
    public static final int VALUE_PRIORITY_NORMAL = 1;
    public static final int VALUE_PRIORITY_LOW = 2;

    public static final int TODO_VALUE_PRIORITY_HIGH = 1;
    public static final int TODO_VALUE_PRIORITY_NORMAL = 5;
    public static final int TODO_VALUE_PRIORITY_LOW = 9;

    private TaskFindScriptableFunction _find;

    /**
     * Default constructor of the TaskConstructor
     */
    public TaskConstructor() {
        _find = new TaskFindScriptableFunction();
    }

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args != null && args.length == 1 ) {
            ServiceObject serviceObject = (ServiceObject) args[ 0 ];
            return new TaskObject( serviceObject );
        }
        return new TaskObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( STATUS_LABEL_NOT_STARTED ) ) {
            return new Integer( STATUS_VALUE_NOT_STARTED );
        } else if( name.equals( STATUS_LABEL_COMPLETED ) ) {
            return new Integer( STATUS_VALUE_COMPLETED );
        } else if( name.equals( STATUS_LABEL_DEFERRED ) ) {
            return new Integer( STATUS_VALUE_DEFERRED );
        } else if( name.equals( STATUS_LABEL_IN_PROGRESS ) ) {
            return new Integer( STATUS_VALUE_IN_PROGRESS );
        } else if( name.equals( STATUS_LABEL_WAITING ) ) {
            return new Integer( STATUS_VALUE_WAITING );
        } else if( name.equals( LABEL_PRIORITY_HIGH ) ) {
            return new Integer( VALUE_PRIORITY_HIGH );
        } else if( name.equals( LABEL_PRIORITY_NORMAL ) ) {
            return new Integer( VALUE_PRIORITY_NORMAL );
        } else if( name.equals( LABEL_PRIORITY_LOW ) ) {
            return new Integer( VALUE_PRIORITY_LOW );
        } else if( name.equals( TaskFindScriptableFunction.NAME ) ) {
            return _find;
        }
        return UNDEFINED;
    }

    /**
     * This method converts a task priority into a todo priority
     * 
     * @param priority
     *            the priority
     * @return the todo priority
     */
    public static int taskPriorityToTodoPriority( int priority ) {
        switch( priority ) {
            case VALUE_PRIORITY_HIGH:
                return TODO_VALUE_PRIORITY_HIGH;
            case VALUE_PRIORITY_NORMAL:
                return TODO_VALUE_PRIORITY_NORMAL;
            case VALUE_PRIORITY_LOW:
                return TODO_VALUE_PRIORITY_LOW;
            default:
                return TODO_VALUE_PRIORITY_NORMAL;
        }
    }

    /**
     * This method converts a todo priority into a task priority
     * 
     * @param priority
     *            the todo priority
     * @return the priority
     */
    public static int todoPriorityToTaskPriority( int priority ) {
        switch( priority ) {
            case 1:
            case 2:
            case 3:
                return VALUE_PRIORITY_HIGH;
            case 0:
            case 4:
            case 5:
            case 6:
                return VALUE_PRIORITY_NORMAL;
            case 7:
            case 8:
            case 9:
                return VALUE_PRIORITY_LOW;
            default:
                return VALUE_PRIORITY_NORMAL;
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( ServiceObject.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        return UNDEFINED;
    }
}

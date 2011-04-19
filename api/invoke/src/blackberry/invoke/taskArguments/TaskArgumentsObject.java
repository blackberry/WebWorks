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
package blackberry.invoke.taskArguments;

import javax.microedition.pim.ToDo;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.pim.task.TaskObject;

/**
 * This class represents the TaskArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class TaskArgumentsObject extends ScriptableObjectBase {
    private final TaskObject _taskObject;

    public static final String FIELD_VIEW = "view";

    /**
     * Default constructor, constructs a new TaskArgumentsObject object.
     */
    public TaskArgumentsObject() {
        _taskObject = null;
        initial();
    }

    /**
     * Constructs a new TaskArgumentsObject object.
     * 
     * @param t
     *            Task to open into the Task View
     */
    public TaskArgumentsObject( final TaskObject t ) {
        _taskObject = t;
        initial();
    }

    // Injects fields and methods
    private void initial() {
        addItem( new ScriptField( FIELD_VIEW, new Integer( TaskArgumentsConstructor.VIEW_NEW ), ScriptField.TYPE_INT, false,
                false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the TaskArgumentsObject's underlying content.
     * 
     * @return the reference of input Task Object.
     */
    public TaskObject getTaskObject() {
        return _taskObject;
    }

    /**
     * Internal helper method to get direct access to the TaskArgumentsObject's underlying content.
     * 
     * @return the reference of BlackBerry ToDo Object.
     */
    public ToDo getTodo() {
        if( _taskObject == null ) {
            return null;
        }

        return _taskObject.getTodo();
    }

    /**
     * Internal helper method to get direct access to the TaskArgumentsObject's underlying content.
     * 
     * @return the type of view when opening Tasks application.
     */
    public int getView() {
        final Integer i = (Integer) getItem( FIELD_VIEW ).getValue();
        final int view = i.intValue();
        return view;
    }
}

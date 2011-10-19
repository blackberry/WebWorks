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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.find.FindNamespace;
import blackberry.find.TestableScriptableObject;

/**
 * This class implements the find task functionality
 * 
 * @author dmateescu
 */
public class TaskFindScriptableFunction extends ScriptableFunctionBase {
    public static final String NAME = "find";

    /**
     * Constructs a TaskFindScriptableFunction
     */
    public TaskFindScriptableFunction() {
        super();
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        TaskObject[] tasksFound = new TaskObject[ 0 ];

        TestableScriptableObject testable = null;
        String orderByField = "";
        int maxReturn = -1;
        boolean isAscending = true;

        if( !FindNamespace.isValidFindArguments( args, false ) ) {
            return tasksFound;
        }

        if( args.length > 0 ) {
            testable = (TestableScriptableObject) args[ 0 ];
        }

        if( args.length > 1 ) {
            if( args[ 1 ] != null ) {
                orderByField = (String) args[ 1 ];
            }
        }

        if( args.length > 2 ) {
            if( args[ 2 ] != null ) {
                Integer i = (Integer) args[ 2 ];
                maxReturn = i.intValue();
            }
        }

        if( args.length > 3 ) {
            if( args[ 3 ] != null ) {
                Boolean b = (Boolean) args[ 3 ];
                isAscending = b.booleanValue();
            }
        }

        boolean isSorted = orderByField != null && orderByField.length() > 0 ? true : false;

        ToDoList taskList;
        try {
            taskList = (ToDoList) PIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.READ_WRITE );
        } catch( PIMException pime ) {
            return tasksFound;
        }

        Vector found = new Vector();
        Enumeration e;
        int iElement = 0;
        try {
            e = taskList.items();
            while( e.hasMoreElements() ) {
                ToDo t = (ToDo) e.nextElement();
                TaskObject task = new TaskObject( t );
                if( testable != null ) {
                    if( testable.test( task ) ) {
                        FindNamespace.insertElementByOrder( found, task, orderByField, isAscending );
                        iElement++;
                    }
                } else {
                    FindNamespace.insertElementByOrder( found, task, orderByField, isAscending );
                    iElement++;
                }
                if( !isSorted && iElement == maxReturn ) {
                    break;
                }
            }
        } catch( PIMException pime ) {
            return tasksFound;
        }

        int size = found.size();
        if( maxReturn > 0 && size > maxReturn ) {
            size = maxReturn;
        }
        tasksFound = new TaskObject[ size ];
        for( int i = 0; i < size; i++ ) {
            TaskObject task = (TaskObject) found.elementAt( i );
            tasksFound[ i ] = task;
        }
        return tasksFound;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 4 );
        fs.addNullableParam( TestableScriptableObject.class, false );
        fs.addNullableParam( String.class, false );
        fs.addNullableParam( Integer.class, false );
        fs.addNullableParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

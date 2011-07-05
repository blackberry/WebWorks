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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.find.FindNamespace;
import blackberry.find.TestableScriptableObject;
import blackberry.identity.service.ServiceObject;

/**
 * This class represents the constructor of an Appointment
 * 
 * @author dmateescu
 */
public class AppointmentConstructor extends ScriptableFunctionBase {
    public static final String NAME = "blackberry.pim.Appointment";

    public static final String CONST_FREE = "FREE";
    public static final String CONST_TENTATIVE = "TENTATIVE";
    public static final String CONST_BUSY = "BUSY";
    public static final String CONST_OUT_OF_OFFICE = "OUT_OF_OFFICE";

    public static final Integer CONST_VAL_FREE = new Integer( 0 );
    public static final Integer CONST_VAL_TENTATIVE = new Integer( 1 );
    public static final Integer CONST_VAL_BUSY = new Integer( 2 );
    public static final Integer CONST__VAL_OUT_OF_OFFICE = new Integer( 3 );

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( Object thiz, Object[] args ) throws Exception {
        if( args != null && args.length == 1 ) {
            ServiceObject serviceObject = (ServiceObject) args[ 0 ];
            return new AppointmentObject( serviceObject );
        }
        return new AppointmentObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) throws Exception {
        // Properties
        if( name.equals( CONST_FREE ) ) {
            return CONST_VAL_FREE;
        } else if( name.equals( CONST_TENTATIVE ) ) {
            return CONST_VAL_TENTATIVE;
        } else if( name.equals( CONST_BUSY ) ) {
            return CONST_VAL_BUSY;
        } else if( name.equals( CONST_OUT_OF_OFFICE ) ) {
            return CONST__VAL_OUT_OF_OFFICE;
        }
        // Methods
        if( name.equals( AppointmentObject.METHOD_FIND ) ) {
            return createFindMethod();
        }
        return UNDEFINED;
    }

    private ScriptableFunction createFindMethod() {
        return new ScriptableFunctionBase() {

            /**
             * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
             */
            public Object execute( Object thiz, Object[] args ) throws Exception {
                AppointmentObject[] appointmentsFound = new AppointmentObject[ 0 ];

                TestableScriptableObject testable = null;
                String orderByField = "";
                int maxReturn = -1;
                String serviceName = "";
                boolean isAscending = true;

                if( !FindNamespace.isValidFindArguments( args, true ) ) {
                    return appointmentsFound;
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
                        ServiceObject s = (ServiceObject) args[ 3 ];
                        serviceName = s.getName();
                    }
                }

                if( args.length > 4 ) {
                    if( args[ 4 ] != null ) {
                        Boolean b = (Boolean) args[ 4 ];
                        isAscending = b.booleanValue();
                    }
                }

                boolean isSorted = orderByField != null && orderByField.length() > 0 ? true : false;
                EventList eventList;
                try {
                    if( serviceName.length() == 0 ) {
                        eventList = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE );
                    } else {
                        eventList = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE, serviceName );
                    }
                } catch( PIMException pime ) {
                    return appointmentsFound;
                }
                Vector found = new Vector();
                Enumeration e;
                int iElement = 0;
                try {
                    e = eventList.items();
                    while( e.hasMoreElements() ) {
                        Event evt = (Event) e.nextElement();
                        AppointmentObject appointment = new AppointmentObject( evt );
                        if( testable != null ) {
                            if( testable.test( appointment ) ) {
                                FindNamespace.insertElementByOrder( found, appointment, orderByField, isAscending );
                                iElement++;
                            }
                        } else {
                            FindNamespace.insertElementByOrder( found, appointment, orderByField, isAscending );
                            iElement++;
                        }
                        if( !isSorted && iElement == maxReturn ) {
                            break;
                        }
                    }
                } catch( PIMException pime ) {
                    return appointmentsFound;
                }

                int size = found.size();
                if( maxReturn > 0 && size > maxReturn ) {
                    size = maxReturn;
                }
                appointmentsFound = new AppointmentObject[ size ];
                for( int i = 0; i < size; i++ ) {
                    AppointmentObject appointmnet = (AppointmentObject) found.elementAt( i );
                    appointmentsFound[ i ] = appointmnet;
                }
                return appointmentsFound;
            }

            /**
             * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
             */
            protected FunctionSignature[] getFunctionSignatures() {
                FunctionSignature fs = new FunctionSignature( 5 );
                fs.addNullableParam( TestableScriptableObject.class, false );
                fs.addNullableParam( String.class, false );
                fs.addNullableParam( Integer.class, false );
                fs.addNullableParam( ServiceObject.class, false );
                fs.addNullableParam( Boolean.class, false );
                return new FunctionSignature[] { fs };
            }
        };
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

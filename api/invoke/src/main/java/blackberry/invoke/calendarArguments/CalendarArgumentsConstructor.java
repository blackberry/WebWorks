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

import java.util.Date;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.pim.appointment.AppointmentObject;

/**
 * The CalendarArgumentsConstructor class is used to create new CalendarArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class CalendarArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "CalendarArguments";

    public static final int VIEW_NEW = 0;
    public static final int VIEW_VIEW = 1;
    public static final int VIEW_AGENDA = 2;
    public static final int VIEW_DAY = 3;
    public static final int VIEW_DEFAULT = 4;
    public static final int VIEW_MONTH = 5;
    public static final int VIEW_WEEK = 6;

    public static final String LABEL_VIEW_NEW = "VIEW_NEW";
    public static final String LABEL_VIEW_VIEW = "VIEW_VIEW";
    public static final String LABEL_VIEW_AGENDA = "VIEW_AGENDA";
    public static final String LABEL_VIEW_DAY = "VIEW_DAY";
    public static final String LABEL_VIEW_DEFAULT = "VIEW_DEFAULT";
    public static final String LABEL_VIEW_MONTH = "VIEW_MONTH";
    public static final String LABEL_VIEW_WEEK = "VIEW_WEEK";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        Date d = null;
        if( args == null || args.length == 0 ) {
            return new CalendarArgumentsObject();
        } else if( args[ 0 ] instanceof AppointmentObject ) {
            final AppointmentObject a = (AppointmentObject) args[ 0 ];
            return new CalendarArgumentsObject( a );
        } else if( args[ 0 ] instanceof Scriptable ) {
            // Re-usable method - move somewhere more convenient
            // Converting JS Date from Scriptable to java.util.Date
            try {
                final Scriptable s = (Scriptable) args[ 0 ];
                final Object getTime = s.getField( "getTime" );
                if( getTime instanceof ScriptableFunction ) {
                    final Double millis = (Double) ( (ScriptableFunction) getTime ).invoke( args[ 0 ], new Object[] {} );
                    d = new Date( millis.longValue() );
                }
            } catch( final Exception e ) {
                throw new IllegalArgumentException( "Could not convert date field" );
            }
        } else {
            d = (Date) args[ 0 ];
        }
        return new CalendarArgumentsObject( d );

    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_NEW ) ) {
            return new Integer( VIEW_NEW );
        } else if( name.equals( LABEL_VIEW_VIEW ) ) {
            return new Integer( VIEW_VIEW );
        } else if( name.equals( LABEL_VIEW_AGENDA ) ) {
            return new Integer( VIEW_AGENDA );
        } else if( name.equals( LABEL_VIEW_DAY ) ) {
            return new Integer( VIEW_DAY );
        } else if( name.equals( LABEL_VIEW_DEFAULT ) ) {
            return new Integer( VIEW_DEFAULT );
        } else if( name.equals( LABEL_VIEW_MONTH ) ) {
            return new Integer( VIEW_MONTH );
        } else if( name.equals( LABEL_VIEW_WEEK ) ) {
            return new Integer( VIEW_WEEK );
        }
        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs1 = new FunctionSignature( 1 );
        fs1.addParam( AppointmentObject.class, true );
        final FunctionSignature fs2 = new FunctionSignature( 1 );
        fs2.addParam( Date.class, true );

        // additional signature for 6.0 - Dates are returned from JS as Scriptable
        final FunctionSignature fs3 = new FunctionSignature( 1 );
        fs3.addParam( Scriptable.class, true );

        return new FunctionSignature[] { new FunctionSignature( 0 ), fs1, fs2, fs3 };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

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
package blackberry.pim.attendee;

import blackberry.core.ScriptableFunctionBase;

/**
 * This class represents the constructor of an Attendee
 * 
 * @author dmateescu
 */
public class AttendeeConstructor extends ScriptableFunctionBase {
    public static final String NAME = "blackberry.pim.Attendee";

    public static final String ORGANIZER = "ORGANIZER";
    public static final String INVITED = "INVITED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String DECLINED = "DECLINED";
    public static final String TENTATIVE = "TENTATIVE";

    public static final Integer VAL_ORGANIZER = new Integer( 0 );
    public static final Integer VAL_INVITED = new Integer( 1 );
    public static final Integer VAL_ACCEPTED = new Integer( 2 );
    public static final Integer VAL_DECLINED = new Integer( 3 );
    public static final Integer VAL_TENTATIVE = new Integer( 4 );
    public static final Integer VAL_DELEGATED = new Integer( 5 );

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        return new AttendeeObject();
    }

    /**
     * Returns the field as an Object
     * 
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     * 
     * @param name
     *            the field's name
     * @return the Object representing the field
     */
    public Object getField( String name ) throws Exception {
        // Properties
        if( name.equals( ORGANIZER ) ) {
            return VAL_ORGANIZER;
        } else if( name.equals( INVITED ) ) {
            return VAL_INVITED;
        } else if( name.equals( ACCEPTED ) ) {
            return VAL_ACCEPTED;
        } else if( name.equals( DECLINED ) ) {
            return VAL_DECLINED;
        } else if( name.equals( TENTATIVE ) ) {
            return VAL_TENTATIVE;
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

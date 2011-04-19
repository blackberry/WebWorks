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
package blackberry.identity.phone;

import net.rim.device.api.script.Scriptable;

/**
 * This class defines public properties for blackberry.identity.phone namespace.
 * 
 * @author sgolod
 *
 */
public final class PhoneNamespace extends Scriptable {

    public static final String NAME = "phone";

    private static PhoneNamespace _instance = null;

    /**
     * This method implements singleton on PhoneNamespace and 
     * prevent the creating of more than one instance of it.
     *
     * @return the saved instance or create the new instance of a this class, if no saved instance found.   
     */
    public static synchronized PhoneNamespace getInstance() {
        if( _instance == null ) {
            _instance = new PhoneNamespace();
        }

        return _instance;
    }

    private PhoneNamespace() {
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( GetLineIdsFunction.NAME ) ) {
            return new GetLineIdsFunction();
        } else if( name.equals( GetLineLabelFunction.NAME ) ) {
            return new GetLineLabelFunction();
        } else if( name.equals( GetLineNumberFunction.NAME ) ) {
            return new GetLineNumberFunction();
        } else if( name.equals( GetLineTypeFunction.NAME ) ) {
            return new GetLineTypeFunction();
        }

        return super.getField( name );
    }

}

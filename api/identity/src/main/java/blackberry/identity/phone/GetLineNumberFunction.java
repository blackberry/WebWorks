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

import net.rim.blackberry.api.phone.Phone;

/**
 * Returns the phone number of the line specified.
 */
public final class GetLineNumberFunction extends PhoneFunctionBase {
    public static final String NAME = "getLineNumber";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#invoke(java.lang.Object, java.lang.Object[])
     */
    public Object invoke( final Object thiz, final Object[] args ) throws Exception {
        int lineId = 0;

        if( args.length > 0 ) {
            if( args[ 0 ] instanceof Integer ) {
                lineId = ( (Integer) args[ 0 ] ).intValue();
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            lineId = this.getDefaultLineId();
        }

        return Phone.getLineNumber( lineId );
    }
}

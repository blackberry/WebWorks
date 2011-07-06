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
package blackberry.messaging;

import blackberry.messaging.message.MessageConstructor;
import net.rim.device.api.script.Scriptable;

/**
 * Implementation of blackberry.message namespace.
 */
public class MessagingNamespace extends Scriptable {

    public static final String FIELD_NAME_MESSAGE = "Message";
    private final String SMS_NAMESPACE_NAME = "sms";

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( SMS_NAMESPACE_NAME ) ) {
            return MessagingExtension.getSmsNamespace();
        }

        else if( name.equals( FIELD_NAME_MESSAGE ) ) {
            return new MessageConstructor();
        }
        return super.getField( name );
    }
}

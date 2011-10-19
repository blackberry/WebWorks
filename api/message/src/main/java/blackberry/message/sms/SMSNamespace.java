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
package blackberry.message.sms;

import blackberry.message.MessageNamespace;

/**
 * SMS message namespace.
 * 
 * @author dmeng
 * 
 */
public class SMSNamespace extends MessageNamespace {

    public static final String NAME = "blackberry.message.sms";

    /**
     * Constructor.
     */
    public SMSNamespace() {
        _receiver = new SMSReceiver();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( FunctionSendSMS.NAME ) ) {
            return new FunctionSendSMS();
        }
        return super.getField( name );
    }
}

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
package blackberry.invoke.messageArguments;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.messaging.message.MessageObject;
import blackberry.messaging.message.MessageUtility;

/**
 * The MessageArgumentsConstructor class is used to create new MessageArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class MessageArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "MessageArguments";

    public static final int VIEW_NEW = 0;
    public static final int VIEW_DEFAULT = 1;
    public static final int VIEW_SAVED = 2;
    public static final int VIEW_SEARCH = 3;

    public static final String LABEL_VIEW_NEW = "VIEW_NEW";
    public static final String LABEL_VIEW_DEFAULT = "VIEW_DEFAULT";
    public static final String LABEL_VIEW_SAVED = "VIEW_SAVED";
    public static final String LABEL_VIEW_SEARCH = "VIEW_SEARCH";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        MessageArgumentsObject result = null;
        if( args == null || args.length == 0 ) {
            result = new MessageArgumentsObject( null );
        } else {
            if( args.length == 1 ) {
                final MessageObject a = (MessageObject) args[ 0 ];
                try {
                    a.save();
                } catch( final Exception e ) {
                }

                final Message m = a.getMessage();
                result = new MessageArgumentsObject( m );
            } else if( args.length == 3 ) {
                final String to = (String) args[ 0 ];
                final String subject = (String) args[ 1 ];
                final String body = (String) args[ 2 ];

                final Message m = new Message();
                final Address[] addresses = MessageUtility.stringToAddresses( to );

                try {
                    m.addRecipients( net.rim.blackberry.api.mail.Message.RecipientType.TO, addresses );
                    m.setSubject( subject );
                    m.setContent( body );
                } catch( final Exception e ) {
                }

                result = new MessageArgumentsObject( m );
            }
        }
        return result;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_NEW ) ) {
            return new Integer( VIEW_NEW );
        } else if( name.equals( LABEL_VIEW_DEFAULT ) ) {
            return new Integer( VIEW_DEFAULT );
        } else if( name.equals( LABEL_VIEW_SAVED ) ) {
            return new Integer( VIEW_SAVED );
        } else if( name.equals( LABEL_VIEW_SEARCH ) ) {
            return new Integer( VIEW_SEARCH );
        }

        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs1 = new FunctionSignature( 1 );
        fs1.addParam( MessageObject.class, true );
        final FunctionSignature fs2 = new FunctionSignature( 3 );
        fs2.addParam( String.class, true );
        fs2.addParam( String.class, true );
        fs2.addParam( String.class, true );
        return new FunctionSignature[] { new FunctionSignature( 0 ), fs1, fs2 };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

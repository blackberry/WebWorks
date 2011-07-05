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
package blackberry.messaging.message;

import java.util.Vector;

import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.find.TestableScriptableObject;
import blackberry.identity.service.ServiceObject;

/**
 * Implementation of the find function
 */
public class FindFunction extends ScriptableFunctionBase {
    public static final String NAME = "find";

    /**
     * Looks up the messages that match the expression provided.
     * 
     * @param thiz
     *            Context where this function was called.
     * @param args
     *            args[0]: optional parameter that defines the search criteria for the find. If no value is provided the method
     *            will return all the Messages on the device for the service provided. args[1]: optional integer parameter
     *            specifying the maximum number of results to return from the find. If no value is specified, it will return all
     *            results found. args[2]: optional parameter to define which service you wish to search for your messages. If not
     *            provided the default service for messages will be used.
     * @return Returns list of Messages that match search criteria.
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        MessageObject[] messagesFound = null;

        TestableScriptableObject testable = null;
        int maxReturn = -1;

        if( args.length > 0 ) {
            testable = (TestableScriptableObject) args[ 0 ];
        }

        if( args.length > 1 && args[ 1 ] != null ) {
            Integer i = (Integer) args[ 1 ];
            maxReturn = i.intValue();
        }

        ServiceObject s = null;
        if( args.length > 2 ) {
            s = (ServiceObject) args[ 2 ];
        }

        Store store = null;
        ServiceConfiguration serviceConfig = null;
        if( s == null ) {
            store = Session.getDefaultInstance().getStore();
            serviceConfig = store.getServiceConfiguration();
        } else {
            try {
                serviceConfig = new ServiceConfiguration( s.getUid(), s.getCid() );
            } catch( NoSuchServiceException e ) {
                serviceConfig = null;
            } finally {
                if( serviceConfig != null ) {
                    store = Session.getInstance( serviceConfig ).getStore();
                } else {
                    store = Session.getDefaultInstance().getStore();
                    serviceConfig = store.getServiceConfiguration();
                }
            }
        }

        Vector found = new Vector();
        int iElement = 0;

        try {
            Vector v = new Vector();
            MessageUtility.getAllFoldersRecursively( store, v );
            if( v.size() == 0 ) {
                return messagesFound;
            }

            Folder[] folders = new Folder[ v.size() ];
            v.copyInto( folders );

            for( int i = folders.length - 1; i >= 0; --i ) {
                if( s != null && folders[ i ].getFullName().indexOf( ServiceConfiguration.NO_SERVICE_BOOK ) != -1 ) {
                    continue;
                }

                Folder folder = folders[ i ];
                Message[] messages = folder.getMessages();

                for( int j = 0; j < messages.length; j++ ) {
                    Message m = messages[ j ];
                    MessageObject message = new MessageObject( m, s );
                    if( testable != null ) {
                        if( testable.test( message ) ) {
                            found.addElement( message );
                            iElement++;
                        }
                    } else {
                        found.addElement( message );
                        iElement++;
                    }

                    if( iElement == maxReturn ) {
                        break;
                    }
                }

                if( iElement == maxReturn ) {
                    break;
                }
            }
        } catch( MessagingException e ) {
            return messagesFound;
        }

        messagesFound = new MessageObject[ found.size() ];
        for( int k = 0; k < found.size(); k++ ) {
            MessageObject message = (MessageObject) found.elementAt( k );
            messagesFound[ k ] = message;
        }

        return messagesFound;
    }

    /**
     * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        fs.addNullableParam( TestableScriptableObject.class, false );
        fs.addNullableParam( Integer.class, false );
        fs.addNullableParam( ServiceObject.class, false );
        return new FunctionSignature[] { fs };
    }
}

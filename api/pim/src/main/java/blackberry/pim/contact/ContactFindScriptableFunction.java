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
package blackberry.pim.contact;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.find.FindNamespace;
import blackberry.find.TestableScriptableObject;
import blackberry.identity.service.ServiceObject;

/**
 * This class represents the find function of a Contact object
 * 
 * @author dmateescu
 */
public class ContactFindScriptableFunction extends ScriptableFunctionBase {
    public static final String NAME = "find";

    /**
     * Constructs a find function for the contacts
     */
    public ContactFindScriptableFunction() {
        super();
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        ContactObject[] contactsFound = new ContactObject[ 0 ];

        TestableScriptableObject testable = null;
        String orderByField = "";
        int maxReturn = -1;
        String serviceName = "";
        boolean isAscending = true;

        if( !FindNamespace.isValidFindArguments( args, true ) ) {
            return contactsFound;
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
        ContactList contactList;
        try {
            if( serviceName.length() == 0 ) {
                contactList = (ContactList) PIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.READ_WRITE );
            } else {
                contactList = (ContactList) PIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.READ_WRITE, serviceName );
            }
        } catch( PIMException pime ) {
            return contactsFound;
        }

        Vector found = new Vector();
        Enumeration e;
        int iElement = 0;
        try {
            e = contactList.items();
            while( e.hasMoreElements() ) {
                if( !isSorted && iElement == maxReturn ) {
                    break;
                }

                Contact c = (Contact) e.nextElement();
                ContactObject contact = new ContactObject( c );
                if( testable != null ) {
                    if( testable.test( contact ) ) {
                        FindNamespace.insertElementByOrder( found, contact, orderByField, isAscending );
                        iElement++;
                    }
                } else {
                    FindNamespace.insertElementByOrder( found, contact, orderByField, isAscending );
                    iElement++;
                }
            }
        } catch( PIMException pime ) {
            return contactsFound;
        }

        int size = found.size();
        if( maxReturn > 0 && size > maxReturn ) {
            size = maxReturn;
        }
        contactsFound = new ContactObject[ size ];
        for( int i = 0; i < size; i++ ) {
            ContactObject contact = (ContactObject) found.elementAt( i );
            contactsFound[ i ] = contact;
        }
        return contactsFound;
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
}

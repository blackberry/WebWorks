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
package blackberry.invoke.addressBookArguments;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.pim.contact.ContactObject;

/**
 * The AddressBookArgumentsConstructor class is used to create new AddressBookArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class AddressBookArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "AddressBookArguments";

    public static final int VIEW_NEW = 0;
    public static final int VIEW_COMPOSE = 1;
    public static final int VIEW_DISPLAY = 2;
    public static final String LABEL_VIEW_NEW = "VIEW_NEW";
    public static final String LABEL_VIEW_COMPOSE = "VIEW_COMPOSE";
    public static final String LABEL_VIEW_DISPLAY = "VIEW_DISPLAY";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args == null || args.length == 0 ) {
            return new AddressBookArgumentsObject();
        }
        final ContactObject c = (ContactObject) args[ 0 ];
        return new AddressBookArgumentsObject( c );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_NEW ) ) {
            return new Integer( VIEW_NEW );
        } else if( name.equals( LABEL_VIEW_COMPOSE ) ) {
            return new Integer( VIEW_COMPOSE );
        } else if( name.equals( LABEL_VIEW_DISPLAY ) ) {
            return new Integer( VIEW_DISPLAY );
        }

        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( ContactObject.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

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

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.service.ServiceObject;

/**
 * This class represents the constructor of a Contact
 * 
 * @author dmateescu
 * 
 */
public class ContactConstructor extends ScriptableFunctionBase {
    public static final String NAME = "blackberry.pim.Contact";

    private ContactFindScriptableFunction _find;

    /**
     * Default constructor
     */
    public ContactConstructor() {
        _find = new ContactFindScriptableFunction();
    }

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args != null && args.length == 1 ) {
            ServiceObject serviceObject = (ServiceObject) args[ 0 ];
            return new ContactObject( serviceObject );
        }
        return new ContactObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( ContactFindScriptableFunction.NAME ) ) {
            return _find;
        }

        return UNDEFINED;
    }

    /**
     * This method returns the array of function signatures
     * 
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( ServiceObject.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        return UNDEFINED;
    }
}

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
package blackberry.invoke.searchArguments;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * The SearchArgumentsConstructor class is used to create new SearchArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class SearchArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "SearchArguments";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args != null && args.length == 2 ) {
            return new SearchArgumentsObject( (String) args[ 0 ], (String) args[ 1 ] );
        }
        return new SearchArgumentsObject();
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { new FunctionSignature( 0 ), fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

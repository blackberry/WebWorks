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
package blackberry.invoke.javaArguments;

import net.rim.device.api.script.Scriptable;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * The JavaArgumentsConstructor class is used to create new JavaArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class JavaArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "JavaArguments";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        final StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append( (String) args[ 0 ] );

        // If there is params, which should be an Object represents a String array
        if( args.length == 2 ) {
            // params
            final Scriptable stringArray = (Scriptable) args[ 1 ];
            final int count = stringArray.getElementCount();
            for( int i = 0; i < count; i++ ) {
                if( i == 0 ) {
                    urlBuffer.append( "?" );
                } else {
                    urlBuffer.append( "&" );
                }

                try {
                    urlBuffer.append( stringArray.getElement( i ).toString() );
                } catch( final Exception e ) {
                    throw new IllegalArgumentException( "Problem occurred when parsing parameters. " + e.getMessage() );
                }
            }
        }
        return new JavaArgumentsObject( urlBuffer.toString() );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( Scriptable.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

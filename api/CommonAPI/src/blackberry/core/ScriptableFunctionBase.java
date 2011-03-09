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
package blackberry.core;

import java.util.Vector;

import net.rim.device.api.script.ScriptableFunction;

/**
 * @author sgolod
 *         <p>
 *         The base class for constructing a {@link net.rim.device.api.script.ScriptableFunction}
 */
public abstract class ScriptableFunctionBase extends ScriptableFunction {

    /**
     * @see net.rim.device.api.script.ScriptableFunction#invoke(Object, Object[])
     */
    public Object invoke( Object thiz, Object[] args ) throws Exception {
        validateArgs( args );
        return execute( thiz, args );
    }

    /**
     * Implements the function of this scriptable function.
     * 
     * @param thiz
     *            Context where this function was called.
     * @param args
     *            Arguments passed into the function
     * @return Any values returned from the call of this function.
     */
    protected abstract Object execute( Object thiz, Object[] args ) throws Exception;

    /**
     * Retrieves the possible combination of parameters to be passed into this function.
     * 
     * @return A vector of arrays of type 'class' - each index in the array specifies a different signature; null if this function
     *         takes no parameters.
     */
    protected FunctionSignature[] getFunctionSignatures() {
        return null;
    }

    /**
     * Compares an array of Object classes to the possible set of function signatures to determine if the set of arguments is
     * valid. A valid set of arguments has exactly the right number of arguments, and the type of each argument matches the
     * function signatures defined - see getFunctionSignatures.
     * 
     * @param args
     *            An object array representing the args passed into the function.
     * @return true if the set of arguments is valid; false otherwise
     */
    protected void validateArgs( Object[] args ) {
        FunctionSignature[] fSigs = getFunctionSignatures();

        // check arguments not required
        if( fSigs == null ) {
            if( args == null || args.length == 0 ) {
                // no arguments required
                return;
            } else {
                throw new IllegalArgumentException( "Too many arguments" );
            }
        }

        // loop through signatures
        int argsLength = args.length;
        int numSignatures = fSigs.length;
        boolean isValid = true;
        String errorMsg = null;
        for( int a = 0; a < numSignatures; a++ ) {
            // check num params
            Vector params = fSigs[ a ].getParams();
            int numParams = params.size();

            // check for too many arguments.
            if( argsLength > numParams ) {
                errorMsg = "Function not found with corresponding number of arguments";
                isValid = false;
                continue;
            }

            isValid = true; // assume true to start
            // loop through args and param
            for( int b = 0; b < numParams; b++ ) {
                FunctionSignature.FunctionParam param = (FunctionSignature.FunctionParam) params.elementAt( b );

                // check for optional param
                if( b >= argsLength && param.isRequired() ) {
                    errorMsg = "Required argument missing";
                    isValid = false;
                    break;
                } else if( b >= argsLength && !param.isRequired() ) {
                    continue;
                }
                Object arg = args[ b ];

                // check null/optional param
                if( ( arg == null || arg == UNDEFINED ) ) {
                    if( param.isNullable() ) {
                        continue;
                    } else {
                        errorMsg = "Argument is not nullable";
                    }
                }

                // check type
                if( !( arg == null || arg == UNDEFINED ) ) {
                    if( param.getType().isAssignableFrom( arg.getClass() ) ) {
                        continue;
                    } else {
                        errorMsg = "Invalid type - " + arg.getClass().toString();
                    }
                }
                isValid = false;
                break;
            }

            // found match
            if( isValid ) {
                break;
            }
        }
        if( !isValid ) {
            throw new IllegalArgumentException( errorMsg );
        }
    }
}

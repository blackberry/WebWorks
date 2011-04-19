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

/**
 * @author awong
 * 
 *         A wrapper class for a Function.
 */
public class FunctionSignature {
    private int _numParams;
    private Vector _params;

    /**
     * Constructs the FuctionSignature object.
     * 
     * @param numParams
     *            The number of parameters
     */
    public FunctionSignature( int numParams ) {
        _numParams = numParams;
        _params = new Vector( _numParams );
    }

    /**
     * Adds a parameter to the function.
     * 
     * @param type
     *            The parameter type
     * @param isRequired
     *            If the parameter is required
     */
    public void addParam( Class type, boolean isRequired ) {
        _params.addElement( new FunctionParam( type, isRequired, false ) );
    }

    /**
     * Add a nullable parameter to the function.
     * 
     * @param type
     *            The parameter type
     * @param isRequired
     *            If the parameter is required
     */
    public void addNullableParam( Class type, boolean isRequired ) {
        _params.addElement( new FunctionParam( type, isRequired, true ) );
    }

    /**
     * Returns parameters in the function.
     * 
     * @return The parameters
     */
    public Vector getParams() {
        return _params;
    }

    /**
     * Wrapper class for function parameter.
     * 
     * @author awong
     * 
     */
    public static class FunctionParam {
        Class _type;
        boolean _isRequired;
        boolean _acceptNull;

        /**
         * Constructs a parameter object.
         * 
         * @param type
         *            The parameter type
         * @param isRequired
         *            If the parameter is required
         * @param acceptNull
         *            If the parameter accepts null
         */
        FunctionParam( Class type, boolean isRequired, boolean acceptNull ) {
            _type = type;
            _isRequired = isRequired;
            _acceptNull = acceptNull;
        }

        /**
         * @return If the parameter is required
         */
        public boolean isRequired() {
            return _isRequired;
        }

        /**
         * @return The parameter type
         */
        public Class getType() {
            return _type;
        }

        /**
         * @return If the parameter accepts null
         */
        public boolean isNullable() {
            return _acceptNull;
        }
    }
}

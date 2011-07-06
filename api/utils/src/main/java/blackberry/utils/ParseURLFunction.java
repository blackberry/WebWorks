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
package blackberry.utils;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.utils.URL.URLObject;

/**
 * Implementation of function parseURL
 */
public final class ParseURLFunction extends ScriptableFunctionBase {

    public static final String NAME = "parseURL";

    /**
     * Parse a URL string into a URLObject. A MalformedURLException will be thown if the URL format is invalid
     * 
     * @param thiz
     *            Context where this function was called.
     * @param args
     *            args[0] : the URL link
     * @return An URLObject object
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        return new URLObject( args[ 0 ].toString() );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

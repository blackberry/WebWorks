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
package blackberry.io.file;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - determines if a specified file exists
 * @author awong
 *
 */
public final class ExistsFunction extends ScriptableFunctionBase {

    public static final String NAME = "exists";

    /**
     * Check whether a given file exists or not
     * 
     * @param args
     *            args[0]: the complete URL path to the file
     *            
     * @return Return true if the file exists, false otherwise (note: if the path is to a existing directory (not a file), the
     *         method will still return false)
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {

        Boolean exists = null;
        FileConnectionWrapper fConnWrap = null;
        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );

            exists = new Boolean( fConnWrap.exists() && !fConnWrap.isDirectory() );
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
        return exists;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

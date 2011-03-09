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
package blackberry.io.dir;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - determines the amount of free space available in the specified root
 */
public final class GetFreeSpaceForRootFunction extends ScriptableFunctionBase {

    public static final String NAME = "getFreeSpaceForRoot";

    /**
     * JavaScript extension function; determine how much free space is available for storage based on a root directory
     * 
     * @param args
     *            args[0]: The complete URL path to a root directory
     * 
     * @return Amount of free space in bytes
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {
        Double freeSpace = null;
        FileConnectionWrapper fConnWrap = null;
        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
            freeSpace = new Double( fConnWrap.getFileConnection().availableSize() );
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
        return freeSpace;
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

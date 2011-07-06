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

import net.rim.device.api.io.file.FileIOException;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - deletes a file given the specified path
 */
public final class DeleteFileFunction extends ScriptableFunctionBase {

    public static final String NAME = "deleteFile";

    /**
     * Delete a given file.
     * 
     * @param args
     *            args[0]: a complete URL path of the file.
     * 
     *            IOException will be thrown if src file does not yet exist or if the src path is a directory
     * 
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fConnWrap = null;
        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );

            if( fConnWrap.isDirectory() ) {
                throw new FileIOException( FileIOException.NOT_A_FILE );
            }

            fConnWrap.delete();
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
        return UNDEFINED;
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

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

import net.rim.device.api.io.file.FileIOException;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - renames the given path
 */
public final class RenameFunction extends ScriptableFunctionBase {

    public static final String NAME = "rename";

    /**
     * JavaScript extension function; Rename a given directory.
     * 
     * @param args
     *            args[0] a complete URL path to a directory that is to be renamed args[1] the URL used for renaming
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fConnWrap = null;
        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );

            if( !fConnWrap.exists() ) {
                throw new FileIOException( FileIOException.DIRECTORY_NOT_FOUND );
            }

            if( !fConnWrap.isDirectory() ) {
                throw new FileIOException( FileIOException.NOT_A_DIRECTORY );
            }

            if( args[ 1 ].toString().length() == 0 ) {
                throw new FileIOException( FileIOException.NOT_A_DIRECTORY );
            }

            fConnWrap.getFileConnection().rename( args[ 1 ].toString() );
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
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

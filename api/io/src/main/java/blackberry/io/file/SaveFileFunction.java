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

import java.io.DataOutputStream;

import blackberry.core.Blob;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - saves the specified file by writing it back to the filesystem
 * @author awong
 *
 */
public final class SaveFileFunction extends ScriptableFunctionBase {

    public static final String NAME = "saveFile";

    /**
     * Save a Blob into a file.
     * 
     * @param args
     *            args[0] : A complete URL path of the file. The name of the file should specified at the end of the path, and the
     *            file should not exist prior to the method call. args[1] : byte array that needs to be saved.
     * 
     *            IOException will be thrown if 1. src file does not yet exist, or the src path is to an directory
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fConnWrap = null;
        DataOutputStream dos = null;

        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
            fConnWrap.create();
            dos = fConnWrap.openDataOutputStream();
            byte[] data = ( (Blob) ( args[ 1 ] ) ).getBytes();
            dos.write( data, 0, data.length );
            dos.flush();
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
        fs.addParam( Blob.class, true );
        return new FunctionSignature[] { fs };
    }
}

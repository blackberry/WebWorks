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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import net.rim.device.api.io.file.FileIOException;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - copies a file from one specified location to another
 */
public final class CopyFunction extends ScriptableFunctionBase {

    public static final String NAME = "copy";

    /**
     * Copy a file to a given destination
     * 
     * @param args
     *            args[0]: a complete URL path of the source file. 
     *            args[1]: a complete URL path of the copied file. The URL path should end with the name of the new copied file.
     * 
     *            IOException will be thrown if source file does not yet exist, or the source path is to an directory or if
     *            destination file already exist
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fconnWrapIn = null;
        DataInputStream dis = null;
        FileConnectionWrapper fconnWrapOut = null;
        DataOutputStream dos = null;

        try {
            fconnWrapIn = new FileConnectionWrapper( args[ 0 ].toString() );
            dis = fconnWrapIn.openDataInputStream();

            fconnWrapOut = new FileConnectionWrapper( args[ 1 ].toString() );

            if( fconnWrapOut.isDirectory() ) {
                throw new FileIOException( FileIOException.NOT_A_FILE );
            }

            fconnWrapOut.create();
            dos = fconnWrapOut.openDataOutputStream();

            for( int b = dis.read(); b != -1; b = dis.read() ) {
                dos.write( b );
            }

            dos.flush();
        } finally {
            if( fconnWrapIn != null ) {
                fconnWrapIn.close();
            }
            if( fconnWrapOut != null ) {
                fconnWrapOut.close();
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

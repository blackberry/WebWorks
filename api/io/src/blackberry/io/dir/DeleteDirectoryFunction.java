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

import java.io.IOException;
import java.util.Enumeration;

import net.rim.device.api.io.file.FileIOException;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - deletes a directory given a specified path
 */
public final class DeleteDirectoryFunction extends ScriptableFunctionBase {

    public static final String NAME = "deleteDirectory";

    /**
     * JavaScript extension function; deletes a specified directory
     * 
     * @param args
     *            args[0]: the complete URL path to the directory
     * 
     * @return Returns undefined; throws FileIOException if directory not found or if specified path is not a directory
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

            // Delete all the contents of the directory if the flag is set
            if( args.length > 1 && args[ 1 ] instanceof Boolean && args[ 1 ].toString().equals( "true" ) ) {
                deleteEntireDirectory( fConnWrap );
            } else {
                fConnWrap.getFileConnection().delete();
            }
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
        return UNDEFINED;
    }

    private FileConnectionWrapper deleteFile( FileConnectionWrapper fConn ) throws IOException {

        // Save the parent path
        String filePath = fConn.getFileConnection().getURL();
        String parentPath = filePath.substring( 0, filePath.length() - fConn.getFileConnection().getName().length() );

        // Delete the file
        fConn.delete();

        // Close the stream and open a new one using the parent
        // Could not reuse the old FileConnection because there are
        // still references to the recently deleted file that will
        // throw an IOException
        fConn.close();

        // Set the connector to the parent
        final FileConnectionWrapper newFConn = new FileConnectionWrapper( parentPath );

        return newFConn;
    }

    private FileConnectionWrapper recursiveDelete( FileConnectionWrapper fConn ) throws IOException {

        // Get a list of all the contained files and folders
        Enumeration containedFiles = fConn.list();
        String filename;

        // Cycle through all the files and delete them
        while( containedFiles.hasMoreElements() ) {
            filename = (String) containedFiles.nextElement();

            // Open the connection to the file
            fConn.getFileConnection().setFileConnection( filename );

            // Files can be deleted directly
            if( !fConn.isDirectory() ) {
                fConn = deleteFile( fConn );
            }
            // Directories must be recursively deleted
            else {
                fConn = recursiveDelete( fConn );
            }
        }

        // Delete the current directory and return the parent dir
        fConn = deleteFile( fConn );

        return fConn;
    }

    private void deleteEntireDirectory( final FileConnectionWrapper fConn ) throws IOException {
        recursiveDelete( fConn );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

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
import java.util.Date;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.io.file.ExtendedFileConnection;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;
import blackberry.io.file.FileProperties.FilePropertiesObject;

/**
 * JavaScript function class - retrieves the blackberry.io.file.FileProperties object given a specified path
 */
public final class GetFilePropertiesFunction extends ScriptableFunctionBase {

    public static final String NAME = "getFileProperties";

    /**
     * Return the FileProperties object of a given file
     * 
     * @param args
     *            args[0] : complete URL path of the file.
     * 
     *            IOException will be thrown if src file does not yet exist, or the src path is a directory
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fConnWrap = null;
        FilePropertiesObject fileProObj = null;

        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
            ExtendedFileConnection fConn = (ExtendedFileConnection) fConnWrap.getFileConnection();

            Boolean isReadonly = new Boolean( !fConn.canWrite() );
            Boolean isHidden = new Boolean( fConn.isHidden() );
            Double size = new Double( fConn.fileSize() );
            Date dateModified = new Date( fConn.lastModified() );

            int dotIndex = ( args[ 0 ].toString() ).lastIndexOf( (int) '.' );
            String fileExtension = "";
            if( dotIndex >= 0 ) {
                fileExtension = ( args[ 0 ].toString() ).substring( dotIndex );
            }

            String directory = "file://" + fConn.getPath();
            String mimeType = MIMETypeAssociations.getMIMEType( fConn.getName() );
            if( mimeType == null ) {
                mimeType = "";
            }

            // get the character encoding by scanning the first 2-4 bytes
            byte[] data = null;
            if( size.intValue() >= 2 ) {
                data = new byte[ 4 ];
                DataInputStream dis = fConnWrap.openDataInputStream();
                dis.read( data, 0, 4 ); // IOException may be thrown
            }
            String encoding = getEncoding( data );
            fileProObj = new FilePropertiesObject( isReadonly, isHidden, size, null, dateModified, fileExtension, directory,
                    mimeType, encoding );

        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }

        return fileProObj;
    }

    private String getEncoding( byte[] data ) {

        if( data == null || data.length < 2 ) {
            return "";
        }

        switch( data[ 0 ] & 0xFF ) {
            case 0x00:
                if( data.length >= 4 && data[ 1 ] == (byte) 0x00 && data[ 2 ] == (byte) 0xFE && data[ 3 ] == (byte) 0xFF ) {
                    return "utf-32be";
                }
                break;
            case 0xFE:
                if( data[ 1 ] == (byte) 0xFF ) {
                    return "utf-16be";
                }
                break;
            case 0xFF:
                if( data[ 1 ] == (byte) 0xFE ) {
                    if( data.length >= 4 && data[ 2 ] == (byte) 0x00 && data[ 3 ] == (byte) 0x00 ) {
                        return "utf-32le";
                    } else {
                        return "utf-16le";
                    }
                }
                break;
            case 0xEF:
                if( data.length >= 3 && data[ 1 ] == (byte) 0xBB && data[ 2 ] == (byte) 0xBF ) {
                    return "utf-8";
                }
                break;
        }
        return "";
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

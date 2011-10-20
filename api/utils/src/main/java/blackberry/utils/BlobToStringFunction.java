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

import net.rim.device.api.io.Base64OutputStream;
import blackberry.core.Blob;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of function blobToString
 */
public final class BlobToStringFunction extends ScriptableFunctionBase {

    public static final String NAME = "blobToString";

    private static final String BASE64_ENCODING = "BASE64";
    private static final String UTF8_ENCODING = "UTF-8";

    /**
     * Construct a new String by converting the blob using the specified character encoding.
     * 
     * @param thiz
     *            Context where this function was called.
     * @param args
     *            args[0] : byte array that needs to be saved. args[1] : The name of a supported character encoding.
     * @return String representation of the blob
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        byte[] data = ( (Blob) ( args[ 0 ] ) ).getBytes();

        String encoding = null;
        if( args.length == 2 ) {
            encoding = args[ 1 ].toString();
        }

        if( encoding == null ) {
            return new String( data );
        }

        if( encoding.equalsIgnoreCase( BASE64_ENCODING ) ) {
            byte[] encoded = Base64OutputStream.encode( data, 0, data.length, false, false );
            String encodedStr = new String( encoded, UTF8_ENCODING );
            return encodedStr;
        }

        return new String( data, encoding );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( Blob.class, true );
        fs.addParam( String.class, false );
        return new FunctionSignature[] { fs };
    }
}
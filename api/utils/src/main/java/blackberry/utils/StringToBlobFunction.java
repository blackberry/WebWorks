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

import net.rim.device.api.io.Base64InputStream;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.WidgetBlob;

/**
 * Implementation of function stringToBlob
 */
public class StringToBlobFunction extends ScriptableFunctionBase {

    public static final String NAME = "stringToBlob";

    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final String BASE64_ENCODING = "BASE64";
    private static final String UTF8_ENCODING = "UTF-8";

    /**
     * Convert a String object into a Blob using the specified character encoding. BlackBerry supports the following character
     * encodings: "ISO-8859-1", "UTF-8", "UTF-16BE", "US-ASCII". If encoding information is not provided in the parameter,
     * ISO-8859-1 encoding will be used by default.
     * 
     * @param thiz
     *            Context where this function was called.
     * @param args
     *            args[0]: the String to be converted. args[1]: optional parameter; the encoding type to be used
     * @return a Blob representation of the String.
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        byte[] data = null;
        if( args.length == 1 ) {
            data = ( args[ 0 ].toString() ).getBytes( DEFAULT_ENCODING );
        } else {
            String encoding = args[ 1 ].toString();
            if( encoding.equalsIgnoreCase( BASE64_ENCODING ) ) {
                byte[] dataEncoded = ( args[ 0 ].toString() ).getBytes( UTF8_ENCODING );
                data = Base64InputStream.decode( dataEncoded, 0, dataEncoded.length );
            } else {
                data = ( args[ 0 ].toString() ).getBytes( encoding );
            }
        }

        return new WidgetBlob( data );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( String.class, false );
        return new FunctionSignature[] { fs };
    }
}

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

import net.rim.device.api.system.DeviceInfo;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;

import blackberry.common.util.StringUtilities;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.WidgetBlob;

/**
 * Implementation of function documentToBlob
 */
public final class DocumentToBlobFunction extends ScriptableFunctionBase {

    public static final String NAME = "documentToBlob";
    public static final String DEFAULT_ENCODING = "UTF-16BE";

    /**
     * Supported in 5.0.0 only. Convert a document into a Blob If encoding information is not known, UTF-16BE encoding will be
     * used by default. BlackBerry supports the following character encodings: "ISO-8859-1", "UTF-8", "UTF-16BE", "US-ASCII".
     * 
     *@param thiz
     *            Context where this function was called.
     * @param args
     *            args[0]: the document to be converted.
     * @return a Blob representation of the document.
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        String versionString = DeviceInfo.getSoftwareVersion();
        if( versionString.length() == 0 ) { // return UNDEFINED if version number cannot be retrieved
            return UNDEFINED;
        } else {
            int versionNumber = Integer.parseInt( ( StringUtilities.split( versionString, "." ) )[ 0 ] );
            if( versionNumber > 5 ) { // return UNDEFINED if version number > 5
                return UNDEFINED;
            } else {
                Document dom = (Document) args[ 0 ];
                String domStr = ( (DOMImplementationLS) dom.getImplementation() ).createLSSerializer().writeToString( dom );
                String encoding = ( dom.getInputEncoding() == null ) ? DEFAULT_ENCODING : dom.getInputEncoding();
                byte[] data = domStr.getBytes( encoding );
                return new WidgetBlob( data );
            }
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( Document.class, true );
        return new FunctionSignature[] { fs };
    }
}
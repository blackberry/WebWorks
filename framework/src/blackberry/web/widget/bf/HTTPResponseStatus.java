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
package blackberry.web.widget.bf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.global.Formatter;

import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResponse;

/**
 * This class maps http status code to message, and create a response with corresponding error code and message
 * 
 * @author danlin
 * 
 */
public class HTTPResponseStatus {
    // HTTP status code
    public static final int SC_SUCCESS = 200;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    // HTTP status message
    private final String SM_SUCCESS = "Success";
    private final String SM_BAD_REQUEST = "Bad Request";
    private final String SM_FORBIDDEN = "Forbidden";
    private final String SM_NOT_FOUND = "Not Found";
    private final String SM_SERVER_ERROR = "Server Error";
    private final String SM_NOT_IMPLEMENTED = "Not Implemented";
    private final String SM_SERVICE_UNAVAILABLE = "Service Unavailable";
    // HTTP error message template, it looks like:
    // 404 Not Found
    // Request URL: http://localhost:8472/blackberry/system/methodNotExists
    private final String TMPL_HTTP_ERROR_MESSAGE = "{0}: {2}\nRequest URL: {1}";

    // instance variables
    private BrowserFieldResponse _response;
    private Hashtable _codeMessageDictionary;

    public HTTPResponseStatus( int responseCode, BrowserFieldRequest request ) throws IOException {
        buildCodeMessageDict();
        String message = buildMessage( responseCode, request.getURL() );
        ByteArrayInputStream input = new ByteArrayInputStream( message.getBytes() );
        _response = new BrowserFieldResponse( responseCode, message, request.getURL(), input, request.getHeaders() );
    }

    BrowserFieldResponse getResponse() {
        return _response;
    }

    /**
     * map code to error message
     */
    private void buildCodeMessageDict() {
        if( _codeMessageDictionary == null ) {
            _codeMessageDictionary = new Hashtable();
        }
        _codeMessageDictionary.put( new Integer( SC_SUCCESS ), new String( SM_SUCCESS ) );
        _codeMessageDictionary.put( new Integer( SC_BAD_REQUEST ), new String( SM_BAD_REQUEST ) );
        _codeMessageDictionary.put( new Integer( SC_FORBIDDEN ), new String( SM_FORBIDDEN ) );
        _codeMessageDictionary.put( new Integer( SC_NOT_FOUND ), new String( SM_NOT_FOUND ) );
        _codeMessageDictionary.put( new Integer( SC_SERVER_ERROR ), new String( SM_SERVER_ERROR ) );
        _codeMessageDictionary.put( new Integer( SC_NOT_IMPLEMENTED ), new String( SM_NOT_IMPLEMENTED ) );
        _codeMessageDictionary.put( new Integer( SC_SERVICE_UNAVAILABLE ), new String( SM_SERVICE_UNAVAILABLE ) );
    }

    /**
     * build error message
     *
     * @param code
     *            - error code
     * @param url
     *            - request url
     * @return formatted error message
     */
    private String buildMessage( int code, String url ) {
        String msg = null;
        buildCodeMessageDict();
        String[] msgPrams;

        msgPrams = new String[ 3 ];
        msgPrams[ 0 ] = Integer.toString( code );
        msgPrams[ 1 ] = url;
        msgPrams[ 2 ] = (String) _codeMessageDictionary.get( new Integer( code ) );
        msg = Formatter.formatMessage( TMPL_HTTP_ERROR_MESSAGE, msgPrams );
        return msg;
    }
}
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
package blackberry.core;

import net.rim.device.api.io.http.HttpHeaders;
/**
 * This class holds the data for a JavaScript extension response.
 * @author nbhasin
 *
 */
public class JSExtensionResponse {

    private String _url;
    private byte[] _postData;
    private HttpHeaders _headers;

    /**
     * Constructs a JSExtensionRequest.
     * 
     * @param url
     *            String
     * @param postData
     *            byte[]
     * @param headers
     *            HttpHeaders
     */
    public JSExtensionResponse( String url, byte[] postData, HttpHeaders headers ) {
        _url = url;
        _postData = postData;
        _headers = headers;

    }

    /**
     * Utility Constructor to create a JSExtension from BrowserFieldRequest.
     * 
     * @param request
     *            BrowserFieldRequest
     */
    public JSExtensionResponse( JSExtensionRequest request ) {
        this( request.getURL(), request.getPostData(), request.getHeaders() );
    }

    /**
     * Retrieves the URL from this request.
     * 
     * @return String
     */
    public String getURL() {
        return _url;
    }

    /**
     * Retrieves the post data from this request.
     * 
     * @return byte[]
     */
    public byte[] getPostData() {
        return _postData;
    }

    /**
     * Retrieves the HTTP headers from this request.
     * 
     * @return HttpHeaders
     */
    public HttpHeaders getHeaders() {
        return _headers;
    }

    /**
     * Sets the URL for this request.
     * 
     * @param String
     */
    public void setURL( String url ) {
        this._url = url;
    }

    /**
     * Sets the post data for this request.
     * 
     * @param byte[]
     */
    public void setPostData( byte[] postData ) {
        this._postData = postData;
    }

    /**
     * Sets the headers for this request.
     * 
     * @param HttpHeaders
     */
    public void setHeaders( HttpHeaders headers ) {
        this._headers = headers;
    }
}

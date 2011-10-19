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

import java.util.Hashtable;

import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.io.http.HttpHeaders;
import blackberry.common.util.StringUtilities;
import blackberry.common.util.URLDecoder;

/**
 * This class holds the data for a JavaScript extension request.
 * 
 * @author nbhasin
 */
public class JSExtensionRequest {
    private String _url;
    private byte[] _postData;
    private HttpHeaders _headers;
    private String _methodName;
    private Object[] _args;
    private Hashtable _featureTable;
    private Hashtable _argsTable;
    private Object[] _postArgs;
    private Hashtable _postArgsTable;

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
    public JSExtensionRequest( String url, byte[] postData, HttpHeaders headers, Hashtable featureTable ) {
        _url = URLDecoder.decode(url);
        _postData = postData;
        _headers = headers;
        _featureTable = featureTable;
        _argsTable = new Hashtable();
        _postArgsTable = new Hashtable();

        URI requestURI = null;
        try {
            requestURI = URI.create( _url );
        } catch( IllegalArgumentException e ) {
            e.printStackTrace();
        } catch( MalformedURIException e ) {
            e.printStackTrace();
        }

        String[] splitPath = StringUtilities.split( URLDecoder.decode(requestURI.getPath()), "/" );
        _methodName = splitPath[ splitPath.length - 1 ];

        if( requestURI.getQuery() != null ) {
            String[] completeArgs = StringUtilities.split(  URLDecoder.decode(requestURI.getQuery()), "&" );

            _args = new Object[ completeArgs.length ];
                        
            for( int i = 0; i < completeArgs.length; i++ ) {                
                int index = completeArgs[i].indexOf( "=" );
                String argValue = "";
                // the argValue can have = in it, only take the first = in the string as the delimiter
                argValue = completeArgs[i].substring( index + 1 );                
                _args[ i ] = (Object) argValue;
                
                if( index >= 0 ) {
                    _argsTable.put( completeArgs[ i ].substring( 0, index ), (Object) argValue );
                } else {
                    _argsTable.put( completeArgs[ i ], (Object) argValue );
                }
            }
        }
        
        if( _postData != null ) {
            String[] splitPostData = StringUtilities.split( URLDecoder.decode( new String( _postData ) ), "&" );
            _postArgs = new Object[ splitPostData.length ];

            for( int i = 0; i < splitPostData.length; i++ ) {
                int index = splitPostData[ i ].indexOf( "=" );
                String argValue = "";

                // the argValue can have = in it, only take the first = in the string as the delimiter                
                argValue = splitPostData[ i ].substring( index + 1 );
                _postArgs[ i ] = (Object) argValue;

                if( index >= 0 ) {
                    _postArgsTable.put( splitPostData[ i ].substring( 0, index ), (Object) argValue );
                } else {
                    _postArgsTable.put( splitPostData[ i ], (Object) argValue );
                }
            }
        }        
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
     * Retrieves the method name from this request.
     * 
     * @return String
     */
    public String getMethodName() {
        return _methodName;
    }

    /**
     * Retrieves an array of argument values from this request.
     * 
     * @return Object[]
     */
    public Object[] getArgs() {
        return _args;
    }
    
    /**
     * Retrieves an array of post argument values from this request.
     * 
     * @return Object[]
     */    
    public Object[] getPostArgs() {
        return _postArgs;
    }
    
    /**
     * Determines whether the feature ID exists in the Feature Table
     * 
     * @return boolean
     */
    public boolean isWhiteListed( String featureID ) {
        if( _featureTable.get( featureID ) != null ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves argument value given the argument name
     * 
     * @return Object
     */
    public Object getArgumentByName( String argumentName ) {
        return _argsTable.get( argumentName );
    }
    
    /**
     * Retrieves post argument value given the argument name
     * 
     * @return Object
     */
    public Object getPostArgumentByName( String argumentName ) {
        return _postArgsTable.get( argumentName );
    }    
}

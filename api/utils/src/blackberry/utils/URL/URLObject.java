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
package blackberry.utils.URL;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.script.Scriptable;
import blackberry.common.util.PatternMatchingUtilities;
import blackberry.common.util.StringUtilities;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.utils.UtilsExtension;

/**
 * Class representing the URL object
 */
public class URLObject extends Scriptable {

    public static final String FIELD_HOST = "host";
    public static final String FIELD_PORT = "port";
    public static final String FUNCTION_GETURLPARAMETERBYINDEX = "getURLParameterByIndex";
    public static final String FUNCTION_GETURLPARAMETER = "getURLParameter";

    private static final int DEFAULT_LOCAL_PORT = 0;
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_TLS_PORT = 443;

    private URI _uri;
    private Vector _keys; // stores a list of keys in the order they appear in the URL
    private Hashtable _parameters, _fields;

    /**
     * Construct an URL object.
     * 
     * @param url
     * @throws MalformedURIException
     * @throws IllegalArgumentException
     */
    public URLObject( String url ) throws IllegalArgumentException, MalformedURIException {
        _uri = URI.create( url );
        initParameters();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( FIELD_HOST ) ) {
            return _uri.getHost();
        } else if( name.equals( FIELD_PORT ) ) {
            return getPort( _uri );
        } else if( name.equals( FUNCTION_GETURLPARAMETER ) ) {
            return _fields.get( name );
        } else if( name.equals( FUNCTION_GETURLPARAMETERBYINDEX ) ) {
            return _fields.get( name );
        }
        return super.getField( name );
    }

    private Integer getPort( URI uri ) {
        if( uri.getPort() == null ) {
            if( uri.getScheme().equals( "http" ) ) {
                return new Integer( DEFAULT_HTTP_PORT );
            } else if( uri.getScheme().equals( "https" ) || uri.getScheme().equals( "tls" ) ) {
                return new Integer( DEFAULT_HTTPS_TLS_PORT );
            } else if( uri.getScheme().equals( "ssl" ) || uri.getScheme().equals( "socket" ) ) {
                throw new IllegalArgumentException( "no port specified" );
            } else {
                return new Integer( DEFAULT_LOCAL_PORT );
            }
        }
        return Integer.valueOf( uri.getPort() );
    }

    private void initParameters() {
        String query = _uri.getQuery();

        if( query != null ) {
            _parameters = new Hashtable();
            _keys = new Vector();
            String pairsString = PatternMatchingUtilities.findMatches( UtilsExtension.getScriptEngine(), query, "[^&]*=[^&]*",
                    "&" );

            // convert string to array before parsing
            String[] pairs = StringUtilities.split( pairsString, "&" );
            for( int i = 0; i < pairs.length; i++ ) {
                String parameter = pairs[ i ];
                int index = parameter.indexOf( "=" );
                if( index >= 0 ) {
                    String key = parameter.substring( 0, index );
                    String value = parameter.substring( index + 1 );
                    _parameters.put( key, value );
                    _keys.addElement( key );
                }
            }
        }

        _fields = new Hashtable();
        _fields.put( FUNCTION_GETURLPARAMETER, createGetURLParameterFunction() );
        _fields.put( FUNCTION_GETURLPARAMETERBYINDEX, createGetURLParameterByIndexFunction() );
    }

    private ScriptableFunctionBase createGetURLParameterFunction() {

        return new ScriptableFunctionBase() {

            protected Object execute( Object thiz, Object[] args ) throws Exception {
                String key = ( args[ 0 ] ).toString();

                return ( _parameters == null || _parameters.get( key ) == null ) ? UNDEFINED : _parameters.get( key );
            }

            protected FunctionSignature[] getFunctionSignatures() {
                FunctionSignature fs = new FunctionSignature( 1 );
                fs.addParam( String.class, true );
                return new FunctionSignature[] { fs };
            }
        };
    }

    private ScriptableFunctionBase createGetURLParameterByIndexFunction() {

        return new ScriptableFunctionBase() {

            protected Object execute( Object thiz, Object[] args ) throws Exception {
                int index = ( (Integer) args[ 0 ] ).intValue();

                return ( _keys == null || index < 0 || index >= _keys.size() ) ? UNDEFINED : _parameters.get( _keys
                        .elementAt( index ) );
            }

            protected FunctionSignature[] getFunctionSignatures() {
                FunctionSignature fs = new FunctionSignature( 1 );
                fs.addParam( Integer.class, true );
                return new FunctionSignature[] { fs };
            }
        };
    }
}

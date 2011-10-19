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
package blackberry.invoke.browserArguments;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.transport.TransportObject;

/**
 * This class represents the BrowserArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class BrowserArgumentsObject extends ScriptableObjectBase {

    private final TransportObject _transportObject;
    private String _url;

    /**
     * Default constructor, constructs a new BrowserArgumentsObject object.
     */
    public BrowserArgumentsObject() {
        this( "", null );
    }

    /**
     * Constructs a new BrowserArgumentsObject object.
     * 
     * @param url
     *            The desired url to bring up in the browser
     */
    public BrowserArgumentsObject( final String url ) {
        this( url, null );
    }

    /**
     * Constructs a new BrowserArgumentsObject object.
     * 
     * @param url
     *            The desired url to bring up in the browser
     * @param transport
     *            - an optional parameter representing the transport type that the browser should use. If no parameter is
     *            specified the default browser configured for the device will be used.
     */
    public BrowserArgumentsObject( final String url, final TransportObject transport ) {
        _transportObject = transport;
        if( url != null ) {
            _url = url;
        } else {
            _url = "";
        }
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the BrowserArgumentsObject's underlying content.
     * 
     * @return the transport object.
     */
    public TransportObject getTransportObject() {
        return _transportObject;
    }

    /**
     * Internal helper method to get direct access to the BrowserArgumentsObject's underlying content.
     * 
     * @return the url.
     */
    public String getUrl() {
        return _url;
    }
}

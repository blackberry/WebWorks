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
package net.rim.tumbler.config;

import java.net.URI;

import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.session.SessionManager;

public class WidgetAccess {
    private URI _uri;
    private boolean _allowSubDomain;

    public WidgetAccess(String uri, boolean allowSubDomain) throws Exception {
        try {
            _uri = URI.create(uri);
            // Check for a protocol
            if (!(_uri.toString().equals("WidgetConfig.WIDGET_LOCAL_DOMAIN")) && 
                    (_uri.getScheme() == null || _uri.getScheme().length() == 0)) {
                throw new ValidationException (
                        "EXCEPTION_ACCESSURI_NO_PROTOCOL", uri.toString());
            }
            _allowSubDomain = allowSubDomain;
            
            String host = _uri.getHost();
            if (host != null 
                    && SessionManager.getInstance().getTLD().indexOf(
                            "$$" + host.toLowerCase().trim() + "$$") != -1 && _allowSubDomain) {
                // Throw exception - exit compilation
                throw new ValidationException( "EXCEPTION_CONFIGXML_TLD", uri );
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("EXCEPTION_ACCESSURI_BADURI", uri.toString());
        }
    }

    public URI getURI() {
        return _uri;
    }

    public boolean allowSubDomain() {
        return _allowSubDomain;
    }
}

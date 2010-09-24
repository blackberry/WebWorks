/*
* Copyright 2010 Research In Motion Limited.
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
package blackberry.web.widget.util;

import net.rim.device.api.io.URI;
import net.rim.device.api.system.DeviceInfo;

/**
 * 
 */
public final class WidgetUtil {
    
    private static final String LOCAL_PROTOCOL              = "local://";
    private static final String SLASH_FWD                   = "/";
    private static final String SLASH_BACK                  = "\\";
    private static final String ABSOLUTE_URI_MARKER         = "://";
    private static final String DATA_URI                    = "data:";
        
    /**
     * Parses a path to determine if it is a local resource; if so appends the 'cod' protocol.
     * @param path a path to a particular resource
     * @return a localized path if no protocol was previously specified (e.g. file, cod, http)
     */
    public static String getLocalPath(String path) {
        // if the path is already fully qualified, leave it alone
        if (path.indexOf(ABSOLUTE_URI_MARKER) != -1) {
            return path;
        }
        
        // relative path - default to local
        String prefix = LOCAL_PROTOCOL;
        if ( !path.startsWith(SLASH_FWD) && !path.startsWith(SLASH_BACK)) {
            prefix += SLASH_FWD;
        }
        return prefix + path;
    }
    
    public static boolean isLocalURI(URI uri) {
        return uri.getScheme() != null && LOCAL_PROTOCOL.startsWith(uri.getScheme());
    }
    
    // Checks if the specified uri starts with 'data:'
    public static boolean isDataURI(URI uri) {
        return uri.getScheme() != null && DATA_URI.startsWith(uri.getScheme());
    }
}

/*
 * WidgetUtil.java
 *
 * Copyright © 2009 Research In Motion Limited.  All rights reserved.
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

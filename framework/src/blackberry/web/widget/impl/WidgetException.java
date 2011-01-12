/*
 * WidgetException.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.impl;

/**
 * 
 */
public final class WidgetException extends Exception {
    
    public static final int ERROR_NETWORK_NOT_AVAILABLE = 0;
    public static final int ERROR_INVALID_SIM = 1;
    public static final int ERROR_INSUFFICIENT_COVERAGE = 2;
    public static final int ERROR_WHITELIST_FAIL = 3;
    
    private int         _code;
    private String      _url;
    
    public WidgetException(int code, String url) {
        super();
        _code = code;
        _url = url;
    }
    
    public String getMessage() {
        switch(_code) {
            case ERROR_NETWORK_NOT_AVAILABLE:
                return "The radio on the device is currently turned off or the battery "
                    + "is too low for the radio to be used.  As a result, the following "
                    + "resource could not be retrieved - "
                    + _url
                    + "\n\nPlease turn the radio on or "
                    + "recharge the battery and try again.  Contact your service provider "
                    + "if this problem persists.";
            case ERROR_INVALID_SIM:
                return "Please insert a valid SIM card.";
            case ERROR_INSUFFICIENT_COVERAGE:
                return "You are not currently in an area that can handle data communication.  "
                    + "As a result, the following resource could not be retrieved - "
                    + _url
                    + ".\n\nPlease "
                    + "try again when you are in a different location, or contact your service "
                    + "provider if the problem persists.";
            case ERROR_WHITELIST_FAIL:
                return "The resource - "
                    + _url
                    + " - could not be retrieved because it was not found in the config.xml.\n\n"
                    + "Please verify the <access> elements in the WebWorks config.  Contact your "
                    + "service provider if the problem persists.";
            default:
                return "An unknown error has occured.";
        }
    }
}

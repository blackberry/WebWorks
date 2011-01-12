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
            // check for a protocol - mks354080
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
                // throw exception - exit compilation
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

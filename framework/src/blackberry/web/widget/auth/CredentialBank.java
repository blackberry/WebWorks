package blackberry.web.widget.auth;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;
import net.rim.device.api.browser.field2.BrowserFieldRequest;

public class CredentialBank {
    private static Hashtable _credentials;

    static {
        _credentials = new Hashtable();
    }

    public static boolean hasCredential( String host ) {
        if( !_credentials.containsKey( host ) ) {
            return false;
        } else {
            Credential credential = (Credential) _credentials.get( host );
            return credential.isVerified();
        }
    }

    public static void storeCredential( String host, String user, String pw ) {
        _credentials.put( host, new Credential( user, pw ) );
    }

    public static BrowserFieldRequest createRequestInCredentialBank( HttpConnection response, BrowserFieldRequest request ) {
        Credential credential = (Credential) _credentials.get( response.getHost() );
        if( credential == null ) {
            return null;
        }

        return Authenticator.createRequestWithAuthentication( credential.getUser(), credential.getPassword(), response, request );
    }

    public static void removeCredential( String host ) {
        _credentials.remove( host );
    }

    public static void verifyCredential( String host ) {
        Credential credential = (Credential) _credentials.get( host );
        if( credential != null ) {
            credential.setVerified();
        }
    }
}

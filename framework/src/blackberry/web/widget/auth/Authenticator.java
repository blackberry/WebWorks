package blackberry.web.widget.auth;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.ui.UiApplication;

import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.io.http.HttpHeaders;

import net.rim.device.api.util.StringUtilities;
import net.rim.device.api.ui.component.Dialog;

public class Authenticator {
    // In the future, may consider organize the protocol name and handler with a map/hashtable
    public final static String BASIC_PROTOCOL = "basic";

    public final static String WWW_AUTHENTICATION_HEADER = "www-authenticate";
    public final static String RIM_AUTHENTICATION_HEADER = "x-rim-authenticate";

    public static BrowserFieldRequest getAuthenticationRequest( HttpConnection response, BrowserFieldRequest request ) {
        String hostName = response.getHost();

        if( !CredentialBank.hasCredential( hostName ) ) {
            String domain = getAuthenticationDomain( response );
            System.out.println( "NTLM==>getAuthenticationDomain: " + domain );
            invokeAuthenticationDialogAndStoreCredential( hostName, domain );
        }

        return CredentialBank.createRequestInCredentialBank( response, request );
    }

    public static void verifyCredential( HttpConnection response ) {
        String hostName = response.getHost();
        CredentialBank.verifyCredential( hostName );
    }

    private static void invokeAuthenticationDialogAndStoreCredential( String hostName, String domain ) {
        // ask user for credential
        final AuthenticationScreen askCredential = new AuthenticationScreen( hostName, domain );

        UiApplication.getUiApplication().invokeAndWait( new Runnable() {
            public void run() {
                int ret = askCredential.doModal();
                if( ret == Dialog.CANCEL ) {
                    askCredential.setCancelled();
                }
            }
        } );

        String username = askCredential.getUsername();
        String password = askCredential.getPassword();
        CredentialBank.storeCredential( hostName, username, password );
    }

    public static BrowserFieldRequest createRequestWithAuthentication( String user, String password, HttpConnection response,
            BrowserFieldRequest request ) {
        if( user == null || user.length() == 0 || password == null || password.length() == 0 ) {
            return request;
        }

        String authCredential = user + ":" + password;

        // Encode the login information in Base64 format.
        byte[] encoded = null;

        try {
            encoded = Base64OutputStream.encode( authCredential.getBytes(), 0, authCredential.length(), false, false );
        } catch( IOException ioe ) {
            encoded = null;
        }

        if( encoded == null ) {
            return request;
        }

        String base64AuthCredential = new String( encoded );
        HttpHeaders httpHead = request.getHeaders().cloneHeaders();
        httpHead.setProperty( "Authorization", "Basic " + base64AuthCredential );

        return new BrowserFieldRequest( request.getURL(), request.getPostData(), httpHead );
    }

    private static String getAuthenticationDomain( HttpConnection response ) {
        final String domainToken = "domain=";
        String value = null;
        String headerField;
        int n = 0;
        try {
            while( response.getHeaderField( n ) != null ) {
                headerField = StringUtilities.removeChars( response.getHeaderField( n ), " " );
                if( headerField.toLowerCase().indexOf( domainToken ) == 0 ) {
                    value = headerField.substring( domainToken.length() );
                    break;
                }
                n++;
            }
        } catch( IOException ioe ) {
        }
        return value;
    }
}

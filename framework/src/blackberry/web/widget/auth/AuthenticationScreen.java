package blackberry.web.widget.auth;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.PasswordEditField;

public class AuthenticationScreen extends Dialog {
    private EditField usernameField;
    private EditField domainField;
    private PasswordEditField passwordField;
    private boolean cancelled;

    public AuthenticationScreen( String host, String domain ) {
        super( Dialog.D_OK_CANCEL, loginMessage( host ), 1, Bitmap.getPredefinedBitmap( Bitmap.QUESTION ), Manager.FOCUSABLE );
        usernameField = new EditField( "User Name: ", "", 50, EditField.EDITABLE );
        domainField = new EditField( "Domain: ", domain != null?domain:"", 50, EditField.EDITABLE );
        passwordField = new PasswordEditField( "Password: ", "", 50, EditField.EDITABLE );

        cancelled = false;

        add( usernameField );
        add( domainField );
        add( passwordField );
    }

    public String getUsername() {
        if( !cancelled ) {
            String domain = domainField.getText().trim();
            if (domain.length() > 0) {
                return (domain + "\\" + usernameField.getText());
            } else {
                return usernameField.getText();
            }
        } else {
            return "";
        }
    }

    public String getPassword() {
        if( !cancelled ) {
            return passwordField.getText();
        } else {
            return "";
        }
    }

    public void setCancelled() {
        cancelled = true;
    }

    private static String loginMessage( String host ) {
        return "Content at " + host + " requires authentication. Please enter username and password";
    }
}

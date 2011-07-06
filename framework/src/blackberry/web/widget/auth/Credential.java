package blackberry.web.widget.auth;

import java.io.IOException;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.browser.field2.BrowserFieldRequest;

public class Credential {
    private String _user;
    private String _pw;
    private boolean _verified;

    public Credential( String user, String pw ) {
        _user = user;
        _pw = pw;
        _verified = false;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _pw;
    }

    public boolean isVerified() {
        return _verified;
    }

    public void setVerified() {
        _verified = true;
    }
}

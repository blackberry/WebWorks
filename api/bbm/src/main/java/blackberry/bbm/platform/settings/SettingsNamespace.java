package blackberry.bbm.platform.settings;

import blackberry.bbm.platform.BBMPlatformNamespace;
import net.rim.blackberry.api.bbm.platform.SettingsManager;
import net.rim.device.api.script.Scriptable;

public class SettingsNamespace extends Scriptable {
    
    public static final String NAME = "settings";

    private static final String FIELD_PROFILE_BOX =  "profileboxEnabled";
    private static final String FIELD_PUBLIC_CONNS = "alwaysAllowPublicConns";
    
    private static SettingsNamespace _instance;
    
    private final SettingsManager _settings;
    
    public SettingsNamespace(SettingsManager settings) {
        _settings = settings;
    }
    
    public static SettingsNamespace getInstance() {
        if(_instance == null) {
            final SettingsManager settings = BBMPlatformNamespace.getInstance().getContext().getSettingsManager();
            _instance = new SettingsNamespace(settings);
        }
        return _instance;
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FIELD_PROFILE_BOX)) {
            return new Boolean(_settings.getSetting(SettingsManager.SETTING_PROFILE_BOX) == SettingsManager.VALUE_ENABLED);
        } else if(name.equals(FIELD_PUBLIC_CONNS)) {
            return new Boolean(_settings.getSetting(SettingsManager.SETTING_ALWAYS_ALLOW_PUBLIC_CONN) == SettingsManager.VALUE_ENABLED);
        } else {
            return UNDEFINED;
        }
    }
}

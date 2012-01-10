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
package blackberry.bbm.platform;

import net.rim.blackberry.api.bbm.platform.BBMPlatformApplication;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.BBMPlatformManager;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBox;
import net.rim.blackberry.api.bbm.platform.service.MessagingService;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.UiApplication;
import blackberry.bbm.platform.io.IONamespace;
import blackberry.bbm.platform.io.MessagingServiceListenerImpl;
import blackberry.bbm.platform.self.SelfNamespace;
import blackberry.bbm.platform.settings.SettingsNamespace;
import blackberry.bbm.platform.ui.UINamespace;
import blackberry.bbm.platform.users.UsersNamespace;
import blackberry.bbm.platform.util.ConstantsUtil;
import blackberry.bbm.platform.util.ScriptableFieldManager;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

public final class BBMPlatformNamespace extends Scriptable {
    
    public static final String NAME = "blackberry.bbm.platform";

    public static final String FUNC_REGISTER =                "register";
    public static final String FUNC_REQUEST_USER_PERMISSION = "requestUserPermission";
    public static final String FUNC_SHOW_OPTIONS =            "showBBMAppOptions";
    public static final String FIELD_ENVIRONMENT =            "environment";
    public static final String EVENT_ON_APP_INVOKED =         "onappinvoked";
    public static final String EVENT_ON_ACCESS_CHANGED =      "onaccesschanged";
    
    private static BBMPlatformNamespace _instance;
    private static BBMPlatformContext _bbmpContext;
        
    private final ScriptableFieldManager _wFields;
    
    private UserProfileBox _profileBox;
    private MessagingService _msgService;
    private MessagingServiceListenerImpl _msgServiceListener;
    
    private BBMPlatformNamespace() {
        _wFields = new ScriptableFieldManager();
        _wFields.addField(EVENT_ON_ACCESS_CHANGED);
        _wFields.addField(EVENT_ON_APP_INVOKED);
    }
    
    public static BBMPlatformNamespace getInstance() {
        if(_instance == null) {
            _instance = new BBMPlatformNamespace();
        }
        
        return _instance;
    }
    
    public void initBBMPlatformObjects() {
        _msgService = _bbmpContext.getMessagingService();
        _msgServiceListener = new MessagingServiceListenerImpl(IONamespace.getInstance());
        _msgService.setServiceListener(_msgServiceListener);
        UsersNamespace.getInstance().init();
        _profileBox = _bbmpContext.getUserProfile().getProfileBox();
    }
    
    public BBMPlatformContext getContext() {
        return _bbmpContext;
    }
    
    public MessagingService getMessagingService() {
        return _msgService;
    }
    
    public MessagingServiceListenerImpl getMessagingServiceListener() {
        return _msgServiceListener;
    }
    
    public UserProfileBox getProfileBox() {
        return _profileBox;
    }

    public Object getField(String name) throws Exception {
        if(name.equals(BBMPlatformNamespace.FUNC_REGISTER)) {
            return new RegisterFunction();
        } else if(name.equals(BBMPlatformNamespace.FUNC_REQUEST_USER_PERMISSION)) {
            return new RequestUserPermissionFunction();
        } else if(name.equals(BBMPlatformNamespace.FUNC_SHOW_OPTIONS)) {
            return new ShowOptionsFunction();
        } else if(name.equals(SelfNamespace.NAME)) {
            return SelfNamespace.getInstance();
        } else if(name.equals(UsersNamespace.NAME)) {
            return UsersNamespace.getInstance();
        } else if(name.equals(IONamespace.NAME)) {
            return IONamespace.getInstance();
        } else if(name.equals(UINamespace.NAME)) {
            return UINamespace.getInstance();
        } else if(name.equals(SettingsNamespace.NAME)) {
            return SettingsNamespace.getInstance();
        } else if(_wFields.hasField(name)){
            return _wFields.getField(name);
        } else if(name.equals(FIELD_ENVIRONMENT)) {
            return this.getEnvironment();  
        } else {
            return super.getField(name);
        }
    }
    
    public boolean putField(String name, Object value) throws Exception {
        return _wFields.putField(name, value);
    }
    
    private Object getEnvironment() {
        try {
            return ConstantsUtil.appEnvToString(_bbmpContext.getAppEnvironment());
        } catch(ControlledAccessException e) {
            return UNDEFINED;
        }
    }
    
    private class RegisterFunction extends ScriptableFunctionBase {
        
        public static final String OPTIONS_FIELD_UUID =              "uuid";
        public static final String OPTIONS_FIELD_SHARECONTENTSPLAT = "shareContentSplat";
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Object onAccessChanged = BBMPlatformNamespace.this.getField(EVENT_ON_ACCESS_CHANGED);
            if(onAccessChanged.equals(UNDEFINED)) {
                throw new IllegalStateException("blackberry.bbm.platform.onaccesschanged == undefined");
            }
            
            final Scriptable options = (Scriptable) args[0];
            final String uuid = (String) options.getField(OPTIONS_FIELD_UUID);
            
            // Get optional shareContentSplat property
            final Object shareContentSplatObj = options.getField(OPTIONS_FIELD_SHARECONTENTSPLAT);
            boolean shareContentSplat;
            try {
                shareContentSplat = ((Boolean) shareContentSplatObj).booleanValue();
            } catch(Exception e) {
                shareContentSplat = false;
            }
            final boolean finalShareContentSplat = shareContentSplat;
            
            final BBMPlatformApplication bbmApp = new BBMPlatformApplication(uuid) {
                public int getDefaultSettings() {
                    if(finalShareContentSplat) {
                        return super.getDefaultSettings() | BBMPlatformContext.SETTING_SHARECONTENT_SPLAT;
                    } else {
                        return super.getDefaultSettings();
                    }
                    
                }
            };
            
            Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
                protected void dispatch() {
                    try {
                        _bbmpContext = BBMPlatformManager.register(bbmApp);
                        _bbmpContext.setListener(new BBMPlatformContextListenerImpl(BBMPlatformNamespace.this));
                    } catch(Exception e) {
                        Util.logError("BBMPlatformManager.register() threw " + e);
                    }
                }
            });
            
            return Scriptable.UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Scriptable.class, true);
            
            return new FunctionSignature[] {
                sig1
            };
        }
        
    } // RegisterFunction
    
    private class RequestUserPermissionFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];
            
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    UiApplication.getUiApplication().invokeLater(new Runnable() {
                       public void run() {
                           final boolean allowed = _bbmpContext.requestUserPermission();
                           Util.dispatchCallback(onComplete, new Object[] { new Boolean(allowed) });
                       }
                    });
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // RequestUserPermissionFunction
    
    private class ShowOptionsFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            
            final ScriptableFunction onComplete = (ScriptableFunction) args[0];
            
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    UiApplication.getUiApplication().invokeLater(new Runnable() {
                       public void run() {
                           _bbmpContext.requestAppSettings();
                           Util.dispatchCallback(onComplete, null);
                       }
                    });
                }
            });
            
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(ScriptableFunction.class, true);
            return new FunctionSignature[] {
                sig1
            };
        }
    } // ShowOptionsFunction
}

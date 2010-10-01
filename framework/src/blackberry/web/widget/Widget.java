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
package blackberry.web.widget;

import blackberry.web.widget.WidgetScreen;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.listener.HardwareKeyListener;

import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.loadingScreen.PageManager;
import blackberry.web.widget.util.WidgetUtil;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import net.rim.blackberry.api.homescreen.HomeScreen;

import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.web.jse.BlackBerryWidgetToolkit;
import net.rim.device.api.system.RuntimeStore;

import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.system.ApplicationManager;
import java.lang.ref.WeakReference;
import net.rim.device.api.system.RuntimeStore;
import java.lang.Math;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.Application;

/**
 * 
 */
public class Widget extends UiApplication implements GlobalEventListener {
    private WidgetConfig                    _wConfig;
    private HardwareKeyListener             _hardwareKeyListener;
    public BrowserFieldScreen               _bfScreen;
    private String                                                      _locationURI;
    public static final long                WIDGET_GUID = Long.parseLong(Math.abs(Widget.class.getName().hashCode())+"",16);
    private static final String                         WIDGET_STARTUP_ENTRY = "rim:runOnStartup";
    private static final String                         WIDGET_FOREGROUND_ENTRY = "rim:foreground";
    
    
    public Widget(WidgetConfig wConfig,String locationURI) {
        _wConfig = wConfig;
        initialize();
        _locationURI = locationURI;

        // Create PageManager
            PageManager pageManager = new PageManager(this, (WidgetConfigImpl) _wConfig);
            
            // Push screen
            WidgetScreen wScreen = new BrowserFieldScreen(this, pageManager,_locationURI);
            _bfScreen = (BrowserFieldScreen)wScreen;
            pageManager.pushScreens(_bfScreen);
            
            this.addGlobalEventListener(this); 
    }
    
    public WidgetConfig getConfig() {
        return _wConfig;
    }
    
    /**
     * Intercept for onForeground event
     */
    /* Override */ public void activate() {
        UiApplication.getApplication().addKeyListener(_hardwareKeyListener);
        BlackBerryWidgetToolkit.getInstance().triggerOverridableEvent(
            BlackBerryWidgetToolkit.EVT_APP_FOREGROUND, null);
        
        //If we're switching application from the background we should change location to the widget content source.
        if(_locationURI.equals(((WidgetConfigImpl)_wConfig).getBackgroundSource())) {
                changeLocation(((WidgetConfigImpl)_wConfig).getForegroundSource());
        }
    }
       
    /**
     * Intercept for onBackground event
     */
    /* Override */ public void deactivate() {
        
        UiApplication.getApplication().removeKeyListener(_hardwareKeyListener);
        BlackBerryWidgetToolkit.getInstance().triggerOverridableEvent(
            BlackBerryWidgetToolkit.EVT_APP_BACKGROUND, null);
    }
    
    public static void main(String[] args) {
        WidgetConfigImpl wConfig = new blackberry.web.widget.autogen.WidgetConfigAutoGen();
        EventLogger.register(WIDGET_GUID ,wConfig.getName(),EventLogger.VIEWER_STRING);
        
        if(isAppRunning(wConfig)) {
            String qsParams = argsToQuery(args,wConfig);
            ApplicationManager mgr = ApplicationManager.getApplicationManager();
            mgr.postGlobalEvent(WIDGET_GUID ,0,0,qsParams,null);
            makeDebugArgs(args,wConfig);
        } else {
            /* If the widget is launched during system startup,
            *  wait until system startup is complete.
            *  This will allow startup widget to create BrowserField without the ApplicaitonRegistry timeout error.
            */
            if (ApplicationManager.getApplicationManager().inStartup()) {
                waitForStartupComplete();
            }
            Widget widget = makeWidget(args,(WidgetConfigImpl)wConfig);
            makeDebugArgs(args,wConfig);
            widget.enterEventDispatcher();
        } 
    }
    
    private static void waitForStartupComplete() {
        // use ApplicationManager.waitForStartup() in 6.0 when it's available
        ApplicationManager manager = ApplicationManager.getApplicationManager();
        while(manager.inStartup()) {
            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {}
        }
        
        try {
            Thread.sleep(1500);
        } catch(InterruptedException e) {}        
    }
    
    private static void makeDebugArgs(String[] args,WidgetConfigImpl wConfig) {
        // Makes modifications to the args to support debugging logic
        if(wConfig.isDebugEnabled()) {
                int index = args.length-1;
                args[index]+=";WIDGET;DEBUG_ENABLED";
        }
    }
    
    private static Widget makeWidget(String[] args,WidgetConfigImpl wConfig) {
        String queryString = argsToQuery(args,wConfig);
        Widget widget = null;
        
        // Handle background only cases here.
        if(wConfig.getForegroundSource().length()==0) {
                widget = new Widget(wConfig,queryString) { 
                        protected boolean acceptsForeground() { 
                                return false;
                        }
                };
                widget.requestBackground(); // This may not be necessary.
        } else {
                widget = new Widget(wConfig,queryString);
        }
        return widget;
    }
    
    private static String argsToQuery(String[] args,WidgetConfigImpl wConfig) {
        //If parameters are not specified return a query string that handles default cases
        //If the widget is launched from the icon "WIDGET;" will be present.
        //If allow invoke params is false then fall back on the default cases.
        boolean rimEntryPoint = (args.length>0)?args[0].startsWith("rim:"):false;
        String foregroundSource = wConfig.getForegroundSource();
        String backgroundSource = wConfig.getBackgroundSource();
        
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        if(!wConfig.allowInvokeParams()&&!rimEntryPoint) {
            args= new String[0];
        }
        
        if(args.length==0||args[0].indexOf("WIDGET;")!=-1||rimEntryPoint) {
            if(foregroundSource.length()==0) {
                return backgroundSource;
            } else if(rimEntryPoint) {
                if(args[0].startsWith(WIDGET_FOREGROUND_ENTRY)) {
                        return foregroundSource;
                } else if(args[0].startsWith(WIDGET_STARTUP_ENTRY)) {
                        return backgroundSource;
                }
            } else {
                return foregroundSource;
            }
        }    
        
        // Otherwise form the query string.
        String queryString="";
        
        for(int i=0;i<args.length;i++) {
            queryString+=args[i]+"&";
        }
        queryString = queryString.substring(0,queryString.length()-1);
        return queryString;
    }
     
    private static boolean isAppRunning(WidgetConfigImpl wConfig) {
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        ApplicationDescriptor current = ApplicationDescriptor.currentApplicationDescriptor();
        int moduleHandle = current.getModuleHandle();
        ApplicationDescriptor[] descriptor = CodeModuleManager.getApplicationDescriptors(moduleHandle);
        
                if(wConfig.isStartupEnabled()) {
                        return mgr.getProcessId(descriptor[0])!=-1&&mgr.getProcessId(descriptor[1])!=-1;
                } 
        return false;
    }
      
    public void eventOccurred(long guid,int data0,int data1,Object object0,Object object1) {
        if(guid == WIDGET_GUID ) {
                changeLocation((String)object0);
                this.requestForeground();
        }
    }
    
    public void changeLocation(String uri) {
        if(!_locationURI.equals(uri)) {
                _locationURI = uri;
                _bfScreen.setLocation(uri);
        } 
    }
    
    private void initialize() {
        // Turn on key listener
        _hardwareKeyListener = HardwareKeyListener.getInstance();
        UiApplication.getApplication().addKeyListener(_hardwareKeyListener);
        
        // Register notification profile
        Hashtable notifications = _wConfig.getNotifications();        
        for (Enumeration e = notifications.keys() ; e.hasMoreElements() ;) {
            Object name = e.nextElement();
            NotificationsManager.registerSource(((Long)notifications.get(name)).longValue(), name, NotificationsConstants.DEFAULT_LEVEL);
        }
     }
}


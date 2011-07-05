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
package blackberry.web.widget;

import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.web.WidgetConfig;
import blackberry.core.ApplicationEventHandler;
import blackberry.core.EventService;
import blackberry.core.WidgetProperties;
import blackberry.common.push.PushDaemon;
import blackberry.common.settings.SettingsManager;
import blackberry.common.util.ID;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.listener.HardwareKeyListener;
import blackberry.web.widget.loadingScreen.PageManager;
import blackberry.web.widget.settings.SettingsStoreFactoryImpl;

public class Widget extends UiApplication implements GlobalEventListener {
    private WidgetConfig _wConfig;
    private HardwareKeyListener _hardwareKeyListener;
    public BrowserFieldScreen _bfScreen;
    private String _locationURI;
    public static final long WIDGET_GUID = Long.parseLong( Widget.class.getName().hashCode() + "", 16 );
    private static final String WIDGET_STARTUP_ENTRY = "rim:runOnStartup";
    private static final String WIDGET_FOREGROUND_ENTRY = "rim:foreground";
    private static boolean _launchFromRibbon;

    public Widget( WidgetConfig wConfig, String locationURI ) {
        _wConfig = wConfig;
        initialize();
        _locationURI = locationURI;

        // Create PageManager
        PageManager pageManager = new PageManager( this, (WidgetConfigImpl) _wConfig );

        // Push screen
        WidgetScreen wScreen = new BrowserFieldScreen( this, pageManager, _locationURI );
        _bfScreen = (BrowserFieldScreen) wScreen;
        pageManager.pushScreens( _bfScreen );

        this.addGlobalEventListener( this );
    }

    public WidgetConfig getConfig() {
        return _wConfig;
    }

    /**
     * @see net.rim.device.api.system#activate
     */
    public void activate() {
        EventService.getInstance().fireEvent( ApplicationEventHandler.EVT_APP_FOREGROUND, null, false );

        // If launched from ribbon, change the location to the widget content source.
        if( _launchFromRibbon ) {
            String page = ( (WidgetConfigImpl) _wConfig ).getForegroundSource();
            changeLocation( page );
        }
    }

    /**
     * @see net.rim.device.api.system#deactivate
     */
    public void deactivate() {
        EventService.getInstance().fireEvent( ApplicationEventHandler.EVT_APP_BACKGROUND, null, false );
    }

    public static void main( String[] args ) {
        WidgetConfigImpl wConfig = new blackberry.web.widget.autogen.WidgetConfigAutoGen();
        WidgetProperties.getInstance().setGuid( WIDGET_GUID );
        EventLogger.register( WIDGET_GUID, wConfig.getName(), EventLogger.VIEWER_STRING );

        ID.init(wConfig);
        SettingsManager.setFactory(new SettingsStoreFactoryImpl());

        if( args[ 0 ].indexOf( WIDGET_FOREGROUND_ENTRY ) != -1 ) {
            _launchFromRibbon = true;
        } else {
            _launchFromRibbon = false;
        }
        
        // push entry
        if( args != null && args.length > 0 && args[ 0 ].equals( "PushDaemon" ) ) {
            String page = args[ 1 ];
            int maxQueueCap = Integer.parseInt( args[ 2 ] );
            PushDaemon daemon = new PushDaemon( page, maxQueueCap );
            daemon.enterEventDispatcher();
            return;
        }

        if( isAppRunning( wConfig ) ) {
            String qsParams = argsToQuery( args, wConfig );
            ApplicationManager mgr = ApplicationManager.getApplicationManager();
            mgr.postGlobalEvent( WIDGET_GUID, 0, 0, qsParams, null );
            makeDebugArgs( args, wConfig );
        } else {
            /*
             * If the WebWorks Application is launched during system startup, wait until system startup is complete. This will
             * allow startup widget to create BrowserField without the ApplicaitonRegistry timeout error.
             */
            if( ApplicationManager.getApplicationManager().inStartup() ) {
                waitForStartupComplete();
            }
            Widget widget = makeWidget( args, wConfig );
            makeDebugArgs( args, wConfig );
            widget.enterEventDispatcher();
        }
    }

    private static void waitForStartupComplete() {
        // use ApplicationManager.waitForStartup() in 6.0 when it's available
        ApplicationManager manager = ApplicationManager.getApplicationManager();
        while( manager.inStartup() ) {
            try {
                Thread.sleep( 2000 );
            } catch( InterruptedException e ) {
            }
        }

        try {
            Thread.sleep( 1500 );
        } catch( InterruptedException e ) {
        }
    }

    private static void makeDebugArgs( String[] args, WidgetConfigImpl wConfig ) {
        // Makes modifications to the args to support debugging logic
        if( !wConfig.isDebugEnabled() ) {
            args[ 0 ] = "WIDGET;";
        }
    }

    private static Widget makeWidget( String[] args, WidgetConfigImpl wConfig ) {
        String queryString = argsToQuery( args, wConfig );
        Widget widget = null;

        // Handle background only cases here.
        if( wConfig.getForegroundSource().length() == 0 ) {
            widget = new Widget( wConfig, queryString ) {
                protected boolean acceptsForeground() {
                    return false;
                }
            };
            widget.requestBackground(); // This may not be necessary.
        } else {
            widget = new Widget( wConfig, queryString );
        }
        return widget;
    }

    private static String argsToQuery( String[] args, WidgetConfigImpl wConfig ) {

        // If parameters are not specified return a query string that handles default cases
        // If the WebWorks Application is launched from the icon "WIDGET;" will be present.
        // If allow invoke params is false then fall back on the default cases.
        boolean rimEntryPoint = ( args.length > 0 ) ? args[ 0 ].indexOf( "rim:" ) != -1 : false;
        String foregroundSource = wConfig.getForegroundSource();
        String backgroundSource = wConfig.getBackgroundSource();

        ApplicationManager.getApplicationManager();
        if( !wConfig.allowInvokeParams() && !rimEntryPoint ) {
            args = new String[ 0 ];
        }

        if( args.length == 0 || args[ 0 ].indexOf( "WIDGET;" ) != -1 || rimEntryPoint ) {
            if( foregroundSource.length() == 0 ) {
                return backgroundSource;
            } else if( rimEntryPoint ) {
                if( args[ 0 ].indexOf( WIDGET_FOREGROUND_ENTRY ) != -1 ) {
                    return foregroundSource;
                } else if( args[ 0 ].indexOf( WIDGET_STARTUP_ENTRY ) != -1 ) {
                    return backgroundSource;
                }
            } else {
                return foregroundSource;
            }
        }

        // Otherwise form the query string
        StringBuffer strBuf = new StringBuffer();
        for( int i = 0; i < args.length; i++ ) {
            strBuf.append( args[ i ] + "&" );
        }
        String queryString = strBuf.toString();
        queryString = queryString.substring( 0, queryString.length() - 1 );
        return queryString;
    }

    private static boolean isAppRunning( WidgetConfigImpl wConfig ) {
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        ApplicationDescriptor current = ApplicationDescriptor.currentApplicationDescriptor();
        int moduleHandle = current.getModuleHandle();
        ApplicationDescriptor[] descriptors = CodeModuleManager.getApplicationDescriptors( moduleHandle );
        for (int i = 0; i < descriptors.length; i++) {
            if ( !descriptors[i].equals( current ) ) {
                if ( mgr.getProcessId( descriptors[ i ] ) != -1 ) {
                    return true;
                }
            }
        }
        return false;
    }

    public void eventOccurred( long guid, int data0, int data1, Object object0, Object object1 ) {
        if( guid == WIDGET_GUID ) {
            changeLocation( (String) object0 );
            this.requestForeground();
        }
    }

    public void changeLocation( String uri ) {
        if( !_locationURI.equals( uri ) ) {
            _locationURI = uri;
            _bfScreen.setLocation( uri );
        }
    }

    public HardwareKeyListener getHardwareKeyListener() {
        return _hardwareKeyListener;
    }

    private void initialize() {
        // create hardware key listener
        _hardwareKeyListener = new HardwareKeyListener( (WidgetConfigImpl) _wConfig );

        // Register notification profile
        Hashtable notifications = _wConfig.getNotifications();
        for( Enumeration e = notifications.keys(); e.hasMoreElements(); ) {
            Object name = e.nextElement();
            NotificationsManager.registerSource( ( (Long) notifications.get( name ) ).longValue(), name,
                    NotificationsConstants.DEFAULT_LEVEL );
        }
    }

}

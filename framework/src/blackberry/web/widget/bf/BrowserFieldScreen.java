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
package blackberry.web.widget.bf;

import java.util.Enumeration;

import net.rim.device.api.browser.field.RenderingOptions;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.io.transport.options.BisBOptions;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Trackball;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.web.WidgetExtension;
import blackberry.web.widget.Widget;
import blackberry.web.widget.WidgetScreen;
import blackberry.web.widget.bf.navigationcontroller.NavigationController;
import blackberry.web.widget.bf.navigationcontroller.NavigationExtension;
import blackberry.web.widget.caching.CacheManager;
import blackberry.web.widget.caching.WidgetCacheNamespace;
import blackberry.web.widget.device.DeviceInfo;
import blackberry.web.widget.html5.GearsHTML5Extension;
import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.loadingScreen.PageManager;
import blackberry.web.widget.util.WidgetUtil;

public final class BrowserFieldScreen extends WidgetScreen {

    // Set our preferred transport order.
    // Order is: MDS, BIS-B, TCP_WIFI, TCP_CELLULAR, WAP2, WAP.
    public static int[] PREFERRED_TRANSPORTS = { TransportInfo.TRANSPORT_MDS, TransportInfo.TRANSPORT_BIS_B,
            TransportInfo.TRANSPORT_TCP_WIFI, TransportInfo.TRANSPORT_TCP_CELLULAR, TransportInfo.TRANSPORT_WAP2,
            TransportInfo.TRANSPORT_WAP };

    private GearsHTML5Extension _HTML5ToGearsExtension;
    private BrowserField _browserField;
    private BrowserFieldConfig _bfConfig;
    private Manager _manager;
    private boolean _attached;

    private int _bgColor;

    private static BrowserField _browserFieldReference;

    private NavigationExtension _navigationJS;
    private NavigationController _navigationController;
    private NavigationNamespace _navigationExtension;    

    private PageManager _pageManager;

    private CacheManager _cacheManager;
    private WidgetCacheNamespace _widgetCacheExtension;
    private String _locationURI;

    /**
     * <description>
     * 
     * @param wConfig
     *            <description>
     */
    public BrowserFieldScreen( Widget widget, PageManager pageManager, String locationURI ) {
        super( widget, Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL );
        _locationURI = locationURI;
        _pageManager = pageManager;
        initialize();
    }

    /** Override */
    protected void onUiEngineAttached( boolean attached ) {
        // If an error occurs on the content page that requires
        // a dialog box, an exception was thrown because we tried to use
        // invokeAndWait which is a problem since while we process the first screen
        // we still haven't entered the application's event queue.
        // This puts the request for the first page on the event queue, so
        // it can be safely run.

        _attached = attached;
        if( _attached ) {
            Application.getApplication().invokeLater( new Thread() {
                public void run() {
                    _browserField.requestContent( createRequest( _locationURI ) );
                }
            } );
        }
    }

    public void setLocation( String url ) {
        _browserField.requestContent( createRequest( url ) );
        _browserField.getHistory().clearHistory();
    }

    /**
     * Overrides parent method to reset navigation controller after screen is closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close() {
        if( getAppNavigationMode() ) {
            getNavigationController().clearEventQueue();
        }
        super.close();
    }

    private void initialize() {
        _bfConfig = new BrowserFieldConfig();

        // POINTER mode is used for for normal WebWorks Applications.
        // NONE mode is used for navigation mode WebWorks Applications.
        if( getAppNavigationMode() ) {
            _bfConfig.setProperty( BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_NONE );
        } else {
            _bfConfig.setProperty( BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER );
        }

        // Enable Google Gears.
        _bfConfig.setProperty( BrowserFieldConfig.ENABLE_GEARS, Boolean.TRUE );

        // Enable Cross-Site XHR by default.
        _bfConfig.setProperty( BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE );

        // Disable MDS transcoding since it interferes with whitelist on external sites.
        _bfConfig.setProperty( BrowserFieldConfig.MDS_TRANSCODING_ENABLED, Boolean.FALSE );

        // Enable web inspector debugging if required
        if( _wConfig instanceof WidgetConfigImpl ) {
            WidgetConfigImpl configObj = (WidgetConfigImpl) _wConfig;
            if( DeviceInfo.isCompatibleVersion( 7 ) && configObj.isDebugEnabled() ) {
                _bfConfig.setProperty( "ENABLE_WEB_INSPECTOR", Boolean.TRUE );
            }
        }

        // Check for our Config type and cast.
        if( _wConfig instanceof WidgetConfigImpl ) {
            // Update the transport order.
            updateConnectionFactory();
        }

        // Create Browser field.
        _browserField = new BrowserField( _bfConfig );
        _browserField.addListener( new WidgetBrowserFieldListener( _wConfig ) );

        // Remove animation max value.
        _browserField.getRenderingOptions().setProperty( RenderingOptions.CORE_OPTIONS_GUID,
                RenderingOptions.ANIMATION_COUNT_VALUE, Integer.MAX_VALUE );

        // Enable blackberry.location by default.
        _browserField.getRenderingOptions().setProperty( RenderingOptions.CORE_OPTIONS_GUID,
                RenderingOptions.JAVASCRIPT_LOCATION_ENABLED, true );

        // Add a custom controller to handle requests.
        _browserField.getConfig().setProperty( BrowserFieldConfig.CONTROLLER,
                new WidgetRequestController( _browserField, _wConfig ) );

        // Add a custom error handler.
        _browserField.getConfig().setProperty( BrowserFieldConfig.ERROR_HANDLER,
                new BrowserFieldCustomErrorHandler( _browserField, _wConfig ) );

        // Add custom headers.
        if( _wConfig.getCustomHeaders().size() > 0 ) {
            _browserField.getConfig().setProperty( BrowserFieldConfig.HTTP_HEADERS, _wConfig.getCustomHeaders() );
        }

        // Create field manager.
        if( getAppNavigationMode() ) {
            _manager = new WidgetFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Manager.HORIZONTAL_SCROLL
                    | Manager.HORIZONTAL_SCROLLBAR );

            // navController depends on navExtension. Initialize navExtension first
            _navigationJS = new NavigationExtension();
            _navigationExtension = new NavigationNamespace( this, (WidgetFieldManager) _manager );
            _navigationController = new NavigationController( this );
        } else {
            _manager = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Manager.HORIZONTAL_SCROLL
                    | Manager.HORIZONTAL_SCROLLBAR );
        }

        // Add BrowserField/Manager to the Screen.
        _manager.add( _browserField );
        add( _manager );

        _bgColor = processColorString( _wConfig.getLoadingScreenColor() );

        // Set background color of the browserfield.
        // -1 denotes an invalid color.
        if( _bgColor != -1 ) {
            Background color = BackgroundFactory.createSolidBackground( _bgColor );
            _browserField.setBackground( color );
            _manager.setBackground( color );
            this.setBackground( color );
            this.getMainManager().setBackground( color );
        }

        // Register extensions.
        Enumeration ext = _wConfig.getExtensions();
        while(ext.hasMoreElements()) {
        	Object extension = ext.nextElement();
        	if( extension instanceof WidgetExtension ){
        		((WidgetExtension) extension ).register( _wConfig, _browserField );
        	}
        }

        // Update the static reference of browser field.
        _browserFieldReference = _browserField;

        // Create the CacheManager to handle caching functions.
        _cacheManager = null;
        if( _wConfig instanceof WidgetConfigImpl ) {
            WidgetConfigImpl wConfigImpl = (WidgetConfigImpl) _wConfig;
            if( wConfigImpl.isCacheEnabled() ) {
                _cacheManager = new CacheManager( wConfigImpl );
            }
            _widgetCacheExtension = new WidgetCacheNamespace( this );
        }

        if( DeviceInfo.isBlackBerry5() ) {
            _HTML5ToGearsExtension = new GearsHTML5Extension();
        }
    }

    /**
     * <description> Obtain a handle to the BrowserField that resides in this Screen.
     * 
     * @return <description>
     */
    public static BrowserField getBrowserField() {
        return _browserFieldReference;
    }

    private BrowserFieldRequest createRequest( String url ) {
        BrowserFieldRequest result = new BrowserFieldRequest( WidgetUtil.getLocalPath( url ) );
        return result;
    }

    /* Paints a bgcolor if the page is not loaded yet. */
    protected void paint( Graphics graphics ) {
        super.paint( graphics );
    }

    /*
     * Process a string in the format "#000000" and return the hex int value. -1 denotes an invalid color.
     */
    private int processColorString( String colorString ) {
        // Remove leading #
        String str = colorString;
        if( str != null && str.startsWith( "#" ) && str.length() == 7 ) {
            str = str.substring( 1 );

            // Attempt to convert string to hex.
            try {
                return Integer.parseInt( str, 16 );
            } catch( Exception e ) {
                return -1;
            }
        } else {
            // Failed to determine color.
            return -1;
        }
    }

    /**
     * Sets the ConnectionFactory to have transport settings from the WidgetConfigImpl.
     */
    private void updateConnectionFactory() {
        // Cast the WidgetConfig to WidgetConfigImpl version.
        WidgetConfigImpl wConfigImpl = (WidgetConfigImpl) _wConfig;
        ConnectionFactory connFact = new ConnectionFactory();

        // Set default transport order.
        connFact.setPreferredTransportTypes( PREFERRED_TRANSPORTS );

        // Set default time out for all connections to 30 seconds.
        connFact.setTimeLimit( 30000L );

        // Set transports if specified in config.xml.
        if( wConfigImpl.getPreferredTransports() != null ) {
            connFact.setPreferredTransportTypes( wConfigImpl.getPreferredTransports() );

            // Set the timeout for transports.
            if( wConfigImpl.getTransportTimeout() != null && wConfigImpl.getTransportTimeout().intValue() >= 0 ) {
                // setTimeLimit sets the max time limit for making a connection.
                connFact.setTimeLimit( wConfigImpl.getTransportTimeout().longValue() );
            }
        }

        // Set options.
        connFact.setTransportTypeOptions( TransportInfo.TRANSPORT_BIS_B, new BisBOptions( "mds-public" ) );
        connFact.setTimeoutSupported( true );

        // Set BrowserFieldConfig.
        _bfConfig.setProperty( BrowserFieldConfig.CONNECTION_FACTORY, connFact );

    }

    public GearsHTML5Extension getHTML5Extension() {
        return _HTML5ToGearsExtension;
    }

    public NavigationExtension getNavigationJS() {
        return _navigationJS;
    }
    
    public NavigationNamespace getNavigationExtension() {
        return _navigationExtension;
    }

    public NavigationController getNavigationController() {
        return _navigationController;
    }

    public WidgetFieldManager getWidgetFieldManager() {
        if( _manager instanceof WidgetFieldManager ) {
            return (WidgetFieldManager) _manager;
        }

        return null;
    }

    public BrowserField getWidgetBrowserField() {
        return _browserField;
    }

    public boolean getAppNavigationMode() {
        if( _wConfig instanceof WidgetConfigImpl ) {
            return ( ( (WidgetConfigImpl) _wConfig ).getNavigationMode() && Trackball.isSupported() );
        }

        return false;
    }

    public PageManager getPageManager() {
        return _pageManager;
    }

    public void suppressLoadingScreenForGoingBack() {
        _pageManager.setSuppressLoadingScreen();
    }

    public CacheManager getCacheManager() {
        return _cacheManager;
    }

    public WidgetCacheNamespace getWidgetCacheExtension() {
        return _widgetCacheExtension;
    }

    protected boolean onBackButton() {
        boolean result = false;

        // If the behaviour is 'exit', close instead of checking history.
        String backButtonSetting = ( (WidgetConfigImpl) _wConfig ).getBackButtonBehaviour();
        if( backButtonSetting != null && backButtonSetting.equalsIgnoreCase( "exit" ) ) {
            result = onClose();
        }
        // Default behaviour: Check history, go back if possible.
        // Do not allow going back if the page is in transition process.
        // This may cause conflicting threads to deadlock.
        else if( _browserField.getHistory().canGoBack() ) {
            // If the back button is pressed during page transitions, stop the transition.
            if( _pageManager.isGoingBackSafe() ) {
                _browserField.back();
            } else {
                // Do nothing for now.
            }
            result = true;
        } else {
            // Close the app if there is no previous history
            result = onClose();
        }
        return result;
    }
}

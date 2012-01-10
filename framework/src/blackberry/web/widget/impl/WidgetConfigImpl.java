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
package blackberry.web.widget.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.InputConnection;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldController;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.web.WidgetFeature;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

import blackberry.core.IJSExtension;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.caching.WidgetCacheExtension;
import blackberry.web.widget.loadingScreen.TransitionConstants;
import blackberry.web.widget.util.WidgetUtil;

public abstract class WidgetConfigImpl implements WidgetConfig {

    // Provided by WidgetConfigImpl file
    // - parsed/populated by Web Component Pack generated file
    protected Hashtable _accessList;
    protected String _author;
    protected String _authorURL;
    protected String _authorEmail;
    protected String _copyright;
    protected String _configXML;
    protected String _content;
    protected String _contentCharset;
    protected String _contentType;
    protected HttpHeaders _customHeaders;
    protected String _description;
    protected String _icon;
    protected String _iconHover;
    protected String _id;
    protected String _license;
    protected String _licenseURL;
    protected String _name;
    // Key: name of the profile. Value: unique for registering the profile
    protected Hashtable _notifications;
    protected String _loadingScreenColor;
    protected int[] _preferredTransports;
    protected Integer _transportTimeout;
    protected String _backButtonBehaviour;
    protected boolean _hasMultiAccess;
    protected boolean _widgetNavigationMode;

    // private fields
    protected Vector _widgetExtensions;
    private Vector _keyListeners;
    private Hashtable _featureTable;
    private WidgetAccess[] _accessArray;
    private Document _configXMLDoc;
    private String _version;

    // loading screen configuration
    protected String _backgroundImage;
    protected String _foregroundImage;
    protected boolean _firstPageLoad;
    protected boolean _remotePageLoad;
    protected boolean _localPageLoad;
    protected int _transitionType;
    protected int _transitionDuration;
    protected int _transitionDirection;
	
	// orientation configuration
	protected int _orientation;
	protected boolean _orientationDefined;

    // caches configuration
    protected boolean _cacheEnabled;
    protected boolean _aggressivelyCaching;
    protected int _aggressiveCacheAge;
    protected int _overrodeAge;
    protected int _maxCacheable;
    protected int _maxCacheSize;
    // Key: file extension. Value: mime type
    protected Hashtable _allowedUriTypes;

    // Auto-Startup members
    protected boolean _runOnStartup;
    protected boolean _allowInvokeParams;
    protected String _backgroundSource;
    protected String _foregroundSource;
    protected boolean _debugEnabled = false;

    // JavaScript paths need to be injected.
    protected SimpleSortingVector _jsInjectionPaths = new SimpleSortingVector();
    protected SimpleSortingVector _sharedGlobalJSInjectionPaths = new SimpleSortingVector();

    /**
     * Protected construtor.
     */
    protected WidgetConfigImpl() {
        _customHeaders = new HttpHeaders();
        _notifications = new Hashtable();
        _accessList = new Hashtable();
        _featureTable = new Hashtable();
        _widgetExtensions = new Vector();

        // Set defaults
        setVersion();
        _widgetExtensions.addElement( new WidgetCacheExtension() );

        // Set default value of loading screen configuration
        _backgroundImage = "";
        _foregroundImage = "";
        _firstPageLoad = false;
        _remotePageLoad = false;
        _localPageLoad = false;
        _transitionType = TransitionConstants.TRANSITION_NONE;
        _transitionDuration = TransitionConstants.DEFAULT_DURATION;
        _transitionDirection = TransitionConstants.DIRECTION_LEFT;
		
		// Set default orientation values
		_orientationDefined = false;
		_orientation = -1;

        // Set default value of cache configuration
        _cacheEnabled = true;
        _aggressivelyCaching = true;
        _aggressiveCacheAge = 2592000;
        _overrodeAge = 0;
        _maxCacheable = 131072;
        _maxCacheSize = 1048576;
        // If _allowedUriTypes is null, all Uri types are cacheable
        _allowedUriTypes = null;
    }

    // Implementation - net.rim.device.api.web.WidgetConfig
    public String getAuthor() {
        return _author;
    }

    public String getAuthorEmail() {
        return _authorEmail;
    }

    public String getAuthorURL() {
        return _authorURL;
    }

    public String getCopyright() {
        return _copyright;
    }

    public String getContent() {
        return _content;
    }

    public String getContentCharset() {
        return _contentCharset;
    }

    public String getContentType() {
        return _contentType;
    }

    public HttpHeaders getCustomHeaders() {
        return _customHeaders;
    }

    public String getDescription() {
        return _description;
    }

    public Enumeration getExtensions() {
        return _widgetExtensions.elements();
    }

    public WidgetExtension getExtensionForFeature( String featureID ) {
        return (WidgetExtension) _featureTable.get( featureID );
    }

    public Object getExtensionObjectForFeature( String featureID ) {
        return _featureTable.get( featureID );
    }

    public String getIcon() {
        return _icon;
    }

    public String getIconHover() {
        return _iconHover;
    }

    public String getID() {
        return _id;
    }

    public String getLicense() {
        return _license;
    }

    public String getLicenseURL() {
        return _licenseURL;
    }

    public String getName() {
        return _name;
    }

    public Hashtable getNotifications() {
        return _notifications;
    }

    public String getVersion() {
        return _version;
    }

    public String getLoadingScreenColor() {
        return _loadingScreenColor;
    }

    public int[] getPreferredTransports() {
        return _preferredTransports;
    }

    public Integer getTransportTimeout() {
        return _transportTimeout;
    }

    public String getBackButtonBehaviour() {
        return _backButtonBehaviour;
    }

    public boolean getNavigationMode() {
        return _widgetNavigationMode;
    }

    // Getters of loading screen configuration
    public String getBackgroundImage() {
        return _backgroundImage;
    }

    public String getForegroundImage() {
        return _foregroundImage;
    }

    public boolean getFirstPageLoad() {
        return _firstPageLoad;
    }

    public boolean getRemotePageLoad() {
        return _remotePageLoad;
    }

    public boolean getLocalPageLoad() {
        return _localPageLoad;
    }

    public int getTransitionType() {
        return _transitionType;
    }

    public int getTransitionDuration() {
        return _transitionDuration;
    }

    public int getTransitionDirection() {
        return _transitionDirection;
    }
	
	// Getters of orientation
	public boolean isOrientationDefined() {
		return _orientationDefined;
	}
	
	public int getOrientation() {
		return _orientation;
	}

    // Getters of cache configuration
    public boolean isCacheEnabled() {
        return _cacheEnabled;
    }

    public boolean getAggressivelyCaching() {
        return _aggressivelyCaching;
    }

    public int getAggressiveCacheAge() {
        return _aggressiveCacheAge;
    }

    public int getOverrodeAge() {
        return _overrodeAge;
    }

    public int getMaxCacheable() {
        return _maxCacheable;
    }

    public int getMaxCacheSize() {
        return _maxCacheSize;
    }

    public Hashtable getAllowedUriTypes() {
        return _allowedUriTypes;
    }

    // Getters of Auto-startup
    public boolean isStartupEnabled() {
        return _runOnStartup;
    }

    public boolean allowInvokeParams() {
        return _allowInvokeParams;
    }

    public String getBackgroundSource() {
        return _backgroundSource;
    }

    public String getForegroundSource() {
        return _foregroundSource;
    }

    public boolean isDebugEnabled() {
        return _debugEnabled;
    }

    /**
     * Checks all classes specified as WidgetExtension - determines if they also implement KeyListener. Then adds them to a
     * KeyListener vector.
     * 
     * @return a vector of KeyListener objects; an empty vector if no KeyListener objects are found.
     */
    public Vector getKeyListeners() {
        if( _keyListeners == null ) {
            _keyListeners = new Vector();
            Object ext = null;
            for( Enumeration e = _widgetExtensions.elements(); e.hasMoreElements(); ) {
                ext = e.nextElement();
                if( ext instanceof KeyListener ) {
                    _keyListeners.addElement( ext );
                }
            }
        }
        return _keyListeners;
    }

    public WidgetAccess[] getAccessList() {
        if( _accessArray != null ) {
            return _accessArray;
        }
        // 1-time parse
        _accessArray = new WidgetAccess[ _accessList.size() ];
        Enumeration accessEnum = _accessList.elements();
        int index = 0;
        while( accessEnum.hasMoreElements() ) {
            _accessArray[ index++ ] = (WidgetAccess) accessEnum.nextElement();
        }
        initFeatureTable();
        return _accessArray;
    }

    public Document getConfigXML() {
        // Parse only required on first-time request
        if( _configXMLDoc == null ) {
            if( _configXML == null || _configXML.length() == 0 ) {
                return null;
            }
            try {
                // Obtain the Browser field config
                BrowserFieldConfig bfConfig = getBrowserFieldConfig();

                // Create doc builder
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                // Parse
                Object o = bfConfig.getProperty( BrowserFieldConfig.CONTROLLER );
                if( o instanceof BrowserFieldController ) {
                    BrowserFieldController bfController = (BrowserFieldController) o;

                    // Create request for config.xml file
                    BrowserFieldRequest request = new BrowserFieldRequest( WidgetUtil.getLocalPath( _configXML ) );
                    InputConnection inputConn = bfController.handleResourceRequest( request );

                    // Create a Document object out of the config.xml
                    _configXMLDoc = docBuilder.parse( inputConn.openDataInputStream() );
                }

            } catch( Exception e ) {
            }
        }
        return _configXMLDoc;
    }

    public Hashtable getFeatureTable() {
        return _featureTable;
    }

    public boolean allowMultiAccess() {
        return _hasMultiAccess;
    }

    /**
     * <description> Retrieve paths of shared global JavaScript files to be injected.
     *
     * @return Vector contains JavaScript paths.
     */

    public SimpleSortingVector getSharedGlobalJSInjectionPaths() {
        return _sharedGlobalJSInjectionPaths;
    }
    
    /**
     * <description> Retrieve paths of extension JavaScript files to be injected.
     * 
     * @return Vector contains JavaScript paths.
     */

    public SimpleSortingVector getJSInjectionPaths() {
        return _jsInjectionPaths;
    }

    /**
     * <description> Obtains the BrowserFieldConfig object by accessing the Screen.
     *
     * @return <description>
     */
    private BrowserFieldConfig getBrowserFieldConfig() {
        BrowserField bField = BrowserFieldScreen.getBrowserField();
        return bField.getConfig();
    }

    /**
     * Version is retrieved based on the module itself - this should be set by tumbler when building the app. We don't want to
     * retrieve
     */
    private void setVersion() {
        int handle = CodeModuleManager.getModuleHandleForObject( this );
        _version = CodeModuleManager.getModuleVersion( handle );
    }

    /**
     * Initializes hashtable of the features found in the config.xml mapped to the corresponding WebWorks Extensions that have the
     * matching feature.
     */
    private void initFeatureTable() {
        // Enumerate access elements, map feature elements to proper extension
        // loop through <access>
        int waSize = _accessArray.length;
        for( int a = 0; a < waSize; a++ ) {
            WidgetAccess wa = _accessArray[ a ];
            WidgetFeature[] wfList = wa.getFeatures();
            if( wfList == null ) {
                continue;
            }
            // Loop through <feature>
            int wfSize = wfList.length;
            for( int b = 0; b < wfSize; b++ ) {
                if( !_featureTable.containsKey( wfList[ b ].getID() ) ) {
                    // add to table
                    matchFeature( wfList[ b ].getID(), _featureTable );
                }
            }
        }
    }

    private void matchFeature( String featureID, Hashtable featureTable ) {
        Enumeration e = _widgetExtensions.elements();
        while( e.hasMoreElements() ) {
            Object we = e.nextElement();
            String[] features;
            if( we instanceof WidgetExtension ) {
                features = ( (WidgetExtension) we ).getFeatureList();
            } else {
                features = ( (IJSExtension) we ).getFeatureList();
            }
            for( int a = 0; a < features.length; a++ ) {
                if( features[ a ].equals( featureID ) ) {
                    featureTable.put( featureID, we );
                    return;
                }
            }
        }
    }
}

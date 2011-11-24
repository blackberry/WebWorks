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
package net.rim.tumbler.config;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.session.SessionManager;

public class WidgetConfig {
	private String _content;
	private String _author;
	private String _authorEmail;
	private String _authorURL;
	private String _name;
	private String _version;
	
	protected int _orientation; // Portrait 0, Landscape 1
	protected boolean _orientationDefined;
	
	private String _loadingScreenColour;
	private String _backgroundImage;
	private String _foregroundImage;
	private boolean _firstPageLoad;
	private boolean _remotePageLoad;
	private boolean _localPageLoad;
	private String _transitionType;
	private int _transitionDuration;
	private String _transitionDirection;
	
	private String _copyright;
	private String _description;
	private Vector<String> _hoverIconSrc;
	private Vector<String> _iconSrc;
	private String _id;
	private Map<String, String> _customHeaders;
	private String _backButton;
	private boolean _navigationMode;
	private String _contentType;
	private String _contentCharSet;
	private String _license;
	private String _licenseURL;
	private int _transportTimeout;
	private String[] _transportOrder;
	private boolean _multiAccess;
	private String _configXML;
	private Hashtable<WidgetAccess, Vector<WidgetFeature>> _accessTable;
	private Vector<String> _extensionClasses;
	private Vector<String> _extensionJSFiles;
	private Vector<String> _sharedGlobalJSFiles;

	// Cache fields
    private Boolean               _cacheEnabled;
    private Boolean               _aggressivelyCaching;
    private Integer               _aggressiveCacheAge;    
    private Integer               _maxCacheable; 
    private Integer               _maxCacheSize; // Total cache size

    //Auto-Startup Fields
    private boolean 			  _runOnStartup;
    private boolean               _allowInvokeParams;
    private String                _backgroundSource;
    private String                _foregroundSource;
    
    // Debug issue
    private boolean _debugEnabled=false;
    
	public WidgetConfig() {
		
		// Set defaults
		_accessTable = new Hashtable<WidgetAccess, Vector<WidgetFeature>>();
		_hoverIconSrc = new Vector<String>();
		_customHeaders = new HashMap<String, String>();
		_iconSrc = new Vector<String>();
		_configXML = "config.xml";
		_transportTimeout = -1;
		
		_backgroundImage = null;
		_foregroundImage = null;
		_firstPageLoad = false;
		_remotePageLoad = false;
		_localPageLoad = false;
		_transitionType = null;
		_transitionDuration = -1;
		_transitionDirection = null;
		
		_cacheEnabled = null;
	    _aggressivelyCaching = null;
	    _aggressiveCacheAge= null;    
	    _maxCacheable = null; 
	    _maxCacheSize = null; 
		
		_orientationDefined = false;
		_orientation = -1;
	    
	    _runOnStartup=false;
	    _allowInvokeParams=false;
	    _backgroundSource=null;
	    _foregroundSource=null;
	    
	    _debugEnabled = SessionManager.getInstance().debugMode();
	}

	public void validate() {
		if (_version == null || _version.length() == 0) {
			Logger.logMessage(LogType.WARNING,
					"VALIDATION_CONFIGXML_MISSING_VERSION");
			_version = "1.0.0.0";
		}
	}

	public String getContent() {
		return _content;
	}

	public String getAuthor() {
		return _author;
	}

	public String getName() {
		return _name;
	}

	public String getVersion() {
		return _version;
	}

	public String getLoadingScreenColour() {
		return _loadingScreenColour;
	}

	public String getCopyright() {
		return _copyright;
	}

	public String getDescription() {
		return _description;
	}

	public int getOrientation() {
		return _orientation;
	}
	
	public boolean getOrientationDefined() {
		return _orientationDefined;
	}
	
	public Vector<String> getHoverIconSrc() {
		return _hoverIconSrc;
	}

	public Vector<String> getIconSrc() {
		return _iconSrc;
	}

	public void setContent(String content) {
		_content = content;
	}
	
	public void setOrientation(int value) {
		_orientation = value;
		_orientationDefined = true;
	}

	public void setAuthor(String author) {
		_author = author;
	}

	public void setName(String name) throws ValidationException {
		if (name == null || name.length() == 0) {
			throw new ValidationException(
					"EXCEPTION_CONFIGXML_MISSING_WIDGET_NAME");
		} else if ( name.indexOf(",") != -1 ) {
			throw new ValidationException(
					"EXCEPTION_CONFIGXML_INVALID_WIDGET_NAME" );
		}
		_name = name;
	}

	public void setVersion(String version) throws ValidationException {
		if (SessionManager.getInstance().isVerbose()) {
			Logger.logMessage(LogType.INFO,
					"PROGRESS_VALIDATING_CONFIG_XML_WIDGET_VERSION");
		}
		// version variable should look like one of the options:
		// version="a.b"
		// version="a.b.c"
		// version="a.b.c.d"
		String regex = "\\d{1,3}\\.\\d{1,3}(\\.\\d{1,3}){0,2}$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(version);

		if (!matcher.matches()) {
			throw new ValidationException("EXCEPTION_CONFIGXML_INVALID_VERSION");
		}
		_version = version;
	}

	public void setLoadingScreenColour(String screenColour)
			throws ValidationException {

		if (screenColour != null) {
			if (SessionManager.getInstance().isVerbose()) {
				Logger.logMessage(LogType.INFO,
						"PROGRESS_VALIDATING_CONFIG_XML_LOADINGSCREEN_COLOR");
			}
			// Color variable should look like: #000000
			String regex = "^#[A-Fa-f0-9]{6}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(screenColour);
			if (!matcher.matches()) {
				throw new ValidationException(
						"EXCEPTION_CONFIGXML_LOADINGSCREEN_COLOUR");
			}
		}
		
		_loadingScreenColour = screenColour;
	}

	public String getBackgroundImage() {
		return _backgroundImage;
	}

	public void setBackgroundImage(String src) {
		_backgroundImage = src;
	}
	
	public String getForegroundImage() {
		return _foregroundImage;
	}

	public void setForegroundImage(String src) {
		_foregroundImage = src;
	}
	
	public boolean getFirstPageLoad() {
		return _firstPageLoad;
	}

	public void setFirstPageLoad(boolean value) {
		_firstPageLoad = value;
	}	

	public boolean getRemotePageLoad() {
		return _remotePageLoad;
	}

	public void setRemotePageLoad(boolean value) {
		_remotePageLoad = value;
	}	
	
	public boolean getLocalPageLoad() {
		return _localPageLoad;
	}

	public void setLocalPageLoad(boolean value) {
		_localPageLoad = value;
	}	
	
	public String getTransitionType() {
		return _transitionType;
	}

	public void setTransitionType(String value) {
		_transitionType = value;
	}		
	
	public int getTransitionDuration() {
		return _transitionDuration;
	}

	public void setTransitionDuration(int value) {
		_transitionDuration = value;
	}	
	
	public String getTransitionDirection() {
		return _transitionDirection;
	}

	public void setTransitionDirection(String value) {
		_transitionDirection = value;
	}	
	
	public void setCopyright(String copyright) {
		_copyright = copyright;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void addHoverIcon(String icon) {
		_hoverIconSrc.add(icon);
	}

	public void addIcon(String icon) {
		_iconSrc.add(icon);
	}

	public String getID() {
		return _id;
	}

	public void setID(String id) {
		_id = id;
	}

	public Map<String, String> getCustomHeaders() {
		return _customHeaders;
	}

	public void addHeader(String key, String value) {
		_customHeaders.put(key, value);
	}

	public String getBackButtonBehaviour() {
		return _backButton;
	}

	public void setBackButtonBehaviour(String value) {
		_backButton = value;
	}

	public boolean getNavigationMode() {
		return _navigationMode;
	}

	public void setNavigationMode(boolean value) {
		_navigationMode = value;
	}

	public String getContentType() {
		return _contentType;
	}

	public String getContentCharSet() {
		return _contentCharSet;
	}

	public void setContentType(String type) {
		_contentType = type;
	}

	public void setContentCharSet(String charSet) {
		_contentCharSet = charSet;
	}

	public String getLicense() {
		return _license;
	}

	public String getLicenseURL() {
		return _licenseURL;
	}

	public void setLicense(String license) {
		_license = license;
	}

	public void setLicenseURL(String licenseurl) {
		_licenseURL = licenseurl;
	}

	public void setAuthorURL(String authorURL) {
		_authorURL = authorURL;
	}

	public String getAuthorURL() {
		return _authorURL;
	}

	public void setAuthorEmail(String authorEmail) {
		_authorEmail = authorEmail;
	}

	public String getAuthorEmail() {
		return _authorEmail;
	}

	public void setTransportTimeout(int transportTimeout) {
		_transportTimeout = transportTimeout;
	}

	public int getTransportTimeout() {
		return _transportTimeout;
	}

	public void setTransportOrder(String[] transportOrder) {
		_transportOrder = transportOrder;
	}

	public String[] getTransportOrder() {
		return _transportOrder;
	}

	public boolean allowMultiAccess() {
		return _multiAccess;
	}

	public void setMultiAccess(boolean multiAccess) {
		_multiAccess = multiAccess;
	}

	public String getConfigXML() {
		return _configXML;
	}

	public void setConfigXML(String configXML) {
		_configXML = configXML;
	}

	public Hashtable<WidgetAccess, Vector<WidgetFeature>> getAccessTable() {
		return _accessTable;
	}

	public void setAccessTable(
			Hashtable<WidgetAccess, Vector<WidgetFeature>> table) {
		_accessTable = table;
	}

	public void setExtensionClasses(Vector<String> classes) {
		_extensionClasses = classes;
	}

	public Vector<String> getExtensionClasses() {
		return _extensionClasses;
	}
	
	public void setExtensionJSFiles(Vector<String> extensionJSFiles) {
		_extensionJSFiles = extensionJSFiles;
	}
	
	public Vector<String> getExtensionJSFiles() {
		return _extensionJSFiles;
	}
	
	public void setSharedGlobalJSFiles(Vector<String> sharedGlobalJSFiles) {
		_sharedGlobalJSFiles = sharedGlobalJSFiles;
	}
	
	public Vector<String> getSharedGlobalJSFiles() {
		return _sharedGlobalJSFiles;
	}
	
	// Cache field functions
	
	public Boolean isCacheEnabled() {
		return _cacheEnabled;
	}
	
	public void setCacheEnabled(boolean inputValue) {
		_cacheEnabled = inputValue;
	}
	
	public Boolean isAggressiveCacheEnabled() {
		return _aggressivelyCaching;
	}
	
	private void setAggressiveCache(boolean inputValue) {
		_aggressivelyCaching = inputValue;
	}
	
	public Integer getAggressiveCacheAge() {
		return _aggressiveCacheAge;
	}
	
	public void setAggressiveCacheAge(int inputValue) {
		// Enable aggressive cache flag if the value is above 0
		if (inputValue > 0){
			setAggressiveCache(true);
		} else if (inputValue == -1) {
			setAggressiveCache(false);
		}
		
		// Max value is 30 days
		if(inputValue <= 2592000){
			_aggressiveCacheAge = inputValue;
		}
	}
	
	public Integer getMaxCacheSize() {
		return _maxCacheSize;
	}
	
	public void setMaxCacheSize(int inputValue) {
		// Min value of 0, max value of 2048 KB
	    final int kb_2048 = 2048 * 1024;
		if (inputValue >= 0 && inputValue <= (kb_2048)){
			_maxCacheSize = inputValue;
		} else if (inputValue > kb_2048) {
			_maxCacheSize = kb_2048;
		}
	}
	
	public Integer getMaxCacheItemSize() {
		return _maxCacheable;
	}
	
	public void setMaxCacheItemSize(int inputValue) {
		// -1 is a valid value
		if (inputValue >= -1){
			_maxCacheable = inputValue;
		}
	}

	//Auto-Startup Accessors and Mutators
	
	public Boolean isStartupEnabled() {
		return _runOnStartup;
	}

	public void setStartup(Boolean runOnStartup) {
		_runOnStartup = runOnStartup;
	}

	public String getBackgroundSource() {
		return _backgroundSource;
	}
	
	public String getForegroundSource() {
		return _foregroundSource;
	}
	
	public void setForegroundSource(String foregroundSource) {
		_foregroundSource = foregroundSource;
	}

	public void setBackgroundSource(String backgroundSource) {
		_backgroundSource = backgroundSource;
	}

	public Boolean allowInvokeParams() {
		return _allowInvokeParams;
	}

	public void setAllowInvokeParams(Boolean allowInvokeParams) {
		_allowInvokeParams = allowInvokeParams;
	}
	
	public boolean isDebugEnabled() {
		return _debugEnabled;
	}

}

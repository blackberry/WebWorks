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
package blackberry.system;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import org.w3c.dom.Document;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetException;
import blackberry.common.util.JSUtilities;
import blackberry.common.util.json4j.JSONObject;
import blackberry.core.IJSExtension;
import blackberry.core.JSExtensionRequest;
import blackberry.core.JSExtensionResponse;
import blackberry.core.JSExtensionReturnValue;

/**
 * JavaScript extension for blackberry.system
 */
public class SystemExtension implements IJSExtension, ISystemExtensionConstants {
    private static Vector SUPPORTED_METHODS;    

    static {
        SUPPORTED_METHODS = new Vector();
        SUPPORTED_METHODS.addElement( FUNCTION_HAS_PERMISSION );
        SUPPORTED_METHODS.addElement( FUNCTION_HAS_CAPABILITY );
        SUPPORTED_METHODS.addElement( FUNCTION_HAS_DATA_COVERAGE );
        SUPPORTED_METHODS.addElement( FUNCTION_IS_MASS_STORAGE_ACTIVE );
        SUPPORTED_METHODS.addElement( FUNCTION_SET_HOME_SCREEN );
        SUPPORTED_METHODS.addElement( FUNCTION_GET );
        SUPPORTED_METHODS.addElement( SOFTWARE_VERSION );
        SUPPORTED_METHODS.addElement( SCRIPT_API_VERSION );
        SUPPORTED_METHODS.addElement( MODEL );
    }

    private static String[] JS_FILES = { "system_dispatcher.js", "system_ns.js" };

    public String[] getFeatureList() {
        String[] featureList;
        featureList = new String[ 1 ];
        featureList[ 0 ] = FEATURE_ID;
        return featureList;
    }

    /**
     * @param capability is the ability that the device can perform </br>
     * Possible values are:</br><code>
     * input.keyboard.issuretype input.touch</br>
     * media.audio.capture media.video.capture media.recording</br>
     * location.gps location.maps storage.memorycard</br>
     * network.bluetooth network.wlan (WLAN wireless family includes 802.11, 802.11a, 802.11b, 802.11g)</br>
     * network.3gpp (3GPP wireless family includes GPRS, EDGE, UMTS, GERAN, UTRAN, and GAN)</br>
     * network.cdma (CDMA wireless family includes CDMA1x and EVDO)</br>
     * network.iden</br></code>
     * @return if has the capability, return true; or return false.
     */
    public boolean hasCapability( String capability ) {
        return isCapable( capability );
    }

    /**
     * Check if device has network coverage
     * @return Returns true if the device is in coverage, otherwise return false
     */
    public boolean hasDataCoverage() {
        return CoverageInfo.getCoverageStatus() != CoverageInfo.COVERAGE_NONE;
    }

    /**
     * Determines the level of access to the requested module. 
     * @param module
     * @return 0 - ALLOW, 1 - DENY, 2 - PROMPT, 3 - NOTSET
     */
    public int hasPermission( String module ) {
        switch( checkPermission( module ) ) {
            case ApplicationPermissions.VALUE_ALLOW:
                return ALLOW_VALUE;
            case ApplicationPermissions.VALUE_DENY:
                return DENY_VALUE;
            case ApplicationPermissions.VALUE_PROMPT:
                return PROMPT_VALUE;
            default:
                return NOTSET_VALUE;
        }
    }

    private String[] checkAllPermissions() {
        Vector permissions = new Vector();
        String[] result = null;

        for( int i = 0; i < ALL_PERMISSIONS.length; i++ ) {
            if( hasPermission( ALL_PERMISSIONS[ i ] ) == ALLOW_VALUE ) {
                permissions.addElement( ALL_PERMISSIONS[ i ] );
            }
        }

        result = new String[ permissions.size() ];

        if( !permissions.isEmpty() ) {
            permissions.copyInto( result );
        }

        return result;
    }

    private String getSoftwareVersion() {
        return DeviceInfo.getSoftwareVersion();
    }

    private String getModel() {
        return DeviceInfo.getDeviceName();
    }

    /**
     * Implements invoke() of interface IJSExtension. Methods of extension will be called here.
     * @throws WidgetException if specified method cannot be recognized
     */
    public void invoke( JSExtensionRequest request, JSExtensionResponse response ) throws WidgetException {
        String method = request.getMethodName();
        Object[] args = request.getArgs();
        String msg = "";
        int code = JSExtensionReturnValue.SUCCESS;
        JSONObject data = new JSONObject();
        JSONObject returnValue = null;

        if( !SUPPORTED_METHODS.contains( method ) ) {
            throw new WidgetException("Undefined method: " + method);
        }

        try {
            if( method.equals( FUNCTION_HAS_PERMISSION ) ) {
                String module = (String) args[ 0 ];
                data.put( ARG_MODULE, module );
                data.put( FUNCTION_HAS_PERMISSION, hasPermission( module ) );
            } else if( method.equals( FUNCTION_HAS_CAPABILITY ) ) {
                String capability = (String) args[ 0 ];
                data.put( ARG_CAPABILITY, capability );
                data.put( FUNCTION_HAS_CAPABILITY, hasCapability( capability ) );
            } else if( method.equals( FUNCTION_HAS_DATA_COVERAGE ) ) {
                data.put( FUNCTION_HAS_DATA_COVERAGE, hasDataCoverage() );
            } else if( method.equals( FUNCTION_IS_MASS_STORAGE_ACTIVE ) ) {
                data.put( FUNCTION_IS_MASS_STORAGE_ACTIVE, isMassStorageActive() );
            } else if( method.equals( FUNCTION_SET_HOME_SCREEN ) ) {
                String picture = (String) args[ 0 ];
                data.put( ARG_PICTURE, picture );
                setHomeScreenBackground( picture );
            } else if( method.equals( SOFTWARE_VERSION ) ) {
                data.put( SOFTWARE_VERSION, getSoftwareVersion() );
            } else if( method.equals( SCRIPT_API_VERSION ) ) {
                data.put( SCRIPT_API_VERSION, getSoftwareVersion() );
            } else if( method.equals( MODEL ) ) {
                data.put( MODEL, getModel() );
            } else if ( method.equals( FUNCTION_GET ) ) {
                data.put( FUNCTION_HAS_CAPABILITY, checkAllCapabilties() );
                data.put( SOFTWARE_VERSION, getSoftwareVersion() );
                data.put( FUNCTION_HAS_PERMISSION, checkAllPermissions() );
                data.put( MODEL, getModel() );
                data.put( FUNCTION_HAS_DATA_COVERAGE, hasDataCoverage() );
                data.put( SCRIPT_API_VERSION, getSoftwareVersion() );
                data.put( FUNCTION_IS_MASS_STORAGE_ACTIVE, isMassStorageActive() );
            }
        } catch( Exception e ) {
            msg = e.getMessage();
            code = JSExtensionReturnValue.FAIL;
        }

        returnValue = new JSExtensionReturnValue( msg, code, data ).getReturnValue();

        response.setPostData( returnValue.toString().getBytes() );
    }

    /**
     * Returns whether USB MassStorage mode is active.
     * This method is not implemented yet
     * @return false
     */
    private boolean isMassStorageActive() {
        return false;
    }

    // Not sure if it's a published API, there is no this API in WebWorks
    // API reference. It returns UNDEFINED in old style java code.
    /**
     * Set background of home screen
     * @param filepath - path of image file
     * @throws Exception
     */
    public void setHomeScreenBackground( String filePath ) throws Exception {
        FileConnection fileConnection = null;
        try {
            Connection con = Connector.open( filePath );
            if( con != null && con instanceof FileConnection ) {
                fileConnection = (FileConnection) con;
                if( !fileConnection.exists() || fileConnection.isDirectory() ) {
                    throw new Exception( "Invalid file URI" );
                }
                // set home screen background
                HomeScreen.setBackgroundImage( filePath );
            }
        } finally {
            if( fileConnection != null ) {
                fileConnection.close();
            }
        }
    }

    private int checkPermission( String module ) {
        ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
        if( module.equals( BLACKBERRY_APP ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT );
        } else if( module.equals( BLACKBERRY_INVOKE ) || module.equals( BLACKBERRY_INVOKE_ADDRESSBOOKARGUMENTS )
                || module.equals( BLACKBERRY_INVOKE_BROWSERARGUMENTS ) || module.equals( BLACKBERRY_INVOKE_CALENDARARGUMENTS )
                || module.equals( BLACKBERRY_INVOKE_CAMERAARGUMENTS ) || module.equals( BLACKBERRY_INVOKE_JAVAARGUMENTS )
                || module.equals( BLACKBERRY_INVOKE_MAPSARGUMENTS ) || module.equals( BLACKBERRY_INVOKE_MESSAGEARGUMENTS )
                || module.equals( BLACKBERRY_INVOKE_PHONEARGUMENTS ) || module.equals( BLACKBERRY_INVOKE_SEARCHARGUMENTS )
                || module.equals( BLACKBERRY_INVOKE_TASKARGUMENTS ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION );
        } else if( module.equals( BLACKBERRY_IO_DIR ) || module.equals( BLACKBERRY_IO_FILE ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_FILE_API );
        } else if( module.equals( BLACKBERRY_MESSAGE ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_EMAIL );
        } else if( module.equals( BLACKBERRY_PIM_ADDRESS ) || module.equals( BLACKBERRY_PIM_APPOINTMENT )
                || module.equals( BLACKBERRY_PIM_ATTENDEE ) || module.equals( BLACKBERRY_PIM_CATEGORY )
                || module.equals( BLACKBERRY_PIM_CONTACT ) || module.equals( BLACKBERRY_PIM_MEMO )
                || module.equals( BLACKBERRY_PIM_RECURRENCE ) || module.equals( BLACKBERRY_PIM_REMINDER )
                || module.equals( BLACKBERRY_PIM_TASK ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_ORGANIZER_DATA );
        } else if( module.equals( BLACKBERRY_PUSH ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_INTERNET );
        }
        return ApplicationPermissions.VALUE_ALLOW;
    }

    private boolean isCapable( String capability ) {
        // Is the current keyboard a sure type?
        if( capability.equals( C_INPUT_KEYBOARD_ISSURETYPE ) ) {
            switch( Keypad.getHardwareLayout() ) {
                case Keypad.HW_LAYOUT_REDUCED:
                case Keypad.HW_LAYOUT_REDUCED_24:
                case Keypad.HW_LAYOUT_TOUCHSCREEN_24:
                    return true;
                default:
                    return false;
            }
        }
        // Are we using a touch device?
        else if( capability.equals( C_INPUT_TOUCH ) ) {
            return Touchscreen.isSupported();
        }
        // Use System.getProperty() to determine
        else if( capability.equals( C_MEDIA_AUDIO_CAPTURE ) ) {
            return System.getProperty( "supports.audio.capture" ).equals( "true" );
        } else if( capability.equals( C_MEDIA_VIDEO_CAPTURE ) ) {
            return System.getProperty( "supports.video.capture" ).equals( "true" );
        } else if( capability.equals( C_MEDIA_RECORDING ) ) {
            return System.getProperty( "supports.recording" ).equals( "true" );
        } else if( capability.equals( C_LOCATION_MAPS ) ) {
            return ( CodeModuleManager.getModuleHandle( "net_rim_bb_maps" ) != 0 );
        } else if( capability.equals( C_LOCATION_GPS ) ) {
            return ( GPSInfo.getDefaultGPSMode() != GPSInfo.GPS_MODE_NONE );
        }
        // Is there an SD card inserted into the device?
        else if( capability.equals( C_STORAGE_MEMORYCARD ) ) {
            return sdCardInserted();
        }
        // Is the device bluetooth capable
        else if( capability.equals( C_NETWORK_BLUETOOTH ) ) {
            // Bluetooth has been on every device since 4.0
            return true;
        }
        // Check if the wireless family is supported by this device
        else if( capability.equals( C_NETWORK_WLAN ) ) {
            return RadioInfo.areWAFsSupported( RadioInfo.WAF_WLAN );
        } else if( capability.equals( C_NETWORK_CDMA ) ) {
            return RadioInfo.areWAFsSupported( RadioInfo.WAF_CDMA );
        } else if( capability.equals( C_NETWORK_3GPP ) ) {
            return RadioInfo.areWAFsSupported( RadioInfo.WAF_3GPP );
        } else if( capability.equals( C_NETWORK_IDEN ) ) {
            return RadioInfo.areWAFsSupported( RadioInfo.WAF_IDEN );
        }
        return false;
    }

    private String[] checkAllCapabilties() {
        Vector capabilities = new Vector();
        String[] result = null;

        for( int i = 0; i < ALL_CAPABILITIES.length; i++ ) {
            if( hasCapability( ALL_CAPABILITIES[ i ] ) ) {
                capabilities.addElement( ALL_CAPABILITIES[ i ] );
            }
        }

        result = new String[capabilities.size()];

        if (!capabilities.isEmpty()) {
            capabilities.copyInto( result );
        }

        return result;
    }

    private boolean sdCardInserted() {
        String root = null;
        Enumeration e = FileSystemRegistry.listRoots();
        // loop through the file system roots and look for the SD card prefix
        while( e.hasMoreElements() ) {
            root = (String) e.nextElement();
            if( root.equalsIgnoreCase( "sdcard/" ) ) {
                // device has a microSD inserted
                return true;
            }
        }
        return false;
    }

    public void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine,
            SimpleSortingVector jsInjectionPaths ) {
        JSUtilities.loadJS( scriptEngine, JS_FILES, jsInjectionPaths );
    }

    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {

    }

    public void unloadFeatures() {

    }
}

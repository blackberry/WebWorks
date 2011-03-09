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

import javax.microedition.io.file.FileSystemRegistry;
import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Touchscreen;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of blackberry.system.hasCapability function.
 */
public final class HasCapabilityFunction extends ScriptableFunctionBase {

    private static final String C_INPUT_KEYBOARD_ISSURETYPE = "input.keyboard.issuretype";
    private static final String C_INPUT_TOUCH = "input.touch";
    private static final String C_MEDIA_AUDIO_CAPTURE = "media.audio.capture";
    private static final String C_MEDIA_VIDEO_CAPTURE = "media.video.capture";
    private static final String C_MEDIA_RECORDING = "media.recording";
    private static final String C_LOCATION_MAPS = "location.maps";
    private static final String C_LOCATION_GPS = "location.gps";
    private static final String C_STORAGE_MEMORYCARD = "storage.memorycard";
    private static final String C_NETWORK_BLUETOOTH = "network.bluetooth";
    private static final String C_NETWORK_WLAN = "network.wlan";
    private static final String C_NETWORK_3GPP = "network.3gpp";
    private static final String C_NETWORK_CDMA = "network.cdma";
    private static final String C_NETWORK_IDEN = "network.iden";

    /**
     * @see ScriptableFunctionBase#execute(Object, Object[] )
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        String capability = args[ 0 ].toString();
        return new Boolean( isCapable( capability ) );
    }

    private boolean isCapable( String capability ) {
        // Is the current keyboard a suretype?
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

    /**
     * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
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
}

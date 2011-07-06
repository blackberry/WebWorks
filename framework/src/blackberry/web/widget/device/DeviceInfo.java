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
package blackberry.web.widget.device;

/**
 * 
 */
public class DeviceInfo {

    private static DeviceInfo _instance;

    private String _phoneOS;
    private boolean _isBB6;

    public static final int OS_VERSION_6 = 6;
    public static final int OS_VERSION_5 = 5;
    public static final int OS_VERSION_OTHER = 0;

    static {
        _instance = new DeviceInfo();
    }

    /**
     * Convenience method to determine if the current OS is 6.0 OR BETTER
     * 
     * @return true if the OS is 6 or higher; false otherwise
     */
    public static boolean isBlackBerry6() {
        return _instance._isBB6;
    }

    private DeviceInfo() {
        init();
    }

    private void init() {
        // retrieve OS version
        _isBB6 = false;
        try {
            _phoneOS = net.rim.device.api.system.DeviceInfo.getSoftwareVersion();
            if( !_phoneOS.equals( "" ) && _phoneOS.length() >= 1 ) {
                // Check first value for major revision
                int majorOSRev = Integer.parseInt( _phoneOS.substring( 0, _phoneOS.indexOf( '.' ) ) );
                _isBB6 = !( majorOSRev < 6 );
            }
        } catch( Exception e ) {
            // Set the value to 0 if there is some sort of error.
            // 0 will be the value for simulators
            _isBB6 = false;
        }
    }
}

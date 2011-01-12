/*
 * DeviceInfo.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2010-2010
 */

package blackberry.web.widget.device;

/**
 * 
 */
public class DeviceInfo {
    
    private static DeviceInfo           _instance; 
    
    private String                      _phoneOS;
    private boolean                     _isBB6;
    
    public static final int             OS_VERSION_6 = 6;
    public static final int             OS_VERSION_5 = 5;
    public static final int             OS_VERSION_OTHER = 0;
    
    static {
        _instance = new DeviceInfo();
    }
    
    /**
     * Convenience method to determine if the current OS is 6.0 OR BETTER
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
        try{
            _phoneOS = net.rim.device.api.system.DeviceInfo.getSoftwareVersion();            
            if(!_phoneOS.equals("") && _phoneOS.length() >= 1){
                // Check first value for major revision
                int majorOSRev = Integer.parseInt(_phoneOS.substring(0, _phoneOS.indexOf('.')));
                _isBB6 = !(majorOSRev < 6);
            }
        } catch(Exception e){
            // Set the value to 0 if there is some sort of error.
            // 0 will be the value for simulators
            _isBB6 = false;
        }
    }
}

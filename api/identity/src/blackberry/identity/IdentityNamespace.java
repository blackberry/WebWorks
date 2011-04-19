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
package blackberry.identity;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.SIMCardInfo;
import blackberry.identity.phone.PhoneNamespace;

/**
 * This class defines public properties for blackberry.identity namespace.
 * 
 * @author sgolod
 * 
 */
public class IdentityNamespace extends Scriptable {

    public static final String NAME = "blackberry.identity";

    public static final String FIELD_PIN = "PIN";
    public static final String FIELD_IMSI = "IMSI";
    public static final String FIELD_IMEI = "IMEI";

    private String pin;
    private String imsi;
    private String imei;

    /**
     * Default constructor, creates a new instance of {@link IdentityNamespace}
     * and populates its properties.
     */
    public IdentityNamespace() {
        populateProperties();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( IdentityFunctions.FUNCTION_GETSERVICELIST ) ) {
            return IdentityFunctions.createGetServiceListFunction();
        } else if( name.equals( GetTransportListFunction.NAME ) ) {
            return new GetTransportListFunction();
        } else if( name.equals( PhoneNamespace.NAME ) ) {
            return PhoneNamespace.getInstance();
        }

        else if( name.equals( FIELD_PIN ) ) {
            return pin;
        } else if( name.equals( FIELD_IMSI ) ) {
            return imsi;
        } else if( name.equals( FIELD_IMEI ) ) {
            return imei;
        }

        return super.getField( name );
    }

    /**
     * Helper method that populates value for the "PIN", "IMSI" and "IMEI" properties.
     */
    private void populateProperties() {

        try {
            pin = Integer.toHexString( DeviceInfo.getDeviceId() );
        } catch( final Exception ex ) {
            pin = "";
        }

        try {
            imsi = GPRSInfo.imeiToString( SIMCardInfo.getIMSI() );
        } catch( final Exception ex ) {
            imsi = "";
        }

        try {
            imei = GPRSInfo.imeiToString( GPRSInfo.getIMEI() );
        } catch( final Exception ex ) {
            imei = "";
        }
    }
}

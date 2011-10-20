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
package blackberry.bbm.platform.self.location;

import net.rim.blackberry.api.bbm.platform.profile.ProfileLocation;
import net.rim.device.api.script.Scriptable;
import blackberry.bbm.platform.util.Util;

public class LocationNamespace extends Scriptable {
    public static final String NAME = "location";
    
    public static final String FIELD_TIMEZONE_OFFSET =  "timezoneOffset";
    public static final String FIELD_FLAG =             "flag";
    public static final String FIELD_COUNTRY_CODE =     "countryCode";
    
    private ProfileLocation _location;
    
    public LocationNamespace(ProfileLocation location) {
        _location = location;
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(LocationNamespace.FIELD_COUNTRY_CODE)) {
            return _location.getCountryCode();
        } else if(name.equals(LocationNamespace.FIELD_FLAG)) {
            return Util.bitmapToBase64Str(_location.getCountryFlagImage());
        } else if(name.equals(LocationNamespace.FIELD_TIMEZONE_OFFSET)) {
            return new Integer(_location.getTimeZone().getRawOffset() / 60 / 1000);
        } else {
            return super.getField(name);
        }
    }
}

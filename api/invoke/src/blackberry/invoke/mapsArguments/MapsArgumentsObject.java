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
package blackberry.invoke.mapsArguments;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.pim.address.AddressObject;

/**
 * This class represents the MapsArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class MapsArgumentsObject extends ScriptableObjectBase {

    private final double _latitude;
    private final double _longitude;
    private final AddressObject _addessObject;
    private final String _xml;
    private final boolean _default;

    /**
     * Default constructor, constructs a new MapsArgumentsObject object.
     */
    public MapsArgumentsObject() {
        _default = true;
        _latitude = 0;
        _longitude = 0;
        _addessObject = null;
        _xml = null;
    }

    /**
     * Constructs a new MapsArgumentsObject object.
     * 
     * @param latitude
     *            Specifies the latitude for display.
     * @param longitude
     *            Specifies the longitude for display.
     */
    public MapsArgumentsObject( final double latitude, final double longitude ) {
        _default = false;
        _latitude = latitude;
        _longitude = longitude;
        _addessObject = null;
        _xml = null;
    }

    /**
     * Constructs a new MapsArgumentsObject object.
     * 
     * @param a
     *            Address for display in Map application.
     */
    public MapsArgumentsObject( final AddressObject a ) {
        _default = false;
        _latitude = 0;
        _longitude = 0;
        _addessObject = a;
        _xml = null;
    }

    /**
     * Constructs a new MapsArgumentsObject object.
     * 
     * @param x
     *            XML document contains map information.
     */
    public MapsArgumentsObject( final String x ) {
        _default = false;
        _latitude = 0;
        _longitude = 0;
        _addessObject = null;
        _xml = x;
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return the contained Document Object.
     */
    public String getXML() {
        return _xml;
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return latitude value.
     */
    public double getLatitude() {
        return _latitude;
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return longitude value.
     */
    public double getLongitude() {
        return _longitude;
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return the address of the location.
     */
    public String getAddress() {
        if( _addessObject == null ) {
            return null;
        }

        return _addessObject.toString();
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return the address object of the location.
     */
    public AddressObject getAddressObject() {
        return _addessObject;
    }

    /**
     * Internal helper method to get direct access to the MapsArgumentsObject's underlying content.
     * 
     * @return whether there is no location specified
     */
    public boolean isDefault() {
        return _default;
    }
}

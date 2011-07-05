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
package blackberry.pim.address;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents a PIM Address
 * 
 * @author dmateescu
 */
public class AddressObject extends ScriptableObjectBase {

    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_ADDRESS1 = "address1";
    public static final String FIELD_ADDRESS2 = "address2";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_ZIP = "zipPostal";
    public static final String FIELD_STATE = "stateProvince";

    static final String SEPARATOR = ", ";
    private final static String BLANK = "";

    /**
     * Constructs an AddressObject
     */
    public AddressObject() {
        initial();
    }

    private void initial() {
        addItem( new ScriptField( FIELD_COUNTRY, BLANK, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_ADDRESS1, BLANK, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_ADDRESS2, BLANK, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_CITY, BLANK, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_ZIP, BLANK, ScriptField.TYPE_STRING, false, false ) );
        addItem( new ScriptField( FIELD_STATE, BLANK, ScriptField.TYPE_STRING, false, false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String address1 = getItem( FIELD_ADDRESS1 ).getStringValue();
        String address2 = getItem( FIELD_ADDRESS2 ).getStringValue();
        String city = getItem( FIELD_CITY ).getStringValue();
        String state = getItem( FIELD_STATE ).getStringValue();
        String zip = getItem( FIELD_ZIP ).getStringValue();
        String country = getItem( FIELD_COUNTRY ).getStringValue();

        StringBuffer s = new StringBuffer();
        if( address1.length() != 0 ) {
            s.append( address1 );
        }

        if( address2.length() != 0 ) {
            if( s.length() != 0 ) {
                s.append( SEPARATOR );
            }

            s.append( address2 );
        }

        if( city.length() != 0 ) {
            if( s.length() != 0 ) {
                s.append( SEPARATOR );
            }

            s.append( city );
        }

        if( state.length() != 0 ) {
            if( s.length() != 0 ) {
                s.append( SEPARATOR );
            }

            s.append( state );
        }

        if( country.length() != 0 ) {
            if( s.length() != 0 ) {
                s.append( SEPARATOR );
            }

            s.append( country );
        }

        if( zip.length() != 0 ) {
            if( s.length() != 0 ) {
                s.append( SEPARATOR );
            }

            s.append( zip );
        }

        return s.toString();
    }
}

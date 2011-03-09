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
package blackberry.identity.service;

import net.rim.device.api.script.Scriptable;

/**
 * This class represent the Service object.
 * 
 * @author sgolod
 *
 */
public class ServiceObject extends Scriptable {

    private final String _emailAddress;
    private final String _name;
    private final Integer _type;
    private final String _uid;
    private final String _cid;

    public static final String FIELD_EMAILADDRESS = "emailAddress";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";

    public static final short TYPE_EMAIL = 0;
    public static final short TYPE_CALENDAR = 1;
    public static final short TYPE_CONTACT = 2;

    /**
     * Constructor of ServiceObject.
     *
     * @param emailAddress is a email address of ServiceObject when its type is ServiceObject.TYPE_EMAIL.
     * @param name is a name of a particular ServiceRecord.
     * @param type is a type of a particular ServiceRecord.
     * @param uid  is a uid of a particular ServiceRecord.
     * @param cid  is a cid of a particular ServiceRecord.
     */
    public ServiceObject( final String emailAddress, final String name, final Integer type,
            final String uid, final String cid ) {
        _emailAddress = emailAddress;
        _name = name;
        _type = type;
        _uid = uid;
        _cid = cid;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( FIELD_EMAILADDRESS ) ) {
            return _emailAddress;
        } else if( name.equals( FIELD_NAME ) ) {
            return _name;
        } else if( name.equals( FIELD_TYPE ) ) {
            return _type;
        } else if( name.equals( "TYPE_EMAIL" ) ) {
            return new Integer( TYPE_EMAIL );
        } else if( name.equals( "TYPE_CALENDAR" ) ) {
            return new Integer( TYPE_CALENDAR );
        } else if( name.equals( "TYPE_CONTACT" ) ) {
            return new Integer( TYPE_CONTACT );
        }

        return super.getField( name );
    }

    /**
     * Getter for the name field.
     *
     * @return the name of this service instance.
     */
    public String getName() {
        try {
            return (String) getField( FIELD_NAME );
        } catch( final Exception e ) {
            return "";
        }
    }

    /**
     * Getter for the type field.
     *
     * @return the type of this service instance.
     */
    public int getType() {
        try {
            return ( (Integer) getField( FIELD_TYPE ) ).intValue();
        } catch( final Exception e ) {
            return 0;
        }
    }

    /**
     * Getter for the uid field.
     *
     * @return the uid of this service instance.
     */
    public String getUid() {
        return _uid;
    }

    /**
     * Getter for the cid field.
     *
     * @return the cid of this service instance.
     */
    public String getCid() {
        return _cid;
    }
}

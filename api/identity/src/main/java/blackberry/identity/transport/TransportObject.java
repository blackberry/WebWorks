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
package blackberry.identity.transport;

import net.rim.device.api.script.Scriptable;

/**
 * This class represent the Transport object.
 * 
 * @author sgolod
 *
 */
public class TransportObject extends Scriptable {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";

    private final String _name;
    private final String _type;
    private final String _uid; // uid of the ServiceRecord that corresponds to this transport service

    /**
     * Constructor of TransportObject.
     *
     * @param name is a name of a particular ServiceRecord.
     * @param type is a type of a particular ServiceRecord.
     * @param uid is a uid of a particular ServiceRecord.
     */
    public TransportObject( final String name, final String type, final String uid ) {
        _name = name;
        _type = type;
        _uid = uid;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( FIELD_NAME ) ) {
            return _name;
        } else if( name.equals( FIELD_TYPE ) ) {
            return _type;
        }

        return super.getField( name );
    }

    /**
     * Getter for the uid field. 
     *
     * @return the uid of this transport instance.
     */
    public String getUID() {
        return _uid;
    }
}

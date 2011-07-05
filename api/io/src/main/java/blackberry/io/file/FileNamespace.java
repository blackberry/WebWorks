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
package blackberry.io.file;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

/**
 * Scriptable object that represents the blackberry.io.file JavaScript namespace
 */
public final class FileNamespace extends Scriptable {

    private Hashtable _fields;

    /**
     * Constructor
     */
    public FileNamespace() {
        _fields = new Hashtable();

        _fields.put( ExistsFunction.NAME, new ExistsFunction() );
        _fields.put( RenameFunction.NAME, new RenameFunction() );
        _fields.put( CopyFunction.NAME, new CopyFunction() );
        _fields.put( DeleteFileFunction.NAME, new DeleteFileFunction() );
        _fields.put( GetFilePropertiesFunction.NAME, new GetFilePropertiesFunction() );
        _fields.put( SaveFileFunction.NAME, new SaveFileFunction() );
        _fields.put( ReadFileFunction.NAME, new ReadFileFunction() );
        _fields.put( OpenFunction.NAME, new OpenFunction() );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#enumerateFields
     */
    public void enumerateFields( Vector v ) {
        if( !_fields.isEmpty() ) {
            for( Enumeration e = _fields.keys(); e.hasMoreElements(); ) {
                v.addElement( e.nextElement() );
            }
        }
    }

    /**
     * see net.rim.device.api.script.Scriptable#getElementCount
     */
    public int getElementCount() {
        return _fields.size();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField
     */
    public Object getField( String name ) throws Exception {
        Object field = _fields.get( name );
        if( field == null ) {
            return UNDEFINED;
        }
        return field;
    }
}

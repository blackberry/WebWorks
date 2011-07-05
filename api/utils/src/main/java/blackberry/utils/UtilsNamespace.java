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
package blackberry.utils;

import java.util.Hashtable;

import net.rim.device.api.script.Scriptable;

/**
 * A class registering utils extension functions in the namespace
 */
public class UtilsNamespace extends Scriptable {

    private Hashtable _fields;

    /**
     * @see net.rim.device.api.script.Scriptable#Scriptable()
     */
    public UtilsNamespace() {
        super();
        _fields = new Hashtable();
        _fields.put( GenerateUniqueIdFunction.NAME, new GenerateUniqueIdFunction() );
        _fields.put( DocumentToBlobFunction.NAME, new DocumentToBlobFunction() );
        _fields.put( ParseURLFunction.NAME, new ParseURLFunction() );
        _fields.put( StringToBlobFunction.NAME, new StringToBlobFunction() );
        _fields.put( BlobToStringFunction.NAME, new BlobToStringFunction() );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        Object field = _fields.get( name );
        if( field == null ) {
            return super.getField( name );
        }
        return field;
    }
}

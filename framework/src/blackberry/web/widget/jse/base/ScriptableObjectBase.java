/*
* Copyright 2010 Research In Motion Limited.
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
package blackberry.web.widget.jse.base;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

/**
 * Our base class of Scriptable object
 */
public abstract class ScriptableObjectBase extends Scriptable {

    protected Hashtable _lookup; // Script items that are actually available
    
    public ScriptableObjectBase() {
        _lookup = new Hashtable();
    }
    
    /* @Override */ public Scriptable getParent() {
        return null;
    }

    /* @Override */ public void enumerateFields( Vector v ) {
        if (!_lookup.isEmpty()) {
            for (Enumeration e = _lookup.keys(); e.hasMoreElements(); ) {
                v.addElement( e.nextElement() );
            }
        }        
    }

    /* @Override */ public Object getField( String name ) throws Exception {
        Object field = _lookup.get( name );
        if (field == null) {
            return UNDEFINED;
        }
        return ((ScriptField) field).getValue();
    }

    /* @Override */ public boolean putField( String name, Object value ) throws Exception {
        if (_lookup.containsKey(name)) {
            // Retrieve the field from the lookup
            ScriptField field = (ScriptField) _lookup.get(name);
            
            if (field.isReadOnly()) {
                return false;
            }
            
            // Type check/verify by implementing object
            if (verify(field, value)) {            
                // Set the new field
                field.setValue(value);
                return true;
            }           
        }
        
        return false;
    }
    
    protected void addItem( ScriptField field ) {
        _lookup.put(field.getName(), field);
    }

    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        // default assume class contains read-only fields
        return false;
    }
}

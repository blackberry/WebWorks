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
package blackberry.bbm.platform.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

/**
 * Helper class for managing writable fields in a Scriptable. Like ScriptableObjectBase, but without
 * type checking and the heavy implementation.
 */
public class ScriptableFieldManager {
    
    protected final Hashtable _fields;
    
    public ScriptableFieldManager() {
        _fields = new Hashtable();
    }
    
    public void addField(String name) {
        _fields.put(name, Scriptable.UNDEFINED);
    }
    
    public boolean hasField(String name) {
        return _fields.containsKey(name);
    }
    
    //////////////////////////////
    // Scriptable field methods //
    //////////////////////////////
    
    public void enumerateFields(Vector v) {
        final Enumeration eFields = _fields.elements();
        while(eFields.hasMoreElements()) {
            v.addElement(eFields.nextElement());
        }
    }
    
    public Object getField(String name) {
        if(_fields.containsKey(name)) {
            return _fields.get(name);
        } else {
            return Scriptable.UNDEFINED;
        }
    }
    
    public boolean putField(String name, Object value) {
        if(_fields.containsKey(name)) {
            _fields.put(name, value);
            return true;
        } else {
            return false;
        }
    }
}

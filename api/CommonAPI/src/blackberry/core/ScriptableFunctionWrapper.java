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

package blackberry.core;

import java.util.Vector;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

/**
 * Wrap a ScriptableFunction. Used to help wrap a singleton in a unique object reference
 */
public class ScriptableFunctionWrapper extends ScriptableFunction {
    private ScriptableFunction _baseObject;

    /**
     * default constructor
     * @param baseObject the object to wrap
     */
    public ScriptableFunctionWrapper( ScriptableFunction baseObject ) {
        _baseObject = baseObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#getParent()
     */
    public Scriptable getParent() {
        return _baseObject.getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#enumerateFields(java.util.Vector)
     */
    public void enumerateFields( Vector v ) {
        _baseObject.enumerateFields( v );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( String name ) throws Exception {
        return _baseObject.getField( name );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#putField(java.lang.String, java.lang.Object)
     */
    public boolean putField( String name, Object value ) throws Exception {
        return _baseObject.putField( name, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#getElementCount()
     */
    public int getElementCount() {
        return _baseObject.getElementCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#getElement(int)
     */
    public Object getElement( int index ) throws Exception {
        return _baseObject.getElement( index );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.Scriptable#putElement(int, java.lang.Object)
     */
    public boolean putElement( int index, Object value ) throws Exception {
        return _baseObject.putElement( index, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.ScriptableFunction#invoke(java.lang.Object, java.lang.Object[])
     */
    public Object invoke( Object thiz, Object[] args ) throws Exception {
        return _baseObject.invoke( thiz, args );
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( Object thiz, Object[] args ) throws Exception {
        return _baseObject.construct( thiz, args );
    }
}
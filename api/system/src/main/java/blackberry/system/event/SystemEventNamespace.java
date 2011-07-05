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
package blackberry.system.event;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.CoverageStatusListener;
import net.rim.device.api.util.IntHashtable;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Class representing the functions and properties of blackberry.system.event
 */
public class SystemEventNamespace extends Scriptable {

    public static final String NAME = "event";

    private static final String FUNCTION_ON_HARDWARE_KEY = "onHardwareKey";
    private static final String FUNCTION_ON_COVERAGE_CHANGE = "onCoverageChange";

    public static final String KEY_BACK = "KEY_BACK";
    public static final String KEY_MENU = "KEY_MENU";
    public static final String KEY_CONVENIENCE_1 = "KEY_CONVENIENCE_1";
    public static final String KEY_CONVENIENCE_2 = "KEY_CONVENIENCE_2";
    public static final String KEY_STARTCALL = "KEY_STARTCALL";
    public static final String KEY_ENDCALL = "KEY_ENDCALL";
    public static final String KEY_VOLUME_UP = "KEY_VOLUMEUP";
    public static final String KEY_VOLUME_DOWN = "KEY_VOLUMEDOWN";

    public static final int IKEY_BACK = 0;
    public static final int IKEY_MENU = 1;
    public static final int IKEY_CONVENIENCE_1 = 2;
    public static final int IKEY_CONVENIENCE_2 = 3;
    public static final int IKEY_STARTCALL = 4;
    public static final int IKEY_ENDCALL = 5;
    public static final int IKEY_VOLUME_DOWN = 6;
    public static final int IKEY_VOLUME_UP = 7;

    private static SystemEventNamespace _instance;

    private CoverageMonitor _currentCoverageMonitor;
    private IntHashtable _callbackLookup;
    private Hashtable _fields;

    /**
     * Static method to return an instance of the EventNamespace object. Reset parameter provided to allow the namespace's
     * elements to be 'reset'
     * 
     * @param reset
     *            Boolean flag indicating whether the instance should be reset or not before being retrieved.
     * 
     * @return An instance of the EventNamespace object
     */
    public static SystemEventNamespace getInstance( boolean reset ) {
        if( _instance == null ) {
            _instance = new SystemEventNamespace();
        }
        if( reset ) {
            _instance.reset();
        }
        return _instance;
    }

    /**
     * Private constructor
     */
    private SystemEventNamespace() {
        _callbackLookup = new IntHashtable();
        _fields = new Hashtable();
        _fields.put( KEY_BACK, new Integer( IKEY_BACK ) );
        _fields.put( KEY_MENU, new Integer( IKEY_MENU ) );
        _fields.put( KEY_CONVENIENCE_1, new Integer( IKEY_CONVENIENCE_1 ) );
        _fields.put( KEY_CONVENIENCE_2, new Integer( IKEY_CONVENIENCE_2 ) );
        _fields.put( KEY_STARTCALL, new Integer( IKEY_STARTCALL ) );
        _fields.put( KEY_ENDCALL, new Integer( IKEY_ENDCALL ) );
        _fields.put( KEY_VOLUME_DOWN, new Integer( IKEY_VOLUME_DOWN ) );
        _fields.put( KEY_VOLUME_UP, new Integer( IKEY_VOLUME_UP ) );
        _fields.put( FUNCTION_ON_HARDWARE_KEY, new OnHardwareKeyFunction() );
        _fields.put( FUNCTION_ON_COVERAGE_CHANGE, new OnCoverageChangeFunction() );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#enumerateFields(Vector)
     */
    public void enumerateFields( Vector v ) {
        if( !_fields.isEmpty() ) {
            for( Enumeration e = _fields.keys(); e.hasMoreElements(); ) {
                v.addElement( e.nextElement() );
            }
        }
    }

    /**
     * see net.rim.device.api.script.Scriptable#getElementCount()
     */
    public int getElementCount() {
        return _fields.size();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String fieldName ) throws Exception {
        Object field = _fields.get( fieldName );
        if( field == null ) {
            return super.getField( fieldName );
        }
        return field;
    }

    /**
     * Unload all event listeners
     * 
     * @throws Exception
     */
    public void unload() throws Exception {
        _callbackLookup.clear();
        reset();
    }

    /**
     * Retrieves the scriptable callback assigned to this event;
     * 
     * @param eventID
     *            the event ID to lookup
     * @return callback associated with event ID or null if no callback found
     * 
     */
    public ScriptableFunction getCallback( int eventID ) {
        return (ScriptableFunction) _callbackLookup.get( eventID );
    }

    /**
     * Resets/Stops any listeners
     */
    private void reset() {
        // remove coverage listener
        if( _currentCoverageMonitor != null ) {
            _currentCoverageMonitor.stop();
            _currentCoverageMonitor = null;
        }
    }

    /**
     * Class to implement the script function OnDataCoverageChange()
     */
    private class OnCoverageChangeFunction extends ScriptableFunctionBase {

        protected Object execute( Object thiz, Object[] args ) throws Exception {
            if( _currentCoverageMonitor != null ) {
                _currentCoverageMonitor.stop();
                _currentCoverageMonitor = null;
            }
            if( args[ 0 ] != null ) {
                _currentCoverageMonitor = new CoverageMonitor( (ScriptableFunction) args[ 0 ] );
            }
            return UNDEFINED;
        }

        /**
         * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature( 1 );
            fs.addNullableParam( ScriptableFunction.class, true );
            return new FunctionSignature[] { fs };
        }
    }

    /**
     * Class to implement the script function OnHardwareKey()
     */
    private class OnHardwareKeyFunction extends ScriptableFunctionBase {

        protected Object execute( Object thiz, Object[] args ) throws Exception {
            int eventID = ( (Integer) args[ 0 ] ).intValue();
            ScriptableFunction storedCallback = (ScriptableFunction) _callbackLookup.get( eventID );

            if( args[ 1 ] == null && storedCallback != null ) {
                _callbackLookup.remove( eventID );
            } else if( args[ 1 ] != null ) {
                _callbackLookup.put( eventID, args[ 1 ] );
            }
            return UNDEFINED;
        }

        /**
         * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase#getFunctionSignatures
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature( 2 );
            fs.addParam( Integer.class, true );
            fs.addNullableParam( ScriptableFunction.class, true );
            return new FunctionSignature[] { fs };
        }
    }

    /**
     * Helper class to implement CoverageStatusListener
     */
    private static class CoverageMonitor implements CoverageStatusListener {

        ScriptableFunction _myCallback;

        CoverageMonitor( ScriptableFunction callback ) {
            CoverageInfo.addListener( this );
            _myCallback = callback;
        }

        public void coverageStatusChanged( int newCoverage ) {
            try {
                _myCallback.invoke( null, null );
            } catch( Exception e ) {
            }
        }

        void stop() {
            CoverageInfo.removeListener( this );
        }
    }
}

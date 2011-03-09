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
package blackberry.system;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import blackberry.core.ScriptableFunctionBase;

/**
 * Class representing the functions and properties of blackberry.system
 */
public class SystemNamespace extends Scriptable {

    public static final int ALLOW_VALUE = 0;
    public static final int DENY_VALUE = 1;
    public static final int PROMPT_VALUE = 2;
    public static final int NOTSET_VALUE = 3;

    public static final String SOFTWARE_VERSION = "softwareVersion";
    public static final String MODEL = "model";
    public static final String FUNCTION_HAS_PERMISSION = "hasPermission";
    public static final String FUNCTION_HAS_CAPABILITY = "hasCapability";
    public static final String FUNCTION_HAS_DATA_COVERAGE = "hasDataCoverage";
    public static final String FUNCTION_IS_MASS_STORAGE_ACTIVE = "isMassStorageActive";
    public static final String FUNCTION_SET_HOME_SCREEN = "setHomeScreenBackground";
    public static final String ALLOW = "ALLOW";
    public static final String DENY = "DENY";
    public static final String PROMPT = "PROMPT";

    private Hashtable _fields;

    /**
     * Constructor
     */
    public SystemNamespace() {
        _fields = new Hashtable();
        _fields.put( ALLOW, new Integer( ALLOW_VALUE ) );
        _fields.put( DENY, new Integer( DENY_VALUE ) );
        _fields.put( PROMPT, new Integer( PROMPT_VALUE ) );
        _fields.put( SOFTWARE_VERSION, DeviceInfo.getSoftwareVersion() );
        _fields.put( MODEL, DeviceInfo.getDeviceName() );
        _fields.put( FUNCTION_HAS_PERMISSION, new HasPermissionFunction() );
        _fields.put( FUNCTION_HAS_CAPABILITY, new HasCapabilityFunction() );
        _fields.put( FUNCTION_HAS_DATA_COVERAGE, createHasDataCoverageFunction() );
        _fields.put( FUNCTION_IS_MASS_STORAGE_ACTIVE, createIsMassStorageActiveFunction() );
        _fields.put( FUNCTION_SET_HOME_SCREEN, new SetHomeScreenBackgroundFunction() );
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

    private ScriptableFunction createHasDataCoverageFunction() {
        return new ScriptableFunctionBase() {
            public Object execute( Object thiz, Object[] args ) {
                return new Boolean( getCurrentCoverageStatus() );
            }
        };
    }

    private ScriptableFunction createIsMassStorageActiveFunction() {
        return new ScriptableFunctionBase() {
            public Object execute( Object thiz, Object[] args ) {
                return UNDEFINED;
            }
        };
    }

    private boolean getCurrentCoverageStatus() {
        return CoverageInfo.getCoverageStatus() != CoverageInfo.COVERAGE_NONE;
    }
}

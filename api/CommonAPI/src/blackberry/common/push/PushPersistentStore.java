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
package blackberry.common.push;

import java.util.Hashtable;

import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import blackberry.common.settings.SettingsManager;
import blackberry.common.util.ID;

/**
 * Class to persistent push related stuff (i.e. type, port, application descriptor arguments)
 */
public class PushPersistentStore {

    private static final long LASTKNOWN_ID;

    static {
        LASTKNOWN_ID = ID.getUniqueID( "LASTKNOWN_ID" );
    }

    /**
     * Get the last known push service type
     * 
     * @return push service type
     */
    public static int getLastKnownType() {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info != null && info.containsKey( "type" ) ) {
            return ( (Integer) info.get( "type" ) ).intValue();
        }

        return -1;
    }

    /**
     * Set the last known push service type
     * 
     * @param type
     *            push service type
     */
    public static void setLastKnownType( int type ) {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info == null ) {
            info = SettingsManager.createStorableObject();
            CodeSigningKey codeSigningKey = CodeSigningKey.get( info );
            persistentObject.setContents( new ControlledAccess( info, codeSigningKey ) );
        }
        info.put( "type", new Integer( type ) );
        persistentObject.commit();
    }

    /**
     * Get the last known push service port
     * 
     * @return push service port
     */
    public static int getLastKnownPort() {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info != null && info.containsKey( "port" ) ) {
            return ( (Integer) info.get( "port" ) ).intValue();
        }

        return -1;
    }

    /**
     * Set the last known push service port
     * 
     * @param port
     *            push service port
     */
    public static void setLastKnownPort( int port ) {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info == null ) {
            info = SettingsManager.createStorableObject();
            CodeSigningKey codeSigningKey = CodeSigningKey.get( info );
            persistentObject.setContents( new ControlledAccess( info, codeSigningKey ) );
        }
        info.put( "port", new Integer( port ) );
        persistentObject.commit();
    }

    /**
     * Get the application descriptor arguments
     * 
     * @return <code>ApplicationDescriptor</code>
     */
    public static String[] getAppDescArgs() {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info != null && info.containsKey( "appDescriptor" ) ) {
            return (String[]) info.get( "appDescriptor" );
        }
        return null;
    }

    /**
     * Set the application descriptor arguments
     * 
     * @param application
     *            descriptor arguments
     */
    public static void setAppDescArgs( String[] args ) {
        PersistentObject persistentObject = PersistentStore.getPersistentObject( LASTKNOWN_ID );
        Hashtable info = (Hashtable) persistentObject.getContents();
        if( info == null ) {
            info = SettingsManager.createStorableObject();
            persistentObject.setContents( info );
        }
        info.put( "appDescriptor", args );
        persistentObject.commit();
    }
}

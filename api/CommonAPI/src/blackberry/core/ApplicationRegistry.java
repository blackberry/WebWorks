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

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.RuntimeStore;
import blackberry.common.util.ID;

/**
 * Helps keep track of all instances of a widget. Including foreground, background and invoked.
 */
public final class ApplicationRegistry {
    private static final long APP_STORE_ID;

    private Vector _applications;

    static {
        APP_STORE_ID = ID.getUniqueID( "APP_STORE_ID" );
    }

    /**
     * Load or create a new Application Registry from the runtime store.
     * 
     * @return instance of the application registry
     */
    public static ApplicationRegistry getInstance() {
        ApplicationRegistry registry = null;

        boolean retry = false;
        do {
            Object storeObject = RuntimeStore.getRuntimeStore().get( APP_STORE_ID );
            if( storeObject != null ) {
                registry = (ApplicationRegistry) storeObject;
            } else {
                if( retry ) {
                    retry = false;
                } else {
                    registry = new ApplicationRegistry();
                    CodeSigningKey codeSigningKey = CodeSigningKey.get( registry );
                    try {
                        RuntimeStore.getRuntimeStore().put( APP_STORE_ID, new ControlledAccess( registry, codeSigningKey ) );
                    } catch( IllegalArgumentException e ) {
                        retry = true; // Just in case of a race condition try once more
                    }
                }
            }
        } while( retry );

        return registry;
    }

    /**
     * Clean up processes no longer running
     */
    private void trim() {
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        synchronized( _applications ) {
            for( int i = _applications.size() - 1; i >= 0; i-- ) {
                ApplicationDescriptor application = (ApplicationDescriptor) _applications.elementAt( i );
                if( mgr.getProcessId( application ) == -1 ) {
                    _applications.removeElementAt( i );
                }
            }
        }
    }

    private ApplicationRegistry() {
        _applications = new Vector();
    }

    /**
     * Called by a widget when it's started to add itself into the application registry
     */
    public void notifyStarted() {
        synchronized( _applications ) {
            _applications.addElement( ApplicationDescriptor.currentApplicationDescriptor() );
            trim();
        }
    }

    /**
     * Get a list of ApplicationDescriptors for all instances of this widget
     * 
     * @return a list of ApplicationDescriptors for all instances of this widget
     */
    public ApplicationDescriptor[] getApplications() {
        synchronized( _applications ) {
            trim();
            ApplicationDescriptor[] applications = new ApplicationDescriptor[ _applications.size() ];
            _applications.copyInto( applications );
            return applications;
        }
    }

    /**
     * Based on the given arguments, do they represent a daemon process?
     * @param args the args of a process
     * @return true if the args represent a daemon process, false otherwise
     */
    public static boolean isDaemon( String[] args ) {
        return args != null && args.length > 0 && args[ 0 ].equals( "PushDaemon" );
    }

    /**
     * Check if current widget is running (excluding daemon processes)
     * @return true if running, otherwise false
     */
    public static boolean isAppRunning() {
        ApplicationManager mgr = ApplicationManager.getApplicationManager();
        ApplicationDescriptor current = ApplicationDescriptor.currentApplicationDescriptor();
        int processId = mgr.getProcessId( current );
        ApplicationDescriptor[] descriptors = ApplicationRegistry.getInstance().getApplications();
    
        // Check active descriptors against current descriptor
        for( int i = 0; i < descriptors.length; i++ ) {
            ApplicationDescriptor descriptor = descriptors[ i ];
            int descriptorProcessId = mgr.getProcessId( descriptor );
            if( !isDaemon( descriptor.getArgs() ) && descriptorProcessId != -1 && descriptorProcessId != processId ) {
                return true;
            }
        }
        return false;
    }
}

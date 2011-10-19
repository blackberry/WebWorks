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
package blackberry.media;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

/**
 * Thread to monitor when a process starts or stops
 */
public abstract class ProcessCheckThread extends Thread {

    private boolean _running;
    private String _processName;

    /**
     * Default constructor
     */
    public ProcessCheckThread( String processName ) {
        _processName = processName;
    }

    /**
     * Invoke this method when processName has started
     */
    protected abstract void processStarted();

    /**
     * Invoke this method when processName has closed
     */
    protected abstract void processExited();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
        _running = true;

        // wait for process to start
        while( _running ) {
            if( isProcessRunning() ) {
                processStarted();
                break;
            }
            try {
                Thread.sleep( 100 );
            } catch( InterruptedException e ) {

            }
        }

        // clean up after it closes
        while( _running ) {
            if( !isProcessRunning() ) {
                _running = false;
                processExited();
                continue;
            }
            try {
                Thread.sleep( 100 );
            } catch( InterruptedException e ) {

            }
        }
    }

    /**
     * Stop and interrupt the thread if it is blocked
     */
    public void stop() {
        _running = false;
        this.interrupt();
    }

    private boolean isProcessRunning() {
        return isProcessRunning( _processName );
    }

    /**
     * Check if a process is running
     * 
     * @param processName
     *            the process name to check
     * @return true if processName is running
     */
    public static boolean isProcessRunning( String processName ) {
        boolean foundProcess = false;
        ApplicationManager appManager = ApplicationManager.getApplicationManager();
        ApplicationDescriptor[] appDescriptors = appManager.getVisibleApplications();

        for( int i = 0; i < appDescriptors.length; i++ ) {
            if( processName.equalsIgnoreCase( appDescriptors[ i ].getModuleName() ) ) {
                foundProcess = true;
                break;
            }
        }

        return foundProcess;
    }
}
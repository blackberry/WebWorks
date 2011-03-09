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

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Sensor;
import net.rim.device.api.ui.UiApplication;

/**
 * Abstract class that provides sub-classes the ability to prevent net.rim.device.api.script.ScriptableFunction.invoke() from
 * being called if the current application running the JS extension is not 'active'.
 * 
 * @author awong
 */
public abstract class BlockingScriptableFunction extends ScriptableFunctionBase implements ApplicationEventHandler {

    public static int UNBLOCK_TIMEOUT = 0; // unblock for timeout
    public static int UNBLOCK_BACKGROUND = 1; // unblock app moved to background
    private static int BLOCKING_TIMEOUT = 30000; // 30 seconds

    /**
     * @see net.rim.device.api.script.ScriptableFunction#invoke(Object, Object[])
     */
    public Object invoke( Object thiz, Object[] args ) throws Exception {
        if( isWidgetActive() ) {
            Timer timer = new Timer();
            try {
                Object result = null;

                // Start watchdog
                timer.schedule( new BlockingWatchdog( this ), BLOCKING_TIMEOUT );

                // Add a background listener
                EventService.getInstance().addHandler( ApplicationEventHandler.EVT_APP_BACKGROUND, this );

                // run blocking function
                result = super.invoke( thiz, args );

                return result;
            } finally {
                // Stop timer
                timer.cancel();
                // Remove background listener
                EventService.getInstance().removeHandler( this );
            }
        }
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ApplicationEvent#handlePreEvent(int, Object[])
     */
    public boolean handlePreEvent( int eventID, Object[] args ) {
        return false;
    }

    /**
     * Handle background event.
     * 
     * @see blackberry.core.ApplicationEvent#handleEvent(int, Object[])
     */
    public void handleEvent( int eventID, Object[] args ) {
        unblockNow( UNBLOCK_BACKGROUND );
    }

    private void unblockNow( int reason ) {
        Application.getApplication().invokeLater( new UnblockRunner( this, reason ) );
    }

    protected abstract void unblock( int reason );

    /**
     * Determines if the currently running widget is 'active'. Active meaning the following criteria are met: - The application is
     * the foreground app - The LCD is on and not in sleep mode - The device is out of the holster - The device is not locked
     * 
     * @return True if the widget is 'active'; false otherwise.
     */
    private static boolean isWidgetActive() {
        // is app in foreground?
        if( UiApplication.getUiApplication().isForeground() ) {
            // double-check for secondary conditions
            return Sensor.getState( Sensor.STATE_IN_HOLSTER ) == 0 && Sensor.getState( Sensor.STATE_FLIP_CLOSED ) == 0
                    && Backlight.isEnabled() && !ApplicationManager.getApplicationManager().isSystemLocked();
        }
        return false;
    }

    static class BlockingWatchdog extends TimerTask {

        BlockingScriptableFunction blockingFunction;

        BlockingWatchdog( BlockingScriptableFunction bsf ) {
            blockingFunction = bsf;
        }

        public void run() {
            blockingFunction.unblockNow( UNBLOCK_TIMEOUT );
        }
    }

    static class UnblockRunner implements Runnable {
        BlockingScriptableFunction blockingFunction;
        int myReason;

        UnblockRunner( BlockingScriptableFunction bsf, int reason ) {
            myReason = reason;
            blockingFunction = bsf;
        }

        public void run() {
            blockingFunction.unblock( myReason );
        }
    }
}

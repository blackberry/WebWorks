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
package blackberry.web.widget;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.web.WidgetConfig;

import blackberry.core.ApplicationEventHandler;
import blackberry.core.EventService;
import blackberry.web.widget.listener.HardwareKeyListener;

/**
 * Just a pass through class - provides flexibility with future screen types (I hope).
 */
public abstract class WidgetScreen extends MainScreen {
    protected HardwareKeyListener _keyListener;
    protected WidgetConfig _wConfig;
    
    protected WidgetScreen() {
        super();
    }

    protected WidgetScreen( long style ) {
        super( style );
    }
    
    protected WidgetScreen( Widget widget, long style ) {
        super( style );
        _keyListener = widget.getHardwareKeyListener();
        _wConfig = widget.getConfig();
    }
    
    public boolean onClose() {        
        if( EventService.getInstance().fireEvent(ApplicationEventHandler.EVT_APP_EXIT, null, true) ) {
            return false;
        }
        System.gc(); // MemoryMaid
        // Do not call the default onClose function so the save dialog is skipped.
        close();
        return true;
    }
    
    public void close() {
        // fire an unconsumable EXT event
        EventService.getInstance().fireEvent(ApplicationEventHandler.EVT_APP_EXIT, null, false);
        super.close();
    }
    
    /**
     * Handle the escape button if it was not previously handled.
     */
    protected boolean keyCharUnhandled( char key, int status, int time ) {
        // Fire previously stored key handling events
        if( _keyListener.isActive() ) {
            if (_keyListener.fireStoredEvent()) {
                return true;
            }
        }
        
        // Specific handling for 'back' button
        if( key != Characters.ESCAPE ) {
            return super.keyCharUnhandled( key, status, time );
        } else {
            return onBackButton();
        }
    }
    
    protected abstract boolean onBackButton();
}

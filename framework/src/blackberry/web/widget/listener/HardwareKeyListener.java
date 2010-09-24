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
package blackberry.web.widget.listener;

import net.rim.device.api.web.jse.BlackBerryWidgetToolkit;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;

/**
 * Simple implmentation of KeyListener that raises key presses to the toolkit.
 */
public final class HardwareKeyListener implements KeyListener {
    private static HardwareKeyListener _instance;
    private Integer _storedEvent;
    
    public static HardwareKeyListener getInstance() {
        if (_instance == null) {
            _instance = new HardwareKeyListener();
        }
        return _instance;
    }
    
    /**
     * @see KeyListener#keyDown
     */
    public boolean keyDown(int keycode, int time) {
        // This is a new event, clear any stored ones we have
        clearStoredEvent();
        
        int keyPressed = Keypad.key(keycode);
        int event;
        switch(keyPressed) {
            case Keypad.KEY_CONVENIENCE_1:
                event = BlackBerryWidgetToolkit.EVT_KEY_CONVENIENCE_1;
                break;
            case Keypad.KEY_CONVENIENCE_2:
                event = BlackBerryWidgetToolkit.EVT_KEY_CONVENIENCE_2;
                break;
            case Keypad.KEY_MENU:
                event = BlackBerryWidgetToolkit.EVT_KEY_MENU;
                break;
            case Keypad.KEY_SEND:
                event = BlackBerryWidgetToolkit.EVT_KEY_STARTCALL;
                break;
            case Keypad.KEY_END:
                event = BlackBerryWidgetToolkit.EVT_KEY_ENDCALL;
                break;
            case Keypad.KEY_ESCAPE:
                event = BlackBerryWidgetToolkit.EVT_KEY_BACK;
                // Store the event and handle it later
                _storedEvent = new Integer(event);
                return false;                
            case Keypad.KEY_VOLUME_DOWN:
                event = BlackBerryWidgetToolkit.EVT_KEY_VOLUME_DOWN;
                break;
            case Keypad.KEY_VOLUME_UP:
                 event = BlackBerryWidgetToolkit.EVT_KEY_VOLUME_UP;
                break;
            default:
                return false;
        }        
        return BlackBerryWidgetToolkit.getInstance().triggerOverridableEvent(event, null);
    }
    
    /**
     * @see KeyListener#keyChar
     */
    public boolean keyChar(char key, int status, int time) {
        return false;
    }
    
    /**
     * @see KeyListener#keyRepeat
     */    
    public boolean keyRepeat(int keycode, int time) {
        return false;
    }
    
    /**
     * @see KeyListener#keyStatus
     */    
    public boolean keyStatus(int keycode, int time) {
        return false;
    }
    
    /**
     * @see KeyListener#keyUp
     */    
    public boolean keyUp(int keycode, int time) {
        return false;
    }
    
    public int getStoredEvent(){
        int returnValue = _storedEvent.intValue();               
        return returnValue;
    }
    
    public boolean hasStoredEvent(){
        return (_storedEvent != null);
    }
    
    public void clearStoredEvent(){
        _storedEvent = null;
    }
    
}



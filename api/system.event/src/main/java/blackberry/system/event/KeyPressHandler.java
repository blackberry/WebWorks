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

import net.rim.device.api.system.Application;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;

/** 
 * Key listener implementation. Proxy for system key listener that
 * notifies manager generically of relevant key events.
 *
 * @author ababut
 */
class KeyPressHandler {
    private static final int IKEY_BACK = 0;
    private static final int IKEY_MENU = 1;
    private static final int IKEY_CONVENIENCE_1 = 2;
    private static final int IKEY_CONVENIENCE_2 = 3;
    private static final int IKEY_STARTCALL = 4;
    private static final int IKEY_ENDCALL = 5;
    private static final int IKEY_VOLUME_DOWN = 6;
    private static final int IKEY_VOLUME_UP = 7;
    
    private ISystemEventListener _manager;
    
    private KeyListener _keyMonitor;
    
    private boolean[] _listenerForKey = new boolean[] { false, false, false, false, false, false, false, false};
    
    KeyPressHandler(ISystemEventListener manager) {
        _manager = manager;
    }
    
    /**
     * Creates a listener if not present and registers it. Listener is active if
     * we are listening for at least one key.
     *
     * @param forKey the key code to listen for
     */
    public void listen(String forKey) {
        int keyToListenFor = Integer.parseInt(forKey);
        
        if(keyToListenFor < 0 || keyToListenFor > _listenerForKey.length) {
            throw new IllegalArgumentException("Invalid key code requested [" + keyToListenFor + "]");
        }
        
        if(_keyMonitor == null) {
            
            //Anonymous implementation of the net.rim.device.api.system.KeyListener interface
            _keyMonitor = new KeyListener() {
                
                /**
                * @see net.rim.device.api.system.KeyListener#keyDown(int, int)
                */
                public boolean keyDown( int keycode, int time ) {
                    int keyPressed = Keypad.key( keycode );
                    int event;
            
                    switch( keyPressed ) {
                        case Keypad.KEY_CONVENIENCE_1:
                            event = IKEY_CONVENIENCE_1;
                            break;
                        case Keypad.KEY_CONVENIENCE_2:
                            event = IKEY_CONVENIENCE_2;
                            break;
                        case Keypad.KEY_MENU:
                            event = IKEY_MENU;
                            break;
                        case Keypad.KEY_SEND:
                            event = IKEY_STARTCALL;
                            break;
                        case Keypad.KEY_END:
                            event = IKEY_ENDCALL;
                            break;
                        case Keypad.KEY_ESCAPE:
                            event = IKEY_BACK;
                            break;
                        case Keypad.KEY_VOLUME_DOWN:
                            event = IKEY_VOLUME_DOWN;
                            break;
                        case Keypad.KEY_VOLUME_UP:
                            event = IKEY_VOLUME_UP;
                            break;
                        default:
                            return false;
                    }
            
                    //If we're listening for this hardware key, queue up an event for it
                    if(_listenerForKey[event]) {
                        _manager.onSystemEvent(_manager.EVENT_HARDWARE_KEY, String.valueOf(event));
            
                        return true;
                    }
            
                    return false;
                }
                
                /**
                * @see net.rim.device.api.system.KeyListener#keyChar(char, int, int)
                */
                public boolean keyChar( char arg0, int arg1, int arg2 ) {
                    return false;
                }
                
                /**
                * @see net.rim.device.api.system.KeyListener#keyRepeat(int, int)
                */
                public boolean keyRepeat( int arg0, int arg1 ) {
                    return false;
                }
            
                /**
                * @see net.rim.device.api.system.KeyListener#keyStatus(int, int)
                */
                public boolean keyStatus( int arg0, int arg1 ) {
                    return false;
                }
                
                /**
                * @see net.rim.device.api.system.KeyListener#keyUp(int, int)
                */
                public boolean keyUp( int arg0, int arg1 ) {
                    return false;
                }
            };
            
            Application.getApplication().addKeyListener(_keyMonitor);
        }
        
        //Mark our event as listening
        _listenerForKey[keyToListenFor] = true;
    }
    
    /**
     * Unregisters a listener for a given key. 
     *
     * @param forKey the key code to stop listening for
     */
    public void stopListening(String forKey) {
        int keyToStop = Integer.parseInt(forKey);
        
        if(keyToStop < 0 || keyToStop > _listenerForKey.length) {
            throw new IllegalArgumentException("Invalid key code requested [" + keyToStop + "]");
        }
        
        //Mark key as not listening
        _listenerForKey[keyToStop] = false;
        
        //De-register application listener if we are no longer listening for any keys
        if(!isListening()) {
            Application.getApplication().removeKeyListener(_keyMonitor);
            _keyMonitor = null;
        }
    }
    
    /**
     * Unregisters all active listeners. 
     */
    public void stopListeningToAll() {
        for(int i = _listenerForKey.length - 1; i >= 0; i--) {
            if (_listenerForKey[i]) stopListening(String.valueOf(i));
        }
    }
    
    /**
     * Indicates listener status
     *
     * @return true if listener is active
     */
    public boolean isListening() {
        for(int i = _listenerForKey.length - 1; i >= 0; i--) {
            if (_listenerForKey[i]) return true;
        }
        
        return false;
    }
}

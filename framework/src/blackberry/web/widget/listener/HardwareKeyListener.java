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
package blackberry.web.widget.listener;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import blackberry.web.widget.impl.WidgetConfigImpl;

/**
 * Implementation of a key listener that will capture key events, store them, and report the event 'unconsumed'. This allows other
 * handlers to perform regular handling first - for example, allowing the back button to close a menu, instead of intercepting the
 * close and using a JS handler, which will prevent the menu from ever being closed.
 * 
 */
public final class HardwareKeyListener implements KeyListener {
    
    private Vector           _keyListeners;
    private StoredEvent      _storedEvent;
    private boolean          _isActive;

    private static final int KEY_DOWN   = 0;
    private static final int KEY_REPEAT = 1;
    private static final int KEY_UP     = 2;
    private static final int KEY_STATUS = 3;
    private static final int KEY_CHAR   = 4;

    /**
     * Constructor
     * 
     * @param config
     *            implementation of the WidgetConfig object used to created this widget
     */
    public HardwareKeyListener( WidgetConfigImpl config ) {
        _keyListeners = config.getKeyListeners();
        _storedEvent = null;
        _isActive = false;

        // add listener - if necessary
        if( _keyListeners.size() > 0 ) {
            _isActive = true;
            Application.getApplication().addKeyListener( this );
        }
    }

    /**
     * Indicates if the KeyListener is actively listening for events. This occurs if key listeners have been found in the
     * extensions, otherwise it will not add itself for key presses.
     * 
     * @return true if key presses are caught; false otherwise
     */
    public boolean isActive() {
        return _isActive;
    }

    /**
     * @see KeyListener#keyDown(int, int)
     */
    public boolean keyDown( int keycode, int time ) {
        _storedEvent = new StoredEvent( KEY_DOWN, keycode, time );
        int key = Keypad.key( keycode );
        if( key == Keypad.KEY_SEND || key == Keypad.KEY_END || key == Keypad.KEY_CONVENIENCE_1 || key == Keypad.KEY_CONVENIENCE_2 ) {
            // fire the event right away because these two keys do not trigger
            // WidgetScreen.keyCharUnhandled callback.
            return fireStoredEvent();
        }
        return false;
    }

    /**
     * @see KeyListener#keyChar(char, int, int)
     */
    public boolean keyChar( char key, int status, int time ) {
        _storedEvent = new StoredEvent( KEY_CHAR, key, status, time );
        return false;
    }

    /**
     * @see KeyListener#keyRepeat(int, int)
     */
    public boolean keyRepeat( int keycode, int time ) {
        _storedEvent = new StoredEvent( KEY_REPEAT, keycode, time );
        return false;
    }

    /**
     * @see KeyListener#keyStatus(int, int)
     */
    public boolean keyStatus( int keycode, int time ) {
        _storedEvent = new StoredEvent( KEY_STATUS, keycode, time );
        return false;
    }

    /**
     * @see KeyListener#keyUp(int, int)
     */
    public boolean keyUp( int keycode, int time ) {
        _storedEvent = new StoredEvent( KEY_UP, keycode, time );
        return false;
    }

    /**
     * Fires the last stored event - if one exists
     * 
     * @return true if the event was consumed; false otherwise
     */
    public boolean fireStoredEvent() {
        boolean result = false;

        if( _storedEvent != null ) {
            result = _storedEvent.fireEvent();
            _storedEvent = null;
        }
        return result;
    }

    private class StoredEvent {
        int _keyCode, _time, _status, _keyType;
        char _keyChar;

        StoredEvent( int keyType, int keyCode, int time ) {
            _keyCode = keyCode;
            _time = time;
            _keyType = keyType;
        }

        StoredEvent( int keyType, char keyChar, int status, int time ) {
            _status = status;
            _time = time;
            _keyChar = keyChar;
            _keyType = keyType;
        }

        boolean fireEvent() {
            boolean result = false;
            KeyListener kl = null;
            for( Enumeration e = _keyListeners.elements(); e.hasMoreElements(); ) {
                kl = (KeyListener) e.nextElement();

                switch( _keyType ) {
                    case KEY_DOWN:
                        result = result || kl.keyDown( _keyCode, _time );
                        break;
                    case KEY_REPEAT:
                        result = result || kl.keyRepeat( _keyCode, _time );
                        break;
                    case KEY_STATUS:
                        result = result || kl.keyStatus( _keyCode, _time );
                        break;
                    case KEY_UP:
                        result = result || kl.keyUp( _keyCode, _time );
                        break;
                    case KEY_CHAR:
                        result = result || kl.keyChar( _keyChar, _status, _time );
                        break;
                }
            }
            return result;
        }
    }
}

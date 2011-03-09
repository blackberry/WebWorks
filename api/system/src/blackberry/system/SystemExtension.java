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

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import blackberry.system.SystemNamespace;
import blackberry.system.event.SystemEventNamespace;

/**
 * Implementation of blackberry.system extension.
 */
public class SystemExtension implements WidgetExtension, KeyListener {

    private static final String FEATURE_BLACKBERRY_SYSTEM = "blackberry.system";
    private static final String FEATURE_BLACKBERRY_SYSTEM_EVENT = "blackberry.system.event";

    private SystemNamespaceHandler _smh;

    /**
     * Constructor
     */
    public SystemExtension() {
        _smh = new SystemNamespaceHandler();
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(String, String, Document, ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {
        if( feature.equals( FEATURE_BLACKBERRY_SYSTEM ) ) {
            _smh.setNamespace( new SystemNamespace() );
        } else if( feature.equals( FEATURE_BLACKBERRY_SYSTEM_EVENT ) ) {
            _smh.setNamespace( SystemEventNamespace.getInstance( true ) );
        }
        scriptEngine.addExtension( FEATURE_BLACKBERRY_SYSTEM, _smh );
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { FEATURE_BLACKBERRY_SYSTEM, FEATURE_BLACKBERRY_SYSTEM_EVENT };
    }

    /**
     * @see net.rim.device.api.system.KeyListener#keyChar(char, int, int)
     */
    public boolean keyChar( char arg0, int arg1, int arg2 ) {
        return false;
    }

    /**
     * @see net.rim.device.api.system.KeyListener#keyDown(int, int)
     */
    public boolean keyDown( int keycode, int time ) {
        int keyPressed = Keypad.key( keycode );
        int event;
        switch( keyPressed ) {
            case Keypad.KEY_CONVENIENCE_1:
                event = SystemEventNamespace.IKEY_CONVENIENCE_1;
                break;
            case Keypad.KEY_CONVENIENCE_2:
                event = SystemEventNamespace.IKEY_CONVENIENCE_2;
                break;
            case Keypad.KEY_MENU:
                event = SystemEventNamespace.IKEY_MENU;
                break;
            case Keypad.KEY_SEND:
                event = SystemEventNamespace.IKEY_STARTCALL;
                break;
            case Keypad.KEY_END:
                event = SystemEventNamespace.IKEY_ENDCALL;
                break;
            case Keypad.KEY_ESCAPE:
                event = SystemEventNamespace.IKEY_BACK;
                break;
            case Keypad.KEY_VOLUME_DOWN:
                event = SystemEventNamespace.IKEY_VOLUME_DOWN;
                break;
            case Keypad.KEY_VOLUME_UP:
                event = SystemEventNamespace.IKEY_VOLUME_UP;
                break;
            default:
                return false;
        }
        ScriptableFunction func = SystemEventNamespace.getInstance( false ).getCallback( event );
        if( func != null ) {
            try {
                func.invoke( null, new Object[] {} );
                // consume the event
                return true;
            } catch( Exception e ) {
                // function failed
            }
        }
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

    private static class SystemNamespaceHandler extends Scriptable {

        Scriptable _sysNamespace;
        Scriptable _eventNamespace;

        void setNamespace( SystemEventNamespace es ) {
            _eventNamespace = es;
        }

        void setNamespace( SystemNamespace ns ) {
            _sysNamespace = ns;
        }

        public Object getField( String fieldName ) {
            if( fieldName.equals( SystemEventNamespace.NAME ) ) {
                return _eventNamespace;
            } else if( _sysNamespace != null ) {
                try {
                    return _sysNamespace.getField( fieldName );
                } catch( Exception e ) {
                }
            }
            return UNDEFINED;
        }
    }
}

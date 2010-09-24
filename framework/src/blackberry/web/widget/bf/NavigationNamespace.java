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
package blackberry.web.widget.bf;

import java.util.Hashtable;
import net.rim.device.api.script.*;

import net.rim.device.api.browser.field2.BrowserField;

import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.bf.WidgetNavigationController;

public class NavigationNamespace extends Scriptable {

    public static final String NAME = "blackberry.focus";

    public static final String LABEL_RIGHT = "RIGHT";
    public static final String LABEL_LEFT = "LEFT";
    public static final String LABEL_UP = "UP";
    public static final String LABEL_DOWN = "DOWN";
    
    public static final String LABEL_SET_FOCUS = "setFocus";
    public static final String LABEL_GET_FOCUS = "getFocus";
    public static final String LABEL_GET_OLDFOCUS = "getOldFocus";
    public static final String LABEL_GET_DIRECTION = "getDirection";
    
    private BrowserFieldScreen _widgetScreen;
    
    private String _oldFocused;
    private String _currentFocused;
    private int _direction;
    
    private SetRimFocus _funcSetRimFocus;
    private GetRimFocus _funcGetRimFocus;
    private GetOldFocus _funcGetOldFocus;
    private GetDirection _funcGetDirection;
    
    public NavigationNamespace(BrowserFieldScreen widgetScreen) {
        _widgetScreen = widgetScreen;
        
        _oldFocused = "";
        _currentFocused = "";
        _direction = -1;
        
        _funcSetRimFocus = new SetRimFocus();
        _funcGetRimFocus = new GetRimFocus();
        _funcGetOldFocus = new GetOldFocus();
        _funcGetDirection= new GetDirection();
    }
    
    public void setOldFocusedId(String id) {
        if (id == null) {
            _oldFocused = "";
        } else {
            _oldFocused = id;
        }
    }
    
    public void setNewFocusedId(String id) {
        if (id == null) {
            _currentFocused = "";
        } else {
            _currentFocused = id;
        }
    }
        
    public void setDirection(int direction) {
        _direction = direction;
    }

    /* @Override */ public Scriptable getParent() {
        return null;
    }

    /* @Override */ public Object getField( String name ) throws Exception {
        if (name.equals(LABEL_RIGHT)) {
            return new Integer(WidgetNavigationController.FOCUS_NAVIGATION_RIGHT);
        }

        if (name.equals(LABEL_LEFT)) {
            return new Integer(WidgetNavigationController.FOCUS_NAVIGATION_LEFT);
        }
        
        if (name.equals(LABEL_DOWN)) {
            return new Integer(WidgetNavigationController.FOCUS_NAVIGATION_DOWN);
        }
        
        if (name.equals(LABEL_UP)) {
            return new Integer(WidgetNavigationController.FOCUS_NAVIGATION_UP);
        }

        if (name.equals(LABEL_SET_FOCUS)) {
            return _funcSetRimFocus;
        }

        if (name.equals(LABEL_GET_FOCUS)) {
            return _funcGetRimFocus;
        }
        
        if (name.equals(LABEL_GET_OLDFOCUS)) {
            return _funcGetOldFocus;
        }

        if (name.equals(LABEL_GET_DIRECTION)) {
            return _funcGetDirection;
        }

        return UNDEFINED;
    }

    /* @Override */ public boolean putField( String name, Object value ) throws Exception {
        return false;
    }

    private class SetRimFocus extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args != null && args.length == 1 && args[0] != null) {
                String id = args[0].toString();
                _widgetScreen.getWidgetNavigationController().setRimFocus(id);
                return UNDEFINED;
            }
            
            throw new IllegalArgumentException();
        }
    }
    
    private class GetRimFocus extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
                return _currentFocused;
            }
            
            throw new IllegalArgumentException();
        }
    }
    
    private class GetOldFocus extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
                return _oldFocused;
            }
            
            throw new IllegalArgumentException();
        }
    }    
    
    private class GetDirection extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
                return (new Integer(_direction));
            }
            
            throw new IllegalArgumentException();
        }
    }    
}

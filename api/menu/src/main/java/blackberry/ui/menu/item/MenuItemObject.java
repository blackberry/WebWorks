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
package blackberry.ui.menu.item;

import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;
import blackberry.ui.menu.MenuExtension;
import blackberry.ui.menu.MenuNamespace;

import net.rim.device.api.script.*;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

/**
 * class representing the MenuItem object.
 */
public class MenuItemObject extends Scriptable {

    private final Boolean _isSeparator;
    private Integer _ordinal;
    private String _caption;

    public static final String FIELD_CAPTION = "caption";
    public static final String FIELD_ORDINAL = "ordinal";
    public static final String FIELD_ISDEFAULT = "isDefault";
    public static final String FIELD_ISSEPARATOR = "isSeparator";

    private MenuItem _menuItem;

    private static class MenuItemImpl extends MenuItem {
        private ScriptableFunction _callBackFunc;
        
        public MenuItemImpl( String text, int ordinal, int priority,  ScriptableFunction callback ) {
            super( text , ordinal, priority );
            _callBackFunc= callback;
        }

        public void run () {
            Dispatcher.getInstance().dispatch(new MenuItemDispatcherEvent(MenuNamespace.getInstance(), _callBackFunc));
        } 
    }

    /**
     * Constructs a new empty MenuItem Object
     * 
     * @param isSeparator
     *                  - the value to set Separator. 
     * @param ordinal
     *                  - the value to set for the ordinal of this menu item.
     * @param caption
     *                  - text to appear in the Menu.
     * @param callback
     *                  - the value to set the Callback functions for menuItem 
     */
    public MenuItemObject( final Boolean isSeparator, final Integer ordinal, final String caption, final ScriptableFunction callback ) {

        _isSeparator = isSeparator;
        _ordinal = ordinal;
        _caption = caption;
        _menuItem = isSeparator.booleanValue() ? MenuItem.separator( ordinal.intValue() ) : new MenuItemImpl( caption, ordinal
                .intValue(), Integer.MAX_VALUE, callback );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#putField(String, Object)
     */
    public boolean putField( String name, Object value ) throws Exception {
        if( name.equals( FIELD_CAPTION ) ) {
            _caption = (String) value;
            _menuItem.setText( _caption );
            return true;
        }else if( name.equals( FIELD_ORDINAL ) ) {
            _ordinal = (Integer) value;
            _menuItem.setOrdinal( _ordinal.intValue() );
           return true;
        }
        
        return super.putField( name, value );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( FIELD_CAPTION ) ) {
            return _caption;
        } else if( name.equals( FIELD_ISSEPARATOR ) ) {
            return _isSeparator;
        } else if( name.equals( FIELD_ORDINAL ) ) {
            return _ordinal;
        } else if( name.equals( FIELD_ISDEFAULT ) ) {
            // Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) MenuExtension.getBrowserField().getScreen();

            MenuItem mi = screen.getDefaultMenuItem( Menu.INSTANCE_DEFAULT );
            if( mi == null ) {
                return Boolean.FALSE;
            }
            return new Boolean( mi.equals( _menuItem ) );
        }

        return super.getField( name );
    }
    
    /**
     * Get current menuItem object.
     * 
     * @return MenuItem 
     */
    public MenuItem getMenuItem() {
        return _menuItem;
    }

    /**
     * Dynamically sets the priority of this menu item.
     * 
     * @param priority - the value to set the priority of this menu item.
     */
    public void setPriority( int priority ) {
        _menuItem.setPriority( priority );
    }

    private static class MenuItemDispatcherEvent extends DispatchableEvent {
        ScriptableFunction _callBackFunc;
        
        MenuItemDispatcherEvent(MenuNamespace context, ScriptableFunction callBackFunc) {
            super(context);
            _callBackFunc = callBackFunc;
        }
                        
        protected void dispatch() {
            try {
                if(_callBackFunc != null) {
                   _callBackFunc.invoke(null, null);
                }
            } catch (Exception e) {
            }
        }
    } 
}

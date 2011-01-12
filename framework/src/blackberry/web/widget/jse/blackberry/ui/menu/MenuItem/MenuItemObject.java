/*
 * MenuItemObject.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.jse.blackberry.ui.menu.MenuItem;

import blackberry.web.widget.jse.blackberry.ui.menu.MenuNamespace;

import blackberry.web.widget.jse.base.ScriptField;
import blackberry.web.widget.jse.base.ScriptableObjectBase;

import net.rim.device.api.script.*;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.threading.Dispatcher;


public class MenuItemObject extends ScriptableObjectBase {

    public static final String FIELD_CAPTION = "caption";
    public static final String FIELD_ORDINAL = "ordinal";
    public static final String FIELD_ISDEFAULT = "isDefault";
    public static final String FIELD_ISSEPARATOR = "isSeparator";
    
    private ScriptableFunction _callBackFunc;
    private MenuItem _menuItem;
    
    private class MenuItemImpl extends MenuItem {
        public MenuItemImpl (String text, int ordinal, int priority) {
            super(text, ordinal, priority);
        }

        public void run () {
            MenuNamespace.getInstance().getWidgetScreen().getUiEventDispatcher().dispatch(new MenuItemDispatcherEvent(MenuNamespace.getInstance(), MenuItemObject.this));
        }        
    }
    
    public MenuItemObject(Boolean isSeparator, Integer ordinal, String caption, ScriptableFunction callback) {
        addItem(new ScriptField(FIELD_CAPTION, caption, ScriptField.TYPE_STRING, false, false));
        addItem(new ScriptField(FIELD_ISSEPARATOR, isSeparator, ScriptField.TYPE_BOOLEAN, true, false));
        addItem(new ScriptField(FIELD_ORDINAL, ordinal, ScriptField.TYPE_INT, false, false));
        
        _callBackFunc = callback;
        _menuItem = isSeparator.booleanValue() ? MenuItem.separator(ordinal.intValue()) :
                                                 new MenuItemImpl(caption, ordinal.intValue(), Integer.MAX_VALUE); 
    }
 
     /* @Override */ public boolean putField( String name, Object value ) throws Exception {
        boolean putSucceed = super.putField(name, value);
        
        if(putSucceed) { 
            if(name.equals(FIELD_CAPTION)) {
                _menuItem.setText((String)(value));
            }
            if(name.equals(FIELD_ORDINAL)) {
                _menuItem.setOrdinal(((Integer)value).intValue());
            }
        }
        return putSucceed;
    }
    
    /* @Override */ public Object getField( String name ) throws Exception {
        if(name.equals(FIELD_ISDEFAULT)) {
            //Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) MenuNamespace.getInstance().getWidgetScreen();
            
            MenuItem mi = screen.getDefaultMenuItem(Menu.INSTANCE_DEFAULT);
            if (mi == null) {
                return Boolean.FALSE;
            }
            return new Boolean(mi.equals(_menuItem));
        }
        
        return super.getField(name);
    }
    
    public MenuItem getMenuItem() {
        return _menuItem;
    }
    
    public void setPriority (int priority) {
        _menuItem.setPriority(priority);
    }
    
    private class MenuItemDispatcherEvent extends Dispatcher.DispatchableEvent {
        MenuItemObject _menuItemObject;
        
        MenuItemDispatcherEvent(MenuNamespace context, MenuItemObject menuItemObject) {
            super(context);
            _menuItemObject = menuItemObject;
        }
                        
        protected void dispatch() {
            try {
                if(_menuItemObject._callBackFunc != null) {
                    _menuItemObject._callBackFunc.invoke(null, null);
                }
            } catch (Exception e) {
            }
            return;
        }
    }    
}

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
package blackberry.web.widget.jse.blackberry.ui.menu;

import java.util.Hashtable;

import blackberry.web.widget.jse.base.ScriptableObjectBase;
import blackberry.web.widget.jse.base.ScriptField;
import blackberry.web.widget.jse.blackberry.ui.menu.MenuItem.MenuItemConstructor;
import blackberry.web.widget.jse.blackberry.ui.menu.MenuItem.MenuItemObject;
import blackberry.web.widget.jse.base.ScriptableFunctionBase;
import blackberry.web.widget.jse.base.FunctionSignature;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import blackberry.web.widget.bf.BrowserFieldScreen;


public class MenuNamespace extends ScriptableObjectBase{
    public static final String NAME = "blackberry.ui.menu";
    public static final String VERSION = "1.0.1";
    
    private static MenuNamespace _instance;
    
    private BrowserFieldScreen _widgetScreen;
    
    public static MenuNamespace createInstance(BrowserFieldScreen widgetScreen) {
        if (_instance == null) {
            _instance = new MenuNamespace(widgetScreen);
        }
        return _instance;
    }
    
    public static MenuNamespace getInstance() {
        return _instance;
    }
    
    public BrowserFieldScreen getWidgetScreen() {
        return _widgetScreen;
    }

    private MenuNamespace(BrowserFieldScreen widgetScreen) {
        _widgetScreen = widgetScreen;
        
        addItem(new ScriptField(MenuItemConstructor.NAME, 
                                new MenuItemConstructor(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));
        addItem(new ScriptField(ClearMenuItemsFunction.NAME, 
                                new ClearMenuItemsFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));
        addItem(new ScriptField(GetMenuItemsFunction.NAME, 
                                new GetMenuItemsFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));                   
        addItem(new ScriptField(SetDefaultMenuItemFunction.NAME, 
                                new SetDefaultMenuItemFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));
        addItem(new ScriptField(RemoveMenuItemFunction.NAME, 
                                new RemoveMenuItemFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true)); 
        addItem(new ScriptField(AddMenuItemFunction.NAME, 
                                new AddMenuItemFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));           
        addItem(new ScriptField(OpenFunction.NAME, 
                                new OpenFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));                    
        addItem(new ScriptField(HasMenuItemFunction.NAME, 
                                new HasMenuItemFunction(), 
                                ScriptField.TYPE_SCRIPTABLE, 
                                true, 
                                true));
    }
    
    private class AddMenuItemFunction extends ScriptableFunctionBase{
        public static final String NAME = "addMenuItem";
            
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {
            //Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) _widgetScreen;
            MenuItemObject menuItem = (MenuItemObject) args[0];
            
            boolean addSucceed = MenuItemObjectManager.addMenuItemObject(menuItem);
            
            if(addSucceed) {
                screen.addMenuItem(menuItem.getMenuItem());
            }    
            return UNDEFINED;
        }
        
        /* @Override */ protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature(1);
            fs.addParam(MenuItemObject.class, true);
            return new FunctionSignature[] { fs };
        }    
    }
    
    private class ClearMenuItemsFunction extends ScriptableFunctionBase{
        public static final String NAME = "clearMenuItems";
            
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {
            //Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) _widgetScreen;
            Object [] list = MenuItemObjectManager.getMenuItemObjects();
            
            for(int i = 0; i < list.length; i++) {
                screen.removeMenuItem(((MenuItemObject)list[i]).getMenuItem());
            }
            MenuItemObjectManager.clearMenuItemObjects();
    
            return UNDEFINED;
        }
    }
    
    private class GetMenuItemsFunction extends ScriptableFunctionBase {
        public static final String NAME = "getMenuItems";
    
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {
            return MenuItemObjectManager.getMenuItemObjects();        
        }
    }
    
    private class HasMenuItemFunction extends ScriptableFunctionBase{
        public static final String NAME = "hasMenuItem";
            
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {     
            MenuItemObject menuItem = (MenuItemObject) args[0];   
            return new Boolean(MenuItemObjectManager.hasMenuItemObject(menuItem));
        }
        
        /* @Override */ protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature(1);
            fs.addParam(MenuItemObject.class, true);
            return new FunctionSignature[] { fs };
        }
    }
    
    private class OpenFunction extends ScriptableFunctionBase {
        public static final String NAME = "open";
            
        private final class ShowMenu implements Runnable {
            private Menu _menu;

            public ShowMenu (Menu menu) {
                _menu = menu;
            }
            
            public void run() {
                _menu.show();
            }    
        }
    
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {
            //Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) _widgetScreen;
            ShowMenu thread = new ShowMenu(screen.getMenu(Menu.INSTANCE_DEFAULT));
            screen.getApplication().invokeLater(thread);
            
            return UNDEFINED;
        }
    }
    
    private class RemoveMenuItemFunction extends ScriptableFunctionBase {
        public static final String NAME = "removeMenuItem";
    
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {        
            //Get the screen that currently owns the BrowserField
            MainScreen screen = (MainScreen) _widgetScreen;
            MenuItemObject menuItem = (MenuItemObject) args[0];
            
            screen.removeMenuItem(menuItem.getMenuItem()); 
            MenuItemObjectManager.removeMenuItemObject(menuItem);        
            return UNDEFINED;
        }
        
        /* @Override */ protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature(1);
            fs.addParam(MenuItemObject.class, true);
            return new FunctionSignature[] { fs };
        }
    }
    
    private class SetDefaultMenuItemFunction extends ScriptableFunctionBase {
        public static final String NAME = "setDefaultMenuItem";
    
        /* @Override */ protected Object execute( Object thiz, Object[] args ) throws Exception {
            MenuItemObject menuItem = (MenuItemObject)args[0];
            
            if(!((Boolean)menuItem.getField(MenuItemObject.FIELD_ISSEPARATOR)).booleanValue()) {   
                //Get the screen that currently owns the BrowserField
                MainScreen screen = (MainScreen) _widgetScreen;

                MenuItem defaultItem = screen.getDefaultMenuItem(Menu.INSTANCE_DEFAULT);
                if(defaultItem == null) {
                    menuItem.setPriority(Integer.MAX_VALUE);
                } else {
                    menuItem.setPriority(defaultItem.getPriority()-1);
                }
            }
            return UNDEFINED;
        }

        /* @Override */ protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature(1);
            fs.addParam(MenuItemObject.class, true);
            return new FunctionSignature[] { fs };
        }    
    }    
}

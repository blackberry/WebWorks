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
import blackberry.web.widget.jse.blackberry.ui.menu.MenuItem.MenuItemObject;
import java.util.Vector;


/*
    A manager class that manages all the dynamically created MenuItemObjects
*/
public class MenuItemObjectManager {
    
    private static Vector _menuItemSet;
    
    /**
     * Add a MenuItemObject to the manager. MenuItemObjectManger would not contain duplicate MenuItemObjects.
     * @param item <description> Item to be added.
     * @return <description> return true if item is added successfully, false otherwise 
     * (means manager already contains that item)
     */
    public static boolean addMenuItemObject (MenuItemObject item) {
        if(_menuItemSet == null) {
            _menuItemSet = new Vector();
        }
        
        if(_menuItemSet.contains(item)) {
            return false;
        }
        
        _menuItemSet.addElement(item);
        return true;
    }
    
    public static void removeMenuItemObject (MenuItemObject item) {
        if(_menuItemSet != null) {
            _menuItemSet.removeElement(item);
        }
    }
    
    public static Object[] getMenuItemObjects () {
        if(_menuItemSet == null) {
            return new MenuItemObject[0];
        }
           
        Object[] list = new Object[_menuItemSet.size()];
        _menuItemSet.copyInto(list);
        
        return list;
    }
    
    public static void clearMenuItemObjects () {
        if(_menuItemSet != null) {
            _menuItemSet.removeAllElements();
            _menuItemSet = null;
        }
    }
    
    public static boolean hasMenuItemObject (MenuItemObject obj) {
        if(_menuItemSet != null) {
            return _menuItemSet.contains(obj);
        }
        return false;
    }
}

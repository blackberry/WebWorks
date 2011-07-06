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
package blackberry.ui.menu;

import java.util.Vector;

import blackberry.ui.menu.item.MenuItemObject;




/**
 * A manager class that manages all the dynamically created MenuItemObjects
 */
public class MenuItemObjectManager {

    private static MenuItemObjectManager _instance = null;
    private Vector _menuItemSet = null;

    private MenuItemObjectManager() {
        _menuItemSet = new Vector();
    }

    /**
     * getInstance of MenuItemObjectManger
     * 
     * @return instance of MenuItemObjectManager
     */
    public static MenuItemObjectManager getInstance() {
       if( _instance == null ) {
           _instance = new MenuItemObjectManager();
       }
       
        return _instance;
    }

    /**
     * Add a MenuItemObject to the manager. MenuItemObjectManger would not contain duplicate MenuItemObjects.
     * 
     * @param Item
     *            Item to be added.
     * 
     * @return return true if item is added successfully, false otherwise (means manager already contains that item)
     */
    public static boolean addMenuItemObject( MenuItemObject item ) {
        MenuItemObjectManager instance = MenuItemObjectManager.getInstance();

        if( instance._menuItemSet.contains( item ) ) {
            return false;
        }

        instance._menuItemSet.addElement( item );
        return true;
    }

    /**
     * Remove a MenuItemObject from the manager.
     * 
     * @param item
     *            Item to be removed.
     * 
     */
    public static void removeMenuItemObject( MenuItemObject item ) {
        MenuItemObjectManager instance = MenuItemObjectManager.getInstance();
        if( instance._menuItemSet != null ) {
            instance._menuItemSet.removeElement( item );
        }
    }

    /**
     * Get all MenuItemObjects from the manager.
     * 
     * @return return array of all MenuItemObjects .
     */
    public static Object[] getMenuItemObjects() {
        MenuItemObjectManager instance = MenuItemObjectManager.getInstance();
        if( instance._menuItemSet == null ) {
            return new MenuItemObject[ 0 ];
        }

        Object[] list = new Object[ instance._menuItemSet.size() ];
        instance._menuItemSet.copyInto( list );

        return list;
    }

    /**
     * Clear all MenuItemObject from the manager.
     */
    public static void clearMenuItemObjects() {
        MenuItemObjectManager instance = MenuItemObjectManager.getInstance();
        if( instance._menuItemSet != null ) {
            instance._menuItemSet.removeAllElements();
        }
    }

    /**
     * Has a MenuItemObject.Test if the specific item is a component in the manager list.
     * 
     * @param item
     *            - an MenuItemObject
     * 
     * @return return true if item is in the manager list, false otherwise (means manager doesn't contains that item)
     */
    public static boolean hasMenuItemObject( MenuItemObject item ) {
        MenuItemObjectManager instance = MenuItemObjectManager.getInstance();
        if( instance._menuItemSet != null ) {
            return instance._menuItemSet.contains( item );
        }
        return false;
    }

}

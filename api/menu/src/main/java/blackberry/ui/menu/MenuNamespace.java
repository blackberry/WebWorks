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

import java.util.Hashtable;

import blackberry.ui.menu.item.MenuItemConstructor;
import net.rim.device.api.script.Scriptable;

/**
 * A class registering UI Menu extension function in namespace
 */
public class MenuNamespace extends Scriptable {

    private static MenuNamespace _instance;
    private Hashtable _functions;
        
    /**
     * Constructs a MenuNamespace object.
     */
    public MenuNamespace() {
        _functions = new Hashtable();
        _functions.put( MenuItemConstructor.NAME, new MenuItemConstructor() );
        _functions.put( ClearMenuItemsFunction.NAME, new ClearMenuItemsFunction() );
        _functions.put( GetMenuItemsFunction.NAME, new GetMenuItemsFunction() );
        _functions.put( SetDefaultMenuItemFunction.NAME, new SetDefaultMenuItemFunction() );
        _functions.put( RemoveMenuItemFunction.NAME, new RemoveMenuItemFunction() );
        _functions.put( AddMenuItemFunction.NAME, new AddMenuItemFunction() );
        _functions.put( OpenFunction.NAME, new OpenFunction() );
        _functions.put( HasMenuItemFunction.NAME, new HasMenuItemFunction() );

    }

    /**
     * Get the instance of MenuNamescape
     * 
     * @return instance of ManuNamespace
     */
    public static MenuNamespace getInstance() {
        if( _instance == null ) {
            _instance = new MenuNamespace();
        }
        return _instance;
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( _functions.containsKey( name ) )
            return _functions.get( name );

        return UNDEFINED;

    }
   
}

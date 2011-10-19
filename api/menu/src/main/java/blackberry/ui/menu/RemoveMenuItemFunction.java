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

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.ui.menu.item.MenuItemObject;

import net.rim.device.api.ui.container.MainScreen;

/**
 * Implementation of function removeMenuItem.
 */
public class RemoveMenuItemFunction extends ScriptableFunctionBase {
    public static final String NAME = "removeMenuItem";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        // Get the screen that currently owns the BrowserField
        MainScreen screen = (MainScreen) MenuExtension.getBrowserField().getScreen();
        MenuItemObject menuItem = (MenuItemObject) args[ 0 ];

        screen.removeMenuItem( menuItem.getMenuItem() );
        MenuItemObjectManager.removeMenuItemObject( menuItem );
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( MenuItemObject.class, true );
        return new FunctionSignature[] { fs };
    }
}

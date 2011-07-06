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

import blackberry.core.ScriptableFunctionBase;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

/**
 * Implementation of open function
 */
public final class OpenFunction extends ScriptableFunctionBase {

    public static final String NAME = "open";

    private final static class ShowMenu implements Runnable {

        private Menu _menu;

        public ShowMenu( Menu menu ) {
            _menu = menu;
        }

        public void run() {
            _menu.show();
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        // Get the screen that currently owns the BrowserField
        MainScreen screen = (MainScreen) MenuExtension.getBrowserField().getScreen();
        ShowMenu thread = new ShowMenu( screen.getMenu( Menu.INSTANCE_DEFAULT ) );
        screen.getApplication().invokeLater( thread );

        return UNDEFINED;
    }

}

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
package blackberry.ui.dialog;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of asynchronous custom ask function (blackberry.ui.dialog.customAskAsync)
 * 
 * @author dmateescu
 * 
 */
public class CustomAskAsyncFunction extends ScriptableFunctionBase {

    public static final String NAME = "customAskAsync";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        String message;
        String[] buttons;
        // the default value of the default choice. The developer cannot change it.
        final int defaultChoice = 0;
        // callback function
        ScriptableFunction callback = (ScriptableFunction) args[ 2 ];
        // the default value of the global status. The developer cannot change it.
        final boolean global = false;
        // message
        message = (String) args[ 0 ];
        // choices & values
        Scriptable stringArray = (Scriptable) args[ 1 ];
        int count = stringArray.getElementCount();
        buttons = new String[ count ];
        for( int i = 0; i < count; i++ ) {
            buttons[ i ] = stringArray.getElement( i ).toString();
        }

        Runnable dr = DialogRunnableFactory.getCustomAskRunnable( message, buttons, defaultChoice, global, callback );
        // queue
        UiApplication.getUiApplication().invokeLater( dr );
        // return value
        return Scriptable.UNDEFINED;
    }


    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        // message
        fs.addParam( String.class, true );
        // choices
        fs.addParam( Scriptable.class, true );
        // callback
        fs.addParam( ScriptableFunction.class, true );
        // filler
        fs.addParam( Object.class, false );
        return new FunctionSignature[] { fs };
    }
}

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
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import blackberry.core.BlockingScriptableFunction;
import blackberry.core.FunctionSignature;

/**
 * Implementation of custom ask function (blackberry.ui.dialog.customAsk)
 * 
 * @author dmeng
 * 
 */
public class CustomAskFunction extends BlockingScriptableFunction {

    public static final String NAME = "customAsk";

    private DialogRunner _currentDialog;

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        String message;
        String[] choices;
        int[] values;
        int defaultChoice = 0;
        boolean global = false;

        // message
        message = (String) args[ 0 ];

        // choices & values
        Scriptable stringArray = (Scriptable) args[ 1 ];
        int count = stringArray.getElementCount();
        choices = new String[ count ];
        values = new int[ count ];
        for( int i = 0; i < count; i++ ) {
            choices[ i ] = stringArray.getElement( i ).toString();
            values[ i ] = i;
        }

        // default choice
        if( args.length > 2 && args[ 2 ] != null ) {
            if( !( args[ 2 ] instanceof Integer ) ) {
                throw new IllegalArgumentException();
            }

            defaultChoice = ( (Integer) args[ 2 ] ).intValue();
            if( defaultChoice < 0 || defaultChoice >= count ) {
                defaultChoice = 0;
            }
        }

        // global
        if( args.length > 3 && args[ 3 ] != null ) {
            if( !( args[ 3 ] instanceof Boolean ) ) {
                throw new IllegalArgumentException();
            }

            global = ( (Boolean) args[ 3 ] ).booleanValue();
        }

        try {
            // create dialog
            Dialog d = new Dialog( message, choices, values, defaultChoice, null /* bitmap */, global ? Dialog.GLOBAL_STATUS : 0 /* style */);
            _currentDialog = new DialogRunner( d, global );

            // queue
            UiApplication.getUiApplication().invokeAndWait( _currentDialog );

            // return value
            return _currentDialog.getReturnCode();
        } finally {
            _currentDialog = null;
        }
    }

    protected void unblock( int reason ) {
        if( _currentDialog != null ) {
            if( reason == BlockingScriptableFunction.UNBLOCK_TIMEOUT || !_currentDialog.isGlobal() ) {
                _currentDialog.close();
            }
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 4 );
        // message
        fs.addParam( String.class, true );
        // choices
        fs.addParam( Scriptable.class, true );
        // default choice
        fs.addParam( Integer.class, false );
        // global status
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

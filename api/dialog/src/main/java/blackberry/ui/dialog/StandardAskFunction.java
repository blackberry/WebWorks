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

import blackberry.core.BlockingScriptableFunction;
import blackberry.core.FunctionSignature;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.UiApplication;

/**
 * Implementation of standard ask function (blackberry.ui.dialog.standardAsk)
 * 
 * @author dmeng
 * 
 */
public class StandardAskFunction extends BlockingScriptableFunction {

    public static final String NAME = "standardAsk";

    private DialogRunner _currentDialog;

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        int type;
        String message;
        int defaultChoice = -2;
        boolean global = false;

        // type
        type = ( (Integer) args[ 0 ] ).intValue();
        if( type < Dialog.D_OK || type > Dialog.D_OK_CANCEL ) {
            throw new IllegalArgumentException();
        }

        // message
        message = (String) args[ 1 ];

        // default choice
        if( args.length > 2 && args[ 2 ] != null ) {
            if( !( args[ 2 ] instanceof Integer ) ) {
                throw new IllegalArgumentException();
            }

            defaultChoice = ( (Integer) args[ 2 ] ).intValue();
            if( defaultChoice < Dialog.CANCEL || defaultChoice > Dialog.YES ) {
                throw new IllegalArgumentException();
            }
        }

        // global
        if( args.length > 3 && args[ 3 ] != null ) {
            if( !( args[ 3 ] instanceof Boolean ) ) {
                throw new IllegalArgumentException();
            }

            global = ( (Boolean) args[ 3 ] ).booleanValue();
        }

        if( defaultChoice < -1 ) {
            defaultChoice = DialogNamespace.getDefaultChoice( type );
        }

        try {
            // create dialog
            Dialog d = new Dialog( type, message, defaultChoice, null /* bitmap */, global ? Dialog.GLOBAL_STATUS : 0 /* style */);
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
        // type
        fs.addParam( Integer.class, true );
        // message
        fs.addParam( String.class, true );
        // default choice
        fs.addParam( Integer.class, false );
        // global status
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

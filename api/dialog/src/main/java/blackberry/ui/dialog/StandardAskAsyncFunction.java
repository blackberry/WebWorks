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
import net.rim.device.api.ui.component.Dialog;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of asynchronous standard ask function (blackberry.ui.dialog.standardAskAsync)
 * 
 * @author dmateescu
 * 
 */
public class StandardAskAsyncFunction extends ScriptableFunctionBase {

    public static final String NAME = "standardAskAsync";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
    	String message;
    	int type;      
        final boolean global = false;

        // message
        message = (String) args[ 0 ];
        // type
        type = ( (Integer) args[ 1 ] ).intValue();
        if( type < Dialog.D_OK || type > Dialog.D_OK_CANCEL ) {
            throw new IllegalArgumentException();
        }
        // default choice
        final int defaultChoice = DialogNamespace.getDefaultChoice( type );
        //callback function
        ScriptableFunction callback = (ScriptableFunction) args[ 2 ];
        
        Runnable dr = DialogRunnableFactory.getStandardAskRunnable(message, type, defaultChoice, global, callback);
        // queue
        UiApplication.getUiApplication().invokeLater(dr);
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
        // type
        fs.addParam( Integer.class, true );
        // callback
        fs.addParam( ScriptableFunction.class, true );
	// filler
        fs.addParam( Object.class, false );
        return new FunctionSignature[] { fs };
    }
}

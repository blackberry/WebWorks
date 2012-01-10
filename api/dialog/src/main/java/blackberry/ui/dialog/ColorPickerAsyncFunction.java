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
import blackberry.ui.dialog.DialogRunnableFactory;

/**
 * Implementation of asynchronous color picker dialog
 * 
 * @author jachoi
 * 
 */
public class ColorPickerAsyncFunction extends ScriptableFunctionBase {

    public static final String NAME = "colorPickerAsync";
    private final int HEX_BASE = 16;

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        int initialColor = stringToColor( (String) args[ 0 ] );
        ScriptableFunction callback = (ScriptableFunction) args[ 1 ];

        // create dialog
        Runnable dr = DialogRunnableFactory.getColorPickerRunnable( initialColor, callback, thiz );

        // queue
        UiApplication.getUiApplication().invokeLater( dr );

        // return value
        return Scriptable.UNDEFINED;
    }
    
    private int stringToColor(String color) {
        if ( color.startsWith( "#" ) ) {
            return Integer.parseInt( color.substring( 1 ), HEX_BASE );
        }
        return Integer.parseInt( color, HEX_BASE );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        // initialColor
        fs.addParam( String.class, true );
        // callback
        fs.addParam( ScriptableFunction.class, true );

        return new FunctionSignature[] { fs };
    }
}
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

import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;
import blackberry.common.util.json4j.JSONArray;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

import blackberry.ui.dialog.DialogRunnableFactory;
import blackberry.ui.dialog.select.SelectDialog;

/**
 * Implementation of asynchronous selection dialog
 * 
 * @author jachoi
 * 
 */
public class SelectAsyncFunction extends ScriptableFunctionBase {

    public static final String NAME = "selectAsync";

    public static final int POPUP_ITEM_TYPE_OPTION = 0;
    public static final int POPUP_ITEM_TYPE_GROUP = 1;
    public static final int POPUP_ITEM_TYPE_SEPARATOR = 2;

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        boolean allowMultiple = ( (Boolean) args[ 0 ] ).booleanValue();
        Scriptable choices = (Scriptable) args[ 1 ];
        ScriptableFunction callback = (ScriptableFunction) args[ 2 ];

        int numChoices = choices.getElementCount();
        String[] labels = new String[ numChoices ];
        boolean[] enabled = new boolean[ numChoices ];
        boolean[] selected = new boolean[ numChoices ];
        int[] types = new int[ numChoices ];

        populateChoiceStateArrays( choices, labels, enabled, selected, types, allowMultiple );
        
        Runnable dr = DialogRunnableFactory.getSelectRunnable(allowMultiple, labels, enabled, selected, types, callback, thiz);
        
        // queue
        UiApplication.getUiApplication().invokeLater(dr);
        
        // return value
        return Scriptable.UNDEFINED;
    }

    private void populateChoiceStateArrays( Scriptable fromScriptableChoices, String[] labels, boolean[] enabled,
            boolean[] selected, int[] type, boolean allowMultiple ) {
        try {

            boolean firstSelected = false;
            boolean canSelect = true;
            
            for( int i = 0; i < fromScriptableChoices.getElementCount(); i++ ) {
                Scriptable choice = (Scriptable) fromScriptableChoices.getElement( i );
                labels[ i ] = (String) choice.getField( "label" );
                enabled[ i ] = ( (Boolean) choice.getField( "enabled" ) ).booleanValue();
                
                canSelect = allowMultiple || !firstSelected;
                selected[ i ] = canSelect && enabled[ i ] && ( (Boolean) choice.getField( "selected" ) ).booleanValue();
                firstSelected = firstSelected || selected[ i ];
                
                type[ i ] = ( (String) choice.getField( "type" ) ).equals( "group" ) ? POPUP_ITEM_TYPE_GROUP
                        : POPUP_ITEM_TYPE_OPTION;
            }
        } catch( Exception e ) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        // allowMultiple
        fs.addParam( Boolean.class, true );
        // choices
        fs.addParam( Scriptable.class, true );
        // callback
        fs.addParam( ScriptableFunction.class, true );

        return new FunctionSignature[] { fs };
    }
}

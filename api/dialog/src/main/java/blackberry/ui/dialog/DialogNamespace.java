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

import java.util.Hashtable;

import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.ui.component.Dialog;

/**
 * BlackBerry UI Dialog namespace.
 * 
 * @author dmeng
 * 
 */
public class DialogNamespace extends Scriptable {

    private static final String D_OK = "D_OK";
    private static final String D_SAVE = "D_SAVE";
    private static final String D_DELETE = "D_DELETE";
    private static final String D_YES_NO = "D_YES_NO";
    private static final String D_OK_CANCEL = "D_OK_CANCEL";

    private static final String C_CANCEL = "C_CANCEL";
    private static final String C_OK = "C_OK";
    private static final String C_SAVE = "C_SAVE";
    private static final String C_DISCARD = "C_DISCARD";
    private static final String C_DELETE = "C_DELETE";
    private static final String C_YES = "C_YES";
    private static final String C_NO = "C_NO";

    private Hashtable _fields;

    /**
     * Constructs a DialogNamespace object.
     */
    public DialogNamespace() {
        _fields = new Hashtable();

        _fields.put( StandardAskFunction.NAME, new StandardAskFunction() );
        _fields.put( CustomAskFunction.NAME, new CustomAskFunction() );
        _fields.put( SelectAsyncFunction.NAME, new SelectAsyncFunction() );
        _fields.put( DateTimeAsyncFunction.NAME, new DateTimeAsyncFunction() );
        _fields.put( ColorPickerAsyncFunction.NAME, new ColorPickerAsyncFunction() );
        _fields.put( D_OK, new Integer( Dialog.D_OK ) );
        _fields.put( D_SAVE, new Integer( Dialog.D_SAVE ) );
        _fields.put( D_DELETE, new Integer( Dialog.D_DELETE ) );
        _fields.put( D_YES_NO, new Integer( Dialog.D_YES_NO ) );
        _fields.put( D_OK_CANCEL, new Integer( Dialog.D_OK_CANCEL ) );
        _fields.put( C_CANCEL, new Integer( Dialog.CANCEL ) );
        _fields.put( C_OK, new Integer( Dialog.OK ) );
        _fields.put( C_SAVE, new Integer( Dialog.SAVE ) );
        _fields.put( C_DISCARD, new Integer( Dialog.DISCARD ) );
        _fields.put( C_DELETE, new Integer( Dialog.DELETE ) );
        _fields.put( C_YES, new Integer( Dialog.YES ) );
        _fields.put( C_NO, new Integer( Dialog.NO ) );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        Object field = _fields.get( name );
        if( field == null ) {
            return UNDEFINED;
        }
        return field;
    }

    /**
     * Returns default choice for a given dialog type.
     * 
     * @param type
     *            The dialog type
     * @return The default choice
     */
    public static int getDefaultChoice( int type ) {
        switch( type ) {
            case Dialog.D_OK:
                return Dialog.OK;
            case Dialog.D_SAVE:
                return Dialog.SAVE;
            case Dialog.D_DELETE:
                return Dialog.CANCEL;
            case Dialog.D_YES_NO:
                return Dialog.YES;
            case Dialog.D_OK_CANCEL:
                return Dialog.OK;
            default:
                throw new IllegalArgumentException();
        }
    }

}

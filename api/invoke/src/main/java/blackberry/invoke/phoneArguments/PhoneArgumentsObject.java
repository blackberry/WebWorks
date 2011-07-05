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
package blackberry.invoke.phoneArguments;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents the PhoneArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class PhoneArgumentsObject extends ScriptableObjectBase {

    private final String _dialString;
    private final boolean _smartDialing;
    private final int _lineId;

    public static final String FIELD_VIEW = "view";

    /**
     * Constructs a new PhoneArgumentsObject object.
     * 
     * @param dialString
     *            Specifies the Number to dial; this may contain special dialing characters in addition to the components of a
     *            traditional phone number.
     * @param smartDialing
     *            If true, smart dialing will be enabled.
     * @param lineId
     *            Specifies the line to use for the call.
     * 
     */
    public PhoneArgumentsObject( final String dialString, final boolean smartDialing, final int lineId ) {
        _dialString = dialString;
        _smartDialing = smartDialing;
        _lineId = lineId;
        initial();
    }

    // Injects fields and methods
    private void initial() {
        addItem( new ScriptField( FIELD_VIEW, new Integer( PhoneArgumentsConstructor.VIEW_CALL ), ScriptField.TYPE_INT, false,
                false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the PhoneArgumentsObject's underlying content.
     * 
     * @return the type of view when opening Phone application.
     */
    public int getView() {
        final Integer i = (Integer) getItem( FIELD_VIEW ).getValue();
        final int view = i.intValue();
        return view;
    }

    /**
     * Internal helper method to get direct access to the PhoneArgumentsObject's underlying content.
     * 
     * @return the telephone string to dial.
     */
    public String getDialString() {
        return _dialString;
    }

    /**
     * Internal helper method to get direct access to the PhoneArgumentsObject's underlying content.
     * 
     * @return whether smart dialing will be enabled.
     */
    public boolean isSmartDialing() {
        return _smartDialing;
    }

    /**
     * Internal helper method to get direct access to the PhoneArgumentsObject's underlying content.
     * 
     * @return the Line Id.
     */
    public int getLineId() {
        return _lineId;
    }
}

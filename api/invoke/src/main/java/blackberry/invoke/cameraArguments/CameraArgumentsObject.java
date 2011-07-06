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
package blackberry.invoke.cameraArguments;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents the CameraArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class CameraArgumentsObject extends ScriptableObjectBase {

    public static final String FIELD_VIEW = "view";

    /**
     * Default constructor, constructs a new CameraArgumentsObject object.
     */
    public CameraArgumentsObject() {
        initial();
    }

    // Injects fields and methods
    private void initial() {
        addItem( new ScriptField( FIELD_VIEW, new Integer( CameraArgumentsConstructor.VIEW_CAMERA ), ScriptField.TYPE_INT, false,
                false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the CameraArgumentsObject's underlying content.
     * 
     * @return the type of camera application to open.
     */
    public int getView() {
        final Integer i = (Integer) getItem( FIELD_VIEW ).getValue();
        final int view = i.intValue();
        return view;
    }
}

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

import blackberry.core.ScriptableFunctionBase;

/**
 * The CameraArgumentsConstructor class is used to create new CameraArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class CameraArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "CameraArguments";

    public static final int VIEW_CAMERA = 0;
    public static final int VIEW_RECORDER = 1;

    public static final String LABEL_VIEW_CAMERA = "VIEW_CAMERA";
    public static final String LABEL_VIEW_RECORDER = "VIEW_RECORDER";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        return new CameraArgumentsObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_CAMERA ) ) {
            return new Integer( VIEW_CAMERA );
        } else if( name.equals( LABEL_VIEW_RECORDER ) ) {
            return new Integer( VIEW_RECORDER );
        }

        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

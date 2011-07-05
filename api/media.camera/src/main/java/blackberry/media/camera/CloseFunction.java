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
package blackberry.media.camera;

import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EventInjector;
import blackberry.media.ProcessCheckThread;

/**
 * 
 * Implementation of function camera.close()
 * Close the camera or video recorder
 * 
 */
public class CloseFunction extends ScriptableFunction {
    public static final String NAME = "close";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object invoke( Object thiz, Object[] args ) throws Exception {

        if( ProcessCheckThread.isProcessRunning( CameraNamespace.CAMERA_PROCESS )
                || ProcessCheckThread.isProcessRunning( CameraNamespace.VIDEORECORDER_PROCESS ) ) {
            try {
                EventInjector.KeyEvent inject = new EventInjector.KeyEvent( EventInjector.KeyEvent.KEY_DOWN, Characters.ESCAPE, 0 );
                inject.post();
                inject.post();

            } catch( Exception e ) {

            }
        }

        return UNDEFINED;

    }

}

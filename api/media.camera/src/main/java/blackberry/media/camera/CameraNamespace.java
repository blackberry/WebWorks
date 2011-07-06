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

import java.util.Hashtable;

import net.rim.device.api.script.Scriptable;

/**
 * CameraNamespace defines public properties for blackberry.media.camera namespace.
 */
public class CameraNamespace extends Scriptable {

    private Hashtable _functions;
    public static final String NAME = "blackberry.media.camera";
    public static final String CAMERA_PROCESS = "net_rim_bb_camera";
    public static final String VIDEORECORDER_PROCESS = "net_rim_bb_videorecorder";

    /**
     * Default constructor
     */
    public CameraNamespace() {
        _functions = new Hashtable();
        _functions.put( TakePictureFunction.NAME, new TakePictureFunction( this ) );
        _functions.put( TakeVideoFunction.NAME, new TakeVideoFunction( this ) );
        _functions.put( CloseFunction.NAME, new CloseFunction() );

    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {

        if( _functions.containsKey( name ) ) {
            return _functions.get( name );
        }

        return super.getField( name );
    }

}
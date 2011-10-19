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
package blackberry.media.microphone;

import java.util.Hashtable;

import blackberry.core.threading.CallbackDispatcherEvent;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

/**
 * MicrophoneNamespace defines public properties for blackberry.media.microphone namespace.
 */
public class MicrophoneNamespace extends Scriptable {

    private Hashtable _functions;
    public static final String NAME = "blackberry.media.microphone";
    public static final String RECORDER_PROCESS = "net_rim_bb_voicenotesrecorder";

    /**
     * Default constructor
     */
    public MicrophoneNamespace() {

        Record record = new Record();

        _functions = new Hashtable();
        _functions.put( RecordFunction.NAME, new RecordFunction( record ) );
        _functions.put( PauseFunction.NAME, new PauseFunction( record ) );
        _functions.put( StopFunction.NAME, new StopFunction( record ) );
        _functions.put( GetSupportedMediaTypesFunction.NAME, new GetSupportedMediaTypesFunction( record ) );

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

    static void handleError( ScriptableFunction errorCallback, Exception e ) {
        if( errorCallback != null ) {
            new CallbackDispatcherEvent( errorCallback, new Object[] { new Integer(-1), e.getMessage() } ).Dispatch();
        }
    }

}
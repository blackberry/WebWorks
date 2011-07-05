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
package blackberry.push;

import java.util.Hashtable;

import net.rim.device.api.script.Scriptable;

/**
 * Implements the blackberry.push namespace in the BlackBerry Widget API
 */
public class PushNamespace extends Scriptable {

    private static final String FUNCTION_OPEN_PUSH = "openPushListener";
    private static final String FUNCTION_OPEN_BIS_PUSH = "openBISPushListener";
    private static final String FUNCTION_OPEN_BES_PUSH = "openBESPushListener";
    private static final String FUNCTION_CLOSE_PUSH = "closePushListener";

    private Hashtable _functions;

    /**
     * Constructs a PushNamespace Object.
     */
    public PushNamespace() {
        _functions = new Hashtable();
        _functions.put( FUNCTION_OPEN_PUSH, new OpenPushListenerFunction() );
        _functions.put( FUNCTION_CLOSE_PUSH, new ClosePushListenerFunction() );
        _functions.put( FUNCTION_OPEN_BIS_PUSH, new OpenBISPushListenerFunction() );
        _functions.put( FUNCTION_OPEN_BES_PUSH, new OpenBESPushListenerFunction() );
    }

    /**
     * @see Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( _functions.containsKey( name ) )
            return _functions.get( name );

        return UNDEFINED;

    }
}

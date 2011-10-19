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
package blackberry.core.threading;

import net.rim.device.api.script.ScriptableFunction;

/*
 * Invokes a javascript callback with params
 */
public class CallbackDispatcherEvent extends DispatchableEvent {
    ScriptableFunction _callback;
    Object[] _params;

    public CallbackDispatcherEvent( ScriptableFunction callback ) {
        this( null, callback, null );
    }

    public CallbackDispatcherEvent( ScriptableFunction callback, Object[] params ) {
        this( null, callback, params );
    }

    public CallbackDispatcherEvent( Object context, ScriptableFunction callback ) {
        this( context, callback, null );
    }

    public CallbackDispatcherEvent( Object context, ScriptableFunction callback, Object[] params ) {
        super( context );
        _callback = callback;
        _params = params;
    }

    protected void dispatch() {
        try {
            if( _callback != null ) {
                _callback.invoke( null, _params );
            }
        } catch( Exception e ) {
        }
    }

    public void Dispatch() {
        Dispatcher.getInstance().dispatch( this );
    }
}

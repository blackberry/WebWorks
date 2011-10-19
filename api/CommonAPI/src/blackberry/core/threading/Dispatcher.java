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

/**
 * Dispatcher allows client code to dispatch a DispatchableEvent and run in a
 * new thread. One use case is to prevent deadlocks in the situation where code
 * run on the main Java UI thread must call code in the JavaScript engine where
 * both want to lock the UI thread.
 */
final public class Dispatcher {
    
    /**
     * Obtain an instance of the Dispatcher singleton.
     */
    final public static Dispatcher getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Dispatch a DispatchableEvent on a new thread.
     * 
     * @return True if successful. False is the event queue size is exceeded.
     */
    final public boolean dispatch( final DispatchableEvent event ) {
        return DispatcherImpl.getInstance().dispatch( event );
    }

    /**
     * Clear the event queue of pending DispatchableEvent(s).
     * 
     * @param context
     */
    final public void clear( final Object context ) {
        DispatcherImpl.getInstance().clear( context );
    }
    
    // Internal methods
    
    // Enforce singleton. Use 'Initialization on Demand Holder' idiom.
    private Dispatcher() {}
 
    private static class SingletonHolder {
        private static final Dispatcher INSTANCE = new Dispatcher();
    }
    
}

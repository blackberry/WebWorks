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
 * Allows an event to be defined to run on the Dispatch thread. Call
 * Dispatcher.getInstance().dispatch(DispatchableEvent) to run the event.
 */
public abstract class DispatchableEvent {
    
    private Object            _context;
    private DispatchableEvent _next;
    private long              _dispatchTime;
    
    /**
     * Implement dispatch() to provide an event to be run on the Dispatch thread.
     */
    protected abstract void dispatch();

    protected DispatchableEvent( final Object context ) {
        _context = context;
    }
    
    protected boolean merge( final DispatchableEvent other ) {
        return false;
    }

    final boolean hasContext( final Object context ) {
        return (_context == context);
    }

    final void setDispatchTime( long dispatchTime ) {
        _dispatchTime = dispatchTime;
    }

    final long getDispatchTime() {
        return _dispatchTime;
    }

    final DispatchableEvent prependTo( final DispatchableEvent prevHead ) {
        _next = prevHead;
        return this;
    }

    final boolean appendTo( DispatchableEvent prev ) {
        if( prev._next == null || prev._next.getDispatchTime() > _dispatchTime ) {
            _next = prev._next;
            prev._next = this;
            return true;
        }
        return false;
    }

    final DispatchableEvent removeNext() {
        DispatchableEvent next = _next;
        _next = _next._next;
        return next;
    }

    final DispatchableEvent next() {
        return _next;
    }

}

/*
* Copyright 2010 Research In Motion Limited.
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
package blackberry.web.widget.threading;

public final class Dispatcher implements Runnable {
    private static final int MAX_QUEUE_SIZE = 256;
    private static final int DISPATCHER_TIMEOUT = 120000;

    private DispatchableEvent _priority;
    private DispatchableEvent _timed;
    private DispatchableEvent _regular;
    private DispatchableEvent _current;
    private int _queueSize;             // number of events waiting (includes all three lists)
    private InterruptibleThread _thread;
    private int _threadPriority;
    private String _name;
    
    private Dispatcher() {
        // don't allow this constructor
    }
    
    public Dispatcher( String name ) {
        _name = name;
        _threadPriority = Thread.NORM_PRIORITY;
    }

    public synchronized void setPriority( int value ) {
        synchronized (this) {
            _threadPriority = value;
            if (_thread != null) {
                _thread.setPriority( _threadPriority );
            }
        }
    }

    public synchronized boolean dispatch( DispatchableEvent event ) {
        DispatchableEvent newHead = dispatchCommon( _regular, event, -1 );
        if (newHead == null) {
            return false;
        }
        _regular = newHead;
        return true;
    }

    public synchronized boolean dispatchHead( DispatchableEvent event ) {
        DispatchableEvent newHead = dispatchCommon( _priority, event, -1 );
        if (newHead == null) {
            return false;
        }
        _priority = newHead;
        return true;
    }

    public synchronized boolean dispatchAt( DispatchableEvent event, long dispatchTime ) {
        DispatchableEvent newHead = dispatchCommon( _timed, event, dispatchTime );
        if (newHead == null) {
            return false;
        }
        _timed = newHead;
        return true;
    }

    private DispatchableEvent dispatchCommon( DispatchableEvent head, DispatchableEvent event, long dispatchTime ) {
        event.setDispatchTime( dispatchTime );

        // check to see if it can be merged with another event in that list
        for (DispatchableEvent iter = head; iter != null; iter = iter.next()) {
            if (iter == event || iter.merge( event )) {
                return head;
            }
        }

        // check against max queue size
        if (_queueSize >= MAX_QUEUE_SIZE) {
            return null;
        }

        // restart the thread if it's gone dead from idleness
        if (_thread == null) {
            _thread = new InterruptibleThread( this, _name );
            _thread.setPriority( _threadPriority );
            _thread.start();
        }

        if (head == null || head.getDispatchTime() > dispatchTime) {
            head = event.prependTo( head );
        } else {
            for (DispatchableEvent iter = head; iter != null; iter = iter.next()) {
                if (event.appendTo( iter )) {
                    break;
                }
            }
        }

        _queueSize++;
        this.notifyAll();
        return head;
    }

    public synchronized void clear( Object context ) {
        _priority = clear( _priority, context );
        _timed = clear( _timed, context );
        _regular = clear( _regular, context );

        if (_current != null && _current.hasContext( context ) && _thread != null) {
            try {
                _thread.interrupt();
            } catch (IllegalThreadStateException itse) {
                // somehow the thread died. set it to null so that it will get 
                // re-created and recover
                _thread = null;
            }
        }
    }

    private DispatchableEvent clear( DispatchableEvent head, Object context ) {
        while (head != null && head.hasContext( context )) {
            _queueSize--;
            head = head.next();
        }
        if (head == null) {
            return head;
        }
        DispatchableEvent iter = head;
        DispatchableEvent next = iter.next();
        while (next != null) {
            if (next.hasContext( context )) {
                _queueSize--;
                iter.removeNext();
            } else {
                iter = next;
            }
            next = iter.next();
        }

        return head;
    }

    public void run() {
        while (true) {
            synchronized (this) {
                while (_queueSize == 0) {
                    try {
                        wait( DISPATCHER_TIMEOUT );
                        // are we terminating?
                        if (_queueSize == 0) {
                            _thread = null;
                            return;
                        }
                    } catch (InterruptedException ie) {
                        // if it was triggered by a call to interrupt() then
                        // clear the interrupted flag since we want to keep going
                        _thread.reset();
                    }
                }

                // Process queues and get "_current" event
                long waitTime = 0;
                if (_priority != null) {
                    _current = _priority;
                    _priority = _priority.next();
                } else {
                    if (_timed != null) {
                        waitTime = _timed.getDispatchTime() - System.currentTimeMillis();
                        if (waitTime <= 0) {
                            _current = _timed;
                            _timed = _timed.next();
                        }
                    }
                    if (_current == null && _regular != null) {
                        _current = _regular;
                        _regular = _regular.next();
                    }
                }
                if (_current == null) {
                    // no events found, time to wait for the first timed event
                    try {
                        wait( waitTime );
                    } catch (InterruptedException ie) {
                        // if it was triggered by a call to interrupt() then
                        // clear the interrupted flag since we want to keep going
                    }
                    continue;   // loop back to the top to find an event again
                } else {
                    _queueSize--;
                }
            }

            // Process "_current" event
            try {
                _current.dispatch();
            } catch (Interruption in) {
                // clear the interrupted flag
                _thread.reset();
            } catch (Error e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            synchronized (this) {
                _thread.reset();    // in case clear() was called between dispatch() and this
                _current = null;
            }
        }
    }

    public static abstract class DispatchableEvent {
        private Object _context;
        private DispatchableEvent _next;
        private long _dispatchTime;

        protected DispatchableEvent( Object context ) {
            _context = context;
        }

        /*package*/ final boolean hasContext( Object context ) {
            return _context == context;
        }

        /*package*/ final void setDispatchTime( long dispatchTime ) {
            _dispatchTime = dispatchTime;
        }

        /*package*/ final long getDispatchTime() {
            return _dispatchTime;
        }

        /*package*/ final DispatchableEvent prependTo( DispatchableEvent prevHead ) {
            _next = prevHead;
            return this;
        }

        /*package*/ final boolean appendTo( DispatchableEvent prev ) {
            if (prev._next == null || prev._next.getDispatchTime() > _dispatchTime) {
                _next = prev._next;
                prev._next = this;
                return true;
            }
            return false;
        }

        /*package*/ final DispatchableEvent removeNext() {
            DispatchableEvent next = _next;
            _next = _next._next;
            return next;
        }

        /*package*/ final DispatchableEvent next() {
            return _next;
        }

        protected boolean merge( DispatchableEvent other ) {
            return false;
        }

        protected abstract void dispatch();
    }
}

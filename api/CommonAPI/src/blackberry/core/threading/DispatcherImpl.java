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

import blackberry.core.threading.InterruptibleThread.Interruption;

final class DispatcherImpl implements Runnable {
    
    private static final int    MAX_QUEUE_SIZE     = 256;
    private static final int    DISPATCHER_TIMEOUT = 120000;
    private static String       NAME               = "WebWorksDispatcher";

    private DispatchableEvent   _priority;
    private DispatchableEvent   _timed;
    private DispatchableEvent   _regular;
    private DispatchableEvent   _current;
    // Number of events waiting (includes all three lists)
    private int                 _queueSize;
    private InterruptibleThread _thread;
    private int                 _threadPriority;
    
    // Enforce singleton
    private DispatcherImpl() {
        _threadPriority = Thread.NORM_PRIORITY;
    }
 
    private static class SingletonHolder {
        private static final DispatcherImpl INSTANCE = new DispatcherImpl();
    }
 
    static final DispatcherImpl getInstance() {
        // Use 'Initialization on Demand Holder' idiom
        return SingletonHolder.INSTANCE;
    }

    synchronized void setPriority( int value ) {
        synchronized( this ) {
            _threadPriority = value;
            if( _thread != null ) {
                _thread.setPriority( _threadPriority );
            }
        }
    }

    synchronized boolean dispatch( final DispatchableEvent event ) {
        DispatchableEvent newHead = dispatchCommon( _regular, event, -1 );
        if( newHead == null ) {
            return false;
        }
        _regular = newHead;
        return true;
    }

    synchronized boolean dispatchHead( final DispatchableEvent event ) {
        DispatchableEvent newHead = dispatchCommon( _priority, event, -1 );
        if( newHead == null ) {
            return false;
        }
        _priority = newHead;
        return true;
    }

    synchronized boolean dispatchAt( final DispatchableEvent event, long dispatchTime ) {
        DispatchableEvent newHead = dispatchCommon( _timed, event, dispatchTime );
        if( newHead == null ) {
            return false;
        }
        _timed = newHead;
        return true;
    }

    private DispatchableEvent dispatchCommon( DispatchableEvent head,
            DispatchableEvent event, long dispatchTime ) {
        
        event.setDispatchTime( dispatchTime );

        // Check to see if it can be merged with another event in that list
        for ( DispatchableEvent iter = head; iter != null; iter = iter.next() ) {
            if( iter == event || iter.merge( event ) ) {
                return head;
            }
        }

        // Check against max queue size
        if( _queueSize >= MAX_QUEUE_SIZE ) {
            return null;
        }

        // Restart the thread if it's gone dead from idleness
        if( _thread == null ) {
            _thread = new InterruptibleThread( this, NAME );
            _thread.setPriority( _threadPriority );
            _thread.start();
        }

        if( head == null || head.getDispatchTime() > dispatchTime ) {
            head = event.prependTo( head );
        } else {
            for ( DispatchableEvent iter = head; iter != null; iter = iter.next() ) {
                if( event.appendTo( iter ) ) {
                    break;
                }
            }
        }

        _queueSize++;
        this.notifyAll();
        return head;
    }

    /**
     * Remove all DispatchableEvent(s) from the queue.
     */
    synchronized void clear( final Object context ) {
        _priority = clear( _priority, context );
        _timed = clear( _timed, context );
        _regular = clear( _regular, context );

        if( _current != null && _current.hasContext( context ) && _thread != null ) {
            try {
                _thread.interrupt();
            } catch ( IllegalThreadStateException itse ) {
                // The thread died. Set it to null so it will get re-created and recover.
                _thread = null;
            }
        }
    }

    private DispatchableEvent clear( DispatchableEvent head, final Object context ) {
        while ( head != null && head.hasContext( context ) ) {
            _queueSize--;
            head = head.next();
        }
        if( head == null ) {
            return head;
        }
        DispatchableEvent iter = head;
        DispatchableEvent next = iter.next();
        while ( next != null ) {
            if( next.hasContext( context ) ) {
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
        while ( true ) {
            synchronized( this ) {
                while ( _queueSize == 0 ) {
                    try {
                        wait( DISPATCHER_TIMEOUT );
                        // Are we terminating?
                        if( _queueSize == 0 ) {
                            _thread = null;
                            return;
                        }
                    } catch ( InterruptedException ie ) {
                        // If it was triggered by a call to interrupt(), then
                        // clear the interrupted flag since we want to keep going
                        _thread.reset();
                    }
                }

                // Process queues and get "_current" event
                long waitTime = 0;
                if( _priority != null ) {
                    _current = _priority;
                    _priority = _priority.next();
                } else {
                    if( _timed != null ) {
                        waitTime = _timed.getDispatchTime() - System.currentTimeMillis();
                        if( waitTime <= 0 ) {
                            _current = _timed;
                            _timed = _timed.next();
                        }
                    }
                    if( _current == null && _regular != null ) {
                        _current = _regular;
                        _regular = _regular.next();
                    }
                }
                if( _current == null ) {
                    // No events found. Wait for the first timed event.
                    try {
                        wait( waitTime );
                    } catch ( InterruptedException ie ) {
                        // If it was triggered by a call to interrupt(), then
                        // clear the interrupted flag since we want to keep going
                    }
                    continue; // Loop back to the top to find an event again
                } else {
                    _queueSize--;
                }
            }

            // Process "_current" event
            try {
                synchronized( this ) {
                    _current.dispatch();
                }
            } catch ( Interruption in ) {
                synchronized( this ) {
                    // Clear the interrupted flag
                    _thread.reset();
                }
            } catch ( Error e ) {
                e.printStackTrace();
            } catch ( Throwable t ) {
                t.printStackTrace();
            }

            synchronized( this ) {
                // In case clear() was called between dispatch() and this
                _thread.reset();
                _current = null;
            }
        }
    }
    
}

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

package blackberry.system.event;

import java.util.Vector;
import blackberry.system.event.SystemEventReturnValue;

 /** Thread safe queue implementation based on Vector.
 *
 * @author ababut
 */
public class EventQueue {
    private Vector _queue;
    private Object _lock;
    
    EventQueue() {
        _queue = new Vector();
        _lock = new Object();
    }
    
    /**
     * Queues a SystemReturnValue object and signals the lock that there are items 
     * waiting. 
     *
     * @param event the event to queue up
     */
    public void enqueue(SystemEventReturnValue event) {
        synchronized(_lock) {
            _queue.addElement(event);
            _lock.notify();
        }
    }
    
    /**
     * Removes first SystemReturnValue object in queue. If queue is empty, wait
     * on lock until signalled.
     *
     * @param event the event to queue up
     */
    public SystemEventReturnValue dequeueWaitIfEmpty() {
        SystemEventReturnValue result = null;
        
        if(_queue.isEmpty()) {
            try {
                synchronized(_lock) {
                    _lock.wait();
                }
            } catch(InterruptedException e) {
                System.out.println("InterrupedException while waiting on event queue");
                throw new RuntimeException("Polling thread interrupted while waiting.");
            }
            
        }
        
        synchronized(_lock) {
            result = (SystemEventReturnValue)_queue.elementAt(0);
            _queue.removeElementAt(0);
        }
        
        return result;
    }
}

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

/** Manages registering/deregistering and consumption of event objects.
 *
 * This class will register and queue up system events. It will pass itself to
 * system event handlers to be notified when a system event has occurred. The
 * event is transformed to a WebWorks text-based return object and queued in a
 * blocking queue. 
 *
 * @author ababut
 */
public class SystemEventManager implements ISystemEventListener {
    //The blocking event queue
    private EventQueue _eventQueue;
    
    //System event handlers
    private CoverageChangeHandler _coverageHandler = null;
    private KeyPressHandler _keyHandler = null;
    
    public SystemEventManager() {
        _eventQueue = new EventQueue();
        _coverageHandler = new CoverageChangeHandler(this);
        _keyHandler = new KeyPressHandler(this);
    }
    
    /**
     * Validates and registers the requested event. 
     *
     * @param event the event to listen for
     * @param arg optional argument to be passed to the event handler
     */
    public void listenFor(String event, String arg) {
        if(EVENT_COV_CHANGE.equals(event)) {
            _coverageHandler.listen();
        } else if(EVENT_HARDWARE_KEY.equals(event)) {
            if(arg == null || arg.length() == 0) {
                throw new IllegalArgumentException("Expected [key] argument to register listener.");
            }
            
            _keyHandler.listen(arg);
        } else {
            throw new IllegalArgumentException("Unable to register unknown event [" + event + "]");
        }
    }
    
    /**
     * Validates and unregisters the requested event. 
     *
     * @param event the event to listen for
     * @param arg optional argument to be passed to the event handler
     */
    public void stopListeningFor(String event, String arg) {
        //Signal appropriate event handler to stop listening
        if(EVENT_COV_CHANGE.equals(event)) {
            _coverageHandler.stopListening();
        } else if(EVENT_HARDWARE_KEY.equals(event)) {
            if(arg == null || arg.length() == 0) {
                throw new IllegalArgumentException("Expected [key] argument to unregister listener.");
            }
            
            _keyHandler.stopListening(arg);
        } else {
            throw new IllegalArgumentException("Unable to unregister unknown event [" + event + "]");
        }
        
        //If there are no more active listeners, queue up a channel closed message to
        //close any lingering poll requests
        if(!hasActiveListeners()) {
            _eventQueue.enqueue(SystemEventReturnValue.CHANNEL_CLOSED);
        }
    }
    
    /**
     * Checks if any handlers are listening to system events and unregisters them. 
     */
    public void shutDown() {
        if(_coverageHandler.isListening()) {
            _coverageHandler.stopListening();
        }
        
        if(_keyHandler.isListening()) {
            _keyHandler.stopListeningToAll();
        }
    }
    
    private boolean hasActiveListeners() {
        return _coverageHandler.isListening() || _keyHandler.isListening();
    }
    
    /**
     * Blocking call that returns next event queued.
     * 
     * @return a return value object representing a queued system event
     */
    public SystemEventReturnValue getNextWaitingEvent() {
        return _eventQueue.dequeueWaitIfEmpty();
    }
    
    /**
     * Called by system event handlers to queue up a system event that has occurred.
     * The event is converted into a return value object.
     * 
     * @param eventName name of event
     * @param eventArg optional argument describing the event
     */
    public void onSystemEvent(String eventName, String eventArg) {
        _eventQueue.enqueue(SystemEventReturnValue.getReturnValueForEvent(eventName, eventArg));
    }
}

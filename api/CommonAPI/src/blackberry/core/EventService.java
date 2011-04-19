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
package blackberry.core;

import java.util.Enumeration;

import net.rim.device.api.util.IntMultiMap;

/**
 * Service that propagates events to subscribers.
 */
public final class EventService {

    private static EventService _instance;
    private IntMultiMap _callbackLookup;

    public static EventService getInstance() {
        if( _instance == null ) {
            _instance = new EventService();
        }
        return _instance;
    }

    /**
     * Constructor
     */
    public EventService() {
        _callbackLookup = new IntMultiMap();
    }

    /**
     * Triggers an event to all subscribers.
     * 
     * @param eventID
     *            the event id to trigger to all subscribers.
     * @param args
     *            the arguments to pass to each subscribers.
     * @param isConsumable
     *            indicates if the event fired can be consumed so that default handling is ignored
     * 
     * @return true if default handling of the event fired should be ignored; false otherwise, if isConsumable is false, return
     *         value is ignored
     */
    public boolean fireEvent( int eventID, Object[] args, boolean isConsumable ) {
        boolean result = false;
        for( Enumeration e = _callbackLookup.elements( eventID ); e.hasMoreElements(); ) {
            ApplicationEventHandler ae = (ApplicationEventHandler) e.nextElement();
            if( isConsumable ) {
                result = result || ae.handlePreEvent( eventID, args );
            } else {
                ae.handleEvent( eventID, args );
            }
        }
        return result;
    }

    /**
     * Subscribes to the specified event, with the given callback.
     * 
     * @param eventID
     *            The event id of the events to subscribe to.
     * @param callback
     *            The callback to invoke when this event is triggered.
     * 
     */
    public void addHandler( int eventID, ApplicationEventHandler callback ) {
        _callbackLookup.add( eventID, callback );
    }

    /**
     * Unsubscribes the callback object for only the event ID the callback is subscribed to
     * 
     * @param callback
     *            the callback object to unsubscribe
     * 
     */
    public void removeHandler( int eventID, ApplicationEventHandler callback ) {
        _callbackLookup.removeValue( eventID, callback );
    }

    /**
     * Unsubscribes the callback object for all events it subscribed to.
     * 
     * @param callback
     *            the callback object to unsubscribe
     * 
     */
    public void removeHandler( ApplicationEventHandler callback ) {
        _callbackLookup.removeValue( callback );
    }
}

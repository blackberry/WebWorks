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

public interface ApplicationEventHandler {
    /**
     * Event identifier for application exit.<br>
     */
    public static final int EVT_APP_EXIT = 100;

    /**
     * Event identifier for application returning to foreground, this event cannot be consumed, handlePreEvent is not possible
     */
    public static final int EVT_APP_FOREGROUND = 101;

    /**
     * Event identifier for application returning to background, this event cannot be consumed, handlePreEvent is not possible
     */
    public static final int EVT_APP_BACKGROUND = 102;

    /**
     * Called when an event is about to happen, but the event can be 'consumed' so that default behaviour is ignored by returning
     * true. Furthermore, even if a handler returns false, another handler may return true to cancel default behaviour - returning
     * false does not guarantee that default behaviour will occur.
     * 
     * For example, if EVT_APP_EXIT is fired, and handlePreEvent() returns true, it is up to the handler to close the application
     * since default behaviour is ignored.
     * 
     * @param eventID
     *            the type of event being fired
     * @param args
     *            any arguments to be passed to the event handler
     * 
     * @return true if the event was consumed; false otherwise
     */
    public boolean handlePreEvent( int eventID, Object[] args );

    /**
     * Called when an event is imminent, and no event handler can prevent the default behaviour from occuring
     * 
     * @param eventID
     *            the type of event being fired
     * @param args
     *            any arguments to be passed to the event handler
     */
    public void handleEvent( int eventID, Object[] args );
}

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
package blackberry.web.widget.bf.navigationcontroller;

import blackberry.core.threading.DispatchableEvent;

/**
 * NavigationUiDispatcherEvent
 */
class NavigationUiDispatcherEvent extends DispatchableEvent {

    private final NavigationController _navigationController;
    private int _eventType;
    private int _direction;
    private int _delta;

    NavigationUiDispatcherEvent( final NavigationController navigationController, final NavigationController context,
            int eventType, int direction, int delta ) {
        super( context );
        _navigationController = navigationController;
        _eventType = eventType;
        _direction = direction;
        _delta = delta;
    }

    // << DispatchableEvent >>
    protected void dispatch() {
        switch( _eventType ) {
            case NavigationController.NAVIGATION_EVENT_DIRECTION:
                handleDirection( _direction, _delta );
                break;
            case NavigationController.NAVIGATION_EVENT_CLICK:
                _navigationController.triggerNavigationMouseDown();
                break;
            case NavigationController.NAVIGATION_EVENT_UNCLICK:
                _navigationController.triggerNavigationMouseUp();
                break;
            case NavigationController.NAVIGATION_EVENT_INITFOCUS:
                break;
            default:
                throw new Error( "Invalid event type: " + _eventType );
        }
    }

    // Call back from NavigationUiDispatcherEvent
    private void handleDirection( int direction, int delta ) {
        _navigationController.triggerNavigationDirection( direction, delta );
    }
}
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

import java.util.Hashtable;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.system.Application;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import blackberry.core.threading.Dispatcher;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.bf.NavigationNamespace;
import blackberry.web.widget.bf.WidgetFieldManager;

/**
 * NavigationController
 */
final public class NavigationController {

    // Constants
    public static final int FOCUS_NAVIGATION_UNDEFINED = -1;
    public static final int FOCUS_NAVIGATION_RIGHT = 0;
    public static final int FOCUS_NAVIGATION_LEFT = 1;
    public static final int FOCUS_NAVIGATION_UP = 2;
    public static final int FOCUS_NAVIGATION_DOWN = 3;

    public static final String RIM_FOCUSABLE = "x-blackberry-focusable";
    public static final String RIM_FOCUSED = "x-blackberry-focused";
    public static final String INITIAL_FOCUS = "x-blackberry-initialFocus";
    public static final String DEFAULT_HOVER_EFFECT = "x-blackberry-defaultHoverEffect";

    public static final int NAVIGATION_EVENT_DIRECTION = 0;
    public static final int NAVIGATION_EVENT_CLICK = 1;
    public static final int NAVIGATION_EVENT_UNCLICK = 2;
    public static final int NAVIGATION_EVENT_INITFOCUS = 3;

    // Cached references to BrowserFieldScreen components /*package*/
    BrowserField _browserField;
    NavigationNamespace _navigationNamespace;
    WidgetFieldManager _widgetFieldManager;
    Hashtable _iframeHashtable;

    /* Creates a new NavigationController. */
    public NavigationController( BrowserFieldScreen widgetScreen ) {
        _browserField = widgetScreen.getWidgetBrowserField();
        _navigationNamespace = widgetScreen.getNavigationExtension();
        _widgetFieldManager = widgetScreen.getWidgetFieldManager();
        _iframeHashtable = new Hashtable();
    }

    public void reset() {
        clearEventQueue();
    }

    public void clearEventQueue() {
        synchronized( Application.getEventLock() ) {
            Dispatcher.getInstance().clear( this );
        }
    }

    /* Handles the navigation movement based on direction */
    public void handleDirection( int direction, int delta ) {
        dispatchUiEvent( NAVIGATION_EVENT_DIRECTION, direction, delta );
    }

    public void handleClick() {
        dispatchUiEvent( NAVIGATION_EVENT_CLICK, FOCUS_NAVIGATION_UNDEFINED, 0 ); // TODO: [RT] find out how to get delta
    }

    public void handleUnclick() {
        dispatchUiEvent( NAVIGATION_EVENT_UNCLICK, FOCUS_NAVIGATION_UNDEFINED, 0 ); // TODO: [RT] find out how to get delta
    }

    public void handleInitFocus() {
        dispatchUiEvent( NAVIGATION_EVENT_INITFOCUS, FOCUS_NAVIGATION_UNDEFINED, 0 ); // TODO: [RT] find out how to get delta
    }

    public void setIFrameHashtable( Hashtable newHash ) {
        _iframeHashtable = newHash;
    }

    public Hashtable getIFrameHashtable() {
        return _iframeHashtable;
    }

    /**
     * Deselects the node with the current navigation focus
     */
    public void deselectFocusedNode() {
        _navigationNamespace.triggerNavigationFocusOut();
    }

    // Internal methods...

    private boolean dispatchUiEvent( int eventType, int direction, int delta ) {
        return Dispatcher.getInstance().dispatch( new NavigationUiDispatcherEvent( this, this, eventType, direction, delta ) );
    }

    // Utility functions...

    String getNamedAttibute( Node node, String name ) {
        if( node == null )
            return null;
        NamedNodeMap nnm = node.getAttributes();
        if( nnm != null ) {
            Node att = nnm.getNamedItem( name );
            if( att instanceof Attr )
                return ( (Attr) att ).getValue();
        }
        return null;
    }

    boolean isFocusableDisabled( Node node ) {
        /*
         * Optimized for OS 6.0 NamedNodeMap nnm = node.getAttributes(); if (nnm != null) { Node att =
         * nnm.getNamedItem(RIM_FOCUSABLE); if ((att instanceof Attr) && ((Attr)att).getValue().equals("false")){ return true; } }
         */
        return false;
    }

    void triggerNavigationDirection( int direction, int delta ) {
        _navigationNamespace.triggerNavigationDirection( direction, delta );
    }

    void triggerNavigationMouseDown() {
        _navigationNamespace.triggerNavigationMouseDown();
    }

    void triggerNavigationMouseUp() {
        _navigationNamespace.triggerNavigationMouseUp();
    }
}

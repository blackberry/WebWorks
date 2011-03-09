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
import java.util.Vector;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.XYRect;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html2.HTMLButtonElement;
import org.w3c.dom.html2.HTMLInputElement;
import org.w3c.dom.html2.HTMLObjectElement;
import org.w3c.dom.html2.HTMLSelectElement;
import org.w3c.dom.html2.HTMLTextAreaElement;

import blackberry.core.threading.Dispatcher;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.bf.NavigationNamespace;
import blackberry.web.widget.bf.WidgetFieldManager;
import blackberry.web.widget.device.DeviceInfo;

/**
 * NavigationController
 */
final public class NavigationController {

    // Constants
    public static final int     FOCUS_NAVIGATION_UNDEFINED = -1;
    public static final int     FOCUS_NAVIGATION_RIGHT     = 0;
    public static final int     FOCUS_NAVIGATION_LEFT      = 1;
    public static final int     FOCUS_NAVIGATION_UP        = 2;
    public static final int     FOCUS_NAVIGATION_DOWN      = 3;

    public static final String  RIM_FOCUSABLE              = "x-blackberry-focusable";
    public static final String  RIM_FOCUSED                = "x-blackberry-focused";
    public static final String  INITIAL_FOCUS              = "x-blackberry-initialFocus";
    public static final String  DEFAULT_HOVER_EFFECT       = "x-blackberry-defaultHoverEffect";

    public static final int     NAVIGATION_EVENT_DIRECTION = 0;
    public static final int     NAVIGATION_EVENT_CLICK     = 1;
    public static final int     NAVIGATION_EVENT_UNCLICK   = 2;

    // Cached references to BrowserFieldScreen components /*package*/
    BrowserField               _browserField;
    NavigationNamespace        _navigationNamespace;
    WidgetFieldManager         _widgetFieldManager;

    // Members /*package*/
    Node                       _currentFocusNode;
    boolean                    _currentNodeHovered;
    boolean                    _currentNodeFocused;
    Document                   _dom;
    Vector                     _focusableNodes;
    private boolean            _pageLoaded;
    boolean                    _defaultHoverEffect;
    Hashtable				   _iframeHashtable;

    /* Creates a new NavigationController. */
    public NavigationController( BrowserFieldScreen widgetScreen ) {
        _browserField = widgetScreen.getWidgetBrowserField();
        _navigationNamespace = widgetScreen.getNavigationExtension();
        _widgetFieldManager = widgetScreen.getWidgetFieldManager();
        _currentNodeHovered = true;
        _defaultHoverEffect = true;
        _iframeHashtable = new Hashtable();
    }

    public void reset() {
        clearEventQueue();
        _dom = null;
        _currentFocusNode = null;
        _currentNodeHovered = true;
        _currentNodeFocused = false;
        _focusableNodes = null;
        _pageLoaded = false;
        _defaultHoverEffect = true;
    }
    
    public void clearEventQueue() {
        synchronized( Application.getEventLock() ) {
            Dispatcher.getInstance().clear( this );
        }
    }

    public void update() {
        if( !_pageLoaded ) {
            _pageLoaded = true;
            Dispatcher.getInstance().dispatch( 
                    new NavigationMapUpdateDispatcherEvent( this, this, true ) );
        } else {
            if( _currentFocusNode != null ) {
                if( !isValidFocusableNode( _currentFocusNode ) ) {
                    _currentFocusNode = null;
                } else {
                    _currentNodeHovered = _browserField.setHover( _currentFocusNode, true );
                    // Scroll to the current focus node
                    _widgetFieldManager.scrollToNode( _currentFocusNode );
                }
            }
            Dispatcher.getInstance().dispatch( 
                    new NavigationMapUpdateDispatcherEvent( this, this, false ) );
        }
    }
    
    public void setRimFocus( String id ) {
        if( id.length() == 0 ) {
            focusOut();
            return;
        }
        Node nextFocusNode = null;
        nextFocusNode = _dom.getElementById( id );
        if( nextFocusNode != null ) {
            if( !isValidFocusableNode( nextFocusNode ) ) {
                nextFocusNode = null;
            }
        }
        if( nextFocusNode != null ) {
            setFocus( nextFocusNode );
        }
    }

    public Node getCurrentFocusNode() {
        return _currentFocusNode;
    }

    public boolean requiresDefaultHover() {
        return ( _currentFocusNode != null && !_currentNodeHovered && _defaultHoverEffect );
    }

    public boolean requiresDefaultNavigation() {
        return ( _currentFocusNode != null && _currentNodeFocused && requiresNavigation( _currentFocusNode ) );
    }
    
    /* Handles the navigation movement based on direction */
    public void handleDirection( int direction ) {
        dispatchUiEvent( NAVIGATION_EVENT_DIRECTION, direction );
    }
    
    public void handleClick() {
        dispatchUiEvent( NAVIGATION_EVENT_CLICK, FOCUS_NAVIGATION_UNDEFINED );
    }
    
    public void handleUnclick() {
        dispatchUiEvent( NAVIGATION_EVENT_UNCLICK, FOCUS_NAVIGATION_UNDEFINED );
    }
    
    public void setIFrameHashtable( Hashtable newHash ){
    	_iframeHashtable = newHash;
    }
    
    public Hashtable getIFrameHashtable(){
    	return _iframeHashtable;
    }
    
    /**
     * Deselects the node with the current navigation focus
     */
    public void deselectFocusedNode(){
    	focusOut();
    }
    
    // Internal methods...

    private boolean dispatchUiEvent( int eventType, int direction ) {
        if( _dom == null )
            return false;
        return Dispatcher.getInstance().dispatch( 
                new NavigationUiDispatcherEvent( this, this, eventType, direction ) );
    }
    
    void setFocus( Node node ) {
        if( node == null )
            return;

        focusOut();

        // Focus in...
        _currentFocusNode = node;

        String id = getNamedAttibute( node, "id" );
        _navigationNamespace.setNewFocusedId( id );

        // Create a synthetic mouse over Event
        fireMouseEvent( "mouseover", node );

        // Call BF setHover
        _currentNodeHovered = _browserField.setHover( node, true );

        if( isAutoFocus( node ) ) {
            _currentNodeFocused = _browserField.setFocus( node, true );
        }

        // Scroll to the current focus node
        _widgetFieldManager.scrollToNode( node );
        _widgetFieldManager.invalidateNode( node );
    }

    void focusOut() {
        if( _currentFocusNode != null ) {
            String id = getNamedAttibute( _currentFocusNode, "id" );
            _navigationNamespace.setOldFocusedId( id );

            // Disable BF focus
            if( _currentNodeFocused ) {
                _browserField.setFocus( _currentFocusNode, false );
                _currentNodeFocused = false;
            }

            // Disable BF hover
            _browserField.setHover( _currentFocusNode, false );

            // Create a synthetic mouseout Event
            fireMouseEvent( "mouseout", _currentFocusNode );

            // Invalidate the area of old _currentFocusNode
            _widgetFieldManager.invalidateNode( _currentFocusNode );

            _currentFocusNode = null;
            _navigationNamespace.setNewFocusedId( null );
        }
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

    private static final String SUPPRESS_NAVIGATION_INPUT_TYPES = "|checkbox|radio|button|";
    private static final String AUTO_FOCUS_INPUT_TYPES = "|color|date|month|time|week|email|number|password|search|text|url|";
    private static final String REQUIRE_CLICK_INPUT_TYPES = "|file|";

    private boolean isAutoFocus( Node node ) {
        if( node instanceof HTMLInputElement ) {
            String type = ( (HTMLInputElement) node ).getType();
            return AUTO_FOCUS_INPUT_TYPES.indexOf( type ) > 0;
        }
        if( node instanceof HTMLSelectElement ) {
			if ( DeviceInfo.isBlackBerry6() )
        		// WebKit will autofocus the select element
        		return false;
        	else{
        		HTMLSelectElement select = ( HTMLSelectElement ) node;
        		return !select.getMultiple();
        	}
        }
        return ( node instanceof HTMLTextAreaElement );
    }

    private boolean requiresNavigation( Node node ) {
        if( node instanceof HTMLInputElement ) {
            String type = ( (HTMLInputElement) node ).getType();
            return ( SUPPRESS_NAVIGATION_INPUT_TYPES.indexOf( type ) < 0 );
        } 
        if( node instanceof HTMLSelectElement )
            return true;
        if( node instanceof HTMLTextAreaElement )
            return true;
        if( node instanceof HTMLButtonElement )
            return true;
        return ( node instanceof HTMLObjectElement );
    }

    boolean fireMouseEvent( String type, Node node ) {
        if( node == null )
            return false;
        DocumentEvent domEvent = (DocumentEvent) _dom;
        Event mouseEvent = domEvent.createEvent( "MouseEvents" );
        mouseEvent.initEvent( type, true, true );
        ( (EventTarget) node ).dispatchEvent( mouseEvent );
        return true;
    }

    boolean isValidFocusableNode( Node node ) {
        if( node == null )
            return false;
        XYRect nodeRect = _widgetFieldManager.getPosition( node );
        return !( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0 );
    }    
    
    /**
     * Determines if the current control is a special case that requires a click event in WebKit.
     * @param node <description>
     * @return <description>
     */
    boolean currentNodeRequiresClickInWebKit(){
    	if( _currentFocusNode == null ){
    		return false;
    	}    	
        if( DeviceInfo.isBlackBerry6() ){
            if( _currentFocusNode instanceof HTMLInputElement ){
                String type = ( ( HTMLInputElement ) _currentFocusNode ).getType();
                return REQUIRE_CLICK_INPUT_TYPES.indexOf( type ) > 0;
            }
        }        
        return false;
    }
}

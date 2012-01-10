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
package blackberry.web.widget.bf;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.container.VerticalFieldManager;
import blackberry.web.widget.bf.navigationcontroller.NavigationController;

/**
 * 
 */
public class WidgetFieldManager extends VerticalFieldManager {

    /* Creates a new WidgetFieldManager. */
    public WidgetFieldManager() {
        this( 0 );
    }

    /* Creates a new WidgetFieldManager with a style. */
    public WidgetFieldManager( long style ) {
        super( style );
    }

    private BrowserFieldScreen getBrowserFieldScreen() {
        Screen bfScreen = getScreen();

        // Get the screen object.
        if( bfScreen instanceof BrowserFieldScreen ) {
            return (BrowserFieldScreen) bfScreen;
        }

        return null;
    }

    /* override */public boolean navigationMovement( int dx, int dy, int status, int time ) {
        if( getBrowserFieldScreen().getAppNavigationMode() ) {
            if( dx == 0 && dy == 0 )
                return true;

            boolean handled = super.navigationMovement( dx, dy, status, time );
            if( handled ) {
                return true;
            }

            // Handle the directional event.
            int direction = -1;
            int delta = 0;
            if( Math.abs( dx ) >= Math.abs( dy ) ) {
                if( dx > 0 ) {
                    direction = NavigationController.FOCUS_NAVIGATION_RIGHT;
                } else {
                    direction = NavigationController.FOCUS_NAVIGATION_LEFT;
                }
                delta = dx;
            } else {
                if( dy > 0 ) {
                    direction = NavigationController.FOCUS_NAVIGATION_DOWN;
                } else {
                    direction = NavigationController.FOCUS_NAVIGATION_UP;
                }
                delta = dy;
            }

            try {
                getBrowserFieldScreen().getNavigationController().handleDirection( direction, delta );
            } catch( Exception e ) {
            }

            return true;
        }

        return super.navigationMovement( dx, dy, status, time );
    }

    protected boolean navigationClick( int status, int time ) {
        if( getBrowserFieldScreen().getAppNavigationMode() ) {
            // TODO: [RT] figure out what to do with this
//            if( getBrowserFieldScreen().getNavigationController().requiresDefaultNavigation() ) {
//                super.navigationClick( status, time );
//            }

            try {
                getBrowserFieldScreen().getNavigationController().handleClick();
            } catch( Exception e ) {
            }

            return true;
        }

        return super.navigationClick( status, time );
    }

    protected boolean navigationUnclick( int status, int time ) {
        if( getBrowserFieldScreen().getAppNavigationMode() ) {
            // TODO: [RT] figure out what to do with this
//            if( getBrowserFieldScreen().getNavigationController().requiresDefaultNavigation() ) {
//                super.navigationUnclick( status, time );
//            }

            try {
                getBrowserFieldScreen().getNavigationController().handleUnclick();
            } catch( Exception e ) {
            }

            return true;
        }

        return super.navigationUnclick( status, time );
    }

    // TODO: [RT] Confirm with Tim that it is ok to lose default hover
//    /* override */public void paint( Graphics graphics ) {
//        super.paint( graphics );
//
//        // Paint current node if it exists, is not focused, and does not have a hover style.
//        if( getBrowserFieldScreen().getAppNavigationMode() ) {
//            if( getBrowserFieldScreen().getNavigationController().requiresDefaultHover() ) {
//                Node currentNode = getBrowserFieldScreen().getNavigationController().getCurrentFocusNode();
//                if( currentNode != null ) {
//                    XYRect position = getPosition( currentNode );
//                    if( position != null ) {
//                        position = scaleRect( position );
//                        int oldColor = graphics.getColor();
//                        graphics.setColor( 0xBBDDFF );
//                        graphics.drawRoundRect( position.x - 1, position.y - 1, position.width + 2, position.height + 2, 4, 4 );
//                        graphics.setColor( 0x88AAFF );
//                        graphics.drawRoundRect( position.x, position.y, position.width, position.height, 4, 4 );
//                        graphics.setColor( oldColor );
//                    }
//                }
//            }
//        }
//    }

//    public void invalidateNode( Node node ) {
//        if( node == null )
//            return;
//        XYRect position = getPosition( node );
//        if( position == null )
//            return;
//
//        position = scaleRect( position );
//        invalidate( position.x - 1, position.y - 1, position.width + 2, position.height + 2 );
//    }
//
//    public void scrollToNode( Node node ) {
//        if( node == null )
//            return;
//        XYRect position = getPosition( node );
//        if( position == null )
//            return;
//
//        position = scaleRect( position );
//        scrollToRect( position );
//    }
//
//    public void scrollDown() {
//        int newVerticalScroll = Math.min( getVerticalScroll() + scaleValue( SAFE_MARGIN ), getVirtualHeight() - getHeight() );
//        setVerticalScroll( newVerticalScroll );
//    }
//
//    public void scrollUp() {
//        int newVerticalScroll = Math.max( getVerticalScroll() - scaleValue( SAFE_MARGIN ), 0 );
//        setVerticalScroll( newVerticalScroll );
//    }
//
//    public void scrollRight() {
//        int newHorizontalScroll = Math.min( getHorizontalScroll() + scaleValue( SAFE_MARGIN ), getVirtualWidth() - getWidth() );
//        setHorizontalScroll( newHorizontalScroll );
//    }
//
//    public void scrollLeft() {
//        int newHorizontalScroll = Math.max( getHorizontalScroll() - scaleValue( SAFE_MARGIN ), 0 );
//        setHorizontalScroll( newHorizontalScroll );
//    }
//
//    public static final int SAFE_MARGIN = 30;
//
//    private void scrollToRect( XYRect rect ) {
//        // Check vertical scroll.
//        int verticalScroll = getVerticalScroll();
//        int newVerticalScroll = verticalScroll;
//
//        if( rect.y < verticalScroll ) {
//            newVerticalScroll = Math.max( rect.y - scaleValue( SAFE_MARGIN ), 0 );
//        } else if( rect.y + rect.height > verticalScroll + getHeight() ) {
//            newVerticalScroll = Math.min( rect.y + rect.height - getHeight() + scaleValue( SAFE_MARGIN ), getVirtualHeight()
//                    - getHeight() );
//        }
//
//        if( newVerticalScroll - verticalScroll != 0 ) {
//            setVerticalScroll( newVerticalScroll );
//        }
//
//        // Check horizontal scroll.
//        int horizontalScroll = getHorizontalScroll();
//        int newHorizontalScroll = horizontalScroll;
//
//        if( rect.width >= getWidth() ) {
//            newHorizontalScroll = Math.max( rect.x, 0 );
//        } else if( rect.x < horizontalScroll ) {
//            newHorizontalScroll = Math.max( rect.x - scaleValue( SAFE_MARGIN ), 0 );
//        } else if( rect.x + rect.width > horizontalScroll + getWidth() ) {
//            newHorizontalScroll = Math.min( rect.x + rect.width - getWidth() + scaleValue( SAFE_MARGIN ), getVirtualWidth()
//                    - getWidth() );
//        }
//
//        if( newHorizontalScroll - horizontalScroll != 0 ) {
//            setHorizontalScroll( newHorizontalScroll );
//        }
//    }
//
//    private int scaleValue( int value ) {
//        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
//        float scale = bf.getZoomScale();
//        return MathUtilities.round( value * scale );
//    }
//
//    private XYRect scaleRect( XYRect rect ) {
//        return new XYRect( scaleValue( rect.x ), scaleValue( rect.y ), scaleValue( rect.width ), scaleValue( rect.height ) );
//    }
//
//    public int unscaleValue( int value ) {
//        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
//        float scale = bf.getZoomScale();
//        return MathUtilities.round( value / scale );
//    }
//
//    public XYRect unscaleRect( XYRect rect ) {
//        return new XYRect( unscaleValue( rect.x ), unscaleValue( rect.y ), unscaleValue( rect.width ), unscaleValue( rect.height ) );
//    }
//
//    public XYRect getPosition( Node node ) {
//        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
//        XYRect nodeRect = bf.getNodePosition( node );
//       
//        // Check for iframe parent and adjust the coordinates if found        
//        HTMLIFrameElement iframeRect = getIFrameForNode( node );
//        if( iframeRect != null && nodeRect != null ){
//            nodeRect.x = nodeRect.x + bf.getNodePosition( iframeRect ).x;
//            nodeRect.y = nodeRect.y + bf.getNodePosition( iframeRect ).y;
//        }          
//        
//        return nodeRect;
//    }
    
    /**
	* Overrides the touch event handler.  
	* Deselects the currently focused node of navigation mode on 
	* Touch Down event.  Does not consume the event.
	*/
    protected boolean touchEvent( TouchEvent message ){
    	super.touchEvent( message );    	
    
    	// Check for the Touch down event
        if( message.getEvent() == TouchEvent.DOWN ){
        	
        	// Check if navigation mode is turned on 
        	if( getBrowserFieldScreen().getAppNavigationMode() ){
        		NavigationController navControl = 
        			getBrowserFieldScreen().getNavigationController();
        		// Deselect currently focused node
        		if( navControl != null ){
        			navControl.deselectFocusedNode();
        		}
        	}
        }    	
		return false;
    }    
        
//    /**
//     * Get the iframe parent of the specified node.
//     * Returns null if the node does not have an iframe parent.
//     * @param node
//     * @return HTMLIFrameElement parent of the specified node
//     */
//    private HTMLIFrameElement getIFrameForNode( Node node ){
//        Hashtable iframeHashtable = getBrowserFieldScreen().getNavigationController().getIFrameHashtable();
//        HTMLIFrameElement iframe = null;
//        Object potentialIframe = iframeHashtable.get( node );
//        if( potentialIframe instanceof HTMLIFrameElement ){
//        	iframe = ( HTMLIFrameElement ) potentialIframe;
//        }
//    	return iframe;
//   }
    
    public float getZoomScale() {
        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
        return bf.getZoomScale();
    }
}

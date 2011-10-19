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

import net.rim.device.api.ui.XYRect;

import org.w3c.dom.Node;

import blackberry.core.threading.DispatchableEvent;
import blackberry.web.widget.bf.WidgetFieldManager;

/**
 * NavigationUiDispatcherEvent
 */
class NavigationUiDispatcherEvent extends DispatchableEvent {
    
    private final NavigationController _navigationController;
    private int                        _eventType;
    private int                        _direction;

    NavigationUiDispatcherEvent( final NavigationController navigationController, 
            final NavigationController context, int eventType, int direction ) {
        super( context );
        _navigationController = navigationController;
        _eventType = eventType;
        _direction = direction;
    }

    // << DispatchableEvent >>
    protected void dispatch() {
        switch ( _eventType ) {
            case NavigationController.NAVIGATION_EVENT_DIRECTION:
                handleDirection( _direction );
                break;
            case NavigationController.NAVIGATION_EVENT_CLICK:
                if( _navigationController._currentFocusNode == null )
                    return;
                if( !_navigationController._currentNodeFocused ) {
                    // Add current focus
                if( _navigationController._currentFocusNode != null && !_navigationController._currentNodeFocused ) {
                    _navigationController._currentNodeFocused = _navigationController._browserField.setFocus( _navigationController._currentFocusNode, true );
                    _navigationController._widgetFieldManager.invalidateNode( _navigationController._currentFocusNode );
                }
                    _navigationController.fireMouseEvent( "mousedown", _navigationController._currentFocusNode );
                }
                break;
            case NavigationController.NAVIGATION_EVENT_UNCLICK:
                if( _navigationController._currentFocusNode == null )
                    return;
                if( !_navigationController._currentNodeFocused || _navigationController.currentNodeRequiresClickInWebKit()) {
                    _navigationController.fireMouseEvent( "mouseup", _navigationController._currentFocusNode );
                    _navigationController.fireMouseEvent( "click", _navigationController._currentFocusNode );
                }
                break;
            case NavigationController.NAVIGATION_EVENT_INITFOCUS:
                Node nd = findHighestFocusableNodeInScreen();
                if (nd != null) {
                    _navigationController.setFocus(nd);
                }
                break;
            default:
                throw new Error("Invalid event type: " + _eventType);
        }
    }
    
    // Call back from NavigationUiDispatcherEvent
    private void handleDirection( int direction ) {
        
        if (direction < NavigationController.FOCUS_NAVIGATION_UNDEFINED || direction > NavigationController.FOCUS_NAVIGATION_DOWN)
            throw new Error("Invalid direction: " + direction);
        
        _navigationController._navigationNamespace.setDirection( direction );
        String attributeValue = null;
        
        if( _navigationController._currentFocusNode != null ) {
            switch( direction ) {
                case NavigationController.FOCUS_NAVIGATION_RIGHT:
                    attributeValue = _navigationController.getNamedAttibute( _navigationController._currentFocusNode, "x-blackberry-onRight" );
                    break;
                case NavigationController.FOCUS_NAVIGATION_LEFT:
                    attributeValue = _navigationController.getNamedAttibute( _navigationController._currentFocusNode, "x-blackberry-onLeft" );
                    break;
                case NavigationController.FOCUS_NAVIGATION_UP:
                    attributeValue = _navigationController.getNamedAttibute( _navigationController._currentFocusNode, "x-blackberry-onUp" );
                    break;
                case NavigationController.FOCUS_NAVIGATION_DOWN:
                    attributeValue = _navigationController.getNamedAttibute( _navigationController._currentFocusNode, "x-blackberry-onDown" );
                    break;
                default:
                    throw new Error("Invalid direction: " + direction);
            }
        }

        if( attributeValue != null && attributeValue.length() > 2 ) {
            _navigationController._browserField.executeScript( attributeValue );
            return;
        }

        XYRect screenRect = getUnscaledScreenRect();
        
        switch ( direction ) {
            case NavigationController.FOCUS_NAVIGATION_DOWN:
                handleDirectionDown( screenRect );
                break;
            case NavigationController.FOCUS_NAVIGATION_UP:
                handleDirectionUp( screenRect );
                break;
            case NavigationController.FOCUS_NAVIGATION_RIGHT:
                handleDirectionRight( screenRect );
                break;
            case NavigationController.FOCUS_NAVIGATION_LEFT:
                handleDirectionLeft( screenRect );
                break;
        }

        if( _navigationController._currentFocusNode != null ) {
            _navigationController._currentNodeHovered = _navigationController._browserField.setHover( _navigationController._currentFocusNode, true );
        }
    }   
    
    // Support method for handleDirection(int)
    private void handleDirectionDown( final XYRect screenRect ) {
        Node node = findDownFocusableNode();
        if( node != null ) {
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( nodeRect.y <= ( screenRect.y + screenRect.height + WidgetFieldManager.SAFE_MARGIN ) ) {
                _navigationController.setFocus( node );
                return;
            }
        }
        // Only scroll down the screen when there is more content underneath
        int screenVerticalDelta = _navigationController._widgetFieldManager.unscaleValue( _navigationController._widgetFieldManager
                .getVirtualHeight() ) - screenRect.y - screenRect.height;
        if( screenVerticalDelta > WidgetFieldManager.SAFE_MARGIN ) {
            screenVerticalDelta = WidgetFieldManager.SAFE_MARGIN;
        }
        if( screenVerticalDelta > 0 ) {
            if( _navigationController._currentFocusNode != null ) {
                // If current focused node is out of screen, focus out
                XYRect currentNodeRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
                if( currentNodeRect.y + currentNodeRect.height <= screenRect.y
                        + screenVerticalDelta ) {
                    _navigationController.focusOut();
                }
            }
            _navigationController._widgetFieldManager.scrollDown();
        }
    }
    
    // Support method for handleDirection(int)
    private void handleDirectionUp( final XYRect screenRect ) {
        Node node = findUpFocusableNode();
        if( node != null ) {
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( ( nodeRect.y + nodeRect.height ) > ( screenRect.y - WidgetFieldManager.SAFE_MARGIN ) ) {
                _navigationController.setFocus( node );
                return;
            }
        }
        // Only scroll down the screen when there is more content underneath
        int screenVerticalDelta = screenRect.y;
        if( screenVerticalDelta > WidgetFieldManager.SAFE_MARGIN ) {
            screenVerticalDelta = WidgetFieldManager.SAFE_MARGIN;
        }
        if( screenVerticalDelta > 0 ) {
            if( _navigationController._currentFocusNode != null ) {
                // If current focused node is out of screen, focus out.
                XYRect currentNodeRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
                if( currentNodeRect.y > screenRect.y - screenVerticalDelta + screenRect.height ) {
                    _navigationController.focusOut();
                }
            }
            _navigationController._widgetFieldManager.scrollUp();
        } 
    }
    
    // Support method for handleDirection(int)
    private void handleDirectionRight( final XYRect screenRect ) {
        Node node = findRightFocusableNode();
        if( node != null ) {
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( nodeRect.x <= ( screenRect.x + screenRect.width + WidgetFieldManager.SAFE_MARGIN ) ) {
                _navigationController.setFocus( node );
                return;
            }
        }
        // Only scroll down the screen when there is more content underneath.
        int screenHorizontalDelta = _navigationController._widgetFieldManager.unscaleValue( _navigationController._widgetFieldManager.getVirtualWidth() )
                - screenRect.x - screenRect.width;
        if( screenHorizontalDelta > WidgetFieldManager.SAFE_MARGIN ) {
            screenHorizontalDelta = WidgetFieldManager.SAFE_MARGIN;
        }
        if( screenHorizontalDelta > 0 ) {
            if( _navigationController._currentFocusNode != null ) {
                // If current focused node is out of screen, focus out.
                XYRect currentNodeRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
                if( currentNodeRect.x + currentNodeRect.width <= screenRect.x + screenHorizontalDelta ) {
                    _navigationController.focusOut();
                }
            }
            _navigationController._widgetFieldManager.scrollRight();
        }
    }
    
    // Support method for handleDirection(int)
    private void handleDirectionLeft( final XYRect screenRect ) {
        Node node = findLeftFocusableNode();
        if( node != null ) {
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( ( nodeRect.x + nodeRect.width ) > ( screenRect.x - WidgetFieldManager.SAFE_MARGIN ) ) {
                _navigationController.setFocus( node );
                return;
            }
        }
        // Only scroll down the screen when there is more content underneath.
        int screenHorizontalDelta = screenRect.x;
        if( screenHorizontalDelta > WidgetFieldManager.SAFE_MARGIN ) {
            screenHorizontalDelta = WidgetFieldManager.SAFE_MARGIN;
        }
        if( screenHorizontalDelta > 0 ) {
            if( _navigationController._currentFocusNode != null ) {
                // If current focused node is out of screen, focus out.
                XYRect currentNodeRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
                if( currentNodeRect.x > screenRect.x - screenHorizontalDelta + screenRect.width ) {
                    _navigationController.focusOut();
                }
            }
            _navigationController._widgetFieldManager.scrollLeft();
        }
    }
    
    private Node findDownFocusableNode() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        if( _navigationController._currentFocusNode == null || _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode ) == -1 )
            return findHighestFocusableNodeInScreen();

        int index = _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode );
        XYRect currentRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
        Node downNode = null;
        XYRect downRect = null;
        XYRect screenRect = getUnscaledScreenRect();

        for( int i = 0; i < _navigationController._focusableNodes.size(); i++ ) {
            if( i == index ) {
                continue;
            }

            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }

            if( isRectIntersectingVertically( nodeRect, currentRect ) ) {
                boolean swap = false;
                if( nodeRect.y == currentRect.y ) {
                    if( nodeRect.height == currentRect.height ) {
                        if( i > index ) {
                            return node;
                        }
                    } else if( nodeRect.height > currentRect.height ) {
                        swap = needSwapWithDownRectInPriority( downRect, nodeRect );
                    }
                } else if( nodeRect.y > currentRect.y ) {
                    swap = needSwapWithDownRectInPriority( downRect, nodeRect );
                }
                if( swap ) {
                    downNode = node;
                    downRect = nodeRect;
                }
            } else if( !isRectIntersectingHorizontally( nodeRect, currentRect )
                    && isRectIntersectingVertically( nodeRect, screenRect ) ) {
                boolean swap = false;
                if( nodeRect.y > currentRect.y ) {
                    swap = needSwapWithDownRect( downRect, nodeRect );
                }
                if( swap ) {
                    downNode = node;
                    downRect = nodeRect;
                }
            }
        }
        return downNode;
    }
    
    private Node findUpFocusableNode() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        if( _navigationController._currentFocusNode == null || _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode ) == -1 )
            return findLowestFocusableNodeInScreen();

        int index = _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode );
        XYRect currentRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
        Node upNode = null;
        XYRect upRect = null;
        XYRect screenRect = getUnscaledScreenRect();
        for( int i = _navigationController._focusableNodes.size() - 1; i >= 0; i-- ) {
            if( i == index ) {
                continue;
            }

            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }

            if( isRectIntersectingVertically( nodeRect, currentRect ) ) {
                boolean swap = false;
                if( nodeRect.y == currentRect.y ) {
                    if( nodeRect.height == currentRect.height ) {
                        if( i < index ) {
                            return node;
                        }
                    } else if( nodeRect.height < currentRect.height ) {
                        swap = needSwapWithUpRectInPriority( upRect, nodeRect );
                    }
                } else if( nodeRect.y < currentRect.y ) {
                    swap = needSwapWithUpRectInPriority( upRect, nodeRect );
                }
                if( swap ) {
                    upNode = node;
                    upRect = nodeRect;
                }
            } else if( !isRectIntersectingHorizontally( nodeRect, currentRect )
                    && isRectIntersectingVertically( nodeRect, screenRect ) ) {
                boolean swap = false;
                if( nodeRect.y < currentRect.y ) {
                    swap = needSwapWithUpRect( upRect, nodeRect );
                }
                if( swap ) {
                    upNode = node;
                    upRect = nodeRect;
                }
            }
        }
        return upNode;
    }
    
    private Node findLowestFocusableNodeInScreen() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        XYRect screenRect = getUnscaledScreenRect();
        Node firstNode = null;
        XYRect firstRect = null;

        for( int i = _navigationController._focusableNodes.size() - 1; i >= 0; i-- ) {
            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }
            if( isRectIntersectingVertically( nodeRect, screenRect ) ) {
                boolean swap = false;
                // Should select the lowest item in the screen that completely fits on screen
                if( nodeRect.y + nodeRect.height < screenRect.y + screenRect.height ) {
                    swap = needSwapWithUpRect( firstRect, nodeRect );
                }
                if( swap ) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    }
    
    private Node findRightFocusableNode() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        if( _navigationController._currentFocusNode == null || _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode ) == -1 )
            return findRightestFocusableNodeInScreen();

        int index = _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode );
        XYRect currentRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
        Node rightNode = null;
        XYRect rightRect = null;

        for( int i = 0; i < _navigationController._focusableNodes.size(); i++ ) {
            if( i == index ) {
                continue;
            }

            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }

            if( isRectIntersectingHorizontally( nodeRect, currentRect ) ) {
                boolean swap = false;
                if( nodeRect.x == currentRect.x ) {
                    if( nodeRect.width == currentRect.width ) {
                        if( i > index ) {
                            return node;
                        }
                    } else if( nodeRect.width > currentRect.width ) {
                        if( rightNode == null ) {
                            swap = true;
                        } else {
                            if( nodeRect.x == rightRect.x ) {
                                if( nodeRect.width < rightRect.width ) {
                                    swap = true;
                                }
                            } else if( nodeRect.x < rightRect.x ) {
                                swap = true;
                            }
                        }
                    }
                } else if( nodeRect.x > currentRect.x ) {
                    if( rightNode == null ) {
                        swap = true;
                    } else {
                        if( nodeRect.x == rightRect.x ) {
                            if( nodeRect.width < rightRect.width ) {
                                swap = true;
                            }
                        } else if( nodeRect.x < rightRect.x ) {
                            swap = true;
                        }
                    }
                }
                if( swap ) {
                    rightNode = node;
                    rightRect = nodeRect;
                }
            }
        }

        return rightNode;
    }
    
    private Node findRightestFocusableNodeInScreen() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        XYRect screenRect = getUnscaledScreenRect();
        Node firstNode = null;
        XYRect firstRect = null;

        for( int i = 0; i < _navigationController._focusableNodes.size(); i++ ) {
            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }
            if( isRectIntersectingHorizontally( nodeRect, screenRect ) ) {
                boolean swap = false;

                if( nodeRect.x >= screenRect.x ) {
                    if( firstNode == null ) {
                        swap = true;
                    } else {
                        if( nodeRect.x == firstRect.x ) {
                            if( nodeRect.width < firstRect.width ) {
                                swap = true;
                            }
                        } else if( nodeRect.x < firstRect.x ) {
                            swap = true;
                        }
                    }
                }
                if( swap ) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    }
    
    private Node findLeftFocusableNode() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        if( _navigationController._currentFocusNode == null || _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode ) == -1 )
            return findLeftestFocusableNodeInScreen();

        int index = _navigationController._focusableNodes.indexOf( _navigationController._currentFocusNode );

        XYRect currentRect = _navigationController._widgetFieldManager.getPosition( _navigationController._currentFocusNode );
        Node leftNode = null;
        XYRect leftRect = null;

        for( int i = _navigationController._focusableNodes.size() - 1; i >= 0; i-- ) {
            if( i == index ) {
                continue;
            }

            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }

            if( isRectIntersectingHorizontally( nodeRect, currentRect ) ) {
                boolean swap = false;
                if( nodeRect.x == currentRect.x ) {
                    if( nodeRect.width == currentRect.width ) {
                        if( i < index ) {
                            return node;
                        }
                    } else if( nodeRect.width < currentRect.width ) {
                        if( leftNode == null ) {
                            swap = true;
                        } else {
                            if( nodeRect.x == leftRect.x ) {
                                if( nodeRect.width > leftRect.width ) {
                                    swap = true;
                                }
                            } else if( nodeRect.x > leftRect.x ) {
                                swap = true;
                            }
                        }
                    }
                } else if( nodeRect.x < currentRect.x ) {
                    if( leftNode == null ) {
                        swap = true;
                    } else {
                        if( nodeRect.x == leftRect.x ) {
                            if( nodeRect.width > leftRect.width ) {
                                swap = true;
                            }
                        } else if( nodeRect.x > leftRect.x ) {
                            swap = true;
                        }
                    }
                }
                if( swap ) {
                    leftNode = node;
                    leftRect = nodeRect;
                }
            }
        }
        return leftNode;
    }
    
    private Node findLeftestFocusableNodeInScreen() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        XYRect screenRect = getUnscaledScreenRect();
        Node firstNode = null;
        XYRect firstRect = null;

        for( int i = _navigationController._focusableNodes.size() - 1; i >= 0; i-- ) {
            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0 ) {
                continue;
            }
            if( _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }
            if( isRectIntersectingHorizontally( nodeRect, screenRect ) ) {
                boolean swap = false;

                if( nodeRect.x < screenRect.x + screenRect.width ) {
                    if( firstNode == null ) {
                        swap = true;
                    } else {
                        if( nodeRect.x == firstRect.x ) {
                            if( nodeRect.width > firstRect.width ) {
                                swap = true;
                            }
                        } else if( nodeRect.x > firstRect.x ) {
                            swap = true;
                        }
                    }
                }
                if( swap ) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    }
    
    private Node findHighestFocusableNodeInScreen() {
        
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 )
            return null;

        XYRect screenRect = getUnscaledScreenRect();
        Node firstNode = null;
        XYRect firstRect = null;

        for( int i = 0; i < _navigationController._focusableNodes.size(); i++ ) {
            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0
                    || _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }
            if( isRectIntersectingVertically( nodeRect, screenRect ) ) {
                boolean swap = false;
                if( nodeRect.y >= screenRect.y ) {
                    swap = needSwapWithDownRect( firstRect, nodeRect );
                }
                if( swap ) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    }
    
    private boolean isRectIntersectingVertically( final XYRect rect1, final XYRect rect2 ) {
        if( rect1 == null || rect2 == null )
            return false;
        if( rect2.x <= rect1.x && ( rect2.x + rect2.width - 1 ) >= rect1.x )
            return true;
        return ( rect2.x >= rect1.x && rect2.x <= ( rect1.x + rect1.width - 1 ) );
    }

    private boolean isRectIntersectingHorizontally( final XYRect rect1, final XYRect rect2 ) {
        if( rect1 == null || rect2 == null )
            return false;
        if( rect2.y <= rect1.y && ( rect2.y + rect2.height - 1 ) >= rect1.y )
            return true;
        return ( rect2.y >= rect1.y && rect2.y <= ( rect1.y + rect1.height - 1 ) );
    }
    
    private boolean needSwapWithUpRect( final XYRect upRect, final XYRect checkedRect ) {
        if( upRect == null )
            return true;
        if( checkedRect.y == upRect.y && checkedRect.height > upRect.height )
            return true;
        return ( checkedRect.y > upRect.y );
    }
    
    private boolean needSwapWithUpRectInPriority( final XYRect upRect, final XYRect checkedRect ) {
        if( upRect == null )
            return true;
        if( checkedRect.y == upRect.y && checkedRect.height >= upRect.height )
            return true;
        return ( checkedRect.y > upRect.y );
    }

    private boolean needSwapWithDownRect( final XYRect downRect, final XYRect checkedRect ) {
        if( downRect == null )
            return true;
        if( checkedRect.y == downRect.y && checkedRect.height < downRect.height )
            return true;
        return ( checkedRect.y < downRect.y );
    }

    private boolean needSwapWithDownRectInPriority( final XYRect downRect, final XYRect checkedRect ) {
        if( downRect == null )
            return true;
        if( checkedRect.y == downRect.y && checkedRect.height <= downRect.height )
            return true;
        return ( checkedRect.y < downRect.y );
    }
    
    private XYRect getUnscaledScreenRect() {
        XYRect screenRect = new XYRect( _navigationController._widgetFieldManager.getHorizontalScroll(), 
                                        _navigationController._widgetFieldManager.getVerticalScroll(), 
                                        _navigationController._widgetFieldManager.getWidth(), 
                                        _navigationController._widgetFieldManager.getHeight() );
        return _navigationController._widgetFieldManager.unscaleRect( screenRect );
    }        
    
}
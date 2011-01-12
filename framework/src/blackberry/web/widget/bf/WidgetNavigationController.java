/*
 * WidgetNavigationController.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.bf;

import net.rim.device.api.browser.field2.BrowserField;

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.*;
import org.w3c.dom.events.*;
import org.w3c.dom.html2.*;

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Field;

import net.rim.device.api.browser.field2.BrowserField;

import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.bf.NavigationNamespace;
import blackberry.web.widget.bf.WidgetFieldManager;

import java.util.Vector;

import blackberry.web.widget.threading.Dispatcher;

/**
 * 
 */
public class WidgetNavigationController {
    
    // Constants
    public static final int FOCUS_NAVIGATION_RIGHT  =   0;
    public static final int FOCUS_NAVIGATION_LEFT   =   1;
    public static final int FOCUS_NAVIGATION_UP     =   2;
    public static final int FOCUS_NAVIGATION_DOWN   =   3;
    
    public static final String RIM_FOCUSABLE        =   "x-blackberry-focusable";
    public static final String RIM_FOCUSED          =   "x-blackberry-focused";
    public static final String INITIAL_FOCUS        =   "x-blackberry-initialFocus";
    public static final String DEFAULT_HOVER_EFFECT =   "x-blackberry-defaultHoverEffect";
    
    public static final int NAVIGATION_EVENT_DIRECTION =   0;
    public static final int NAVIGATION_EVENT_CLICK     =   1;
    public static final int NAVIGATION_EVENT_UNCLICK   =   2;
    
    // Members
    private BrowserFieldScreen _widgetScreen;
    
    private Node _currentFocusNode;
    private boolean _currentNodeHovered;
    private boolean _currentNodeFocused;
    
    private Document _dom;
    private TreeWalker _treeWalker;
    
    private Vector _focusableNodes;
    
    private boolean _pageLoaded;
    private boolean _defaultHoverEffect;
    
    private boolean _allowInaccurateVerticallyMove;
    
    /* Creates a new WidgetNavigationController */
    public WidgetNavigationController(BrowserFieldScreen widgetScreen) {
        _widgetScreen = widgetScreen;
        _allowInaccurateVerticallyMove = true;
        
        initializeMembers();
    }
    
    private BrowserField getBrowserField() {
        return _widgetScreen.getWidgetBrowserField();
    }
    
    private NavigationNamespace getNavigationNamespace() {
        return _widgetScreen.getNavigationExtension();
    }
    
    private Dispatcher getUiDispatcher() {
        return _widgetScreen.getUiEventDispatcher();
    }
    
    public void reset() {
        synchronized(Application.getEventLock()) {
            getUiDispatcher().clear(this);
            initializeMembers();
        }
    }
    
    private void initializeMembers() {
        _dom = null;
        _treeWalker = null;

        _currentFocusNode = null;
        _currentNodeHovered = true;
        _currentNodeFocused = false;        
        
        _focusableNodes = null;
        
        _pageLoaded = false;
        _defaultHoverEffect = true;
    }
    
    public void update() {
        //System.out.println("WIDGET==> enter update()");
        if (!_pageLoaded) {
            _pageLoaded = true;
            getUiDispatcher().dispatch( new NavigationMapUpdateDispatcherEvent(this, true) );
        } else {
            if (_currentFocusNode != null) {
                if (!isValidFocusableNode(_currentFocusNode)) {
                    _currentFocusNode = null;
                } else {
                    _currentNodeHovered = getBrowserField().setHover(_currentFocusNode, true);
                    scrollToNode(_currentFocusNode);
                }
            }
            getUiDispatcher().dispatch( new NavigationMapUpdateDispatcherEvent(this, false) );
        }
        //System.out.println("WIDGET==> leave update()");
    }    
    
    private void internalCreateNavigationMap() {
        _dom = getBrowserField().getDocument();
        _focusableNodes = populateFocusableNodes(true);
        _defaultHoverEffect = isDefaultHoverEffectEnabled(_dom);
        setFirstFocus();
    }
    
    private void internalUpdateNavigationMap() {
        _dom = getBrowserField().getDocument();
        _focusableNodes = populateFocusableNodes(false);

        if (_currentFocusNode != null) {
            if (!isValidFocusableNode(_currentFocusNode)) {
                _currentFocusNode = null;
            }
        }        
    }
    
    private void setFirstFocus() {
        Node firstFocusNode = findInitialFocusNode();

        if (firstFocusNode == null) {
            firstFocusNode = findHighestFocusableNodeInScreen();
        }
        
        if(firstFocusNode != null){
            setFocus(firstFocusNode);
        }
    }
    
    public void setRimFocus(String id) {
        if (id.length() == 0) {
                focusOut();
                return;
        }
        
        Node nextFocusNode = null;
        nextFocusNode = getNamedNode(id);
        if (nextFocusNode != null ) {
            if (!isValidFocusableNode(nextFocusNode)) {
                nextFocusNode = null;
            }
        }

        if (nextFocusNode != null) {
            setFocus(nextFocusNode);
        }
    }
        
    public Node getCurrentFocusNode(){
        return _currentFocusNode;
    }
    
    public boolean requiresDefaultHover() {
        return (_currentFocusNode != null && _currentNodeHovered == false && _defaultHoverEffect);
    }
    
    public boolean requiresDefaultNavigation() {
        return (_currentFocusNode != null && _currentNodeFocused && requiresNavigation(_currentFocusNode) );
    }
    
    private Node findHighestFocusableNodeInScreen() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
        
        XYRect screenRect = getUnscaledScreenRect();
        
        Node firstNode = null;
        XYRect firstRect = null;
        
        for (int i = 0; i < _focusableNodes.size(); i++) {
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }

            if (isRectIntersectingVertically(nodeRect, screenRect)) {
                boolean swap = false;
                if (nodeRect.y >= screenRect.y) {
                    swap = needSwapWithDownRect(firstRect, nodeRect);
                }
                
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;       
                }
            }
        }
        
        return firstNode;
    }
    
    private Node findLowestFocusableNodeInScreen() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }

        XYRect screenRect = getUnscaledScreenRect();
        
        Node firstNode = null;
        XYRect firstRect = null;
        
        for (int i = _focusableNodes.size() - 1; i >= 0 ; i--) {
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }
            
            if (isRectIntersectingVertically(nodeRect, screenRect)) {
                boolean swap = false;
                if (nodeRect.y < screenRect.y) {
                    swap = needSwapWithUpRect(firstRect, nodeRect);
                }
                
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;       
                }
            }
        }
        
        return firstNode;
    }
    
    private Node findRightestFocusableNodeInScreen() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }

        XYRect screenRect = getUnscaledScreenRect();
        
        Node firstNode = null;
        XYRect firstRect = null;
        
        for (int i = 0; i < _focusableNodes.size(); i++) {
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (isFocusableDisabled(node)) {
                continue;
            }
            
            if (isRectIntersectingHorizontally(nodeRect, screenRect)) {
                boolean swap = false;
                
                if (nodeRect.x >= screenRect.x) {
                    if (firstNode == null) {
                        swap = true;
                    } else {
                        if (nodeRect.x == firstRect.x) {
                            if (nodeRect.width < firstRect.width) {
                                swap = true;
                            }
                        } else if (nodeRect.x < firstRect.x) {
                            swap = true;
                        }
                    }                    
                }                
                
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;       
                }
            }
        }
        
        return firstNode;
    }

    private Node findLeftestFocusableNodeInScreen() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
        
        XYRect screenRect = getUnscaledScreenRect();
        
        Node firstNode = null;
        XYRect firstRect = null;
        
        for (int i = _focusableNodes.size() - 1; i >= 0 ; i--) {
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }            

            if (isRectIntersectingHorizontally(nodeRect, screenRect)) {
                boolean swap = false;
                
                if (nodeRect.x < screenRect.x + screenRect.width) {
                    if (firstNode == null) {
                        swap = true;
                    } else {
                        if (nodeRect.x == firstRect.x) {
                            if (nodeRect.width > firstRect.width) {
                                swap = true;
                            }
                        } else if (nodeRect.x > firstRect.x) {
                            swap = true;
                        }
                    }                    
                }                
                
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;       
                }
            }
        }
        
        return firstNode;
    }
    
    private static boolean needSwapWithDownRect(XYRect downRect, XYRect checkedRect) {
        boolean swap = false;

        if (downRect == null) {
            swap = true;
        } else {
            if (checkedRect.y == downRect.y) {
                if (checkedRect.height < downRect.height) {
                    swap = true;
                }
            } else if (checkedRect.y < downRect.y) {
                swap = true;
            }
        }
        
        return swap;
    }
    
    private static boolean needSwapWithDownRectInPriority(XYRect downRect, XYRect checkedRect) {
        boolean swap = false;

        if (downRect == null) {
            swap = true;
        } else {
            if (checkedRect.y == downRect.y) {
                if (checkedRect.height <= downRect.height) {
                    swap = true;
                }
            } else if (checkedRect.y < downRect.y) {
                swap = true;
            }
        }
        
        return swap;
    }
    
    private Node findDownFocusableNode() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
            
        if (_currentFocusNode == null || _focusableNodes.indexOf(_currentFocusNode) == -1) {
            return findHighestFocusableNodeInScreen();
        }
        
        int index = _focusableNodes.indexOf(_currentFocusNode);
                
        XYRect currentRect = getPosition(_currentFocusNode);
        Node downNode = null;
        XYRect downRect = null;
        XYRect screenRect = getUnscaledScreenRect();
        
        for (int i = 0; i < _focusableNodes.size(); i++) {
            if (i == index) {
                continue;
            }
            
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null) {
                continue;
            }
            
            if (nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (isFocusableDisabled(node)) {
                continue;
            }

            if (isRectIntersectingVertically(nodeRect, currentRect)) {
                boolean swap = false;
                if (nodeRect.y == currentRect.y) {
                    if (nodeRect.height == currentRect.height) {
                        if (i > index) {
                            return node;
                        }
                    } else if (nodeRect.height > currentRect.height) {
                        swap = needSwapWithDownRectInPriority(downRect, nodeRect);
                    }
                } else if (nodeRect.y > currentRect.y) {
                    swap = needSwapWithDownRectInPriority(downRect, nodeRect);
                }
                
                if (swap) {
                    downNode = node;
                    downRect = nodeRect;       
                }
            } else if (!isRectIntersectingHorizontally(nodeRect, currentRect) && isRectIntersectingVertically(nodeRect, screenRect)) {
                boolean swap = false;
                if (nodeRect.y > currentRect.y) {
                    swap = needSwapWithDownRect(downRect, nodeRect);
                }
                
                if (swap) {
                    downNode = node;
                    downRect = nodeRect;       
                }
            }
        }
        
        return downNode;
    }

    private static boolean needSwapWithUpRect(XYRect upRect, XYRect checkedRect) {
        boolean swap = false;

        if (upRect == null) {
            swap = true;
        } else {
            if (checkedRect.y == upRect.y) {
                if (checkedRect.height > upRect.height) {
                    swap = true;
                }
            } else if (checkedRect.y > upRect.y) {
                swap = true;
            }
        }
        
        return swap;
    }
    
    private static boolean needSwapWithUpRectInPriority(XYRect upRect, XYRect checkedRect) {
        boolean swap = false;

        if (upRect == null) {
            swap = true;
        } else {
            if (checkedRect.y == upRect.y) {
                if (checkedRect.height >= upRect.height) {
                    swap = true;
                }
            } else if (checkedRect.y > upRect.y) {
                swap = true;
            }
        }
        
        return swap;
    }
        
     private Node findUpFocusableNode() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
            
        if (_currentFocusNode == null || _focusableNodes.indexOf(_currentFocusNode) == -1) {
            return findLowestFocusableNodeInScreen();
        }
        
        int index = _focusableNodes.indexOf(_currentFocusNode);
                
        XYRect currentRect = getPosition(_currentFocusNode);
        Node upNode = null;
        XYRect upRect = null;
        XYRect screenRect = getUnscaledScreenRect();
        for (int i = _focusableNodes.size() - 1; i >= 0 ; i--) {
            if (i == index) {
                continue;
            }
            
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null) {
                continue;
            }

            if (nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }
                        
            if (isRectIntersectingVertically(nodeRect, currentRect)) {
                boolean swap = false;
                if (nodeRect.y == currentRect.y) {
                    if (nodeRect.height == currentRect.height) {
                        if (i < index) {
                            return node;
                        }
                    } else if (nodeRect.height < currentRect.height) {
                        swap = needSwapWithUpRectInPriority(upRect, nodeRect);
                    }
                } else if (nodeRect.y < currentRect.y) {
                    swap = needSwapWithUpRectInPriority(upRect, nodeRect);
                }
                
                if (swap) {
                    upNode = node;
                    upRect = nodeRect;       
                }
            } else if (!isRectIntersectingHorizontally(nodeRect, currentRect) && isRectIntersectingVertically(nodeRect, screenRect)) {
                boolean swap = false;
                if (nodeRect.y < currentRect.y) {
                    swap = needSwapWithUpRect(upRect, nodeRect);
                }
                
                if (swap) {
                    upNode = node;
                    upRect = nodeRect;       
                }
            }
        }
        
        return upNode;
    }

    private Node findRightFocusableNode() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
            
        if (_currentFocusNode == null || _focusableNodes.indexOf(_currentFocusNode) == -1) {
            return findRightestFocusableNodeInScreen();
        }
        
        int index = _focusableNodes.indexOf(_currentFocusNode);
                
        XYRect currentRect = getPosition(_currentFocusNode);
        Node rightNode = null;
        XYRect rightRect = null;
        
        for (int i = 0; i < _focusableNodes.size(); i++) {
            if (i == index) {
                continue;
            }
            
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null) {
                continue;
            }

            if (nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }
                        
            if (isRectIntersectingHorizontally(nodeRect, currentRect)) {
                boolean swap = false;
                if (nodeRect.x == currentRect.x) {
                    if (nodeRect.width == currentRect.width) {
                        if (i > index) {
                            return node;
                        }
                    } else if (nodeRect.width > currentRect.width) {
                        if (rightNode == null) {
                            swap = true;
                        } else {
                            if (nodeRect.x == rightRect.x) {
                                if (nodeRect.width < rightRect.width) {
                                    swap = true;
                                }
                            } else if (nodeRect.x < rightRect.x) {
                                swap = true;
                            }
                        }    
                    }
                } else if (nodeRect.x > currentRect.x) {
                    if (rightNode == null) {
                        swap = true;
                    } else {
                        if (nodeRect.x == rightRect.x) {
                            if (nodeRect.width < rightRect.width) {
                                swap = true;
                            }
                        } else if (nodeRect.x < rightRect.x) {
                            swap = true;
                        }
                    }                    
                }
                
                if (swap) {
                    rightNode = node;
                    rightRect = nodeRect;       
                }
            }
        }
        
        return rightNode;
    }
    
    private Node findLeftFocusableNode() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
            
        if (_currentFocusNode == null || _focusableNodes.indexOf(_currentFocusNode) == -1) {
            return findLeftestFocusableNodeInScreen();
        }
                
        int index = _focusableNodes.indexOf(_currentFocusNode);

        XYRect currentRect = getPosition(_currentFocusNode);
        Node leftNode = null;
        XYRect leftRect = null;
        
        for (int i = _focusableNodes.size() - 1; i >= 0 ; i--) {
            if (i == index) {
                continue;
            }
            
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null) {
                continue;
            }

            if (nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }
                        
            if (isRectIntersectingHorizontally(nodeRect, currentRect)) {
                boolean swap = false;
                if (nodeRect.x == currentRect.x) {
                    if (nodeRect.width == currentRect.width) {
                        if (i < index) {
                            return node;
                        }
                    } else if (nodeRect.width < currentRect.width) {
                        if (leftNode == null) {
                            swap = true;
                        } else {
                            if (nodeRect.x == leftRect.x) {
                                if (nodeRect.width > leftRect.width) {
                                    swap = true;
                                }
                            } else if (nodeRect.x > leftRect.x) {
                                swap = true;
                            }
                        }    
                    }
                } else if (nodeRect.x < currentRect.x) {
                    if (leftNode == null) {
                        swap = true;
                    } else {
                        if (nodeRect.x == leftRect.x) {
                            if (nodeRect.width > leftRect.width) {
                                swap = true;
                            }
                        } else if (nodeRect.x > leftRect.x) {
                            swap = true;
                        }
                    }                    
                }
                
                if (swap) {
                    leftNode = node;
                    leftRect = nodeRect;       
                }
            }
        }
        
        return leftNode;
    }
        
    private static boolean isRectIntersectingVertically(XYRect rect1, XYRect rect2) {
        if (rect1 == null || rect2 == null) {
            return false;
        }
        
        if (rect2.x <= rect1.x && (rect2.x + rect2.width - 1) >= rect1.x) {
            return true;
        } else if (rect2.x >= rect1.x && rect2.x <= (rect1.x + rect1.width - 1)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isRectIntersectingHorizontally(XYRect rect1, XYRect rect2) {
        if (rect1 == null || rect2 == null) {
            return false;
        }
        
        if (rect2.y <= rect1.y && (rect2.y + rect2.height - 1) >= rect1.y) {
            return true;
        } else if (rect2.y >= rect1.y && rect2.y <= (rect1.y + rect1.height - 1)) {
            return true;
        }
        
        return false;
    }
        
    private void setFocus(Node node) {
        if (node == null) {
            return;
        }

        focusOut();
        focusIn(node);
    }

    private void focusOut(){
        if(_currentFocusNode != null) {
            String id = getNamedAttibute(_currentFocusNode, "id");
            getNavigationNamespace().setOldFocusedId(id);
            
            // disable BF focus
            if (_currentNodeFocused) {
                getBrowserField().setFocus(_currentFocusNode, false);
                _currentNodeFocused = false;         
            }
        
            // disable BF hover
            getBrowserField().setHover(_currentFocusNode, false);
            
            // create a synthetic mouseout Event
            fireMouseEvent("mouseout", _currentFocusNode);
            
            // invalidate the area of old _currentFocusNode
            invalidateNode(_currentFocusNode);
            
            _currentFocusNode = null;
            
            getNavigationNamespace().setNewFocusedId(null);
        }
    }
        
    private void focusIn(Node node){
        _currentFocusNode = node;

        if (node == null) {
            getNavigationNamespace().setNewFocusedId(null);
        }  else {
            String id = getNamedAttibute(node, "id");
            getNavigationNamespace().setNewFocusedId(id);
    
            // create a synthetic mouse over Event
            fireMouseEvent("mouseover", node);
            
            // call BF setHover
            _currentNodeHovered = getBrowserField().setHover(node, true);
            
            if (isAutoFocus(node)) {
                _currentNodeFocused = getBrowserField().setFocus(node, true);
            }
            
            scrollToNode(node);
            invalidateNode(node);            
        }
    }

    private void scrollToNode(Node node) {
        // scroll to the current focus node
        _widgetScreen.getWidgetFieldManager().scrollToNode(node);
    }
    
    private void scrollDown() {
        _widgetScreen.getWidgetFieldManager().scrollDown();
    }

    private void scrollUp() {
        _widgetScreen.getWidgetFieldManager().scrollUp();
    }

    private void scrollRight() {
        _widgetScreen.getWidgetFieldManager().scrollRight();
    }

    private void scrollLeft() {
        _widgetScreen.getWidgetFieldManager().scrollLeft();
    }
    
    private XYRect scaleScreenRectToBrowserFieldRect(XYRect screenRect) {
        return _widgetScreen.getWidgetFieldManager().unscaleRect(screenRect);
    }

    private int unscaleScreenValue(int value) {
        return _widgetScreen.getWidgetFieldManager().unscaleValue(value);
    }

    private void invalidateNode(Node node) {
        _widgetScreen.getWidgetFieldManager().invalidateNode(node);
    }
    
    /* Handles the navigation movement based on direction */
    public void handleDirection(int direction) {
        //System.out.println("WIDGET==> handleDirection: " + (new Integer(direction)).toString());
        dispathUiEvent(NAVIGATION_EVENT_DIRECTION, direction); //Application.getApplication().invokeLater(new JavaScriptExecutantRunnable(_currentFocusNode, direction));
    }
    
    private void internalHandleDirection(int direction) throws Exception {
        //System.out.println("WIDGET==> internalHandleDirection: " + (new Integer(direction)).toString());
        getNavigationNamespace().setDirection(direction);
        
        String attributeValue = null;
        if (_currentFocusNode != null) {
            switch (direction) {
                case FOCUS_NAVIGATION_RIGHT:
                    attributeValue = getNamedAttibute(_currentFocusNode, "x-blackberry-onRight");
                    break;
                case FOCUS_NAVIGATION_LEFT:
                    attributeValue = getNamedAttibute(_currentFocusNode, "x-blackberry-onLeft");
                    break;
                case FOCUS_NAVIGATION_UP:
                    attributeValue = getNamedAttibute(_currentFocusNode, "x-blackberry-onUp");
                    break;
                case FOCUS_NAVIGATION_DOWN:
                    attributeValue = getNamedAttibute(_currentFocusNode, "x-blackberry-onDown");
                    break;
            }
        }

        if (attributeValue != null && attributeValue.length() > 2) {
            getBrowserField().executeScript(attributeValue);
        } else {
            Node node = null;
            
            if (direction == FOCUS_NAVIGATION_DOWN) {
                node = findDownFocusableNode();
            } else if (direction == FOCUS_NAVIGATION_UP) {
                node = findUpFocusableNode();
            } else if (direction == FOCUS_NAVIGATION_RIGHT) {
                node = findRightFocusableNode();
            } else if (direction == FOCUS_NAVIGATION_LEFT) {
                node = findLeftFocusableNode();
            }
            
            XYRect screenRect = getUnscaledScreenRect();
            
            if (direction == FOCUS_NAVIGATION_DOWN) {
                if (node != null) {
                    XYRect nodeRect = getPosition(node);
                    if (nodeRect.y <= (screenRect.y + screenRect.height + WidgetFieldManager.SAFE_MARGIN)) {
                        setFocus(node);
                        return;
                    }
                }
                
                // Only scroll down the screen when there is more content underneath
                int screenVerticalDelta = unscaleScreenValue(_widgetScreen.getWidgetFieldManager().getVirtualHeight()) - screenRect.y - screenRect.height;
                if (screenVerticalDelta > WidgetFieldManager.SAFE_MARGIN) {
                    screenVerticalDelta = WidgetFieldManager.SAFE_MARGIN;
                }
                
                if (screenVerticalDelta > 0) {
                    if (_currentFocusNode != null) {    
                        // If current focused node is out of screen, focus out
                        XYRect currentNodeRect = getPosition(_currentFocusNode);
                        if (currentNodeRect.y + currentNodeRect.height <= screenRect.y + screenVerticalDelta) {
                            focusOut();
                        }
                    }
                    
                    scrollDown();
                    if (_currentFocusNode != null) { 
                        _currentNodeHovered = getBrowserField().setHover(_currentFocusNode, true);
                    }
                } 
            } else if (direction == FOCUS_NAVIGATION_UP) {
                if (node != null) {
                    XYRect nodeRect = getPosition(node);
                    if ((nodeRect.y + nodeRect.height) > (screenRect.y - WidgetFieldManager.SAFE_MARGIN)) {
                        setFocus(node);
                        return;
                    }
                }
                
                // Only scroll down the screen when there is more content underneath
                int screenVerticalDelta = screenRect.y;
                if (screenVerticalDelta > WidgetFieldManager.SAFE_MARGIN) {
                    screenVerticalDelta = WidgetFieldManager.SAFE_MARGIN;
                }
                
                if (screenVerticalDelta > 0) {
                    if (_currentFocusNode != null) {    
                        // If current focused node is out of screen, focus out
                        XYRect currentNodeRect = getPosition(_currentFocusNode);
                        if (currentNodeRect.y > screenRect.y - screenVerticalDelta + screenRect.height) {
                            focusOut();
                        }
                    }
                    
                    scrollUp();
                    if (_currentFocusNode != null) { 
                        _currentNodeHovered = getBrowserField().setHover(_currentFocusNode, true);
                    }                    
                } 
            } else if (direction == FOCUS_NAVIGATION_RIGHT) {
                if (node != null) {
                    XYRect nodeRect = getPosition(node);
                    if (nodeRect.x <= (screenRect.x + screenRect.width + WidgetFieldManager.SAFE_MARGIN)) {
                        setFocus(node);
                        return;
                    }
                }
                
                // Only scroll down the screen when there is more content underneath
                int screenHorizontalDelta = unscaleScreenValue(_widgetScreen.getWidgetFieldManager().getVirtualWidth()) - screenRect.x - screenRect.width;
                if (screenHorizontalDelta > WidgetFieldManager.SAFE_MARGIN) {
                    screenHorizontalDelta = WidgetFieldManager.SAFE_MARGIN;
                }
                
                if (screenHorizontalDelta > 0) {
                    if (_currentFocusNode != null) {    
                        // If current focused node is out of screen, focus out
                        XYRect currentNodeRect = getPosition(_currentFocusNode);
                        if (currentNodeRect.x + currentNodeRect.width <= screenRect.x + screenHorizontalDelta) {
                            focusOut();
                        }
                    }
                    
                    scrollRight();
                    if (_currentFocusNode != null) { 
                        _currentNodeHovered = getBrowserField().setHover(_currentFocusNode, true);
                    }                    
                } 
            } else if (direction == FOCUS_NAVIGATION_LEFT) {
                if (node != null) {
                    XYRect nodeRect = getPosition(node);
                    if ((nodeRect.x + nodeRect.width) > (screenRect.x - WidgetFieldManager.SAFE_MARGIN)) {
                        setFocus(node);
                        return;
                    }
                }
                
                // Only scroll down the screen when there is more content underneath
                int screenHorizontalDelta = screenRect.x;
                if (screenHorizontalDelta > WidgetFieldManager.SAFE_MARGIN) {
                    screenHorizontalDelta = WidgetFieldManager.SAFE_MARGIN;
                }
                
                if (screenHorizontalDelta > 0) {
                    if (_currentFocusNode != null) {    
                        // If current focused node is out of screen, focus out
                        XYRect currentNodeRect = getPosition(_currentFocusNode);
                        if (currentNodeRect.x > screenRect.x - screenHorizontalDelta + screenRect.width) {
                            focusOut();
                        }
                    }
                    
                    scrollLeft();
                    if (_currentFocusNode != null) { 
                        _currentNodeHovered = getBrowserField().setHover(_currentFocusNode, true);
                    }                    
                } 
            }
        }
        //System.out.println("WIDGET==> leave internalHandleDirection");
    }
    
    public void handleClick() {
        dispathUiEvent(NAVIGATION_EVENT_CLICK, -1);
    }
    
    private void internalHandleClick() throws Exception {
        if (_currentFocusNode == null) {
            return;
        }
        
        if (!_currentNodeFocused) {
            addCurrentFocus();
            fireMouseEvent("mousedown", _currentFocusNode);
        }

        return;
    }
    
    public void handleUnclick() {
        dispathUiEvent(NAVIGATION_EVENT_UNCLICK, -1);
    }
    
    private void internalHandleUnclick() throws Exception {
        if (_currentFocusNode == null) {
            return;
        }
        
        if (!_currentNodeFocused) {
            fireMouseEvent("mouseup", _currentFocusNode);
            fireMouseEvent("click", _currentFocusNode);
        }
        return;
    }
        
    private boolean dispathUiEvent(int eventType, int direction) {
        if (_dom == null) {
            return false;
        }
        
        return getUiDispatcher().dispatch( new NavigationUiDispatcherEvent( this, _currentFocusNode, eventType, direction ) );
    }
    
    // ==================================================
    // Utility functions
    // ==================================================
    private String getNamedAttibute(Node node, String name) {
        if (node == null) {
            return null;
        }
        
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node att = nnm.getNamedItem(name);
            if (att instanceof Attr){
                return ((Attr)att).getValue();
            }
        }
        
        return null;
    }

/*    
    private void setNamedAttibuteValue(Node node, String name, String value) {
        if((node.getAttributes().getNamedItem(name) != null) && (node.getAttributes().getNamedItem(name) instanceof Attr)){
            ((Attr)(node.getAttributes().getNamedItem(name))).setValue(value);            
        }
    }
*/    
    private Node getNamedNode(String name) {
        return _dom.getElementById(name);
    }    
    
    private boolean isValidFocusableNode(Node node) {
        if (node == null) {
            return false;
        }

        XYRect nodeRect = getPosition(node);
        if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
            return false;
        }
        
        return true;
    }    
    
    private boolean isFocusableDisabled(Node node) {
/* Optimized for OS 6.0
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node att = nnm.getNamedItem(RIM_FOCUSABLE);
            if ((att instanceof Attr) && ((Attr)att).getValue().equals("false")){
                return true;
            }
        }
*/
        return false;        
    }

    private boolean isFocusableEnabled(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node att = nnm.getNamedItem(RIM_FOCUSABLE);
            if ((att instanceof Attr) && ((Attr)att).getValue().equals("true")){
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isInitialFocusNode(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node att = nnm.getNamedItem(INITIAL_FOCUS);
            if ((att instanceof Attr) && ((Attr)att).getValue().equals("true")){
                return true;
            }
        }
        
        return false;        
    }    
    
    private static final String SUPPRESS_NAVIGATION_INPUT_TYPES = "|checkbox|radio|button|";
    private static final String AUTO_FOCUS_INPUT_TYPES = "|color|date|month|time|week|email|number|password|search|text|url|";
    
    private boolean isAutoFocus( Node node ) {
        if (node instanceof HTMLInputElement) {
            String type = ((HTMLInputElement)node).getType();
            return AUTO_FOCUS_INPUT_TYPES.indexOf( type ) > 0;
        } else if (node instanceof HTMLSelectElement) {
            HTMLSelectElement select = (HTMLSelectElement)node;
            return !select.getMultiple();
        } else if (node instanceof HTMLTextAreaElement) {
            return true;
        }
        return false;        
    }

    private boolean requiresNavigation( Node node ) {
        if (node instanceof HTMLInputElement) {
            String type = ((HTMLInputElement)node).getType();
            return (SUPPRESS_NAVIGATION_INPUT_TYPES.indexOf(type) < 0);
        } else if (node instanceof HTMLSelectElement) {
            return true;
        } else if (node instanceof HTMLTextAreaElement) {
            return true;
        } else if (node instanceof HTMLButtonElement) {
            return true;
        } else if (node instanceof HTMLObjectElement) {
            return true;
        }
        return false;
    } 
    
    private boolean fireMouseEvent(String type, Node node) {
        if (node != null) {
            ((EventTarget)node).dispatchEvent( createMouseEvent(type) );
            return true;
        }        
        return false;
    }
        
    private Event createMouseEvent(String type) {
        DocumentEvent domEvent = (DocumentEvent) _dom;
        Event event = domEvent.createEvent("MouseEvents");
        event.initEvent(type, true, true);
        return event;
    }    

/*    
    private void removeCurrentFocus() {
        if (_currentFocusNode != null && _currentNodeFocused) {
            getBrowserField().setFocus( _currentFocusNode, false );   
            _currentNodeFocused = false;         
            invalidateNode( _currentFocusNode );
        }
    }
*/

    private void addCurrentFocus() {
        if (_currentFocusNode != null && !_currentNodeFocused) {
            _currentNodeFocused = getBrowserField().setFocus( _currentFocusNode, true );   
            invalidateNode( _currentFocusNode );
        }
    }

    private XYRect getPosition(Node node) {
        return _widgetScreen.getWidgetFieldManager().getPosition(node);
    }
    
    private XYRect getUnscaledScreenRect() {
        return scaleScreenRectToBrowserFieldRect( getScreenRect() );
    }

    private XYRect getScreenRect() {
        return 
               new XYRect(_widgetScreen.getWidgetFieldManager().getHorizontalScroll(), 
                          _widgetScreen.getWidgetFieldManager().getVerticalScroll(), 
                          _widgetScreen.getWidgetFieldManager().getWidth(), 
                          _widgetScreen.getWidgetFieldManager().getHeight());
    }
        
    private Vector populateFocusableNodes(boolean firstLoad) {
/* ORIGINAL CODE
        _treeWalker = ((DocumentTraversal)_dom).createTreeWalker(_dom, NodeFilter.SHOW_ALL, null, false);
        
        Vector focusableNodes = new Vector();
        Node node = _treeWalker.getCurrentNode();             
        while(node != null){
            if (node instanceof HTMLAnchorElement || 
                node instanceof HTMLInputElement ||
                node instanceof HTMLSelectElement ||
                node instanceof HTMLTextAreaElement ||
                node instanceof HTMLButtonElement) {
                
                if (node instanceof HTMLSelectElement && ((HTMLSelectElement) node).getMultiple()) {
                } else if (node instanceof HTMLInputElement && ((HTMLInputElement) node).getType().equals("hidden")) {
                } else if (!isFocusableDisabled(node)) {
                    focusableNodes.addElement(node);
                }
                
                if (firstLoad) {
                    getBrowserField().setFocus(node, false);
                }
            } else {
                if (isFocusableEnabled(node)) {
                    focusableNodes.addElement(node);
                }                
            }

            node = _treeWalker.nextNode();
        }      
*/          
        Vector focusableNodes = new Vector();
        NodeSelector selector = (NodeSelector)_dom;
        NodeList nodeList = selector.querySelectorAll("textarea:not([x-blackberry-focusable=false]),a:not([x-blackberry-focusable=false]),input:not([x-blackberry-focusable=false]),select:not([x-blackberry-focusable=false]),button:not([x-blackberry-focusable=false]),[x-blackberry-focusable=true]");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof HTMLSelectElement && ((HTMLSelectElement) node).getMultiple()) {
            } else if (node instanceof HTMLInputElement && ((HTMLInputElement) node).getType().equals("hidden")) {
            } else {
                focusableNodes.addElement(node);
            }
            // getBrowserField().setFocus(node, false); // Not doing this now, because it requires a lot of "time"
        }
        return focusableNodes;
    }
    
    private Node findInitialFocusNode() {
        if (_focusableNodes == null || _focusableNodes.size() == 0) {
            return null;
        }
        
        for (int i = 0; i < _focusableNodes.size(); i++) {
            Node node =  (Node) _focusableNodes.elementAt(i);
            XYRect nodeRect = getPosition(node);
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            
            if (isFocusableDisabled(node)) {
                continue;
            }
                        
            if (isInitialFocusNode(node)) {
                return node;
            }
        }
        
        return null;
    }
    
    private static boolean isDefaultHoverEffectEnabled(Document dom) {
        if (dom == null) {
            return true;
        }
        
        Element head = null;
        Element doc = dom.getDocumentElement();
        if (doc instanceof ElementTraversal) {
            head = ( (ElementTraversal)doc ).getFirstElementChild();
        }

        if (! (head instanceof HTMLHeadElement)) {
            return true;
        }
        
        for (Node node = head.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof HTMLMetaElement) {
                HTMLMetaElement meta = (HTMLMetaElement)node;
                String name = meta.getName();
                String content;
                
                if (name != null && name.equalsIgnoreCase(DEFAULT_HOVER_EFFECT)) {
                    content = meta.getContent();
                    if (content != null && content.equalsIgnoreCase("false")) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private class NavigationUiDispatcherEvent extends Dispatcher.DispatchableEvent {
        Node _focusedNode;
        int _eventType;
        int _direction;
        
        NavigationUiDispatcherEvent(WidgetNavigationController context, Node focusedNode, int eventType) {
            super(context);
            _focusedNode = focusedNode;
            _eventType = eventType;
            _direction = -1;
        }
                        
        NavigationUiDispatcherEvent(WidgetNavigationController context, Node focusedNode, int eventType, int direction) {
            super(context);
            _focusedNode = focusedNode;
            _eventType = eventType;
            _direction = direction;            
        }
        
        protected void dispatch() {
            try {            
                if (_eventType == NAVIGATION_EVENT_DIRECTION) {
                    internalHandleDirection(_direction);
                } else if (_eventType == NAVIGATION_EVENT_CLICK) {
                    internalHandleClick();
                } else if (_eventType == NAVIGATION_EVENT_UNCLICK) {
                    internalHandleUnclick();
                }
            } catch (Exception e) {
            }
            return;
        }
    }
    
    private class NavigationMapUpdateDispatcherEvent extends Dispatcher.DispatchableEvent {
        boolean _pageLoaded;
        
        NavigationMapUpdateDispatcherEvent(WidgetNavigationController context, boolean pageLoaded) {
            super(context);
            _pageLoaded = pageLoaded;
        }
                        
        protected void dispatch() {
            if (_pageLoaded) {
                internalCreateNavigationMap();
            } else {
                internalUpdateNavigationMap();
            }
            return;
        }
    }    
}

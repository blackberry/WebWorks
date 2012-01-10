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
(function() {
navigationController = {

    SAFE_MARGIN : 30,
    SUPPRESS_NAVIGATION_INPUT_TYPES : '|checkbox|radio|button|',
    AUTO_FOCUS_INPUT_TYPES : '|color|date|month|time|week|email|number|password|search|text|url|tel|',
    SCROLLABLE_INPUT_TYPES : '|text|password|email|search|tel|number|url|',
    REQUIRE_CLICK_INPUT_TYPES : '|file|',
    querySelector : 'textarea:not([x-blackberry-focusable=false]),a:not([x-blackberry-focusable=false]),input:not([x-blackberry-focusable=false]), select:not([x-blackberry-focusable=false]), iframe:not([x-blackberry-focusable=false]), button:not([x-blackberry-focusable=false]), [x-blackberry-focusable=true]',

    DOWN : 3,
    UP : 2,
    RIGHT : 0,
    LEFT : 1,

    domDirty : false, // This is for use with BB5 only
    currentFocused : null,
    priorFocusedId : '',
    lastDirection : null,

    focusableNodes : [],

    // Scroll Data
    zoomScale : null,
    currentDirection : null,
    delta : null,
    virtualHeight : null,
    virtualWidth : null,
    verticalScroll : null,
    horizontalScroll : null,
    height : null,
    width : null,
    rangeNavigationOn : false,
    lastCaretPosition : 0,

    /* Sets the top mose focusable item as selected on first load of the page */
    initialize : function(data) {
        // Prepend 'x-blackberry-' in front of these <input> types to prevent browser from automatically displaying its UI on focus
        navigationController.changeInputNodeTypes(["date", "month", "time", "datetime", "datetime-local"]);
        
        // Initialize the scroll information
        navigationController.assignScrollData(data);
        navigationController.focusableNodes = navigationController.getFocusableElements();

        // Figure out our safe margins
        /*
         * if (navigationController.device.isBB5() ||
         * navigationController.device.isBB6()) {
         * navigationController.SAFE_MARGIN = 30; } else {
         * navigationController.SAFE_MARGIN = 50; }
         */
        navigationController.SAFE_MARGIN = navigationController.height / 10;

        /*
         * Set our DOM Mutation events if it is BB5 to mark the DOM as dirty if
         * any elements were inserted or removed from the DOM
         */
        if (navigationController.device.isBB5()) {
            addEventListener("DOMNodeInsertedIntoDocument", function() {
                navigationController.domDirty = true;
            }, true);

            addEventListener("DOMNodeRemovedFromDocument", function() {
                navigationController.domDirty = true;
            }, true);
        }

        // Find our first focusable item
        var initialItems = document.body.querySelectorAll('[x-blackberry-initialFocus=true]');
        if (initialItems.length === 0) {
            navigationController.setRimFocus(navigationController.findHighestFocusableNodeInScreen());
        } else {
            var nextFocusNode = initialItems[0];
            if (!navigationController.isValidFocusableNode(nextFocusNode)) {
                nextFocusNode = null;
            }
            if (nextFocusNode !== null) {
                var result = navigationController.determineBoundingRect(nextFocusNode);
                var bounds = {
                    'element' : nextFocusNode,
                    'rect' : result.rect,
                    'scrollableParent' : result.scrollableParent
                };
                navigationController.setRimFocus(bounds);
            } else { // Get the top most node
                navigationController.setRimFocus(navigationController.findHighestFocusableNodeInScreen());
            }
        }
    },
    
    changeInputNodeTypes : function(inputTypes) {
        var i, 
            j,
            selector,
            nodes;
        
        for(i = 0; i < inputTypes.length; i++) {
            selector = "input[type=" + inputTypes[i] + "]";
            nodes = document.querySelectorAll(selector);
            
            for(j = 0; j < nodes.length; j++) {
                nodes[j].type = "x-blackberry-" + inputTypes[i];
            }
        }
    },

    // Contains all device information
    device : {
        // Determine if this browser is BB5
        isBB5 : function() {
            return navigator.appVersion.indexOf('5.0.0') >= 0;
        },

        // Determine if this browser is BB6
        isBB6 : function() {
            return navigator.appVersion.indexOf('6.0.0') >= 0;
        },

        // Determine if this browser is BB7
        isBB7 : function() {
            return navigator.appVersion.indexOf('7.0.0') >= 0;
        }

    },

    // Assigns all the scrolling data
    assignScrollData : function(data) {
        navigationController.currentDirection = data.direction;
        navigationController.delta = data.delta;
        navigationController.zoomScale = data.zoomScale;
        navigationController.virtualHeight = data.virtualHeight;
        navigationController.virtualWidth = data.virtualWidth;
        navigationController.verticalScroll = data.verticalScroll;
        navigationController.horizontalScroll = data.horizontalScroll;
        navigationController.height = data.height;
        navigationController.width = data.width;
    },

    /* returns the current scrolling direction */
    getDirection : function() {
        return navigationController.currentDirection;
    },

    /* Returns the current focused element's id */
    getFocus : function() {
        if (navigationController.currentFocused === null) {
            return null;
        } else {
            return navigationController.currentFocused.element.getAttribute('id');
        }
    },

    /* Set's the focus to an element with the supplied id */
    setFocus : function(id) {
        if (id.length === 0) {
            navigationController.focusOut();
            return;
        }
        var nextFocusNode = null;
        nextFocusNode = document.getElementById(id);
        if (nextFocusNode !== null) {
            if (!navigationController.isValidFocusableNode(nextFocusNode)) {
                nextFocusNode = null;
            }
        }
        if (nextFocusNode !== null) {
            var result = navigationController.determineBoundingRect(nextFocusNode);
            var bounds = {
                'element' : nextFocusNode,
                'rect' : result.rect,
                'scrollableParent' : result.scrollableParent
            };
            navigationController.setRimFocus(bounds);
        }
    },

    /* Returns the previously focused element's id */
    getPriorFocus : function() {
        return navigationController.priorFocusedId;
    },

    isScrollableElement : function(element) {
        if (element.tagName == 'TEXTAREA') {
            return true;
        }
        if (element.tagName == 'INPUT' && element.hasAttribute('type')) {
            var type = element.getAttribute('type').toLowerCase();
            return navigationController.SCROLLABLE_INPUT_TYPES.indexOf(type) > 0;
        }
        return false;
    },
    
    /* Handle scrolling the focus in the proper direction */
    onScroll : function(data) {
        navigationController.assignScrollData(data);

        // If it is BB5 then don't re-calculate the bounding rects unless
        // the DOM is dirty
        // it's too much of a performance hit on BB5 to re-calculate each
        // scroll
        if (!navigationController.device.isBB5() || navigationController.domDirty) {
            navigationController.focusableNodes = navigationController.getFocusableElements();
            navigationController.domDirty = false;
        }

        // Logic to handle cursor movement in scrollable controls (e.g. text input and textarea),
        // Only handle scrolling with cursor is at the beginning or end position
        if (navigationController.currentFocused) {
            var element = navigationController.currentFocused.element;
            if (navigationController.isScrollableElement(element)) {
                var caretPos = element.selectionStart;
                if (navigationController.currentDirection == navigationController.RIGHT ||
                    navigationController.currentDirection == navigationController.DOWN) {
                    if (navigationController.lastCaretPosition < element.value.length-1) {
                        navigationController.lastCaretPosition = caretPos;
                        return;
                    }
                } else if (navigationController.currentDirection == navigationController.LEFT ||
                    navigationController.currentDirection == navigationController.UP) {
                    if (navigationController.lastCaretPosition > 0) {
                        navigationController.lastCaretPosition = caretPos;
                        return;
                    }
                }
            }
        }
        
        // Determine our direction and scroll
        if (navigationController.currentDirection === navigationController.DOWN) {
            if (navigationController.currentFocused
                    && navigationController.currentFocused.element.hasAttribute('x-blackberry-onDown')) {
                eval(navigationController.currentFocused.element.getAttribute('x-blackberry-onDown'));
                return;
            } else {
                navigationController.handleDirectionDown();
            }
        } else if (navigationController.currentDirection === navigationController.UP) {
            if (navigationController.currentFocused
                    && navigationController.currentFocused.element.hasAttribute('x-blackberry-onUp')) {
                eval(navigationController.currentFocused.element.getAttribute('x-blackberry-onUp'));
                return;
            } else {
                navigationController.handleDirectionUp();
            }
        } else if (navigationController.currentDirection === navigationController.RIGHT) {
            if (navigationController.currentFocused
                    && navigationController.currentFocused.element.hasAttribute('x-blackberry-onRight')) {
                eval(navigationController.currentFocused.element.getAttribute('x-blackberry-onRight'));
                return;
            } else {
                navigationController.handleDirectionRight();
            }
        } else if (navigationController.currentDirection === navigationController.LEFT) {
            if (navigationController.currentFocused
                    && navigationController.currentFocused.element.hasAttribute('x-blackberry-onLeft')) {
                eval(navigationController.currentFocused.element.getAttribute('x-blackberry-onLeft'));
                return;
            } else {
                navigationController.handleDirectionLeft();
            }
        }

        navigationController.lastDirection = navigationController.currentDirection;
    },

    /* Handle the press from the trackpad */
    onTrackpadDown : function() {
    },

    /* Handle the "release" of the press from the trackpad */
    onTrackpadUp : function() {
        if (navigationController.currentFocused === null) {
            return;
        }
        
        try {
            // Now send the mouseup DOM event
            var mouseup = document.createEvent("MouseEvents");
            mouseup.initMouseEvent("mouseup", true, true, window, 0, navigationController.currentFocused.rect.x,
                    navigationController.currentFocused.rect.y, 1, 1, false, false, false, false, 0, null);
            navigationController.currentFocused.element.dispatchEvent(mouseup);
            navigationController.onTrackpadClick();
        } catch (e) {
            // TODO: the last line sometimes causes an exception in 5.0 only, could not figure out why
            // do nothing
        }
    },
    
    onTrackpadClick : function() {
        if (!navigationController.currentFocused) {
            return;
        }
        if (navigationController.isRangeControl(navigationController.currentFocused.element)) {
            navigationController.rangeNavigationOn = !navigationController.rangeNavigationOn;
        }
        
        var focus = navigationController.currentFocused,     //Closure the current focus
            click = document.createEvent("MouseEvents"),
            cancelled,
            nativeInfo;

        // Now send the DOM event and see if any listeners preventDefault()
        //click.initMouseEvent("click", true, true, window, 0, navigationController.currentFocused.rect.x, navigationController.currentFocused.rect.y, 1, 1, false, false, false, false, 0, null);
        click.initMouseEvent("click", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
        cancelled = !focus.element.dispatchEvent(click);
        
        if(!cancelled) {
            //By convention we'll define a handler for each tag with a native UI at navigationController.tagName
            if(typeof(navigationController[focus.element.tagName] === "function")) {
                navigationController[focus.element.tagName](focus.element);
            }
        }
    },
    
    INPUT : function(htmlElem) {
        navigationController.onInput = function(value) {
                                        var change = document.createEvent("HTMLEvents"),
                                            fireChange = false;

                                        if(htmlElem.value !== value) {
                                            htmlElem.value = value;
                                            fireChange = true;
                                        }

                                        if(fireChange) {
                                            change.initEvent("change", true, true);
                                            htmlElem.dispatchEvent(change);
                                        }
        };
        
        var type = htmlElem.attributes.getNamedItem("type").value;
        switch(type) {
            case "x-blackberry-date" : 
            case "x-blackberry-datetime" : 
            case "x-blackberry-datetime-local" : 
            case "x-blackberry-month" : 
            case "x-blackberry-time" : navigationController.handleInputDateTime(
                    type.substring(type.lastIndexOf("-") + 1), 
                    {
                        value : htmlElem.value,
                        min : htmlElem.min,
                        max : htmlElem.max,
                        step : htmlElem.step
                    },
                    navigationController.onInput
                );
                break;
            case "color" :
                    if (navigationController.device.isBB5() || navigationController.device.isBB6()) {
                        var value = htmlElem.value;
                        if (value === "") {
                            value = "000000";
                        }
                        navigationController.handleInputColor(
                            value,
                            navigationController.onInput
                        );
                }
                break;
            default: break; //no special handling
        }
    },
    
    SELECT: function(htmlElem) {
        //We'll stick our event handler at on[tagName]
        navigationController.onSELECT = function(evtData) {
                                    var i,
                                        change = document.createEvent("HTMLEvents"),
                                        newSelection = [],
                                        fireChange = false;
                                    
                                    //Initialize to all false
                                    for(i = 0; i < htmlElem.options.length; i++) {
                                        newSelection.push(false);
                                    }
                                    
                                    //flip the selected items to true
                                    for(i = 0; i < evtData.length; i++) {
                                        newSelection[evtData[i]] = true;
                                    } 
                                    
                                    // change state of multi select to match selection array
                                    // set changed event to fire only if the selection state is
                                    // different
                                    for(i = 0; i < newSelection.length; i++) {
                                        if(newSelection[i] !== htmlElem.options.item(i).selected) {
                                            htmlElem.options.item(i).selected = newSelection[i];
                                            fireChange = true;
                                        }
                                    }
                                    
                                    if(fireChange) {
                                        change.initEvent("change", true, true);
                                        htmlElem.dispatchEvent(change);
                                    }
                                };
        
        function getSelectChoices(htmlElem) {
            var opts = [],
                optionNodes = htmlElem.options,
                i = 0,
                currentOption,
                currentGroup = "", 
                nodeGroup;

            for(i; i < optionNodes.length; i++) {
                currentOption = optionNodes.item(i);
                nodeGroup = (currentOption.parentNode.tagName === "OPTGROUP") ? currentOption.parentNode.label : "";
                
                if(currentGroup !== nodeGroup) {
                    currentGroup = nodeGroup;
                    
                    opts.push(
                        {
                            "label" : currentGroup,
                            "enabled" : false,
                            "selected" : false, 
                            "type" : "group"
                        }
                    );
                } 
                
                opts.push( 
                    {   
                        "label" : currentOption.text,
                        "enabled" : !currentOption.disabled || (currentOption.disabled == false),
                        "selected" : currentOption.selected || (currentOption.selected == true), 
                        "type" : "option"
                    } 
                );
            }
            
            return opts;
        };
        
        navigationController.handleSelect(
            typeof(htmlElem.attributes.multiple) !== "undefined" ? true : false, 
            getSelectChoices(htmlElem),
            navigationController.onSELECT
        );
    },

    /* See if the passed in element is still in our focusable list */
    indexOf : function(node) {
        var length = navigationController.focusableNodes.length;
        for ( var i = 0; i < length; i++) {
            if (navigationController.focusableNodes[i].element == node.element)
                return i;
        }
        return -1;
    },

    // Support function for scrolling down
    handleDirectionDown : function() {
        var screenRect = navigationController.getUnscaledScreenRect();
        var node = navigationController.findDownFocusableNode();

        if (node != null) {
            var nodeRect = node.rect;
            if (nodeRect.y <= (screenRect.y + screenRect.height /* + navigationController.SAFE_MARGIN */)) {
                navigationController.setRimFocus(node);
                return;
            }
        }

        // Only scroll down the screen when there is more content underneath
        var screenVerticalDelta = navigationController.unscaleValue(navigationController.virtualHeight) - screenRect.y
                - screenRect.height;
        if (screenVerticalDelta > navigationController.SAFE_MARGIN) {
            screenVerticalDelta = navigationController.SAFE_MARGIN;
        }

        if (screenVerticalDelta > 0) {
            if (navigationController.currentFocused != null) {
                // If current focused node is out of screen, focus out
                var currentNodeRect = navigationController.currentFocused.rect;
                if (currentNodeRect.y + currentNodeRect.height <= screenRect.y + screenVerticalDelta) {
                    navigationController.focusOut();
                }
            }
            navigationController.scrollDown();
        }
    },

    // Support function for scrolling up
    handleDirectionUp : function() {
        var screenRect = navigationController.getUnscaledScreenRect();
        var node = navigationController.findUpFocusableNode();
        if (node != null) {
            var nodeRect = node.rect;
            if ((nodeRect.y + nodeRect.height) > (screenRect.y /* - navigationController.SAFE_MARGIN */)) {
                navigationController.setRimFocus(node);
                return;
            }
        }
        // Only scroll down the screen when there is more content above
        var screenVerticalDelta = screenRect.y;
        if (screenVerticalDelta > navigationController.SAFE_MARGIN) {
            screenVerticalDelta = navigationController.SAFE_MARGIN;
        }
        if (screenVerticalDelta > 0) {
            if (navigationController.currentFocused != null) {
                // If current focused node is out of screen, focus out.
                var currentNodeRect = navigationController.currentFocused.rect;
                if (currentNodeRect.y > screenRect.y - screenVerticalDelta + screenRect.height) {
                    navigationController.focusOut();
                }
            }
            navigationController.scrollUp();
        }
    },
        
    //determines whether an input control is of the "range" type
    isRangeControl : function(inputControl) {
        if (inputControl.type == "range") {
            return true;
        }
        return false;
    },

    //Support function for handling the slider movement of the range input control in navigation mode   
    handleRangeSliderMovement : function(direction) {
        var currentNode = navigationController.currentFocused.element;  
        var currentValue = currentNode.value;   
        switch (direction) {
            case 'r': //scroll right, increment position                                
                if (currentValue < currentNode.clientWidth) {
                    currentNode.value ++;
                }       
                break;
            case 'l': //scroll left, decrement position
                if (currentValue > 1) {
                    currentNode.value --;
                }
                break;
            default:
                console.log("Impossible");                      
            } 
        }, 
        
    // Support function for scrolling right
    handleDirectionRight : function() {
        if (navigationController.currentFocused != null &&
                navigationController.isRangeControl(navigationController.currentFocused.element) &&
                navigationController.rangeNavigationOn) {
            navigationController.handleRangeSliderMovement('r');
        } else { //we are not on a range control in navigation mode
            var screenRect = navigationController.getUnscaledScreenRect();
            var node = navigationController.findRightFocusableNode();
            if (node != null) {
                var nodeRect = node.rect;
                if (nodeRect.x <= (screenRect.x + screenRect.width /* + navigationController.SAFE_MARGIN */)) {
                    navigationController.setRimFocus(node);
                    return;
                }
            }
            // Only scroll down the screen when there is more content to the right.
            var screenHorizontalDelta = navigationController.unscaleValue(navigationController.virtualWidth) - screenRect.x
                - screenRect.width;
            if (screenHorizontalDelta > navigationController.SAFE_MARGIN) {
                screenHorizontalDelta = navigationController.SAFE_MARGIN;
            }
            if (screenHorizontalDelta > 0) {
                if (navigationController.currentFocused != null) {
                    // If current focused node is out of screen, focus out.
                    var currentNodeRect = navigationController.currentFocused.rect;
                    if (currentNodeRect.x + currentNodeRect.width <= screenRect.x + screenHorizontalDelta) {
                        navigationController.focusOut();
                    }
                }
                navigationController.scrollRight();
            }
        }
    },

    /* Support function for scrolling left */
    handleDirectionLeft : function() {
        if (navigationController.currentFocused != null && 
                navigationController.isRangeControl(navigationController.currentFocused.element) &&
                navigationController.rangeNavigationOn) {
            navigationController.handleRangeSliderMovement('l');
        } else { //we are not on a range control in navigation mode
            var screenRect = navigationController.getUnscaledScreenRect();
            var node = navigationController.findLeftFocusableNode();
            if (node != null) {
                var nodeRect = node.rect;
                if ((nodeRect.x + nodeRect.width) > (screenRect.x /* - navigationController.SAFE_MARGIN */)) {
                    navigationController.setRimFocus(node);
                return;
                }
            }
            // Only scroll down the screen when there is more content to the left.
            var screenHorizontalDelta = screenRect.x;
            if (screenHorizontalDelta > navigationController.SAFE_MARGIN) {
                screenHorizontalDelta = navigationController.SAFE_MARGIN;
            }
            if (screenHorizontalDelta > 0) {
                if (navigationController.currentFocused != null) {
                    // If current focused node is out of screen, focus out.
                    var currentNodeRect = navigationController.currentFocused.rect;
                    if (currentNodeRect.x > screenRect.x - screenHorizontalDelta + screenRect.width) {
                        navigationController.focusOut();
                    }
                }
                navigationController.scrollLeft();
            }
        }
    },

    /* Highlight the first item on the screen */
    findHighestFocusableNodeInScreen : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;
        var screenRect = navigationController.getUnscaledScreenRect();
        var firstNode = null;
        var firstRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = 0; i < length; i++) {
            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            if (navigationController.isRectIntersectingVertically(nodeRect, screenRect)) {
                var swap = false;
                if (nodeRect.y >= screenRect.y) {
                    swap = navigationController.needSwapWithDownRect(firstRect, nodeRect);
                }
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    },

    /* Find the lowest focusable node visible on the screen */
    findLowestFocusableNodeInScreen : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var screenRect = navigationController.getUnscaledScreenRect();
        var firstNode = null;
        var firstRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = length - 1; i >= 0; i--) {
            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            if (navigationController.isRectIntersectingVertically(nodeRect, screenRect)) {
                var swap = false;
                // Should select the lowest item in the screen that
                // completely fits on screen
                if (nodeRect.y + nodeRect.height < screenRect.y + screenRect.height) {
                    swap = navigationController.needSwapWithUpRect(firstRect, nodeRect);
                }
                if (swap) {
                    firstNode = node;
                    firstRect = nodeRect;
                }
            }
        }
        return firstNode;
    },

    /* Find the leftmost focusable node visible on the screen */
    findLeftmostFocusableNodeInScreen : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var screenRect = navigationController.getUnscaledScreenRect();
        var firstNode = null;
        var firstRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = length - 1; i >= 0; i--) {
            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            if (navigationController.isRectIntersectingHorizontally(nodeRect, screenRect)) {
                var swap = false;

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
    },

    /* Find the rightmost focusable node visible on the screen */
    findRightmostFocusableNodeInScreen : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var screenRect = navigationController.getUnscaledScreenRect();
        var firstNode = null;
        var firstRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = 0; i < length; i++) {
            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }
            if (navigationController.isRectIntersectingHorizontally(nodeRect, screenRect)) {
                var swap = false;

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
    },

    /* Scrolls downward to the next available focusable item */
    findDownFocusableNode : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var index;

        if (navigationController.currentFocused != null)
            index = navigationController.indexOf(navigationController.currentFocused);
        else
            return navigationController.findHighestFocusableNodeInScreen();

        if (index == -1) {
            return navigationController.findHighestFocusableNodeInScreen();
        }

        var currentRect = navigationController.currentFocused.rect;
        var screenRect = navigationController.getUnscaledScreenRect();
        var length = navigationController.focusableNodes.length;
        var downNode = null;
        var downRect = null;
        for ( var i = 0; i < length; i++) {
            if (i == index) {
                continue;
            }

            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (navigationController.isRectIntersectingVertically(nodeRect, currentRect)) {
                var swap = false;
                if (nodeRect.y == currentRect.y) {
                    if (nodeRect.height == currentRect.height) {
                        if (i > index) {
                            return node;
                        }
                    } else if (nodeRect.height > currentRect.height) {
                        swap = navigationController.needSwapWithDownRectInPriority(downRect, nodeRect);
                    }
                } else if (nodeRect.y > currentRect.y) {
                    swap = navigationController.needSwapWithDownRectInPriority(downRect, nodeRect);
                }
                if (swap) {
                    downNode = node;
                    downRect = nodeRect;
                }
            } else if (!navigationController.isRectIntersectingHorizontally(nodeRect, currentRect)
                    && navigationController.isRectIntersectingVertically(nodeRect, screenRect)) {
                var swap = false;
                if (nodeRect.y > currentRect.y) {
                    swap = navigationController.needSwapWithDownRect(downRect, nodeRect);
                }
                if (swap) {
                    downNode = node;
                    downRect = nodeRect;
                }
            }
        }
        return downNode;
    },

    /* Find the next node that should have focus in the Up direction */
    findUpFocusableNode : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var index;

        if (navigationController.currentFocused != null)
            index = navigationController.indexOf(navigationController.currentFocused);
        else
            return navigationController.findLowestFocusableNodeInScreen();

        if (index == -1) {
            return navigationController.findLowestFocusableNodeInScreen();
        }

        var currentRect = navigationController.currentFocused.rect;
        var upNode = null;
        var upRect = null;
        var screenRect = navigationController.getUnscaledScreenRect();
        var length = navigationController.focusableNodes.length;
        for ( var i = length - 1; i >= 0; i--) {
            if (i == index) {
                continue;
            }

            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;
            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (navigationController.isRectIntersectingVertically(nodeRect, currentRect)) {
                var swap = false;
                if (nodeRect.y == currentRect.y) {
                    if (nodeRect.height == currentRect.height) {
                        if (i < index) {
                            return node;
                        }
                    } else if (nodeRect.height < currentRect.height) {
                        swap = navigationController.needSwapWithUpRectInPriority(upRect, nodeRect);
                    }
                } else if (nodeRect.y < currentRect.y) {
                    swap = navigationController.needSwapWithUpRectInPriority(upRect, nodeRect);
                }
                if (swap) {
                    upNode = node;
                    upRect = nodeRect;
                }
            } else if (!navigationController.isRectIntersectingHorizontally(nodeRect, currentRect)
                    && navigationController.isRectIntersectingVertically(nodeRect, screenRect)) {
                var swap = false;
                if (nodeRect.y < currentRect.y) {
                    swap = navigationController.needSwapWithUpRect(upRect, nodeRect);
                }
                if (swap) {
                    upNode = node;
                    upRect = nodeRect;
                }
            }
        }
        return upNode;
    },

    /* Find the next node that should have focus in the Left direction */
    findLeftFocusableNode : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var index;

        if (navigationController.currentFocused != null)
            index = navigationController.indexOf(navigationController.currentFocused);
        else
            return navigationController.findLeftmostFocusableNodeInScreen();

        if (index == -1) {
            return navigationController.findLeftmostFocusableNodeInScreen();
        }

        var currentRect = navigationController.currentFocused.rect;
        var leftNode = null;
        var leftRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = length - 1; i >= 0; i--) {
            if (i == index) {
                continue;
            }

            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (navigationController.isRectIntersectingHorizontally(nodeRect, currentRect)) {
                var swap = false;
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
    },

    /* Find the next node that should have focus in the Left direction */
    findRightFocusableNode : function() {
        if (navigationController.focusableNodes == null || navigationController.focusableNodes.length == 0)
            return null;

        var index;

        if (navigationController.currentFocused != null)
            index = navigationController.indexOf(navigationController.currentFocused);
        else
            return navigationController.findRightmostFocusableNodeInScreen();

        if (index == -1) {
            return navigationController.findRightmostFocusableNodeInScreen();
        }

        var currentRect = navigationController.currentFocused.rect;
        var rightNode = null;
        var rightRect = null;
        var length = navigationController.focusableNodes.length;
        for ( var i = 0; i < length; i++) {
            if (i == index) {
                continue;
            }
            var node = navigationController.focusableNodes[i];
            var nodeRect = node.rect;

            if (nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0) {
                continue;
            }

            if (navigationController.isRectIntersectingHorizontally(nodeRect, currentRect)) {
                var swap = false;
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
    },

    /*
     * This function will find all of the focusable items in the DOM and then
     * populate the list of elements and their absolute bounding rects
     */
    getFocusableElements : function() {
        var items = [],
            i, j,
            iframes = document.getElementsByTagName("iframe"),
            iframeFocusables,
            focusables = document.body.querySelectorAll(navigationController.querySelector);
        
        for(i = 0; i < focusables.length; i++) {
                if (focusables[i].tagName === "SELECT") {
                        if (!focusables[i].disabled) {
                                items.push(focusables[i]);
                        }
                } else {
                        items.push(focusables[i]);
                }
        }
        
        for(i = 0; i < iframes.length; i++) {
            //Make sure the iframe has loaded content before we add it to the navigation map
            if(iframes[i].contentDocument.body !== null) {
                iframeFocusables = iframes[i].contentDocument.body.querySelectorAll(navigationController.querySelector);
                for(j = 0; j < iframeFocusables.length; j++) {
                    items.push(iframeFocusables[j]);
                }
            }
        }
        
        var length = items.length;
        // Determine bounding rects and populate list
        var boundingRects = [];
        for ( var i = 0; i < length; i++) {
            var item = items[i];
            var result = navigationController.determineBoundingRect(item);
            var bounds = {
                'element' : item,
                'rect' : result.rect,
                'scrollableParent' : result.scrollableParent
            };
            /*
             * A mouseover event listener is attached to each element so we know
             * the currently focused element even if it the mouseover was invoked 
             * by using touch
             */
            bounds.element.addEventListener("mouseover", function(event) {
                var length = navigationController.focusableNodes.length;
                var element;
                for (var i = 0 ; i < length; i++) {
                    if( this == navigationController.focusableNodes[i].element) {
                        navigationController.currentFocused = navigationController.focusableNodes[i];
                        element = navigationController.currentFocused.element;
                        if (navigationController.isScrollableElement(element)) {
                            // this is to workaround the issue where input is selected on the first time
                            if (element.tagName == 'INPUT') {
                                element.value = element.value;
                            }
                            navigationController.lastCaretPosition = element.selectionStart;
                        }
                    }
                }
            }
            ,false);
            boundingRects.push(bounds);
        }

        return boundingRects;
    },

    /*
     * This function will recursively traverse the dom through the element's
     * parent nodes to find its true absolute bounding rectangle on the screen
     */
    determineBoundingRect : function(element) {
        var y = 0;
        var x = 0;
        var height = element.offsetHeight;
        var width = element.offsetWidth;
        var scrollableParent = null;

        if (element.offsetParent) {
            do {
                y += element.offsetTop;
                x += element.offsetLeft;

                // See if the parent is scrollable
                if (scrollableParent == null && element.parentNode != null
                        && element.parentNode.style.overflow == 'scroll') {
                    scrollableParent = element.parentNode;
                }

                // If the element is absolute or fixed then it doesn't
                // matter what their
                // parent's positions are. Their position is already
                // accurate
                if (element.style.position == 'absolute' || element.style.position == 'fixed')
                    break;

                if (!element.offsetParent)
                    break;
            } while (element = element.offsetParent)
        }

        return {
            'scrollableParent' : scrollableParent,
            'rect' : {
                'y' : y,
                'x' : x,
                'height' : height,
                'width' : width
            }
        };
    },

    setRimFocus : function(target) {
        try {
            // First un focus the old focused item
            navigationController.focusOut();

            // Now set focus to the new item
            var mouseover = document.createEvent('MouseEvents');
            mouseover.initMouseEvent('mouseover', true, true, window, 0, target.rect.x, target.rect.y, 1, 1, false, false,
                    false, false, null, null);
            target.element.dispatchEvent(mouseover);

            // Set our focused item
            navigationController.currentFocused = target;

            if (navigationController.isAutoFocus(target)) {
                target.element.focus();
            }

            // Scroll to the current focus node
            navigationController.scrollToRect(navigationController.scaleRect(target.rect));
        } catch(error) {
            console.log(error);
            console.log(error.message);
        }
    },

    focusOut : function() {
        if (navigationController.currentFocused != null) {
            var priorFocused = navigationController.currentFocused;
            navigationController.priorFocusedId = priorFocused.element.getAttribute('id');

            // Fire our mouse out event
            var mouseout = document.createEvent("MouseEvents");
            mouseout.initMouseEvent("mouseout", true, true, window, 0, navigationController.currentFocused.rect.x,
                    navigationController.currentFocused.rect.y, 1, 1, false, false, false, false, null, null);
            navigationController.currentFocused.element.dispatchEvent(mouseout);
            navigationController.currentFocused = null;

            if (navigationController.isAutoFocus(priorFocused)) {
                priorFocused.element.blur();
            }
        }
    },

    /* See if the two rectangles intersect with each other Vertically */
    isRectIntersectingVertically : function(rect1, rect2) {
        if (rect1 == null || rect2 == null)
            return false;
        if (rect2.x <= rect1.x && (rect2.x + rect2.width - 1) >= rect1.x)
            return true;
        return (rect2.x >= rect1.x && rect2.x <= (rect1.x + rect1.width - 1));
    },

    /* See if we need to swap the two rects in priority */
    needSwapWithDownRectInPriority : function(downRect, checkedRect) {
        if (downRect == null)
            return true;
        if (checkedRect.y == downRect.y && checkedRect.height <= downRect.height)
            return true;
        return (checkedRect.y < downRect.y);
    },

    /* Do these rects intersect Horizontally */
    isRectIntersectingHorizontally : function(rect1, rect2) {
        if (rect1 == null || rect2 == null)
            return false;
        if (rect2.y <= rect1.y && (rect2.y + rect2.height - 1) >= rect1.y)
            return true;
        return (rect2.y >= rect1.y && rect2.y <= (rect1.y + rect1.height - 1));
    },

    /* Do we need to swap down */
    needSwapWithDownRect : function(downRect, checkedRect) {
        if (downRect == null)
            return true;
        if (checkedRect.y == downRect.y && checkedRect.height < downRect.height)
            return true;
        return (checkedRect.y < downRect.y);
    },

    /* See if we need to swap the two rects in priority */
    needSwapWithUpRectInPriority : function(upRect, checkedRect) {
        if (upRect == null)
            return true;
        if (checkedRect.y == upRect.y && checkedRect.height >= upRect.height)
            return true;
        return (checkedRect.y > upRect.y);
    },

    /* Do we need to swap up */
    needSwapWithUpRect : function(upRect, checkedRect) {
        if (upRect == null)
            return true;
        if (checkedRect.y == upRect.y && checkedRect.height > upRect.height)
            return true;
        return (checkedRect.y > upRect.y);
    },

    /* TODO: Fill this with real code to deal with content scrolls */
    getUnscaledScreenRect : function() {
        var screenRect = {
            'y' : navigationController.verticalScroll,
            'x' : navigationController.horizontalScroll,
            'height' : navigationController.height,
            'width' : navigationController.width
        };
        return navigationController.unscaleRect(screenRect);
    },

    /* See if this node can be focused */
    isValidFocusableNode : function(node) {
        if (node == null)
            return false;

        // Should only consider node that is in the valid set of nodes
        if (navigationController.indexOf({'element' : node}) == -1) {
            return false;
        }

        var nodeRect = navigationController.determineBoundingRect(node);
        return !(nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0);
    },

    /* See if this node needs a focus */
    isAutoFocus : function(node) {

        if (node.element.tagName == 'INPUT') {
            if (node.element.hasAttribute('type')) {
                var type = node.element.getAttribute('type').toLowerCase();
                return navigationController.AUTO_FOCUS_INPUT_TYPES.indexOf(type) > 0;
            }
        }
        
        if (node.element.tagName == 'TEXTAREA') {
            return true;
        }
        
        return false;
    },

    scrollToRect : function(rect) {
        // Check vertical scroll.
        var verticalScroll = navigationController.verticalScroll;
        var newVerticalScroll = verticalScroll;

        // alert("height: " + navigationController.height + " vScroll: " +
        // verticalScroll + " rect.y: " + rect.y + " rect.height: " +
        // rect.height);

        if (rect.y < verticalScroll) {
            newVerticalScroll = Math.max(rect.y, 0);
            // alert("rect.y (" + rect.y + ") < vScroll (" + verticalScroll +
            // "), need scroll up, newVScroll: " + newVerticalScroll);
        } else if (rect.y + rect.height > verticalScroll + navigationController.height) {
            /*
             * (alert("need scroll down, a: " + (rect.y + rect.height -
             * navigationController.height + navigationController.scaleValue(
             * navigationController.SAFE_MARGIN ) ) + " b: " +
             * (navigationController.virtualHeight -
             * navigationController.height) + " c: " + (rect.y + rect.height -
             * navigationController.height));
             */
            newVerticalScroll = Math
                    .min(
                            rect.y + rect.height - navigationController.height,
                            navigationController.virtualHeight - navigationController.height);
        }

        // Check horizontal scroll.
        var horizontalScroll = navigationController.horizontalScroll;
        var newHorizontalScroll = horizontalScroll;
        if (rect.width >= navigationController.width) {
            newHorizontalScroll = Math.max(rect.x, 0);
        } else if (rect.x < horizontalScroll) {
            newHorizontalScroll = Math.max(rect.x, 0);
        } else if (rect.x + rect.width > horizontalScroll + navigationController.width) {
            newHorizontalScroll = Math.min(rect.x + rect.width - navigationController.width,
                    navigationController.virtualWidth - navigationController.width);
        }
        
        if (newHorizontalScroll != horizontalScroll || newVerticalScroll != verticalScroll) {
            navigationController.scrollXY(newHorizontalScroll, newVerticalScroll);
        }
    },

    scrollDown : function() {
        var newVerticalScroll = Math.min(navigationController.verticalScroll
                + navigationController.scaleValue(navigationController.SAFE_MARGIN), navigationController.virtualHeight
                - navigationController.height);
        navigationController.scrollY(newVerticalScroll);
    },

    scrollUp : function() {
        var newVerticalScroll = Math.max(navigationController.verticalScroll
                - navigationController.scaleValue(navigationController.SAFE_MARGIN), 0);
        navigationController.scrollY(newVerticalScroll);
    },

    scrollRight : function() {
        var newHorizontalScroll = Math.min(navigationController.horizontalScroll
                + navigationController.scaleValue(navigationController.SAFE_MARGIN), navigationController.virtualWidth
                - navigationController.width);
        navigationController.scrollXY(newHorizontalScroll, navigationController.verticalScroll);
    },

    scrollLeft : function() {
        var newHorizontalScroll = Math.max(navigationController.horizontalScroll
                - navigationController.scaleValue(navigationController.SAFE_MARGIN), 0);
        navigationController.scrollXY(newHorizontalScroll, navigationController.verticalScroll);
    },

    scaleRect : function(rect) {
        var newRect = {
            'y' : navigationController.scaleValue(rect.y),
            'x' : navigationController.scaleValue(rect.x),
            'height' : navigationController.scaleValue(rect.height),
            'width' : navigationController.scaleValue(rect.width)
        };

        return newRect;
    },

    scaleValue : function(value) {
        return Math.round(value * navigationController.zoomScale);
    },

    unscaleValue : function(value) {
        return Math.round(value / navigationController.zoomScale);
    },

    unscaleRect : function(rect) {
        var newRect = {
            'y' : navigationController.unscaleValue(rect.y),
            'x' : navigationController.unscaleValue(rect.x),
            'height' : navigationController.unscaleValue(rect.height),
            'width' : navigationController.unscaleValue(rect.width)
        };

        return newRect;
    },

    scrollXY : function(x, y) {
        window.scrollTo(navigationController.unscaleValue(x), navigationController.unscaleValue(y));
    },

    scrollX : function(value) {
        window.scrollTo(navigationController.unscaleValue(value), window.pageYOffset);
    },

    scrollY : function(value) {
        window.scrollTo(window.pageXOffset, navigationController.unscaleValue(value));
    }

}

/* Arms length object so that we can remove any reference to blackberry.* from the navigationController */

bbNav = {
    init : function() {
                if (window.top === window.self) {
                        var data = {
                                'direction' : 3,
                                'delta' : 1,
                                'zoomScale' : 1,
                                'virtualHeight' : screen.height,
                                'virtualWidth' : screen.width,
                                'verticalScroll' : 0,
                                'horizontalScroll' : 0,
                                'height' : screen.height,
                                'width' : screen.width
                        };
                        
                        blackberry.focus.onScroll = navigationController.onScroll;
                        blackberry.focus.onTrackpadDown = navigationController.onTrackpadDown;
                        blackberry.focus.onTrackpadUp = navigationController.onTrackpadUp;
                        blackberry.focus.getDirection = navigationController.getDirection;
                        blackberry.focus.getFocus = navigationController.getFocus;
                        blackberry.focus.getPriorFocus = navigationController.getPriorFocus;
                        blackberry.focus.setFocus = navigationController.setFocus;
                        blackberry.focus.focusOut = navigationController.focusOut;
                        
                        navigationController.initialize(data);
                        
                        navigationController.handleSelect = blackberry.ui.dialog.selectAsync;
                        navigationController.handleInputDateTime = blackberry.ui.dialog.dateTimeAsync;
                        navigationController.handleInputColor = blackberry.ui.dialog.colorPickerAsync;
                }
    }
}

addEventListener("load", bbNav.init, false);
}());

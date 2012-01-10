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

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.web.widget.bf.navigationcontroller.NavigationController;

public class NavigationNamespace extends Scriptable {

    public static final String NAME = "blackberry.focus";

    public static final String LABEL_RIGHT = "RIGHT";
    public static final String LABEL_LEFT = "LEFT";
    public static final String LABEL_UP = "UP";
    public static final String LABEL_DOWN = "DOWN";

    public static final String LABEL_SET_FOCUS = "setFocus";
    public static final String LABEL_GET_FOCUS = "getFocus";
    public static final String LABEL_GET_PRIORFOCUS = "getPriorFocus";
    public static final String LABEL_GET_DIRECTION = "getDirection";

    public static final String LABEL_FOCUS_OUT = "focusOut";

    public static final String LABEL_ON_SCROLL     = "onScroll";
    public static final String LABEL_ON_TRACKPADUP    = "onTrackpadUp";
    public static final String LABEL_ON_TRACKPADDOWN  = "onTrackpadDown";

    private WidgetFieldManager _fieldManager;

    private ScriptableFunction _funcSetRimFocus;
    private ScriptableFunction _funcGetRimFocus;
    private ScriptableFunction _funcGetPriorFocus;
    private ScriptableFunction _funcGetDirection;

    private ScriptableFunction _onScroll;
    private ScriptableFunction _focusOut;
    private ScriptableFunction _onTrackpadUp;
    private ScriptableFunction _onTrackpadDown;

    public NavigationNamespace( BrowserFieldScreen widgetScreen, WidgetFieldManager fieldManager ) {
        _fieldManager = fieldManager;
    }
    
    /* @Override */
    public Scriptable getParent() {
        return null;
    }

    /* @Override */
    public Object getField( String name ) throws Exception {
        if( name.equals( LABEL_RIGHT ) ) {
            return new Integer( NavigationController.FOCUS_NAVIGATION_RIGHT );
        }

        if( name.equals( LABEL_LEFT ) ) {
            return new Integer( NavigationController.FOCUS_NAVIGATION_LEFT );
        }

        if( name.equals( LABEL_DOWN ) ) {
            return new Integer( NavigationController.FOCUS_NAVIGATION_DOWN );
        }

        if( name.equals( LABEL_UP ) ) {
            return new Integer( NavigationController.FOCUS_NAVIGATION_UP );
        }

        if( name.equals( LABEL_SET_FOCUS ) ) {
            return _funcSetRimFocus;
        }

        if( name.equals( LABEL_GET_FOCUS ) ) {
            return _funcGetRimFocus;
        }

        if( name.equals( LABEL_GET_PRIORFOCUS ) ) {
            return _funcGetPriorFocus;
        }

        if( name.equals( LABEL_GET_DIRECTION ) ) {
            return _funcGetDirection;
        }
        
        if( name.equals( LABEL_ON_SCROLL ) ) {
            return _onScroll;
        }

        if( name.equals( LABEL_ON_TRACKPADUP ) ) {
            return _onTrackpadUp;
        }

        if( name.equals( LABEL_ON_TRACKPADDOWN ) ) {
            return _onTrackpadDown;
        }

        return UNDEFINED;
    }

    /* @Override */
    public boolean putField( String name, Object value ) throws Exception {
        if( name.equals( LABEL_ON_SCROLL ) ) {
            _onScroll = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_ON_TRACKPADUP ) ) {
            _onTrackpadUp = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_ON_TRACKPADDOWN ) ) {
            _onTrackpadDown = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_SET_FOCUS ) ) {
            _funcSetRimFocus = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_GET_FOCUS ) ) {
            _funcGetRimFocus = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_GET_PRIORFOCUS ) ) {
            _funcGetPriorFocus = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_GET_DIRECTION ) ) {
            _funcGetDirection = (ScriptableFunction)value;
        }

        if( name.equals( LABEL_FOCUS_OUT ) ) {
            _focusOut = (ScriptableFunction)value;
        }
        
        return super.putField(name, value);
    }

    /**
     * Object that contains all data needed by JS navigation logic
     */
    private class NavigationData extends Scriptable {
        public static final String LABEL_DIRECTION          = "direction";
        public static final String LABEL_DELTA              = "delta";
        public static final String LABEL_ZOOMSCALE          = "zoomScale";
        public static final String LABEL_VIRTUALHEIGHT      = "virtualHeight";
        public static final String LABEL_VIRTUALWIDTH       = "virtualWidth";
        public static final String LABEL_VERTICALSCROLL     = "verticalScroll";
        public static final String LABEL_HORIZONTALSCROLL   = "horizontalScroll";
        public static final String LABEL_HEIGHT             = "height";
        public static final String LABEL_WIDTH              = "width";
        
        private int _direction;
        private int _delta;
        private double _zoomScale;
        private int _virtualHeight;
        private int _virtualWidth;
        private int _verticalScroll;
        private int _horizontalScroll;
        private int _height;
        private int _width;
    
        /* @Override */
        public Scriptable getParent() {
            return null;
        }

        /* @Override */
        public Object getField( String name ) throws Exception {
            if( name.equals( LABEL_WIDTH ) ) {
                return new Integer( _width );
            }
            
            if( name.equals( LABEL_HEIGHT ) ) {
                return new Integer( _height );
            }
            
            if( name.equals( LABEL_HORIZONTALSCROLL ) ) {
                return new Integer( _horizontalScroll );
            }
            
            if( name.equals( LABEL_VERTICALSCROLL ) ) {
                return new Integer( _verticalScroll );
            }
            
            if( name.equals( LABEL_VIRTUALWIDTH ) ) {
                return new Integer( _virtualWidth );
            }
            
            if( name.equals( LABEL_VIRTUALHEIGHT ) ) {
                return new Integer( _virtualHeight );
            }
            
            if( name.equals( LABEL_ZOOMSCALE ) ) {
                return new Double( _zoomScale );
            }
            
            if( name.equals( LABEL_DIRECTION ) ) {
                return new Integer( _direction );
            }
            
            if( name.equals( LABEL_DELTA ) ) {
                return new Integer( _delta );
            }
        
            return super.getField(name);
        }
        
        /* @Override */
        public boolean putField( String name, Object value ) throws Exception {
            if( name.equals( LABEL_WIDTH ) ) {
                _width = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_HEIGHT ) ) {
                _height = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_HORIZONTALSCROLL ) ) {
                _horizontalScroll = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_VERTICALSCROLL ) ) {
                _verticalScroll = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_VIRTUALWIDTH ) ) {
                _virtualWidth = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_VIRTUALHEIGHT ) ) {
                _virtualHeight = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_ZOOMSCALE ) ) {
                _zoomScale = ((Double)value).doubleValue();
            }
            
            if( name.equals( LABEL_DIRECTION ) ) {
                _direction = ((Integer)value).intValue();
            }
            
            if( name.equals( LABEL_DELTA ) ) {
                _delta = ((Integer)value).intValue();
            }
        
            return super.putField(name, value);
        }
    }
    
    // call onScroll in navmode.js
    public void triggerNavigationDirection( final int direction, final int delta ) {
        if( _onScroll == null ) {
            return;
        }

        new Thread() {
            public void run() {
                try {
                    NavigationData data = new NavigationData();
                    data.putField( NavigationData.LABEL_DIRECTION, new Integer( direction ) );
                    data.putField( NavigationData.LABEL_DELTA, new Integer( delta ) );
                    data.putField( NavigationData.LABEL_ZOOMSCALE, new Double( _fieldManager.getZoomScale() ) );
                    data.putField( NavigationData.LABEL_VIRTUALHEIGHT, new Integer( _fieldManager.getVirtualHeight() ) );
                    data.putField( NavigationData.LABEL_VIRTUALWIDTH, new Integer( _fieldManager.getVirtualWidth() ) );
                    data.putField( NavigationData.LABEL_VERTICALSCROLL, new Integer( _fieldManager.getVerticalScroll() ) );
                    data.putField( NavigationData.LABEL_HORIZONTALSCROLL, new Integer( _fieldManager.getHorizontalScroll() ) );
                    data.putField( NavigationData.LABEL_HEIGHT, new Integer( _fieldManager.getHeight() ) );
                    data.putField( NavigationData.LABEL_WIDTH, new Integer( _fieldManager.getWidth() ) );

                    Object[] result = new Object[ 1 ];
                    result[ 0 ] = data;

                    // Pass the event back to the JavaScript callback
                    ScriptableFunction onScroll = _onScroll;
                    onScroll.invoke( onScroll, result );
                } catch( Exception e ) {
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }.start();
    }
    
    // call onMouseDown in navmode.js    
    public void triggerNavigationMouseDown() {
        if( _onTrackpadDown == null ) {
            return;
        }

        new Thread() {
            public void run() {
                try {
                    Object[] result = new Object[ 0 ];

                    // Pass the event back to the JavaScript callback
                    ScriptableFunction onMouseDown = _onTrackpadDown;
                    onMouseDown.invoke( onMouseDown, result );
                } catch( Exception e ) {
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }.start();
    }
    
    // call onMouseUp in navmode.js    
    public void triggerNavigationMouseUp() {
        if( _onTrackpadUp == null ) {
            return;
        }

        new Thread() {
            public void run() {
                try {
                    Object[] result = new Object[ 0 ];

                    // Pass the event back to the JavaScript callback
                    ScriptableFunction onMouseUp = _onTrackpadUp;
                    onMouseUp.invoke( onMouseUp, result );
                } catch( Exception e ) {
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }.start();
    }   

    public void triggerNavigationFocusOut() {

        new Thread() {
            public void run() {
                try {
                    Object[] result = new Object[ 0 ];

                    // Pass the event back to the JavaScript callback
                    ScriptableFunction focusOut = _focusOut;
                    focusOut.invoke( focusOut, result );
                } catch( Exception e ) {
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }.start();
    }
}

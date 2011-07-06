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
package blackberry.app.event;

import java.util.Enumeration;
import java.util.Hashtable;

import blackberry.core.ApplicationEventHandler;
import blackberry.core.EventService;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

/**
 * blackberry.app.event namespace
 */
public class AppEventNamespace extends Scriptable {

    public static final String NAME = "event";

    private static final String FUNCTION_ON_EXIT = "onExit";
    private static final String FUNCTION_ON_BACKGROUND = "onBackground";
    private static final String FUNCTION_ON_FOREGROUND = "onForeground";

    private Hashtable _fields;

    /**
     * Constructs a AppEventNamespace object.
     */
    public AppEventNamespace() {
        _fields = new Hashtable();
        _fields.put( FUNCTION_ON_EXIT, new OnAppEventFunction( ApplicationEventHandler.EVT_APP_EXIT ) );
        _fields.put( FUNCTION_ON_BACKGROUND, new OnAppEventFunction( ApplicationEventHandler.EVT_APP_BACKGROUND ) );
        _fields.put( FUNCTION_ON_FOREGROUND, new OnAppEventFunction( ApplicationEventHandler.EVT_APP_FOREGROUND ) );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String fieldName ) throws Exception {
        Object field = _fields.get( fieldName );
        if( field == null ) {
            return UNDEFINED;
        }
        return field;
    }

    /**
     * Unload all event listeners
     * 
     * @throws Exception
     */
    public void unload() throws Exception {
        for( Enumeration e = _fields.elements(); e.hasMoreElements(); ) {
            OnAppEventFunction event = (OnAppEventFunction) e.nextElement();
            event.execute( null, new Object[] { null } );
        }
    }

    /**
     * Generic ScriptableFunction implementation to handle the callback event functions in blackberry.app.event
     */
    private static class OnAppEventFunction extends ScriptableFunctionBase implements ApplicationEventHandler {

        private int _eventId;
        private ScriptableFunction _func;

        public boolean handlePreEvent( int eventID, Object[] args ) {
            try {
                _func.invoke( null, args );
            } catch( Exception e ) {
                // do nothing
            }
            return true;
        }

        public void handleEvent( int eventID, Object[] args ) {
            try {
                _func.invoke( null, args );
            } catch( Exception e ) {
                // do nothing
            }
        }

        /**
         * Constructor
         * 
         * @param eventId
         *            An integer representing the event id that this callback function will support.
         */
        OnAppEventFunction( int eventId ) {
            _eventId = eventId;
        }

        /**
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        protected Object execute( Object thiz, Object[] args ) throws Exception {
            if( args[ 0 ] == null ) {
                EventService.getInstance().removeHandler( _eventId, this );
            } else {
                _func = (ScriptableFunction) args[ 0 ];
                EventService.getInstance().addHandler( _eventId, this );
            }
            return null;
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures();
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature( 1 );
            fs.addNullableParam( ScriptableFunction.class, true );
            return new FunctionSignature[] { fs };
        }
    }
}

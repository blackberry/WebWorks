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
package blackberry.media.camera;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.threading.CallbackDispatcherEvent;
import blackberry.media.JournalListener;
import blackberry.media.ProcessCheckThread;

/**
 * 
 * Implementation of function camera.takePicture
 * 
 */
public class TakePictureFunction extends ScriptableFunctionBase {
    public static final String NAME = "takePicture";

    private CameraNamespace _context;

    /**
     * Default constructor
     */
    public TakePictureFunction( CameraNamespace context ) {
        _context = context;
    }

    /**
     * Listens to the file system for changes when the camera takes a picture
     */
    private final class CameraJournalListener extends JournalListener {
        private ScriptableFunction _callback;
        private ScriptableFunction _errorCallback;

        public CameraJournalListener( ScriptableFunction callback, ScriptableFunction errorCallback ) {
            _callback = callback;
            _errorCallback = errorCallback;
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.JournalListener#pathMatches(java.lang.String)
         */
        protected boolean pathMatches( String path ) {
            return path.toLowerCase().endsWith( ".jpg" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.JournalListener#onFileAdded(java.lang.String)
         */
        protected void onFileAdded( String path ) {
            // invoke callback
            if( path.length() > 0 ) {
                try {
                    FileConnection conn = (FileConnection) Connector.open( "file://" + path );
                    if( conn.exists() ) {
                        // invoke callback for the file
                        new CallbackDispatcherEvent( _context, _callback, new Object[] { path } ).Dispatch();
                    } else {
                        handleError( _errorCallback, new FileNotFoundException( path ) );
                    }
                } catch( Exception e ) {
                    handleError( _errorCallback, e );
                }
            }
        }

    }

    /**
     * Thread to do cleanup operations if the camera is closed
     */
    private static class CameraCheckThread extends ProcessCheckThread {

        private CameraJournalListener _listener;
        private ScriptableFunction _closeCallback;

        public CameraCheckThread( CameraJournalListener listener, ScriptableFunction closeCallback ) {
            super( CameraNamespace.CAMERA_PROCESS );
            _listener = listener;
            _closeCallback = closeCallback;
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.ProcessCheckThread#processStarted()
         */
        protected void processStarted() {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.ProcessCheckThread#processExited()
         */
        protected void processExited() {
            UiApplication.getUiApplication().removeFileSystemJournalListener( _listener );
            if( _closeCallback != null ) {
                new CallbackDispatcherEvent( _closeCallback ).Dispatch();
            }
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {

        ScriptableFunction callback = (ScriptableFunction) args[ 0 ];
        ScriptableFunction onCloseCallback = null;
        ScriptableFunction onErrorCallback = null;

        if( args.length >= 2 ) {
            onCloseCallback = (ScriptableFunction) args[ 1 ];
        }

        if( args.length >= 3 ) {
            onErrorCallback = (ScriptableFunction) args[ 2 ];
        }

        try {
            CameraJournalListener listener = new CameraJournalListener( callback, onErrorCallback );
            CameraCheckThread checkThread = new CameraCheckThread( listener, onCloseCallback );

            UiApplication.getUiApplication().addFileSystemJournalListener( listener );
            checkThread.start();

            Invoke.invokeApplication( Invoke.APP_TYPE_CAMERA, new CameraArguments() );

        } catch( Exception e ) {
            handleError( onErrorCallback, e );
        }

        return UNDEFINED;
    }

    private void handleError( ScriptableFunction errorCallback, Exception e ) {
        if( errorCallback != null ) {
            new CallbackDispatcherEvent( errorCallback, new Object[] { e.getMessage() } ).Dispatch();
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        fs.addParam( ScriptableFunction.class, true );
        fs.addParam( ScriptableFunction.class, false );
        fs.addParam( ScriptableFunction.class, false );
        return new FunctionSignature[] { fs };
    }
}

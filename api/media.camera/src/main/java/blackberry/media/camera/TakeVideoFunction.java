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
 * Implementation of function camera.takeVideo
 * 
 */
public class TakeVideoFunction extends ScriptableFunctionBase {
    public static final String NAME = "takeVideo";

    private CameraNamespace _context;
    private String _currentVideoPath = "";

    /**
     * Default constructor
     */
    public TakeVideoFunction( CameraNamespace context ) {
        _context = context;
    }

    /**
     * Listens to the file system for changes when the camera takes a picture
     */
    private final class VideoJournalListener extends JournalListener {
        private ScriptableFunction _callback;
        private ScriptableFunction _errorCallback;

        public VideoJournalListener( ScriptableFunction callback, ScriptableFunction errorCallback ) {
            _callback = callback;
            _errorCallback = errorCallback;
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.JournalListener#pathMatches(java.lang.String)
         */
        protected boolean pathMatches( String path ) {
            return path.toLowerCase().endsWith( ".3gp" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.JournalListener#onFileAdded(java.lang.String)
         */
        protected void onFileAdded( String path ) {

            /*
             * may need wait till the next video is recording to detect if the previous finished prior to dispatching the callback
             */
            String oldPath;
            synchronized( _currentVideoPath ) {
                oldPath = _currentVideoPath;
                _currentVideoPath = path;
            }

            // start a new video invoke callback for the old file
            dispatchFile( oldPath );
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.JournalListener#onFileRenamed(java.lang.String, java.lang.String)
         */
        protected void onFileRenamed( String oldPath, String newPath ) {
            // if video becomes available on a rename ( *.3gp.lock -> *.3gp) send it right away
            dispatchFile( newPath );
        }

        private void dispatchFile( String path ) {
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
     * Thread to do cleanup operations if the video camera is closed
     */
    private class VideoCheckThread extends ProcessCheckThread {
        private VideoJournalListener _listener;
        private ScriptableFunction _callback;
        private ScriptableFunction _closeCallback;

        public VideoCheckThread( VideoJournalListener listener, ScriptableFunction callback, ScriptableFunction closeCallback ) {
            super( CameraNamespace.VIDEORECORDER_PROCESS );
            _listener = listener;
            _callback = callback;
            _closeCallback = closeCallback;
        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.ProcessCheckThread#processStarted()
         */
        protected void processStarted() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see blackberry.media.ProcessCheckThread#processExited()
         */
        protected void processExited() {
            UiApplication.getUiApplication().removeFileSystemJournalListener( _listener );

            // No need to synchronize _currentVideoPath on exit?
            if( _currentVideoPath.length() > 0 ) {
                // invoke the video callback for the path we tracked previously
                new CallbackDispatcherEvent( _context, _callback, new Object[] { _currentVideoPath } ).Dispatch();
            }
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
            _currentVideoPath = "";
            VideoJournalListener listener = new VideoJournalListener( callback, onErrorCallback );
            VideoCheckThread checkThread = new VideoCheckThread( listener, callback, onCloseCallback );

            UiApplication.getUiApplication().addFileSystemJournalListener( listener );
            checkThread.start();

            Invoke.invokeApplication( Invoke.APP_TYPE_CAMERA, new CameraArguments( CameraArguments.ARG_VIDEO_RECORDER ) );
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

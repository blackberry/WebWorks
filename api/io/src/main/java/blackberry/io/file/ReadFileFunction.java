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
package blackberry.io.file;

import java.io.DataInputStream;
import java.io.IOException;

import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.WidgetBlob;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - reads a file and returns a JS callback to allow
 * users to retrieve the data
 */
public final class ReadFileFunction extends ScriptableFunctionBase {

    public static final String NAME = "readFile";

    /**
     * Read a given file and call the callback function after finishing reading.
     * The size of the file cannot exceed 2147483647 bytes (Max value of int).
     * The JavaScript callback function will be provided two parameters. The
     * first is a string representing the full path of the file. The second is
     * the Blob that contains the contents of the file opened.
     * 
     * @param args
     *            args[0]: A complete URL path to a file args[1]: A call back
     *            function (ScriptableFunction) args[2]: true if read should be
     *            asynchronous, false otherwise
     * 
     *            IOException will be thrown if 1. src file does not yet exist,
     *            or the src path is to an directory; if reading is asynchronous, 
     *            no exceptions will be thrown when an error occurs
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
        fConnWrap.openDataInputStream();

        if( args.length == 2 || ( args.length == 3 && ( (Boolean) args[ 2 ] ).booleanValue() ) ) { // Async read
            new Thread( new AsyncRead( this, fConnWrap, args[ 0 ].toString(), (ScriptableFunction) args[ 1 ] ) ).start();
        } else { // Sync read
            read( this, fConnWrap, args[ 0 ].toString(), (ScriptableFunction) args[ 1 ] );
        }
        return UNDEFINED;
    }

    private void read( Object thiz, FileConnectionWrapper fConnWrap, String path,
            ScriptableFunction func ) throws Exception {
        try {
            DataInputStream dis = fConnWrap.getDataInputStream();
            int size = (int) fConnWrap.getFileConnection().fileSize();
            byte[] data = new byte[ size ];

            int numBytesRead;
            try {
                numBytesRead = dis.read( data, 0, size );
            } catch ( IOException ioe ) {
                throw new Exception( "Exception reading file" );
            }
            
            if( numBytesRead != size ) {
                throw new Exception( "File size does not match number of bytes read." );
            }

            Object[] args = new Object[ 2 ];
            args[ 0 ] = path;
            args[ 1 ] = new WidgetBlob( data );

            func.invoke( thiz, args );
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        fs.addParam( String.class, true );
        fs.addParam( ScriptableFunction.class, true );
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * Class provides async read functionality
     */
    private class AsyncRead implements Runnable {
        private FileConnectionWrapper _fConnWrap;
        private Object _thiz;
        private String _path;
        private ScriptableFunction _callBackFunc;

        /**
         * Constructor
         * 
         * @param thiz
         * @param fConnWrap
         * @param path
         * @param func
         */
        public AsyncRead( Object thiz, FileConnectionWrapper fConnWrap, String path,
                ScriptableFunction func ) {
            _thiz = thiz;
            _fConnWrap = fConnWrap;
            _path = path;
            _callBackFunc = func;
        }

        /**
         * @see Runnable#run
         */
        public void run() {
            try {
                read( _thiz, _fConnWrap, _path, _callBackFunc );
            } catch( Exception ignore ) {
            }
        }
    }
}

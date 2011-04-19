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
package blackberry.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.file.FileIOException;

/**
 * Wrapper class that abstracts the file connection and stream functionality
 */
public class FileConnectionWrapper {

    private FileConnection _fileConnection;
    private DataInputStream _dataInputStream;
    private DataOutputStream _dataOutputStream;

    /**
     * Constructor
     * 
     * @param path
     *            directory/file to create connection to
     * 
     * @throws IOException
     *             throws an IOException for invalid path values
     * 
     */
    public FileConnectionWrapper( String path ) throws IOException {

        Connection con = Connector.open( path );

        if( con instanceof FileConnection ) {
            _fileConnection = (FileConnection) con;
        } else {
            // not a file connection - throw exception
            if( con != null ) {
                try {
                    con.close();
                } catch( Exception e ) {
                }
            }
            throw new FileIOException( FileIOException.INVALID_PARAMETER );
        }
    }

    /**
     * Retrieves the underlying FileConnection class
     * 
     * @return underlying FileConnection class
     */
    public FileConnection getFileConnection() {
        return _fileConnection;
    }

    /**
     * Opens a data input stream from the wrapped file connection
     * 
     * @return returns an open input data stream
     * 
     * @throws IOException
     *             throws an I/O exception if there is a problem opening the stream
     */
    public DataInputStream openDataInputStream() throws IOException {
        if( this._dataInputStream != null ) {
            _dataInputStream.close();
        }

        _dataInputStream = _fileConnection.openDataInputStream();

        return _dataInputStream;
    }

    /**
     * Opens a data output stream from the wrapped file connection
     * 
     * @return returns an open output data stream
     * 
     * @throws IOException
     *             throws an I/O exception if there is a problem opening the stream
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        if( this._dataOutputStream != null ) {
            _dataOutputStream.close();
        }

        _dataOutputStream = _fileConnection.openDataOutputStream();

        return _dataOutputStream;
    }

    /**
     * Retrieves the underlying DataOutputStream class
     * 
     * @return the underlying DataOutputStream
     */
    public DataOutputStream getDataOutputStream() {
        return _dataOutputStream;
    }

    /**
     * Retrieves the underlying DataInputStream class
     * 
     * @return the underlying DataInputStream
     */
    public DataInputStream getDataInputStream() {
        return _dataInputStream;
    }

    /**
     * @see javax.microedition.io.file.FileConnection#delete
     */
    public void delete() throws IOException {
        _fileConnection.delete();
    }

    /**
     * @see javax.microedition.io.file.FileConnection#isDirectory
     */
    public boolean isDirectory() {
        return _fileConnection.isDirectory();
    }

    /**
     * @see javax.microedition.io.file.FileConnection#exists
     */
    public boolean exists() {
        return _fileConnection.exists();
    }

    /**
     * @see javax.microedition.io.file.FileConnection#list
     */
    public Enumeration list() throws IOException {
        return _fileConnection.list();
    }

    /**
     * @see javax.microedition.io.file.FileConnection#create
     */
    public void create() throws IOException {
        _fileConnection.create();
    }

    /**
     * @see javax.microedition.io.file.FileConnection#close
     */
    public void close() {
        try {
            if( _fileConnection != null ) {
                _fileConnection.close();
            }
        } catch( IOException ioe ) {
        }

        try {
            if( _dataInputStream != null ) {
                _dataInputStream.close();
            }
        } catch( IOException ioe ) {
        }

        try {
            if( _dataOutputStream != null ) {
                _dataOutputStream.close();
            }
        } catch( IOException ioe ) {
        }
    }
}

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
package blackberry.common.push;

import java.io.InputStream;
import java.io.IOException;

import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.http.HttpServerConnection;
import net.rim.device.api.io.http.MDSPushInputStream;
import net.rim.device.api.io.http.PushInputStream;

/**
 * Encapsulates an individual push message received from an open push channel.
 */
public final class PushData {

    private boolean _isChannelEncrypted;
    private byte[] _payload;
    private HttpServerConnection _httpConnection;
    private String _source;
    private InputStream _input;
    private PushInputStream _pushStream;
    private boolean _ack;

    /**
     * Constructor
     * 
     * @param conn
     *            StreamConnection that this message was received on.
     * @param input
     *            InputSteram this message was received on.
     * @param payload
     *            The byte array payload of this message.
     */
    public PushData( StreamConnection conn, InputStream input, byte[] payload ) {
        _httpConnection = (HttpServerConnection) conn;
        _input = input;
        _payload = payload;
        _pushStream = createPushInputStream( conn, input );
        _isChannelEncrypted = _pushStream.isChannelEncrypted();
        _source = _pushStream.getSource();
    }

    /**
     * Sends a response to the sender of the push message indicating the message has been accepted.
     */
    public void accept() throws IOException {
        if( !_ack ) {
            _pushStream.accept();
            _ack = true;
        }
    }

    /**
     * Sends a response to the sender of the push message indicating the message has been declined with the given reason.
     * 
     * @param reason
     *            The reason the message was declined - see net.rim.device.api.io.http.PushInputStream
     */
    public void decline( int reason ) throws IOException {
        if( !_ack ) {
            _pushStream.decline( reason );
            _ack = true;
        }
    }

    /**
     * Gets a header field key by index.
     * 
     * @param index
     *            the index of the header field.
     * @return the key of the nth header field or null if the array index is out of range.
     */
    public String getHeaderField( int index ) throws IOException {
        return _httpConnection.getHeaderField( index );
    }

    /**
     * Returns the value of the named header field.
     * 
     * @param field
     *            of a header field.
     * @return the value of the named header field, or null if there is no such field in the header.
     */
    public String getHeaderField( String field ) throws IOException {
        return _httpConnection.getHeaderField( field );
    }

    /**
     * Returns the requested URI.
     * 
     * @return Return the requested URI.
     */
    public String getRequestURI() throws IOException {
        return _httpConnection.getRequestURI();
    }

    /**
     * Retrieves the source for this push stream.
     * 
     * @return String form of the source of this push; it could be a UID for a connection, IPv4 address, or SMSC.
     */
    public String getSource() {
        return _source;
    }

    /**
     * Determines if the channel is encrypted.
     * 
     * @return True if this channel is encrypted; otherwise, false.
     */
    public boolean isChannelEncrypted() {
        return _isChannelEncrypted;
    }

    /**
     * Returns the payload of this message as a byte-array.
     * 
     * @return the payload of this push message.
     */
    public byte[] getPayload() {
        return _payload;
    }

    /**
     * Cleans up this message - calling this method will cause other methods for this object to throw exceptions if called
     * afterwards.
     */
    public void discard() {
        try {
            _input.close();
        } catch( IOException ioe ) {
        }
        try {
            _httpConnection.close();
        } catch( IOException ioe ) {
        }
    }

    /**
     * Creates a PushInputStream based on the transport type.
     * 
     * @param stream
     *            the StreamConnection for the message.
     * @param input
     *            the InputStream for this message.
     * @return an implementation of PushInputStream.
     */
    private PushInputStream createPushInputStream( StreamConnection stream, InputStream input ) {
        PushInputStream result = new MDSPushInputStream( (HttpServerConnection) stream, input );
        return result;
    }
}

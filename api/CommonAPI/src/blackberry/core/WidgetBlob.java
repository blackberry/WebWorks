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
package blackberry.core;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

import blackberry.core.Blob;

/**
 * A WidgetBlob is a reference to an opaque block of binary data.
 * 
 * Blobs are a general-purpose interchange format. They can be passed into, and returned by, a variety of Gears methods. Blobs
 * are, in many ways, parallel to Strings. Most APIs that accept a 'String' could be updated to accept 'String-or-Blob'.
 * 
 * Blobs are immutable. The binary data referenced by a Blob cannot be modified directly. (This guarantees Blobs will have
 * predictable behavior when passed to asynchronous APIs.) In practice, this is not a limitation; APIs can accept Blobs and return
 * new Blobs. Note that JavaScript strings are also immutable; they behave the same way.
 */
public final class WidgetBlob extends Scriptable implements Blob {

    // Content of the blob as byte array.
    private final byte[] _bytes;
    private final int _size;
    private Hashtable _fields;

    /**
     * Constructs a Blob object.
     * 
     * @param bytes
     *            The contents of the blob.
     */
    public WidgetBlob( byte[] bytes ) {
        // The byte array is NOT copied intentionally, as Gears fully controls
        // the construction of the Blob objects.
        _bytes = bytes;
        _size = bytes.length;

        // add JS fields/methods - length, slice(), getBytes()
        _fields = new Hashtable();
        _fields.put( "length", new Integer( _size ) );
        _fields.put( "getBytes", new GetBytesFunction() );
        _fields.put( "slice", new SliceFunction() );
    }

    /**
     * @see blackberry.core.Blob
     */
    public Blob slice( int offset, int length ) {
        if( _bytes.length < offset + length ) {
            throw new IllegalArgumentException( "length of the byte array must be >= offset + length" );
        }

        if( offset == 0 && length == _bytes.length ) {
            return this;
        } else {
            byte[] bytes = new byte[ length ];
            System.arraycopy( _bytes, offset, bytes, 0, length );
            return new WidgetBlob( bytes );
        }
    }

    /**
     * @see blackberry.core.Blob
     */
    public int size() {
        return _size;
    }

    /**
     * @see blackberry.core.Blob
     */
    public byte[] getBytes() {
        return _bytes;
    }

    /**
     * see net.rim.device.api.script.Scriptable#getElementCount()
     */
    public int getElementCount() {
        return _fields.size();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#enumerateFields(Vector)
     */
    public void enumerateFields( Vector v ) {
        if( !_fields.isEmpty() ) {
            for( Enumeration e = _fields.keys(); e.hasMoreElements(); ) {
                v.addElement( e.nextElement() );
            }
        }
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        Object field = _fields.get( name );
        if( field == null ) {
            return UNDEFINED;
        }
        return field;
    }

    /**
     * Scriptable function wrapper class for getBytes() method.
     */
    private static class GetBytesFunction extends ScriptableFunctionBase {
        /**
         * Returns the bytes (as integers in the range 0-255) of a slice of the Blob.
         * 
         * Parameters: offset - Optional. The position of the first byte to return. The default value is zero. length - Optional.
         * The number of bytes to return. The default value means to the end of the Blob.
         * 
         * Return: An integer array containing the Blob's bytes.
         */
        public Object execute( Object thiz, Object[] args ) throws Exception {
            // Perform action
            byte[] blobBytes = ( (Blob) thiz ).getBytes();
            Integer[] result = new Integer[ blobBytes.length ];

            // populate int array
            for( int i = 0; i < result.length; i++ ) {
                result[ i ] = new Integer( (int) blobBytes[ i ] & 0xFF );
            }

            return result;
        }

        /**
         * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
         */
        protected FunctionSignature[] getFunctionSignatures() {
            return null;
        }
    }

    /**
     * Scriptable function wrapper class for slice() method.
     */
    private static class SliceFunction extends ScriptableFunctionBase {
        /**
         * Extracts a subset of the current Blob and returns it as a new Blob.
         * 
         * Parameters: offset - The position of the first byte to extract. length - Optional. The number of bytes to extract. The
         * default value means to the end of the Blob.
         * 
         * Return: A new Blob containing the specified subset.
         */
        public Object execute( Object thiz, Object[] args ) throws Exception {
            int offset = ( (Integer) args[ 0 ] ).intValue();
            int length = 0;
            if( args.length == 2 ) {
                length = ( (Integer) args[ 1 ] ).intValue();
            } else {
                length = ( (Blob) thiz ).size();
            }

            // validate non-negative
            if( offset < 0 ) {
                throw new IllegalArgumentException( "offset must be non-negative integer" );
            }
            if( length < 0 ) {
                throw new IllegalArgumentException( "length must be non-negative integer" );
            }

            // Perform action
            return ( (Blob) thiz ).slice( offset, length );
        }

        /**
         * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature( 2 );
            fs.addParam( Integer.class, true );
            fs.addParam( Integer.class, false );
            return new FunctionSignature[] { fs };
        }
    }
}

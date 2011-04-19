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
package blackberry.web.widget.caching;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ByteVector;
import net.rim.device.api.util.Persistable;

public class CacheItem implements Persistable {
    private String _url;
    private long _expires;
    private int _size;
    private int _fileSize;
    private long _storeKey;

    public CacheItem( long storeKey, String url, long expires, int size, int fileSize ) {
        _storeKey = storeKey;
        _url = url;
        _expires = expires;
        _size = size;
        _fileSize = fileSize;
    }

    public String getUrl() {
        return _url;
    }

    public long getExpires() {
        return _expires;
    }

    public int getSize() {
        return _fileSize;
    }

    public long getStoreKey() {
        return _storeKey;
    }

    /**
     * Obtain data from the cached item.
     * 
     * @return Data from the cached item, or null on error.
     */
    public byte[] getData() {
        byte[] data = null;

        // Get the code signing key associated with this BlackBerry WebWorks Application
        CodeSigningKey codeSigningKey = CodeSigningKey.get( this );
        // Check Persistent Store for existing data
        PersistentObject cacheItemStore = PersistentStore.getPersistentObject( _storeKey );        

        // If we find an entry in the Persistent store
        if( cacheItemStore != null ) {
            Object cacheItemObj = null;
            try {
            	// codeSigningKey is nullable
                cacheItemObj = cacheItemStore.getContents( codeSigningKey );
            } catch ( ControlledAccessException ignore ) {
                // cacheItemObj remains null
            }
            if( cacheItemObj instanceof ByteVector ) {
                ByteVector cacheItemVector = (ByteVector) cacheItemObj;
                data = cacheItemVector.getArray();
            }
        }

        if( data != null ) {
            // Create InputStream
            ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
            CacheManager.receiveLine( dataStream );
            CacheManager.receiveLine( dataStream );
            CacheManager.receiveLine( dataStream );

            // Read headers
            while( true ) {
                String line = CacheManager.receiveLine( dataStream );

                // Headers end with double CRLF
                if( line.length() == 0 ) {
                    break;
                }
            }

            // Read data
            try {
                data = readDataFromStore( dataStream );
            } catch ( IOException ignore ) {
                // data remains null
            }
            if( data != null && data.length != _size ) {
                data = null;
            }
        }

        return data;
    }

    /**
     * @return May return null
     * 
     * @throws IOException
     */
    private byte[] readDataFromStore( ByteArrayInputStream dataInputStream ) throws IOException {
        // Check whether it's compressed or not
        String lineCompressFlag = CacheManager.receiveLine( dataInputStream );
        int compressed = -1;
        try {
            compressed = Integer.parseInt( lineCompressFlag );
        } catch( NumberFormatException ignore ) {
            // compressed will remain -1
        }

        if( compressed < 0 ) {
            throw new IOException();
        }

        boolean bCompressed = ( compressed == 1 );

        // Read compressed size
        String lineSize = CacheManager.receiveLine( dataInputStream );
        int size = -1;
        try {
            size = Integer.parseInt( lineSize );
        } catch( NumberFormatException ignore ) {
            // size will remain -1
        }

        if( size <= 0 ) {
            throw new IOException();
        }

        byte[] data = new byte[ size ];
        int read = dataInputStream.read( data );
        if( read != size ) {
            throw new IOException();
        }

        if( !bCompressed ) {
            return data;
        }

        GZIPInputStream gin = new GZIPInputStream( new ByteArrayInputStream( data ) );
        return IOUtilities.streamToBytes( gin ); // Return original data, or null
    }
    
    /**
     * Obtain headers.
     * 
     * @return HttpHeaders from the cached item, or null on error.
     */
    public HttpHeaders getHeaders() {
        HttpHeaders headers = null;
        byte[] data = null;

        // Get the code signing key associated with this BlackBerry WebWorks Application
        CodeSigningKey codeSigningKey = CodeSigningKey.get( this );
        // Check Persistent Store for existing data
        PersistentObject cacheItemStore = PersistentStore.getPersistentObject( _storeKey );        

        // If we find an entry in the Persistent store
        if( cacheItemStore != null ) {
            Object cacheItemObj = null;
            try {
            	// codeSigningKey is nullable            	
                cacheItemObj = cacheItemStore.getContents( codeSigningKey );
            } catch ( ControlledAccessException ignore ) {
                // cacheItemObj remains null
            }
            if( cacheItemObj instanceof ByteVector ) {
                ByteVector cacheItemVector = (ByteVector) cacheItemObj;
                data = cacheItemVector.getArray();
            }
        }
        
        if( data != null ) {
            // Create InputStream
            ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
            CacheManager.receiveLine( dataStream );
            CacheManager.receiveLine( dataStream );
            CacheManager.receiveLine( dataStream );
    
            // Read headers
            headers = new HttpHeaders();
            String line = null;
            while( true ) {
                line = CacheManager.receiveLine( dataStream );
    
                // Headers end with double CRLF
                if( line.length() == 0 ) {
                    break;
                }
    
                try {
                    int indexOfColon = line.indexOf( ':' );
                    if( indexOfColon != -1 ) {
                        headers.setProperty( line.substring( 0, indexOfColon ).trim(), 
                                line.substring( indexOfColon + 1 ).trim() );
                    } else {
                        // Drop the header
                    }
                } catch( IndexOutOfBoundsException ignore ) {
                    // Drop the header
                }
            }
        }

        return headers;
    }
}

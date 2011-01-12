/*
 * CacheItem.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2010-2011
 */

package blackberry.web.widget.caching;

import javax.microedition.io.InputConnection;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.http.HttpHeaders;

 
import java.util.Date;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.rim.device.api.io.IOUtilities;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.CodeSigningKey;

import net.rim.device.api.util.ByteVector;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Persistable;

public class CacheItem implements Persistable{   
    private String  _url;
    private long    _expires;
    private int     _size;
    private int     _fileSize;
    private long	_storeKey;

    public CacheItem(long storeKey, String url, long expires, int size, int fileSize) {
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
        
    public byte[] getData() {
        byte[] data = null;       

        try {                  	
        	 // Check Persistent Store for existing data
            PersistentObject cacheItemStore = PersistentStore.getPersistentObject(_storeKey);
            
            // Get the code signing key associated with this BlackBerry WebWorks Application
            CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
            
            // If we find an entry in the Persistent store              
            if(cacheItemStore != null){
                Object cacheItemObj = cacheItemStore.getContents(codeSigningKey);
                if(cacheItemObj instanceof ByteVector){ 
                    ByteVector cacheItemVector  = (ByteVector) cacheItemObj;
                    data = cacheItemVector.getArray();
                }
            }
            
            // Create InputStream
            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
            
            // read URL
            String url = CacheManager.receiveLine(dataStream);

            // read expires
            String lineExpires = CacheManager.receiveLine(dataStream);
    
            // read size
            String lineSize = CacheManager.receiveLine(dataStream);
            
            // read headers
            while (true) {
                String line = CacheManager.receiveLine(dataStream);
                
                // headers end with double CRLF
                if (line.length() == 0) {
                    break;
                }
            }
            
            // read data
            data = readDataFromStore(dataStream);
            if (data.length != _size) {
                data = null;
            }        
        } catch (Exception e) {
        	
        }
       
        
        return data;
    }
    
    private byte[] readDataFromStore(ByteArrayInputStream dataInputStream) throws IOException {
        // check whether it's compressed or not
        String lineCompressFlag = CacheManager.receiveLine(dataInputStream);
        int compressed = -1;
           try {
               compressed = Integer.parseInt(lineCompressFlag);
           } catch (NumberFormatException nfe) {
           }
           
           if (compressed < 0) {
               throw new IOException();
           }
           
           boolean bCompressed = (compressed == 1);
           
           // read compressed size
        String lineSize = CacheManager.receiveLine(dataInputStream);
        int size = -1;
           try {
               size = Integer.parseInt(lineSize);
           } catch (NumberFormatException nfe) {
           }
           
           if (size <= 0) {
               throw new IOException();
           }
           
           byte[] data = new byte[size];
        int read = dataInputStream.read(data);
           if (read != size) {
               throw new IOException();
           }
        
           if (!bCompressed) {
               return data;
           }
           
           GZIPInputStream gin = new GZIPInputStream(new ByteArrayInputStream(data));
           byte[] originalData = IOUtilities.streamToBytes(gin);
           return originalData;
       }
        
    public HttpHeaders getHeaders() {
        HttpHeaders headers = null;        
        byte[] data = null;       

        try {
        	
        	// Check Persistent Store for existing data
            PersistentObject cacheItemStore = PersistentStore.getPersistentObject(_storeKey);
            
            // Get the code signing key associated with this BlackBerry WebWorks Application
            CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
            
            // If we find an entry in the Persistent store              
            if(cacheItemStore != null){
                Object cacheItemObj = cacheItemStore.getContents(codeSigningKey);
                if(cacheItemObj instanceof ByteVector){ 
                    ByteVector cacheItemVector  = (ByteVector) cacheItemObj;
                    data = cacheItemVector.getArray();
                }
            }
            
            // Create InputStream
            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);            
            
            // read URL
            String url = CacheManager.receiveLine(dataStream);

            // read expires
            String lineExpires = CacheManager.receiveLine(dataStream);
    
            // read size
            String lineSize = CacheManager.receiveLine(dataStream);
            
            // read headers
            headers = new HttpHeaders();
            String line = null;
            while (true) {
                line = CacheManager.receiveLine(dataStream);
                
                // headers end with double CRLF
                if (line.length() == 0) {
                    break;
                }
    
                try {
                    int indexOfColon = line.indexOf(':');
                    if( indexOfColon != -1 ) {
                        headers.setProperty(line.substring(0,indexOfColon).trim(), line.substring(indexOfColon+1).trim());
                    } else {
                        // DROP the header.
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new IOException( e.toString());
                }
            }
        } catch (IOException ioe) {
        } catch (Exception e) {
        } 
        
        return headers;
    }
}

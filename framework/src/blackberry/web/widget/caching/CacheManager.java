/*
* Copyright 2010 Research In Motion Limited.
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

import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.util.WidgetUtil;

import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResponse;

import javax.microedition.io.InputConnection;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.http.HttpHeaders;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.HttpConnection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.util.Date;
import net.rim.device.api.io.URI;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.CodeSigningKey;

import net.rim.device.api.util.ByteVector; 

public class CacheManager implements HttpProtocolConstants {
    // Constants
    private static String CRLF = "\r\n";
    private static String HTTP_HEADER_FIELD_SEPARATOR = ":";
    private static String HTTP_HEADER_VALUE_SEPARATOR = ",";
    private static String HTTP_HEADER_SINGLE_SPACE = " ";    
    
    private static String WIDGETCACHEROOT = "file:///store/home/user/cache/";
    private static final int MAX_STANDARD_CACHE_AGE = 2592000;
    
    private String _cacheStoreRoot;
    private WidgetConfigImpl _widgetConfigImpl;
    private Hashtable _cacheTable;
    private long _storeKey;
    
    public CacheManager(WidgetConfigImpl widgetConfigImpl) {
        _widgetConfigImpl = widgetConfigImpl;
        String author = (widgetConfigImpl.getAuthor() == null)? "" : widgetConfigImpl.getAuthor();
        _cacheStoreRoot = WIDGETCACHEROOT + Integer.toHexString(widgetConfigImpl.getName().hashCode()) + Integer.toHexString(widgetConfigImpl.getVersion().hashCode()) + Integer.toHexString(author.hashCode()) + "/";
        _cacheTable = new Hashtable();
        populateCacheTable();
    }
    
    private void populateCacheTable() {
        long now = (new Date()).getTime();
        try {
            // Generate store key for this app.
            _storeKey = generateStoreKeyFromPackageName();         
           
           // Check Persistent Store for existing cacheTable data.
           PersistentObject cacheTableStore = PersistentStore.getPersistentObject(_storeKey);
           
           // Get the code signing key associated with this widget.
           CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
           Object cacheTableObj = cacheTableStore.getContents(codeSigningKey);
           
           // If we find an entry in the Persistent store.    
           if(cacheTableObj != null){
               
               if(cacheTableObj instanceof Hashtable){
                   // Set the cache table using the stored value.
                   _cacheTable  = (Hashtable) cacheTableObj;
                   
                   // Ensure that expired entries are cleaned out.
                   cleanExpiredCache();
               }
           }
           // Otherwise, create the cacheTable entry in persistent store.
           else{
                        synchronized(cacheTableStore){
                                cacheTableStore.setContents(new ControlledAccess(_cacheTable, codeSigningKey));
                                cacheTableStore.commit();
                        }
           }
         
        } catch (Exception e) {
        }
    }
    
    private void addCacheItem(CacheItem ci) {
        if (ci != null) {
            synchronized(_cacheTable) {
                _cacheTable.put(ci.getUrl(), ci);
                
                int totalSize = getTotalCacheSize();
                if (totalSize > _widgetConfigImpl.getMaxCacheSize()) {
                    refreshCache(totalSize - _widgetConfigImpl.getMaxCacheSize());
                }
                
                
                
            }
            
            // Update cache table in persistent store.
            PersistentObject cacheTableStore = PersistentStore.getPersistentObject(_storeKey);
            CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
            synchronized(cacheTableStore){
                cacheTableStore.setContents(new ControlledAccess(_cacheTable, codeSigningKey));
                cacheTableStore.commit();
            }
        }
    }
    
    private void refreshCache(int spaceToFree) {
        Vector itemsFoundSorted = new Vector();
        int sizeFound = 0;
        synchronized(_cacheTable) {
            Enumeration e = _cacheTable.elements();
            while (e.hasMoreElements()) {
                CacheItem ci = (CacheItem) e.nextElement();

                boolean bFound = false;
                int size = itemsFoundSorted.size();
                for (int i = 0; i<size; i++) {
                    CacheItem ciFound = (CacheItem) itemsFoundSorted.elementAt(i);
                    if (ciFound.getExpires() > ci.getExpires()) {
                        itemsFoundSorted.insertElementAt(ci, i);
                        bFound = true;
                        break;
                    } else if (ciFound.getExpires() == ci.getExpires()) {
                        if (ciFound.getSize() < ci.getSize()) {
                            itemsFoundSorted.insertElementAt(ci, i);
                            bFound = true;
                            break;
                        }
                    }
                }
                
                if (!bFound && sizeFound >= spaceToFree) {
                } else {
                    if (!bFound) {
                        itemsFoundSorted.addElement(ci);
                    }

                    sizeFound = 0;
                    size = itemsFoundSorted.size();
                    for (int i = 0; i<size; i++) {
                        CacheItem ciFound = (CacheItem) itemsFoundSorted.elementAt(i);
                        sizeFound += ciFound.getSize();
                        if (sizeFound >= spaceToFree) {
                            itemsFoundSorted.setSize(i+1);
                            break;
                        }
                    }
                }
            }
            
            for (int i = 0; i<itemsFoundSorted.size(); i++) {
                clearCache(((CacheItem) itemsFoundSorted.elementAt(i)).getUrl());
            }
        }
    }
    
  
    
    private void removeCacheFile(long storeKey) {

        // Check Persistent Store for existing cacheTable data.
        PersistentObject cacheItemStore = PersistentStore.getPersistentObject(storeKey);
        
        // Get the code signing key associated with this widget.
        CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
        
        // If we find an entry in the Persistent store.    
        if(cacheItemStore != null){
            Object cacheItemObj = cacheItemStore.getContents(codeSigningKey);
            if(cacheItemObj instanceof ByteVectorWrapper){
                // Remove the entry.
                synchronized(cacheItemStore){
                        cacheItemStore.setContents(null);
                        cacheItemStore.commit();
                }
            }
        }        
        
    }
    
    private CacheItem writeCacheFile(String url, long expires, byte[] data, HttpHeaders headers) {
           String filePath = _cacheStoreRoot + Integer.toHexString(url.hashCode()) + Integer.toHexString(data.length);
           long storeKey = generateCacheItemStoreKey(filePath);           
           CacheItem ci = null;         
           ByteVectorWrapper pDataStore = null;           
           try {
                             
               // Open persistent store.
               // Use key to access store.
               PersistentObject persistentObject = PersistentStore.getPersistentObject(storeKey);
            
               // Get the code signing key associated with this widget.
               CodeSigningKey codeSigningKey = CodeSigningKey.get(this);
               
               // ByteVector is used to bypass persistent storage object size limits.
               pDataStore = new ByteVectorWrapper();

               // Write URL.
               writeToByteVector(url.getBytes(), pDataStore);
               writeToByteVector(CRLF.getBytes(), pDataStore);
               
               // Write expires.
               writeToByteVector((new Long(expires)).toString().getBytes(), pDataStore);
               writeToByteVector(CRLF.getBytes(), pDataStore);            
               
               // Write size.
               int size = data.length;
               writeToByteVector((new Integer(size)).toString().getBytes(), pDataStore);
               writeToByteVector(CRLF.getBytes(), pDataStore);               
               
               // Write headers.
               int xPropertiesSize = headers.size();
               for (int i=0; i < xPropertiesSize; i++) {                 
                   writeToByteVector(headers.getPropertyKey(i).getBytes(), pDataStore);
                   writeToByteVector(HTTP_HEADER_FIELD_SEPARATOR.getBytes(), pDataStore);
                   writeToByteVector(HTTP_HEADER_SINGLE_SPACE.getBytes(), pDataStore);
                   writeToByteVector(headers.getPropertyValue(i).getBytes(), pDataStore);
                   writeToByteVector(CRLF.getBytes(), pDataStore);                
               }

               writeToByteVector(CRLF.getBytes(), pDataStore);
               
               // Write data.
               writeDataToStore(pDataStore, data);                 
               
               // Save to Pstore.
               synchronized(persistentObject){
                   persistentObject.setContents(new ControlledAccess(pDataStore, codeSigningKey));
                   persistentObject.commit();
               }
                              
              // Create cache item using store key.
               ci = new CacheItem(storeKey, url, expires, size, pDataStore.size());        
        } catch (Exception e) {
        }         
        
        return ci;
    }
    
    private void writeDataToStore(ByteVectorWrapper dataVector, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream(baos, 6, GZIPOutputStream.MAX_LOG2_WINDOW_LENGTH);
        gzipStream.write(data);
        gzipStream.close();
        byte[] compressedData = baos.toByteArray();
        
        // Write compressed size
        int compressedSize = compressedData.length;
        int originalSize = data.length;
        
        if (compressedSize < originalSize * 0.95) {
            // If the compression ratio is greater than 95%            
                writeToByteVector((new Integer(1)).toString().getBytes(), dataVector);
                writeToByteVector(CRLF.getBytes(), dataVector);
                writeToByteVector((new Integer(compressedSize)).toString().getBytes(), dataVector);
                writeToByteVector(CRLF.getBytes(), dataVector);
                writeToByteVector(compressedData, dataVector);
        } else {
            // Compression ratio is not satisfactory, it's not necessary to store the compressed data but the original data                      
                writeToByteVector((new Integer(0)).toString().getBytes(), dataVector);
                writeToByteVector(CRLF.getBytes(), dataVector);
                writeToByteVector((new Integer(originalSize)).toString().getBytes(), dataVector);
                writeToByteVector(CRLF.getBytes(), dataVector);
                writeToByteVector(data, dataVector);
        }
    }
    
    private boolean isAggressivelyCaching() {
        return _widgetConfigImpl.getAggressivelyCaching();
    }
    
    private int getAggressiveCacheAge() {
        return _widgetConfigImpl.getAggressiveCacheAge();
    }

    private int getMaxCacheSize() {
        return _widgetConfigImpl.getMaxCacheSize();
    }
    
    public boolean isRequestCacheable(BrowserFieldRequest request) {
        // Only http request is cacheable.
        if (!request.getProtocol().equals("http")) {
            return false;
        }
        
        // Don't cache the request whose method is not "GET".
        if (request instanceof HttpConnection) {
            if (!((HttpConnection) request).getRequestMethod().equals("GET")) {
                return false;
            }
        }
        
        // Don't cache the request with post data.
        if (request.getPostData() != null) {
                return false;
        }
                
        // Don't cache authentication request.
        if (request.getHeaders().getPropertyValue("Authorization") != null) {
            return false;
        }
        
        // Check URI file types from config.xml.
        if (!isUriCacheable(request.getURL(), _widgetConfigImpl.getAllowedUriTypes())) {
            return false;
        }
        
        return true;
    }

    public boolean isResponseCacheable(HttpConnection response) {
        try {
            if (response.getResponseCode() != 200) {
                return false;
            }
        } catch (IOException ioe) {
            return false;
        }

        if (!response.getRequestMethod().equals("GET")) {
            return false;
        }
        
        if (containsPragmaNoCache(response)) {
            return false;
        }
        
        if (isExpired(response)) {
            return false;
        }
        
        if (containsCacheControlNoCache(response)) {
            return false;
        }
        
        if (containsNoContentLength(response)) {
            return false;
        }

        // Bypass size check if -1
        long size = getDataSize(response);
        long maxCacheable = _widgetConfigImpl.getMaxCacheable();
        long maxCacheSize = _widgetConfigImpl.getMaxCacheSize();
        
        if (maxCacheable!=-1 && (size > maxCacheable||size > maxCacheSize)) {
            return false;
        }

        long expires = getResponseExpires(response);
        if (expires <= 0) {
            if (!isAggressivelyCaching()) {
                return false;
            }
        }
        
        return true;
    }

    private boolean containsPragmaNoCache(HttpConnection response) {
        try {
            if (response.getHeaderField("pragma") != null && response.getHeaderField("pragma").toLowerCase().indexOf("no-cache") >= 0) {
                return true;
            } 
            
            return false;
        } catch (IOException ioe) {
            return true;
        }
    }

    private boolean isExpired(HttpConnection response) {
        try {
            long expires = response.getExpiration(); // getExpiration() returns 0 if not known
            if (expires > 0 && expires <= (new Date()).getTime()) {
                return true;
            }

            return false;
        } catch (IOException ioe) {
            return true;
        }
    }
    
    private boolean containsCacheControlNoCache(HttpConnection response) {
        try {
            String cacheControl = response.getHeaderField("cache-control");
            if (cacheControl != null) {
                cacheControl = removeSpace(cacheControl.toLowerCase());
                if (cacheControl.indexOf("no-cache") >= 0 
                    || cacheControl.indexOf("no-store") >= 0 
                    || cacheControl.indexOf("private") >= 0 
                    || cacheControl.indexOf("max-age=0") >= 0) {
                    return true;        
                }
                
                long maxAge = parseMaxAge(cacheControl);
                if (maxAge > 0 && response.getDate() > 0) {
                    long date = response.getDate();
                    long now = (new Date()).getTime();                    
                    if (now > date + maxAge) {
                        // Already expired
                        return true;
                    }
                }
            } 

            return false;
        } catch (IOException ioe) {
            return true;
        }
    }
    
    private boolean containsNoContentLength(HttpConnection response) {
        return (response.getLength() <= 0 );
    }
    
    private long getDataSize(HttpConnection response) {
        return response.getLength();
    }
    
    private long getResponseExpires(HttpConnection response) {
        try {
            // Calculate expires from "expires"
            long expires = response.getExpiration();
            if (expires > 0) {
                return expires;
            }
            
            // Calculate expires from "max-age" and "date"
            if (response.getHeaderField("cache-control") != null) {
                String cacheControl = removeSpace(response.getHeaderField("cache-control").toLowerCase());
                long maxAge = parseMaxAge(cacheControl);
                long date = response.getDate();
                
                if (maxAge > 0 && date > 0) {
                    return (date + maxAge);
                }
            }
        } catch (IOException ioe) {
        }
        
        return 0;
    }
    
    private long parseMaxAge(String cacheControl) {
        if (cacheControl == null) {
            return 0;
        }
        
        long maxAge = 0;
        if (cacheControl.indexOf("max-age=") >= 0) {
            int maxAgeStart = cacheControl.indexOf("max-age=") + 8;
            int maxAgeEnd = cacheControl.indexOf(',', maxAgeStart);
            if (maxAgeEnd < 0) {
                maxAgeEnd = cacheControl.length();
            }
            
            try {
                maxAge = Long.parseLong(cacheControl.substring(maxAgeStart, maxAgeEnd));
            } catch (NumberFormatException nfe) {
            }
        }
        
                // Multiply maxAge by 1000 to convert seconds to milliseconds
                maxAge *= 1000L;
        return maxAge;
    }
    
    private boolean containsCacheControlHeaders(HttpConnection response) {
        try {
            if (response.getHeaderField("pragma") != null && response.getHeaderField("pragma").toLowerCase().indexOf("no-cache") >= 0) {
                return true;
            }
    
            if (response.getHeaderField("expires") != null) {
                return true;
            }
            
            if (response.getHeaderField("cache-control") != null) {
                String cacheControl = response.getHeaderField("cache-control").toLowerCase();
                if (cacheControl.indexOf("no-cache") >= 0 
                || cacheControl.indexOf("no-store") >= 0 
                || cacheControl.indexOf("private") >= 0 
                || cacheControl.indexOf("max-age") >= 0) {
                    return true;
                }
            }
        } catch (IOException ioe) {
        }
        return false;
    }
    
    public InputConnection createCache(String url, HttpConnection response) {
        System.out.println("WIDGET ==> createCache: " + url);
        
        // Calculate expires
        long expires = calculateCacheExpires(response);
        
        // Copy headers
        HttpHeaders headers = copyResponseHeaders(response);
        
        // Read data
        byte[] data = null;
        InputStream is = null;
        try {
            int len = (int) response.getLength();
            if (len > 0) {
                is = response.openInputStream();
                int actual = 0;
                int bytesread = 0 ;
                data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                    actual = is.read(data, bytesread, len - bytesread);
                    bytesread += actual;
                }
            }
        } catch (IOException ioe) {
            data = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioe) {
                }
            } 
        }

        if (data == null) {
            return null;
        } 
        
        // Store the cache copy and create in-memory cache item
        CacheItem ci = writeCacheFile(url, expires, data, headers);
        if (ci != null) {
            System.out.println("WIDGET ==> cache created: " + url + " at " + ci.getStoreKey());
            addCacheItem(ci);
        } else {
            System.out.println("WIDGET ==> cache not created: " + url);
        }
        
        return new BrowserFieldResponse(url, data, headers);
    }
    
    private long calculateCacheExpires(HttpConnection response) {
        long date = 0;
        try {
            date = response.getDate();
        } catch (IOException ioe) {
        }
        
        if (date == 0) {
            date = (new Date()).getTime();
        }

        long expires = getResponseExpires(response);
        if (expires <= 0) {
            // Calculate the aggressive cache's expires based on AggressiveCacheAge
                /* Multiply the cache age value by 1000 to convert the value 
                   from seconds to milliseconds */
            expires = date + (_widgetConfigImpl.getAggressiveCacheAge() * 1000L);
        } else {
            // Check whether the cache's fresh age is overridden
            if (_widgetConfigImpl.getOverrodeAge() > 0 && expires > date + _widgetConfigImpl.getOverrodeAge()) {
                expires = date + _widgetConfigImpl.getOverrodeAge();
            }
        }
        
        // Do not allow the expires value to exceed the max allowed
        expires = Math.min(date + (MAX_STANDARD_CACHE_AGE * 1000L), expires);
        
        return expires;
    }
    
    private HttpHeaders copyResponseHeaders(HttpConnection response) {
        HttpHeaders headers = new HttpHeaders();
        try {
            int index = 0;
            while (response.getHeaderFieldKey(index) != null) {
                headers.addProperty(response.getHeaderFieldKey(index), response.getHeaderField(index));
                index++;
            }
        } catch (IOException ioe) {
        }
        
        return headers;
    }
    
    public boolean hasCache(String url) {
        boolean ret;
        synchronized(_cacheTable) {
            ret = _cacheTable.containsKey(url);
        }
        
        return ret;
    }
    
    public void clearCache(String url) {
        Object o ;
        synchronized(_cacheTable) {
            o = _cacheTable.get(url);
            _cacheTable.remove(url);
        }
        
        if (o instanceof CacheItem) {
            removeCacheFile(((CacheItem) o).getStoreKey());
        }
    }
    
    public void clearAll() {
        Vector files = new Vector();
        CacheItem nextElement = null;
        synchronized(_cacheTable) {
            Enumeration e = _cacheTable.elements();
            while (e.hasMoreElements()) {
                nextElement = (CacheItem) e.nextElement();
                files.addElement(new Long(nextElement.getStoreKey()));
            }
            
            
            _cacheTable.clear();
        }
        
        // File deletion doesn't require synchronization
        for (int i = 0; i<files.size(); i++) {
            removeCacheFile(((Long)files.elementAt(i)).longValue());
        }
    }
    
    public int getTotalCacheSize() {
        int size = 0;
        synchronized(_cacheTable) {
            Enumeration e = _cacheTable.elements();
            while (e.hasMoreElements()) {
                size += ((CacheItem) e.nextElement()).getSize();
            }
        }
        
        return size;
    }

    public ScriptableCacheItem[] getScriptableCacheItems() {
        int count = 0;
        ScriptableCacheItem[] items = null;
        
        synchronized(_cacheTable) {
            items = new ScriptableCacheItem[_cacheTable.size()];
            Enumeration e = _cacheTable.elements();
            while (e.hasMoreElements()) {
                CacheItem ci  = (CacheItem) e.nextElement();
                items[count] = new ScriptableCacheItem(ci.getUrl(), ci.getSize(), ci.getExpires());
                count++;
            }        
        }

        return items;
    }
    
    public boolean hasCacheExpired(String url) {
        Object o;
        synchronized(_cacheTable) {
            o = _cacheTable.get(url);
        }
        
        if (o instanceof CacheItem) {
            CacheItem ci = (CacheItem) o;
            long date = (new Date()).getTime();
            if (ci.getExpires() > date) {
                return false;
            } else {
                // Remove the expired cache item
                clearCache(url);
            }
        }
        
        return true;
    }
    
    public InputConnection getCache(String url) {
        Object o;
        synchronized(_cacheTable) {
            o = _cacheTable.get(url);
        }
        
        if (o instanceof CacheItem) {
            CacheItem ci = (CacheItem) o;
            HttpHeaders headers = ci.getHeaders();
            byte[] data = ci.getData();
            return new BrowserFieldResponse(url, (byte[]) data, (HttpHeaders) headers);
        }
        
        return null;
    }
    
    public static String receiveLine(InputStream inputStream) {
        int value;
        boolean shouldContinue = true;
            StringBuffer result= new StringBuffer();
            while ( shouldContinue ) {
                try {
                value = inputStream.read();
                switch((value)) {
                        case  -1:
                            // fall-through
                        case  10:
                            shouldContinue = false;
                            // fall-through
                        case  13: 
                            break;
                        default :
                            result.append((char)value);
                        }
                } catch (IOException e) {
                    return "";
                    }
            }
        return (result.toString().trim());
    }
    
    private static String removeSpace(String s) {
        StringBuffer result= new StringBuffer();
        int count = s.length();
        for (int i = 0; i < count; i++) {
            char c = s.charAt(i);
            if (c != ' ') {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private boolean isUriCacheable(String url, Hashtable filters) {
        if (filters == null) {
            return true;
        }
        
        URI uri = null;
        try {
            uri = URI.create(url.trim());
        } catch (MalformedURIException mue) {
            return false;
        }
        
        if (uri == null) {
            return false;
        }
        
        String file = URI.getFile(uri.getPath());
        int lastDot = file.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        } else {
            String ext = file.substring( lastDot + 1 );
            if (filters.containsKey(ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    private long generateStoreKeyFromPackageName(){
        long returnValue;
        String packageName = this.getClass().getName();    
        int hashcodeInt = packageName.hashCode();
        returnValue = Long.parseLong(Integer.toString(Math.abs(hashcodeInt)));
        
        return returnValue;     
    }
    
    private long generateCacheItemStoreKey(String filepath){
        long returnValue;
        int hashcodeInt = filepath.hashCode();
        returnValue = Long.parseLong(Integer.toString(Math.abs(hashcodeInt)));
        return returnValue;
    }
    
    
    private void cleanExpiredCache(){
        // Determine current time
        long now = (new Date()).getTime();
        
        // Go through all the elements in the cache table
        synchronized(_cacheTable) {
            Enumeration e = _cacheTable.elements();
            while (e.hasMoreElements()) {
                CacheItem ci = (CacheItem) e.nextElement();
                
                // Check the expiry date with the current time
                if (ci == null || ci.getExpires() <= now) {
                    // Remove cache file if invalid or expired
                    removeCacheFile(ci.getStoreKey());                    
                } 
            }            
        }
    }
    
    private void writeToByteVector(byte[] dataToWrite, ByteVectorWrapper vector){
        if(dataToWrite != null){
                for(int i = 0; i < dataToWrite.length; i++){
                        vector.addElement(dataToWrite[i]);
                }
        }       
    }
    
    /* Utility class to simulate consuming a stream */
    private static byte[] copyAndFrontTruncate(byte[] data, int numBytesToTruncate){
        byte[] tempArray = new byte[data.length - numBytesToTruncate];
        System.arraycopy(data, numBytesToTruncate, tempArray, 0, data.length - numBytesToTruncate);
        return tempArray;
    }
}
   

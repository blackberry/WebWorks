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
package net.rim.tumbler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;

public class WidgetArchive {
    private static final String[]           RESERVED_DIRS = new String[] { "src", "bin" };
    
    private String                          _archiveFile;
    private byte[]                          _configXML;
    private String                          _indexFile;
    private String                          _iconFile;

    public WidgetArchive(String widgetArchive) {
        _archiveFile = widgetArchive;
        _configXML = new byte[0];
        _indexFile = null;
        _iconFile = null;
    }

    public void validate() throws ValidationException, PackageException {
        File f = new File(_archiveFile);
        try {
            FileInputStream fis = new FileInputStream(f);
            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                    checksum));
                
            // Parse each zip file
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName().replace('\\', '/');
                if (entryName.equals("index.htm")) {
                    _indexFile = "index.htm";
                }
                else if (_indexFile == null && entryName.equals("index.html")) {
                    _indexFile = "index.html";
                } 
                else if (entryName.equals("config.xml")) {
                    _configXML = getBytes(zis);
                } 
                else if (entryName.equals("icon.svg")) {
                    _iconFile = "icon.svg";
                }
                else if ((_iconFile == null || _iconFile.equals("icon.gif")) && entryName.equals("icon.png")) {
                    _iconFile = "icon.png";
                    
                }
                else if (_iconFile == null && entryName.equals("icon.gif")) {
                    _iconFile = "icon.gif";                        
                }
                else if (entry.isDirectory()) {
                    // Check for reservedDirs
                    for (String reserved : RESERVED_DIRS) {
                        // The dir entry name has a trailing / like "dir/"
                        if (entryName.equals(reserved + "/")) {
                            throw new ValidationException("EXCEPTION_ARCHIVE_RESERVED_DIR");
                        }
                    }
                }
                // Validate the resource name
                Pattern patternEntryName = Pattern.compile("[a-zA-Z0-9][a-zA-Z_0-9\\.]*");;
                String entity;
                String fullEntryName = entryName;
                boolean noMoreEntity = false;
                
                while (!noMoreEntity) {
                    if (entryName.charAt(entryName.length() - 1) == '/') {
                    	entryName = entryName.substring(0, entryName.length() - 1); // Remove the ending '/'
                    }
                	
                	if (entryName.lastIndexOf('/') >= 0) {
	                	entity = entryName.substring(entryName.lastIndexOf('/') + 1);
	                	entryName = entryName.substring(0, entryName.lastIndexOf('/'));
                	} else {
                		entity = entryName;
                		noMoreEntity = true;
                	}
                	
                    if (!patternEntryName.matcher(entity).matches()) {
                        throw new ValidationException("EXCEPTION_INVALID_RESOURCE_NAME", fullEntryName);                            
                    }
                }
            } 
            
            if (_configXML.length == 0) {
                throw new PackageException("EXCEPTION_CONFIGXML_MISSING");
            }
        }
        catch (FileNotFoundException fnfe) {
            // Already validated for existence of archive file - never get here
            Logger.logMessage(LogType.FATAL, "EXCEPTION_WIDGET_ARCHIVE_NOT_FOUND");
        }
        catch (IOException ioe) {
            throw new PackageException("EXCEPTION_ARCHIVE_IO", ioe);
        }
    }
    
    public byte[] getConfigXML() {
        return _configXML;
    }
    
    public String getIndexFile() {
        return _indexFile;
    }
    
    public String getIconFile() {
        return _iconFile;
    }
    
    private byte[] getBytes(ZipInputStream zis) throws IOException {
        int size;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os, buffer.length);
        while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
            bos.write(buffer, 0, size);
        }
        bos.flush();
        bos.close();        
        return os.toString().trim().getBytes();
    }
}

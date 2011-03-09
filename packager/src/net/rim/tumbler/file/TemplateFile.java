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
package net.rim.tumbler.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import net.rim.tumbler.session.SessionManager;

public class TemplateFile {
    protected File _sourceFile;
    protected String _relativeLocation;
    
    public TemplateFile(String src, String location) {
        _sourceFile = new File(src);
        _relativeLocation = location;
    }

    public String getContents() throws IOException {
        // read file
        byte b[] = getFromFile();
        String content = new String(b);

        return refactor(content);
    }

    public String getName() {
        return _relativeLocation;
    }

    public static String refactor(String original) {
        return original.replace(
                TemplateWrapper.DEVICE_PACKAGE,
                genPackageName(SessionManager.getInstance().getArchiveName()));
    }

    private byte[] getFromFile() throws IOException {
        InputStream is = new FileInputStream(_sourceFile);

        // Get the size of the file
        long length = _sourceFile.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Error when reading file " + getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
    private static String genPackageName(String widgetName) {
        String packageHash;

        try {
            MessageDigest md;

            md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(widgetName.getBytes());
            byte[] byteArray = md.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                hexString.append(Integer.toHexString(0xFF & byteArray[i]));
            }

            packageHash = hexString.toString();
            
        } catch (Exception e) {
            packageHash = widgetName;
        }

        return TemplateWrapper.DEVICE_PACKAGE + packageHash + "package";
    }
}

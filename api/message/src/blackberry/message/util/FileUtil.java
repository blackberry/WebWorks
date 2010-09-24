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
package blackberry.message.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;

public class FileUtil {
    public final static String FILE_NAME = "name";
    public final static String FILE_MIME = "mime";
    public final static String FILE_ENCODING = "enc";
    public final static String FILE_CONTENT = "content";

    public static boolean exists(String fullPath) {
        FileConnection con = open(fullPath);
        boolean retval = (con != null && con.exists() && !con.isDirectory());
        close(con);

        return retval;
    }

    public static Hashtable getProperties(String fullPath) throws IOException {
        FileConnection con = open(fullPath);
        if (con == null) {
            throw new IOException(fullPath + " does not exist.");
        }

        String name = con.getName();
        String mimeType = MIMETypeAssociations.getMIMEType(name);

        int size = (int) con.fileSize();
        byte[] data = new byte[size];
        DataInputStream dis = con.openDataInputStream();
        dis.read(data, 0, size);
        close(dis);
        close(con);

        String encoding = getEncoding(data);

        Hashtable properties = new Hashtable();
        properties.put(FILE_NAME, name);
        properties.put(FILE_MIME, mimeType);
        properties.put(FILE_ENCODING, encoding);
        properties.put(FILE_CONTENT, data);

        return properties;
    }

    private static FileConnection open(String fullPath) {
        Connection con;
        try {
            con = Connector.open(fullPath);
        } catch (IOException e) {
            return null;
        }

        if (con instanceof FileConnection) {
            return (FileConnection) con;
        }
        else {
            close(con);
        }

        return null;
    }

    private static void close(DataInputStream con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
    }

    private static void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
    }

    public static String getEncoding(byte[] data) {

        if (data == null || data.length < 2) {
            return "";
        }

        switch (data[0] & 0xFF) {
        case 0x00:
            if (data.length >= 4 && data[1] == (byte) 0x00 && data[2] == (byte) 0xFE && data[3] == (byte) 0xFF) {
                return "utf-32be";
            }
            break;
        case 0xFE:
            if (data[1] == (byte) 0xFF) {
                return "utf-16be";
            }
            break;
        case 0xFF:
            if (data[1] == (byte) 0xFE) {
                if (data.length >= 4 && data[2] == (byte) 0x00 && data[3] == (byte) 0x00) {
                    return "utf-32le";
                }
                else {
                    return "utf-16le";
                }
            }
            break;
        case 0xEF:
            if (data.length >= 3 && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) {
                return "utf-8";
            }
            break;
        }
        return "";
    }
}

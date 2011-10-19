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
package blackberry.io.file.FileProperties;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;

/**
 * Class represents the blackberry.io.file.FileProperties object. 
 * This object is created by the blackberry.io.file funcitons and
 * contains information on a specific file
 */
public final class FilePropertiesObject extends Scriptable {

    private Hashtable _fields;

    private static final String FIELD_ISREADONLY = "isReadonly";
    private static final String FIELD_ISHIDDEN = "isHidden";
    private static final String FIELD_SIZE = "size";
    private static final String FIELD_DATEMODIFIED = "dateModified";
    private static final String FIELD_FILEEXTENSION = "fileExtension";
    private static final String FIELD_DIRECTORY = "directory";
    private static final String FIELD_MIMETYPE = "mimeType";
    private static final String FIELD_ENCODING = "encoding";

    /**
     * Constructor
     * 
     * @param isReadonly
     *            indicates if the file is read only
     * @param isHidden
     *            indicates if the file has been marked hidden
     * @param size
     *            the size, in bytes, of the file
     * @param dateCreated
     *            the date the file was created
     * @param dateModified
     *            the date the file was last modified
     * @param fileExtension
     *            the file extension
     * @param directory
     *            the full path to the directory the file is located
     * @param mimeType
     *            the mime type associated to the file
     * @param encoding
     *            the encoding used to save the file
     */
    public FilePropertiesObject( Boolean isReadonly, Boolean isHidden, Double size, Date dateCreated, Date dateModified,
            String fileExtension, String directory, String mimeType, String encoding ) {
        _fields = new Hashtable();

        _fields.put( FIELD_ISREADONLY, isReadonly );
        _fields.put( FIELD_ISHIDDEN, isHidden );
        _fields.put( FIELD_SIZE, size );
        _fields.put( FIELD_DATEMODIFIED, dateModified );
        _fields.put( FIELD_FILEEXTENSION, fileExtension );
        _fields.put( FIELD_DIRECTORY, directory );
        _fields.put( FIELD_MIMETYPE, mimeType );
        _fields.put( FIELD_ENCODING, encoding );
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
     * see net.rim.device.api.script.Scriptable#getElementCount()
     */
    public int getElementCount() {
        return _fields.size();
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
}

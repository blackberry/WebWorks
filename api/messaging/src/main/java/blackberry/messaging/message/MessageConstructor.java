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
package blackberry.messaging.message;

import java.util.Hashtable;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.service.ServiceObject;

/**
 * The MessageConstructor class is used to create new MessageObject object.
 */
public class MessageConstructor extends ScriptableFunctionBase {

    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_SAVED = 0;
    public static final int STATUS_DRAFT = 1;
    public static final int STATUS_SENT = 2;
    public static final int STATUS_ERROR_OCCURED = 3;

    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_LOW = 2;

    public static final int FOLDER_INBOX = 0;
    public static final int FOLDER_SENT = 1;
    public static final int FOLDER_DRAFT = 2;
    public static final int FOLDER_OUTBOX = 3;
    public static final int FOLDER_DELETED = 4;
    public static final int FOLDER_OTHER = 5;

    public static final String LABEL_STATUS_UNKNOWN = "STATUS_UNKNOWN";
    public static final String LABEL_STATUS_SAVED = "STATUS_SAVED";
    public static final String LABEL_STATUS_DRAFT = "STATUS_DRAFT";
    public static final String LABEL_STATUS_SENT = "STATUS_SENT";
    public static final String LABEL_STATUS_ERROR_OCCURED = "STATUS_ERROR_OCCURED";

    public static final String LABEL_PRIORITY_HIGH = "PRIORITY_HIGH";
    public static final String LABEL_PRIORITY_MEDIUM = "PRIORITY_MEDIUM";
    public static final String LABEL_PRIORITY_LOW = "PRIORITY_LOW";

    public static final String LABEL_FOLDER_INBOX = "FOLDER_INBOX";
    public static final String LABEL_FOLDER_SENT = "FOLDER_SENT";
    public static final String LABEL_FOLDER_DRAFT = "FOLDER_DRAFT";
    public static final String LABEL_FOLDER_OUTBOX = "FOLDER_OUTBOX";
    public static final String LABEL_FOLDER_DELETED = "FOLDER_DELETED";
    public static final String LABEL_FOLDER_OTHER = "FOLDER_OTHER";

    private Hashtable _fields;

    /**
     * Construct a Message object and create a new instance of the find function class
     */
    public MessageConstructor() {
        _fields = new Hashtable();
        _fields.put( LABEL_STATUS_UNKNOWN, new Integer( STATUS_UNKNOWN ) );
        _fields.put( LABEL_STATUS_SAVED, new Integer( STATUS_SAVED ) );
        _fields.put( LABEL_STATUS_DRAFT, new Integer( STATUS_DRAFT ) );
        _fields.put( LABEL_STATUS_SENT, new Integer( STATUS_SENT ) );
        _fields.put( LABEL_STATUS_ERROR_OCCURED, new Integer( STATUS_ERROR_OCCURED ) );
        _fields.put( LABEL_PRIORITY_HIGH, new Integer( PRIORITY_HIGH ) );
        _fields.put( LABEL_PRIORITY_MEDIUM, new Integer( PRIORITY_MEDIUM ) );
        _fields.put( LABEL_PRIORITY_LOW, new Integer( PRIORITY_LOW ) );
        _fields.put( LABEL_FOLDER_INBOX, new Integer( FOLDER_INBOX ) );
        _fields.put( LABEL_FOLDER_SENT, new Integer( FOLDER_SENT ) );
        _fields.put( LABEL_FOLDER_DRAFT, new Integer( FOLDER_DRAFT ) );
        _fields.put( LABEL_FOLDER_OUTBOX, new Integer( FOLDER_OUTBOX ) );
        _fields.put( LABEL_FOLDER_DELETED, new Integer( FOLDER_DELETED ) );
        _fields.put( LABEL_FOLDER_OTHER, new Integer( FOLDER_OTHER ) );
        _fields.put( FindFunction.NAME, new FindFunction() );
    }

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args != null && args.length == 1 ) {
            ServiceObject serviceObject = (ServiceObject) args[ 0 ]; // type error will be thrown if input is invalid
            return new MessageObject( serviceObject );
        }
        return new MessageObject();
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        Object field = _fields.get( name );
        if( field == null ) {
            super.getField( name );
        }
        return field;
    }

    /**
     * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( ServiceObject.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        return UNDEFINED;
    }
}

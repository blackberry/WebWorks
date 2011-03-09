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

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.FolderNotFoundException;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Transport;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.service.ServiceObject;

/**
 * The MessageObject class that implements blackberry.message.Message class.
 */
public class MessageObject extends ScriptableObjectBase {
    private Store _store;
    private ServiceConfiguration _serviceConfig;
    private Message _message;

    private MessageSaveScriptableFunction _save;
    private MessageRemoveScriptableFunction _remove;
    private MessageSendScriptableFunction _send;

    public static final String FIELD_TORECIPIENTS = "toRecipients";
    public static final String FIELD_CCRECIPIENTS = "ccRecipients";
    public static final String FIELD_BCCRECIPIENTS = "bccRecipients";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_BODY = "body";
    public static final String FIELD_FROM = "from";
    public static final String FIELD_REPLYTO = "replyTo";
    public static final String FIELD_FOLDER = "folder";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_UID = "uid";

    public static final String METHOD_SAVE = "save";
    public static final String METHOD_REMOVE = "remove";
    public static final String METHOD_SEND = "send";

    private final static String BLANK = "";
    
    /**
     * Constructs a new empty MessageObject object.
     * 
     */
    public MessageObject() {
        this( null, null );
    }

    /**
     * Constructs a new MessageObject object.
     * 
     * @param s
     *            The Service object specifies the type of service
     * 
     */
    public MessageObject( ServiceObject s ) {
        this( null, s );
    }

    /**
     * Constructs a new MessageObject object.
     * 
     * @param m
     *            The Blackberry Mail Message object that represents Message object
     * @param s
     *            The Service object specifies the type of service
     * 
     */
    public MessageObject( Message m, ServiceObject s ) {
        _message = m;
        if( s == null ) {
            if( _message != null ) {
                _store = _message.getFolder().getStore();
            } else {
                _store = Session.getDefaultInstance().getStore();
            }
            _serviceConfig = _store.getServiceConfiguration();

        } else {
            try {
                _serviceConfig = new ServiceConfiguration( s.getUid(), s.getCid() );
                _store = Session.getInstance( _serviceConfig ).getStore();
            } catch( NoSuchServiceException e ) {
                _store = Session.getDefaultInstance().getStore();
                _serviceConfig = _store.getServiceConfiguration();
            }
        }
        initial();
    }

    // Injects fields and methods
    private void initial() {
        if( _message != null ) {
            addItem( new ScriptField( FIELD_TORECIPIENTS, toRecipients(), ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_CCRECIPIENTS, ccRecipients(), ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_BCCRECIPIENTS, bccRecipients(), ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_SUBJECT, subject(), ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_BODY, body(), ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FROM, from(), ScriptField.TYPE_STRING, true, false ) );
            addItem( new ScriptField( FIELD_REPLYTO, replyTo(), ScriptField.TYPE_STRING, true, false ) );
            addItem( new ScriptField( FIELD_FOLDER, new Integer( MessageUtility.folderTypeToFolderId( _message.getFolder()
                    .getType() ) ), ScriptField.TYPE_INT, false, false ) );
            addItem( new ScriptField( FIELD_PRIORITY, new Integer( MessageUtility.messagePriorityToJSPriority( _message
                    .getPriority() ) ), ScriptField.TYPE_INT, false, false ) );
            addItem( new ScriptField( FIELD_STATUS,
                    new Integer( MessageUtility.messageStatusToJSStatus( _message.getStatus() ) ), ScriptField.TYPE_INT, true,
                    false ) );
            addItem( new ScriptField( FIELD_UID, uid(), ScriptField.TYPE_INT, true, false ) );
        } else {
            addItem( new ScriptField( FIELD_TORECIPIENTS, BLANK, ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_CCRECIPIENTS, BLANK, ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_BCCRECIPIENTS, BLANK, ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_SUBJECT, BLANK, ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_BODY, BLANK, ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FROM, BLANK, ScriptField.TYPE_STRING, true, false ) );
            addItem( new ScriptField( FIELD_REPLYTO, BLANK, ScriptField.TYPE_STRING, true, false ) );
            addItem( new ScriptField( FIELD_FOLDER, new Integer( MessageConstructor.FOLDER_DRAFT ), ScriptField.TYPE_INT, false,
                    false ) );
            addItem( new ScriptField( FIELD_PRIORITY, new Integer( MessageConstructor.PRIORITY_MEDIUM ), ScriptField.TYPE_INT,
                    false, false ) );
            addItem( new ScriptField( FIELD_STATUS, new Integer( MessageConstructor.STATUS_DRAFT ), ScriptField.TYPE_INT, true,
                    false ) );
            addItem( new ScriptField( FIELD_UID, new Integer( 0 ), ScriptField.TYPE_INT, true, false ) );
        }

        _save = new MessageSaveScriptableFunction();
        _remove = new MessageRemoveScriptableFunction();
        _send = new MessageSendScriptableFunction();

        addItem( new ScriptField( METHOD_SAVE, _save, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_REMOVE, _remove, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_SEND, _send, ScriptField.TYPE_SCRIPTABLE, true, true ) );
    }

    private String toRecipients() {
        String recipientsString = "";
        Address[] recipients = null;
        try {
            recipients = _message.getRecipients( Message.RecipientType.TO );
        } catch( MessagingException me ) {
            // do nothing
        }

        if( recipients != null ) {
            for( int i = 0; i < recipients.length; i++ ) {
                if( i == 0 ) {
                    recipientsString = recipients[ i ].getAddr();
                } else {
                    recipientsString = recipientsString + ";" + recipients[ i ].getAddr();
                }
            }
        }
        return recipientsString;
    }

    private String ccRecipients() {
        String recipientsString = "";
        Address[] recipients = null;
        try {
            recipients = _message.getRecipients( Message.RecipientType.CC );
        } catch( MessagingException me ) {
            // do nothing
        }

        if( recipients != null ) {
            for( int i = 0; i < recipients.length; i++ ) {
                if( i == 0 ) {
                    recipientsString = recipients[ i ].getAddr();
                } else {
                    recipientsString = recipientsString + ";" + recipients[ i ].getAddr();
                }
            }
        }
        return recipientsString;
    }

    private String bccRecipients() {
        String recipientsString = "";
        Address[] recipients = null;
        try {
            recipients = _message.getRecipients( Message.RecipientType.BCC );
        } catch( MessagingException me ) {
            // do nothing
        }

        if( recipients != null ) {
            for( int i = 0; i < recipients.length; i++ ) {
                if( i == 0 ) {
                    recipientsString = recipients[ i ].getAddr();
                } else {
                    recipientsString = recipientsString + ";" + recipients[ i ].getAddr();
                }
            }
        }
        return recipientsString;
    }

    private String subject() {
        String s = _message.getSubject();
        return ( s == null ) ? "" : s;
    }

    private String body() {
        String s = _message.getBodyText();
        return ( s == null ) ? "" : s;
    }

    private String from() {
        Address a = null;
        try {
            a = _message.getFrom();
        } catch( MessagingException me ) {
            // do nothing
        }

        String s = ( a == null ) ? "" : a.getAddr();
        return ( s == null ) ? "" : s;
    }

    private String replyTo() {
        String recipientsString = "";
        Address[] recipients = null;
        try {
            recipients = _message.getReplyTo();
        } catch( MessagingException me ) {
            // do nothing
        }

        if( recipients != null ) {
            for( int i = 0; i < recipients.length; i++ ) {
                if( i == 0 ) {
                    recipientsString = recipients[ i ].getAddr();
                } else {
                    recipientsString = recipientsString + ";" + recipients[ i ].getAddr();
                }
            }
        }
        return recipientsString;
    }

    private Integer uid() {
        return new Integer( _message.getMessageId() );
    }

    private int getFolder() {
        Integer i = (Integer) ( getField( FIELD_FOLDER ) );
        return i.intValue();
    }

    private int getPriority() {
        Integer i = (Integer) ( getField( FIELD_PRIORITY ) );
        return i.intValue();
    }

    /**
     * Get Message variable of this MessageObject. It is important that Message is not be confused with MessageObject.
     * 
     * @return Message variable of this MessageObject.
     */
    public Message getMessage() {
        return _message;
    }

    /**
     * Execute the save function.
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        _save.execute( null, null );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }
    
    /**
     * Implementation of the save function.
     */
    public class MessageSaveScriptableFunction extends ScriptableFunctionBase {
        private final MessageObject _outer;

        /**
         * Default constructor.
         */
        public MessageSaveScriptableFunction() {
            _outer = MessageObject.this;
        }

        /**
         * Saves the changes made to the message object. On a successful save of a new message the uid attribute will be filled in
         * with its new value.
         * 
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        public Object execute( Object thiz, Object[] innerArgs ) throws Exception {
            if( _message == null ) {
                Folder folderNew = getFirstFolder( MessageUtility.folderIdToFolderType( _outer.getFolder() ) );

                if( folderNew == null ) {
                    throw new FolderNotFoundException( "" );
                }

                _message = new Message( folderNew );
            }

            // toRecipients, ccRecipients, bccRecipients
            _message.removeAllRecipients( Message.RecipientType.TO );
            _message.removeAllRecipients( Message.RecipientType.CC );
            _message.removeAllRecipients( Message.RecipientType.BCC );

            String value = (String) _outer.getField( MessageObject.FIELD_TORECIPIENTS );
            Address[] addresses = MessageUtility.stringToAddresses( value );
            if( addresses.length > 0 ) {
                _message.addRecipients( Message.RecipientType.TO, addresses );
            }

            value = (String) _outer.getField( MessageObject.FIELD_CCRECIPIENTS );
            addresses = MessageUtility.stringToAddresses( value );
            if( addresses.length > 0 ) {
                _message.addRecipients( Message.RecipientType.CC, addresses );
            }

            value = (String) _outer.getField( MessageObject.FIELD_BCCRECIPIENTS );
            addresses = MessageUtility.stringToAddresses( value );
            if( addresses.length > 0 ) {
                _message.addRecipients( Message.RecipientType.BCC, addresses );
            }

            // subject, body
            value = (String) _outer.getField( MessageObject.FIELD_SUBJECT );
            _message.setSubject( value );

            value = (String) _outer.getField( MessageObject.FIELD_BODY );
            _message.setContent( value );

            // priority
            int priority = _outer.getPriority();
            byte p = MessageUtility.JSPriorityToMessagePriority( priority );
            _message.setPriority( p );

            Folder folder = _message.getFolder();
            folder.appendMessage( _message );

            // from and replyTo
            String from = _serviceConfig.getEmailAddress();
            _outer.setValue( MessageObject.FIELD_FROM, from );
            _outer.setValue( MessageObject.FIELD_REPLYTO, from );
            
            // folder - it may change after message is saved
            int type = _message.getFolder().getType();
            int newfolder = MessageUtility.folderTypeToFolderId( type );
            _outer.setValue( MessageObject.FIELD_FOLDER, new Integer( newfolder ) );

            // status
            int st = _message.getStatus();
            int status = MessageUtility.messageStatusToJSStatus( st );
            _outer.setValue( MessageObject.FIELD_STATUS, new Integer( status ) );

            // uid
            _outer.setValue( MessageObject.FIELD_UID, new Integer( _message.getMessageId() ) );

            return UNDEFINED;
        }

        private Folder getFirstFolder( int type ) {
            Folder[] folders = _store.list( type );
            if( folders == null ) {
                return null;
            }

            for( int i = folders.length - 1; i >= 0; --i ) {
                if( folders[ i ].getFullName().indexOf( ServiceConfiguration.NO_SERVICE_BOOK ) == -1 ) {
                    return folders[ i ]; // use the first folder that corresponds to a valid service config
                }
            }
            return folders.length > 0 ? folders[ 0 ] : null;
        }
    }

    /**
     * Implementation of the remove function.
     */
    public class MessageRemoveScriptableFunction extends ScriptableFunctionBase {

        /**
         * Removes a message from the PIM storage. If the message has not been previously saved, an exception will be thrown.
         * 
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        public Object execute( Object thiz, Object[] innerArgs ) throws Exception {

            if( _message == null ) {
                throw new MessagingException( "Message item not found." );
            }

            Folder folder = _message.getFolder();
            folder.deleteMessage( _message );
            _message = null;

            return UNDEFINED;
        }
    }

    /**
     * Implementation of the send function.
     */
    public class MessageSendScriptableFunction extends ScriptableFunctionBase {
        private final MessageObject _outer;

        /**
         * Default constructor.
         */
        public MessageSendScriptableFunction() {
            _outer = MessageObject.this;
        }

        /**
         * Sends the message to its recipients. If the message isn't a new
         * message or a draft, an exception will be thrown. On a successful save
         * of a new message the uid attribute will be filled in with its new
         * value. If the message has invalid fields an exception will be thrown.
         * 
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        public Object execute( Object thiz, Object[] innerArgs ) throws Exception {
            if( _message == null ) {
                try {
                    _outer._save.execute( null, null );
                } catch( Exception e ) {
                    _outer._remove.execute( null, null );
                    throw e;
                }
            }

            if( _message != null ) {
                Transport.send( _message );
                // Update folder and status of MessageObject
                int type = _message.getFolder().getType();
                int newfolder = MessageUtility.folderTypeToFolderId( type );
                _outer.setValue( MessageObject.FIELD_FOLDER, new Integer( newfolder ) );
                int st = _message.getStatus();
                int status = MessageUtility.messageStatusToJSStatus( st );
                _outer.setValue( MessageObject.FIELD_STATUS, new Integer( status ) );
            }
            return UNDEFINED;
        }
    }
}

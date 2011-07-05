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

import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Store;

/**
 * Message utility class.
 */
public class MessageUtility {

    /**
     * Returns folder type from the folder id.
     * 
     * @param folderId
     *            The folder id
     * @return The folder type, see {@link net.rim.blackberry.api.mail.Folder}
     */
    public static int folderIdToFolderType( int folderId ) {
        switch( folderId ) {
            case MessageConstructor.FOLDER_INBOX:
                return Folder.INBOX;
            case MessageConstructor.FOLDER_SENT:
                return Folder.SENT;
            case MessageConstructor.FOLDER_DRAFT:
                return Folder.DRAFT;
            case MessageConstructor.FOLDER_OUTBOX:
                return Folder.OUTBOX;
            case MessageConstructor.FOLDER_DELETED:
                return Folder.DELETED;
            case MessageConstructor.FOLDER_OTHER:
                return Folder.OTHER;
            default:
                return Folder.OTHER;
        }
    }

    private static final void insertFolder( Folder f, Vector v ) {
        if( f.getId() == Folder.INVALID_FOLDER_ID ) {
            return;
        }

        boolean bFound = false;
        int size = v.size();
        for( int i = 0; i < size; ++i ) {
            Folder folder = (Folder) v.elementAt( i );
            if( folder.getId() == f.getId() ) {
                String f1 = folder.getFullName();
                String f2 = f.getFullName();
                if( f1.equals( f2 ) ) {
                    bFound = true;
                    break;
                }
            }
        }

        if( !bFound ) {
            v.addElement( f );
        }
    }

    /**
     * Get all folders recursively in a Store and put them in a vector
     * 
     * @param s
     *            a message store
     * @param v
     *            a vector for storing
     */
    public static final void getAllFoldersRecursively( Store s, Vector v ) {
        Folder[] list = s.list();
        if( list == null ) {
            return;
        }

        for( int i = 0; i < list.length; ++i ) {
            insertFolder( list[ i ], v );
        }

        for( int i = 0; i < list.length; ++i ) {
            getAllFoldersRecursively( list[ i ], v );
        }
    }

    /**
     * Get all sub-folders recursively in a given folder and put them in a vector
     * 
     * @param s
     *            a mailbox folder
     * @param v
     *            a vector for storing
     */
    public static final void getAllFoldersRecursively( Folder f, Vector v ) {
        Folder[] list = f.list();
        if( list == null ) {
            return;
        }

        for( int i = 0; i < list.length; ++i ) {
            insertFolder( list[ i ], v );
        }

        for( int i = 0; i < list.length; ++i ) {
            getAllFoldersRecursively( list[ i ], v );
        }
    }

    /**
     * Returns message priority from the priority defined in Javascript.
     * 
     * @param jsPriority
     *            The priority defined in Javascript
     * @return Message priority
     */
    public static final byte JSPriorityToMessagePriority( int jsPriority ) {
        byte priority;
        if( jsPriority == MessageConstructor.PRIORITY_LOW ) {
            priority = Message.Priority.LOW;
        } else if( jsPriority == MessageConstructor.PRIORITY_MEDIUM ) {
            priority = Message.Priority.NORMAL;
        } else {
            priority = Message.Priority.HIGH;
        }
        return priority;
    }

    /**
     * Returns the priority used in Javascript from the message priority.
     * 
     * @param messagePriority
     *            The message priority
     * @return The priority used in Javascript
     */
    public static final int messagePriorityToJSPriority( byte messagePriority ) {
        int priority;
        switch( messagePriority ) {
            case Message.Priority.LOW:
                priority = MessageConstructor.PRIORITY_LOW;
                break;
            case Message.Priority.NORMAL:
                priority = MessageConstructor.PRIORITY_MEDIUM;
                break;
            case Message.Priority.HIGH:
                priority = MessageConstructor.PRIORITY_HIGH;
                break;
            default:
                priority = MessageConstructor.PRIORITY_MEDIUM;
                break;
        }
        return priority;
    }

    /**
     * Returns the status used in the Javascript from the status defined in Message.
     * 
     * @param messageStatus
     *            The status defined in Message
     * @return The message status used in Javascript
     */
    public static final int messageStatusToJSStatus( int messageStatus ) {
        int status;
        switch( messageStatus ) {
            case Message.Status.TX_COMPOSING:
                status = MessageConstructor.STATUS_DRAFT;
                break;
            case Message.Status.RX_RECEIVED:
                status = MessageConstructor.STATUS_SAVED;
                break;
            case Message.Status.TX_SENT:
            case Message.Status.TX_DELIVERED:
                status = MessageConstructor.STATUS_SENT;
                break;
            case Message.Status.RX_ERROR:
            case Message.Status.TX_ERROR:
                status = MessageConstructor.STATUS_ERROR_OCCURED;
                break;
            default:
                status = MessageConstructor.STATUS_UNKNOWN;
                break;
        }
        return status;
    }

    /**
     * Returns folder id from the folder type.
     * 
     * @param folderType
     *            The folder type
     * @return The folder id
     */
    public static final int folderTypeToFolderId( int folderType ) {
        int folder;
        switch( folderType ) {
            case Folder.INBOX:
                folder = MessageConstructor.FOLDER_INBOX;
                break;
            case Folder.SENT:
                folder = MessageConstructor.FOLDER_SENT;
                break;
            case Folder.DRAFT:
                folder = MessageConstructor.FOLDER_DRAFT;
                break;
            case Folder.OUTBOX:
                folder = MessageConstructor.FOLDER_OUTBOX;
                break;
            case Folder.DELETED:
                folder = MessageConstructor.FOLDER_DELETED;
                break;
            default:
                folder = MessageConstructor.FOLDER_OTHER;
                break;
        }
        return folder;
    }

    /**
     * Splits the specified string into tokens delimited by ' ', '\t', '\n', ',' and ';'.
     * 
     * @param value
     *            The string to split.
     * @return The array of string tokens.
     */
    public static String[] split( final String value ) {
        if( value == null ) {
            return new String[ 0 ];
        }

        String str = value.trim();

        if( str.length() == 0 ) {
            return new String[ 0 ];
        }

        Vector list = new Vector();

        int start = 0;
        int length = str.length();

        int end = -1;
        end = min( end, str.indexOf( ' ', start ) );
        end = min( end, str.indexOf( '\t', start ) );
        end = min( end, str.indexOf( '\n', start ) );
        end = min( end, str.indexOf( ',', start ) );
        end = min( end, str.indexOf( ';', start ) );

        while( end != -1 ) {
            list.addElement( str.substring( start, end ).trim() );

            start = end + 1; // skip past the delimeter
            if( start >= length ) {
                break; // done
            }

            end = -1;
            end = min( end, str.indexOf( ' ', start ) );
            end = min( end, str.indexOf( '\t', start ) );
            end = min( end, str.indexOf( '\n', start ) );
            end = min( end, str.indexOf( ',', start ) );
            end = min( end, str.indexOf( ';', start ) );
        }

        // Add the remaining characters
        if( start < length ) {
            list.addElement( str.substring( start ).trim() );
        }

        String[] strings = new String[ list.size() ];
        list.copyInto( strings );
        return strings;
    }

    private static int min( int end, int b ) {
        if( b == -1 ) {
            return end;
        }

        if( end == -1 ) {
            return b;
        }

        return ( end < b ) ? end : b;
    }
    
    /**
     * Convert String addresses to an array of Address objects.
     * 
     * @param addressString
     * @return an array of Address objects
     */
    public static Address[] stringToAddresses( String addressString ) {
        String[] emails = MessageUtility.split( addressString );
        Address[] addresses = new Address[ emails.length ];

        for( int i = 0; i < emails.length; i++ ) {
            try {
                addresses[ i ] = new Address( emails[ i ], emails[ i ] );
            } catch( AddressException e ) {
                addresses[ i ] = null;
            }
        }

        return addresses;
    }
}

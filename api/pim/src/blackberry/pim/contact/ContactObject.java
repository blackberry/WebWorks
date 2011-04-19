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
package blackberry.pim.contact;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.FieldFullException;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.UnsupportedFieldException;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryPIM;
import net.rim.device.api.io.Base64OutputStream;
import blackberry.core.Blob;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.service.ServiceObject;
import blackberry.pim.address.AddressObject;
import blackberry.pim.category.CategoryNamespace;

/**
 * Implementation of the pim Contact
 * 
 * @author dmateescu
 */
public class ContactObject extends ScriptableObjectBase {
    private Contact _contact;
    private String _serviceName;

    private ContactSaveScriptableFunction _save;
    private ContactRemoveScriptableFunction _remove;
    private ContactSetPictureScriptableFunction _setPicture;

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_FIRSTNAME = "firstName";
    public static final String FIELD_LASTNAME = "lastName";
    public static final String FIELD_HOMEADDRESS = "homeAddress";
    public static final String FIELD_WORKADDRESS = "workAddress";
    public static final String FIELD_EMAIL1 = "email1";
    public static final String FIELD_EMAIL2 = "email2";
    public static final String FIELD_EMAIL3 = "email3";
    public static final String FIELD_COMPANY = "company";
    public static final String FIELD_JOBTITLE = "jobTitle";
    public static final String FIELD_HOMEPHONE = "homePhone";
    public static final String FIELD_HOMEPHONE2 = "homePhone2";
    public static final String FIELD_WORKPHONE = "workPhone";
    public static final String FIELD_WORKPHONE2 = "workPhone2";
    public static final String FIELD_MOBILEPHONE = "mobilePhone";
    public static final String FIELD_MOBILEPHONE2 = "mobilePhone2";
    public static final String FIELD_FAXPHONE = "faxPhone";
    public static final String FIELD_FAXPHONE2 = "faxPhone2";
    public static final String FIELD_PAGERPHONE = "pagerPhone";
    public static final String FIELD_OTHERPHONE = "otherPhone";
    public static final String FIELD_NOTE = "note";
    public static final String FIELD_WEBPAGE = "webpage";
    public static final String FIELD_BIRTHDAY = "birthday";
    public static final String FIELD_ANNIVERSARY = "anniversary";
    public static final String FIELD_USER1 = "user1";
    public static final String FIELD_USER2 = "user2";
    public static final String FIELD_USER3 = "user3";
    public static final String FIELD_USER4 = "user4";
    public static final String FIELD_PIN = "pin";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_PICTURE = "picture";
    public static final String FIELD_CATEGORIES = "categories";

    public static final String METHOD_SAVE = "save";
    public static final String METHOD_REMOVE = "remove";
    public static final String METHOD_SETPICTURE = "setPicture";

    private static final String PICTURE_ENCODING = "UTF-8";

    /**
     * Default constructor of the Contact
     */
    public ContactObject() {
        super();
        _contact = null;
        _serviceName = "";
        initial();
    }

    /**
     * Constructs a Contact object based on a given javax.microedition.pim.Contact
     * 
     * @param c
     *            the javax.microedition.pim.Contact
     */
    public ContactObject( Contact c ) {
        super();
        _contact = c;
        _serviceName = "";
        initial();
    }

    /**
     * Constructs a Contact object based on a ServiceObject
     * 
     * @param s
     *            the ServiceObject
     */
    public ContactObject( ServiceObject s ) {
        super();
        _contact = null;
        _serviceName = s.getName();
        initial();
    }

    /**
     * Constructs a Contact object based on a given javax.microedition.pim.Contact and a given ServiceObject
     * 
     * @param c
     *            the javax.microedition.pim.Contact
     * @param s
     *            the ServiceObject
     */
    public ContactObject( Contact c, ServiceObject s ) {
        super();
        _contact = c;
        _serviceName = s.getName();
        initial();
    }

    /**
     * This class implements the save method of a Contact
     * 
     */
    public class ContactSaveScriptableFunction extends ScriptableFunctionBase {
        private final ContactObject _outer;

        /**
         * Default constructor of a ContactSaveScriptableFunction
         */
        public ContactSaveScriptableFunction() {
            super();
            _outer = ContactObject.this;
        }

        /**
         * This method updates the contact when save has been triggered
         * 
         * @throws Exception
         */

        public void update() throws Exception {
            int nameArraySize;
            int addressArraySize;
            // open the handheld contacts database
            // receive nameArraySize and addressArraySize

            ContactList contactList;
            if( _serviceName.length() == 0 ) {
                contactList = (ContactList) BlackBerryPIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.READ_WRITE );
            } else {
                contactList = (ContactList) BlackBerryPIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.READ_WRITE,
                        _serviceName );
            }
            nameArraySize = contactList.stringArraySize( Contact.NAME );
            addressArraySize = contactList.stringArraySize( Contact.ADDR );

            if( _contact == null ) {
                _contact = (Contact) contactList.createContact();
            }

            // name & title
            String[] name = new String[ nameArraySize ];
            name[ Contact.NAME_GIVEN ] = _outer.getItem( ContactObject.FIELD_FIRSTNAME ).getStringValue();
            name[ Contact.NAME_FAMILY ] = _outer.getItem( ContactObject.FIELD_LASTNAME ).getStringValue();
            name[ Contact.NAME_PREFIX ] = _outer.getItem( ContactObject.FIELD_TITLE ).getStringValue();
            if( _contact.countValues( Contact.NAME ) == 0 ) {
                if( name[ Contact.NAME_GIVEN ].length() > 0 || name[ Contact.NAME_FAMILY ].length() > 0
                        || name[ Contact.NAME_PREFIX ].length() > 0 ) {
                    _contact.addStringArray( Contact.NAME, Contact.ATTR_NONE, name );
                }
            } else {
                if( name[ Contact.NAME_GIVEN ].length() > 0 || name[ Contact.NAME_FAMILY ].length() > 0
                        || name[ Contact.NAME_PREFIX ].length() > 0 ) {
                    _contact.setStringArray( Contact.NAME, 0, Contact.ATTR_NONE, name );
                } else {
                    // remove old values
                    _contact.removeValue( Contact.NAME, 0 );
                }
            }

            // homeAddress && workAddress
            Object o;
            o = _outer.getItem( ContactObject.FIELD_HOMEADDRESS ).getValue();
            if( o == null ) {
                String[] address = new String[ addressArraySize ];
                address[ Contact.ADDR_STREET ] = "";
                address[ Contact.ADDR_LOCALITY ] = "";
                address[ Contact.ADDR_REGION ] = "";
                address[ Contact.ADDR_POSTALCODE ] = "";
                address[ Contact.ADDR_COUNTRY ] = "";
                address[ Contact.ADDR_EXTRA ] = "";
                setAddress( _contact, Contact.ATTR_HOME, address );
            } else if( o instanceof AddressObject ) {
                AddressObject addressScriptable = (AddressObject) o;

                String[] addressHome = new String[ addressArraySize ];
                addressHome[ Contact.ADDR_STREET ] = addressScriptable.getItem( AddressObject.FIELD_ADDRESS1 ).getStringValue();
                addressHome[ Contact.ADDR_LOCALITY ] = addressScriptable.getItem( AddressObject.FIELD_CITY ).getStringValue();
                addressHome[ Contact.ADDR_REGION ] = addressScriptable.getItem( AddressObject.FIELD_STATE ).getStringValue();
                addressHome[ Contact.ADDR_POSTALCODE ] = addressScriptable.getItem( AddressObject.FIELD_ZIP ).getStringValue();
                addressHome[ Contact.ADDR_COUNTRY ] = addressScriptable.getItem( AddressObject.FIELD_COUNTRY ).getStringValue();
                addressHome[ Contact.ADDR_EXTRA ] = addressScriptable.getItem( AddressObject.FIELD_ADDRESS2 ).getStringValue();
                setAddress( _contact, Contact.ATTR_HOME, addressHome );
            }

            o = _outer.getItem( ContactObject.FIELD_WORKADDRESS ).getValue();
            if( o == null ) {
                String[] address = new String[ addressArraySize ];
                address[ Contact.ADDR_STREET ] = "";
                address[ Contact.ADDR_LOCALITY ] = "";
                address[ Contact.ADDR_REGION ] = "";
                address[ Contact.ADDR_POSTALCODE ] = "";
                address[ Contact.ADDR_COUNTRY ] = "";
                address[ Contact.ADDR_EXTRA ] = "";
                setAddress( _contact, Contact.ATTR_WORK, address );
            } else if( o instanceof AddressObject ) {
                AddressObject addressScriptable = (AddressObject) o;

                String[] addressWork = new String[ addressArraySize ];
                addressWork[ Contact.ADDR_STREET ] = addressScriptable.getItem( AddressObject.FIELD_ADDRESS1 ).getStringValue();
                addressWork[ Contact.ADDR_LOCALITY ] = addressScriptable.getItem( AddressObject.FIELD_CITY ).getStringValue();
                addressWork[ Contact.ADDR_REGION ] = addressScriptable.getItem( AddressObject.FIELD_STATE ).getStringValue();
                addressWork[ Contact.ADDR_POSTALCODE ] = addressScriptable.getItem( AddressObject.FIELD_ZIP ).getStringValue();
                addressWork[ Contact.ADDR_COUNTRY ] = addressScriptable.getItem( AddressObject.FIELD_COUNTRY ).getStringValue();
                addressWork[ Contact.ADDR_EXTRA ] = addressScriptable.getItem( AddressObject.FIELD_ADDRESS2 ).getStringValue();
                setAddress( _contact, Contact.ATTR_WORK, addressWork );
            }

            // email
            int countEmail = _contact.countValues( Contact.EMAIL );
            for( int j = 0; j < countEmail; j++ ) {
                _contact.removeValue( Contact.EMAIL, 0 );
            }

            countEmail = 0;
            String[] emails = new String[ 3 ];
            String email;

            email = _outer.getItem( ContactObject.FIELD_EMAIL1 ).getStringValue();
            if( email != null && email.length() > 0 ) {
                emails[ countEmail++ ] = email;
            }

            email = _outer.getItem( ContactObject.FIELD_EMAIL2 ).getStringValue();
            if( email != null && email.length() > 0 ) {
                emails[ countEmail++ ] = email;
            }

            email = _outer.getItem( ContactObject.FIELD_EMAIL3 ).getStringValue();
            if( email != null && email.length() > 0 ) {
                emails[ countEmail++ ] = email;
            }

            // reset email
            _outer.getItem( ContactObject.FIELD_EMAIL1 ).setValue( "" );
            _outer.getItem( ContactObject.FIELD_EMAIL2 ).setValue( "" );
            _outer.getItem( ContactObject.FIELD_EMAIL3 ).setValue( "" );

            for( int j = 0; j < countEmail; j++ ) {
                _contact.addString( Contact.EMAIL, PIMItem.ATTR_NONE, emails[ j ] );
                if( j == 0 ) {
                    _outer.getItem( ContactObject.FIELD_EMAIL1 ).setValue( emails[ j ] );
                } else if( j == 1 ) {
                    _outer.getItem( ContactObject.FIELD_EMAIL2 ).setValue( emails[ j ] );
                } else if( j == 2 ) {
                    _outer.getItem( ContactObject.FIELD_EMAIL3 ).setValue( emails[ j ] );
                }
            }

            // company
            String value;
            value = _outer.getItem( ContactObject.FIELD_COMPANY ).getStringValue();
            if( _contact.countValues( Contact.ORG ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( Contact.ORG, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( Contact.ORG, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( Contact.ORG, 0 );
                }
            }

            // jobtitle
            value = _outer.getItem( ContactObject.FIELD_JOBTITLE ).getStringValue();
            if( _contact.countValues( Contact.TITLE ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( Contact.TITLE, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( Contact.TITLE, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( Contact.TITLE, 0 );
                }
            }

            // telephone numbers
            setTelephone( _contact, Contact.ATTR_HOME, _outer.getItem( ContactObject.FIELD_HOMEPHONE ).getStringValue() );
            setTelephone( _contact, BlackBerryContact.ATTR_HOME2, _outer.getItem( ContactObject.FIELD_HOMEPHONE2 )
                    .getStringValue() );
            setTelephone( _contact, Contact.ATTR_WORK, _outer.getItem( ContactObject.FIELD_WORKPHONE ).getStringValue() );
            setTelephone( _contact, BlackBerryContact.ATTR_WORK2, _outer.getItem( ContactObject.FIELD_WORKPHONE2 )
                    .getStringValue() );
            setTelephone( _contact, Contact.ATTR_MOBILE, _outer.getItem( ContactObject.FIELD_MOBILEPHONE ).getStringValue() );
            // setTelephone(_contact, BlackBerryContact.ATTR_MOBILE2,
            // _outer.getItem(ContactObject.FIELD_MOBILEPHONE2).getStringValue());
            setTelephone( _contact, Contact.ATTR_FAX, _outer.getItem( ContactObject.FIELD_FAXPHONE ).getStringValue() );
            // setTelephone(_contact, BlackBerryContact.ATTR_FAX2,
            // _outer.getItem(ContactObject.FIELD_FAXPHONE2).getStringValue());
            setTelephone( _contact, Contact.ATTR_PAGER, _outer.getItem( ContactObject.FIELD_PAGERPHONE ).getStringValue() );
            setTelephone( _contact, Contact.ATTR_OTHER, _outer.getItem( ContactObject.FIELD_OTHERPHONE ).getStringValue() );

            // note
            value = _outer.getItem( ContactObject.FIELD_NOTE ).getStringValue();
            if( _contact.countValues( Contact.NOTE ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( Contact.NOTE, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( Contact.NOTE, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( Contact.NOTE, 0 );
                }
            }

            // webpage
            value = _outer.getItem( ContactObject.FIELD_WEBPAGE ).getStringValue();
            try {
                if( _contact.countValues( Contact.URL ) == 0 ) {
                    if( value.length() > 0 ) {
                        _contact.addString( Contact.URL, PIMItem.ATTR_NONE, value );
                    }
                } else {
                    if( value.length() > 0 ) {
                        _contact.setString( Contact.URL, 0, PIMItem.ATTR_NONE, value );
                    } else {
                        _contact.removeValue( Contact.URL, 0 );
                    }
                }
            } catch( UnsupportedFieldException e ) {
                throw new Exception( "\"webpage\" is not a supported property of Contact" );
            }

            // birthday & anniversary
            Date d = (Date) _outer.getItem( ContactObject.FIELD_BIRTHDAY ).getValue();
            if( _contact.countValues( Contact.BIRTHDAY ) == 0 ) {
                if( d != null ) {
                    _contact.addDate( Contact.BIRTHDAY, PIMItem.ATTR_NONE, d.getTime() );
                }
            } else {
                if( d != null ) {
                    _contact.setDate( Contact.BIRTHDAY, 0, PIMItem.ATTR_NONE, d.getTime() );
                } else {
                    _contact.removeValue( Contact.BIRTHDAY, 0 );
                }
            }

            d = (Date) _outer.getItem( ContactObject.FIELD_ANNIVERSARY ).getValue();
            if( _contact.countValues( BlackBerryContact.ANNIVERSARY ) == 0 ) {
                if( d != null ) {
                    _contact.addDate( BlackBerryContact.ANNIVERSARY, PIMItem.ATTR_NONE, d.getTime() );
                }
            } else {
                if( d != null ) {
                    _contact.setDate( BlackBerryContact.ANNIVERSARY, 0, PIMItem.ATTR_NONE, d.getTime() );
                } else {
                    _contact.removeValue( BlackBerryContact.ANNIVERSARY, 0 );
                }
            }

            // user
            value = _outer.getItem( ContactObject.FIELD_USER1 ).getStringValue();
            if( _contact.countValues( BlackBerryContact.USER1 ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( BlackBerryContact.USER1, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( BlackBerryContact.USER1, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( BlackBerryContact.USER1, 0 );
                }
            }

            value = _outer.getItem( ContactObject.FIELD_USER2 ).getStringValue();
            if( _contact.countValues( BlackBerryContact.USER2 ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( BlackBerryContact.USER2, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( BlackBerryContact.USER2, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( BlackBerryContact.USER2, 0 );
                }
            }

            value = _outer.getItem( ContactObject.FIELD_USER3 ).getStringValue();
            if( _contact.countValues( BlackBerryContact.USER3 ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( BlackBerryContact.USER3, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( BlackBerryContact.USER3, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( BlackBerryContact.USER3, 0 );
                }
            }

            value = _outer.getItem( ContactObject.FIELD_USER4 ).getStringValue();
            if( _contact.countValues( BlackBerryContact.USER4 ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( BlackBerryContact.USER4, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( BlackBerryContact.USER4, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( BlackBerryContact.USER4, 0 );
                }
            }

            // pin
            value = _outer.getItem( ContactObject.FIELD_PIN ).getStringValue();
            if( _contact.countValues( BlackBerryContact.PIN ) == 0 ) {
                if( value.length() > 0 ) {
                    _contact.addString( BlackBerryContact.PIN, PIMItem.ATTR_NONE, value );
                }
            } else {
                if( value.length() > 0 ) {
                    _contact.setString( BlackBerryContact.PIN, 0, PIMItem.ATTR_NONE, value );
                } else {
                    _contact.removeValue( BlackBerryContact.PIN, 0 );
                }
            }
            setPhoto();

            // categories
            CategoryNamespace.updateCategories( _contact, contactList, _outer );
        }

        /**
         * Commits the changes
         * 
         * @throws Exception
         */
        public void commit() throws Exception {
            if( _contact == null ) {
                return;
            }

            // save the contact to the address book
            _contact.commit();

            // uid
            final String uid = _contact.getString( Contact.UID, 0 );
            _outer.getItem( ContactObject.FIELD_UID ).setValue( uid );
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
            update();
            commit();

            return UNDEFINED;
        }

        private void setAddress( Contact c, int attribute, String[] address ) {
            if( c.countValues( Contact.ADDR ) == 0 ) {
                if( address[ Contact.ADDR_STREET ].length() > 0 || address[ Contact.ADDR_LOCALITY ].length() > 0
                        || address[ Contact.ADDR_REGION ].length() > 0 || address[ Contact.ADDR_POSTALCODE ].length() > 0
                        || address[ Contact.ADDR_COUNTRY ].length() > 0 || address[ Contact.ADDR_EXTRA ].length() > 0 ) {
                    c.addStringArray( Contact.ADDR, attribute, address );
                }
            } else {
                boolean isExisting = false;
                for( int j = 0; j < c.countValues( Contact.ADDR ); j++ ) {
                    if( attribute == c.getAttributes( Contact.ADDR, j ) ) {
                        if( address[ Contact.ADDR_STREET ].length() > 0 || address[ Contact.ADDR_LOCALITY ].length() > 0
                                || address[ Contact.ADDR_REGION ].length() > 0 || address[ Contact.ADDR_POSTALCODE ].length() > 0
                                || address[ Contact.ADDR_COUNTRY ].length() > 0 || address[ Contact.ADDR_EXTRA ].length() > 0 ) {
                            c.setStringArray( Contact.ADDR, j, attribute, address );
                        } else {
                            c.removeValue( Contact.ADDR, j );
                        }
                        isExisting = true;
                        break;
                    }
                }

                if( !isExisting ) {
                    if( address[ Contact.ADDR_STREET ].length() > 0 || address[ Contact.ADDR_LOCALITY ].length() > 0
                            || address[ Contact.ADDR_REGION ].length() > 0 || address[ Contact.ADDR_POSTALCODE ].length() > 0
                            || address[ Contact.ADDR_COUNTRY ].length() > 0 || address[ Contact.ADDR_EXTRA ].length() > 0 ) {
                        c.addStringArray( Contact.ADDR, attribute, address );
                    }
                }
            }
        }

        private void setTelephone( Contact c, int attribute, String tel ) {
            if( c.countValues( Contact.TEL ) == 0 ) {
                if( tel.length() > 0 ) {
                    c.addString( Contact.TEL, attribute, tel );
                }
            } else {
                boolean isExisting = false;
                for( int j = 0; j < c.countValues( Contact.TEL ); j++ ) {
                    if( attribute == c.getAttributes( Contact.TEL, j ) ) {
                        if( tel.length() > 0 ) {
                            c.setString( Contact.TEL, j, attribute, tel );
                        } else {
                            c.removeValue( Contact.TEL, j );
                        }
                        isExisting = true;
                        break;
                    }
                }

                if( !isExisting ) {
                    if( tel.length() > 0 ) {
                        try {
                            c.addString( Contact.TEL, attribute, tel );
                        } catch( FieldFullException e ) {
                        }
                    }
                }
            }
        }
    }

    private void setPhoto() throws IOException {
        String picture = (String) getItem( FIELD_PICTURE ).getValue();

        if( picture.length() == 0 ) {
            if( _contact.countValues( Contact.PHOTO ) > 0 ) {
                _contact.removeValue( Contact.PHOTO, 0 );
            }
            return;
        }

        byte[] photoEncoded = picture.getBytes( PICTURE_ENCODING );

        if( _contact.countValues( Contact.PHOTO ) > 0 ) {
            _contact.setBinary( Contact.PHOTO, 0, PIMItem.ATTR_NONE, photoEncoded, 0, photoEncoded.length );
        } else {
            _contact.addBinary( Contact.PHOTO, PIMItem.ATTR_NONE, photoEncoded, 0, photoEncoded.length );
        }
    }

    /**
     * This class implements the remove function of a Contact
     * 
     */
    public class ContactRemoveScriptableFunction extends ScriptableFunctionBase {

        /**
         * Default constructor of a ContactRemoveScriptableFunction
         */
        public ContactRemoveScriptableFunction() {
            super();
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {

            // if the _contact is not in ContactList, do nothing
            if( _contact == null ) {
                throw new PIMException( "PIMItem not found." );
            }

            // open the handheld contacts database for removing
            ContactList contactList;
            if( _serviceName.length() == 0 ) {
                contactList = (ContactList) BlackBerryPIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.WRITE_ONLY );
            } else {
                contactList = (ContactList) BlackBerryPIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.WRITE_ONLY,
                        _serviceName );
            }

            contactList.removeContact( _contact );
            _contact = null;

            return UNDEFINED;
        }
    }

    /**
     * This class implements the setPicture method of a Contact
     * 
     */
    public class ContactSetPictureScriptableFunction extends ScriptableFunctionBase {

        /**
         * Default constructor of a ContactSetPictureScriptableFunction
         */
        public ContactSetPictureScriptableFunction() {
            super();
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
            setPictureProperty( (Blob) innerArgs[ 0 ] );
            return UNDEFINED;
        }

        /**
         * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs = new FunctionSignature( 1 );
            fs.addParam( Blob.class, true );
            return new FunctionSignature[] { fs };
        }
    }

    private void setPictureProperty( Blob blobPicture ) throws Exception {
        byte[] photo = blobPicture.getBytes();
        byte[] photoEncoded = Base64OutputStream.encode( photo, 0, photo.length, false, false );

        String picture = new String( photoEncoded, PICTURE_ENCODING );
        getItem( FIELD_PICTURE ).setValue( picture );
    }

    // Injects fields and methods
    private void initial() {
        if( _contact != null ) {
            // name & title
            if( _contact.countValues( Contact.NAME ) == 0 ) {
                addItem( new ScriptField( FIELD_TITLE, "", ScriptField.TYPE_STRING, false, false ) );
                addItem( new ScriptField( FIELD_FIRSTNAME, "", ScriptField.TYPE_STRING, false, false ) );
                addItem( new ScriptField( FIELD_LASTNAME, "", ScriptField.TYPE_STRING, false, false ) );
            } else {
                String[] name;
                name = _contact.getStringArray( Contact.NAME, 0 );
                addItem( new ScriptField( FIELD_TITLE,
                        ( name[ Contact.NAME_PREFIX ] != null ) ? name[ Contact.NAME_PREFIX ] : "", ScriptField.TYPE_STRING,
                        false, false ) );
                addItem( new ScriptField( FIELD_FIRSTNAME, ( name[ Contact.NAME_GIVEN ] != null ) ? name[ Contact.NAME_GIVEN ]
                        : "", ScriptField.TYPE_STRING, false, false ) );
                addItem( new ScriptField( FIELD_LASTNAME, ( name[ Contact.NAME_FAMILY ] != null ) ? name[ Contact.NAME_FAMILY ]
                        : "", ScriptField.TYPE_STRING, false, false ) );
            }

            // homeAddress & workAddress
            AddressObject homeAddress = new AddressObject();
            AddressObject workAddress = new AddressObject();
            addItem( new ScriptField( FIELD_HOMEADDRESS, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            addItem( new ScriptField( FIELD_WORKADDRESS, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );

            int count = _contact.countValues( Contact.ADDR );
            if( count > 0 ) {
                for( int j = 0; j < count; j++ ) {
                    String[] address;
                    address = _contact.getStringArray( Contact.ADDR, j );
                    int attribute = _contact.getAttributes( Contact.ADDR, j );

                    if( Contact.ATTR_HOME == attribute ) {
                        populateAddressObject( homeAddress, address );
                        addItem( new ScriptField( FIELD_HOMEADDRESS, homeAddress, ScriptField.TYPE_SCRIPTABLE, false, false ) );
                    } else if( Contact.ATTR_WORK == attribute ) {
                        populateAddressObject( workAddress, address );
                        addItem( new ScriptField( FIELD_WORKADDRESS, workAddress, ScriptField.TYPE_SCRIPTABLE, false, false ) );
                    }
                }
            }

            // email
            addItem( new ScriptField( FIELD_EMAIL1, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_EMAIL2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_EMAIL3, "", ScriptField.TYPE_STRING, false, false ) );

            count = _contact.countValues( Contact.EMAIL );
            if( count > 0 ) {
                for( int j = 0; j < count; j++ ) {
                    String email = _contact.getString( Contact.EMAIL, j );
                    if( j == 0 ) {
                        addItem( new ScriptField( FIELD_EMAIL1, email, ScriptField.TYPE_STRING, false, false ) );
                    } else if( j == 1 ) {
                        addItem( new ScriptField( FIELD_EMAIL2, email, ScriptField.TYPE_STRING, false, false ) );
                    } else if( j == 2 ) {
                        addItem( new ScriptField( FIELD_EMAIL3, email, ScriptField.TYPE_STRING, false, false ) );
                    }
                }
            }

            // company
            if( _contact.countValues( Contact.ORG ) > 0 ) {
                addItem( new ScriptField( FIELD_COMPANY, _contact.getString( Contact.ORG, 0 ), ScriptField.TYPE_STRING, false,
                        false ) );
            } else {
                addItem( new ScriptField( FIELD_COMPANY, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // jobtitle
            if( _contact.countValues( Contact.TITLE ) > 0 ) {
                addItem( new ScriptField( FIELD_JOBTITLE, _contact.getString( Contact.TITLE, 0 ), ScriptField.TYPE_STRING, false,
                        false ) );
            } else {
                addItem( new ScriptField( FIELD_JOBTITLE, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // phone
            addItem( new ScriptField( FIELD_HOMEPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_HOMEPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_WORKPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_WORKPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_MOBILEPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_MOBILEPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FAXPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FAXPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_PAGERPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_OTHERPHONE, "", ScriptField.TYPE_STRING, false, false ) );

            count = _contact.countValues( Contact.TEL );
            if( count > 0 ) {
                for( int j = 0; j < count; j++ ) {
                    String tel = _contact.getString( Contact.TEL, j );
                    int attribute = _contact.getAttributes( Contact.TEL, j );

                    if( Contact.ATTR_HOME == attribute ) {
                        addItem( new ScriptField( FIELD_HOMEPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( BlackBerryContact.ATTR_HOME2 == attribute ) {
                        addItem( new ScriptField( FIELD_HOMEPHONE2, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( Contact.ATTR_WORK == attribute ) {
                        addItem( new ScriptField( FIELD_WORKPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( BlackBerryContact.ATTR_WORK2 == attribute ) {
                        addItem( new ScriptField( FIELD_WORKPHONE2, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( Contact.ATTR_MOBILE == attribute ) {
                        addItem( new ScriptField( FIELD_MOBILEPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( Contact.ATTR_FAX == attribute ) {
                        addItem( new ScriptField( FIELD_FAXPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( Contact.ATTR_PAGER == attribute ) {
                        addItem( new ScriptField( FIELD_PAGERPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    } else if( Contact.ATTR_OTHER == attribute ) {
                        addItem( new ScriptField( FIELD_OTHERPHONE, tel, ScriptField.TYPE_STRING, false, false ) );
                    }
                }
            }

            // note
            if( _contact.countValues( Contact.NOTE ) > 0 ) {
                addItem( new ScriptField( FIELD_NOTE, _contact.getString( Contact.NOTE, 0 ), ScriptField.TYPE_STRING, false,
                        false ) );
            } else {
                addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // webpage
            try {
                if( _contact.countValues( Contact.URL ) > 0 ) {
                    addItem( new ScriptField( FIELD_WEBPAGE, _contact.getString( Contact.URL, 0 ), ScriptField.TYPE_STRING,
                            false, false ) );
                } else {
                    addItem( new ScriptField( FIELD_WEBPAGE, "", ScriptField.TYPE_STRING, false, false ) );
                }
            } catch( UnsupportedFieldException e ) {
                addItem( new ScriptField( FIELD_WEBPAGE, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // birthday & anniiversary
            if( _contact.countValues( Contact.BIRTHDAY ) > 0 ) {
                addItem( new ScriptField( FIELD_BIRTHDAY, new Date( _contact.getDate( Contact.BIRTHDAY, 0 ) ),
                        ScriptField.TYPE_DATE, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_BIRTHDAY, null, ScriptField.TYPE_DATE, false, false ) );
            }

            if( _contact.countValues( BlackBerryContact.ANNIVERSARY ) > 0 ) {
                addItem( new ScriptField( FIELD_ANNIVERSARY, new Date( _contact.getDate( BlackBerryContact.ANNIVERSARY, 0 ) ),
                        ScriptField.TYPE_DATE, false, false ) );
            } else {
                addItem( new ScriptField( FIELD_ANNIVERSARY, null, ScriptField.TYPE_DATE, false, false ) );
            }

            // user
            if( _contact.countValues( BlackBerryContact.USER1 ) > 0 ) {
                addItem( new ScriptField( FIELD_USER1, _contact.getString( BlackBerryContact.USER1, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            } else {
                addItem( new ScriptField( FIELD_USER1, "", ScriptField.TYPE_STRING, false, false ) );
            }

            if( _contact.countValues( BlackBerryContact.USER2 ) > 0 ) {
                addItem( new ScriptField( FIELD_USER2, _contact.getString( BlackBerryContact.USER2, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            } else {
                addItem( new ScriptField( FIELD_USER2, "", ScriptField.TYPE_STRING, false, false ) );
            }

            if( _contact.countValues( BlackBerryContact.USER3 ) > 0 ) {
                addItem( new ScriptField( FIELD_USER3, _contact.getString( BlackBerryContact.USER3, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            } else {
                addItem( new ScriptField( FIELD_USER3, "", ScriptField.TYPE_STRING, false, false ) );
            }

            if( _contact.countValues( BlackBerryContact.USER4 ) > 0 ) {
                addItem( new ScriptField( FIELD_USER4, _contact.getString( BlackBerryContact.USER4, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            } else {
                addItem( new ScriptField( FIELD_USER4, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // pin
            if( _contact.countValues( BlackBerryContact.PIN ) > 0 ) {
                addItem( new ScriptField( FIELD_PIN, _contact.getString( BlackBerryContact.PIN, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            } else {
                addItem( new ScriptField( FIELD_PIN, "", ScriptField.TYPE_STRING, false, false ) );
            }

            // uid
            if( _contact.countValues( Contact.UID ) > 0 ) {
                addItem( new ScriptField( FIELD_UID, _contact.getString( Contact.UID, 0 ), ScriptField.TYPE_STRING, true, false ) );
            } else {
                addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );
            }

            // picture
            if( _contact.countValues( Contact.PHOTO ) > 0 ) {
                byte[] photoEncoded = _contact.getBinary( Contact.PHOTO, 0 );
                String picture = "";
                try {
                    picture = new String( photoEncoded, PICTURE_ENCODING );
                } catch( UnsupportedEncodingException e ) {
                }
                addItem( new ScriptField( FIELD_PICTURE, picture, ScriptField.TYPE_STRING, true, false ) );
            } else {
                addItem( new ScriptField( FIELD_PICTURE, "", ScriptField.TYPE_STRING, true, false ) );
            }

            // categories
            addItem( new ScriptField( FIELD_CATEGORIES, _contact.getCategories(), ScriptField.TYPE_SCRIPTABLE, false, false ) );
        } else {
            // name & title
            addItem( new ScriptField( FIELD_TITLE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FIRSTNAME, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_LASTNAME, "", ScriptField.TYPE_STRING, false, false ) );

            // homeAddress & workAddress
            addItem( new ScriptField( FIELD_HOMEADDRESS, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
            addItem( new ScriptField( FIELD_WORKADDRESS, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );

            // email
            addItem( new ScriptField( FIELD_EMAIL1, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_EMAIL2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_EMAIL3, "", ScriptField.TYPE_STRING, false, false ) );

            // company
            addItem( new ScriptField( FIELD_COMPANY, "", ScriptField.TYPE_STRING, false, false ) );

            // jobtitle
            addItem( new ScriptField( FIELD_JOBTITLE, "", ScriptField.TYPE_STRING, false, false ) );

            // phone
            addItem( new ScriptField( FIELD_HOMEPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_HOMEPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_WORKPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_WORKPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_MOBILEPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_MOBILEPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FAXPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_FAXPHONE2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_PAGERPHONE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_OTHERPHONE, "", ScriptField.TYPE_STRING, false, false ) );

            // note
            addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );

            // webpage
            addItem( new ScriptField( FIELD_WEBPAGE, "", ScriptField.TYPE_STRING, false, false ) );

            // birthday & anniiversary
            addItem( new ScriptField( FIELD_BIRTHDAY, null, ScriptField.TYPE_DATE, false, false ) );
            addItem( new ScriptField( FIELD_ANNIVERSARY, null, ScriptField.TYPE_DATE, false, false ) );

            // user
            addItem( new ScriptField( FIELD_USER1, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_USER2, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_USER3, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_USER4, "", ScriptField.TYPE_STRING, false, false ) );

            // pin
            addItem( new ScriptField( FIELD_PIN, "", ScriptField.TYPE_STRING, false, false ) );

            // uid
            addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );

            // picture
            addItem( new ScriptField( FIELD_PICTURE, "", ScriptField.TYPE_STRING, true, false ) );

            // categories
            addItem( new ScriptField( FIELD_CATEGORIES, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
        }

        _save = new ContactSaveScriptableFunction();
        _remove = new ContactRemoveScriptableFunction();
        _setPicture = new ContactSetPictureScriptableFunction();

        addItem( new ScriptField( METHOD_SAVE, _save, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_REMOVE, _remove, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_SETPICTURE, _setPicture, ScriptField.TYPE_SCRIPTABLE, true, true ) );
    }

    private void populateAddressObject( AddressObject addressObject, String[] address ) {
        if( address[ Contact.ADDR_STREET ] != null )
            addressObject.getItem( AddressObject.FIELD_ADDRESS1 ).setValue( address[ Contact.ADDR_STREET ] );
        if( address[ Contact.ADDR_LOCALITY ] != null )
            addressObject.getItem( AddressObject.FIELD_CITY ).setValue( address[ Contact.ADDR_LOCALITY ] );
        if( address[ Contact.ADDR_REGION ] != null )
            addressObject.getItem( AddressObject.FIELD_STATE ).setValue( address[ Contact.ADDR_REGION ] );
        if( address[ Contact.ADDR_POSTALCODE ] != null )
            addressObject.getItem( AddressObject.FIELD_ZIP ).setValue( address[ Contact.ADDR_POSTALCODE ] );
        if( address[ Contact.ADDR_COUNTRY ] != null )
            addressObject.getItem( AddressObject.FIELD_COUNTRY ).setValue( address[ Contact.ADDR_COUNTRY ] );
        if( address[ Contact.ADDR_EXTRA ] != null )
            addressObject.getItem( AddressObject.FIELD_ADDRESS2 ).setValue( address[ Contact.ADDR_EXTRA ] );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the ContactObject's underlying content.
     * 
     * @return the contained blackberry contact item.
     */
    public Contact getContact() {
        return _contact;
    }

    /**
     * Returns the categories of a Contact object
     * 
     * @return the categories
     * @throws Exception
     *             when the categories cannot be obtained
     */
    public String[] getCategories() throws Exception {
        return CategoryNamespace.getCategoriesFromScriptField( getItem( FIELD_CATEGORIES ) );
    }

    /**
     * Updates the Contact object
     * 
     * @throws Exception
     */
    public void update() throws Exception {
        _save.update();
    }

    /**
     * Saves the Contact object
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        _save.execute( null, null );
    }
}

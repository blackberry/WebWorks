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
package blackberry.invoke;

import java.util.Calendar;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.Event;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.ToDo;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.invoke.AddressBookArguments;
import net.rim.blackberry.api.invoke.CalendarArguments;
import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MapsArguments;
import net.rim.blackberry.api.invoke.MemoArguments;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.invoke.SearchArguments;
import net.rim.blackberry.api.invoke.TaskArguments;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.pdap.BlackBerryMemo;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.invoke.addressBookArguments.AddressBookArgumentsConstructor;
import blackberry.invoke.addressBookArguments.AddressBookArgumentsObject;
import blackberry.invoke.browserArguments.BrowserArgumentsObject;
import blackberry.invoke.calendarArguments.CalendarArgumentsConstructor;
import blackberry.invoke.calendarArguments.CalendarArgumentsObject;
import blackberry.invoke.cameraArguments.CameraArgumentsConstructor;
import blackberry.invoke.cameraArguments.CameraArgumentsObject;
import blackberry.invoke.javaArguments.JavaArgumentsObject;
import blackberry.invoke.mapsArguments.MapsArgumentsObject;
import blackberry.invoke.memoArguments.MemoArgumentsConstructor;
import blackberry.invoke.memoArguments.MemoArgumentsObject;
import blackberry.invoke.messageArguments.MessageArgumentsConstructor;
import blackberry.invoke.messageArguments.MessageArgumentsObject;
import blackberry.invoke.phoneArguments.PhoneArgumentsConstructor;
import blackberry.invoke.phoneArguments.PhoneArgumentsObject;
import blackberry.invoke.searchArguments.SearchArgumentsObject;
import blackberry.invoke.taskArguments.TaskArgumentsConstructor;
import blackberry.invoke.taskArguments.TaskArgumentsObject;
import blackberry.pim.address.AddressObject;
import blackberry.pim.appointment.AppointmentObject;
import blackberry.pim.contact.ContactObject;
import blackberry.pim.memo.MemoObject;
import blackberry.pim.task.TaskObject;

/**
 * The InvokeFunction class extends ScriptableFunctionBase.
 * 
 * @author sgolod
 * 
 */
public class InvokeFunction extends ScriptableFunctionBase {
    public static final String NAME = "invoke";

    final static String BROWSER_URL = "net_rim_bb_browser_daemon";

    // Work Around for OS 5.0: this static variable can't be accessed via MessageArguments.ARG_SEARCH as it states in the API.
    private static final String MESSAGEARGUMENTS_ARG_SEARCH = "search_invoke";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    public Object execute( final Object thiz, final Object[] args ) throws Exception {
        // Parse appType, arg
        final Integer i = (Integer) args[ 0 ];
        Object o = null;
        final int appType = i.intValue();
        if( args.length == 2 ) {
            o = args[ 1 ];
        }

        switch( appType ) {
            case InvokeNamespace.APP_ADDRESSBOOK:
                invokeAddressBook( o );
                break;
            case InvokeNamespace.APP_BLUETOOTH_CONFIG:
                invokeBluetoothConfig();
                break;
            case InvokeNamespace.APP_CALCULATOR:
                invokeCalculator();
                break;
            case InvokeNamespace.APP_CALENDAR:
                invokeCalendar( o );
                break;
            case InvokeNamespace.APP_CAMERA:
                invokeCamera( o );
                break;
            case InvokeNamespace.APP_MAPS:
                invokeMaps( o );
                break;
            case InvokeNamespace.APP_MEMOPAD:
                invokeMemo( o );
                break;
            case InvokeNamespace.APP_MESSAGES:
                invokeMessages( o );
                break;
            case InvokeNamespace.APP_PHONE:
                invokePhone( o );
                break;
            case InvokeNamespace.APP_SEARCH:
                invokeSearch( o );
                break;
            case InvokeNamespace.APP_TASKS:
                invokeTasks( o );
                break;
            case InvokeNamespace.APP_BROWSER:
                invokeBrowser( o );
                break;
            case InvokeNamespace.APP_JAVA:
                invokeJavaApp( o );
                break;
            default:
                // already checked - so should not happen
                break;
        }
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#validateArgs(java.lang.Object[])
     */
    protected void validateArgs( final Object[] args ) {
        super.validateArgs( args );

        final Integer i = (Integer) args[ 0 ];
        final int appType = i.intValue();
        Class classType = null;

        switch( appType ) {
            case InvokeNamespace.APP_ADDRESSBOOK:
                classType = AddressBookArgumentsObject.class;
                break;
            case InvokeNamespace.APP_CALENDAR:
                classType = CalendarArgumentsObject.class;
                break;
            case InvokeNamespace.APP_CAMERA:
                classType = CameraArgumentsObject.class;
                break;
            case InvokeNamespace.APP_MAPS:
                classType = MapsArgumentsObject.class;
                break;
            case InvokeNamespace.APP_MEMOPAD:
                classType = MemoArgumentsObject.class;
                break;
            case InvokeNamespace.APP_MESSAGES:
                classType = MessageArgumentsObject.class;
                break;
            case InvokeNamespace.APP_PHONE:
                classType = PhoneArgumentsObject.class;
                break;
            case InvokeNamespace.APP_SEARCH:
                classType = SearchArgumentsObject.class;
                break;
            case InvokeNamespace.APP_TASKS:
                classType = TaskArgumentsObject.class;
                break;
            case InvokeNamespace.APP_BROWSER:
                classType = BrowserArgumentsObject.class;
                break;
            case InvokeNamespace.APP_JAVA:
                if( args.length == 1 ) {
                    throw new IllegalArgumentException( "Missing required argument: 'args'" );
                }
                classType = JavaArgumentsObject.class;
                break;
            case InvokeNamespace.APP_BLUETOOTH_CONFIG:
            case InvokeNamespace.APP_CALCULATOR:
                if( args.length == 2 ) {
                    throw new IllegalArgumentException( "Too many arguments" );
                }
                return;
            default:
                throw new IllegalArgumentException( "Invalid 'appType'" );
        }

        // validate type
        if( args.length == 2 && args[ 1 ] != null && !classType.isAssignableFrom( args[ 1 ].getClass() ) ) {
            throw new IllegalArgumentException( "Invalid type: 'args'" );
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( Integer.class, true );
        fs.addNullableParam( Object.class, false );
        return new FunctionSignature[] { fs };
    }

    private void invokeAddressBook( final Object arg ) throws Exception {
        AddressBookArguments args = null;

        if( arg instanceof AddressBookArgumentsObject ) {
            final AddressBookArgumentsObject a = (AddressBookArgumentsObject) arg;

            final int view = a.getView();
            String viewArg = null;
            final ContactObject co = a.getContactObject();
            if( co != null ) {
                co.update();
            }
            Contact c = a.getContact();

            switch( view ) {
                case AddressBookArgumentsConstructor.VIEW_NEW:
                    if( c == null ) {
                        // Create a new contact
                        try {
                            final ContactList contactList = (ContactList) PIM.getInstance().openPIMList( PIM.CONTACT_LIST,
                                    PIM.READ_WRITE );
                            c = contactList.createContact();
                        } catch( final PIMException e ) {
                            throw e;
                        }
                    }
                    viewArg = AddressBookArguments.ARG_NEW;
                    break;
                case AddressBookArgumentsConstructor.VIEW_COMPOSE:
                    viewArg = AddressBookArguments.ARG_COMPOSE;
                    break;
                case AddressBookArgumentsConstructor.VIEW_DISPLAY:
                    viewArg = AddressBookArguments.ARG_VIEW;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            if( c == null ) {
                args = new AddressBookArguments( viewArg );
            } else {
                args = new AddressBookArguments( viewArg, c );
            }
        } else {
            args = new AddressBookArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_ADDRESSBOOK, args );
    }

    private void invokeBluetoothConfig() throws Exception {
        Invoke.invokeApplication( Invoke.APP_TYPE_BLUETOOTH_CONFIG, null );
    }

    private void invokeCalculator() throws Exception {
        Invoke.invokeApplication( Invoke.APP_TYPE_CALCULATOR, null );
    }

    private void invokeCalendar( final Object arg ) throws Exception {
        CalendarArguments args = null;

        if( arg instanceof CalendarArgumentsObject ) {
            final CalendarArgumentsObject c = (CalendarArgumentsObject) arg;

            // get View parameter
            final int view = c.getView();
            String viewArg = null;

            switch( view ) {
                case CalendarArgumentsConstructor.VIEW_NEW:
                    viewArg = CalendarArguments.ARG_NEW;
                    break;
                case CalendarArgumentsConstructor.VIEW_DAY:
                    viewArg = CalendarArguments.ARG_VIEW_DAY;
                    break;
                case CalendarArgumentsConstructor.VIEW_WEEK:
                    viewArg = CalendarArguments.ARG_VIEW_WEEK;
                    break;
                case CalendarArgumentsConstructor.VIEW_MONTH:
                    viewArg = CalendarArguments.ARG_VIEW_MONTH;
                    break;
                case CalendarArgumentsConstructor.VIEW_DEFAULT:
                    viewArg = CalendarArguments.ARG_VIEW_DEFAULT;
                    break;
                case CalendarArgumentsConstructor.VIEW_AGENDA:
                    viewArg = CalendarArguments.ARG_VIEW_AGENDA;
                    break;
                case CalendarArgumentsConstructor.VIEW_VIEW:
                    viewArg = CalendarArguments.ARG_VIEW_DEFAULT;
                    break;
                default:
                    viewArg = CalendarArguments.ARG_NEW;
                    break;
            }

            final AppointmentObject ao = c.getAppointmentObject();
            if( ao != null ) {
                ao.update();
            }

            final Event event = c.getEvent();
            final Calendar calendar = c.getCalendar();

            if( event != null ) {
                args = new CalendarArguments( viewArg, event );
            } else if( calendar != null ) {
                args = new CalendarArguments( viewArg, calendar );
            } else {
                args = new CalendarArguments( viewArg );
            }
        } else {
            args = new CalendarArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_CALENDAR, args );
    }

    private void invokeCamera( final Object arg ) throws Exception {
        CameraArguments args = null;

        if( arg instanceof CameraArgumentsObject ) {
            final CameraArgumentsObject c = (CameraArgumentsObject) arg;
            final int view = c.getView();
            if( view != CameraArgumentsConstructor.VIEW_CAMERA && view != CameraArgumentsConstructor.VIEW_RECORDER ) {
                throw new IllegalArgumentException( "Invalid view for CameraArgumentsObject." );
            }

            args = new CameraArguments(
                    c.getView() == CameraArgumentsConstructor.VIEW_RECORDER ? CameraArguments.ARG_VIDEO_RECORDER
                            : CameraArguments.ARG_CAMERA_APP );
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_CAMERA, args );
    }

    private void invokeMaps( final Object arg ) throws Exception {
        MapsArguments args = null;

        if( arg instanceof MapsArgumentsObject ) {
            final MapsArgumentsObject m = (MapsArgumentsObject) arg;

            if( m.isDefault() ) {
                args = new MapsArguments();
            } else {
                // Populate location string from document
                final String xml = m.getXML();
                if( xml != null ) {
                    args = new MapsArguments( MapsArguments.ARG_LOCATION_DOCUMENT, xml );
                } else {
                    // Populate location string from address
                    final AddressObject addressObject = m.getAddressObject();
                    if( addressObject != null ) {
                        final String locality = addressObject.getItem( AddressObject.FIELD_CITY ).getStringValue();
                        final String region = addressObject.getItem( AddressObject.FIELD_STATE ).getStringValue();

                        if( locality.length() > 0 && region.length() > 0 ) {
                            Contact c;

                            // Create a new contact
                            try {
                                final ContactList contactList = (ContactList) PIM.getInstance().openPIMList( PIM.CONTACT_LIST,
                                        PIM.READ_WRITE );
                                c = contactList.createContact();
                            } catch( final PIMException e ) {
                                throw e;
                            }

                            final String[] addressHome = new String[ 7 ];
                            addressHome[ Contact.ADDR_STREET ] = addressObject.getItem( AddressObject.FIELD_ADDRESS1 )
                                    .getStringValue();
                            addressHome[ Contact.ADDR_LOCALITY ] = addressObject.getItem( AddressObject.FIELD_CITY )
                                    .getStringValue();
                            addressHome[ Contact.ADDR_REGION ] = addressObject.getItem( AddressObject.FIELD_STATE )
                                    .getStringValue();
                            addressHome[ Contact.ADDR_POSTALCODE ] = addressObject.getItem( AddressObject.FIELD_ZIP )
                                    .getStringValue();
                            addressHome[ Contact.ADDR_COUNTRY ] = addressObject.getItem( AddressObject.FIELD_COUNTRY )
                                    .getStringValue();
                            addressHome[ Contact.ADDR_EXTRA ] = addressObject.getItem( AddressObject.FIELD_ADDRESS2 )
                                    .getStringValue();

                            c.addStringArray( Contact.ADDR, Contact.ATTR_HOME, addressHome );
                            args = new MapsArguments( c, 0 );
                        } else {
                            throw new IllegalArgumentException( "Address should contain 'city' and 'state' information!" );
                        }
                    } else {
                        // Populate location string from coordinate
                        final long lat = (long) ( m.getLatitude() * 100000 );
                        final long lon = (long) ( m.getLongitude() * 100000 );
                        final String locationCoordinate = "<lbs clear='ALL'>" + "<location lon='" + new Long( lon ).toString()
                                + "' lat='" + new Long( lat ).toString() + "'/>" + "</lbs>";
                        args = new MapsArguments( MapsArguments.ARG_LOCATION_DOCUMENT, locationCoordinate );
                    }
                }
            }
        } else {
            args = new MapsArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_MAPS, args );
    }

    private void invokeMemo( final Object arg ) throws Exception {
        MemoArguments args = null;

        if( arg instanceof MemoArgumentsObject ) {
            final MemoArgumentsObject m = (MemoArgumentsObject) arg;

            final MemoObject mo = m.getMemoObject();
            if( mo != null ) {
                mo.update();
            }

            final BlackBerryMemo bbm = m.getMemo();
            final int view = m.getView();

            if( bbm == null ) {
                args = new MemoArguments( MemoArguments.ARG_NEW );
            } else {
                args = new MemoArguments( view == MemoArgumentsConstructor.VIEW_EDIT ? MemoArguments.ARG_EDIT
                        : MemoArguments.ARG_NEW, bbm );
            }
        } else {
            args = new MemoArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_MEMOPAD, args );
    }

    private void invokeMessages( final Object arg ) throws Exception {
        MessageArguments args = null;

        if( arg instanceof MessageArgumentsObject ) {
            final MessageArgumentsObject m = (MessageArgumentsObject) arg;
            final Message msg = m.getMessage();
            if( msg != null ) {
                args = new MessageArguments( msg );
            } else {
                final int view = m.getView();
                String viewArg = null;

                switch( view ) {
                    case MessageArgumentsConstructor.VIEW_NEW:
                        viewArg = MessageArguments.ARG_NEW;
                        break;
                    case MessageArgumentsConstructor.VIEW_DEFAULT:
                        viewArg = MessageArguments.ARG_DEFAULT;
                        break;
                    case MessageArgumentsConstructor.VIEW_SAVED:
                        viewArg = MessageArguments.ARG_SAVED;
                        break;
                    case MessageArgumentsConstructor.VIEW_SEARCH:
                        viewArg = MESSAGEARGUMENTS_ARG_SEARCH;
                        break;
                    default:
                        viewArg = CalendarArguments.ARG_NEW;
                        break;
                }
                args = new MessageArguments( viewArg );
            }
        } else {
            args = new MessageArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_MESSAGES, args );
    }

    private void invokePhone( final Object arg ) throws Exception {
        PhoneArguments args = null;

        if( arg instanceof PhoneArgumentsObject ) {
            final PhoneArgumentsObject p = (PhoneArgumentsObject) arg;

            final boolean isSmartDialing = p.isSmartDialing();
            final int callType = p.getView();
            final int lineId = p.getLineId();

            if( lineId != PhoneArgumentsConstructor.NO_LINEID && callType == PhoneArgumentsConstructor.VIEW_VOICEMAIL ) {
                // net.rim.blackberry.api.phone.Phone does not support using 'voicemail' to get to the device's
                // voicemail account, since Invoke.invokeApplication doesn't support line id's, we can't
                // handle the case where a developer selects a LINE_ID and wants to call it's specific voicemail.
                throw new IllegalArgumentException();
            }

            String phoneNumber;

            if( callType == PhoneArgumentsConstructor.VIEW_VOICEMAIL ) {
                phoneNumber = PhoneArguments.VOICEMAIL;
            } else {
                phoneNumber = p.getDialString();
            }

            if( phoneNumber == null || phoneNumber.length() == 0 ) {
                args = new PhoneArguments();
            } else if( lineId != PhoneArgumentsConstructor.NO_LINEID ) {
                net.rim.blackberry.api.phone.Phone.initiateCall( lineId, phoneNumber );
                return;
            } else {
                args = new PhoneArguments( PhoneArguments.ARG_CALL, phoneNumber, isSmartDialing );
            }
        } else {
            args = new PhoneArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_PHONE, args );
    }

    private void invokeSearch( final Object arg ) throws Exception {
        SearchArguments args = null;

        if( arg instanceof SearchArgumentsObject ) {
            final SearchArgumentsObject s = (SearchArgumentsObject) arg;
            args = new SearchArguments( s.getText(), s.getName() );
        } else {
            args = new SearchArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_SEARCH, args );
    }

    private void invokeTasks( final Object arg ) throws Exception {
        TaskArguments args = null;

        if( arg instanceof TaskArgumentsObject ) {
            final TaskArgumentsObject t = (TaskArgumentsObject) arg;

            final TaskObject to = t.getTaskObject();
            if( to != null ) {
                to.update();
            }

            final ToDo todo = t.getTodo();
            final int view = t.getView();

            if( todo == null ) {
                args = new TaskArguments( TaskArguments.ARG_NEW );
            } else {
                args = new TaskArguments( view == TaskArgumentsConstructor.VIEW_EDIT ? TaskArguments.ARG_VIEW
                        : TaskArguments.ARG_NEW, todo );
            }
        } else {
            args = new TaskArguments();
        }

        Invoke.invokeApplication( Invoke.APP_TYPE_TASKS, args );
    }

    private void invokeBrowser( final Object arg ) throws Exception {
        if( arg instanceof BrowserArgumentsObject ) {
            final BrowserArgumentsObject b = (BrowserArgumentsObject) arg;
            final String url = b.getUrl();
            BrowserSession bs = null;

            if( b.getTransportObject() == null ) {
                bs = Browser.getDefaultSession();
            } else {
                bs = Browser.getSession( b.getTransportObject().getUID() );
            }

            bs.displayPage( url );

        } else {
            try {
                ApplicationManager.getApplicationManager().launch( BROWSER_URL );
            } catch( final ApplicationManagerException e ) {
                throw new ApplicationManagerException( "Failed to invoke net_rim_bb_browser_daemon.\n" + e.getMessage() );
            }
        }
    }

    private void invokeJavaApp( final Object arg ) throws Exception {
        if( arg instanceof JavaArgumentsObject ) {
            final JavaArgumentsObject j = (JavaArgumentsObject) arg;

            final String url = j.getUrl();

            try {
                ApplicationManager.getApplicationManager().launch( url );
            } catch( final ApplicationManagerException e ) {
                throw new ApplicationManagerException( "Failed to invoke Java application url=" + url + "\n" + e.getMessage() );
            }
        } else {
            throw new IllegalArgumentException( "Argument is null." );
        }
    }
}

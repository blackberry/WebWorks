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
package blackberry.bbm.platform.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.util.Enumeration;

import javax.microedition.io.InputConnection;

import blackberry.bbm.platform.BBMPlatformExtension;
import blackberry.bbm.platform.users.BBMPlatformUser;
import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.blackberry.api.bbm.platform.profile.ContactListProvider;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldController;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.text.TextFilter;
import net.rim.device.api.util.AbstractStringWrapper;
import net.rim.device.api.util.ByteVector;

public class Util {
    
    public static final long EVENT_LOGGER_GUID = 0xba7272b3d84525deL; //blackberry.bbm.platform.BBMPlatformExtension
    
    /**
     * Returns the base 64 string representation of a bitmap. This is suitable for rendering in on
     * an HTML page.
     * @param bitmap The bitmap.
     * @return the base 64 string representation of a bitmap. Returns null if <code>bitmap</code>
     * is null.
     */
    public static String bitmapToBase64Str(Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }
        
        final PNGEncodedImage pngImage = PNGEncodedImage.encode(bitmap);
        final byte[] pngData = pngImage.getData();
        try {
            final byte[] base64PngData = Base64OutputStream.encode(pngData, 0, pngData.length, false, false);
            return "data:image/png;base64," + new String(base64PngData, "UTF-8");
        } catch(Exception e) {
            return null;
        }
    }
    
    public static Bitmap requestBitmap(String uri) throws Exception {
        byte[] bmpBytes = requestBitmapBytes(uri);
        return Bitmap.createBitmapFromBytes(bmpBytes, 0, bmpBytes.length, 1);
    }
    
    public static byte[] requestBitmapBytes(String uri) throws Exception {
        BrowserField browserField = (BrowserField) BBMPlatformExtension._browserField.get();
        final BrowserFieldConfig bfConfig = browserField.getConfig();
        final BrowserFieldController bfController =
                (BrowserFieldController) bfConfig.getProperty(BrowserFieldConfig.CONTROLLER);
        
        InputConnection ic = null;
        DataInputStream is = null;
        
        try {
            final BrowserFieldRequest bfReq = new BrowserFieldRequest(uri);
            ic = bfController.handleResourceRequest(bfReq);
            is = ic.openDataInputStream();
            
            final ByteVector bmpBytes = new ByteVector();
            try {
                while(true) {
                    bmpBytes.addElement(is.readByte());
                }
            } catch(EOFException e) {
            }
            
            return bmpBytes.getArray();
        } finally {
            try {
                ic.close();
            } catch(Exception e) { }
            
            try {
                is.close();
            } catch(Exception e) { }
        }
    }
    
    public static BBMPlatformUser[] scriptableUsersArrayToUserArray(Scriptable array) {
        BBMPlatformUser[] users;
        try {
            final int numUsers = array.getElementCount();
            users = new BBMPlatformUser[numUsers];
            for(int i = 0; i < numUsers; i++) {
                users[i] = (BBMPlatformUser) array.getElement(i);
            }
        } catch(Exception e) {
            users = null;
        }
        return users;
    }
    
    public static Object contactListToArray(BBMPlatformContactList contactList) {
        final Enumeration contactsEnum = contactList.getAll();
        
        // Create array of scriptable BBM platform user objects
        final BBMPlatformUser[] userArray = new BBMPlatformUser[contactList.size()];
        int i = 0;
        while(contactsEnum.hasMoreElements()) {
            BBMPlatformContact contact = (BBMPlatformContact) contactsEnum.nextElement();
            userArray[i++] = new BBMPlatformUser(contact);
        }
        return wrapUserArrayIn50(userArray);
    }
    
    public static Object wrapUserArrayIn50(BBMPlatformUser[] users) {
        if(DeviceInfo.getSoftwareVersion().compareTo("6.0.0") >= 0) {
            return users;
        } else {
            return new Util.ScriptableUsersArray(users);
        }
    }
    
    public static BBMPlatformContactList userArrayToContactList(BBMPlatformUser[] users) {
        final BBMPlatformContactList contactList = new BBMPlatformContactList();
        for(int i = 0; i < users.length; i++) {
            BBMPlatformContact contact = (BBMPlatformContact) users[i].getPresence();
            contactList.add(contact);
        }
        return contactList;
    }
    
    public static class SimpleContactListProvider implements ContactListProvider {
        
        private final BBMPlatformContact[] _users;
        
        public SimpleContactListProvider(BBMPlatformUser[] users) {
            _users = new BBMPlatformContact[users.length];
            for(int i = 0; i < users.length; i++) {
                _users[i] = (BBMPlatformContact) users[i].getPresence();
            }
        }

        public BBMPlatformContactList getContactsForGroup(int groupIndex) {
            if(groupIndex == 0) {
                final BBMPlatformContactList contacts = new BBMPlatformContactList();
                for(int i = 0; i < _users.length; i++) {
                    contacts.add(_users[i]);
                }
                return contacts;
            } else {
                return null;
            }
        }

        public int getDefaultGroupIndex() {
            return 0;
        }
        
        public String[] getGroupNames() {
            return new String[] { "Contacts" };
        }
    }
    
    public static int groupTypeStrToInt(String groupType) {
        if(groupType.equals("contactswithapp")) {
            return ContactListProvider.BBM_CONTACTS_WITH_APP;
        } else {
            throw new IllegalArgumentException("Invalide type: " + groupType);
        }
    }
    
    public static boolean isValidPIN(String pin) {
        final int pinLength = 8;
        if(pin == null || pin.length() != pinLength) {
            return false;
        } else {
            pin = pin.toUpperCase();
            TextFilter pinFilter = TextFilter.get(TextFilter.PIN_ADDRESS);
            return pinFilter.validate(AbstractStringWrapper.createInstance(pin));
        }
    }

    public static void dispatchCallback(final ScriptableFunction function, final Object[] args) {
        Dispatcher.getInstance().dispatch(new DispatchableEvent(null) {
            protected void dispatch() {
                try {
                    function.invoke(null, args);
                } catch(Exception e) {
                    // do nothing
                }
            }
        });
    }

    public static void setupEventLogger() {
        EventLogger.register(Util.EVENT_LOGGER_GUID, "BBMPlatformExtension", EventLogger.VIEWER_STRING);
    }
    
    public static void logWarning(String message) {
        Util.log(message, EventLogger.WARNING);
    }
    
    public static void logError(String message) {
        Util.log(message, EventLogger.SEVERE_ERROR);
    }
    
    public static void log(String message, int level) {
        EventLogger.logEvent(Util.EVENT_LOGGER_GUID, message.getBytes(), level);
    }
    
    public static void alert(final String message) {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                Dialog.alert(message);
            }
        });
    }
    
    public static class ScriptableUsersArray extends Scriptable {
        private final BBMPlatformUser[] _users;
        private final Integer _length;
        
        public ScriptableUsersArray(BBMPlatformUser[] users) {
            _users = users;
            _length = new Integer(_users.length);
        }
        
        public Object getField(String name) throws Exception {
            if(name.equals("length")) {
                return _length;
            } else {
                return super.getField(name);
            }
        }
        
        public Object getElement(int index) {
            return _users[index];
        }
        
        public int getElementCount() {
            return _users.length;
        }
    }
}

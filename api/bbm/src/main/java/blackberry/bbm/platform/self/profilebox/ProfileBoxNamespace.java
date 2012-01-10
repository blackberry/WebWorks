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
package blackberry.bbm.platform.self.profilebox;

import java.nio.ByteBuffer;

import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBox;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBoxAccessException;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBoxItem;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PNGEncodedImage;
import blackberry.bbm.platform.util.Util;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class ProfileBoxNamespace extends Scriptable {
    public static final String NAME = "profilebox";
    
    public static final String FIELD_ACCESSIBLE =   "accessible";
    
    // Items
    public static final String FIELD_ITEMS =        "items";
    public static final String FUNC_ADD_ITEM =      "addItem";
    public static final String FUNC_REMOVE_ITEM =   "removeItem";
    public static final String FUNC_CLEAR_ITEMS =   "clearItems";
    
    
    private static ProfileBoxNamespace instance;
    
    private UserProfile _userProfile;
    private UserProfileBox _profileBox;
    
    public static ProfileBoxNamespace getInstance(UserProfile userProfile) {
        if(instance == null) {
            instance = new ProfileBoxNamespace();
        }
        instance.setUserProfile(userProfile);
        return instance;
    }
    
    private void setUserProfile(UserProfile userProfile) {
        _userProfile = userProfile;
    }
    
    public Object getField(String name) throws Exception {
        // Initialize profile box
        if(_profileBox == null) {
            try {
                _profileBox = _userProfile.getProfileBox();
            } catch(UserProfileBoxAccessException e) {
                if(name.equals(FIELD_ACCESSIBLE)) {
                    return Boolean.FALSE;
                } else if(name.equals(FIELD_ITEMS)) {
                    return UNDEFINED;
                } else {
                    return UNDEFINED;
                }
            }
        }
        
        if(name.equals(FIELD_ACCESSIBLE)) {
            return new Boolean(_profileBox.isAccessible());
        } else if(name.equals(FIELD_ITEMS)) {
            return this.getProfileBoxItems();
        } else if(name.equals(FUNC_ADD_ITEM)) {
            return new AddItemFunction();
        } else if(name.equals(FUNC_REMOVE_ITEM)) {
            return new RemoveItemFunction();
        } else if(name.equals(FUNC_CLEAR_ITEMS)) {
            return new ClearItemsFunction();
        } else {
            return super.getField(name);
        }
    }
    
    private ScriptableProfileBoxItem[] getProfileBoxItems() {
        UserProfileBoxItem[] items = _profileBox.getItems();
        ScriptableProfileBoxItem[] scriptItems = new ScriptableProfileBoxItem[items.length];
        for(int i = 0; i < items.length; i++) {
            scriptItems[i] = new ScriptableProfileBoxItem(_profileBox, items[i]);
        }
        return scriptItems;
    }
    
    private class AddItemFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Scriptable options = (Scriptable) args[0];
            
            final String text = (String) options.getField("text");
            
            // cookie is optional
            final String cookie;
            final Object cookieObj = options.getField("cookie");
            if(cookieObj.equals(UNDEFINED)) {
                cookie = null;
            } else {
                cookie = (String) cookieObj;
            }
            
            // icon is optional
            final int iconID;
            final Object iconURI = options.getField("icon");
            if(iconURI.equals(UNDEFINED)) {
                iconID = -1;
            } else {
                final byte[] iconBytes = Util.requestBitmapBytes((String) iconURI);
                final Bitmap icon = Bitmap.createBitmapFromBytes(iconBytes, 0, iconBytes.length, 1);
                iconID = Math.abs(ByteBuffer.wrap(iconBytes).hashCode()); // icon ID is hash code of bitmap
                if(! _profileBox.isIconRegistered(iconID)) {
                    _profileBox.registerIcon(iconID, PNGEncodedImage.encode(icon));
                }
            }
            
            _profileBox.addItem(iconID, text, cookie);
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Scriptable.class, true);
            return new FunctionSignature[] {
                    sig1
            };
        }
    }    
    
    private class RemoveItemFunction extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            final ScriptableProfileBoxItem scriptItem = (ScriptableProfileBoxItem) args[0];
            final UserProfileBoxItem item = scriptItem.getItem();
            _profileBox.removeItem(item.getItemId());
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(ScriptableProfileBoxItem.class, true);
            return new FunctionSignature[] {
                    sig1
            };
        }
    }
    
    private class ClearItemsFunction extends ScriptableFunctionBase {

        protected Object execute(Object thiz, Object[] args) throws Exception {
            _profileBox.removeAllItems();
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                    new FunctionSignature(0)
            };
        }
    }
}

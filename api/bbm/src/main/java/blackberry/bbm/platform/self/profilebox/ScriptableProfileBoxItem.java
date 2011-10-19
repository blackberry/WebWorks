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

import blackberry.bbm.platform.util.Util;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBox;
import net.rim.blackberry.api.bbm.platform.profile.UserProfileBoxItem;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.system.EncodedImage;

public class ScriptableProfileBoxItem extends Scriptable {

    private static final String FIELD_ID =     "id";
    private static final String FIELD_TEXT =   "text";
    private static final String FIELD_ICON =   "icon";
    private static final String FIELD_COOKIE = "cookie";
    
    private final UserProfileBox _box;
    private final UserProfileBoxItem _item;
    
    public ScriptableProfileBoxItem(UserProfileBox box, UserProfileBoxItem item) {
        _box = box;
        _item = item;
    }
    
    public Object getField(String name) throws Exception {
        if(name.equals(FIELD_ID)) {
            return new Integer(_item.getItemId());
        } else if(name.equals(FIELD_TEXT)) {
            return _item.getText();
        } else if(name.equals(FIELD_ICON)) {
            return this.getIcon();
        } else if(name.equals(FIELD_COOKIE)) {
            final String cookie = _item.getCookie();
            if(cookie != null) {
                return cookie;
            } else {
                return UNDEFINED;
            }
        } else {
            return super.getField(name);
        }
    }
    
    public Object getIcon() {
        final int iconId = _item.getIconId();
        if(iconId == -1) {
            return UNDEFINED;
        } else {
            final EncodedImage icon = _box.getIcon(_item.getIconId());
            if(icon == null) {
                return UNDEFINED;
            } else {
                return Util.bitmapToBase64Str(icon.getBitmap());
            }
        }
    }
    
    public UserProfileBoxItem getItem() {
        return _item;
    }
}

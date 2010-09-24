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
package blackberry.web.widget.caching;

import java.util.Date;
import java.util.Hashtable;
import net.rim.device.api.script.*;

public class ScriptableCacheItem extends Scriptable {
    public static final String LABEL_URL = "url";
    public static final String LABEL_SIZE = "size";
    public static final String LABEL_EXPIRES = "expires";
    
    private String  _url;
    private int     _size;
    private long    _expires;
    
    public ScriptableCacheItem(String url, int size, long expires) {
        _url = url;
        _size = size;
        _expires = expires;
    }

    /* @Override */ public Scriptable getParent() {
        return null;
    }

    /* @Override */ public Object getField( String name ) throws Exception {
        if (name.equals(LABEL_URL)) {
            return _url;
        }

        if (name.equals(LABEL_SIZE)) {
            return new Integer(_size);
        }
        
        if (name.equals(LABEL_EXPIRES)) {
            return new Date(_expires);
        }

        return UNDEFINED;
    }

    /* @Override */ public boolean putField( String name, Object value ) throws Exception {
        return false;
    }
}

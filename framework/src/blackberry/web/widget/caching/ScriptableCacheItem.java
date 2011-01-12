/*
 * ScriptableCacheItem.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2008-2008
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

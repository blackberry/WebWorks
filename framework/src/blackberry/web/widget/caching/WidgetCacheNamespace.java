/*
 * WidgetCacheNamespace.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2008-2008
 */

package blackberry.web.widget.caching;

import java.util.Hashtable;
import net.rim.device.api.script.*;
//import net.rim.device.api.web.jse.base.ScriptField;
//import net.rim.device.api.web.jse.base.ScriptableObjectBase;
//import net.rim.device.api.web.jse.base.ScriptableFunctionBase;
//import net.rim.device.api.web.jse.base.FunctionSignature;

import net.rim.device.api.browser.field2.BrowserField;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.caching.CacheManager;

public class WidgetCacheNamespace extends Scriptable {

    public static final String NAME = "blackberry.widgetcache";

    public static final String LABEL_GET_CURRENT_SIZE = "getCurrentSize";
    public static final String LABEL_GET_CACHES_INFORMATION = "getCacheInformation";
    public static final String LABEL_CLEAR_ALL = "clearAll";
    public static final String LABEL_CLEAR_CACHE = "clearCache";
    public static final String LABEL_HAS_CACHE = "hasCache";
    
    private BrowserFieldScreen _widgetScreen;

    private GetCurrentSize _funcGetCurrentSize;
    private GetCachesInformation _funcGetCachesInformation;
    private ClearAll _funcClearAll;
    private ClearCache _funcClearCache;
    private HasCache _funcHasCache;
    
    public WidgetCacheNamespace(BrowserFieldScreen widgetScreen) {
        _widgetScreen = widgetScreen;
        
        _funcGetCurrentSize = new GetCurrentSize();
        _funcGetCachesInformation = new GetCachesInformation();
        _funcClearAll = new ClearAll();
        _funcClearCache= new ClearCache();
        _funcHasCache = new HasCache();
    }

    /* @Override */ public Scriptable getParent() {
        return null;
    }

    /* @Override */ public Object getField( String name ) throws Exception {
        if (name.equals(LABEL_GET_CURRENT_SIZE)) {
            return _funcGetCurrentSize;
        }

        if (name.equals(LABEL_GET_CACHES_INFORMATION)) {
            return _funcGetCachesInformation;
        }

        if (name.equals(LABEL_CLEAR_ALL)) {
            return _funcClearAll;
        }

        if (name.equals(LABEL_CLEAR_CACHE)) {
            return _funcClearCache;
        }

        if (name.equals(LABEL_HAS_CACHE)) {
            return _funcHasCache;
        }

        return UNDEFINED;
    }

    /* @Override */ public boolean putField( String name, Object value ) throws Exception {
        return false;
    }

    private class GetCurrentSize extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
            	CacheManager mgr = _widgetScreen.getCacheManager();
            	if(mgr!=null) {
            		int size = mgr.getTotalCacheSize();
            		return new Integer(size);
            	} else {
            		return null;
            	}
            }
            
            throw new IllegalArgumentException();
        }
    }

    private class GetCachesInformation extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
            	CacheManager mgr = _widgetScreen.getCacheManager();
            	if(mgr!=null) {
            		return mgr.getScriptableCacheItems();
            	} else {
            		return null;
            	}
            }
            
            throw new IllegalArgumentException();
        }
    }

    private class ClearAll extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args == null || args.length == 0) {
            	CacheManager mgr = _widgetScreen.getCacheManager();
            	if(mgr!=null) {
            		mgr.clearAll();
            		return UNDEFINED;
            	} else {
            		return null;
            	}
            }
            
            throw new IllegalArgumentException();
        }
    }
    
    private class ClearCache extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args != null && args.length == 1 && args[0] != null) {
            	CacheManager mgr = _widgetScreen.getCacheManager();
            	if(mgr!=null) {
	                mgr.clearCache(args[0].toString());
	                return UNDEFINED;
            	} else {
            		return null;
            	}
            }
            
            throw new IllegalArgumentException();
        }
    }
    
    private class HasCache extends ScriptableFunction {
        /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
            if (args != null && args.length == 1 && args[0] != null) {
            	CacheManager mgr = _widgetScreen.getCacheManager();
            	if(mgr!=null) {
            		return new Boolean(mgr.hasCache(args[0].toString()));
            	} else {
            		return null;
            	}
            }
            
            throw new IllegalArgumentException();
        }
    }
}

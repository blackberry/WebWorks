/*
 * ScriptableConstructorBase.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.jse.base;

/**
 * Our base class of Constructor object
 */
public abstract class ScriptableConstructorBase extends ScriptableFunctionBase {    
    /* @Override */ public Object construct( Object thiz, Object[] args ) throws Exception {        
        validateArgs(args);
        return createNewObj(args);
    }
    
    protected abstract Object createNewObj( Object[] args );
    
    /* @Override */ public Object invoke( Object thiz, Object[] args ) throws Exception {
        // Constructor - not supported
        return UNDEFINED;
    }
        
    /**
     * @see blackberry.web.widget.jse.base.ScriptableFunctionBase
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        // Constructor - not supported
        return UNDEFINED;
    }
}

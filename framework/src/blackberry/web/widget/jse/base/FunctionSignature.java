/*
 * FunctionSignature.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.jse.base;

import java.util.Vector;

/**
 * The class that represents the argument signatures of a scriptable function
 */
public class FunctionSignature {
    private int _numParams;
    private Vector _params;
    
    public FunctionSignature(int numParams) {
        _numParams = numParams;
        _params = new Vector(_numParams);
    }
    
    public void addParam(Class type, boolean isRequired) {
        _params.addElement(new FunctionParam(type, isRequired, false));
    }
    
    public void addNullableParam(Class type, boolean isRequired) {
        _params.addElement(new FunctionParam(type, isRequired, true));
    }
    
    public Vector getParams() {
        return _params;
    }

    public static class FunctionParam {
        Class _type;
        boolean _isRequired;
        boolean _acceptNull;
                
        FunctionParam(Class type, boolean isRequired, boolean acceptNull) {
            _type = type;
            _isRequired = isRequired;
            _acceptNull = acceptNull;            
        }
        
        public boolean isRequired() { return _isRequired; }      
        public Class getType() { return _type; }
        public boolean isNullable() { return _acceptNull; }
    }
}

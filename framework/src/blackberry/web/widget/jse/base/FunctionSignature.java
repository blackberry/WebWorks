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

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

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
package blackberry.pim.category;

import javax.microedition.pim.PIM;

import net.rim.blackberry.api.pdap.BlackBerryPIM;
import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * This class implements the add category functionality
 * 
 * @author dmateescu
 * 
 */
public final class AddCategoryFunction extends ScriptableFunctionBase {

    public static final String NAME = "addCategory";

    /**
     * Add the specified category to the PIM database
     * 
     * @param thiz
     *            context where this function was called.
     * @param args
     *            arguments passed into the function
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        // It does not matter which type of PIM list is being opened here
        // They share the same categories defined in the PIM database
        BlackBerryPIMList pimList = (BlackBerryPIMList) BlackBerryPIM.getInstance().openPIMList( PIM.TODO_LIST, PIM.READ_WRITE );
        pimList.addCategory( args[ 0 ].toString() );
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

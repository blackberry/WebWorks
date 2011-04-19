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
package blackberry.io.dir;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - creates a new directory from a specified path.
 */
public final class CreateNewDirFunction extends ScriptableFunctionBase {

    public static final String NAME = "createNewDir";

    /**
     * Create a new directory in a given path.
     * 
     * @param args
     *            args[0]: a complete URL path of the new directory
     * 
     *            The new directory name specified in the path must be followed by a slash "/".
     * 
     *            Ex: To create a new dir "foo" in "file:///SDCard/BlackBerry/documents/" :
     *            createNewDir("file:///SDCard/BlackBerry/documents/foo/");
     * 
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        FileConnectionWrapper fileConnWrap = null;

        try {
            fileConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
            fileConnWrap.getFileConnection().mkdir();
        } finally {
            if( fileConnWrap != null ) {
                fileConnWrap.close();
            }
        }
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

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

import java.util.Enumeration;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - lists the files in the specified path
 */
public final class ListFilesFunction extends ScriptableFunctionBase {

    public static final String NAME = "listFiles";

    /**
     * JavaScript extension function; determine the name of the files that reside in a given directory.
     * 
     * @param args
     *            args[0]: the complete URL path of the directory.
     * @return a list of file names
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {
        String[] list = null;
        FileConnectionWrapper fConnWrap = null;
        try {
            fConnWrap = new FileConnectionWrapper( args[ 0 ].toString() );
            Enumeration fileEnum = fConnWrap.list();

            int count = 0;
            String fileName;

            while( fileEnum.hasMoreElements() ) {
                fileName = (String) fileEnum.nextElement();
                if( fileName.charAt( fileName.length() - 1 ) != '/' ) { // Is a file
                    count++;
                }
            }

            fileEnum = fConnWrap.list();
            list = new String[ count ];

            int i = 0;
            while( fileEnum.hasMoreElements() ) {
                fileName = (String) fileEnum.nextElement();
                if( fileName.charAt( fileName.length() - 1 ) != '/' ) {// Is a file
                    list[ i++ ] = fileName;
                }
            }

        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }
        return list;
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

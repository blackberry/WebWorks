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

import javax.microedition.io.file.FileSystemRegistry;

import blackberry.core.ScriptableFunctionBase;

/**
 * JavaScript function class - retrieves the root directories available
 */
public final class GetRootDirsFunction extends ScriptableFunctionBase {

    public static final String NAME = "getRootDirs";

    /**
     * JavaScript extension function; determine the path of the root directories.
     * 
     * @return a list of complete URL paths to the root directories.
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {

        String[] dirList = null;

        Enumeration rootEnum = FileSystemRegistry.listRoots();
        int count = 0;

        while( rootEnum.hasMoreElements() ) { // get the number of root dirs
            String rootDir = (String) rootEnum.nextElement();
            if( rootDir != null && !rootDir.equals( "system/" ) ) {
                count++;
            }
        }

        if( count == 0 ) {
            return UNDEFINED;
        }

        dirList = new String[ count ];
        rootEnum = FileSystemRegistry.listRoots();

        for( int i = 0; i < count; ) {
            // MKS360365 - don't return "file:///system" - nobody can use it anyway
            String rootDir = (String) rootEnum.nextElement();
            if( rootDir != null && !rootDir.equals( "system/" ) ) {
                dirList[ i ] = "file:///" + rootDir;
                i++;
            }
        }

        return dirList;
    }
}

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
package blackberry.system;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.homescreen.HomeScreen;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implements blackberry.system.setHomeScreenBackground function.
 */
public final class SetHomeScreenBackgroundFunction extends ScriptableFunctionBase {

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[] )
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        // validate file path
        String filepath = (String) args[ 0 ];
        FileConnection fileConnection = null;
        try {
            Connection con = Connector.open( filepath );
            if( con != null && con instanceof FileConnection ) {
                fileConnection = (FileConnection) con;
                if( !fileConnection.exists() || fileConnection.isDirectory() ) {
                    throw new Exception( "Invalid file URI" );
                }
            }
        } finally {
            if( fileConnection != null ) {
                fileConnection.close();
            }
        }
        // set home screen background
        HomeScreen.setBackgroundImage( filepath );
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

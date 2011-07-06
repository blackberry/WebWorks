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
package blackberry.io.file;

import javax.microedition.content.ContentHandler;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.rim.device.api.io.MIMETypeAssociations;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.io.FileConnectionWrapper;

/**
 * JavaScript function class - returns a handler to the specified file
 */
public final class OpenFunction extends ScriptableFunctionBase {

    public static final String NAME = "open";

    public static final String CLASSNAME = "net.rim.device.api.web.jse.blackberry.io.file.OpenFunction";

    /**
     * Open the specified file with the registered content handler
     * 
     * @param args
     *            args[0]: the complete file path to the file
     * @return Return true if succeeds, false otherwise (note: if the path is an existing directory (not a file), the method will
     *         still return false)
     */

    public Object execute( Object thiz, Object[] args ) throws Exception {
        // whether the full path file exists and not a directory
        boolean exist = false;
        String fullPathUrl = args[ 0 ].toString();
        FileConnectionWrapper fConnWrap = null;

        try {
            fConnWrap = new FileConnectionWrapper( fullPathUrl );
            if( fConnWrap.exists() && !fConnWrap.isDirectory() ) {
                exist = true;
            }
        } catch( Exception e ) {
        } finally {
            if( fConnWrap != null ) {
                fConnWrap.close();
            }
        }

        if( exist ) {
            if( invokeContentHandler( fullPathUrl ) ) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }

    private boolean invokeContentHandler( String filename ) {
        String type = MIMETypeAssociations.getMIMEType( filename );

        try {
            Registry registry = Registry.getRegistry( CLASSNAME );

            // create an invocation and invoke the content handler
            Invocation invocation = new Invocation( filename, type );
            invocation.setResponseRequired( false );
            invocation.setAction( ContentHandler.ACTION_OPEN );

            ContentHandler[] handlers = registry.findHandler( invocation );
            if( handlers.length == 0 ) {
                return false;
            }
            registry.invoke( invocation );
            return true;
        } catch( Exception e ) {
        }
        return false;
    }
}

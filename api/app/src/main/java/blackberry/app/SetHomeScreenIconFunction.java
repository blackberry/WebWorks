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
package blackberry.app;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.microedition.io.InputConnection;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldController;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.util.ByteVector;

/**
 * Implements blackberry.app.setHomeScreenIcon function
 */
public final class SetHomeScreenIconFunction extends ScriptableFunctionBase {

    public static final String NAME = "setHomeScreenIcon";
    private WeakReference _weakReferenceBrwoserFieldController;

    /**
     * Constructor
     * 
     * @param browserField
     *            The {@link BrowserField}
     */
    public SetHomeScreenIconFunction( BrowserField browserField ) {
        _weakReferenceBrwoserFieldController = new WeakReference((BrowserFieldController) browserField.getConfig().getProperty( BrowserFieldConfig.CONTROLLER ));
    }

    /**
     * @see ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        DataInputStream dis = null;
        InputConnection ic = null;

        try {
            BrowserFieldRequest bfr = new BrowserFieldRequest( args[ 0 ].toString() );
            ic = ((BrowserFieldController)_weakReferenceBrwoserFieldController.get()).handleResourceRequest( bfr );

            dis = ic.openDataInputStream();
            ByteVector vc = new ByteVector();

            for( int b = dis.read(); b != -1; b = dis.read() ) {
                vc.addElement( (byte) b );
            }

            // An application could have multiple entry points and each entry point has its own icon.
            // We need to update icons for all entry points to be the same one. 
            Bitmap image = Bitmap.createBitmapFromBytes( vc.getArray(), 0, vc.size(), 1 );
            ApplicationDescriptor current = ApplicationDescriptor.currentApplicationDescriptor();
            int moduleHandle = current.getModuleHandle();
            ApplicationDescriptor[] descriptors = CodeModuleManager.getApplicationDescriptors( moduleHandle );
            
            if( args.length == 1 || !( (Boolean) args[ 1 ] ).booleanValue() ) {
                for( int i = 0; i < descriptors.length; i++ ) {
                    HomeScreen.updateIcon( image, descriptors[ i ] );
                }
            } else {
                for( int i = 0; i < descriptors.length; i++ ) {
                    HomeScreen.setRolloverIcon( image, descriptors[ i ] );
                }
            }
        } finally {
            try {
                if( dis != null ) {
                    dis.close();
                }
                if( ic != null ) {
                    ic.close();
                }
            } catch( IOException e ) {
            }
        }

        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

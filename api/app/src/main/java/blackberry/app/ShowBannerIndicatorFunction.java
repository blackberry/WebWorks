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

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Function to show banner indicator.
 */
public final class ShowBannerIndicatorFunction extends ScriptableFunctionBase {

    public static final String NAME = "showBannerIndicator";

    /**
     * @see ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) {
        ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();

        EncodedImage image = EncodedImage.getEncodedImageResource( (String) args[ 0 ] );
        if( image == null ) {
            throw new IllegalArgumentException( "Icon was not found." );
        }
        try {
            ApplicationIcon icon = new ApplicationIcon( image );

            reg.unregister();
            ApplicationIndicator indicator;
            if( args.length > 1 ) {
                // Set icon and value
                indicator = reg.register( icon, false, false );
                indicator.set( icon, ( (Integer) args[ 1 ] ).intValue() );
            } else {
                // Set icon only
                indicator = reg.register( icon, true, false );
                indicator.setIcon( icon );
            }
            indicator.setVisible( true );

        } catch( IllegalArgumentException e ) {
            // According to the Java API, IllegalArgumentException will only
            // occur when the icon is too big.
            throw new IllegalArgumentException( "Icon is too big." );
        }
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 2 );
        fs.addParam( String.class, true );
        fs.addParam( Integer.class, false );
        return new FunctionSignature[] { fs };
    }
}

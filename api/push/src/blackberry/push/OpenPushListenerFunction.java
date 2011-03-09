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
package blackberry.push;

import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.transport.TransportObject;
import blackberry.push.common.PushService;

/**
 * Class to implement the script function openPushListener()
 */
class OpenPushListenerFunction extends ScriptableFunctionBase {

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        // parse max queue capacity if available
        int maxQueueCap = -1;
        if( args.length > 3 ) {
            maxQueueCap = ( (Integer) args[ 3 ] ).intValue();
        }

        // open the channel
        PushService.getInstance().openPushChannel( ( (Integer) args[ 1 ] ).intValue(), (ScriptableFunction) args[ 0 ],
                maxQueueCap );
        return UNDEFINED;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#validateArgs(Object[])
     */
    protected void validateArgs( Object[] args ) {
        super.validateArgs( args );

        int port = ( (Integer) args[ 1 ] ).intValue();
        if( port < 0 ) {
            throw new IllegalArgumentException( "Invalid port." );
        }

    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 4 );
        fs.addParam( ScriptableFunction.class, true );
        fs.addParam( Integer.class, true );
        fs.addParam( TransportObject.class, false );
        fs.addParam( Integer.class, false );
        return new FunctionSignature[] { fs };
    }
}
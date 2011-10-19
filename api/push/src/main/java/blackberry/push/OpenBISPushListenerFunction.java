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

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Class to implement the script function OpenBISPushListenerFunction()
 */
public class OpenBISPushListenerFunction extends ScriptableFunctionBase {

    private static final String KEY_PORT = "port";
    private static final String KEY_APP_ID = "appId";
    private static final String KEY_SERVER_URL = "serverUrl";
    private static final String KEY_WAKEUP_PAGE = "wakeUpPage";
    private static final String KEY_MAX_QUEUE_CAP = "maxQueueCap";

    /**
     * @see ScriptableFunctionBase#execute(Object, Object[])
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {

        Scriptable obj = (Scriptable) args[ 0 ];
        int port = ( (Integer) obj.getField( KEY_PORT ) ).intValue();
        String appId = obj.getField( KEY_APP_ID ).toString();
        String serverUrl = obj.getField( KEY_SERVER_URL ).toString();
        String wakeUpPage = obj.getField( KEY_WAKEUP_PAGE ).toString();
        int maxQueueCap = 0;
        Object maxQueueCapObj = obj.getField( KEY_MAX_QUEUE_CAP );
        if( maxQueueCapObj != UNDEFINED ) {
            maxQueueCap = ( (Integer) maxQueueCapObj ).intValue();
        }
        ScriptableFunction onData = (ScriptableFunction) args[ 1 ];
        ScriptableFunction onRegister = (ScriptableFunction) args[ 2 ];
        ScriptableFunction onSimChange = (ScriptableFunction) args[ 3 ];
        PushService.getInstance().openBISPushChannel( port, appId, serverUrl, wakeUpPage, maxQueueCap, onData, onRegister,
                onSimChange );
        return UNDEFINED;
    }

    /**
     * @see ScriptableFunctionBase#validateArgs(Object[])
     */
    protected void validateArgs( Object[] args ) {
        super.validateArgs( args );

        Scriptable obj = (Scriptable) args[ 0 ];
        try {
            Object port = obj.getField( KEY_PORT );
            if( port != null && port != UNDEFINED ) {
                int portValue = ( (Integer) port ).intValue();
                if( portValue < 0 ) {
                    throw new IllegalArgumentException( "Invalid port." );
                } else if( !PushService.isValidPort( portValue ) ) {
                    throw new IllegalArgumentException( "Reserved port" );
                }
            } else {
                throw new IllegalArgumentException( "Port is missing." );
            }
            Object appId = obj.getField( KEY_APP_ID );
            if( appId == null || appId == UNDEFINED ) {
                throw new IllegalArgumentException( "AppId is missing." );
            }
            Object serverUrl = obj.getField( KEY_SERVER_URL );
            if( serverUrl == null || serverUrl == UNDEFINED ) {
                throw new IllegalArgumentException( "serverUrl is missing." );
            }
            Object wakeUpPage = obj.getField( KEY_WAKEUP_PAGE );
            if( wakeUpPage == null || wakeUpPage == UNDEFINED ) {
                throw new IllegalArgumentException( "AppId is missing." );
            }
        } catch( Exception e ) {
            throw new IllegalArgumentException( "Error retrieving arguments: " + e.getMessage() );
        }
    }

    /**
     * @see ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 4 );
        fs.addParam( Object.class, true );
        fs.addParam( ScriptableFunction.class, true );
        fs.addParam( ScriptableFunction.class, true );
        fs.addParam( ScriptableFunction.class, true );
        return new FunctionSignature[] { fs };
    }

}

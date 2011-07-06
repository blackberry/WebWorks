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
package blackberry.push.data;

import blackberry.common.push.PushData;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.WidgetBlob;
import net.rim.device.api.script.Scriptable;

/**
 * Class implements blackberry.push.data in BlackBerry Widget API. This object encapsulates the push message received from a
 * previously opened push channel.
 */
public class PushDataObject extends Scriptable {

    /** Push message accepted */
    public static final int ACCEPT = 0;
    /** Push message discarded due to resource shortage */
    public static final int DECLINE_USERDCR = 1;
    /** Content type of Push message cannot be processed */
    public static final int DECLINE_USERDCU = 2;
    /** Push message cannot be delivered to intended destination */
    public static final int DECLINE_USERPND = 3;
    /** User refused Push message */
    public static final int DECLINE_USERREQ = 4;
    /** User refused Push message */
    public static final int DECLINE_USERRFS = 5;

    private static final String DECLINE_USERDCR_VALUE = "DECLINE_USERDCR";
    private static final String DECLINE_USERDCU_VALUE = "DECLINE_USERDCU";
    private static final String DECLINE_USERPND_VALUE = "DECLINE_USERPND";
    private static final String DECLINE_USERREQ_VALUE = "DECLINE_USERREQ";
    private static final String DECLINE_USERRFS_VALUE = "DECLINE_USERRFS";
    private static final String ACCEPT_VALUE = "ACCEPT";

    /*
     * Properties
     */
    private static final String PROPERTY_IS_CHANNEL_ENCRYPED = "isChannelEnrypted";
    private static final String PROPERTY_PAYLOAD = "payload";

    /*
     * Functions
     */
    private static final String FUNCTION_GET_HEADER_FIELD = "getHeaderField";
    private static final String FUNCTION_GET_REQUEST_URI = "getRequestURI";
    private static final String FUNCTION_GET_SOURCE = "getSource";

    private PushData _pushData;

    /**
     * Constructs a Push Data Object.
     */
    public PushDataObject( PushData pushData ) {
        _pushData = pushData;
    }

    /**
     * @see Scriptable#getField(String)
     */
    public Object getField( String name ) throws Exception {
        if( name.equals( ACCEPT_VALUE ) ) {
            return new Integer( ACCEPT );
        } else if( name.equals( DECLINE_USERDCR_VALUE ) ) {
            return new Integer( DECLINE_USERDCR );
        } else if( name.equals( DECLINE_USERDCU_VALUE ) ) {
            return new Integer( DECLINE_USERDCU );
        } else if( name.equals( DECLINE_USERPND_VALUE ) ) {
            return new Integer( DECLINE_USERPND );
        } else if( name.equals( DECLINE_USERREQ_VALUE ) ) {
            return new Integer( DECLINE_USERREQ );
        } else if( name.equals( DECLINE_USERRFS_VALUE ) ) {
            return new Integer( DECLINE_USERRFS );
        } else if( name.equals( PROPERTY_IS_CHANNEL_ENCRYPED ) ) {
            return new Boolean( _pushData.isChannelEncrypted() );
        } else if( name.equals( PROPERTY_PAYLOAD ) ) {
            return parsePayload( _pushData.getPayload() );
        } else if( name.equals( FUNCTION_GET_HEADER_FIELD ) ) {
            return new GetHeaderFieldFunction();
        } else if( name.equals( FUNCTION_GET_REQUEST_URI ) ) {
            return new GetRequestURIFunction();
        } else if( name.equals( FUNCTION_GET_SOURCE ) ) {
            return new GetSourceFunction();
        }

        return super.getField( name );
    }

    /**
     * Parses the byte array payload of this push message.
     * 
     * @param payload
     *            The contents of the push message.
     * @return A Blob object - Scriptable
     */
    private Object parsePayload( byte[] payload ) {
        return new WidgetBlob( payload );
    }

    /**
     * Class to implement the script function getHeaderField()
     */
    private class GetHeaderFieldFunction extends ScriptableFunctionBase {
        /**
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        protected Object execute( Object thiz, Object[] args ) throws Exception {
            if( args[ 0 ] instanceof Integer ) {
                return _pushData.getHeaderField( ( (Integer) args[ 0 ] ).intValue() );
            } else if( args[ 0 ] instanceof String ) {
                return _pushData.getHeaderField( (String) args[ 0 ] );
            }
            return UNDEFINED;
        }

        /**
         * @see ScriptableFunctionBase#getFunctionSignatures()
         */
        protected FunctionSignature[] getFunctionSignatures() {
            FunctionSignature fs1 = new FunctionSignature( 1 );
            fs1.addParam( Integer.class, true );
            FunctionSignature fs2 = new FunctionSignature( 1 );
            fs2.addParam( String.class, true );
            return new FunctionSignature[] { fs1, fs2 };
        }
    }

    /**
     * Class to implement the script function getRequestURI()
     */
    private class GetRequestURIFunction extends ScriptableFunctionBase {
        /**
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        protected Object execute( Object thiz, Object[] args ) throws Exception {
            return _pushData.getRequestURI();
        }
    }

    /**
     * Class to implement the script function accept()
     */
    private class GetSourceFunction extends ScriptableFunctionBase {
        /**
         * @see ScriptableFunctionBase#execute(Object, Object[])
         */
        protected Object execute( Object thiz, Object[] args ) throws Exception {
            return _pushData.getSource();
        }
    }
}

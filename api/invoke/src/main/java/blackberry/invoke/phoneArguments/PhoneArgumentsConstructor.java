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
package blackberry.invoke.phoneArguments;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * The PhoneArgumentsConstructor class is used to create new PhoneArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class PhoneArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "PhoneArguments";

    public static final int VIEW_CALL = 0;
    public static final int VIEW_VOICEMAIL = 1;
    public static final String LABEL_VIEW_CALL = "VIEW_CALL";
    public static final String LABEL_VIEW_VOICEMAIL = "VIEW_VOICEMAIL";

    public static final int NO_LINEID = -1;

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        String dialString = null;
        boolean smartDialing = false;
        int lineId = NO_LINEID;

        if( args.length > 0 ) {
            dialString = (String) args[ 0 ];
        }

        if( args.length > 1 ) {
            final Boolean b = (Boolean) args[ 1 ];
            smartDialing = b.booleanValue();
        }

        if( args.length > 2 ) {
            final Integer i = (Integer) args[ 2 ];
            lineId = i.intValue();
        }
        return new PhoneArgumentsObject( dialString, smartDialing, lineId );
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_CALL ) ) {
            return new Integer( VIEW_CALL );
        } else if( name.equals( LABEL_VIEW_VOICEMAIL ) ) {
            return new Integer( VIEW_VOICEMAIL );
        }

        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 3 );
        fs.addParam( String.class, false );
        fs.addParam( Boolean.class, false );
        fs.addParam( Integer.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

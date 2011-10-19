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
package blackberry.media.microphone;

import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of function microphone.record(). Start recording an audio clip to the given path
 */
public class RecordFunction extends ScriptableFunctionBase {
    public static final String NAME = "record";

    private Record _record;

    /**
     * Default constructor
     */
    public RecordFunction( Record record ) {
        _record = record;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        String path = (String) args[ 0 ];
        ScriptableFunction completeCallback = null;
        ScriptableFunction onErrorCallback = null;

        if( args.length >= 2 ) {
            completeCallback = (ScriptableFunction) args[ 1 ];
        }

        if( args.length >= 3 ) {
            onErrorCallback = (ScriptableFunction) args[ 2 ];
        }

        _record.record( path, completeCallback, onErrorCallback );

        return UNDEFINED;

    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 3 );
        fs.addParam( String.class, true );
        fs.addParam( ScriptableFunction.class, false );
        fs.addParam( ScriptableFunction.class, false );
        return new FunctionSignature[] { fs };
    }
}
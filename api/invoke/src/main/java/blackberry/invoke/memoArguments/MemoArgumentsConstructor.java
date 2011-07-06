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
package blackberry.invoke.memoArguments;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.pim.memo.MemoObject;

/**
 * The MemoArgumentsConstructor class is used to create new MemoArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class MemoArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "MemoArguments";

    public static final int VIEW_NEW = 0;
    public static final int VIEW_EDIT = 1;
    public static final String LABEL_VIEW_NEW = "VIEW_NEW";
    public static final String LABEL_VIEW_EDIT = "VIEW_EDIT";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        validateArgs( args );
        if( args == null || args.length == 0 ) {
            return new MemoArgumentsObject();
        } else {
            final MemoObject m = (MemoObject) args[ 0 ];
            return new MemoArgumentsObject( m );
        }
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {
        if( name.equals( LABEL_VIEW_NEW ) ) {
            return new Integer( VIEW_NEW );
        } else if( name.equals( LABEL_VIEW_EDIT ) ) {
            return new Integer( VIEW_EDIT );
        }

        return super.getField( name );
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( MemoObject.class, false );
        return new FunctionSignature[] { fs };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

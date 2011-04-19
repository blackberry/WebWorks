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
package blackberry.pim.memo;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import net.rim.blackberry.api.pdap.BlackBerryMemo;
import net.rim.blackberry.api.pdap.BlackBerryMemoList;
import net.rim.blackberry.api.pdap.BlackBerryPIM;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.find.FindNamespace;
import blackberry.find.TestableScriptableObject;

/**
 * This class represents the implementation of the find function of a Memo
 * 
 * @author dmateescu
 */
public class MemoFindScriptableFunction extends ScriptableFunctionBase {
    public static final String NAME = "find";

    /**
     * The default constructor
     */
    public MemoFindScriptableFunction() {
        super();
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    public Object execute( Object thiz, Object[] args ) throws Exception {
        MemoObject[] memosFound = new MemoObject[ 0 ];

        TestableScriptableObject testable = null;
        String orderByField = "";
        int maxReturn = -1;
        boolean isAscending = true;

        if( !FindNamespace.isValidFindArguments( args, false ) ) {
            return memosFound;
        }

        if( args.length > 0 ) {
            testable = (TestableScriptableObject) args[ 0 ];
        }

        if( args.length > 1 ) {
            if( args[ 1 ] != null ) {
                orderByField = (String) args[ 1 ];
            }
        }

        if( args.length > 2 ) {
            if( args[ 2 ] != null ) {
                Integer i = (Integer) args[ 2 ];
                maxReturn = i.intValue();
            }
        }

        if( args.length > 3 ) {
            if( args[ 3 ] != null ) {
                Boolean b = (Boolean) args[ 3 ];
                isAscending = b.booleanValue();
            }
        }

        boolean isSorted = orderByField != null && orderByField.length() > 0 ? true : false;

        BlackBerryMemoList memoList;
        try {
            memoList = (BlackBerryMemoList) PIM.getInstance().openPIMList( BlackBerryPIM.MEMO_LIST, PIM.READ_WRITE );
        } catch( PIMException pime ) {
            return memosFound;
        }

        Vector found = new Vector();
        Enumeration e;
        int iElement = 0;
        try {
            e = memoList.items();
            while( e.hasMoreElements() ) {
                BlackBerryMemo m = (BlackBerryMemo) e.nextElement();
                MemoObject memo = new MemoObject( m );
                if( testable != null ) {
                    if( testable.test( memo ) ) {
                        FindNamespace.insertElementByOrder( found, memo, orderByField, isAscending );
                        iElement++;
                    }
                } else {
                    FindNamespace.insertElementByOrder( found, memo, orderByField, isAscending );
                    iElement++;
                }

                if( !isSorted && iElement == maxReturn ) {
                    break;
                }
            }
        } catch( PIMException pime ) {
            return memosFound;
        }

        int size = found.size();
        if( maxReturn > 0 && size > maxReturn ) {
            size = maxReturn;
        }
        memosFound = new MemoObject[ size ];
        for( int i = 0; i < size; i++ ) {
            MemoObject memo = (MemoObject) found.elementAt( i );
            memosFound[ i ] = memo;
        }

        return memosFound;
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 4 );
        fs.addNullableParam( TestableScriptableObject.class, false );
        fs.addNullableParam( String.class, false );
        fs.addNullableParam( Integer.class, false );
        fs.addNullableParam( Boolean.class, false );
        return new FunctionSignature[] { fs };
    }
}

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
package blackberry.invoke.mapsArguments;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;

import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import blackberry.pim.address.AddressObject;

/**
 * The MapsArgumentsConstructor class is used to create new MapsArgumentsObject object.
 * 
 * @author sgolod
 * 
 */
public class MapsArgumentsConstructor extends ScriptableFunctionBase {
    public static final String NAME = "MapsArguments";

    /**
     * @see net.rim.device.api.script.ScriptableFunction#construct(java.lang.Object, java.lang.Object[])
     */
    public Object construct( final Object thiz, final Object[] args ) throws Exception {
        if( args != null && args.length > 0 ) {
            if( args.length == 2 ) {
                return new MapsArgumentsObject( Double.parseDouble( args[ 0 ].toString() ), Double.parseDouble( args[ 1 ]
                        .toString() ) );
            } else if( args[ 0 ] instanceof AddressObject ) {
                return new MapsArgumentsObject( (AddressObject) args[ 0 ] );
            } else if( args[ 0 ] instanceof String ) {
                return new MapsArgumentsObject( (String) args[ 0 ] );
            } else if( args[ 0 ] instanceof Document ) {
                Document doc = (Document) args[ 0 ];
                // This works on 5.0 device only
                try {
                    String st = ( (DOMImplementationLS) doc.getImplementation() ).createLSSerializer().writeToString( doc );
                    return new MapsArgumentsObject( st );
                } catch( Exception e ) {
                    throw new IllegalArgumentException( "Document objects are only supported in 5.0." );
                }
            }
        }
        return new MapsArgumentsObject();
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#validateArgs(java.lang.Object[])
     */
    protected void validateArgs( final Object[] args ) {
        super.validateArgs( args );

        // additional validation
        // blackberry.invoke.MapsArguments ( latitude : Number , longitude : Number ) - Number is
        // returned as either an Integer or Double
        if( args != null && args.length == 2 ) {
            if( !( args[ 0 ] instanceof Integer || args[ 0 ] instanceof Double ) ) {
                throw new IllegalArgumentException( "Invalid type - " + args[ 0 ].getClass().toString() );
            }
            if( !( args[ 1 ] instanceof Integer || args[ 1 ] instanceof Double ) ) {
                throw new IllegalArgumentException( "Invalid type - " + args[ 1 ].getClass().toString() );
            }
        }
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#getFunctionSignatures()
     */
    protected FunctionSignature[] getFunctionSignatures() {
        final FunctionSignature fs1 = new FunctionSignature( 2 );
        fs1.addParam( Object.class, true );
        fs1.addParam( Object.class, true );
        final FunctionSignature fs2 = new FunctionSignature( 1 );
        fs2.addParam( String.class, true );
        final FunctionSignature fs3 = new FunctionSignature( 1 );
        fs3.addParam( AddressObject.class, true );
        return new FunctionSignature[] { new FunctionSignature( 0 ), fs1, fs2, fs3 };
    }

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    protected Object execute( final Object thiz, final Object[] args ) throws Exception {
        return UNDEFINED;
    }

}

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
package blackberry.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

/**
 * Helper class for loading JavaScript files into ScriptEngine
 *
 */
public class JSUtilities {
    private static String getValidPath( String jsPath ) {
        String SLASH_FWD = "/";

        if( !jsPath.startsWith( SLASH_FWD ) ) {
            return SLASH_FWD + jsPath;
        }

        return jsPath;
    }

    private static String readJSContent( String jsURI ) {
        String jsContent = "";
        InputStream is = null;
        try {
            is = Class.class.getResourceAsStream( jsURI );
            byte[] data = IOUtilities.streamToBytes( is );
            jsContent = new String( data );
        } catch( Exception e ) {
        } finally {
            try {
                if( is != null ) {
                    is.close();
                    is = null;
                }
            } catch( IOException e ) {
            }
        }
        return jsContent;
    }    
    
    /**
     * @return String comparator
     */
    public static Comparator getStringComparator() {
        Comparator comp = new Comparator() {
            public int compare( Object arg0, Object arg1 ) {
                if( arg0 instanceof String && arg1 instanceof String ) {
                    return ( (String) arg0 ).compareTo( (String) arg1 );
                }

                return 0;
            }
        };
        
        return comp;
    }     
    
    /**
     * Load a specified set of JS files into the script engine
     * 
     * @param scriptEngine
     * @param jsPaths
     * @param jsInjectionPaths
     */
    public static void loadJS( ScriptEngine scriptEngine, String[] jsPaths, SimpleSortingVector jsInjectionPaths ) {
        if( jsInjectionPaths != null && scriptEngine != null && jsPaths != null ) {
            jsInjectionPaths.setSortComparator( getStringComparator() );
            jsInjectionPaths.reSort();
            Enumeration jsPathsElems = jsInjectionPaths.elements();

            while( jsPathsElems.hasMoreElements() ) {
                String jsPath = (String) jsPathsElems.nextElement();

                for( int i = 0; i < jsPaths.length; i++ ) {
                    if( jsPath.endsWith( jsPaths[ i ] ) ) {
                        Object compiledScript = scriptEngine.compileScript( readJSContent( getValidPath( jsPath ) ) );
                        scriptEngine.executeCompiledScript( compiledScript, null );
                    }
                }
            }
        }
    }    
}

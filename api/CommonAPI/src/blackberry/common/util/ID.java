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

import java.lang.IllegalStateException;

/**
 * 
 */
public final class ID {
    
    private static long _packageNameHash = 0;
    
    
    private ID() {
        
    }
        
    // Init ID generator
    public static void init(Object seed) {
        // package name is auto-generated to be unique
        if(seed instanceof String) {
            _packageNameHash = seed.hashCode();
        } else {
            _packageNameHash = seed.getClass().getName().hashCode();
        }
    }

    public static long getUniqueID(String identifier) {
        if(_packageNameHash == 0) {
            throw new IllegalStateException( ID.class.getName() + " is not initialized" );
        }
        return (_packageNameHash & 0x00000000FFFFFFFFL) | ( (long) identifier.hashCode() << 32 );
    }
    
    public static long getUniqueID() {
        if(_packageNameHash == 0) {
            throw new IllegalStateException( ID.class.getName() + " is not initialized" );
        }
        return _packageNameHash;
    }
}

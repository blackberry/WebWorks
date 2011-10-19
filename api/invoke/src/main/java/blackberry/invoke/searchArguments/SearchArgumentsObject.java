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
package blackberry.invoke.searchArguments;

import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents the SearchArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class SearchArgumentsObject extends ScriptableObjectBase {

    private final String _text;
    private final String _name;

    /**
     * Default constructor, constructs a new SearchArgumentsObject object.
     */
    public SearchArgumentsObject() {
        _text = null;
        _name = null;
    }

    /**
     * Constructs a new SearchArgumentsObject object.
     * 
     * @param text
     *            Specifies the text for search.
     * @param name
     *            Specifies the name for search.
     */
    public SearchArgumentsObject( final String text, final String name ) {
        _text = text;
        _name = name;
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the SearchArgumentsObject's underlying content.
     * 
     * @return text to search.
     */
    public String getText() {
        return _text;
    }

    /**
     * Internal helper method to get direct access to the SearchArgumentsObject's underlying content.
     * 
     * @return name to search.
     */
    public String getName() {
        return _name;
    }
}

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

import net.rim.device.api.script.ScriptEngine;

/**
 * A simple utility class for pattern matching and testing.
 */
public class PatternMatchingUtilities {
    /**
     * Matches a given string with the regular expression.
     * <p>
     * For example: PatternMatchingUtilities.match(scriptEngine, "Watch out for the rock!", "r?or?", "|") will return "o|or|ro".
     * 
     * @param scriptEngine
     *            The current script engine
     * @param input
     *            The string to be matched
     * @param regex
     *            the regular expression pattern for matching
     * @param separator
     *            A delimiter separating the found matches
     * @return A String of all the matches separated by the provided separator.
     */
    public static String findMatches( ScriptEngine scriptEngine, String input, String regex, String separator ) {
        final String script = "(function getMatch(){" + "var url = \"" + input + "\"; var regex=new RegExp(\"" + regex
                + "\", \"g\");var matches = url.match(regex);return matches.join('" + separator + "');})();";
        return (String) scriptEngine.executeScript( script, null );
    }

    /**
     * Tests if the given string matches the regular expression.
     * 
     * @param scriptEngine
     *            The current script engine
     * @param input
     *            The string to be tested
     * @param regex
     *            The regular expression pattern for testing
     * @return Returns true if the given regular expression is found in the input String.
     */
    public static boolean isMatch( ScriptEngine scriptEngine, String input, String regex ) {
        final String script = "(function patternMatches() { var pattern = new RegExp(\"" + regex + "\");" + "var input = \""
                + input + "\"; return pattern.test(input);})();";
        return ( (Boolean) scriptEngine.executeScript( script, null ) ).booleanValue();
    }
}

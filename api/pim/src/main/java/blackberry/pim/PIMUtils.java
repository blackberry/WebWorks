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
package blackberry.pim;

import javax.microedition.pim.FieldEmptyException;
import javax.microedition.pim.RepeatRule;

/**
 * Utility class containing useful methods of the PIM APIs
 * 
 * @author dmateescu
 * 
 */
public class PIMUtils {
    /**
     * Helper function to return an integer field of a RepeatRule, gracefully handing the situation where the field has no value.
     * 
     * @param repeat
     *            the RepeatRule whose integer field to return.
     * @param field
     *            the field whose value to return.
     * @return the value of the specified integer field of the specified RepeatRule, or 0 (zero) if the field has no value.
     */
    public static int getRepeatRuleInt( RepeatRule repeat, int field ) {
        try {
            return repeat.getInt( field );
        } catch( FieldEmptyException fee ) {
            return 0;
        }
    }

    /**
     * Helper function to return a date field of a RepeatRule, gracefully handing the situation where the field has no value.
     * 
     * @param repeat
     *            the RepeatRule whose date field to return.
     * @param field
     *            the field whose value to return.
     * @return the value of the specified date field of the specified RepeatRule, or 0 (zero) if the field has no value.
     */
    public static long getRepeatRuleDate( RepeatRule repeat, int field ) {
        try {
            return repeat.getDate( field );
        } catch( FieldEmptyException fee ) {
            return 0;
        }
    }

}

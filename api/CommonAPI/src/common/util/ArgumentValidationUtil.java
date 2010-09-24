/*
* Copyright 2010 Research In Motion Limited.
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
package common.util;

public class ArgumentValidationUtil {

    /**
     * Validate the number of arguments.
     * <p>
     * Throws an exception when arguments number is not valid.
     * 
     * @param validParamNumber
     *            The array contains all valid values of arguments number.
     * @param args
     *            The array of arguments.
     * @exception IllegalArgumentException
     *                Thrown if arguments number doesn't match any of allowed numbers.
     */
    public static void validateParameterNumber(final int[] validParamNumber, final Object[] args) throws IllegalArgumentException {
        boolean nullArrayIsAllowed = false;

        for (int i = 0; i < validParamNumber.length; i++) {
            if (validParamNumber[i] == 0) {
                nullArrayIsAllowed = true;
                break;
            }
        }

        if (args != null) {
            for (int i = 0; i < validParamNumber.length; i++) {
                if (args.length == validParamNumber[i]) {
                    return;
                }
            }
        }
        else if (args == null && nullArrayIsAllowed) {
            return;
        }

        throw new IllegalArgumentException("Provided illegal number of parameters.");
    }

    public static void checkNull(Object param) {
    	if (param == null) {
    		throw new NullPointerException();
    	}
    }
}

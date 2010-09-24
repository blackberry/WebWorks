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
package common.base;

import net.rim.device.api.util.CharacterUtilities;
import common.constants.DataTypes;
import common.util.StringUtilities;

public class SignatureParser {

	private final String _sig;

	public SignatureParser(String signature) {
		
		if(StringUtilities.isNullOrEmpty(signature)) {
			throw new IllegalArgumentException("Signature cannot be null or empty.");
		}
		
		_sig = signature;
	}

	public ArgumentCollection parseArguments() {
		/*
		 * Syntax of definition:
		 * function_name(requiredArgType, [optionalArgType], ...) : returnValueType
		 */
		
		//The arguments are found between the '(' and ')' characters
		String argList = _sig.substring(_sig.indexOf('(') + 1, _sig.indexOf(')'));
		
		//Divide arguments by the ',' delimiter
		String[] args = StringUtilities.split(argList, ",");
		
		return createArgumentVector(args);
	}
	
	private ArgumentCollection createArgumentVector(String[] fromStringArray) {
		ArgumentCollection args = new ArgumentCollection();
		
		for(int i = 0; i < fromStringArray.length; i++) {
			if(!StringUtilities.isNullOrEmpty(fromStringArray[i])) {
				Argument arg = createArgFromString(fromStringArray[i]);
				args.addArg(arg);
			}
		}
		
		return args;
	}

	private Argument createArgFromString(String fromStringRepresentation) {
		/*
		 * String representation can be one of four possibilities from the function signature:
		 * 1) requiredArgType - required argument
		 * 2) [optionalArgType] - optional argument
		 * 3) requiredArgType[] - required array argument
		 * 4) [optionalArgType[]] - optional array argument
		 */
		
		//Trim whitespace around the string
		String argString = fromStringRepresentation.trim();
		
		if(StringUtilities.isNullOrEmpty(argString)) {
			throw new IllegalArgumentException("Argument string cannot be null or empty.");
		}
		
		boolean isRequired = isNotSurroundedByBrackets(fromStringRepresentation);
		boolean isArray = hasArrayBrackets(fromStringRepresentation);
		
		//Strip out the brackets if they exist and determine the type
		String type = determineType(stripBrackets(argString));
		
		return new Argument(type, isRequired, isArray);
	}

	private String stripBrackets(String argString) {
		//Start at position 0 unless there's a open bracket there
		int beginIndex = argString.startsWith("[") ? 1 : 0;
		//End index is the last letter of the string
		int endIndex = argString.length() - 1;
		while(endIndex > 0 && !CharacterUtilities.isLetter(argString.charAt(endIndex))) {
			endIndex--;
		}
		
		return argString.substring(beginIndex, endIndex + 1);
	}

	private String determineType(String argType) {
		//Return the constant that matches the defined arg type
		if(DataTypes.TYPE_STRING.equals(argType)) {
			return DataTypes.TYPE_STRING;
		} else if(DataTypes.TYPE_NUMBER.equals(argType)) {
			return DataTypes.TYPE_NUMBER;
		} else if(DataTypes.TYPE_BOOLEAN.equals(argType)) {
			return DataTypes.TYPE_BOOLEAN;
		} else if(DataTypes.TYPE_OBJECT.equals(argType)) {
			return DataTypes.TYPE_OBJECT;
		}
		
		return DataTypes.TYPE_INVALID;
	}

	private boolean isNotSurroundedByBrackets(String argString) {
		return !(argString.startsWith("[") && argString.endsWith("]"));
	}

	private boolean hasArrayBrackets(String argString) {
		return argString.indexOf("[]") >= 0;
	}

}

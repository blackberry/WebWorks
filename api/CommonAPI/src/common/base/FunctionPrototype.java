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

public class FunctionPrototype {

	private ArgumentCollection _args;

	FunctionPrototype(String signature) {
		_args = parseArguments(signature);
	}

	int getRequiredNumberOfArguments() {
		return countRequiredArgs();
	}

	ArgumentCollection getArgumentTemplates() {
		ArgumentCollection copy = new ArgumentCollection();
		
		for(int i = 0; i < _args.size(); i++) {
			copy.addArg(new Argument(_args.getAtIndex(i)));
		}
		
		return copy;
	}

	ArgumentCollection parseArguments(String signature) {
		SignatureParser sp = new SignatureParser(signature);
		
		return sp.parseArguments();
	}
	
	private int countRequiredArgs() {
		int reqCount = 0;
		
		for(int i = 0; i < _args.size(); i++) {
			if(_args.getAtIndex(i).isRequired) {
				reqCount++;
			}
		}
		
		return reqCount;
	}

}

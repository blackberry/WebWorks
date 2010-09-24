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

import java.util.Vector;

public class ArgumentCollection {
	
	private Vector _args;
	
	public ArgumentCollection() {
		_args = new Vector();
	}
	
	public void addArg(Argument arg) {
		if(arg == null) {
			throw new IllegalArgumentException("Cannot add null argument.");
		}
		_args.addElement(arg);
	}
	
	public Argument getAtIndex(int index) {
		return (Argument)_args.elementAt(index);
	}

	public int size() {
		return _args.size();
	}

}

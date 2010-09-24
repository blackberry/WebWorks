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

import net.rim.device.api.script.ScriptableFunction;

public abstract class ScriptableFunctionBase extends ScriptableFunction {

	private static FunctionPrototype _functionPrototype;

	public ScriptableFunctionBase() {
		if(_functionPrototype == null) {
			_functionPrototype = new FunctionPrototype(getSignature());
		}
	}
	
	public Object invoke(Object thiz, Object[] args) throws Exception {
		checkCorrectNumberOfArgs(args.length);
		
		FunctionInstance call = new FunctionInstance(_functionPrototype);
		call.setArgumentValues(args);
		
		return methodInvoked(call);
	}

	private void checkCorrectNumberOfArgs(int providedArgumentsLength) {
		if(providedArgumentsLength < _functionPrototype.getRequiredNumberOfArguments()) {
			throw new InsufficientArgumentsException("Not enough arguments provided.");
		}
	}
	
	protected abstract String getSignature();
	
	protected abstract Object methodInvoked(FunctionInstance call);
	
	private class InsufficientArgumentsException extends RuntimeException {

		private String _message;

		public InsufficientArgumentsException(String message) {
			_message = message;
		}
		
		public String getMessage() {
			return _message;
		}

	}
}

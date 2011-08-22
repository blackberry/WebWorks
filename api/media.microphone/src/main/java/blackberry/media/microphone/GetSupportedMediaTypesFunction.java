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
package blackberry.media.microphone;

import net.rim.device.api.script.ScriptableFunction;

/**
 * Implementation of function microphone.getSupportedMediaTypes(). Get media types that can be recorded.
 */
public class GetSupportedMediaTypesFunction extends ScriptableFunction {
	public static final String NAME = "getSupportedMediaTypes";
	private Record _record;

	public GetSupportedMediaTypesFunction(Record record) {
		_record = record;
	}

	/**
	 * @see blackberry.core.ScriptableFunctionBase#execute(Object, Object[])
	 */
	public Object invoke(Object thiz, Object[] args) throws Exception {
		return _record.getSupportedMediaTypes();
	}

}
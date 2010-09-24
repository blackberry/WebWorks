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

import net.rim.device.api.script.Scriptable;
import common.constants.DataTypes;

public class Argument {

	public final String type;
	private Object _value;
	public final boolean isRequired;
	public final boolean isArray;

	public Argument(String dataType, boolean required, boolean array) {
		type = dataType;
		isRequired = required;
		isArray = array;

		//Initial value null
		_value = null;
	}

	/*
	 * Copy constructor
	 */
	public Argument(Argument fromArgument) {
		type = fromArgument.type;
		isRequired = fromArgument.isRequired;
		isArray = fromArgument.isArray;
		_value = fromArgument.getAsObject();
	}

	public void setValue(Object theValue) {
		_value = theValue;
	}

	public boolean getAsBoolean() {
		if(isArray) {
			throw new ClassCastException("Cannot convert array argument to Boolean.");
		}

		if(_value == null || _value.equals(Scriptable.UNDEFINED)) {
			return false;
		} 
		
		if(type == DataTypes.TYPE_STRING) {
			if("".equals(_value)) {
				return false;
			}	
		} else if(type == DataTypes.TYPE_NUMBER) {
			double val = ((Double)_value).doubleValue();

			if(Double.isNaN(val) || val == 0) {
				return false;
			}
		} else if(type == DataTypes.TYPE_BOOLEAN) {
			return ((Boolean)_value).booleanValue();
		} 

		return true;
	}

	public String getAsString() {
		if(isArray) {
			throw new ClassCastException("Cannot convert array argument to String.");
		}

		if(_value == null) {
			return DataTypes.TYPE_NULL;
		} else if (_value.equals(Scriptable.UNDEFINED)) {
			return DataTypes.TYPE_UNDEFINED;
		} else {
			return _value.toString();
		}
	}

	public double getAsDouble() {
		if(isArray) {
			throw new ClassCastException("Cannot convert array argument to Double.");
		}
		
		if(_value == null) {
			return 0;
		}

		if(_value.equals(Scriptable.UNDEFINED)) {
			return Double.NaN;
		}

		if(type == DataTypes.TYPE_STRING) {
			double d;
			try {
				d = Double.parseDouble((String)_value);
			} catch (NumberFormatException nfe) {
				d = Double.NaN;
			}

			return d;
		} else if(type == DataTypes.TYPE_NUMBER) {
			return ((Double)_value).doubleValue();
		} else if(type == DataTypes.TYPE_BOOLEAN) {
			return ((Boolean)_value).booleanValue() ? 1 : 0;
		} 		

		return Double.NaN;
	}

	public Object getAsObject() {
		return _value;
	}

}

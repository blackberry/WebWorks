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
package net.rim.tumbler.config;

import java.util.Arrays;

public class WidgetFeature {
    private String _id;
    private boolean _isRequired;
    private String _version;
    private WidgetFeature[] _dependentFeatures;

    public WidgetFeature(String id, boolean isRequired, String version,
            WidgetFeature[] dependentFeatures) {
        _id = id;
        _isRequired = isRequired;
        _version = version;
        _dependentFeatures = dependentFeatures;
    }

    public String getID() {
        return _id;
    }

    public boolean isRequired() {
        return _isRequired;
    }

    public String getVersion() {
        return _version;
    }

    public WidgetFeature[] getDependentFeatures() {
        return _dependentFeatures;
    }

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Feature{id: ");
		buf.append(_id);
		buf.append(", required: ");
		buf.append(_isRequired);
		buf.append(", version: ");
		buf.append(_version);
		buf.append(", dependentFeatures: ");
		buf.append(Arrays.toString(_dependentFeatures));
		buf.append("}");
		return buf.toString();
	}        
}

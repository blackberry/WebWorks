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

import java.util.Hashtable;

public class FeaturesHash {

    private static Hashtable _featuresHashed = new Hashtable();

    public static Object getObjectForLoadedFeature(final String feature) {
        return _featuresHashed.get(feature);
    }

    public static Object setObjectForLoadedFeature(final String feature, final Object obj) {
        return _featuresHashed.put(feature, obj);
    }

    // In 6.0 the feature name which is a superset of other feature (i.e. blackberry.audio.Player is superset of blackberry.audio)
    // isn't handled in the same manner as in 5.0 (instead it's looking in the blackberry.audio namespace after the Player name).
    public static String formatFeature(final String namespace, final String name) {
        return namespace + "." + name;
    }
}

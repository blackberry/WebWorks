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
package blackberry.core;

/**
 * Store various widget properties so that it can be accessed from both framework and extensions.
 */
public class WidgetProperties {

    private static WidgetProperties _instance;

    private long _guid;

    public static WidgetProperties getInstance() {
        if( _instance == null ) {
            _instance = new WidgetProperties();
        }
        return _instance;
    }

    private WidgetProperties() {
        // do nothing
    }

    public void setGuid( long guid ) {
        _guid = guid;
    }

    public long getGuid() {
        return _guid;
    }
}

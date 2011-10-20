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
package blackberry.system.event;

/**
 * interface <code>ISystemEventExtensionConstants</code> contains constants
 * which are need by SystemEventExtension
 * @author rtse
 */
public interface ISystemEventExtensionConstants {
    // Feature name
    public final static String FEATURE_ID = "blackberry.system.event";

    public static final String REQ_FUNCTION_ON_HARDWARE_KEY = "onHardwareKey";
    public static final String REQ_FUNCTION_ON_COVERAGE_CHANGE = "onCoverageChange";

    public static final String ARG_MONITOR = "monitor";
    public static final String ARG_KEY = "key";

    public static final String KEY_BACK = "KEY_BACK";
    public static final String KEY_MENU = "KEY_MENU";
    public static final String KEY_CONVENIENCE_1 = "KEY_CONVENIENCE_1";
    public static final String KEY_CONVENIENCE_2 = "KEY_CONVENIENCE_2";
    public static final String KEY_STARTCALL = "KEY_STARTCALL";
    public static final String KEY_ENDCALL = "KEY_ENDCALL";
    public static final String KEY_VOLUME_UP = "KEY_VOLUMEUP";
    public static final String KEY_VOLUME_DOWN = "KEY_VOLUMEDOWN";

    public static final int IKEY_BACK = 0;
    public static final int IKEY_MENU = 1;
    public static final int IKEY_CONVENIENCE_1 = 2;
    public static final int IKEY_CONVENIENCE_2 = 3;
    public static final int IKEY_STARTCALL = 4;
    public static final int IKEY_ENDCALL = 5;
    public static final int IKEY_VOLUME_DOWN = 6;
    public static final int IKEY_VOLUME_UP = 7;
}

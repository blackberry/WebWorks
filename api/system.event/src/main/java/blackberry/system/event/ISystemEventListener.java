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

 /** Interface implemented by the class that will generically signalled that
 * an event has occurred. Contains several constants for convenience.
 *
 * @author ababut
 */
public interface ISystemEventListener {
    
    static final String EVENT_COV_CHANGE = "onCoverageChange";
    static final String EVENT_HARDWARE_KEY = "onHardwareKey";
    
    void onSystemEvent(String event, String eventArg);
    
}

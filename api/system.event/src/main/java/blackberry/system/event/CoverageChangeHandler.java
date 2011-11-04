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

import blackberry.system.event.ISystemEventListener;

import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.CoverageStatusListener;

/** 
 * Coverage listener implementation. Proxy for system coverage listener that
 * notifies manager generically of coverage change events.
 *
 * @author ababut
 */
public class CoverageChangeHandler {
    private ISystemEventListener _manager;
    
    private CoverageStatusListener _currentCoverageMonitor;
    
    CoverageChangeHandler(ISystemEventListener manager) {
        _manager = manager;
    }
    
    /**
     * Creates a listener if not present and registers it.
     *
     */
    public void listen() {
        if( _currentCoverageMonitor == null ) {
            _currentCoverageMonitor = new CoverageStatusListener() {
                public void coverageStatusChanged( int newCoverage ) {
                    //Notify manager of coverage changed event
                    //TODO: Add new coverage to the event argument if JS cares about it 
                    _manager.onSystemEvent(_manager.EVENT_COV_CHANGE, "");
                }
            };
            
            CoverageInfo.addListener(_currentCoverageMonitor);
        }
    }
    
    /**
     * Removes system listener if present.
     *
     */    
    public void stopListening() {
        if(_currentCoverageMonitor != null) {
            CoverageInfo.removeListener(_currentCoverageMonitor);
            //Explicitly null it out to avoid memory leaks
            _currentCoverageMonitor = null;
        }
    }
    
    /**
     * Indicates listener status
     *
     * @return true if listener is active
     */ 
    public boolean isListening() {
        return (_currentCoverageMonitor != null);
    }
}

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

(function () {
	var OK = 0,
        CHANNEL_CLOSED = 1,
        _polling = false,
        FUNCTION_ON_COVERAGE_CHANGE = "onCoverageChange",
        FUNCTION_ON_HARDWARE_KEY = "onHardwareKey",
        _callbacks = {};
    
	function poll() {
        
		blackberry.transport.poll("blackberry/system/event/poll", {}, function(response) {
			// do not invoke callback unless return code is OK (negative codes for errors, or channel closed from Java side)
            // stop polling if response code is not OK
			if (response.code < OK || response.code === CHANNEL_CLOSED) {
                _polling = false;
				return false;
			}
            
            var event = response.data.event,
                func = (event === FUNCTION_ON_HARDWARE_KEY) ?  _callbacks[event][response.data.arg] : _callbacks[event];
			
			if (typeof(func) !== "undefined") {
				func();
			}

			return !!func;
		});
        
	}
    
    function registerForEvent(evt, args) {
        blackberry.transport.call(
            "blackberry/system/event/register",
            { get : { event : evt, arg : args } },
            function(response) {
                if(response.code < OK) {
                    throw new Error("Unable to register event handler for " + evt + ". Implementation returned: " + response.code);
                }
            }
        );
        
        if(!_polling) {
            _polling = true;
            poll();
        }
    }
    
    function unregisterForEvent(evt, args) {
        blackberry.transport.call(
            "blackberry/system/event/unregister",
            { get : { event : evt, arg : args } },
            function(response) {
                if(response.code < OK) {
                    throw new Error("Unable to unregister event handler for " + evt + ". Implementation returned: " + response.code);
                }
            }
        );
    }
    
    function SystemEventDispatcher() {
        _callbacks[FUNCTION_ON_COVERAGE_CHANGE] = {};
        _callbacks[FUNCTION_ON_HARDWARE_KEY] = {};
	}

	SystemEventDispatcher.prototype.onCoverageChange = function(onSystemEvent) {
        var listen = (typeof(onSystemEvent) === "function"),
            alreadyListening = (typeof(_callbacks[FUNCTION_ON_COVERAGE_CHANGE]) === "function");
            
        if(listen) {
            //Update the callback reference
            _callbacks[FUNCTION_ON_COVERAGE_CHANGE] = onSystemEvent;
            
            //If we are already listening, don't re-register
            if(!alreadyListening) {
                //Start listening for this event
                registerForEvent(FUNCTION_ON_COVERAGE_CHANGE); 
            }
        } else {
            //Update the callback reference
            _callbacks[FUNCTION_ON_COVERAGE_CHANGE] = undefined;
            if(alreadyListening) {
                unregisterForEvent(FUNCTION_ON_COVERAGE_CHANGE);
            }
        }
	};

	SystemEventDispatcher.prototype.onHardwareKey = function(onSystemEvent, key) {
		switch (key) {
			case blackberry.system.event.KEY_BACK:
			case blackberry.system.event.KEY_MENU:
			case blackberry.system.event.KEY_CONVENIENCE_1:
			case blackberry.system.event.KEY_CONVENIENCE_2:
			case blackberry.system.event.KEY_STARTCALL:
			case blackberry.system.event.KEY_ENDCALL:
			case blackberry.system.event.KEY_VOLUMEDOWN:
			case blackberry.system.event.KEY_VOLUMEUP:
				break;
			default:
				throw new Error("key parameter must be one of the pre-defined KEY_* constants");
		}
        
        var listen = (typeof(onSystemEvent) === "function"),
            alreadyListening = (typeof(_callbacks[FUNCTION_ON_HARDWARE_KEY][key]) === "function");
            
        if(listen) {
            //Update the callback reference
            _callbacks[FUNCTION_ON_HARDWARE_KEY][key] = onSystemEvent;
            
            //Only register with the implementation if we're not already listening
            if(!alreadyListening) {
                //Start listening for this event
                registerForEvent(FUNCTION_ON_HARDWARE_KEY, key); 
            }
        } else {
            //Update the callback reference
            _callbacks[FUNCTION_ON_HARDWARE_KEY][key] = undefined;
            if(alreadyListening) {
                unregisterForEvent(FUNCTION_ON_HARDWARE_KEY, key);
            }
        }
	};

	blackberry.Loader.javascriptLoaded("blackberry.system.event", SystemEventDispatcher);
}());

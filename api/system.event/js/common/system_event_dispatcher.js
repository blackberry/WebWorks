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
	var OK = 0;
	var FUNCTION_ON_COVERAGE_CHANGE = "onCoverageChange";
	var FUNCTION_ON_HARDWARE_KEY = "onHardwareKey";

	var _callbacks = {};

	function SystemEventDispatcher() {
	};

	function poll(evt, args, handler) {
		if (evt == FUNCTION_ON_HARDWARE_KEY) {
			var keyCallbacks = _callbacks[evt];
			keyCallbacks = keyCallbacks || {};

			keyCallbacks[args["get"]["key"]] = handler;
			_callbacks[evt] = keyCallbacks;
		} else {
			_callbacks[evt] = handler;
		}

		blackberry.transport.poll("blackberry/system/event/" + evt, args, function(response) {
			if (response.code < OK) {
				// do not invoke callback unless return code is OK
				return false;
			}

			var func;

			if (evt == FUNCTION_ON_HARDWARE_KEY) {
				func = _callbacks[evt][response.data["key"]];
			} else {
				func = _callbacks[evt];
			}

			if (func) {
				func();
			}

			return !!func;
		});
	};

	function initPoll(evt, args, handler) {
		args = args || {};

		args["monitor"] = (handler ? true : false);

		poll(evt, { "get" : args }, handler);
	};

	SystemEventDispatcher.prototype.onCoverageChange = function(onSystemEvent) {
		initPoll(FUNCTION_ON_COVERAGE_CHANGE, {}, onSystemEvent);
	};

	SystemEventDispatcher.prototype.onHardwareKey = function(key, onSystemEvent) {
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

		initPoll(FUNCTION_ON_HARDWARE_KEY, { "key" : key }, onSystemEvent);
	};

	blackberry.Loader.javascriptLoaded("blackberry.system.event", SystemEventDispatcher);
})();

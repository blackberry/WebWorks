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

	function SystemEvent(disp) {
		this.onCoverageChange = function(onSystemEvent) { return disp.onCoverageChange(onSystemEvent); };

		this.onHardwareKey = function(key, onSystemEvent) { return disp.onHardwareKey(onSystemEvent, key); };
	}

	SystemEvent.prototype.__defineGetter__("KEY_BACK", function() { return 0; });
	SystemEvent.prototype.__defineGetter__("KEY_MENU", function() { return 1; });
	SystemEvent.prototype.__defineGetter__("KEY_CONVENIENCE_1", function() { return 2; });
	SystemEvent.prototype.__defineGetter__("KEY_CONVENIENCE_2", function() { return 3; });
	SystemEvent.prototype.__defineGetter__("KEY_STARTCALL", function() { return 4; });
	SystemEvent.prototype.__defineGetter__("KEY_ENDCALL", function() { return 5; });
	SystemEvent.prototype.__defineGetter__("KEY_VOLUMEDOWN", function() { return 6; });
	SystemEvent.prototype.__defineGetter__("KEY_VOLUMEUP", function() { return 7; });

	blackberry.Loader.javascriptLoaded("blackberry.system.event", SystemEvent);
})();

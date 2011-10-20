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
	var SYSTEM_API_URL = "blackberry/system";
	var OK = 0;

	function makeCall(toFunction, functionArgs) {
		var request = new blackberry.transport.RemoteFunctionCall(SYSTEM_API_URL + "/" + toFunction);

		if(functionArgs) {
			for(var name in functionArgs) {
				request.addParam(name, functionArgs[name]);
			}
		}

		return request.makeSyncCall(); //don't care about the return value
	}

	function SystemDispatcher() {
	};

	SystemDispatcher.prototype.__defineGetter__("model", function() {
		var result = makeCall("model");
		if (result.code >= OK) {
			return result.data["model"];
		} else {
			throw new Error(result.msg);
		}
	});

	SystemDispatcher.prototype.__defineGetter__("scriptApiVersion", function() {
		var result = makeCall("scriptApiVersion");
		if (result.code >= OK) {
			return result.data["scriptApiVersion"];
		} else {
			throw new Error(result.msg);
		}
	});

	SystemDispatcher.prototype.__defineGetter__("softwareVersion", function() {
		var result = makeCall("softwareVersion");
		if (result.code >= OK) {
			return result.data["softwareVersion"];
		} else {
			throw new Error(result.msg);
		}
	});

	SystemDispatcher.prototype.hasCapability = function(desiredCapability) {
		var result = makeCall("hasCapability", {capability : desiredCapability});
		if (result.code >= OK) {
			return result.data["hasCapability"];
		} else {
			throw new Error(result.msg);
		}
	};

	SystemDispatcher.prototype.hasDataCoverage = function() {
		var result = makeCall("hasDataCoverage");
		if (result.code >= OK) {
			return result.data["hasDataCoverage"];
		} else {
			throw new Error(result.msg);
		}
	};

	SystemDispatcher.prototype.hasPermission = function(desiredModule) {
		var result = makeCall("hasPermission", {module : desiredModule});
		if (result.code >= OK) {
			return result.data["hasPermission"];
		} else {
			throw new Error(result.msg);
		}
	};

	SystemDispatcher.prototype.isMassStorageActive = function() {
		var result = makeCall("isMassStorageActive");
		if (result.code >= OK) {
			return result.data["isMassStorageActive"];
		} else {
			throw new Error(result.msg);
		}
	};

	SystemDispatcher.prototype.setHomeScreenBackground = function(desiredPicture) {
		var result = makeCall("setHomeScreenBackground", {picture : desiredPicture});

		// no return value

		if (result.code < OK) {
			throw new Error(result.msg);
		}
	};

	blackberry.Loader.javascriptLoaded("blackberry.system", SystemDispatcher);
})();

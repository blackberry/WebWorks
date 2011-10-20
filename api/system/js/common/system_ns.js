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
	
	function System(disp) {
		/*
		 * The function will check the capability String passed through the capability argument. The possible capabilities are:
		 * input.keyboard.issuretype 
		 * input.touch 
		 * media.audio.capture 
		 * media.video.capture 
		 * media.recording
		 * location.gps
		 * location.maps 
		 * storage.memorycard
		 * network.bluetooth
		 * network.wlan (WLAN wireless family includes 802.11, 802.11a, 802.11b, 802.11g)
		 * network.3gpp (3GPP wireless family includes GPRS, EDGE, UMTS, GERAN, UTRAN, and GAN)
		 * network.cdma (CDMA wireless family includes CDMA1x and EVDO) 
		 * network.iden
		 * 
		 * @param {String} capability The capability to check for.
		 * @return {Boolean} Returns true if the capability is supported.
		 */
			
		this.constructor.prototype.hasCapability = function(desiredCapability) { return disp.hasCapability(desiredCapability); }
		
		this.constructor.prototype.hasDataCoverage = function() { return disp.hasDataCoverage(); };
		
		this.constructor.prototype.hasPermission = function(desiredModule) { return disp.hasPermission(desiredModule); };
		
		this.constructor.prototype.isMassStorageActive = function() { return disp.isMassStorageActive(); };
		
		this.constructor.prototype.setHomeScreenBackground = function(desiredPicture) { return disp.setHomeScreenBackground(desiredPicture); };
		
		/*
		 * Getters for public static properties
		 */
		this.constructor.prototype.__defineGetter__("model", function() { return disp.model; });
		this.constructor.prototype.__defineGetter__("scriptApiVersion", function() { return disp.scriptApiVersion; });
		this.constructor.prototype.__defineGetter__("softwareVersion", function() { return disp.softwareVersion; });
		
		//Temporary fix for sync function call - to be removed
		this.dataCoverage = false;
		
		//Temporary fix for sync function call - to be removed
		this.accessList = [];
	};
	
	/*
	 * Define public static constants that will be returned by the hasPermission() method. 
	 */
	System.prototype.__defineGetter__("ALLOW", function() { return 0; });
	System.prototype.__defineGetter__("DENY", function() { return 1; });
	System.prototype.__defineGetter__("PROMPT", function() { return 2; });
	System.prototype.__defineGetter__("NOT_SET", function() { return 3; });
	
	blackberry.Loader.javascriptLoaded("blackberry.system", System);
})();

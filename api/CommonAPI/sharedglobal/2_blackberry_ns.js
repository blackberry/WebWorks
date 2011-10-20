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
	 this.blackberry = {};
	 
	var WebWorksLoader = function() {
		this.loadedJs = {}; //hash of literal namespace - javascript constructor (can be either API or dispatcher, whichever is loaded first)
	}
	
	var isDispatcher = function(func) {
		return (typeof(func) == 'function' && func.name.search(/dispatcher/i) > -1);
	}
	
	/*
		Called by each API/dispatcher when it has finished loading. If its pair has already been loaded, it instantiates the API. Otherwise
		stores the current half of the API and waits for the other one to load before instantiating.
	*/	
	WebWorksLoader.prototype.javascriptLoaded = function(ns, js) {
		//Check if the other half of the API has loaded
		if(typeof this.loadedJs[ns] == 'function') {
			var api, dispatcher;
			//If the function just loaded is the dispatcher, the one we have is the API, and vice-versa
			if(isDispatcher(js)) {
				api = this.loadedJs[ns];
				dispatcher = js;
			} else {
				api = js;
				dispatcher = this.loadedJs[ns];
			}
			
			this.loadApi(ns, api, dispatcher);
			delete this.loadedJs[ns]; //remove namespace entry to prevent double loading
		} else {
			//Otherwise queue up and wait for API to load
			this.loadedJs[ns] = js;
		}
	}
	 
	 /*
		Instantiates the provided API constructor with the provided dispatcher and attaches
		the result to the requested namespace.
	 */
	WebWorksLoader.prototype.loadApi = function(namespace, apiConstructor, dispatcher) {
		//Instantiate the API
		var d = (typeof dispatcher != 'undefined') ? new dispatcher() : {};
		var api = new apiConstructor(d);
	
		//Break namespace into array of parts around the '.'
		var nsParts = namespace.split('.');
		//Must start with 'blackberry'
		if(nsParts[0] != 'blackberry') {
			throw new Error('Namespace does not start with blackberry:' + namespace);
		}
		
		//Iterate over remaining namespace parts and create the empty namespace if it does not exist.
		//If it exists, just ignore it.
		var ns = blackberry;
		for(var i = 1; i < nsParts.length - 1; i++) {
			if(typeof ns[nsParts[i]] == 'undefined') {
				ns[nsParts[i]] = {};
			}
			ns = ns[nsParts[i]];
		}
		
		//Once namespace is built, attach the object to it
		ns[nsParts[nsParts.length - 1]] = api;
	}
	 
	this.blackberry.Loader = new WebWorksLoader();
})();
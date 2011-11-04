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

	var _frameworkReady = false;
	var _deferredRequests = new Array();

	function onFrameworkReady( e ) { 
		_frameworkReady = true;
		
		for(var i = 0; i < _deferredRequests.length; i++) {
			var item = _deferredRequests[i];
			try {
				item.request.send(item.postData);
			} catch(e) {}
		}
		
		_deferredRequests = null;
		window.removeEventListener("frameworkready", onFrameworkReady, false);
	}

	window.addEventListener("frameworkready", onFrameworkReady, false, true);

	if(!this.blackberry) {
		return;
	}
	
	this.blackberry.transport = {
		"RemoteFunctionCall" : function(functionUri) {
			/*
			 * Private members
			 */
			var uri = functionUri;
			var params = {};
			var postParams = {};
			var postData = null;

			var composeUri = function() {
				//SERVER_URL is defined in constants.js and is assumed to have already been attached
				var uri = SERVER_URL + functionUri;
				
				//If we have parameters, let's append them 
				var paramCount = 1;
				for(param in params) {
					if(params.hasOwnProperty(param)) {						
						//If it's not the first parameter, prepend the '&' separator
						if(paramCount == 1) {
							//start the query string with an '?'
							uri += "?";	
						} else {
							uri += "&";
						}
						uri += param + "=" + params[param];
						paramCount++;
					}
				}
								
				return uri;
			};

			var composePostData = function() {
				for (param in postParams) {
					if (postParams.hasOwnProperty(param)) {
						if (!postData) {
							postData = "";
						} else {
							postData += "&";
						}

						postData += param + "=" + postParams[param];
					}
				}
			};
			
			var createXhrRequest = function(uri, isAsync) {
				var request = new XMLHttpRequest();
				
				request.open("POST", uri, isAsync);
				//request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
				request.setRequestHeader("Accept", "*/*");
				request.setRequestHeader("X-Requested-With", "XMLHttpRequest");
				
				return request;
			};
			
			/*
			 * Public members
			 */
			this.addParam = function(name, value) {
				params[name] = encodeURIComponent(value);
			};

			this.addPostParam = function(name, value) {
				postParams[name] = encodeURIComponent(value);
			};
			
			this.makeSyncCall = function(jsonReviver) {
				var requestUri = composeUri();
				var request = createXhrRequest(requestUri, false);
				
				composePostData();

				try {
					request.send(postData);
				} catch(e) {}
				//alert(request.responseText);
				return JSON.parse(request.responseText, jsonReviver); //retrieve result encoded as JSON
			};

			this.makeAsyncCall = function(responseCallback, jsonReviver) {

				// Asynchronous http requests may still count towards the "loading" time of a page
				// To prevent a dead lock with the loading page waiting for a blocking event to finish (example: polling), 
				// defer any async requests.
				var requestUri = composeUri();
				var request = createXhrRequest(requestUri, true);
				composePostData();
	
				request.onreadystatechange = function() {
					// continue if the process is completed
					if (request.readyState == 4 && request.status == 200) {
						// retrieve the response
						var response = JSON.parse(request.responseText, jsonReviver);
						responseCallback(response); //call the client code with the parsed response
					}
				};
	
				if(_frameworkReady) {
					try {
						request.send(postData);
					} catch(e) {}
				} else {
					_deferredRequests.push( { 'request': request, 'postData': postData } );
				}
			};
		}
	};

	this.blackberry.transport.call = function(url, opts, callback) {
		var request = new blackberry.transport.RemoteFunctionCall(url), name;

		opts = opts || {};

		if (opts.get) {
			for (name in opts.get) {
				if (Object.hasOwnProperty.call(opts.get, name)) {
					request.addParam(name, opts.get[name]);
				}
			}
		}

		if (opts.post) {
			for (name in opts.post) {
				if (Object.hasOwnProperty.call(opts.post, name)) {
					request.addPostParam(name, opts.post[name]);
				}
			}
		}

		return opts.async ? request.makeAsyncCall(callback) : request.makeSyncCall(callback);
	};

	this.blackberry.transport.poll = function(url, opts, callback) {
		opts = opts || {};
		opts.async = true;

		blackberry.transport.call(url, opts, function (response) {
			if (callback(response)) {
                setTimeout(
                    function() { this.blackberry.transport.poll(url, opts, callback); }, 
                    0
                    );
            }
		});
	};
})();

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
(function() {
	var PAYMENT_API_URL = "blackberry/payment";
	var OK = 0;

	var FUNCTION_PURCHASE = "purchase";
	var FUNCTION_GETEXISTINGPURCHASES = "getExistingPurchases";
	var PARAMETER_DEVMODE = "developmentMode";

	var _callbackSuccess = {};
	var _callbackError  = {};
	var _developmentMode = false;

	function PaymentDispatcher() {};

	function makeFunctionCall(evt, args, handler1, handler2) {
		if (evt == FUNCTION_PURCHASE || evt == FUNCTION_GETEXISTINGPURCHASES) {
			_callbackSuccess[evt] = handler1;
			_callbackError[evt] = handler2;
		}
		args = args || {};

		blackberry.transport.call(PAYMENT_API_URL + "/" + evt, { "get" : args, "async" : true }, function(response) {
			if (response.code == OK) {
				var callback = _callbackSuccess[evt];
				if (callback) {
				 	var responseString = new String(JSON.stringify(response.data));
				 	callback(responseString);
				}
			 } else if (response.code > OK || response.code == -1) {
				 // error codes
				 var callback = _callbackError[evt];
				 if (callback) {
					 var msg = response.msg;
					 var code = response.code;
					 callback(msg, code);
				 }
			 }
		});
	};

	function makeParamCall(toFunction, functionArgs) {
		var request = new blackberry.transport.RemoteFunctionCall(
				PAYMENT_API_URL + "/" + toFunction);
		
		if (functionArgs) {
			for ( var name in functionArgs) {
				request.addParam(name, functionArgs[name]);
			}
		}
		request.makeSyncCall();
	};
	
	// Checks parameters of purchase(purchaseArgs, function, [function])
	function validatePurchaseArgs(arg1, arg2, arg3) {
		// arg1 and arg2 must be defined and not null
		if (arg1 === undefined || arg2 === undefined) {
			throw new Error("Purchase parameters must be defined.");
		}

		if (arg1 === null || arg2 === null) {
			throw new Error("Purchase Parameters cannot be null.");
		}
		
		// Check type of arg2
		if (!(typeof arg2 === 'function')) {
			throw new Error("Please make sure callbackOnSuccess parameter is a function.");
		}
		
		
		// If arg3 is defined, check if null, check if a function.
		if (arg3 !== undefined) {
			if (arg3 === null) {
				throw new Error("Purchase Parameters cannot be null.");
			}
			
			if (!(typeof arg3 === 'function')) {
				throw new Error("Please make sure callbackOnFailure parameter is a function.");
			}
		}
	}
	
	/*
	 * Checks arguments of getExistingPurchases([boolean], function, [function])
	 * Example: getExistingPurchases(boolean, function)
	 * 			getExistingPurchases(function)
	 *			getExistingPurchases(function, function)
	 * Note: arg1 can be either a boolean or a function
	 */
	function validateGEPArgs(arg1, arg2, arg3) {
		// arg1 must be defined and not null
		if (arg1 === undefined) {
			throw new Error("getExistingPurchases parameters must be defined.");
		}
		
		if (arg1 === null) {
			throw new Error("getExistingPurchases Parameters cannot be null.");
		}
		
		// if arg2 is defined, check if null 
		if (arg2 !== undefined) {
			if (arg2 === null) {
				throw new Error("getExistingPurchases Parameters cannot be null.");
			}
		}
		
		// if arg3 is defined, check if null 
		if (arg3 !== undefined) {
			if (arg3 === null) {
				throw new Error("getExistingPurchases Parameters cannot be null.");
			}
		}
		
		// Check if arg1 is a boolean or a function to determine type for second and third arguments
		if (arg1.constructor === Boolean) {
			// assume getExistingPurchases(boolean, function, [function]);
			if (!(typeof arg2 === 'function')) {
				throw new Error("Please make sure callbackOnSuccess parameter is a function.");
			}
			
			if (arg3 !== undefined) {
				if (!(typeof arg3 === 'function')) {
					throw new Error("Please make sure callbackOnFailure parameter is a function.");
				}
			}
		} else if (typeof arg1 === 'function') {
			// assume getExistingPurchases(function, [function]);
			if (arg2 !== undefined) {
				if (!(typeof arg2 === 'function')) {
					throw new Error("Please make sure callbackOnFailure parameter is a function.");
				}
			}
		} else {
			// arg1 is either a boolean or function
			throw new Error("getExistingPurchases() Invalid parameter type")
		}
	}

	// function purchase
	PaymentDispatcher.prototype.purchase = function(purchaseArgs, callbackOnSuccess, callbackOnFailure) {
		validatePurchaseArgs(purchaseArgs, callbackOnSuccess, callbackOnFailure);
		
		purchaseArgs = purchaseArgs || {};

		var o = [];
		for (var name in purchaseArgs) {
			o[name] = purchaseArgs[name];
		}

		makeFunctionCall(FUNCTION_PURCHASE, {
			"digitalGoodID" : o["digitalGoodID"],
			"digitalGoodSKU" : o["digitalGoodSKU"],
			"digitalGoodName" : o["digitalGoodName"],
			"metaData" : o["metaData"],
			"purchaseAppName" : o["purchaseAppName"],
			"purchaseAppIcon" : o["purchaseAppIcon"]
			}, callbackOnSuccess, callbackOnFailure);
	};

	// function getExistingPurchases
	PaymentDispatcher.prototype.getExistingPurchases = function(refresh, callbackOnSuccess, callbackOnFailure) {
		validateGEPArgs(refresh, callbackOnSuccess, callbackOnFailure);
		
		if (refresh.constructor === Boolean) {
			makeFunctionCall(FUNCTION_GETEXISTINGPURCHASES, { "allowRefresh" : refresh }, callbackOnSuccess, callbackOnFailure);
		} else {
			// refresh is not the first param, replace args
			callbackOnFailure = callbackOnSuccess;
			callbackOnSuccess = refresh;
			makeFunctionCall(FUNCTION_GETEXISTINGPURCHASES, { "allowRefresh" : true }, callbackOnSuccess, callbackOnFailure);
		}
	};

	 // Get & Set development mode
	PaymentDispatcher.prototype.getDevelopmentMode = function(){
		return makeGetDevelopmentModeCall();
	};

	PaymentDispatcher.prototype.setDevelopmentMode = function(mode){
		return makeSetDevelopmentModeCall(mode);
	};
	
	function makeGetDevelopmentModeCall() {
		var recall = makeParamCall("getDevelopmentMode", {});
	}

	function makeSetDevelopmentModeCall(mode) {
		var modeString = (mode) ? "true" : "false";
		var recall = makeParamCall("setDevelopmentMode", { "developmentMode" : modeString } );
	}

	blackberry.Loader.javascriptLoaded("blackberry.payment", PaymentDispatcher);
})();

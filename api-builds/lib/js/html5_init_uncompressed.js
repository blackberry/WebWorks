/**

Date: 07/29/2010
Version: 1.0.0.4

Copyright (c) 2010 Research In Motion Limited.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

This License shall be included in all copies or substantial portions of the Software.

The name(s) of the above copyright holders shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization.

 **/  

/*
 Device 5.0 HTML 5 wrapper on Google Gears.
 */

(function() {

    //Check pre-requisites and attach the various namespaces
    if (!window.google || !google.gears) {
        return; //Nothing else we can do
    }
    
    attachNamespaces();
    
    //-----INITIALIZATION AREA-----
    var timer = google.gears.factory.create("beta.timer");
   
    function attachNamespaces() {
             
        navigator.geolocation = new Geolocation();
    }
  
    //-----END OF INITIALIZATION AREA-----
    
    /*
     * HTML5 API
     *
     * handle = window.setTimeout( handler [, timeout [, arguments ] ] ); handle =
     * window.setTimeout( code [, timeout ] )
     *
     */
    window.setTimeout = function() {
        /*
         * Even though a non-string non-function handler is not part of HTML5
         * API, the browser on 6.0 does not fail in those cases (returns a handler),
         * so here we are just making a dummy call.
         */
        if (!isHandlerValid(arguments[0])) {
            return timer.setTimeout("", 0);
        }
        
        // The first argument is required to be function/code
        var handler = getTimerTask(arguments);
        // The second argument is the timeout value
        var timeout = getTimeout(arguments[1]);
        
        return timer.setTimeout(handler, timeout);
    };
    
    window.setInterval = function() {
        const MIN_INTERVAL = 10;
        /*
         * Even though a non-string non-function handler is not part of HTML5
         * API, the browser on 6.0 does not fail in those cases (returns a handler),
         * so here we are just making a dummy call.
         */
        if (!isHandlerValid(arguments[0])) {
            return timer.setInterval("", 0);
        }
        
        // The first argument is required to be function/code
        var handler = getTimerTask(arguments);
        // The second argument is the timeout value
        var timeout = getTimeout(arguments[1]);
        
        //If timeout is less than MIN_INTERVAL, then increase timeout to MIN_INTERVAL.
        timeout = Math.max(timeout, MIN_INTERVAL);
        
        return timer.setInterval(handler, timeout);
    };
    
    window.clearTimeout = function() {
        /*
         * illegal arguments would throw exception in Gears, but only fail
         * silently in 6.0 browser
         */
        if (util.isPositiveNumber(arguments[0])) {
            timer.clearTimeout(arguments[0]);
        }
    };
    
    window.clearInterval = function() {
        if (util.isPositiveNumber(arguments[0])) {
            return timer.clearInterval(arguments[0]);
        }
    };
    
    /*
     * check whether the handler is valid
     */
    function isHandlerValid(handler) {
        return (util.isString(handler) || util.isFunction(handler));
    }
    
    /*
     * HTML5 Documentation - get timeout
     *
     * 1. Let timeout be the second argument to the method, or zero if the
     * argument was omitted. 2. Apply the ToString() abstract operation to
     * timeout, and let timeout be the result. [ECMA262] 3. Apply the ToNumber()
     * abstract operation to timeout, and let timeout be the result. [ECMA262]
     * 4. If timeout is an Infinity value, a Not-a-Number (NaN) value, or
     * negative, let timeout be zero. 5. Round timeout down to the nearest
     * integer, and let timeout be the result. 6. Return timeout.
     */
    function getTimeout(timeout) {
        if (!isFinite(timeout) || !util.isPositiveNumber(timeout)) {
            timeout = 0;
        }
        
        return timeout;
    }
    
    /*
     * getTimerTask take the whole array of arguments.
     * If the handler is a funciton, it pass any arguments straight through to the handler
     */
    function getTimerTask(arguments) {
        var handler = arguments[0];
        if (util.isFunction(handler) && util.argumentsLength(arguments) > 2) {
            // The user put a function signature as the first argument
            // The third arguments and onwards are parameters for the first
            // function
            var args = new Array();
            for (var i = 0; i < util.argumentsLength(arguments) - 2; i++) {
                args[i] = arguments[i + 2];
            }
            
            handler = handler.apply(this, args);
        }
        return handler;
    }
    
    function Geolocation() {
        var geolocation = google.gears.factory.create("beta.geolocation");
        
        this.getCurrentPosition = function(positionCB, positionErrorCB, positionOptions) {
            try {
                geolocation.getCurrentPosition(wrapSuccessCB(positionCB), wrapErrorCB(positionErrorCB), new PositionOptions(positionOptions));
            } 
            catch (e) {
                throw (errorsMap["geo_watch_getCurrent"]);
            }
        };
        
        this.watchPosition = function(positionCB, positionErrorCB, positionOptions) {
            try {
                return geolocation.watchPosition(wrapSuccessCB(positionCB), wrapErrorCB(positionErrorCB), new PositionOptions(positionOptions));
            } 
            catch (e) {
                throw (errorsMap["geo_watch_getCurrent"]);
            }
        };
        
        function wrapSuccessCB(positionCB) {
            var result;
            
            if (util.isFunction(positionCB)) {
                result = function(position) {
                    positionCB(new Position(createCoordinates(position.coords), position.timestamp));
                };
            }
            
            return result;
        }
        
        function wrapErrorCB(positionErrorCB) {
            // Pass null when there is no usage of the callback
            var result = null;
            
            if (util.isFunction(positionErrorCB)) {
                result = function(error) {
                    positionErrorCB(createPositionError(error));
                };
            }
            
            return result;
        }
        
        this.clearWatch = function(watchId) {
            //Throw the only error required by HTML 5
            if (util.isUndefined(watchId)) {
                throw (errorsMap["geo_clearWatch"]);
            }
            
            /* Only pass the call to Gears if we have a positive integer watchId.
             * Gears throws exceptions for invalid parameter values that HTML 5
             * ignores
             */
            if (util.isPositiveNumber(watchId)) {
                geolocation.clearWatch(watchId);
            }
        };
        
        function Position(coords, timestamp) {
            this.__defineGetter__("coords", function() {
                return coords;
            });
            
            this.__defineGetter__("timestamp", function() {
                return timestamp;
            });
        }
        
        function PositionOptions(args) {
            args = args || {};
            
            this.__defineGetter__("enableHighAccuracy", function() {
                return args.enableHighAccuracy || false;
            });
            
            this.__defineSetter__("enableHighAccuracy", function(value) {
                if (!util.isUndefined(value) && !util.isBoolean(value)) {
                    throw (errorsMap["TMP_ERROR"]);
                }
                
                args.enableHighAccuracy = value;
            });
            
            this.__defineGetter__("timeout", function() {
                return args.timeout;
            });
            
            this.__defineSetter__("timeout", function(value) {
                if (!util.isUndefined(value) && !util.isNumberInRange(value, 0, Infinity)) {
                    throw (errorsMap["TMP_ERROR"]);
                }
                
                args.timeout = value;
            });
            
            this.__defineGetter__("maximumAge", function() {
                return args.maximumAge || 0;
            });
            
            this.__defineSetter__("maximumAge", function(value) {
                if (!util.isUndefined(value) && !util.isNumberInRange(value, 0, Infinity)) {
                    throw (errorsMap["TMP_ERROR"]);
                }
                
                args.maximumAge = value;
            });
        }
        
        //Factory function that checks Gears object to ensure that it contains
        //valid coordinate data and creates an HTML 5 Coordinates object from it
        var createCoordinates = function(fromGearsCoords) {
        
            //Keep the Coordinates object private so we only navigator members can instantiate it
            var Coordinates = function(lat, lon, alt, acc, altAcc) {
                this.__defineGetter__("latitude", function() {
                    return lat;
                });
                
                this.__defineGetter__("longitude", function() {
                    return lon;
                });
                
                this.__defineGetter__("altitude", function() {
                    return util.isPositiveNumber(alt) ? alt : null;
                });
                
                this.__defineGetter__("accuracy", function() {
                    return util.isPositiveNumber(acc) ? acc : Number.MAX_VALUE;
                });
                
                this.__defineGetter__("altitudeAccuracy", function() {
                    return util.isPositiveNumber(altAcc) ? altAcc : null;
                });
                
                this.__defineGetter__("heading", function() {
                    return null; //Not supported by Gears
                });
                
                this.__defineGetter__("speed", function() {
                    return null; //Not supported by Gears
                });
            };
            
            if (util.nullUndefinedArgument(fromGearsCoords.latitude)) {
                throw errorsMap["geo_coordinates_nullUndefinedLatitude"];
            }
            
            if (util.nullUndefinedArgument(fromGearsCoords.longitude)) {
                throw errorsMap["geo_coordinates_nullUndefinedLongitude"];
            }
            
            return new Coordinates(fromGearsCoords.latitude, fromGearsCoords.longitude, fromGearsCoords.altitude, fromGearsCoords.accuracy, fromGearsCoords.altitudeAccuracy);
        };
        
        var createPositionError = function(fromGearsError) {
            var PositionError = function(errorCode, msg) {
                this.__defineGetter__("code", function() {
                    return errorCode;
                });
                
                this.__defineGetter__("message", function() {
                    return msg;
                });
            };
            
            //Getters for the instance error codes used by clients
            PositionError.prototype.__defineGetter__("PERMISSION_DENIED", function() {
                return 1;
            });
            
            PositionError.prototype.__defineGetter__("POSITION_UNAVAILABLE", function() {
                return 2;
            });
            
            PositionError.prototype.__defineGetter__("TIMEOUT", function() {
                return 3;
            });
            
            //Map the Gears error code to the HTML 5 error codes and return the Gears error message
            switch (fromGearsError.code) {
                //Not used by Gears but included for completeness
                case fromGearsError.PERMISSION_DENIED:
                    return new PositionError(PositionError.prototype.PERMISSION_DENIED, fromGearsError.message);
                case fromGearsError.TIMEOUT:
                    return new PositionError(PositionError.prototype.TIMEOUT, fromGearsError.message);
                default:
                    return new PositionError(PositionError.prototype.POSITION_UNAVAILABLE, fromGearsError.message);
            }
        };
         
    }
    var nativeXMLHttpRequest = XMLHttpRequest;
    
    XMLHttpRequest = function() {
        var that = this;
        var request = new nativeXMLHttpRequest();
        var hasValidOnReadyStateChange = false;
        
        // The object has been constructed.
        this.__defineGetter__("UNSENT", function() {
            return 0;
        });
        
        // The open() method has been successfully invoked. During this state request headers can be set using setRequestHeader() and the request can be made using the send() method.
        this.__defineGetter__("OPENED", function() {
            return 1;
        });
        
        /*
         * All HTTP headers have been received. Several response members of the object are now available.
         * HEADERS_RECEIVED is same as SENT of Google Gears.
         */
        this.__defineGetter__("HEADERS_RECEIVED", function() {
            return 2;
        });
        
        /*
         * The response entity body is being received.
         * LOADING is same as Interactive of Google Gears.
         */
        this.__defineGetter__("LOADING", function() {
            return 3;
        });
        
        // The data transfer has been completed or something went wrong during the transfer (e.g. infinite redirects).
        this.__defineGetter__("DONE", function() {
            return 4;
        });
        
        // The XMLHttpRequest object can be in several states. The readyState attribute, on getting, must return the current state, which must be one of the above values.
        this.__defineGetter__("readyState", function() {
            return request.readyState;
        });
        
        this.__defineGetter__("status", function() {
            var result = 0;
            
            try {
                result = request.status;
            } 
            catch (e) {
            }
            
            // Return the HTTP status text.
            return result;
        });
        
        this.__defineGetter__("statusText", function() {
            var result = "";
            
            try {
                result = request.statusText;
            } 
            catch (e) {
            }
            
            // Return the HTTP status text. 
            return result;
        });
        
        this.__defineGetter__("responseText", function() {
            var result = "";
            
            try {
                result = request.responseText;
            } 
            catch (e) {
            }
            
            // Return the text response entity body. 
            return result;
        });
        
        this.__defineGetter__("responseXML", function() {
            // If the state is not DONE return null and terminate these steps
            if (!(this.readyState == this.DONE)) {
                return null;
            }
            
            //Return the XML response entity body.
            return document.createTextNode(this.responseText);
        });
        
        this.open = function(method, url, sync, username, password) {
            if (!util.hasArgument(arguments) || arguments.length == 1) {
                throw errorsMap["not_enough_args"];
            }
            
            try {
            	switch(arguments.length) {
            		case 2:
            			// Native XMLHttp Request must have three arguments
            			// However, gears only takes two. Therefore, we
            			// should make request asynchronous to support
            			// previous functionality.
            			request.open(method, url, true);
            			break;
            		case 3:
            			request.open(method, url, sync);
            			break;
            		case 4:
            			request.open(method, url, sync, username);
            			break;
            		default:
            			request.open(method, url, sync, username, password);
            	}
            } 
            catch (e) {
            }
        };
        
        this.send = function(data) {
            if (util.hasTooManyArguments(arguments)) {
                throw errorsMap["invalid_state_err"];
            }
            
            // Gears accept data to be a blob which we are not supporting here.
            if (data && !util.isString(data)) {
                throw errorsMap["TMP_ERROR"];
            }
            
            try {
                request.send(data);
            } 
            catch (e) {
                throw errorsMap["invalid_state_err"];
            }
        };
        
        this.abort = function() {
            try {
                request.abort();
            } 
            catch (e) {
            }
        };
        
        this.getResponseHeader = function(header) {
            if (!util.isString(header)) {
                return null;
            }
            
            try {
                return request.getResponseHeader(header);
            } 
            catch (e) {
                throw errorsMap["invalid_state_err"];
            }
        };
        
        this.getAllResponseHeaders = function() {
            try {
                return request.getAllResponseHeaders();
            } 
            catch (e) {
                throw errorsMap["invalid_state_err"];
            }
        };
        
        this.setRequestHeader = function(header, value) {
            if (!util.hasArgument(arguments) || arguments.length == 1) {
                throw errorsMap["not_enough_args"];
            }
            else if (!header || !util.isString(header) || !value || !util.isString(value)) {
                return;
            }
            
            try {
                request.setRequestHeader(header, value);
            } 
            catch (e) {
                throw errorsMap["invalid_state_err"];
            }
        };
        
        this.__defineSetter__("onreadystatechange", function(handler) {
            /*
             * The behaviour in 6.0 browser:
             * If the handler is not a function
             * (this includes undefined, string, number, etc)
             * Then onreadystatechange is set to null (Gears would throw exception)
             * Otherwise assign the function to onreadystatechange
             */
            if (util.isFunction(handler)) {
                hasValidOnReadyStateChange = true;
                request.onreadystatechange = function() {
                    handler.apply(that);
                };
            }
            else {
                hasValidOnReadyStateChange = false;
            }
        });
        
        this.__defineGetter__("onreadystatechange", function() {
            if (hasValidOnReadyStateChange) {
                return request.onreadystatechange;
            }
            return null;
        });
        
        // The object name is not 'XMLHttpRequest' to prevent collision with a global XMLHttpRequest object.
        // Overriding toString method to return expected string.
        this.toString = function() {
            return "[object XMLHttpRequest]";
        };
    };
    
    openDatabase = function(name, version, displayName, estimatedSize) {
        if (!util.hasArgument(arguments)) {
            throw errorsMap["syntax_err"];
        }
        else if (!util.isString(name)) {
            throw errorsMap["invalid_state_err"];
        }
        
        try {
            return new Database(name);
        } 
        catch (e) {
        }
    };
    
    function Database(name) {
        var db = google.gears.factory.create("beta.database");
        
        try {
            db.open(name);
        } 
        catch (e) {
            throw errorsMap["TMP_ERROR"];
        }
        
        // Should return immediately, so timer is set.
        this.transaction = function(callback, errorCallback, successCallback) {
            if (!util.hasArgument(arguments)) {
                throw errorsMap["invalid_state_err"];
            }
            else if (!util.isFunction(callback) || (errorCallback && !util.isFunction(errorCallback)) || (successCallback && !util.isFunction(successCallback))) {
                throw errorsMap["type_mismatch_err"];
            }
            
            setTimeout(function() {
                transactionAsync(callback, errorCallback, successCallback);
            }, 50);
        };
        
        // Should return immediately, so timer is set.
        this.readTransaction = function(callback, errorCallback, successCallback) {
            if (!util.hasArgument(arguments)) {
                throw errorsMap["type_mismatch_err"];
            }
            else if (!util.isFunction(callback) || (errorCallback && !util.isFunction(errorCallback)) || (successCallback && !util.isFunction(successCallback))) {
                throw errorsMap["type_mismatch_err"];
            }
            
            setTimeout(function() {
                transactionAsync(callback, errorCallback, successCallback, true);
            }, 50);
        };
        
        function transactionAsync(callback, errorCallback, successCallback, readOnly) {
            try {
                // Optimizing by using begin and commit commands.
                db.execute("begin", null);
                var sqlTransaction = new SQLTransaction(db, readOnly);
                callback(sqlTransaction);
                var lastError = sqlTransaction.getLastError();
                
                if (lastError) {
                    throw lastError;
                }
            } 
            catch (e) {
                if (errorCallback) {
                    errorCallback(e instanceof SQLError ? e : new SQLError());
                }
                return;
            }
            finally {
                db.execute("commit", null);
            }
            
            if (successCallback) {
                successCallback();
            }
        }
        
        // Google gears doesn't support version. version will return 0.0 as default
        this.__defineGetter__("version", function() {
            return "0.0";
        });
        
        // changeVersion does nothing
        this.changeVersion = function() {
        };
        
        
        this.toString = function() {
            return "[object Database]";
        };
    }
    
    function SQLTransaction(db, mode) {
        var readOnly = mode;
        var lastError;
        
        this.executeSql = function(sqlStatement, args, callback, errorCallback) {
            if (!util.hasArgument(arguments)) {
                throw errorsMap["syntax_err"];
            }
            
            try {
                if (readOnly) {
                    // In readonly mode, checking if select is at the begining of the sqlStatement.  
                    sqlStatement = sqlStatement.replace(/^\s+|\s+$/g, "");
                    if (sqlStatement.search(/^select/i) != 0) {
                        throw new SQLError(1, errorsMap["not_authorized"]);
                    }
                }
                
                var gearsResultSet;
                
                try {
                    gearsResultSet = db.execute(sqlStatement, args);
                } 
                catch (e) {
                    if (e.toString().search(/constraint failed/) != -1) {
                        throw new SQLError(6, errorsMap["constraint_failed"]);
                    }
                    else {
                        throw new SQLError();
                    }
                }
                
                if (util.isFunction(callback)) {
                    callback(this, new SQLResultSet(db.lastInsertRowId, db.rowsAffected, buildSQLResultSetRowList(gearsResultSet)));
                }
                
                //It's required to call close() when finished with any result set.
                if (gearsResultSet) {
                    gearsResultSet.close();
                    gearsResultSet = null;
                }
                
            } 
            catch (e) {
                lastError = (e instanceof SQLError ? e : new SQLError());
                
                if (util.isFunction(errorCallback)) {
                    errorCallback(this, lastError);
                }
            }
        };
        
        this.getLastError = function() {
            return lastError;
        };
        
        this.toString = function() {
            return "[object SQLTransaction]";
        };
    }
    
    function SQLResultSet(insertId, rowsAffected, sqlResultSetRowList) {
        this.__defineGetter__("insertId", function() {
            return insertId;
        });
        
        this.__defineGetter__("rowsAffected", function() {
            return rowsAffected;
        });
        
        this.__defineGetter__("rows", function() {
            return sqlResultSetRowList;
        });
        
        this.toString = function() {
            return "[object SQLResultSet]";
        };
    }
    
    function SQLResultSetRowList(items) {
        this.__defineGetter__("length", function() {
            return items.length;
        });
        
        this.item = function(index) {
            if (index < 0 || index >= this.length) {
                return null;
            }
            
            return items[index];
        };
        
        this.toString = function() {
            return "[object SQLResultSetRowList]";
        };
    }
    
    function SQLError(code, message) {
        code = code || 0;
        message = message || errorsMap["db_error_unknown"];
        
        this.__defineGetter__("code", function() {
            return code;
        });
        
        this.__defineGetter__("message", function() {
            return message;
        });
        
        this.toString = function() {
            return "[object SQLError]";
        };
    }
    
    function buildSQLResultSetRowList(gearsResultSet) {
        var items = new Array();
        
        while (gearsResultSet.isValidRow()) {
            fieldCount = gearsResultSet.fieldCount();
            
            var item = new Object();
            for (var counter = 0; counter < fieldCount; counter++) {
                var fieldName = gearsResultSet.fieldName(counter);
                item[fieldName] = gearsResultSet.field(counter);
            }
            
            items.push(item);
            
            gearsResultSet.next();
        }
        
        return new SQLResultSetRowList(items);
    }
    
    /*
     * workerRelay is responsible for keeping track of messages
     * mainly to make sure that no message is lost while the worker
     * is initializing
     * workerRelay creates all userWorkers using one workerpool,
     * although this means that the childWorkers are capable of communicating with each other
     * we are not allowing it by restricting the workerId the childWorkers may send messages to
     */    
    var workerRelay = (function() {
    
        /* Location of worker shim in URL.*/
        var workerWrapperUrl = "html5_worker.js";
        
        var relayMap = {};
        var relayWP = google.gears.factory.create("beta.workerpool");
        
        relayWP.onmessage = function(messageText, senderId, messageObject) {
        	if (messageObject.body.type == "ready") {
                //a one-time signal that the initialization is done
                //start working on the backlog if there is one
                relayMap[messageObject.sender].ready = true;
                processQueue(messageObject.sender);
            }
            else if (messageObject.body.type == "message") {
            	//Test to see if sender contained in object is valid            	
               	var mainWorker = relayMap[messageObject.sender].main;
                var onMessageFunc = mainWorker.onmessage;
                if (util.isFunction(onMessageFunc)) {
                    onMessageFunc.call(mainWorker, {
                        data: messageObject.body.content
                    });
                }
            }
            else if (messageObject.body.type == "error") {
                var mainWorker = relayMap[messageObject.sender].main;
                var onErrorFunc = mainWorker.onerror;
                if (util.isFunction(onErrorFunc)) {
                    onErrorFunc.call(mainWorker, messageObject.body.content);
                }
            }
            else if (messageObject.body.type == "debug") {
            }
        };
        
        this.createNewWorker = function(fileUrl, mainWorker) {
            var childId = relayWP.createWorkerFromUrl(workerWrapperUrl);
            fetchAndSendScript(fileUrl, childId);
            
            relayMap[childId] = {
                queue: new Array(),
                main: mainWorker,
                ready: false
            };
            return childId;
        };
        
        this.postToWorker = function(message, childId) {
            if (relayMap[childId].ready && !relayMap[childId].queue.length) {
                relayWP.sendMessage(message, childId);
            }
            else {
                addToQueue(message, childId);
            }
        };
        
        function processQueue(childId) {
            while (relayMap[childId].queue.length) {
                var msg = relayMap[childId].queue.shift();
                relayWP.sendMessage(msg, childId);
            }
        }
        
        function addToQueue(message, childId) {
            relayMap[childId].queue.push(message);
        }
        
        function fetchAndSendScript(fileUrl, childId) {
            var requestFile = google.gears.factory.create("beta.httprequest");
            requestFile.open('GET', fileUrl);
            requestFile.onreadystatechange = function() {
                if (requestFile.readyState == 4 && requestFile.status == 200) {
                	relayWP.sendMessage(requestFile.responseText, childId);
                }                 
            };
            
            requestFile.send();
        }
        return this;
    })();
    
    /*The Worker object is a facade to simulate HTML5 
     *Beside error checking,
     *the actual processing is delegated to workerRelay
     */
    Worker = function(fileUrl) {
        if (!workerRelay) {
            throw "Elemented with id 'html5_init' is not found. Worker with " + fileUrl + " cannot be created";
        }
        
        if (!util.hasArgument(arguments)) {
            throw errorsMap["not_enough_args"];
        }
        
        if (!util.isString(fileUrl)) {
            fileUrl = " ";
        }
        else if (!fileUrl.length) {
            throw errorsMap["syntax_err"];
        }
        
        var onMessageFunction = null;
        var sendToThisId;
        var INSTANCE_CONTEXT = this;
        
        this.__defineSetter__("onmessage", function(func) {
            //only reassign if onmessage is a valid function
            //otherwise let it go to null
            
            if (!util.isFunction(func)) {
                onMessageFunction = null;
                return;
            }
            else {
                onMessageFunction = func;
            }
        });
        
        this.__defineGetter__("onmessage", function() {
            return onMessageFunction;
        });
        
        this.postMessage = function(message) {
            if (!util.isPositiveNumber(sendToThisId)) {
                return;
            }
            
            if (util.hasTooManyArguments(arguments)) {
                throw errorsMap["worker_error_message_port"];
            }
            
            workerRelay.postToWorker(message, sendToThisId);
        };
        
        this.__defineSetter__("onerror", function(func) {
            window.onerror = func;
        });
        
        this.__defineGetter__("onerror", function() {
            return window.onerror;
        });
        
        this.toString = function() {
            return "[object Worker]";
        };
        
        if (fileUrl.indexOf(".js") != -1) {
            sendToThisId = workerRelay.createNewWorker(fileUrl, INSTANCE_CONTEXT);
        }
    };
    
    /* Utility class */
    function Utility() {
        // hasArgument() - returns true if at least 1 arg is present (including
        // null or undefined), false otherwise
        this.hasArgument = function(args) {
            return args && (args.length > 0);
        };
        
        // hasTooManyArguments() - returns true if number of args passed is
        // greater than number expected, false otherwise
        this.hasTooManyArguments = function(args) {
            return args && (args.length > args.callee.length);
        };
        
        // nullUndefinedArgument() - returns true if arg is null or undefined,
        // false otherwise
        this.nullUndefinedArgument = function(arg) {
            return (this.isNull(arg) || this.isUndefined(arg));
        };
        
        // isUndefined() - returns true if arg is undefined, false otherwise
        // Agree it looks bizarre but safe, since undefined can be re-assigned.
        this.isUndefined = function(arg) {
            return (arg == null && arg !== null);
        };
        
        // isNull() - returns true if arg is null, false otherwise
        this.isNull = function(arg) {
            return (arg === null);
        };
        
        // isNumber() - returns true if arg is of type number, false otherwise
        this.isNumber = function(arg) {
            return typeof arg == "number";
        };
        
        // isPositiveNumber() - returns true if arg is of type number and is
        // positive, false otherwise
        this.isPositiveNumber = function(arg) {
            return this.isNumber(arg) && (arg >= 0);
        };
        
        // isNumberInRange() - returns true if arg is of type number and in the
        // specified range, false otherwise
        this.isNumberInRange = function(arg, low, high) {
            return this.isNumber(arg) && (arg >= low && arg <= high);
        };
        
        // isString() - returns true if arg is of type string, false otherwise
        this.isString = function(arg) {
            return typeof arg == "string";
        };
        
        // isNonEmptyString() - returns true if arg is of type string and is
        // non-empty, false otherwise
        this.isNonEmptyString = function(arg) {
            return this.isString(arg) && (arg.length > 0);
        };
        
        // isFunction() - returns true if arg is of type function, false
        // otherwise
        this.isFunction = function(arg) {
            return typeof arg == "function";
        };
        
        // isBoolean() - returns true if arg is of type boolean, false otherwise
        this.isBoolean = function(arg) {
            return typeof arg == "boolean";
        };
        
        // isObject() - returns true if arg is of type object, false otherwise
        this.isObject = function(arg) {
            return typeof arg == "object";
        };
        
        // isArray() - returns true if arg is of type array, false otherwise
        this.isArray = function(arg) {
            return (this.isObject(arg) && (arg instanceof Array));
        };
        
        // isStringArray() - returns true if arg is of type array of strings,
        // false otherwise
        this.isStringArray = function(arg) {
            var result = false;
            var that = this;
            
            if (this.isArray(arg)) {
                result = (function(arg) {
                    for (var i = 0; i < arg.length; i++) {
                        if (!that.isString(arg[i])) {
                            return false;
                        }
                    }
                    
                    return true;
                })(arg);
            }
            return result;
        };
        
        // argumentsLength - returns an arguments length
        this.argumentsLength = function(args) {
            return args.length;
        };
    }
    
    var util = new Utility();
    
    //Initializing exception messages
    var errorsMap = (function() {
        var errors = {
            "geo_watch_getCurrent": new Error("TYPE_MISMATCH_ERR:DOM Exception 17"),
            
            "geo_clearWatch": new Error("Unexpected error: Not enough arguments [nsIDOMGeoGeolocation.clearWatch]"),
            
            "geo_coordinates_nullUndefinedLatitude": new Error("Latitude should not be null or undefined"),
            
            "geo_coordinates_nullUndefinedLongitude": new Error("Longitude should not be null or undefined"),
            
            "invalid_state_err": new Error("INVALID_STATE_ERR: DOM Exception 11"),
            
            "syntax_err": new Error("SYNTAX_ERR: DOM Exception 12"),
            
            "type_mismatch_err": new Error("TYPE_MISMATCH_ERR: DOM Exception 17"),
            
            "not_enough_args": new SyntaxError("Not enough arguments"),
            
            "not_authorized": "not authorized",
            
            "constraint_failed": "constraint failed",
            
            "db_error_unknown": "Unknown database error occured",
            
            "worker_error_message_port": new Error("Type error"),
            
            "TMP_ERROR": new Error("Temporary Error Message")
        };
        
        return errors;
        
    })();
    
})();

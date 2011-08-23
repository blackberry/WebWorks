(function(){if(!window.google||!google.gears){return;}
attachNamespaces();var timer=google.gears.factory.create("beta.timer");function attachNamespaces(){navigator.geolocation=new Geolocation();}
window.setTimeout=function(){if(!isHandlerValid(arguments[0])){return timer.setTimeout("",0);}
var handler=getTimerTask(arguments);var timeout=getTimeout(arguments[1]);return timer.setTimeout(handler,timeout);};window.setInterval=function(){const MIN_INTERVAL=10;if(!isHandlerValid(arguments[0])){return timer.setInterval("",0);}
var handler=getTimerTask(arguments);var timeout=getTimeout(arguments[1]);timeout=Math.max(timeout,MIN_INTERVAL);return timer.setInterval(handler,timeout);};window.clearTimeout=function(){if(util.isPositiveNumber(arguments[0])){timer.clearTimeout(arguments[0]);}};window.clearInterval=function(){if(util.isPositiveNumber(arguments[0])){return timer.clearInterval(arguments[0]);}};function isHandlerValid(handler){return(util.isString(handler)||util.isFunction(handler));}
function getTimeout(timeout){if(!isFinite(timeout)||!util.isPositiveNumber(timeout)){timeout=0;}
return timeout;}
function getTimerTask(arguments){var handler=arguments[0];if(util.isFunction(handler)&&util.argumentsLength(arguments)>2){var args=new Array();for(var i=0;i<util.argumentsLength(arguments)-2;i++){args[i]=arguments[i+2];}
handler=handler.apply(this,args);}
return handler;}
function Geolocation(){var geolocation=google.gears.factory.create("beta.geolocation");this.getCurrentPosition=function(positionCB,positionErrorCB,positionOptions){try{geolocation.getCurrentPosition(wrapSuccessCB(positionCB),wrapErrorCB(positionErrorCB),new PositionOptions(positionOptions));}
catch(e){throw(errorsMap["geo_watch_getCurrent"]);}};this.watchPosition=function(positionCB,positionErrorCB,positionOptions){try{return geolocation.watchPosition(wrapSuccessCB(positionCB),wrapErrorCB(positionErrorCB),new PositionOptions(positionOptions));}
catch(e){throw(errorsMap["geo_watch_getCurrent"]);}};function wrapSuccessCB(positionCB){var result;if(util.isFunction(positionCB)){result=function(position){positionCB(new Position(createCoordinates(position.coords),position.timestamp));};}
return result;}
function wrapErrorCB(positionErrorCB){var result=null;if(util.isFunction(positionErrorCB)){result=function(error){positionErrorCB(createPositionError(error));};}
return result;}
this.clearWatch=function(watchId){if(util.isUndefined(watchId)){throw(errorsMap["geo_clearWatch"]);}
if(util.isPositiveNumber(watchId)){geolocation.clearWatch(watchId);}};function Position(coords,timestamp){this.__defineGetter__("coords",function(){return coords;});this.__defineGetter__("timestamp",function(){return timestamp;});}
function PositionOptions(args){args=args||{};this.__defineGetter__("enableHighAccuracy",function(){return args.enableHighAccuracy||false;});this.__defineSetter__("enableHighAccuracy",function(value){if(!util.isUndefined(value)&&!util.isBoolean(value)){throw(errorsMap["TMP_ERROR"]);}
args.enableHighAccuracy=value;});this.__defineGetter__("timeout",function(){return args.timeout;});this.__defineSetter__("timeout",function(value){if(!util.isUndefined(value)&&!util.isNumberInRange(value,0,Infinity)){throw(errorsMap["TMP_ERROR"]);}
args.timeout=value;});this.__defineGetter__("maximumAge",function(){return args.maximumAge||0;});this.__defineSetter__("maximumAge",function(value){if(!util.isUndefined(value)&&!util.isNumberInRange(value,0,Infinity)){throw(errorsMap["TMP_ERROR"]);}
args.maximumAge=value;});}
var createCoordinates=function(fromGearsCoords){var Coordinates=function(lat,lon,alt,acc,altAcc){this.__defineGetter__("latitude",function(){return lat;});this.__defineGetter__("longitude",function(){return lon;});this.__defineGetter__("altitude",function(){return util.isPositiveNumber(alt)?alt:null;});this.__defineGetter__("accuracy",function(){return util.isPositiveNumber(acc)?acc:Number.MAX_VALUE;});this.__defineGetter__("altitudeAccuracy",function(){return util.isPositiveNumber(altAcc)?altAcc:null;});this.__defineGetter__("heading",function(){return null;});this.__defineGetter__("speed",function(){return null;});};if(util.nullUndefinedArgument(fromGearsCoords.latitude)){throw errorsMap["geo_coordinates_nullUndefinedLatitude"];}
if(util.nullUndefinedArgument(fromGearsCoords.longitude)){throw errorsMap["geo_coordinates_nullUndefinedLongitude"];}
return new Coordinates(fromGearsCoords.latitude,fromGearsCoords.longitude,fromGearsCoords.altitude,fromGearsCoords.accuracy,fromGearsCoords.altitudeAccuracy);};var createPositionError=function(fromGearsError){var PositionError=function(errorCode,msg){this.__defineGetter__("code",function(){return errorCode;});this.__defineGetter__("message",function(){return msg;});};PositionError.prototype.__defineGetter__("PERMISSION_DENIED",function(){return 1;});PositionError.prototype.__defineGetter__("POSITION_UNAVAILABLE",function(){return 2;});PositionError.prototype.__defineGetter__("TIMEOUT",function(){return 3;});switch(fromGearsError.code){case fromGearsError.PERMISSION_DENIED:return new PositionError(PositionError.prototype.PERMISSION_DENIED,fromGearsError.message);case fromGearsError.TIMEOUT:return new PositionError(PositionError.prototype.TIMEOUT,fromGearsError.message);default:return new PositionError(PositionError.prototype.POSITION_UNAVAILABLE,fromGearsError.message);}};}
var nativeXMLHttpRequest=XMLHttpRequest;XMLHttpRequest=function(){var that=this;var request=new nativeXMLHttpRequest();var hasValidOnReadyStateChange=false;this.__defineGetter__("UNSENT",function(){return 0;});this.__defineGetter__("OPENED",function(){return 1;});this.__defineGetter__("HEADERS_RECEIVED",function(){return 2;});this.__defineGetter__("LOADING",function(){return 3;});this.__defineGetter__("DONE",function(){return 4;});this.__defineGetter__("readyState",function(){return request.readyState;});this.__defineGetter__("status",function(){var result=0;try{result=request.status;}
catch(e){}
return result;});this.__defineGetter__("statusText",function(){var result="";try{result=request.statusText;}
catch(e){}
return result;});this.__defineGetter__("responseText",function(){var result="";try{result=request.responseText;}
catch(e){}
return result;});this.__defineGetter__("responseXML",function(){if(!(this.readyState==this.DONE)){return null;}
return document.createTextNode(this.responseText);});this.open=function(method,url,sync,username,password){if(!util.hasArgument(arguments)||arguments.length==1){throw errorsMap["not_enough_args"];}
try{switch(arguments.length){case 2:request.open(method,url,true);break;case 3:request.open(method,url,sync);break;case 4:request.open(method,url,sync,username);break;default:request.open(method,url,sync,username,password);}}
catch(e){}};this.send=function(data){if(util.hasTooManyArguments(arguments)){throw errorsMap["invalid_state_err"];}
if(data&&!util.isString(data)){throw errorsMap["TMP_ERROR"];}
try{request.send(data);}
catch(e){throw errorsMap["invalid_state_err"];}};this.abort=function(){try{request.abort();}
catch(e){}};this.getResponseHeader=function(header){if(!util.isString(header)){return null;}
try{return request.getResponseHeader(header);}
catch(e){throw errorsMap["invalid_state_err"];}};this.getAllResponseHeaders=function(){try{return request.getAllResponseHeaders();}
catch(e){throw errorsMap["invalid_state_err"];}};this.setRequestHeader=function(header,value){if(!util.hasArgument(arguments)||arguments.length==1){throw errorsMap["not_enough_args"];}
else if(!header||!util.isString(header)||!value||!util.isString(value)){return;}
try{request.setRequestHeader(header,value);}
catch(e){throw errorsMap["invalid_state_err"];}};this.__defineSetter__("onreadystatechange",function(handler){if(util.isFunction(handler)){hasValidOnReadyStateChange=true;request.onreadystatechange=function(){handler.apply(that);};}
else{hasValidOnReadyStateChange=false;}});this.__defineGetter__("onreadystatechange",function(){if(hasValidOnReadyStateChange){return request.onreadystatechange;}
return null;});this.toString=function(){return"[object XMLHttpRequest]";};};openDatabase=function(name,version,displayName,estimatedSize){if(!util.hasArgument(arguments)){throw errorsMap["syntax_err"];}
else if(!util.isString(name)){throw errorsMap["invalid_state_err"];}
try{return new Database(name);}
catch(e){}};function Database(name){var db=google.gears.factory.create("beta.database");try{db.open(name);}
catch(e){throw errorsMap["TMP_ERROR"];}
this.transaction=function(callback,errorCallback,successCallback){if(!util.hasArgument(arguments)){throw errorsMap["invalid_state_err"];}
else if(!util.isFunction(callback)||(errorCallback&&!util.isFunction(errorCallback))||(successCallback&&!util.isFunction(successCallback))){throw errorsMap["type_mismatch_err"];}
setTimeout(function(){transactionAsync(callback,errorCallback,successCallback);},50);};this.readTransaction=function(callback,errorCallback,successCallback){if(!util.hasArgument(arguments)){throw errorsMap["type_mismatch_err"];}
else if(!util.isFunction(callback)||(errorCallback&&!util.isFunction(errorCallback))||(successCallback&&!util.isFunction(successCallback))){throw errorsMap["type_mismatch_err"];}
setTimeout(function(){transactionAsync(callback,errorCallback,successCallback,true);},50);};function transactionAsync(callback,errorCallback,successCallback,readOnly){try{db.execute("begin",null);var sqlTransaction=new SQLTransaction(db,readOnly);callback(sqlTransaction);var lastError=sqlTransaction.getLastError();if(lastError){throw lastError;}}
catch(e){if(errorCallback){errorCallback(e instanceof SQLError?e:new SQLError());}
return;}
finally{db.execute("commit",null);}
if(successCallback){successCallback();}}
this.__defineGetter__("version",function(){return"0.0";});this.changeVersion=function(){};this.toString=function(){return"[object Database]";};}
function SQLTransaction(db,mode){var readOnly=mode;var lastError;this.executeSql=function(sqlStatement,args,callback,errorCallback){if(!util.hasArgument(arguments)){throw errorsMap["syntax_err"];}
try{if(readOnly){sqlStatement=sqlStatement.replace(/^\s+|\s+$/g,"");if(sqlStatement.search(/^select/i)!=0){throw new SQLError(1,errorsMap["not_authorized"]);}}
var gearsResultSet;try{gearsResultSet=db.execute(sqlStatement,args);}
catch(e){if(e.toString().search(/constraint failed/)!=-1){throw new SQLError(6,errorsMap["constraint_failed"]);}
else{throw new SQLError();}}
if(util.isFunction(callback)){callback(this,new SQLResultSet(db.lastInsertRowId,db.rowsAffected,buildSQLResultSetRowList(gearsResultSet)));}
if(gearsResultSet){gearsResultSet.close();gearsResultSet=null;}}
catch(e){lastError=(e instanceof SQLError?e:new SQLError());if(util.isFunction(errorCallback)){errorCallback(this,lastError);}}};this.getLastError=function(){return lastError;};this.toString=function(){return"[object SQLTransaction]";};}
function SQLResultSet(insertId,rowsAffected,sqlResultSetRowList){this.__defineGetter__("insertId",function(){return insertId;});this.__defineGetter__("rowsAffected",function(){return rowsAffected;});this.__defineGetter__("rows",function(){return sqlResultSetRowList;});this.toString=function(){return"[object SQLResultSet]";};}
function SQLResultSetRowList(items){this.__defineGetter__("length",function(){return items.length;});this.item=function(index){if(index<0||index>=this.length){return null;}
return items[index];};this.toString=function(){return"[object SQLResultSetRowList]";};}
function SQLError(code,message){code=code||0;message=message||errorsMap["db_error_unknown"];this.__defineGetter__("code",function(){return code;});this.__defineGetter__("message",function(){return message;});this.toString=function(){return"[object SQLError]";};}
function buildSQLResultSetRowList(gearsResultSet){var items=new Array();while(gearsResultSet.isValidRow()){fieldCount=gearsResultSet.fieldCount();var item=new Object();for(var counter=0;counter<fieldCount;counter++){var fieldName=gearsResultSet.fieldName(counter);item[fieldName]=gearsResultSet.field(counter);}
items.push(item);gearsResultSet.next();}
return new SQLResultSetRowList(items);}
var workerRelay=(function(){var workerWrapperUrl="html5_worker.js";var relayMap={};var relayWP=google.gears.factory.create("beta.workerpool");relayWP.onmessage=function(messageText,senderId,messageObject){if(messageObject.body.type=="ready"){relayMap[messageObject.sender].ready=true;processQueue(messageObject.sender);}
else if(messageObject.body.type=="message"){var mainWorker=relayMap[messageObject.sender].main;var onMessageFunc=mainWorker.onmessage;if(util.isFunction(onMessageFunc)){onMessageFunc.call(mainWorker,{data:messageObject.body.content});}}
else if(messageObject.body.type=="error"){var mainWorker=relayMap[messageObject.sender].main;var onErrorFunc=mainWorker.onerror;if(util.isFunction(onErrorFunc)){onErrorFunc.call(mainWorker,messageObject.body.content);}}
else if(messageObject.body.type=="debug"){}};this.createNewWorker=function(fileUrl,mainWorker){var childId=relayWP.createWorkerFromUrl(workerWrapperUrl);fetchAndSendScript(fileUrl,childId);relayMap[childId]={queue:new Array(),main:mainWorker,ready:false};return childId;};this.postToWorker=function(message,childId){if(relayMap[childId].ready&&!relayMap[childId].queue.length){relayWP.sendMessage(message,childId);}
else{addToQueue(message,childId);}};function processQueue(childId){while(relayMap[childId].queue.length){var msg=relayMap[childId].queue.shift();relayWP.sendMessage(msg,childId);}}
function addToQueue(message,childId){relayMap[childId].queue.push(message);}
function fetchAndSendScript(fileUrl,childId){var requestFile=google.gears.factory.create("beta.httprequest");requestFile.open('GET',fileUrl);requestFile.onreadystatechange=function(){if(requestFile.readyState==4&&requestFile.status==200){relayWP.sendMessage(requestFile.responseText,childId);}};requestFile.send();}
return this;})();Worker=function(fileUrl){if(!workerRelay){throw"Elemented with id 'html5_init' is not found. Worker with "+fileUrl+" cannot be created";}
if(!util.hasArgument(arguments)){throw errorsMap["not_enough_args"];}
if(!util.isString(fileUrl)){fileUrl=" ";}
else if(!fileUrl.length){throw errorsMap["syntax_err"];}
var onMessageFunction=null;var sendToThisId;var INSTANCE_CONTEXT=this;this.__defineSetter__("onmessage",function(func){if(!util.isFunction(func)){onMessageFunction=null;return;}
else{onMessageFunction=func;}});this.__defineGetter__("onmessage",function(){return onMessageFunction;});this.postMessage=function(message){if(!util.isPositiveNumber(sendToThisId)){return;}
if(util.hasTooManyArguments(arguments)){throw errorsMap["worker_error_message_port"];}
workerRelay.postToWorker(message,sendToThisId);};this.__defineSetter__("onerror",function(func){window.onerror=func;});this.__defineGetter__("onerror",function(){return window.onerror;});this.toString=function(){return"[object Worker]";};if(fileUrl.indexOf(".js")!=-1){sendToThisId=workerRelay.createNewWorker(fileUrl,INSTANCE_CONTEXT);}};function Utility(){this.hasArgument=function(args){return args&&(args.length>0);};this.hasTooManyArguments=function(args){return args&&(args.length>args.callee.length);};this.nullUndefinedArgument=function(arg){return(this.isNull(arg)||this.isUndefined(arg));};this.isUndefined=function(arg){return(arg==null&&arg!==null);};this.isNull=function(arg){return(arg===null);};this.isNumber=function(arg){return typeof arg=="number";};this.isPositiveNumber=function(arg){return this.isNumber(arg)&&(arg>=0);};this.isNumberInRange=function(arg,low,high){return this.isNumber(arg)&&(arg>=low&&arg<=high);};this.isString=function(arg){return typeof arg=="string";};this.isNonEmptyString=function(arg){return this.isString(arg)&&(arg.length>0);};this.isFunction=function(arg){return typeof arg=="function";};this.isBoolean=function(arg){return typeof arg=="boolean";};this.isObject=function(arg){return typeof arg=="object";};this.isArray=function(arg){return(this.isObject(arg)&&(arg instanceof Array));};this.isStringArray=function(arg){var result=false;var that=this;if(this.isArray(arg)){result=(function(arg){for(var i=0;i<arg.length;i++){if(!that.isString(arg[i])){return false;}}
return true;})(arg);}
return result;};this.argumentsLength=function(args){return args.length;};}
var util=new Utility();var errorsMap=(function(){var errors={"geo_watch_getCurrent":new Error("TYPE_MISMATCH_ERR:DOM Exception 17"),"geo_clearWatch":new Error("Unexpected error: Not enough arguments [nsIDOMGeoGeolocation.clearWatch]"),"geo_coordinates_nullUndefinedLatitude":new Error("Latitude should not be null or undefined"),"geo_coordinates_nullUndefinedLongitude":new Error("Longitude should not be null or undefined"),"invalid_state_err":new Error("INVALID_STATE_ERR: DOM Exception 11"),"syntax_err":new Error("SYNTAX_ERR: DOM Exception 12"),"type_mismatch_err":new Error("TYPE_MISMATCH_ERR: DOM Exception 17"),"not_enough_args":new SyntaxError("Not enough arguments"),"not_authorized":"not authorized","constraint_failed":"constraint failed","db_error_unknown":"Unknown database error occured","worker_error_message_port":new Error("Type error"),"TMP_ERROR":new Error("Temporary Error Message")};return errors;})();})();
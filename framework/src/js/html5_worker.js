var wp=google.gears.workerPool;var mainPoolId=100;var scriptFetched=false;var workerId=-1;var __CONTEXT__=this;function runScript(sourceScript){try{eval(sourceScript);return true;}
catch(e){return false;}}
function wrapGearsErrorInHtml5(workerError){}
wp.onmessage=function(messageText,senderId,messageObject){if(scriptFetched){wp.onmessage=null;}
else{scriptFetched=runScript(messageObject.body);if(scriptFetched){wp.sendMessage({type:"ready"},mainPoolId);}}};this.postMessage=function(message){if(typeof mainPoolId!="number"||mainPoolId<0){return;}
wp.sendMessage({type:"message",content:arguments[0]},mainPoolId);};this.__defineSetter__("onmessage",function(func){wp.onmessage=function(messageText,sendId,messageObject){func.call(__CONTEXT__,{data:messageObject.body});}});this.__defineSetter__("onerror",function(func){wp.onerror=function(errorObject){var html5Error={message:errorObject.message,filename:"Not Implemented",lineno:errorObject.lineNumber,}
func.call(__CONTEXT__,html5Error);wp.sendMessage({type:"error",content:html5Error},mainPoolId);return true;};});
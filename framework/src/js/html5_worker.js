/**
 * 
 * Date: 09/23/2010 Version: 1.0.0.16
 * 
 * Copyright (c) 2010 Research In Motion Limited.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * This License shall be included in all copies or substantial portions of the
 * Software.
 * 
 * The name(s) of the above copyright holders shall not be used in advertising
 * or otherwise to promote the sale, use or other dealings in this Software
 * without prior written authorization.
 * 
 */
var wp = google.gears.workerPool;
var mainPoolId = 100; // This is hard coded because at times because
						// messageObject.sender has incorrect id at times
var scriptFetched = false;
var workerId = -1;
var __CONTEXT__ = this;

function runScript(sourceScript) {
    try {
        eval(sourceScript);
        return true;
    } 
    catch (e) {
        return false;  
    }
}

function wrapGearsErrorInHtml5(workerError) {

}

wp.onmessage = function(messageText, senderId, messageObject) {
    if (scriptFetched) {
        // if script has been successfully evaluated,
        // then we would only get here if onmessage is never assigned
        // in which case we can assume the user intended onmessage = null;
        wp.onmessage = null;
    }
    else {
    	// Attempt to run the the script
        scriptFetched = runScript(messageObject.body);
        if (scriptFetched) {
            wp.sendMessage({
                type: "ready"
            }, mainPoolId);
        }
    }
};

this.postMessage = function(message) {
    if (typeof mainPoolId != "number" || mainPoolId < 0) {
        return;
    }    
    wp.sendMessage({
        type: "message",
        content: arguments[0]
    }, mainPoolId);
};

this.__defineSetter__("onmessage", function(func) {
    wp.onmessage = function(messageText, sendId, messageObject) {        
        func.call(__CONTEXT__, {
            data: messageObject.body
        });        
    }
});

this.__defineSetter__("onerror", function(func) {

    wp.onerror = function(errorObject) {
        var html5Error = {
            message: errorObject.message,
            filename: "Not Implemented",
            lineno: errorObject.lineNumber,
        }
        
        func.call(__CONTEXT__, html5Error);
        wp.sendMessage({
            type: "error",
            content: html5Error
        }, mainPoolId);
        return true;
    };
});

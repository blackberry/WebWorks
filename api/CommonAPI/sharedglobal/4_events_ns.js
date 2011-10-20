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
	if(!this.blackberry) {
		return;
	}
	
	var bb = this.blackberry;
	
	
	function EventMap(){
		var handlers = [];
		
		this.getHandlerById = function(handlerId){
			return handlers[handlerId];
		};
		
		this.addHandler = function(handler){
			handlers.push(handler);
			return handlers.length - 1;
		};
		
		this.removeHandler = function(handlerId){
			if(handlerId > -1 && handlerId < handlers.length) {
				delete handlers[handlerId]; //cannot splice because all published IDs would refer to the wrong handler
			}
		};
		
	};
	
	bb.events = {
		eventsMap  : new EventMap(),
		
		registerEventHandler : function (eventName, eventCallback, eventParams) {
			var handlerId = blackberry.events.eventsMap.addHandler(eventCallback);			
			return handlerId;
		},
		
		getEventHandler : function(handlerId) {
			return blackberry.events.eventsMap.getHandlerById(handlerId);
		}
	};
})();

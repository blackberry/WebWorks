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

package blackberry.system.event;

import blackberry.common.util.json4j.JSONObject;
import blackberry.common.util.json4j.JSONException;

import blackberry.core.JSExtensionReturnValue;

/** 
 * Helper class for creating return value objects.
 *
 * @author ababut
 */
class SystemEventReturnValue {
    private static final int RC_FAIL = JSExtensionReturnValue.FAIL;
    private static final int RC_SUCCESS = JSExtensionReturnValue.SUCCESS;
    private static final int RC_CHANNEL_CLOSED = 1;
    
    /**
     * Constant return value for invalid methods requested
     */
    public static final SystemEventReturnValue INVALID_METHOD = new SystemEventReturnValue(new JSExtensionReturnValue(
                    "Invalid method requested",
                    RC_FAIL,
                    new JSONObject()
                ));
    
    /**
     * Constant return value for channel closed event
     */
    public static final SystemEventReturnValue CHANNEL_CLOSED = new SystemEventReturnValue(new JSExtensionReturnValue(
                    "Listening channel closed",
                    RC_CHANNEL_CLOSED,
                    new JSONObject()
                ));
    
    /**
     * Composes a success return value for a given event and its arguments
     *
     * @param event a String representing the event that occurred
     * @param eventArg optional arguments that further describe the event
     */
    public static SystemEventReturnValue getReturnValueForEvent(String event, String eventArg) {
        return new SystemEventReturnValue(new JSExtensionReturnValue(
                            "Event occurred", 
                            RC_SUCCESS, 
                            createJSONReturnData(event, eventArg)
                ));  
    }
    
    /**
     * Composes a error return value for a given event and its arguments
     *
     * @param event a String representing the event that occurred
     * @param eventArg optional arguments that further describe the event
     */
    public static SystemEventReturnValue getErrorForOp(String method, String arg) {
        return new SystemEventReturnValue(new JSExtensionReturnValue(
                            "Error calling [" + method + "] with [" + arg + "]", 
                            RC_FAIL, 
                            new JSONObject()
                ));  
    }
    
    /**
     * Composes a generic successful call to method return value
     *
     * @param method a String representing the method serviced
     * @param arg arguments passed to the method
     */
    public static SystemEventReturnValue getSuccessForOp(String method, String arg) {
        return new SystemEventReturnValue(new JSExtensionReturnValue(
                            "Success calling [" + method + "] with [" + arg + "]", 
                            RC_SUCCESS, 
                            new JSONObject()
                ));  
    }
    
    private static JSONObject createJSONReturnData(String event, String eventArg) {
        StringBuffer jsonBuilder = new StringBuffer();
        
        jsonBuilder.append("{event:");
        jsonBuilder.append( (null == event || "".equals(event)) ? "" : event);
        
        if(null != eventArg && eventArg.length() > 0) {
            jsonBuilder.append(",arg:" + eventArg);
        }
        
        jsonBuilder.append('}');
        
        JSONObject retval = null;
        
        try {
            retval = new JSONObject(jsonBuilder.toString());
        } catch (JSONException e) {
            throw new RuntimeException("Error creating JSON return value for event [" + event + "] with arg [" + eventArg + "]");
        }
        
        return retval;
    }       
    
    private JSExtensionReturnValue _extReturnValue;
    
    private SystemEventReturnValue(JSExtensionReturnValue extensionReturnValue) {
        _extReturnValue = extensionReturnValue;
    }
    
    /**
     * Returns the wrapped JSExtensionReturnValue object
     *
     * @see blackberry.core.JSExtensionReturnValue
     */
    public JSExtensionReturnValue getJSExtensionReturnValue() {
        return _extReturnValue;
    }
}

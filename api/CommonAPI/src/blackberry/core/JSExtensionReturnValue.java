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
package blackberry.core;

import blackberry.common.util.json4j.JSONException;
import blackberry.common.util.json4j.JSONObject;

/**
 * class <code>JSExtensionRetureValue</code> is used to wrap returned data of extensions
 * in a JSONObject. </br>
 */
public class JSExtensionReturnValue {
    // Status code for return value
    public final static int SUCCESS = 0;
    public final static int FAIL = -1;

    private String _msg;
    private int _code;
    private Object _data;
    private JSONObject _returnValue = new JSONObject();

    public JSExtensionReturnValue( String msg, int code, int data ) {
        this._msg = msg;
        this._code = code;
        this._data = new Integer( data );
    }

    public JSExtensionReturnValue( String msg, int code, boolean data ) {
        this._msg = msg;
        this._code = code;
        this._data = new Boolean( data );
    }

    public JSExtensionReturnValue( String msg, int code, Object data ) {
        this._msg = msg;
        this._code = code;
        this._data = data;
    }

    public JSONObject getReturnValue() {
        try {
            _returnValue.put( "code", _code );
            _returnValue.put( "msg", _msg );
            _returnValue.put( "data", _data );
        } catch( JSONException e ) {
            e.printStackTrace();
        }
        return _returnValue;
    }
}
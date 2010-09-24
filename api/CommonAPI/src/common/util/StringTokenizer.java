/*
* Copyright 2010 Research In Motion Limited.
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
package common.util;

public class StringTokenizer {
    private String _toParse;
    private final char _token;

    public StringTokenizer(final String s, final char token) {
        _toParse = s;
        _token = token;
    }

    public boolean hasMoreTokens() {
        return _toParse.length() != 0;
    }

    public String nextToken() {
        final int index = _toParse.indexOf(_token);
        String result = null;
        if (index != -1) {
            result = _toParse.substring(0, index);
            _toParse = _toParse.substring(index + 1);
        }
        else if (_toParse.length() != 0) {
            result = _toParse;
            _toParse = "";
        }
        return result;
    }

    public String toString() {
        return _toParse;
    }
}

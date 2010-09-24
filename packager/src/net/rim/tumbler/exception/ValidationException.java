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
package net.rim.tumbler.exception;

public class ValidationException extends Exception {
    private String          _info;
    
    public ValidationException(String id) {
        super(id);
    }
    
    public ValidationException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public ValidationException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public String getInfo() {
        return _info;
    }
}

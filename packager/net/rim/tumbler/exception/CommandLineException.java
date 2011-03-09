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
package net.rim.tumbler.exception;

public class CommandLineException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private String _info;
	
	public CommandLineException(String id) {
        super(id);
    }
    
    public CommandLineException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public CommandLineException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public CommandLineException(Exception causedBy, String info) {
        super(causedBy);
        _info = info;
    }
    
    public String getInfo() {
        return _info;
    }
}

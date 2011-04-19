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
package net.rim.tumbler.log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Logger {
    
    private static final Locale             LOCALE_EN_CA = new Locale("en", "CA");
    private static ResourceBundle           _bundle;
    
    static {
        _bundle = ResourceBundle.getBundle(
                "net.rim.tumbler.log.resources.MessageBundle", LOCALE_EN_CA);
    }
    
    public static void logMessage(LogType logType, String messageID) {
        printMessage(logType, _bundle.getString(messageID), null);
    }
    
    public static void logMessage(LogType logType, Exception exception) {
        printMessage(logType, exception.getMessage(), null);
    }
    
    public static void logMessage(LogType logType, Exception exception, String info) {
        printMessage(logType, exception.getMessage(), info);
    }
    
    public static void logMessage(LogType logType, String messageID, String info) {
        printMessage(logType, _bundle.getString(messageID), info);
    }
    
    public static void logMessage(LogType logType, String messageID, Object[] info) {
        try {
            String result = MessageFormat.format( _bundle.getString(messageID), info );
            printMessage(logType, result, null);
        } catch (Exception e) {
            // If the pattern/args fail - ignore
        }
    }
    
    public static String getResource(String id) {
        return _bundle.getString(id);
    }
    
    private static void printMessage(LogType logType, String message, String info) {
        String output = message + ((info != null && info.length() > 0) ? "(" + info + ")" : "");
        if (logType != LogType.NONE) {
            System.out.printf("%-12s\t\t%s\n", "[" + logType.toString() + "]", output);
        } else {
            System.out.println(output);
        }
    }
}

package net.rim.tumbler.log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Logger {
    
    private static final Locale             LOCALE_EN_CA = new Locale("en","CA");
    
    private static ResourceBundle           _bundle;
    
    static {
        _bundle = ResourceBundle.getBundle(
                "net.rim.tumbler.log.resources.MessageBundle", LOCALE_EN_CA);
    }
    
    public static void logMessage(LogType logType, String messageID) {
        printMessage(logType, _bundle.getString(messageID), null);
    }
    
    public static void logMessage(LogType logType, Exception exception) {
        printMessage(logType, _bundle.getString(exception.getMessage()), null);
    }
    
    public static void logMessage(LogType logType, String messageID, String info) {
        printMessage(logType, _bundle.getString(messageID), info);
    }
    
    public static void logMessage(LogType logType, String messageID, Object[] info) {
        try {
            String result = MessageFormat.format( _bundle.getString(messageID), info );
            printMessage(logType, result, null);
        } catch (Exception e) {
            // if the pattern/args fail - ignore
        }
    }
    
    public static String getResource(String id) {
        return _bundle.getString(id);
    }
    
    private static void printMessage(LogType logType, String message, String info) {
        String output = message + ((info != null && info.length() > 0) ? "(" + info + ")" : "");
        if (logType != LogType.NONE) {
            System.out.printf("%-12s\t\t%s\n", "[" + logType.toString() + "]", output);
        }
        else {
            System.out.println(output);
        }
    }
}

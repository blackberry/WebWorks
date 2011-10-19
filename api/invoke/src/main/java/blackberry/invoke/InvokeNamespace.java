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
package blackberry.invoke;

import net.rim.device.api.script.Scriptable;

/**
 * This class represents the InvokeNamespace
 * 
 * @author sgolod
 * 
 */
public class InvokeNamespace extends Scriptable {

    public static final String NAME = "blackberry.invoke";

    public static final int APP_ADDRESSBOOK = 0;
    public static final int APP_BLUETOOTH_CONFIG = 1;
    public static final int APP_CALCULATOR = 2;
    public static final int APP_CALENDAR = 3;
    public static final int APP_CAMERA = 4;
    public static final int APP_MAPS = 5;
    public static final int APP_MEMOPAD = 6;
    public static final int APP_MESSAGES = 7;
    public static final int APP_PHONE = 8;
    public static final int APP_SEARCH = 9;
    public static final int APP_TASKS = 10;
    public static final int APP_BROWSER = 11;
    public static final int APP_JAVA = 12;

    private static final String LABEL_APP_ADDRESSBOOK = "APP_ADDRESSBOOK";
    private static final String LABEL_APP_BLUETOOTH_CONFIG = "APP_BLUETOOTH_CONFIG";
    private static final String LABEL_APP_CALCULATOR = "APP_CALCULATOR";
    private static final String LABEL_APP_CALENDAR = "APP_CALENDAR";
    private static final String LABEL_APP_CAMERA = "APP_CAMERA";
    private static final String LABEL_APP_MAPS = "APP_MAPS";
    private static final String LABEL_APP_MEMOPAD = "APP_MEMOPAD";
    private static final String LABEL_APP_MESSAGES = "APP_MESSAGES";
    private static final String LABEL_APP_PHONE = "APP_PHONE";
    private static final String LABEL_APP_SEARCH = "APP_SEARCH";
    private static final String LABEL_APP_TASKS = "APP_TASKS";
    private static final String LABEL_APP_BROWSER = "APP_BROWSER";
    private static final String LABEL_APP_JAVA = "APP_JAVA";

    /**
     * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
     */
    public Object getField( final String name ) throws Exception {

        // constants
        if( name.equals( LABEL_APP_ADDRESSBOOK ) ) {
            return new Integer( APP_ADDRESSBOOK );
        } else if( name.equals( LABEL_APP_BLUETOOTH_CONFIG ) ) {
            return new Integer( APP_BLUETOOTH_CONFIG );
        } else if( name.equals( LABEL_APP_CALCULATOR ) ) {
            return new Integer( APP_CALCULATOR );
        } else if( name.equals( LABEL_APP_CALENDAR ) ) {
            return new Integer( APP_CALENDAR );
        } else if( name.equals( LABEL_APP_CAMERA ) ) {
            return new Integer( APP_CAMERA );
        } else if( name.equals( LABEL_APP_MAPS ) ) {
            return new Integer( APP_MAPS );
        } else if( name.equals( LABEL_APP_MEMOPAD ) ) {
            return new Integer( APP_MEMOPAD );
        } else if( name.equals( LABEL_APP_MESSAGES ) ) {
            return new Integer( APP_MESSAGES );
        } else if( name.equals( LABEL_APP_PHONE ) ) {
            return new Integer( APP_PHONE );
        } else if( name.equals( LABEL_APP_SEARCH ) ) {
            return new Integer( APP_SEARCH );
        } else if( name.equals( LABEL_APP_TASKS ) ) {
            return new Integer( APP_TASKS );
        } else if( name.equals( LABEL_APP_BROWSER ) ) {
            return new Integer( APP_BROWSER );
        } else if( name.equals( LABEL_APP_JAVA ) ) {
            return new Integer( APP_JAVA );
        }

        // functions
        else if( name.equals( InvokeFunction.NAME ) ) {
            return new InvokeFunction();
        }

        return super.getField( name );
    }

}

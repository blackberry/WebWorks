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

package blackberry.ui.dialog;

import java.util.Vector;

import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.script.ScriptableImpl;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.component.Dialog;
import blackberry.ui.dialog.color.ColorPickerDialog;
import blackberry.ui.dialog.datetime.DateTimeDialog;
import blackberry.ui.dialog.select.SelectDialog;

/**
 * Factory class for running dialogs asynchronously
 *
 */
public class DialogRunnableFactory {
    
    /**
     * Factory method for running an asynchronous DateTime dialog
     * @param type the dialog's type
     * @param value the value
     * @param min the minimum value
     * @param max the maximum value
     * @param callback the callback function
     * @return the Runnable responsible for opening the dialog
     */
    public static Runnable getDateTimeRunnable( String type, String value, String min, String max, ScriptableFunction callback) {
        IWebWorksDialog d = new DateTimeDialog( type, value, min, max );
        return new DialogRunnable( d, callback );
    }

    /**
     * Factory method for running an asynchronous ColorPicker dialog
     * @param initialColor the initial color
     * @param callback the callback function
     * @return the Runnable responsible for opening the dialog
     */
    public static Runnable getColorPickerRunnable( int initialColor, ScriptableFunction callback ) {
        ColorPickerDialog d = new ColorPickerDialog( initialColor );
        return new DialogRunnable( d, callback );
    }
    
    /**
     * Factory method for running an asynchronous Select dialog
     * @param allowMultiple flag indicating whether multiple values are allowed
     * @param labels the labels 
     * @param enabled the enabled values
     * @param selected the selected values
     * @param types the types of the values 
     * @param callback the callback function
     * @return the Runnable responsible for opening the dialog
     */
    public static Runnable getSelectRunnable(boolean allowMultiple, String[] labels, boolean[] enabled, boolean[] selected, int[] types, ScriptableFunction callback) {
        IWebWorksDialog d = new SelectDialog(allowMultiple, labels, enabled, selected, types);
        return new DialogRunnable(d, callback);
    }
    
    /**
     * Factory method for running an asynchronous CustomAsk dialog
     * @param message the message to be displayed in the dialog
     * @param buttons the choices presented as buttons
     * @param values the values of the choices
     * @param defaultChoice the default choice
     * @param global the global status
     * @param callback the callback function
     * @return the Runnable responsible for opening the dialog
     */
    public static Runnable getCustomAskRunnable(String message, String[] buttons, int defaultChoice, boolean global /* style, false */, ScriptableFunction callback) {
        Dialog d = new Dialog( message, buttons, null, defaultChoice, null /* bitmap */, global ? Dialog.GLOBAL_STATUS : 0 /* style */);
        return new DialogAsyncRunnable( d, callback );
    }
    
    /**
     * Factory method for running an asynchronous StandardAsk dialog
     * @param message the message to be displayed in the dialog
     * @param type the dialog's type
     * @param defaultChoice
     * @param global the global status
     * @param callback the callback function
     * @return the Runnable responsible for running the dialog
     */
    public static Runnable getStandardAskRunnable(String message, int type, int defaultChoice, boolean global /* style, false */, ScriptableFunction callback) {
    	Dialog d = new Dialog( type, message, defaultChoice, null /* bitmap */, global ? Dialog.GLOBAL_STATUS : 0 /* style */);
        return new DialogAsyncRunnable(d, callback);
    }
    
    private static class DialogRunnable implements Runnable {
        private IWebWorksDialog _dialog;
        private ScriptableFunction _callback;
            
        /**
         * Constructs a <code>DialogRunnable</code> object.
         * 
         * @param dialog
         *            The dialog
         * @param callback
         *            The callback
         */
        DialogRunnable( IWebWorksDialog dialog, ScriptableFunction callback ) {
            _dialog = dialog;
            _callback = callback;
        }
        

        /**
         * Run the dialog.
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() { 
            if(_dialog.show()) {
                Object dialogValue = _dialog.getSelectedValue();
                Object retVal;
                
                boolean isFive = "5".equals(DeviceInfo.getSoftwareVersion().substring(0, 1));
                
                //we'll accept Vector-type dialog return values for arrays
                //otherwise get object's string as all ecma primitives will return a valid string representation of themselves
                try {
                    if (dialogValue instanceof Vector) {
                        Vector v = (Vector)dialogValue;
                        if(isFive) {
                            ScriptableImpl s = new ScriptableImpl();
                            for(int i = 0; i < v.size(); i++) {
                                s.putElement(i, v.elementAt(i));
                            }
                            retVal = s;
                        } else {
                            Object[] s = new Object[v.size()];
                            v.copyInto(s);
                            retVal = s;
                        }
                    } else {
                        retVal = dialogValue.toString();
                    }
                    
                    _callback.invoke(null, new Object[] { retVal });
                } catch (Exception e) {
                    throw new RuntimeException("Invoke callback failed: " + e.getMessage());
                }
            }
        }
    }
    
    private static class DialogAsyncRunnable implements Runnable {
        private Dialog _dialog;
        private ScriptableFunction _callback;
        private Integer _dialogValue;
            
        /**
         * Constructs a <code>DialogRunnable</code> object.
         * 
         * @param dialog
         *            The dialog
         * @param callback
         *            The callback
         */
        DialogAsyncRunnable( Dialog dialog, ScriptableFunction callback ) {
            _dialog = dialog;
            _callback = callback;
        }
        
        /**
         * Run the dialog.
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            _dialogValue = new Integer( _dialog.doModal() );
            // get object's string as all ecma primitives will return a valid string representation of themselves
            Object retVal = _dialogValue.toString();
            try {
                _callback.invoke( null, new Object[] { retVal } );
            } catch( Exception e ) {
                throw new RuntimeException( "Invoke callback failed: " + e.getMessage() );
            }
        }
    }
}

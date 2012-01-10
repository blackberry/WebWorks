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

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.script.ScriptableImpl;

import blackberry.ui.dialog.datetime.DateTimeDialog;
import blackberry.ui.dialog.IWebWorksDialog;
import blackberry.ui.dialog.select.SelectDialog;
import blackberry.ui.dialog.color.ColorPickerDialog;

public class DialogRunnableFactory {
    
    public static Runnable getDateTimeRunnable( String type, String value, String min, String max, ScriptableFunction callback,
            Object thiz ) {
        IWebWorksDialog d = new DateTimeDialog( type, value, min, max );
        return new DialogRunnable( d, callback, thiz );
    }

    public static Runnable getColorPickerRunnable( int initialColor, ScriptableFunction callback, Object thiz ) {
        ColorPickerDialog d = new ColorPickerDialog( initialColor );
        return new DialogRunnable( d, callback, thiz );
    }
    
    public static Runnable getSelectRunnable(boolean allowMultiple, String[] labels, boolean[] enabled, boolean[] selected, int[] types, ScriptableFunction callback, Object thiz) {
        IWebWorksDialog d = new SelectDialog(allowMultiple, labels, enabled, selected, types);
        return new DialogRunnable(d, callback, thiz);
    }
    
    private static class DialogRunnable implements Runnable {
        private IWebWorksDialog _dialog;
        private ScriptableFunction _callback;
        private Object _context;
            
        /**
         * Constructs a <code>DialogRunnable</code> object.
         * 
         * @param dialog
         *            The dialog
         * @param callback
         *            The callback
         * @param context
         *            The context in which the callback executes (its "this" object)
         */
        DialogRunnable( IWebWorksDialog dialog, ScriptableFunction callback, Object context ) {
            _dialog = dialog;
            _callback = callback;
            _context = context;
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
}

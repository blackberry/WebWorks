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

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.DialogFieldManager;

/**
 * Runs a dialog and returns the state.
 * 
 * @author dmeng
 */
public class DialogRunner implements Runnable {
    private Dialog _dialog;
    private boolean _global;
    private int _ret;
    private boolean _isForceClosed;

    /**
     * Constructs a <code>DialogRunner</code> object.
     * 
     * @param dialog
     *            The dialog
     * @param isGlobal
     *            If the Dialog is a global dialog.
     */
    public DialogRunner( Dialog dialog, boolean isGlobal ) {
        _dialog = dialog;
        _global = isGlobal;
        Bitmap bitmap = Bitmap.getPredefinedBitmap( Bitmap.QUESTION );
        setIcon( bitmap );
        // _dialog.setOverlapping( true );
    }

    /**
     * Run the dialog.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if( _global ) {
            UiApplication.getUiApplication().pushGlobalScreen( _dialog, 0, UiEngine.GLOBAL_MODAL | UiEngine.GLOBAL_QUEUE );
            _ret = _dialog.getSelectedValue();
        } else {
            _ret = _dialog.doModal();
        }
    }

    /**
     * @return The dialog return code.
     */
    public Object getReturnCode() {
        if( _isForceClosed ) {
            return net.rim.device.api.script.Scriptable.UNDEFINED;
        }
        return new Integer( _ret );
    }

    /**
     * Returns if the dialog is global.
     * 
     * @return <code>true</code> if yes; <code>false</code> otherwise.
     */
    public boolean isGlobal() {
        return _global;
    }

    /**
     * Close the dialog.
     */
    public void close() {
        _isForceClosed = true;
        _dialog.close();
    }

    private void setIcon( Bitmap image ) {
        BitmapField field = null;
        if( image != null ) {
            field = new BitmapField( null, BitmapField.VCENTER | BitmapField.STAMP_MONOCHROME );
            field.setBitmap( image );
        }
        DialogFieldManager dfm = (DialogFieldManager) _dialog.getDelegate();
        dfm.setIcon( field );
    }
}

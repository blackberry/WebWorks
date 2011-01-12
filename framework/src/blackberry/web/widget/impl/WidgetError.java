/*
 * WidgetError.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.impl;

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class WidgetError extends Dialog implements Runnable, FieldChangeListener {
    
    private ButtonField     _ok;

    /**
     * Constructor.
     */
    public WidgetError( Throwable t, String url ) {
        super( t.getMessage(), null, null, 0, null );
        initialize();
    }

    /*Override*/ public void run() {
        doModal();

        synchronized ( this ) {
            notify();
        }
    }
    
    /**
     * Handles button select events for this dialog.
     *
     * <p> If the provided field is a {@link ButtonField}, this method invokes
     * {@link #select()} to signal a button push.
     *
     * @param field Field whose state changed.
     * @param context Information specifying the origin of the change.
     * */
    public void fieldChanged( Field field, int context )
    {
        if(field != null) {
            if (field == _ok) {
                select();
            }
        } 
    }
    
    private void initialize() {
        HorizontalFieldManager mgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
        add(mgr);
        _ok = new ButtonField("OK", Field.FIELD_HCENTER);
        _ok.setChangeListener(this);
        mgr.add(_ok);
        setEscapeEnabled( true );
    }
}

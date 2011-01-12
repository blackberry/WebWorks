/*
 * WidgetScreen.java
 *
 * Copyright © 2009 Research In Motion Limited.  All rights reserved.
 */

package blackberry.web.widget;

import net.rim.device.api.ui.container.MainScreen;

/**
 * Just a pass through class - provides flexibility with future screen types (I hope).
 */
public abstract class WidgetScreen extends MainScreen {
    protected WidgetScreen() {
        super();
    }
    
    protected WidgetScreen(long style) {
        super(style);
    }
}

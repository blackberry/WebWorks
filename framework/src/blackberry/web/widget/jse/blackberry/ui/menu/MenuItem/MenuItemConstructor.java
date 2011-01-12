/*
 * MenuItemConstructor.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.jse.blackberry.ui.menu.MenuItem;

import blackberry.web.widget.jse.base.FunctionSignature;
import blackberry.web.widget.jse.base.ScriptableConstructorBase;

import net.rim.device.api.script.*;


public class MenuItemConstructor extends ScriptableConstructorBase {
    public static final String NAME = "MenuItem";

    /* @Override */ protected Object createNewObj( Object[] args ) {       
        if (args.length == 4) {
            return new MenuItemObject((Boolean)args[0], (Integer)args[1], (String)args[2], (ScriptableFunction)args[3]);
        }
        if (args.length == 3) {
            return new MenuItemObject((Boolean)args[0], (Integer)args[1], (String)args[2], null);
        } else {
            return new MenuItemObject((Boolean)args[0], (Integer)args[1], "", null);
        }
    }
    
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature(4);
        fs.addParam(Boolean.class, true);
        fs.addParam(Integer.class, true);
        fs.addNullableParam(String.class, false);
        fs.addParam(ScriptableFunction.class, false);
        return new FunctionSignature[] { fs };
    }    
}

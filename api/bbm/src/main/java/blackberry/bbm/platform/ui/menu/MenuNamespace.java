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
package blackberry.bbm.platform.ui.menu;

import blackberry.bbm.platform.BBMPlatformNamespace;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;
import net.rim.blackberry.api.bbm.platform.ui.MenuItemManager;
import net.rim.device.api.script.Scriptable;

public class MenuNamespace extends Scriptable {

    public static final String NAME = "menu";
    
    public static final String CONST_MAX_MENU_ITEMS =               "MAX_MENU_ITEMS";
    public static final String CONST_MAX_LENGTH_MENU_ITEM_LABEL =   "MAX_LENGTH_MENU_ITEM_LABEL";
    public static final String CONST_MAX_LENGTH_MENU_ITEM_MESSAGE = "MAX_LENGTH_MENU_ITEM_MESSAGE";
    
    public static final String FUNC_ADD_INVITE_ITEM = "addInvitationMenuItem";
    public static final String FUNC_CLEAR_ITEMS =     "clearMenuItems";
    public static final String FUNC_HAS_ITEM =        "hasMenuItem";
    public static final String FUNC_REMOVE_ITEM =     "removeMenuItem";
    
    private static MenuNamespace _instance;
    
    private MenuItemManager _menuMgr;
    
    private MenuNamespace() { }
    
    public static MenuNamespace getInstance() {
        if(_instance == null) {
            _instance = new MenuNamespace();
        }
        return _instance;
    }

    public Object getField(String name) throws Exception {
        _menuMgr = BBMPlatformNamespace.getInstance().getContext().getUIService().getMenuItemManager();
        if(name.equals(FUNC_ADD_INVITE_ITEM)) {
            return new AddInviteItemFunction();
        } else if(name.equals(FUNC_CLEAR_ITEMS)) {
            return new ClearItemsFunction();
        } else if(name.equals(FUNC_HAS_ITEM)) {
            return new HasItemFunction();
        } else if(name.equals(FUNC_REMOVE_ITEM)) {
            return new RemoveItemFunction();
        } else if(name.equals(CONST_MAX_MENU_ITEMS)) {
            return new Integer(MenuItemManager.getMaxMenuItems());
        } else if(name.equals(CONST_MAX_LENGTH_MENU_ITEM_LABEL)) {
            return new Integer(MenuItemManager.getMaxLengthMenuItemLabel());
        } else if(name.equals(CONST_MAX_LENGTH_MENU_ITEM_MESSAGE)) {
            return new Integer(MenuItemManager.getMaxLengthMenuItemMessage());
        } else {
            return super.getField(name);
        }
    }
    
    private class AddInviteItemFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final Scriptable options = (Scriptable) args[0];
            final int menuItemId = ((Integer) options.getField("menuItemId")).intValue();
            final int order =      ((Integer) options.getField("order")).intValue();
            final String label =     (String) options.getField("label");
            final String inviteMsg = (String) options.getField("invitationMessage");
            
            _menuMgr.addChannelInvitationMenuItem(menuItemId, inviteMsg, label, order);
            return UNDEFINED;
        }
        
        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Scriptable.class, true);
            return new FunctionSignature[] {
                sig1,
            };
        }
    }
    
    private class ClearItemsFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            _menuMgr.removeAll();
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            return new FunctionSignature[] {
                new FunctionSignature(0),
            };
        }
    }
    
    private class RemoveItemFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final int menuItemId = ((Integer) args[0]).intValue();
            _menuMgr.remove(menuItemId);
            return UNDEFINED;
        }

        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Integer.class, true);
            return new FunctionSignature[] {
                sig1,
            };
        }
    }
    
    private class HasItemFunction extends ScriptableFunctionBase {
        
        protected Object execute(Object thiz, Object[] args) throws Exception {
            final int menuItemId = ((Integer) args[0]).intValue();
            return new Boolean(_menuMgr.exists(menuItemId));
        }

        protected FunctionSignature[] getFunctionSignatures() {
            final FunctionSignature sig1 = new FunctionSignature(1);
            sig1.addParam(Integer.class, true);
            return new FunctionSignature[] {
                sig1,
            };
        }
    }
}

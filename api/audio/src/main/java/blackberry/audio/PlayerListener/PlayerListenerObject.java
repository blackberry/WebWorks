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
package blackberry.audio.PlayerListener;

import java.util.Hashtable;

import javax.microedition.media.Player;

import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

public class PlayerListenerObject extends ScriptableFunctionBase {
    public static final String NAME = "addPlayerListener";

    private PlayerListenerController _playerListenerCtrl = null;
    // Need this Hashtable to retrieve appropriate PlayerObject who keeps the player.
    public final static Hashtable playersHashed = new Hashtable();

    public PlayerListenerObject(final Object playerObject, final Player player) {
        this._playerListenerCtrl = new PlayerListenerController(player);

        PlayerListenerObject.playersHashed.put(player, playerObject);
    }

    private Object formatSetCallback_Output(final boolean setCallback) {
        if (setCallback == true) {
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }

    /* @Override */
	protected Object execute(Object thiz, Object[] args) throws Exception {
		return formatSetCallback_Output(_playerListenerCtrl.setCallback((args == null ? null : (ScriptableFunction) args[0])));
	}

	/* @Override */
	protected FunctionSignature[] getFunctionSignatures() {
		FunctionSignature playerListenerSig = new FunctionSignature(1);
		playerListenerSig.addNullableParam(ScriptableFunction.class, false);
		return new FunctionSignature[]{playerListenerSig};
	}
	
	

}


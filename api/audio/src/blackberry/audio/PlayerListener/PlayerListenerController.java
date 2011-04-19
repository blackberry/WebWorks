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

import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;

public class PlayerListenerController implements PlayerListener {

    private final UiApplication _uiAppliation = UiApplication.getUiApplication();
    private ScriptableFunction _callback;

    private Player _player = null;
    private boolean _listenerOn = false;

    public PlayerListenerController(final Player player) {
        this._player = player;
    }

    public synchronized void startListening() {
        if (_listenerOn != true) {
            _player.addPlayerListener(this);
            _listenerOn = true;
        }
    }

    public synchronized void stopListening() {
        if (_listenerOn != false) {
            _player.removePlayerListener(this);
            _listenerOn = false;
        }
    }

    public boolean setCallback(final ScriptableFunction callback) {
        if (_player.getState() == Player.CLOSED) {
            return false;
        }

        _callback = callback;

        // Here 'null' is a valid parameter and indicates request to unregister
        // from listener.
        if (callback == null) {
            stopListening();

            return true;
        }
        else {
            startListening();

            return true;
        }
    }

    /* @Override */
    public void playerUpdate(final Player player, final String s, final Object obj) {
        if (_callback != null) {
            _uiAppliation.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Object objToPassWithInvoke = null;
                                    if (obj instanceof Long) {
                                        objToPassWithInvoke = Integer.valueOf(((Long) obj).toString());
                                    }
                                    else if (obj instanceof String) {
                                        objToPassWithInvoke = obj;
                                    }

                                    _callback.invoke(null, new Object[] { PlayerListenerObject.playersHashed.get(player), s, objToPassWithInvoke });
                                } catch (final Exception e) {
                                }
                            }
                        }).start();
                    } catch (final Exception e) {
                    }
                }
            });
        }
    }

}

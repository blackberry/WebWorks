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
package blackberry.audio.Player;

import java.io.InputStream;

import javax.microedition.media.PlayerListener;

import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.script.Scriptable;
import blackberry.audio.AudioExtension;
import blackberry.audio.Player.PlayerController.CloseFunction;
import blackberry.audio.Player.PlayerController.PauseFunction;
import blackberry.audio.Player.PlayerController.PlayFunction;
import blackberry.audio.PlayerListener.PlayerListenerObject;

public class PlayerObject extends Scriptable {

    private PlayerController _playerCtrl = null;
    private PlayerListenerObject _playerListenerObj = null;

    private static final String LOCAL_PROTOCOL = "local:///";

    public PlayerObject(final String locator, final String type, final boolean async) throws Exception {
        if (isLocalURI(locator)) {
            final BrowserFieldRequest bfRequest = new BrowserFieldRequest(locator);
            final InputStream is = AudioExtension.getBrowserFieldController().handleResourceRequest(bfRequest).openInputStream();
            _playerCtrl = new PlayerController(is, type, async);
        }
        else {
            _playerCtrl = new PlayerController(locator, async);
        }

        _playerListenerObj = new PlayerListenerObject(this, _playerCtrl.getPlayer());
    }

    /* @Override */
    public Object getField(final String name) throws Exception {
        if (name.equals(PlayFunction.NAME)) {
            return _playerCtrl.new PlayFunction();
        }
        else if (name.equals(PauseFunction.NAME)) {
            return _playerCtrl.new PauseFunction();
        }
        else if (name.equals(CloseFunction.NAME)) {
            return _playerCtrl.new CloseFunction();
        }
        else if (name.equals(PlayerListenerObject.NAME)) {
            return _playerListenerObj;
        }

        else if (name.equals(PlayerController.FIELD_DURATION)) {
            return formatMediaDuration_Output(_playerCtrl.getMediaDuration());
        }
        else if (name.equals(PlayerController.FIELD_STATE)) {
            return formatPlayerState_Ouptut(_playerCtrl.getPlayerState());
        }
        else if (name.equals(PlayerController.FIELD_MEDIA_TIME)) {
            return formatMediaTime_Output(_playerCtrl.getMediaTime());
        }
        else if (name.equals(PlayerController.FIELD_VOLUME_LEVEL)) {
            return formatPlayerVoluem_Output(_playerCtrl.getPlayerVolume());
        }

        // Player's listener events
        else if (name.equals("EVENT_BUFFERING_STARTED")) {
            return PlayerListener.BUFFERING_STARTED;
        }
        else if (name.equals("EVENT_BUFFERING_STOPPED")) {
            return PlayerListener.BUFFERING_STOPPED;
        }
        else if (name.equals("EVENT_CLOSED")) {
            return PlayerListener.CLOSED;
        }
        else if (name.equals("EVENT_DEVICE_AVAILABLE")) {
            return PlayerListener.DEVICE_AVAILABLE;
        }
        else if (name.equals("EVENT_DEVICE_UNAVAILABLE")) {
            return PlayerListener.DEVICE_UNAVAILABLE;
        }
        else if (name.equals("EVENT_DURATION_UPDATED")) {
            return PlayerListener.DURATION_UPDATED;
        }
        else if (name.equals("EVENT_END_OF_MEDIA")) {
            return PlayerListener.END_OF_MEDIA;
        }
        else if (name.equals("EVENT_ERROR")) {
            return PlayerListener.ERROR;
        }
        else if (name.equals("EVENT_RECORD_ERROR")) {
            return PlayerListener.RECORD_ERROR;
        }
        else if (name.equals("EVENT_RECORD_STARTED")) {
            return PlayerListener.RECORD_STARTED;
        }
        else if (name.equals("EVENT_RECORD_STOPPED")) {
            return PlayerListener.RECORD_STOPPED;
        }
        else if (name.equals("EVENT_SIZE_CHANGED")) {
            return PlayerListener.SIZE_CHANGED;
        }
        else if (name.equals("EVENT_STARTED")) {
            return PlayerListener.STARTED;
        }
        else if (name.equals("EVENT_STOPPED")) {
            return PlayerListener.STOPPED;
        }
        else if (name.equals("EVENT_STOPPED_AT_TIME")) {
            return PlayerListener.STOPPED_AT_TIME;
        }
        else if (name.equals("EVENT_VOLUME_CHANGED")) {
            return PlayerListener.VOLUME_CHANGED;
        }

        return super.getField(name);
    }

    /* @Override */
    public boolean putField(final String name, final Object value) throws Exception {
        if (name.equals(PlayerController.FIELD_MEDIA_TIME)) {
            return _playerCtrl.setMediaTime(formatMediaTime_Input(value));
        }
        if (name.equals(PlayerController.FIELD_VOLUME_LEVEL)) {
            return _playerCtrl.setPlayerVolume(value);
        }

        return super.putField(name, value);
    }

    private static boolean isLocalURI(final String path) {
        return path != null && path.startsWith(LOCAL_PROTOCOL);
    }

    // ---------------FORMATING INPUT/OUTPUT--------------------------
    // ---------------------------------------------------------------

    private int convertMicroToMilli(final long microseconds) {
        return (int) (microseconds / 1000);
    }

    private long convertMilliToMicro(final int milliseconds) {
        return milliseconds * 1000;
    }

    private Object formatMediaDuration_Output(final long mediaDuration) {
        return new Integer(convertMicroToMilli(mediaDuration));
    }

    private Object formatMediaTime_Output(final long mediaTime) {
        return new Integer(convertMicroToMilli(mediaTime));
    }

    private long formatMediaTime_Input(final Object value) {
        return convertMilliToMicro(((Integer) value).intValue());
    }

    private Object formatPlayerVoluem_Output(final int playerVolume) {
        return new Integer(playerVolume);
    }

    private Object formatPlayerState_Ouptut(final int playerState) {
        return new Integer(playerState);
    }
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------

}

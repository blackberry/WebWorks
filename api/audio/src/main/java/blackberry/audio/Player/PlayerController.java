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

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.script.ScriptableFunction;
import blackberry.core.ApplicationEventHandler;
import blackberry.core.EventService;

public class PlayerController implements ApplicationEventHandler {

    private Player _player = null;
    private VolumeControl _volumeControl = null;
    private static final String RSTP_PROTOCOL = "rtsp";

    static final String FIELD_DURATION = "duration";
    static final String FIELD_STATE = "state";
    static final String FIELD_MEDIA_TIME = "mediaTime";
    static final String FIELD_VOLUME_LEVEL = "volumeLevel";

    static final long PLAYER_TIME_UNKNOWN = Player.TIME_UNKNOWN;
    static final long PLAYER_CLOSED = Player.CLOSED;
    static final long PLAYER_UNREALIZED = Player.UNREALIZED;
    static final long PLAYER_REALIZED = Player.REALIZED;
    static final long PLAYER_PREFETCHED = Player.PREFETCHED;
    static final long PLAYER_STARTED = Player.STARTED;

    public PlayerController( final String locator, final boolean async ) throws Exception {
        if( locator.trim().toLowerCase().startsWith( RSTP_PROTOCOL ) ) {
            Browser.getDefaultSession().displayPage( locator );
        } else {
            _player = Manager.createPlayer( locator );
            movePlayerToPrefetchedState( async );
        }
        EventService.getInstance().addHandler( ApplicationEventHandler.EVT_APP_EXIT, this );
    }

    public PlayerController( final InputStream is, final String type, final boolean async ) throws Exception {
        _player = Manager.createPlayer( is, type );
        movePlayerToPrefetchedState( async );
    }

    private void movePlayerToPrefetchedState( final boolean async ) {
        if( async ) {
            try {
                new Thread( new Runnable() {
                    public void run() {
                        prefetchPlayer();
                    }
                } ).start();
            } catch( final Exception e ) {
            }
        } else {
            prefetchPlayer();
        }
    }

    private void prefetchPlayer() {
        try {
            _player.realize();
        } catch( final MediaException e ) {
        }

        _volumeControl = (VolumeControl) _player.getControl( "VolumeControl" );

        try {
            _player.prefetch();
        } catch( final MediaException e ) {
        }
    }

    public class CloseFunction extends ScriptableFunction {
        public static final String NAME = "close";

        /* @Override */
        public Object invoke( final Object thiz, final Object[] args ) throws Exception {
            return closePlayer();
        }
    }

    private Boolean closePlayer() {
        if( _player.getState() != PlayerController.PLAYER_CLOSED ) {
            _player.close();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public class PlayFunction extends ScriptableFunction {
        public static final String NAME = "play";

        /* @Override */
        public Object invoke( final Object thiz, final Object[] args ) throws Exception {
            if( _player.getState() != PlayerController.PLAYER_CLOSED ) {
                _player.start();

                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
    }

    public class PauseFunction extends ScriptableFunction {
        public static final String NAME = "pause";

        /* @Override */
        public Object invoke( final Object thiz, final Object[] args ) throws Exception {
            if( _player.getState() != PlayerController.PLAYER_CLOSED ) {
                _player.stop();

                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
    }

    long getMediaDuration() {
        if( _player.getState() != PlayerController.PLAYER_CLOSED ) {
            return _player.getDuration();
        }

        return PlayerController.PLAYER_TIME_UNKNOWN;
    }

    int getPlayerState() {
        return _player.getState();
    }

    int getPlayerVolume() {
        return _volumeControl.getLevel();
    }

    boolean setPlayerVolume( final Object value ) throws Exception {
        _volumeControl.setLevel( ( (Integer) value ).intValue() );

        return true;
    }

    long getMediaTime() {
        if( _player.getState() != PlayerController.PLAYER_CLOSED ) {
            return _player.getMediaTime();
        }

        return PlayerController.PLAYER_TIME_UNKNOWN;
    }

    boolean setMediaTime( final long mediaTime ) throws Exception {
        if( ( _player.getState() != PlayerController.PLAYER_UNREALIZED )
                && ( _player.getState() != PlayerController.PLAYER_CLOSED ) ) {
            _player.setMediaTime( mediaTime );

            return true;
        }

        return false;
    }

    public Player getPlayer() {
        return _player;
    }

    public boolean handlePreEvent( int eventID, Object[] args ) {
        // DO NOTHING
        return false;
    }

    public void handleEvent( int eventID, Object[] args ) {
        // Check for the close event(could register for more in future)
        if( eventID == ApplicationEventHandler.EVT_APP_EXIT ) {
            // Close the player before exiting
            closePlayer();
        }
    }
}

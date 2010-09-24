/*
* Copyright 2010 Research In Motion Limited.
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

import net.rim.device.api.script.ScriptableFunction;
import common.util.ArgumentValidationUtil;

public class PlayerConstructor extends ScriptableFunction {

    public static final String NAME = "blackberry.audio.Player";

    private static PlayerConstructor _instance = null;

    public static synchronized PlayerConstructor getInstance() {
        if (_instance == null) {
            _instance = new PlayerConstructor();
        }

        return _instance;
    }

    private PlayerConstructor() {
    }

    /* @Override */
    public Object getField(final String name) throws Exception {
        // Player's states.
        if (name.equals("CLOSED")) {
            return new Integer((int) PlayerController.PLAYER_CLOSED);
        }
        else if (name.equals("PREFETCHED")) {
            return new Integer((int) PlayerController.PLAYER_PREFETCHED);
        }
        else if (name.equals("REALIZED")) {
            return new Integer((int) PlayerController.PLAYER_REALIZED);
        }
        else if (name.equals("STARTED")) {
            return new Integer((int) PlayerController.PLAYER_STARTED);
        }
        else if (name.equals("TIME_UNKNOWN")) {
            return new Integer((int) PlayerController.PLAYER_TIME_UNKNOWN);
        }
        else if (name.equals("UNREALIZED")) {
            return new Integer((int) PlayerController.PLAYER_UNREALIZED);
        }

        return super.getField(name);
    }

    /* @Override */
    public Object construct(final Object thiz, final Object[] args) throws Exception {
        final int[] validParamNumber = { 1, 2, 3 };

        ArgumentValidationUtil.validateParameterNumber(validParamNumber, args);

        // Create PlayerObject by passing parameters:
        // The boolean parameter specifies whether the player should
        // asynchronously advance to PREFETCHED state. If not provided set to
        // false.
        // (Prefetching a Player can be a resource and time consuming process).

        if (args != null && args.length > 0) {
            final String fullPathToMedia = (String) args[0];
            String mediaType = null;
            boolean asyncMode = false;

            // Creating Player by providing full path only.
            if (args.length == 1) {
                return new PlayerObject(fullPathToMedia, mediaType, asyncMode);
            }
            else {

                if (args.length == 2) {
                    // Creating Player while using 'local' as a scheme (i.e.
                    // local:///res/filename.mid).
                    if (args[1] instanceof String) {
                        mediaType = (String) args[1];
                        return new PlayerObject(fullPathToMedia, mediaType, asyncMode);
                    }
                    else {
                        asyncMode = ((Boolean) args[1]).booleanValue();
                        return new PlayerObject(fullPathToMedia, mediaType, asyncMode);
                    }
                }
                else if (args.length == 3) {
                    mediaType = (String) args[1];
                    asyncMode = ((Boolean) args[2]).booleanValue();
                    return new PlayerObject(fullPathToMedia, mediaType, asyncMode);
                }
            }
        }

        return UNDEFINED;
    }

}

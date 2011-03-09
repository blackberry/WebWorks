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
package blackberry.phone.calllog;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;

public class ConcretePhoneLogListener extends ScriptableFunction implements PhoneLogListener {

    public static final String NAME = "addPhoneLogListener";

    private final UiApplication _uiApplication = UiApplication.getUiApplication();

    private ScriptableFunction[] _callbackArray = null;
    private boolean _listenerOn = false;

    private static final int CB_CALL_LOG_ADDED_INDEX = 0;
    private static final int CB_CALL_LOG_REMOVED_INDEX = 1;
    private static final int CB_CALL_LOG_UPDATED_INDEX = 2;
    private static final int CB_CALL_LOG_RESET_INDEX = 3;
    private static final int CALLBACK_SIZE = 4;

    private static ConcretePhoneLogListener _instance;

    public static synchronized ConcretePhoneLogListener getInstance() {
        if (_instance == null) {
            _instance = new ConcretePhoneLogListener();
        }

        return _instance;
    }

    private ConcretePhoneLogListener() {
    }

    /* override */
    public Object invoke(final Object thiz, final Object[] args) throws Exception {
        if (args == null || args.length == 1 && args[0] == null) {
            stopListening();

            return new Boolean(true);
        }
        else if (args.length == CALLBACK_SIZE) {
            if (_callbackArray == null) {
                _callbackArray = new ScriptableFunction[CALLBACK_SIZE];
            }

            for (int i = 0; i < CALLBACK_SIZE; i++) {
                _callbackArray[i] = (ScriptableFunction) args[i];
            }

            startListening();

            return new Boolean(true);
        }

        return UNDEFINED;
    }

    private synchronized void startListening() {
        if (_listenerOn != true) {
            PhoneLogs.addListener(this);
            _listenerOn = true;
        }
    }

    private synchronized void stopListening() {
        if (_listenerOn != false) {
            PhoneLogs.removeListener(this);
            _listenerOn = false;
        }

        if (_callbackArray != null) {
            for (int i = 0; i < CALLBACK_SIZE; i++) {
                _callbackArray[i] = null;
            }
        }
    }

    /* @Override */
    public void callLogAdded(final CallLog callLog) {
        if (_callbackArray != null && _callbackArray[CB_CALL_LOG_ADDED_INDEX] != null) {
            final CallLogObject callLogObject = new CallLogObject(callLog);
            _uiApplication.invokeLater(new Runnable() { 
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    _callbackArray[CB_CALL_LOG_ADDED_INDEX].invoke(null, new Object[] { callLogObject });
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

    /* @Override */
    public void callLogRemoved(final CallLog callLog) {
        if (_callbackArray != null && _callbackArray[CB_CALL_LOG_REMOVED_INDEX] != null) {
            final CallLogObject callLogObject = new CallLogObject(callLog);
            _uiApplication.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    _callbackArray[CB_CALL_LOG_REMOVED_INDEX].invoke(null, new Object[] { callLogObject });
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

    /* @Override */
    public void callLogUpdated(final CallLog newCallLog, final CallLog oldCallLog) {
        if (_callbackArray != null && _callbackArray[CB_CALL_LOG_UPDATED_INDEX] != null) {
            final CallLogObject newCallLogObject = new CallLogObject(newCallLog);
            final CallLogObject oldCallLogObject = new CallLogObject(oldCallLog);
            _uiApplication.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    _callbackArray[CB_CALL_LOG_UPDATED_INDEX].invoke(null, new Object[] { newCallLogObject, oldCallLogObject });
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

    /* @Override */
    public void reset() {
        if (_callbackArray != null && _callbackArray[CB_CALL_LOG_RESET_INDEX] != null) {
            _uiApplication.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    _callbackArray[CB_CALL_LOG_RESET_INDEX].invoke(null, null);
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

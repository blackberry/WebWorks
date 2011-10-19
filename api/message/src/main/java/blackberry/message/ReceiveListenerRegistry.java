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
package blackberry.message;

import java.util.Vector;

import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.UiApplication;

/**
 * Message receive listener registry.
 * 
 * @author dmeng
 * 
 */
public abstract class ReceiveListenerRegistry {
    public static final String ADDER = "addReceiveListener";
    public static final String REMOVER = "removeReceiveListener";

    protected Vector messages;
    protected ScriptableFunction listener;
    protected UiApplication uiApplication;

    /**
     * Constructor.
     */
    public ReceiveListenerRegistry() {
        messages = new Vector();
        uiApplication = UiApplication.getUiApplication();
    }

    /**
     * Returns if the listener has been set.
     * 
     * @return <code>true</code> if yes; <code>false</code> otherwise
     */
    public boolean listenerIsSet() {
        return listener != null;
    }

    /**
     * Adds a message listener.
     * 
     * @return The <code>ScripableFunction</code> to be executed.
     */
    public ScriptableFunction add() {
        return new ScriptableFunction() {
            public Object invoke( Object self, Object[] args ) {
                if( args != null && args[ 0 ] instanceof ScriptableFunction ) {
                    listener = (ScriptableFunction) args[ 0 ];
                }
                return UNDEFINED;
            }
        };
    }

    /**
     * Removes a message listener.
     * 
     * @return The <code>ScripableFunction</code> to be executed.
     */
    public ScriptableFunction remove() {
        return new ScriptableFunction() {
            public Object invoke( Object self, Object[] args ) {
                if( listener == null ) {
                    return Boolean.FALSE;
                }
                listener = null;
                return Boolean.TRUE;
            }
        };
    }

    /**
     * Adds the given message to the queue.
     * 
     * @param msg
     *            The message to be added
     */
    public void queueIncomingMessage( Object msg ) {
        if( !isTheRightTypeOfMessage( msg ) ) {
            return;
        }
        synchronized( messages ) {
            messages.addElement( msg );
        }
        new ListenerLauncher().start();
    }

    /**
     * Notifies listeners for message arrival.
     */
    public abstract void fireAll();

    /**
     * Returns if the given message has the right message type.
     * 
     * @param msg
     *            The message object
     * @return <code>true</code> if yes; <code>false</code> otherwise
     */
    protected abstract boolean isTheRightTypeOfMessage( Object msg );

    /**
     * A thread to notify the listeners.
     */
    private class ListenerLauncher extends Thread {
        public void run() {
            synchronized( messages ) {
                fireAll();
            }
        }
    }

}

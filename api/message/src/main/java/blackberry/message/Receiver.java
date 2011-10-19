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

import java.io.IOException;

import javax.microedition.io.Connection;

/**
 * Message receiver thread.
 * 
 * @author dmeng
 *
 */
public abstract class Receiver extends Thread {
    protected Connection _connection;
    protected ReceiveListenerRegistry listenerRegistry;
    private boolean running = false;
    public abstract String getProtocol();

    /**
     * Starts or pauses the receiver.
     * 
     * @param letRun <code>true</code> to run the start the receiver; <code>false</code> to pause it.
     */
    public void signal(boolean letRun) {
        if (letRun && !isRunning()) {
            start();
        }
        else if (!letRun && isRunning()) {
            pause();
        }
    }
    
    /**
     * @see java.laung.Thread#run()
     */
    public void run() {
        try {
            listenForMessages();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (_connection != null) {
                try {
                    _connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            running = true;
        }
    }

    /**
     * Listens for messages.
     * 
     * @throws Exception exception occur during the listening
     */
    protected abstract void listenForMessages() throws Exception;

    /**
     * Adds a message listener.
     * 
     * @return The <code>ScriptableFunction</code> to be executed.
     */
    public Object addReceiveListener() {
        return listenerRegistry.add();
    }

    /**
     * Removes a message listener.
     * 
     * @return The <code>ScriptableFunction</code> to be executed.
     */
    public Object removeReceiveListener() {
        return listenerRegistry.remove();
    }

    /**
     * Pauses the message receiver.
     */
    public void pause() {
        running = false;
    }

    /**
     * Starts the message receiver.
     */
    public void start() {
        running = true;
        if (!isAlive()) {
            super.start();
        }
    }

    /**
     * Returns if the message receiver is running.
     * 
     * @return <code>true</code> if yes; <code>false</code> otherwise
     */
    public boolean isRunning() {
        return isAlive() && running;
    }
}

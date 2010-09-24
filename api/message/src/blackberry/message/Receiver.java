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
package blackberry.message;

import java.io.IOException;

import javax.microedition.io.Connection;

public abstract class Receiver extends Thread {
    protected Connection _connection;

    protected ReceiveListenerRegistry listenerRegistry;

    private boolean running = false;

    public abstract String getProtocol();

    public void signal(boolean letRun) {
        if (letRun && !isRunning()) {
            start();
        }
        else if (!letRun && isRunning()) {
            pause();
        }
    }
    
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

    protected abstract void listenForMessages() throws Exception;

    public Object addReceiveListener() throws Exception {
        return listenerRegistry.add();
    }

    public Object removeReceiveListener() {
        return listenerRegistry.remove();
    }

    public void pause() {
        running = false;
    }

    public void start() {
        running = true;
        if (!isAlive()) {
            super.start();
        }
    }

    public boolean isRunning() {
        return isAlive() && running;
    }
}

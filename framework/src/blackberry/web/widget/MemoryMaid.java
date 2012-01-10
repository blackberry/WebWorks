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
package blackberry.web.widget;

import blackberry.web.widget.device.DeviceInfo;
import net.rim.device.api.system.Memory;

/**
 * Helps force garbage collection in case the application runs low on memory
 */
public class MemoryMaid extends Thread {
    protected final static int LOWMEM_THRESHHOLD = 1024 * 1024 * 5; // 5Mb
    protected final static float DEVIATION_THRESHHOLD = 0.10f; // 10%
    protected final static long GC_TIMEOUT = 25000; // Wait time after a gc. To avoid calling gc too much
    protected final static long SAMPLE_RATE = 5000; // How often to check memory

    protected static MemoryMaid _instance = null;

    private boolean _running;
    private boolean _doGC;
    private int _lastSampleAfterGC;

    private MemoryMaid() {

    }

    /**
     * @return A singleton instance of the MemoryMaid class
     */
    public static MemoryMaid getInstance() {
        if( DeviceInfo.isCompatibleVersion( 6 ) ) {
            if( _instance == null ) {
                _instance = new MemoryMaid();
            }
            return _instance;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
        _running = true;

        _lastSampleAfterGC = Memory.getRAMStats().getFree();

        // Avoid handling low memory if the device is low before we start.
        if( _lastSampleAfterGC < LOWMEM_THRESHHOLD ) {
            _running = false;
        }

        while( _running ) {
            // Don't waste time if we haven't done anything that may have triggered the memory drop
            if( _doGC ) {
                _doGC = false;
                // Clean memory if only LOWMEM_THRESHHOLD is left or if free memory changes by a DEVIATION_THRESHHOLD percentage
                int currentSample = Memory.getRAMStats().getFree();
                if( currentSample < LOWMEM_THRESHHOLD || currentSample / _lastSampleAfterGC <= 1 - DEVIATION_THRESHHOLD ) {
                    System.gc();
                    _lastSampleAfterGC = Memory.getRAMStats().getFree();
                    if( _running ) {
                        try {
                            Thread.sleep( GC_TIMEOUT );
                        } catch( InterruptedException e ) {
                            _running = false;
                        }
                    }
                }
            }

            if( _running ) {
                try {
                    Thread.sleep( SAMPLE_RATE );
                } catch( InterruptedException e ) {
                    _running = false;
                }
            }
        }
        _running = false;
    }

    /**
     * Stop the thread
     */
    public void stop() {
        _lastSampleAfterGC = -1;
        _running = false;
        this.interrupt();
    }

    /**
     * The memory maid will only do a GC if this flag is set and the appropriate wait time has passed
     */
    public void flagGC() {
        _doGC = true;
    }

	
}

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
package net.rim.tumbler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Helper class to execute process
 *
 */
public class ExecUtil {
    private static Process execProcess( String workingDir, Object cmd, String[] env ) {
        Process proc = null;
        try {
            File dir = workingDir != null ? new File( workingDir ) : null;
            if( cmd instanceof String ) {
                proc = Runtime.getRuntime().exec( (String) cmd, env, dir );
            } else if( cmd instanceof String[] ) {
                proc = Runtime.getRuntime().exec( (String[]) cmd, env, dir );
            }
        } catch( IOException ex ) {
            System.err.println( ex );
        }

        if( proc != null ) {
            StreamRedirector isr = new StreamRedirector( proc.getInputStream(), System.out );
            StreamRedirector esr = new StreamRedirector( proc.getErrorStream(), System.out );

            isr.start();
            esr.start();

            try {
                proc.waitFor();
            } catch( InterruptedException e ) {
                System.out.println( "process interrupted" );
            }

            try {
                isr.join();
                esr.join();
            } catch( InterruptedException e ) {
                System.out.println( "stream redirectors interrupted" );
            }
        }

        return proc;
    }

    /**
     * Execute process using a command string
     * @param workingDir working directory, can be null
     * @param cmdStr command string to execute
     * @param env environment
     * @return Process object created
     */
    public static Process exec( String workingDir, String cmdStr, String[] env ) {
        return execProcess( workingDir, cmdStr, env );
    }

    /**
     * Execute process using a command string array
     * @param workingDir working directory, can be null
     * @param cmd command string array
     * @param env environment
     * @return Process object created
     */
    public static Process exec( String workingDir, String[] cmd, String[] env ) {
        return execProcess( workingDir, cmd, env );
    }
}

class StreamRedirector extends Thread {
    // OutputStream os; // Unused field
    InputStream is;

    public StreamRedirector( InputStream is, OutputStream os ) {
        this.is = is;
        // this.os = os;
    }

    public void run() {

        try {
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

            String line;

            while( ( line = br.readLine() ) != null ) {
                System.out.println( line );
            }

        } catch( IOException e ) {
            System.out.println( "error redirecting stream" );
        }
    }
}
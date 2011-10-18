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
package blackberry.media.microphone;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.MediaException;

import blackberry.core.threading.CallbackDispatcherEvent;

import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.script.ScriptableFunction;

/**
 * Class to help record audio clips
 */
class Record {

    // Type of file being recorded
    private static final int TYPE_NONE = -1;
    private static final int TYPE_WAV = 0;
    private static final int TYPE_AMR = 1;

    // record() arguments and callbacks
    private String _path = null;
    private ScriptableFunction _completeCallback = null;
    private ScriptableFunction _errorCallback = null;

    // Record members
    private Player _player = null;
    private RecordControl _rcontrol = null;
    private OutputStream _output = null;

    // state
    private boolean _paused = false;

    private int _type = -1;

    private static byte[] getLittleInt( int bigInt ) {
        return new byte[] { (byte) ( bigInt & 0xFF ), (byte) ( ( bigInt >> 8 ) & 0xFF ), (byte) ( ( bigInt >> 16 ) & 0xFF ),
                (byte) ( ( bigInt >> 24 ) & 0xFF ) };
    }

    /**
     * Write a WAV Header. See https://ccrma.stanford.edu/courses/422/projects/WaveFormat/ for more info BlackBerry PCM recording
     * is 16bit 8000hz mono
     */
    private void writeWavHeader() throws IOException {
        // Reopen the output file
        FileConnection conn = (FileConnection) Connector.open( _path, Connector.READ_WRITE );
        int size = (int) conn.fileSize();

        // use output stream as it is shown in the sample
        OutputStream o = conn.openOutputStream();

        //*********************************************************************************************************************
        //*****************************************************************************    NAME            Endedness       Size
        //*********************************************************************************************************************
        // RIFF Chunk Descriptor
        o.write( new byte[] { (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46 } );   // ChunkID          big             4
        o.write( getLittleInt( size - 8 ) );                                            // ChunkSize        little          4   // 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size) = size - 8
        o.write( new byte[] { (byte) 0x57, (byte) 0x41, (byte) 0x56, (byte) 0x45 } );   // Format           big             4

        //  Format sub-chunk
        o.write( new byte[] { (byte) 0x66, (byte) 0x6d, (byte) 0x74, (byte) 0x20 } );   // Subchunck1      big              4
        o.write( new byte[] { (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00 } );   // Subchunck1Size  little           4   // 16 for PCM
        o.write( new byte[] { (byte) 0x01, (byte) 0x00 } );                             // AudioFormat     little           2   // 1 = PCM
        o.write( new byte[] { (byte) 0x01, (byte) 0x00 } );                             // NumChannels     little           2   // 1 Channel
        o.write( new byte[] { (byte) 0x40, (byte) 0x1F, (byte) 0x00, (byte) 0x00 } );   // SampleRate      little           4   // 8000hz
        o.write( new byte[] { (byte) 0x80, (byte) 0x3E, (byte) 0x00, (byte) 0x00 } );   // ByteRate        little           4   // SampleRate * NumChannels * BitsPerSample/8 = 8000 * 1 * 16 / 8 = 16000
        o.write( new byte[] { (byte) 0x02, (byte) 0x00 } );                             // BlockAlign      little           2   // NumChannels * BitsPerSample/8 = 1 * 16 / 8 = 2
        o.write( new byte[] { (byte) 0x10, (byte) 0x00 } );                             // BitsPerSample   little           2   // 16 bits per sample

        // Data sub-chunk
        o.write( new byte[] { (byte) 0x64, (byte) 0x61, (byte) 0x74, (byte) 0x61 } );   // Subchunk2ID     big              4
        o.write( getLittleInt( size - 44 ) );                                           // Subchunk2Size   little           4   // size - headerSize = size - 44
                                                                                        // Data payload *                   *
        o.close();

        //*********************************************************************************************************************
        //*********************************************************************************************************************
	}

    /**
     * Fetch a string array of mime types that record supports
     * @return array of mime types
     */
    public String[] getSupportedMediaTypes() {
        Vector supportedTypes = new Vector();
        String[] deviceSupportedTypes = Manager.getSupportedContentTypes( "capture" );
        boolean containsPcm = false;
        boolean containsAmr = false;
        for( int i = 0; i < deviceSupportedTypes.length; i++ ) {
            String type = deviceSupportedTypes[ i ];
            if( !containsPcm && ( type.equals( "audio/pcm" ) || type.equals( "audio/basic" ) ) ) {
                containsPcm = true;
            } else if( !containsAmr && type.equals( "audio/amr" ) ) {
                containsAmr = true;
            }
        }

        if( containsPcm ) {
            supportedTypes.addElement( "audio/wav" );
            supportedTypes.addElement( "audio/wave" );
            supportedTypes.addElement( "audio/x-wav" );
        }

        if( containsAmr ) {
            supportedTypes.addElement( "audio/amr" );
        }

        String[] supportedTypesArray = new String[ supportedTypes.size() ];
        supportedTypes.copyInto( supportedTypesArray );
        return supportedTypesArray;
    }

    /**
     * Start recording an audio clip at the given path
     */
    public void record( String path, ScriptableFunction completeCallback, ScriptableFunction errorCallback ) {

        if( _player != null || _rcontrol != null || _output != null ) {
            MicrophoneNamespace.handleError( _errorCallback, new IllegalStateException( "Recorder is already running" ) );
            return;
        }

        _path = path.trim();
        _completeCallback = completeCallback;
        _errorCallback = errorCallback;

        String ext = _path.substring( _path.lastIndexOf( '.' ) + 1 ).toLowerCase();
        if( ext.equals( "wav" ) ) {
            _type = TYPE_WAV;
        } else if( ext.equals( "amr" ) ) {
            _type = TYPE_AMR;
        } else {
            _type = TYPE_NONE;
            MicrophoneNamespace.handleError( _errorCallback, new MediaException( "Unsupported record encoding type" ) );
            return;
        }

        try {
            // Create a Player that captures live audio.
            switch( _type ) {
                case TYPE_WAV:
                    // Note: CDMA devices do not support PCM recording. The
                    // exception will get passed to the error callback.
                    // Could maybe expose
                    // Manager.getSupportedContentTypes("capture") in the future.
                    _player = Manager.createPlayer( "capture://audio?encoding=pcm" );
                    break;
                case TYPE_AMR:
                    _player = Manager.createPlayer( "capture://audio?encoding=amr" );
                    break;
                default:
                    // should not happen
            }

            _player.realize();

            _rcontrol = (RecordControl) _player.getControl( "RecordControl" );

            // Create the output file
            FileConnection conn = (FileConnection) Connector.open( _path, Connector.READ_WRITE );
            if( conn.exists() ) {
                MicrophoneNamespace
                        .handleError( _errorCallback, new IllegalArgumentException( "'" + _path + "' already exists" ) );
                stop();
                return;
            }

            conn.create();

            // use output stream as it is shown in the sample
            _output = conn.openOutputStream();

            // Leave 44 bytes for the WAV header
            if( _type == TYPE_WAV ) {
                for( int i = 0; i < 44; i++ ) {
                    _output.write( 0 );
                }
            }
            _rcontrol.setRecordStream( _output );
            _rcontrol.startRecord();
            _player.start();
            _paused = false;

        } catch( final Exception e ) {
            stop();
            MicrophoneNamespace.handleError( _errorCallback, e );
        }

    }

    /**
     * Pause or resume the audio recording
     */
    public void pause() {
        if( _rcontrol != null ) {
            if( _paused ) {
                _rcontrol.startRecord();
                _paused = false;
            } else {
                _rcontrol.stopRecord();
                _paused = true;
            }
        }
    }

    /**
     * Stop the audio recording and trigger the callback
     */
    public void stop() {

        // Stop recording, close the OutputStream and player.
        if( _rcontrol != null ) {
            try {
                _rcontrol.commit();
            } catch( IOException e ) {
                MicrophoneNamespace.handleError( _errorCallback, e );
            }
            _rcontrol = null;
        }

        if( _output != null ) {
            try {
                _output.close();

                if( _path.length() > 0 ) {

                    try {
                        if( _type == TYPE_WAV ) {
                            writeWavHeader();
                        }

                        FileConnection conn = (FileConnection) Connector.open( _path, Connector.READ );
                        if( conn.exists() ) {
                            // invoke callback for the file
                            new CallbackDispatcherEvent( _completeCallback, new Object[] { _path } ).Dispatch();
                        } else {
                            MicrophoneNamespace.handleError( _errorCallback, new FileNotFoundException( _path ) );
                        }
                    } catch( Exception e ) {
                        MicrophoneNamespace.handleError( _errorCallback, e );
                    }
                }
            } catch( IOException e ) {
                MicrophoneNamespace.handleError( _errorCallback, e );
            }
            _output = null;
        }

        if( _player != null ) {
            _player.close();
            _player = null;
        }

        _type = TYPE_NONE;
        _path = null;
    }
}
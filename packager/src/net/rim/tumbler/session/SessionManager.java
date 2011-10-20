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
package net.rim.tumbler.session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Random;
import java.util.zip.ZipFile;

import net.rim.tumbler.OSUtils;
import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.SessionException;
import net.rim.tumbler.exception.ValidationException;

public class SessionManager {
    private static SessionManager _instance = null;

    // Environment properties
    public static String BBWP_JAR_PATH;

    private String _bbwpJarFolder;
    private String _sessionHome;
    private String _tld;

    // BlackBerry WebWorks Application info
    private String _widgetArchive;
    private String _archiveName;

    // Command line settings
    private boolean _requireSigning;
    private String _password;
    private String _outputFolder;
    private boolean _requireSource;
    private String _sourceFolder;
    private String _safeSourceFolder;
    private boolean _debugMode;
    private boolean _isVerbose;

    static {
        try {
            BBWP_JAR_PATH = URLDecoder.decode(
                    SessionManager.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8" );
        } catch( UnsupportedEncodingException e ) {
            throw new SessionException( e, "Unexpected error decoding BBWP JAR path." );
        }
    }

    public static void createInstance( String archiveName, String widgetArchive, String bbwpInstallFolder, String outputFolder,
            boolean requireSigning, String password, boolean requireSource, String sourceFolder, boolean debugMode,
            boolean isVerbose ) throws Exception {
        _instance = new SessionManager( archiveName, widgetArchive, bbwpInstallFolder, outputFolder, requireSigning, password,
                requireSource, sourceFolder, debugMode, isVerbose );
    }

    public static SessionManager getInstance() {
        return _instance;
    }

    private SessionManager( String archiveName, String widgetArchive, String bbwpInstallFolder, String outputFolder,
            boolean requireSigning, String password, boolean requireSource, String sourceFolder, boolean debugMode,
            boolean isVerbose ) throws Exception {

        _widgetArchive = widgetArchive;
        _archiveName = archiveName;
        _requireSigning = requireSigning;
        _password = password;
        _outputFolder = outputFolder;
        _requireSource = requireSource;
        _sourceFolder = sourceFolder;

        if( !isOriginalSourceFolderSafe() ) {
            _safeSourceFolder = System.getProperty( "java.io.tmpdir" ) + "widgetGen." + new Random().nextInt( 2147483647 )
                    + new Date().getTime() + ".tmp";
        }

        _debugMode = debugMode;
        _isVerbose = isVerbose;
        _bbwpJarFolder = bbwpInstallFolder;

        // Determine home directory
        _sessionHome = determineSessionHome();

        // Validate session - check signing keys
        if( _requireSigning ) {
            checkSignatureKeys();
        }

        // Validate application archive
        validateArchive( _widgetArchive );

        // Load top level domain info
        BufferedReader input = null;
        try {
            input = new BufferedReader( new FileReader( new File( _bbwpJarFolder + "tld.txt" ) ) );
            String line = null; // Not declared within while loop
            StringBuffer sb = new StringBuffer( "$$" );
            while( ( line = input.readLine() ) != null ) {
                sb.append( line.toLowerCase().trim() );
                sb.append( "$$" );
            }
            _tld = sb.toString();
        } finally {
            input.close();
        }
    }

    private void validateArchive( String archive ) throws PackageException {

        File f = new File( archive );
        ZipFile zipFile;

        // Check for file's existence
        if( !f.exists() ) {
            throw new PackageException( "EXCEPTION_WIDGET_ARCHIVE_NOT_FOUND" );
        } else {
            try {
                zipFile = new ZipFile( f );
                zipFile.close();
            } catch( Exception e ) {
                throw new PackageException( "EXCEPTION_ARCHIVE_IO" );
            }
        }
    }

    private String determineSessionHome() {
        String home = "";

        try {
            home = new File( getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getCanonicalPath();
        } catch( Exception e ) {
            home = "";
        }

        if( home.equals( "" ) ) {
            return System.getProperty( "user.dir" );
        } else {
            int idx = home.lastIndexOf( File.separator + "bin" );
            return ( ( idx < 0 ) ? "" : home.substring( 0, idx ) );
        }
    }

    private void checkSignatureKeys() throws Exception {
        String keyPath = _bbwpJarFolder + WidgetPackager.SIGNATURE_KEY_FILE;

        // Check for file's existence
        if( !( new File( keyPath ) ).exists() ) {
            throw new ValidationException( "EXCEPTION_MISSING_SIGNING_KEYS" );
        }
    }

    public String getBBWPJarFolder() {
        return _bbwpJarFolder;
    }

    public String getWidgetArchive() {
        return _widgetArchive;
    }

    public String getArchiveName() {
        return _archiveName;
    }

    public boolean requireSigning() {
        return _requireSigning;
    }

    public String getPassword() {
        return _password;
    }

    public String getOutputFolder() {
        return _outputFolder;
    }

    public boolean requireSource() {
        return _requireSource;
    }

    public String getSourceFolder() {
        if( !isOriginalSourceFolderSafe() ) {
            return _safeSourceFolder;
        } else {
            return _sourceFolder;
        }
    }

    public String getOriginalSourceFolder() {
        return _sourceFolder;
    }

    public String getSafeSourceFolder() {
        return _safeSourceFolder;
    }

    public boolean debugMode() {
        return _debugMode;
    }

    public boolean isVerbose() {
        return _isVerbose;
    }

    public String getSessionHome() {
        return _sessionHome;
    }

    public String getTLD() {
        return _tld;
    }

    /**
     * @return true if source folder is safe to use, it is safe only if
     * (1) OS is Windows, or (2) source folder does not contain spaces
     */
    public boolean isOriginalSourceFolderSafe() {
        return !( !OSUtils.isWindows() && _sourceFolder.contains( " " ) );
    }
}

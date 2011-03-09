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

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import net.rim.tumbler.exception.CommandLineException;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.session.SessionManager;

public class CmdLineHandler {
    private static final String         FILE_SEP = System.getProperty("file.separator");
    private static final String         OPTION_SOURCEDIR = "/s";
    private static final String         OPTION_PASSWORD = "/g";    
    private static final String         OPTION_OUTPUTDIR = "/o";
    private static final String         OPTION_VERBOSE = "/v";
    private static final String         OPTION_HELP = "/h";
    private static final String         OPTION_DEBUG = "/d";
    
    private boolean         _requireSigned;
    private String          _password;
    private String          _outputDir;
    private boolean         _requireSource;
    private String          _sourceDir;
    private boolean         _debugMode;
    private boolean         _isVerbose;
    private String          _widgetArchive;
    private String          _archiveName;
    
    public boolean parse (String[] inputParams) throws PackageException, CommandLineException {
        // Validate at least one parameter.
        if (inputParams.length < 1) {
            throw new CommandLineException("EXCEPTION_INVALID_COMMAND_LINE");
        }

        // Get first param - exception case: /h
        String input1 = inputParams[0].toLowerCase().trim();
        if (input1.equals(OPTION_HELP)) {
            Logger.logMessage(LogType.NONE, "BBWP_USAGE", WidgetPackager.getVersion());
            return false;
        }

        // Check archive format
        if (!input1.endsWith(".zip")) {
            throw new CommandLineException("EXCEPTION_INVALID_COMMAND_LINE");
        }

        // Parse the command line
        _widgetArchive = getAbsolutePath(input1);
        _archiveName = parseWidgetName(_widgetArchive);

        Pattern patternWidgetName = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
        if (!patternWidgetName.matcher(_archiveName).matches()) {
        	throw new PackageException("EXCEPTION_INVALID_ARCHIVE_NAME");
        }

        // Parse options
        try {
            parseOptionParameters(inputParams);
        } catch (Exception e) {
        	throw new CommandLineException("EXCEPTION_INVALID_COMMAND_LINE");
        }  
        return true;
    }
    
    public SessionManager createSession() throws Exception {
        // Parse location of packager
        String bbwpInstallFolder; 
        String installPath = getAbsolutePath(SessionManager.BBWP_JAR_PATH);
        File p = new File(installPath);
        if (p.isDirectory()) {
            if (installPath.lastIndexOf(FILE_SEP) == installPath.length() - 1) {
                bbwpInstallFolder = installPath;
            } else {
                bbwpInstallFolder = installPath + FILE_SEP;
            }
        } else {
            installPath = installPath.substring(0, installPath
                    .lastIndexOf(FILE_SEP))
                    + FILE_SEP;
            bbwpInstallFolder = installPath;
        }
        
        SessionManager.createInstance(
                _archiveName,
                _widgetArchive,
                bbwpInstallFolder,
                _outputDir,
                _requireSigned, 
                _password,
                _requireSource,
                _sourceDir,
                _debugMode,
                _isVerbose);
        return SessionManager.getInstance();
    }
        
    private String parseWidgetName(String archivePath) {
        String name = archivePath.substring(
                archivePath.lastIndexOf(FILE_SEP) + 1, archivePath.lastIndexOf("."));
        return name;
    }
      
    private String getAbsolutePath(String filePath) {
        try {
            return (new File(filePath)).getCanonicalFile().getAbsolutePath();
        } catch (Exception e) {
            return (new File(filePath)).getAbsolutePath();
        }
    }
    
    private void parseOptionParameters(String[] params) throws Exception {
        _requireSigned = false;
        _password = "";
        _outputDir = "";
        _requireSource = false;
        _sourceDir = "";

        int index = 1;
        while (index < params.length) {
            String param = params[index];

            if (param.equals(OPTION_HELP)) {
                throw new Exception();
            } else if (param.equals(OPTION_DEBUG)) {
                _debugMode = true;
                index++;
            } else if (param.equals(OPTION_VERBOSE)) {
                _isVerbose = true;
                index++;
            } else if (param.equals(OPTION_PASSWORD)) {
                _requireSigned = true;
                if (params.length > index + 1) {
                    String followingParameter = params[index + 1];
                    if (followingParameter.charAt(0) != '/') {
                        _password = followingParameter;
                        index++;
                    }
                }
                index++;
            } else if (param.equals(OPTION_OUTPUTDIR)) {
                if (params.length > index + 1) {
                    _outputDir = params[index + 1];
                    _outputDir = getAbsolutePath(_outputDir);
                    index += 2;
                } else {
                    throw new Exception();
                }
            } else if (param.equals(OPTION_SOURCEDIR)) {
                _requireSource = true;
                if (params.length > index + 1) {
                    String followingParameter = params[index + 1];
                    if (followingParameter.charAt(0) != '/') {
                        _sourceDir = followingParameter;
                        _sourceDir = getAbsolutePath(_sourceDir);
                        index++;
                    }
                }
                index++;
            } else {
                throw new Exception();
            }
        }

        // Populate correct source directory
        if (!_requireSource) {
            _sourceDir = System.getProperty("java.io.tmpdir") + "widgetGen."
                    + new Random().nextInt(2147483647) + new Date().getTime()
                    + ".tmp";
        } else {
            if (_sourceDir.length() != 0) {
                _sourceDir = _sourceDir + FILE_SEP + "src";
            } else {
                if (_outputDir.length() != 0) {
                    _sourceDir = _outputDir + FILE_SEP + "src";
                } else {
                    _sourceDir = _widgetArchive.substring(0, _widgetArchive
                            .lastIndexOf(FILE_SEP) + 1)
                            + "src";
                }
            }
        }

        // Populate correct output directory
        if (_outputDir.length() == 0) {
            _outputDir = _widgetArchive.substring(0, _widgetArchive
                    .lastIndexOf(FILE_SEP) + 1)
                    + "bin";
        }
    }
}

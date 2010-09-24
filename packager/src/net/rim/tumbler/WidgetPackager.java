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
package net.rim.tumbler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.exception.CommandLineException;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.file.FileManager;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.rapc.Rapc;
import net.rim.tumbler.serialize.WidgetConfigSerializer;
import net.rim.tumbler.serialize.WidgetConfig_v1Serializer;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;
import net.rim.tumbler.xml.ConfigXMLParser;
import net.rim.tumbler.xml.XMLParser;

public class WidgetPackager {
	
	
	public static final String[] STANDARD_OUTPUTS = new String[] { ".cod",
			".alx", ".cso", ".csl" };
	public static final String[] OTA_OUTPUTS = new String[] { ".cod", ".jad" };

	// TODO: retrieve from logger
	public static final String BLACKBERRY_WIDGET_PORTAL_URL = "http://www.blackberry.com/developers/widget/";
	public static final String PROPERTIES_FILE = "bbwp.properties";
	public static final String SIGNATURE_KEY_FILE = "sigtool.csk";

	private static final String AUTOGEN_FILE = "blackberry/web/widget/autogen/WidgetConfigAutoGen.java";
	
	private static final int NO_ERROR_RETURN_CODE = 0;
	private static final int PACKAGE_ERROR_RCODE = 1;
	private static final int VALIDATION_ERROR_RCODE = 2;
	private static final int RUNTIME_ERROR_RCODE = 3;
	private static final int UNEXPECTED_ERROR_RCODE = 4;
	private static final int COMMAND_LINE_EXCEPTION = 5;
	
	public static void main(String[] args) {
	    WidgetPackager wp = new WidgetPackager();
	    wp.go(args);
	}
	
	public void go(String[] args) {
		Logger.logMessage(LogType.INFO, "PROGRESS_CMDLINE_OPTIONS");
		int returnCode = NO_ERROR_RETURN_CODE;
		
		try {
			CmdLineHandler cmd = new CmdLineHandler();
			if (!cmd.parse(args)) {
			    // nothing to package
			    System.exit(NO_ERROR_RETURN_CODE);
			}
			
			// create SessionManager
			SessionManager sessionManager = cmd.createSession();

			// create bbwp.properties
			Logger.logMessage(LogType.INFO, "PROGRESS_SESSION_BBWP_PROPERTIES");
			String propertiesFile = sessionManager.getBBWPJarFolder()
					+ WidgetPackager.PROPERTIES_FILE;
			BBWPProperties bbwpProperties = new BBWPProperties(propertiesFile,
					sessionManager.getSessionHome());

			// validate widget archive
			Logger.logMessage(LogType.INFO,
					"PROGRESS_VALIDATING_WIDGET_ARCHIVE");
			WidgetArchive wa = new WidgetArchive(sessionManager
					.getWidgetArchive());
			wa.validate();

			// parse/validate config.xml
			Logger.logMessage(LogType.INFO, "PROGRESS_SESSION_CONFIGXML");
			XMLParser xmlparser = new ConfigXMLParser();			
			WidgetConfig config = xmlparser.parseXML(wa); // raw data, without \
			

			// create/clean outputs/source
			// Logger.printInfoMessage("Widget packaging starts...");
			FileManager fileManager = new FileManager(bbwpProperties);
			Logger.logMessage(LogType.INFO, "PROGRESS_FILE_POPULATING_SOURCE");
			fileManager.prepare();

			// Set 3rd party extension classes
			config.setExtensionClasses(fileManager.getExtensionClasses());
			
			// create autogen file
			WidgetConfigSerializer wcs = new WidgetConfig_v1Serializer(config);			
			byte[] autogenFile = wcs.serialize();
			fileManager.writeToSource(autogenFile, AUTOGEN_FILE);

			// create jdw/jdp files
			fileManager.generateProjectFiles(sessionManager.getSourceFolder(),
					sessionManager.getArchiveName(), config.getName(), config
							.getVersion(), config.getAuthor(),config.getContent(),config.getBackgroundSource(),config.isStartupEnabled(), config
							.getIconSrc(), config.getHoverIconSrc(),
					fileManager.getFiles(), bbwpProperties.getImports());

			// run rapc
			Logger.logMessage(LogType.INFO, "PROGRESS_COMPILING");
			Rapc rapc = new Rapc(bbwpProperties, config);
			if (!rapc.run(fileManager.getFiles())) {
				throw new PackageException("EXCEPTION_RAPC");
			}

			// generate ALX
			generateAlxFile(config);

			// Sign the cod if required
			if (sessionManager.requireSigning()) {
				Logger.logMessage(LogType.INFO, "PROGRESS_SIGNING");
				signCod(sessionManager);
				Logger.logMessage(LogType.INFO, "PROGRESS_SIGNING_COMPLETE");
			}

			// clean/prep output folders
			fileManager.cleanOutput();

			// copy output files
			Logger.logMessage(LogType.INFO, "PROGRESS_GEN_OUTPUT");
			fileManager.copyOutputsFromSource();

			// clean source (if necessary)
			if (!sessionManager.requireSource()) {
				fileManager.cleanSource();
			}

			Logger.logMessage(LogType.INFO, "PROGRESS_COMPLETE");
		} catch (CommandLineException cle) {
                        Logger.logMessage(LogType.ERROR, cle.getMessage(), cle.getInfo());
                        Logger.logMessage(LogType.NONE, "BBWP_USAGE", getVersion());
                        returnCode = COMMAND_LINE_EXCEPTION;		    		
		} catch (PackageException pe) {
			Logger.logMessage(LogType.ERROR, pe.getMessage(), pe.getInfo());
			returnCode = PACKAGE_ERROR_RCODE;
		} catch (ValidationException ve) {
			Logger.logMessage(LogType.ERROR, ve.getMessage(), ve.getInfo());
			returnCode = VALIDATION_ERROR_RCODE;
		} catch (RuntimeException re) {
			Logger.logMessage(LogType.FATAL, re);
			returnCode = RUNTIME_ERROR_RCODE;
		} catch (Exception e) {
			System.out.println(e);
			returnCode = UNEXPECTED_ERROR_RCODE;
		} 
		
		System.exit(returnCode);
	}
	
        public static Object[] getVersion() {
            return new Object[] { new WidgetPackager().getClass().getPackage().getImplementationVersion() };
        }
	    

	private static void signCod(SessionManager sessionManager) throws Exception {
		Process signingProcess;
		long lastModified = 0;
		String codFullname = sessionManager.getSourceFolder()
				+ System.getProperty("file.separator")
				+ sessionManager.getArchiveName() + ".cod";

		try {
			lastModified = (new File(codFullname)).lastModified();
			String password = sessionManager.getPassword();
			File cwd = new File(sessionManager.getBBWPJarFolder());
			String cmdline = "java -jar SignatureTool.jar -a -c "
					+ (password.length() == 0 ? "" : "-p " + password + " ")
					+ "\"" + sessionManager.getSourceFolder()
					+ System.getProperty("file.separator")
					+ sessionManager.getArchiveName() + ".cod" + "\"";
			signingProcess = Runtime.getRuntime().exec(cmdline, null, cwd);
		} catch (IOException ex) {
			throw ex;
		}

		try {
			int signingResult = signingProcess.waitFor();

			// Check whether signing is successful
			if (signingResult != 0) {
				throw new PackageException("EXCEPTION_SIGNING_FAILED");
			}

			long newModified = (new File(codFullname)).lastModified();
			if (newModified == lastModified) {
				throw new PackageException("EXCEPTION_SIGNING_FAILED");
			}
		} catch (InterruptedException e) {
			throw e;
		}
	}

	// Generate a .alx file
	private static void generateAlxFile(WidgetConfig widgetConfig)
			throws IOException {
		String EOL = System.getProperty("line.separator");
		String fileName = SessionManager.getInstance().getSourceFolder()
				+ System.getProperty("file.separator")
				+ SessionManager.getInstance().getArchiveName() + ".alx";
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write("<loader version=\"1.0\" >" + EOL);
		writer.write("<application id=\""
				+ SessionManager.getInstance().getArchiveName() + "\">" + EOL);
		writer.write("<name>" + widgetConfig.getName() + "</name>" + EOL);
		if (widgetConfig.getDescription() != null) {
			writer.write("<description>" + widgetConfig.getDescription()
					+ "</description>" + EOL);
		}
		writer.write("<version>" + widgetConfig.getVersion() + "</version>"
				+ EOL);
		if (widgetConfig.getAuthor() != null) {
			writer.write("<vendor>" + widgetConfig.getAuthor() + "</vendor>"
					+ EOL);
		}
		if (widgetConfig.getCopyright() != null) {
			writer.write("<copyright>" + widgetConfig.getCopyright()
					+ "</copyright>" + EOL);
		}
		writer.write("<fileset Java=\"1.45\">" + EOL);
		writer.write("<directory>");
		writer.write("</directory>" + EOL);
		writer.write("<files>");
		writer.write(SessionManager.getInstance().getArchiveName() + ".cod");
		writer.write("</files>" + EOL);
		writer.write("</fileset>" + EOL);
		writer.write("</application>" + EOL);
		writer.write("</loader>" + EOL);
		writer.close();
	}
}

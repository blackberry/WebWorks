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
package net.rim.tumbler.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FileManager {
    private BBWPProperties           _bbwpProperties;
    private Vector<String>           _outputFiles;
    private Vector<String>           _extensionClasses;
    
    private static final String         EOL = System.getProperty("line.separator");
    private static final String         FILE_SEP = System.getProperty("file.separator");
    private static final String         STANDARD_OUTPUT = "StandardInstall";
    private static final String         OTA_OUTPUT = "OTAInstall";
    private static final String         EXTENSION_DIRECTORY = "extension";
    
    public FileManager(BBWPProperties bbwpProperties) {
        _bbwpProperties = bbwpProperties;
        _outputFiles = new Vector<String>();
        _extensionClasses = new Vector<String>();
    }
    
    public List<String> getFiles() {
        return _outputFiles;
    }

    public void cleanOutput() {
        String outputDir = SessionManager.getInstance().getOutputFolder();
        String archiveName = SessionManager.getInstance().getArchiveName();
        deleteDirectory(new File(outputDir + FILE_SEP + FileManager.OTA_OUTPUT));
        deleteDirectory(new File(outputDir + FILE_SEP + FileManager.STANDARD_OUTPUT));
        (new File(outputDir + FILE_SEP + archiveName + ".jar")).delete();
        (new File(outputDir + FILE_SEP + archiveName + ".rapc")).delete();
    }
    
    public void cleanSource() {
        deleteDirectory(new File(SessionManager.getInstance().getSourceFolder()));
    }
    
    public void prepare() throws Exception {
        // Clean out source folder
        deleteDirectory(new File(SessionManager.getInstance().getSourceFolder()));
        (new File(SessionManager.getInstance().getSourceFolder())).mkdirs();
        
        // Copy templates
        try {
            TemplateWrapper templateWrapper = new TemplateWrapper(_bbwpProperties);
            _outputFiles.addAll(templateWrapper.writeAllTemplates(
                    SessionManager.getInstance().getSourceFolder()));
        } catch (IOException ex) {
            throw new PackageException("EXCEPTION_IO_TEMPLATES");
        }
        
        // Extract archive
        ZipFile zip = new ZipFile(new File(SessionManager.getInstance().getWidgetArchive()).getAbsolutePath());
        Enumeration<?> en = zip.entries();
        String sourceFolder = SessionManager.getInstance().getSourceFolder();
        while (en.hasMoreElements()) {
            // Create output file name
            ZipEntry ze = (ZipEntry) en.nextElement();
			if (ze.isDirectory())
				continue;            
            
			String zipEntryName = ze.getName();
            File zipEntryFile = new File(ze.getName());
            boolean isRoot = zipEntryFile.getParent() == null;
            String fname = sourceFolder + FILE_SEP + zipEntryFile.getPath();
            
            // Extract file
			InputStream is = zip.getInputStream(ze);
			File fi = new File(fname);
			if (!fi.getParentFile().isDirectory() || !fi.getParentFile().exists())
				fi.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(fname);
			int bytesRead;
			while ((bytesRead = is.read()) != -1)
				fos.write(bytesRead);
			fos.close();				
            
			if (zipEntryName.startsWith("ext") && zipEntryName.endsWith(".jar")) {
				populateExtension(fname);
			} else { 
	            // Hack for icon files not displayed properly if similar named files exist in sub folders
	            if (!isRoot) {
	                _outputFiles.add(0, fname);
	            } else {
	                _outputFiles.add(fname);
	            }
			}
        }
    }
    
    // Generate .jdp and .jdw files
    public void generateProjectFiles(
            String sourceDir, 
            String codName,
            String appName, 
            String appVersion, 
            String appVendor,
            String contentSource,
            String backgroundSource,
            boolean isStartupEnabled,
            Vector<String> icons, 
            Vector<String> hoverIcons,
            List<String> inputFiles, 
            List<String> libraryFiles)
            throws IOException {
        String fileName;
        BufferedWriter writer;

        // jdw file
        fileName = sourceDir + FILE_SEP + codName + ".jdw";
        writer = new BufferedWriter(new FileWriter(fileName));

        writer.write("## RIM Java Development Environment" + EOL);
        writer.write("# RIM Workspace file" + EOL);
        writer.write("#" + EOL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ EOL);
        writer.write("# It SHOULD NOT BE modified manually." + EOL);
        writer.write("#" + EOL);
        writer.write("[BuildConfigurations" + EOL);
        writer.write("Debug" + EOL);
        writer.write("Release" + EOL);
        writer.write("]" + EOL);
        writer.write("DependenciesInWorkspace=0" + EOL);
        writer.write("[ImplicitRules" + EOL);
        writer.write("]" + EOL);
        writer.write("[Imports" + EOL);
        writer.write("]" + EOL);
        writer.write("[Projects" + EOL);
        writer.write(codName + ".jdp" + EOL);
        
        // Alternate entry project
        if(backgroundSource!=null&&isStartupEnabled) {
        	writer.write("runOnStartup.jdp" + EOL);
        }
        
        writer.write("]" + EOL);
        writer.write("[ReleaseActiveProjects" + EOL);
        writer.write(codName + ".jdp" + EOL);
        writer.write("]" + EOL);
        writer.close();

        // jdp file
        fileName = sourceDir + FILE_SEP + codName + ".jdp";
        writer = new BufferedWriter(new FileWriter(fileName));

        writer.write("## RIM Java Development Environment" + EOL);
        writer.write("# RIM Project file" + EOL);
        writer.write("#" + EOL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ EOL);
        writer.write("# It SHOULD NOT BE modified manually." + EOL);
        writer.write("#" + EOL);
        writer.write("AddOn=0" + EOL);
        writer.write("AlwaysBuild=0" + EOL);
        writer.write("[AlxImports" + EOL);
        writer.write("]" + EOL);
        writer.write("AutoRestart=0" + EOL);
        writer.write("[ClassProtection" + EOL);
        writer.write("]" + EOL);
        writer.write("[CustomBuildFiles" + EOL);
        writer.write("]" + EOL);
        writer.write("[CustomBuildRules" + EOL);
        writer.write("]" + EOL);
        writer.write("[DefFiles" + EOL);
        writer.write("]" + EOL);
        writer.write("[DependsOn" + EOL);
        writer.write("]" + EOL);
        writer.write("ExcludeFromBuildAll=0" + EOL);
        writer.write("Exported=0" + EOL);

        writer.write("[Files" + EOL);
        for (int i = 0; i < inputFiles.size(); ++i) {
            String inputFile = inputFiles.get(i);
            inputFile = inputFile.substring(sourceDir.length() + 1);
            writer.write(inputFile + EOL);
        }
        writer.write("]" + EOL);

        writer.write("HaveAlxImports=0" + EOL);
        writer.write("HaveDefs=0" + EOL);
        writer.write("HaveImports=1" + EOL);

        writer.write("[Icons" + EOL);
        if (icons != null) {
            for (int i = 0; i < icons.size(); ++i) {
                writer.write(icons.elementAt(i) + EOL);
            }
        }
        writer.write("]" + EOL);

        writer.write("[ImplicitRules" + EOL);
        writer.write("]" + EOL);

        writer.write("[Imports" + EOL);
        for (int i = 0; i < libraryFiles.size(); ++i) {
            String libraryFile = libraryFiles.get(i);
            writer.write(libraryFile + EOL);
        }
        writer.write("]" + EOL);

        writer.write("Listing=0" + EOL);
        if(contentSource!=null) {
        	writer.write("MidletClass=rim:foreground"+EOL);
    	} else {
    		writer.write("MidletClass="+EOL);
    	}
        writer.write("Options=-quiet -deprecation" + EOL);
        writer.write("OutputFileName=" + codName + EOL);
        writer.write("[PackageProtection" + EOL);
        writer.write("]" + EOL);
        writer.write("Platform=0" + EOL);
        writer.write("RibbonPosition=0" + EOL);

        writer.write("[RolloverIcons" + EOL);
        if (hoverIcons != null) {
            for (int i = 0; i < hoverIcons.size(); ++i) {
                writer.write(hoverIcons.elementAt(i) + EOL);
            }
        }
        writer.write("]" + EOL);

        writer.write("RunOnStartup=0" + EOL);
        writer.write("StartupTier=7" + EOL); 
        
        if(contentSource!=null&&contentSource.length()!=0) {
        	writer.write("SystemModule=0" + EOL);
        } else {
        	writer.write("SystemModule=1" + EOL);
        }
        
        writer.write("Title=" + appName + EOL);
        writer.write("Type=0" + EOL);
        if (appVendor != null) { writer.write("Vendor=" + appVendor + EOL); }
        writer.write("Version=" + appVersion + EOL);

        writer.close();
        
        //Alternate jdp file
        
        //Do not generate the alternate jdp file if it isn't required
        if(backgroundSource==null||!isStartupEnabled) {
        	return;
        }
        
        fileName = sourceDir + FILE_SEP + "runOnStartup.jdp";
        writer = new BufferedWriter(new FileWriter(fileName));

        writer.write("## RIM Java Development Environment" + EOL);
        writer.write("# RIM Project file" + EOL);
        writer.write("#" + EOL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ EOL);
        writer.write("# It SHOULD NOT BE modified manually." + EOL);
        writer.write("#" + EOL);
        writer.write("AddOn=0" + EOL);
        writer.write("AlwaysBuild=0" + EOL);
        writer.write("[AlxImports" + EOL);
        writer.write("]" + EOL);
        writer.write("AutoRestart=0" + EOL);
        writer.write("[ClassProtection" + EOL);
        writer.write("]" + EOL);
        writer.write("[CustomBuildFiles" + EOL);
        writer.write("]" + EOL);
        writer.write("[CustomBuildRules" + EOL);
        writer.write("]" + EOL);
        writer.write("[DefFiles" + EOL);
        writer.write("]" + EOL);
        writer.write("[DependsOn" + EOL);
        writer.write("]" + EOL);
        writer.write("EntryFor="+codName+EOL);
        writer.write("ExcludeFromBuildAll=0" + EOL);
        writer.write("Exported=0" + EOL);

        writer.write("[Files" + EOL);
        writer.write("]" + EOL);

        writer.write("HaveAlxImports=0" + EOL);
        writer.write("HaveDefs=0" + EOL);
        writer.write("HaveImports=1" + EOL);

        writer.write("[Icons" + EOL);
        writer.write("]" + EOL);

        writer.write("[ImplicitRules" + EOL);
        writer.write("]" + EOL);

        writer.write("[Imports" + EOL);
        writer.write("]" + EOL);

        writer.write("Listing=0" + EOL);
        
        writer.write("MidletClass=rim:runOnStartup"+EOL);
        writer.write("Options=-quiet -deprecation" + EOL);
        writer.write("OutputFileName=" + codName + EOL);
        writer.write("[PackageProtection" + EOL);
        writer.write("]" + EOL);
        writer.write("Platform=0" + EOL);
        writer.write("RibbonPosition=0" + EOL);

        writer.write("[RolloverIcons" + EOL);
        if (hoverIcons != null) {
        	for (int i = 0; i < hoverIcons.size(); ++i) {
        		writer.write(hoverIcons.elementAt(i) + EOL);
        	}
        }
        writer.write("]" + EOL);
        writer.write("RunOnStartup=1" + EOL);
        writer.write("StartupTier=7" + EOL);
        writer.write("SystemModule=1" + EOL);
        writer.write("Title=" + appName + EOL);
        writer.write("Type=3" + EOL);
        if (appVendor != null) { writer.write("Vendor=" + appVendor + EOL); }
        writer.write("Version=" + appVersion + EOL);

        writer.close();
        
        return;
    }
    
    public void writeToSource(byte[] fileToWrite, String relativeFile) throws Exception {
        try {
            String s = SessionManager.getInstance().getSourceFolder() + FILE_SEP + relativeFile;
            if (!new File(s).exists()) {
                new File(s).getParentFile().mkdirs();
            }       
            FileOutputStream fos = new FileOutputStream(s);
            fos.write(fileToWrite);   
            fos.close();
            
            _outputFiles.add(s);
        } 
        catch (Exception e) {
            throw new PackageException(e, relativeFile);
        }
    }

    public void copyOutputsFromSource() throws Exception {
        // TODO: verify for missing files
        String sourceFolder = SessionManager.getInstance().getSourceFolder();
        String outputFolder = SessionManager.getInstance().getOutputFolder();
        String archiveName = SessionManager.getInstance().getArchiveName();
        createOutputDirs(outputFolder);
        
        // Standard output
        File from, to;
        for (String ext : WidgetPackager.STANDARD_OUTPUTS) {
            from = new File(sourceFolder + FILE_SEP + archiveName + ext);
            to = new File(outputFolder + FILE_SEP + FileManager.STANDARD_OUTPUT + FILE_SEP + archiveName + ext);
            copyFile(from, to);
        }
        
        // OTA output
        for (String ext : WidgetPackager.OTA_OUTPUTS) {
            from = new File(sourceFolder + FILE_SEP + archiveName + ext);
            to = new File(outputFolder + FILE_SEP + FileManager.OTA_OUTPUT + FILE_SEP + archiveName + ext);
            copyFile(from, to); 
        }
        from = new File(sourceFolder + FILE_SEP +  archiveName + ".cod");
        expandCod(from);
    }
    
    private void expandCod(File codFile) throws Exception {

        // If the codFile can be unzipped,
        // then the cod is too big and actually in the zip format with smaller
        // cods inside
        // otherwise, the cod is already a good cod

        ZipFile zipFile;

        // Check for file's existence
        if (!codFile.exists()) {
            throw new PackageException("EXCEPTION_COD_NOT_FOUND");
        } else {
            try {
                zipFile = new ZipFile(codFile);
                zipFile.close();
            } catch (Exception e) {
                return; // This is a not a zip file and thus, not a big cod
            }
        }

        FileInputStream fis = new FileInputStream(codFile);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                checksum));

        ZipEntry entry;
        BufferedOutputStream dest = null;
        final int BUFFER_SIZE = 1024;       


        while ((entry = zis.getNextEntry()) != null) {
            int count;
            byte data[] = new byte[BUFFER_SIZE];
            
            File f = new File(
                    SessionManager.getInstance().getOutputFolder() 
                    + FILE_SEP 
                    + "OTAInstall" 
                    + FILE_SEP + entry.getName());
            
            f.getParentFile().mkdirs();
            f.createNewFile();
            
            // Write the files to the disk
            FileOutputStream fos = new FileOutputStream(f);
            dest = new BufferedOutputStream(fos, BUFFER_SIZE);
            while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                dest.write(data, 0, count);
            }
            
            dest.flush();
            dest.close();
        }

        zis.close();
    }
    // Copy a file
    public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            // Windows is limited to 64mb chunks
            long size = inChannel.size();
            long position = 0;
            while (position < size)
                position += inChannel
                        .transferTo(position, 67076096, outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
    
    private void createOutputDirs(String outputFolder)
    {
        File standardInstallDir = new File(outputFolder + File.separator + FileManager.STANDARD_OUTPUT);
        File otaInstallDir = new File(outputFolder + File.separator + FileManager.OTA_OUTPUT);
        
        if (!(standardInstallDir.exists() && standardInstallDir.isDirectory())) {
            standardInstallDir.mkdirs();
        }

        if (!(otaInstallDir.exists() && otaInstallDir.isDirectory())) {
            otaInstallDir.mkdirs();         
        }      
    }

    // Delete a dir
    private boolean deleteDirectory(File dir) {
        // Remove files first
        if (dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDirectory(new File(dir, child)))
                    return false;
            }
        }
        if (dir.exists()) {
            // Then remove the directory
            return dir.delete();
        }
        return false;
    }    
    
	private void populateExtension(String extensionArchive) throws Exception {
		// Create the extension directory
		String extensionPath = SessionManager.getInstance().getSourceFolder() + FILE_SEP + EXTENSION_DIRECTORY  + FILE_SEP;
		(new File(extensionPath)).mkdirs();
		
		// Extract all resource files in archive
		ZipFile zip = new ZipFile(new File(extensionArchive).getAbsolutePath());
		Enumeration<?> en = zip.entries();
		while (en.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) en.nextElement();
			if (ze.isDirectory())
				continue;
			
			String zipEntryName = ze.getName();
			File zipEntryFile = new File(zipEntryName);
			String fname = extensionPath + zipEntryFile.getPath();

			InputStream is = zip.getInputStream(ze);
			File fi = new File(fname);
			if (!fi.getParentFile().isDirectory() || !fi.getParentFile().exists())
				fi.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(fname);
			int bytesRead;
			while ((bytesRead = is.read()) != -1)
				fos.write(bytesRead);
			fos.close();
			
			_outputFiles.add(fname);
			
			if (zipEntryName.equals("library.xml")) {
				is = zip.getInputStream(ze);
				
				int size;
				byte[] buffer = new byte[4096];

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(os, buffer.length);

				while ((size = is.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}

				bos.flush();
				bos.close();
				
				try {
					byte[] bytes = os.toString().trim().getBytes();
            
					DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = builderFactory.newDocumentBuilder();

					// Parse the xml file
					Document doc = builder.parse(new ByteArrayInputStream(bytes));
					doc.getDocumentElement().normalize();
					
			        Node nodeExtension = (Node) doc.getElementsByTagName("extension").item(0);
			        NodeList childNodes = nodeExtension.getChildNodes();
			        for (int i = 0; i < childNodes.getLength(); i++) {
			            Node node = childNodes.item(i);

			            if (node.getNodeType() == Node.ELEMENT_NODE) {
			                if (node.getNodeName().equals("entryClass")) {
			                    NodeList list = node.getChildNodes();
			                    for (int j = 0; j < list.getLength(); j++) {
			                        Node n = list.item(j);
			                        if (n.getNodeType() == Node.TEXT_NODE) {
			                            if (!n.getNodeValue().trim().equals("")) {
			                            	_extensionClasses.add(n.getNodeValue().trim());
			                            }
			                        }
			                    }
			                }
			            }
			        }
				} catch (SAXException saxEx) {
					throw new Exception("EXCEPTION_LIBRARYXML_BADXML", saxEx);
				}
			}			
		}
	} 
	
    public Vector<String> getExtensionClasses() {
        return _extensionClasses;
    }	
}

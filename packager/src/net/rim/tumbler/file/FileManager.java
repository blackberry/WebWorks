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
package net.rim.tumbler.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.config.FeatureManager;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

public class FileManager {
    private BBWPProperties      _bbwpProperties;
    private Vector<String>      _outputFiles;
    private FeatureManager      _featureManager;

    private static final String NL              = System.getProperty("line.separator");
    private static final String FILE_SEP        = System.getProperty("file.separator");
    private static final String STANDARD_OUTPUT = "StandardInstall";
    private static final String OTA_OUTPUT      = "OTAInstall";
    
    public FileManager(BBWPProperties bbwpProperties, Hashtable<WidgetAccess, Vector<WidgetFeature>> accessTable) {
        _bbwpProperties = bbwpProperties;
        _outputFiles = new Vector<String>();
        _featureManager = new FeatureManager(bbwpProperties, accessTable);
    }
    
    public List<String> getFiles() {
        return _outputFiles;
    }

    public void cleanOutput() {
        String outputDir = SessionManager.getInstance().getOutputFolder();
        String archiveName = SessionManager.getInstance().getArchiveName();
        deleteDirectory(new File(outputDir + FILE_SEP + FileManager.OTA_OUTPUT));
        deleteDirectory(new File(outputDir + FILE_SEP + FileManager.STANDARD_OUTPUT));
        File jarFile = new File(outputDir + FILE_SEP + archiveName + ".jar");
        if ( jarFile.exists() && jarFile.delete() == false ) {
            Logger.logMessage( LogType.WARNING, "EXCEPTION_DELETING_FILE", jarFile.getAbsolutePath());
        }
        File rapcFile = new File(outputDir + FILE_SEP + archiveName + ".rapc");
        if ( rapcFile.exists() && rapcFile.delete() == false ) {
            Logger.logMessage( LogType.WARNING, "EXCEPTION_DELETING_FILE", rapcFile.getAbsolutePath());
        }
    }
    
    public void cleanSource() {
        deleteDirectory(new File(SessionManager.getInstance().getSourceFolder()));
    }
    
    public void prepare() throws Exception {
        // Clean out source folder
        deleteDirectory(new File(SessionManager.getInstance().getSourceFolder()));
        
        File f = new File(SessionManager.getInstance().getSourceFolder());
        if ( !(f.exists() && f.isDirectory()) ) {
	        if ( f.mkdirs() == false ) {
	            Logger.logMessage( LogType.WARNING, "EXCEPTION_MAKING_DIRECTORY");
	        }  
        }
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
        HashSet<ZipEntry> extJars = new HashSet<ZipEntry>();
        
        while (en.hasMoreElements()) {
            // Create output file name
            ZipEntry ze = (ZipEntry) en.nextElement();
			if (ze.isDirectory())
				continue;            
            
			String zipEntryName = ze.getName();
            File zipEntryFile = new File(ze.getName());
            boolean isRoot = zipEntryFile.getParent() == null;
            String fname = sourceFolder + FILE_SEP + zipEntryFile.getPath();
            
            if (zipEntryName.startsWith("ext") && zipEntryName.endsWith(".jar")) {
            	extJars.add(ze);
            } else {            
	            // Extract file
            	copyZipEntry(zip, ze, sourceFolder + FILE_SEP);
	            
				// Hack for icon files not displayed properly if similar named
				// files exist in sub folders
				if (!isRoot) {
					_outputFiles.add(0, fname);
				} else {
					_outputFiles.add(fname);
				}
            }
        }
                
        copyExtensionFiles(zip, extJars);
    }
    
	/**
	 * @param zip
	 *            WebWorks application archive
	 * @param extJars
	 *            extension JAR zip entries found in archive's "ext" folder
	 * @throws Exception
	 *             if there is extensions cannot be resolved
	 */
    private void copyExtensionFiles(ZipFile zip, HashSet<ZipEntry> extJars) throws Exception {
		HashSet<String> resolvedExtensionPaths = _featureManager
				.resolveFeatures(zip, extJars);

		_outputFiles.addAll(resolvedExtensionPaths);
		
		for (String path : _featureManager.getCommonAPIPaths()) {
			_outputFiles.add(0, path);
		}
    }
    
	public static File copyZipEntry(ZipFile zipFile, ZipEntry zipEntry,
			String dest) throws IOException {
		String fname = dest;
		if (dest.endsWith(FILE_SEP)) {
			fname = dest + new File(zipEntry.getName()).getPath();
		}
		InputStream is = zipFile.getInputStream(zipEntry);
		File fi = new File(fname);
		if (!fi.getParentFile().isDirectory() || !fi.getParentFile().exists()) {
            if (fi.getParentFile().mkdirs() == false) {
                Logger.logMessage(LogType.WARNING,
                        "EXCEPTION_MAKING_DIRECTORY", fi.getParentFile().toString());
            }
		}
		FileOutputStream fos = new FileOutputStream(fname);
		int bytesRead;
		while ((bytesRead = is.read()) != -1) {
			fos.write(bytesRead);
		}
		fos.close();

		return fi;
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

        writer.write("## RIM Java Development Environment" + NL);
        writer.write("# RIM Workspace file" + NL);
        writer.write("#" + NL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ NL);
        writer.write("# It SHOULD NOT BE modified manually." + NL);
        writer.write("#" + NL);
        writer.write("[BuildConfigurations" + NL);
        writer.write("Debug" + NL);
        writer.write("Release" + NL);
        writer.write("]" + NL);
        writer.write("DependenciesInWorkspace=0" + NL);
        writer.write("[ImplicitRules" + NL);
        writer.write("]" + NL);
        writer.write("[Imports" + NL);
        writer.write("]" + NL);
        writer.write("[Projects" + NL);
        writer.write(codName + ".jdp" + NL);
        
        // Alternate entry project
        if(backgroundSource!=null&&isStartupEnabled) {
        	writer.write("runOnStartup.jdp" + NL);
        }
        
        writer.write("]" + NL);
        writer.write("[ReleaseActiveProjects" + NL);
        writer.write(codName + ".jdp" + NL);
        writer.write("]" + NL);
        writer.close();

        // jdp file
        fileName = sourceDir + FILE_SEP + codName + ".jdp";
        writer = new BufferedWriter(new FileWriter(fileName));

        writer.write("## RIM Java Development Environment" + NL);
        writer.write("# RIM Project file" + NL);
        writer.write("#" + NL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ NL);
        writer.write("# It SHOULD NOT BE modified manually." + NL);
        writer.write("#" + NL);
        writer.write("AddOn=0" + NL);
        writer.write("AlwaysBuild=0" + NL);
        writer.write("[AlxImports" + NL);
        writer.write("]" + NL);
        writer.write("AutoRestart=0" + NL);
        writer.write("[ClassProtection" + NL);
        writer.write("]" + NL);
        writer.write("[CustomBuildFiles" + NL);
        writer.write("]" + NL);
        writer.write("[CustomBuildRules" + NL);
        writer.write("]" + NL);
        writer.write("[DefFiles" + NL);
        writer.write("]" + NL);
        writer.write("[DependsOn" + NL);
        writer.write("]" + NL);
        writer.write("ExcludeFromBuildAll=0" + NL);
        writer.write("Exported=0" + NL);

        writer.write("[Files" + NL);
        for (int i = 0; i < inputFiles.size(); ++i) {
            String inputFile = inputFiles.get(i);
            inputFile = inputFile.substring(sourceDir.length() + 1);
            writer.write(inputFile + NL);
        }
        writer.write("]" + NL);

        writer.write("HaveAlxImports=0" + NL);
        writer.write("HaveDefs=0" + NL);
        writer.write("HaveImports=1" + NL);

        writer.write("[Icons" + NL);
        if (icons != null) {
            for (int i = 0; i < icons.size(); ++i) {
                writer.write(icons.elementAt(i) + NL);
            }
        }
        writer.write("]" + NL);

        writer.write("[ImplicitRules" + NL);
        writer.write("]" + NL);

        writer.write("[Imports" + NL);
        for (int i = 0; i < libraryFiles.size(); ++i) {
            String libraryFile = libraryFiles.get(i);
            writer.write(libraryFile + NL);
        }
        
        for (String file : _featureManager.getCompiledJARDependencies()) {
        	writer.write(file + NL);
        }
        writer.write("]" + NL);

        writer.write("Listing=0" + NL);
        if(contentSource!=null) {
        	writer.write("MidletClass=rim:foreground"+NL);
    	} else {
    		writer.write("MidletClass="+NL);
    	}
        writer.write("Options=-quiet -deprecation" + NL);
        writer.write("OutputFileName=" + codName + NL);
        writer.write("[PackageProtection" + NL);
        writer.write("]" + NL);
        writer.write("Platform=0" + NL);
        writer.write("RibbonPosition=0" + NL);

        writer.write("[RolloverIcons" + NL);
        if (hoverIcons != null) {
            for (int i = 0; i < hoverIcons.size(); ++i) {
                writer.write(hoverIcons.elementAt(i) + NL);
            }
        }
        writer.write("]" + NL);

        writer.write("RunOnStartup=0" + NL);
        writer.write("StartupTier=7" + NL); 
        
        if(contentSource!=null&&contentSource.length()!=0) {
        	writer.write("SystemModule=0" + NL);
        } else {
        	writer.write("SystemModule=1" + NL);
        }
        
        writer.write("Title=" + appName + NL);
        writer.write("Type=0" + NL);
        if (appVendor != null) { writer.write("Vendor=" + appVendor + NL); }
        writer.write("Version=" + appVersion + NL);

        writer.close();
        
        //Alternate jdp file
        
        //Do not generate the alternate jdp file if it isn't required
        if(backgroundSource==null||!isStartupEnabled) {
        	return;
        }
        
        fileName = sourceDir + FILE_SEP + "runOnStartup.jdp";
        writer = new BufferedWriter(new FileWriter(fileName));

        writer.write("## RIM Java Development Environment" + NL);
        writer.write("# RIM Project file" + NL);
        writer.write("#" + NL);
        writer.write("# This file is generated and managed by BlackBerry developer tools."+ NL);
        writer.write("# It SHOULD NOT BE modified manually." + NL);
        writer.write("#" + NL);
        writer.write("AddOn=0" + NL);
        writer.write("AlwaysBuild=0" + NL);
        writer.write("[AlxImports" + NL);
        writer.write("]" + NL);
        writer.write("AutoRestart=0" + NL);
        writer.write("[ClassProtection" + NL);
        writer.write("]" + NL);
        writer.write("[CustomBuildFiles" + NL);
        writer.write("]" + NL);
        writer.write("[CustomBuildRules" + NL);
        writer.write("]" + NL);
        writer.write("[DefFiles" + NL);
        writer.write("]" + NL);
        writer.write("[DependsOn" + NL);
        writer.write("]" + NL);
        writer.write("EntryFor="+codName+NL);
        writer.write("ExcludeFromBuildAll=0" + NL);
        writer.write("Exported=0" + NL);

        writer.write("[Files" + NL);
        writer.write("]" + NL);

        writer.write("HaveAlxImports=0" + NL);
        writer.write("HaveDefs=0" + NL);
        writer.write("HaveImports=1" + NL);

        writer.write("[Icons" + NL);
        writer.write("]" + NL);

        writer.write("[ImplicitRules" + NL);
        writer.write("]" + NL);

        writer.write("[Imports" + NL);
        writer.write("]" + NL);

        writer.write("Listing=0" + NL);
        
        writer.write("MidletClass=rim:runOnStartup"+NL);
        writer.write("Options=-quiet -deprecation" + NL);
        writer.write("OutputFileName=" + codName + NL);
        writer.write("[PackageProtection" + NL);
        writer.write("]" + NL);
        writer.write("Platform=0" + NL);
        writer.write("RibbonPosition=0" + NL);

        writer.write("[RolloverIcons" + NL);
        if (hoverIcons != null) {
        	for (int i = 0; i < hoverIcons.size(); ++i) {
        		writer.write(hoverIcons.elementAt(i) + NL);
        	}
        }
        writer.write("]" + NL);
        writer.write("RunOnStartup=1" + NL);
        writer.write("StartupTier=7" + NL);
        writer.write("SystemModule=1" + NL);
        writer.write("Title=" + appName + NL);
        writer.write("Type=3" + NL);
        if (appVendor != null) { writer.write("Vendor=" + appVendor + NL); }
        writer.write("Version=" + appVersion + NL);

        writer.close();
        
        return;
    }
    
    public void writeToSource(byte[] fileToWrite, String relativeFile) throws Exception {
        try {
            String s = SessionManager.getInstance().getSourceFolder() + FILE_SEP + relativeFile;
            if (!new File(s).exists()) {
            	File pf = (new File(s)).getParentFile();
            	if ( pf != null && !(pf.exists() && pf.isDirectory()) ) {
	                if ( pf.mkdirs() == false ) {
	                    Logger.logMessage( LogType.WARNING, "EXCEPTION_MAKING_DIRECTORY");
	                }  
            	}
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

        // If the codFile can be unzipped, then the cod is too big 
        // and actually in the zip format with smaller (sibling) cods 
        // inside. Otherwise, the cod is already a good cod.

        ZipFile zipFile;

        // Check for file's existence
        if (!codFile.exists())
            throw new PackageException("EXCEPTION_COD_NOT_FOUND");
        
        boolean containsSiblingCods;

        try {
            zipFile = new ZipFile(codFile);
            zipFile.close();
            containsSiblingCods = true;
        } catch (Exception e) {
            containsSiblingCods = false;
        }
        
        if(!containsSiblingCods)
            return;

        FileInputStream fis = new FileInputStream(codFile);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));

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
            
            if( f.getParentFile() != null && !(f.getParentFile().exists() && f.getParentFile().isDirectory()) ) {
	            if( f.getParentFile().mkdirs() == false ) {
	                Logger.logMessage( LogType.WARNING, "EXCEPTION_MAKING_DIRECTORY", f.toString());
	            } 
            }
            
            if( !f.exists() && f.createNewFile() == false ) {
                Logger.logMessage( LogType.WARNING, "EXCEPTION_CREATING_FILE", f.toString());
            }            
            
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
    
    private void createOutputDirs(String outputFolder) {
        File standardInstallDir = new File(outputFolder + File.separator + FileManager.STANDARD_OUTPUT);
        File otaInstallDir = new File(outputFolder + File.separator + FileManager.OTA_OUTPUT);
        
        if (!(standardInstallDir.exists() && standardInstallDir.isDirectory())) {
            if (standardInstallDir.mkdirs() == false) {
                Logger.logMessage(LogType.WARNING, "EXCEPTION_MAKING_DIRECTORY",
                        standardInstallDir.toString());
            }
        }

        if (!(otaInstallDir.exists() && otaInstallDir.isDirectory())) {
            if (otaInstallDir.mkdirs() == false) {
                Logger.logMessage(LogType.WARNING, "EXCEPTION_MAKING_DIRECTORY",
                        otaInstallDir.toString());
            }
        }      
    }

    // Delete a dir
    public static boolean deleteDirectory(File dir) {
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
	
    public Vector<String> getExtensionClasses() {
    	Vector<String> extClasses = new Vector<String>();
    	extClasses.addAll(_featureManager.getExtensionClasses());
    	return extClasses;
    }
    
    public List<String> getCompiledJARDependencies() {
    	List<String> jarDependencies = new ArrayList<String>();
    	jarDependencies.addAll(_featureManager.getCompiledJARDependencies());
    	return jarDependencies;
    }
}

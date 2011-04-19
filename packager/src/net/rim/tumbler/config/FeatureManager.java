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
package net.rim.tumbler.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.file.ExtensionDependencyManager;
import net.rim.tumbler.file.FileManager;
import net.rim.tumbler.file.Library;
import net.rim.tumbler.file.Library.Configuration;
import net.rim.tumbler.file.Library.Extension;
import net.rim.tumbler.file.Library.Jar;
import net.rim.tumbler.file.Library.Platform;
import net.rim.tumbler.file.Library.Src;
import net.rim.tumbler.file.Library.Target;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;
import net.rim.tumbler.xml.LibraryXMLParser;

public class FeatureManager {
    // TODO Hardcode platform="JAVA"
    private String                                _platform           = "JAVA";
    // TODO Hardcode target="default" until target becomes one of the command
    // line params in bbwp.exe
    private String                                _targetVersion      = "default";
    private HashSet< String >                     _requiredFeatures;
    // {featureId, paths & root library.xml info}
    private Hashtable< String, ExtensionInfo >    _repositoryFeatures;
    // {extensionId, paths & root library.xml info}
    private Hashtable< String, ExtensionInfo >    _extensionLookupTable;
    private HashSet< String >                     _resolvedPaths;     
    // By default, "ext" under Tumbler's home, configurable in bbwp.properties
    private String                                _repositoryDir;
    // Common folder contains shared common APIs, folder under _repositoryDir
    // for storing common API
    private String                                _commonAPIDir       = "common";
    private HashSet< String >                     _extensionClasses;
	// List of compiled JARs that the extensions might depend on
    private HashSet< String >                     _compiledJARDependencies;
	
    private HashSet< String >                     _curPaths           = new HashSet< String >();
    private HashSet< String >                     _commonAPIPaths     = new HashSet< String >();
    private File                                  _temporaryDirectory;
	// TODO Hardcode features that are not found in extension repository,
	// temporary workaround until widgetcache extension is moved out of
	// framework
	private static HashSet< String >              FRAMEWORK_FEATURES  = new HashSet< String >();

    private static final String                   TEMPORARY_DIRECTORY = "~temporaryextensionsource";
    private static final String                   EXTENSION_DIRECTORY = "extension";
    private static final String                   LIBRARY_XML         = "library.xml";

	/**
	 * Stores the extension's library.xml information and the paths to source
	 * code in repository.
	 */
	public static class ExtensionInfo {
		private Library _lib;
		private HashSet<String> _repPaths;
		private HashSet<String> _jarPaths;

		public ExtensionInfo(Library lib, HashSet<String> repPaths) {
			_lib = lib;
			_repPaths = repPaths;
		}

		public Library getLibrary() {
			return _lib;
		}
		
		/**
		 * @return the extension id found in library.xml, or null if it is not
		 *         defined
		 */
		public String getExtensionId() {
			if (_lib != null && _lib.getExtension() != null) {
				return _lib.getExtension().getId();
			}

			return null;
		}

		public HashSet<String> getRepositoryPaths() {
			return _repPaths;
		}
		
		public HashSet<String> getCompiledJARPaths() {
			return _jarPaths;
		}
		
		public void addCompiledJARPath(String path) {
			if (_jarPaths == null) {
				_jarPaths = new HashSet<String>();
			}
			
			_jarPaths.add(path);
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("{lib: ");
			buf.append(_lib);
			buf.append(", paths: ");
			buf.append(_repPaths);
			buf.append("}");
			return buf.toString();
		}				
	}

	public FeatureManager(BBWPProperties bbwpProperties,
			Hashtable<WidgetAccess, Vector<WidgetFeature>> accessTable) {
		_repositoryFeatures = new Hashtable<String, ExtensionInfo>();
		_extensionLookupTable = new Hashtable<String, ExtensionInfo>();
		_resolvedPaths = new HashSet<String>();
		_repositoryDir = bbwpProperties.getRepositoryDir();
		_requiredFeatures = getRequiredFeatures(accessTable);
		_extensionClasses = new HashSet<String>();
		_compiledJARDependencies = new HashSet<String>();
		
		// TODO temp workaround, treat widgetcache features as exceptions
		// will not throw error when the code fails to find them in extension
		// repository		
		if (FRAMEWORK_FEATURES.isEmpty()) {
			FRAMEWORK_FEATURES.add("blackberry.widgetcache");
			FRAMEWORK_FEATURES.add("blackberry.widgetcache.CacheInformation");
		}
	}

	private static HashSet<String> getRequiredFeatures(
			Hashtable<WidgetAccess, Vector<WidgetFeature>> accessTable) {
		Set<WidgetAccess> keys = accessTable.keySet();
		HashSet<String> requiredFeatures = new HashSet<String>();

		for (Object accessKey : keys) {
			Vector<WidgetFeature> features = (Vector<WidgetFeature>) accessTable
					.get(accessKey);

			for (Object featureObject : features) {
				WidgetFeature feature = (WidgetFeature) featureObject;
				requiredFeatures.add(feature.getID());
			}
		}

		return requiredFeatures;
	}	
	
	/**
	 * Given a WebWorks application archive, look at the features specified in
	 * the white list and figure out all the extensions that need to be built.
	 * 
	 * @param widgetArchive
	 *            WebWorks application archive
	 * @param extJars
	 *            zip entries of JAR files found in the archive's "ext" folder
	 * @return a set of file paths of the source code of all extensions that
	 *         need to be built
	 * @throws Exception
	 *             if problems encountered while resolving extension
	 *             dependencies
	 */
	public HashSet<String> resolveFeatures(ZipFile widgetArchive,
			HashSet<ZipEntry> extJars) throws Exception {
		// step 1: resolve features from widget archive ext folder
		// if JAR files contribute to features from core API, JAR wins
		if (extJars != null && !extJars.isEmpty()) {
			resolveFeaturesFromExtJars(widgetArchive, extJars);
		}

		// step 2: resolve features from repository folder
		resolveFeaturesFromRepository();

		// step 3: copy common API files to extension folder for code to compile
		copyCommonAPI();

		return _resolvedPaths;
	}

	public HashSet<String> getExtensionClasses() {
		return _extensionClasses;
	}
	
	/**
	 * @return folders that need to be copied for common APIs
	 */
	public HashSet<String> getCommonAPIPaths() {
		return _commonAPIPaths;
	}
	
	/**
	 * @return
	 */
	public HashSet<String> getCompiledJARDependencies() {
		return _compiledJARDependencies;
	}

	/**
	 * Check JARs in widget archive's ext folder <br>
	 * If required features are found in JARs, extract them to source folder <br>
	 * Add paths in HashSet when done
	 */
	private void resolveFeaturesFromExtJars(ZipFile widgetArchive,
			HashSet<ZipEntry> extJars) {
		if (_temporaryDirectory != null) {
            if (_temporaryDirectory.delete() == false) {
                Logger.logMessage(LogType.WARNING, "EXCEPTION_DELETING_DIRECTORY",
                        _temporaryDirectory.toString());
            }
		}
		_temporaryDirectory = new File(getTempExtensionPath());

		for (ZipEntry jarFile : extJars) {
			try {
				// uncompress the jar into folders
				File sourceExtension = unzipJarToTempDir(widgetArchive, jarFile);

				if (sourceExtension == null) {
                    Logger.logMessage(LogType.WARNING,
                            "EXCEPTION_FAILING_DECOMPRESS_JAR", jarFile.getName());
					return;
				}

				// parse features offered from ext JARs
				Hashtable<String, ExtensionInfo> eligibleFeatures = getEligibleFeatures(sourceExtension);

				// if any eligible ids exists, move the content of the temp dir
				// into a permanent location
				finalizeEligibleIDs(eligibleFeatures);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				File[] tempExten = _temporaryDirectory.listFiles();

				for (File f : tempExten) {
					FileManager.deleteDirectory(f);
				}
			}
		}
		
		FileManager.deleteDirectory(_temporaryDirectory);
	}

	/*
	 * Takes the jar, uncompress it to temp folder, then return its handle in
	 * the temp folder
	 */
	private File unzipJarToTempDir(ZipFile widgetArchive, ZipEntry jarFile)
			throws IOException {

		String extensionName = getExtensionName(jarFile.getName());
		File sourceExtensionJar = null;
		ZipFile zipFile = null;
		try {
			// first copy the zip entry into the extension folder and identify
			// it as a jar
			sourceExtensionJar = FileManager.copyZipEntry(widgetArchive,
					jarFile, getExtensionPath() + extensionName + ".jar");

			zipFile = new ZipFile(sourceExtensionJar);
			Enumeration<?> enu = zipFile.entries();

			// go through the jar, copy each entry into the temporary folder
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				
				if (zipEntry.isDirectory()) {
					continue;
				}
				
				FileManager.copyZipEntry(zipFile, zipEntry,
						getTempExtensionPath() + extensionName + File.separator);
			}
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}

			if (sourceExtensionJar != null) {
	            if (sourceExtensionJar.delete() == false) {
	                Logger.logMessage(LogType.WARNING,
	                        "EXCEPTION_DELETING_FILE", sourceExtensionJar.toString());
	            }
			}
		}

		File[] fileList = _temporaryDirectory.listFiles();
		for (File f : fileList) {
			if (extensionName.equals(f.getName())) {
				return f;
			}
		}

		return null;
	}

	private static String getExtensionName(String extensionName) {
		if (extensionName == null) {
			return null;
		}
		int fileTypePosition = extensionName.lastIndexOf(".jar");
		// the zip entry might look like "ext/xxx.jar" or "ext\xxx.jar"
		// if checking index of "/" gives -1, should check the index of "\"
		int fileSepPosition = extensionName.lastIndexOf("/");

		if (fileSepPosition < 0) {
			fileSepPosition = extensionName.lastIndexOf("\\");
		}
		
		if (fileTypePosition < 0) {						
			fileTypePosition = extensionName.length();
		}
		return extensionName.substring(fileSepPosition + 1, fileTypePosition);
	}

	private Hashtable<String, ExtensionInfo> getEligibleFeatures(
			File sourceExtension) {
		Hashtable<String, ExtensionInfo> eligibleFeatures = new Hashtable<String, ExtensionInfo>();

		File[] files = sourceExtension.listFiles();
		Hashtable<String, ExtensionInfo> availableFeatures = null;

		// library.xml should at top level
		for (File f : files) {
			if (f.getName().equalsIgnoreCase(LIBRARY_XML)) {
				try {
					availableFeatures = parseFeaturesInLibraryXML(f, true);
				} catch (Exception e) {
					return eligibleFeatures;
				}
				
				if (availableFeatures != null) {
					for (Entry<String, ExtensionInfo> entry : availableFeatures
							.entrySet()) {
						eligibleFeatures.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}

		return eligibleFeatures;
	}

	@SuppressWarnings("unchecked")
    private void finalizeEligibleIDs(Hashtable<String, ExtensionInfo> eligibleFeatures) {
	    for (Entry<String, ExtensionInfo> entry : eligibleFeatures.entrySet()) {
	        String key = entry.getKey();
	        ExtensionInfo value = entry.getValue();
	        if (_requiredFeatures.contains(key)) {
	            copyExtensionPathsToSourceDir(value.getRepositoryPaths());
				_resolvedPaths.addAll((HashSet<String>) _curPaths.clone());
	            _extensionClasses.add(value.getLibrary().getEntryClass());

				// remove features from the list, so that even if the feature is
				// available in repository, the repository version will not be used
	            _requiredFeatures.remove(key);
	        }
	    }
	}

	/**
	 * Resolve features from extension repository
	 */
	@SuppressWarnings("unchecked")
	private void resolveFeaturesFromRepository() throws Exception {
		parseRepository();

		HashSet<String> extensions = new HashSet<String>();
		
		// derive extensions to be built based on features on white list
		for (String featureId : _requiredFeatures) {
			ExtensionInfo info = _repositoryFeatures.get(featureId);
			
			if (info != null) {
				String extensionId = info.getExtensionId();

				// unable to build app that uses feature from an extension that
				// does not have an id
				// because it is not possible to resolve dependencies
				if (extensionId != null && !extensionId.isEmpty()) {
					extensions.add(extensionId);
					
					// if the extension has any JAR dependencies, add it to the
					// list so that it gets added to rapc classpath
					if (info.getCompiledJARPaths() != null) {
						for (String jarPath : info.getCompiledJARPaths()) {
							File jarFile = new File(jarPath);
							_compiledJARDependencies.add(jarFile
									.getAbsolutePath());
						}
					}
				} else {
					throw new PackageException(
							"EXCEPTION_NEED_FEATURE_FROM_UNIDENTIFIED_EXTENSION", featureId);
				}
			} else {
				// TODO temp workaround to not throw error when widgetcache
				// features cannot be found in repository
				if (!FRAMEWORK_FEATURES.contains(featureId)) {
					throw new PackageException("EXCEPTION_FEATURE_NOT_FOUND",
							featureId);
				}
			}
		}
		
		// find all extensions that need to be built with dependencies taken
		// into account
		ExtensionDependencyManager edm = new ExtensionDependencyManager(
				_extensionLookupTable);
		extensions = edm.resolveExtensions(extensions);
				
		for (String extensionId : extensions) {
			HashSet<String> repPaths = _extensionLookupTable.get(extensionId)
					.getRepositoryPaths();
			copyExtensionPathsToSourceDir(repPaths);
			_resolvedPaths.addAll((HashSet<String>) _curPaths.clone());
			
			// extension can be found in lookup table for sure, otherwise
			// exception would have been thrown in ExtensionDependencyManager.resolve
			Library lib = _extensionLookupTable.get(extensionId).getLibrary();
			_extensionClasses.add(lib.getEntryClass());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void copyCommonAPI() {
		File dir = new File(_repositoryDir + File.separator + _commonAPIDir);		
		File[] files = dir.listFiles();
		
		for (File f : files) {
			if (f.exists()) {
				try {
					File destFile = new File(getExtensionPath() + f.getName());
					_curPaths.clear();
					copyFiles(f, destFile);
					_commonAPIPaths
							.addAll((HashSet<String>) _curPaths.clone());
                } catch (IOException e) {
                    Logger.logMessage(LogType.ERROR, "EXCEPTION_IO_COPY_FILES",
                            new String[] {f.getAbsolutePath(), e.getMessage()});
                }
			}
		}
	}

	/**
	 * For each folder in the extension repository, look for its library.xml and
	 * parse it to resolve source file paths
	 */
	private void parseRepository() {
		File dir = new File(_repositoryDir);

		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();

			for (File f : files) {
				if (f.isDirectory()) {
					File[] extFiles = f.listFiles();

					for (File g : extFiles) {
						try {
							if (g.getName().equalsIgnoreCase(LIBRARY_XML)) {
								Hashtable<String, ExtensionInfo> features = parseFeaturesInLibraryXML(
										g, false);
								if (features != null) {
									_repositoryFeatures.putAll(features);
								}
							}
						} catch (Exception e) {
							// TODO handle error
						}
					}
				}
			}
		}
	}

	/**
	 * Given a library file, find the configuration element that matches the
	 * current platform and target
	 * 
	 * @param lib
	 * @return the configuration that matches the current platform and target
	 */
	private Configuration getTargetConfiguration(Library lib) {
		String matchedConfigName = null;

		// check whether platform and target match what we support
		ArrayList<Platform> platforms = lib.getPlatforms();
		if (platforms != null) {
			for (Platform p : platforms) {
				if (p.getValue().equals(_platform)) {
					ArrayList<Target> targets = p.getTargets();

					if (targets != null) {
						for (Target t : targets) {
							if (t.getVersion().equals(_targetVersion)) {
								matchedConfigName = t.getConfigName();
							}
						}
					}
				}
			}
		}

		// make sure the config is defined
		if (matchedConfigName != null) {
			ArrayList<Configuration> configurations = lib.getConfigurations();

			for (Configuration config : configurations) {
				if (config.getName().equals(matchedConfigName)) {
					return config;
				}
			}
		}

		return null;
	}

	/**
	 * Helper method for parsing out library.xml into an object
	 * 
	 * @param libraryXML
	 * @throws Exception
	 */
	private static Library parseLibraryXML(File libraryXML) throws Exception {
		LibraryXMLParser parser = new LibraryXMLParser();
		return parser.parseXML(libraryXML.getAbsolutePath());
	}

	/**
	 * Parse a given a library.xml to find out the features it offers and store
	 * the proper set of source file paths for the current platform and target
	 * 
	 * @param libraryXML
	 *            library.xml file, cannot be null
	 * @param allowBackwardCompatibility
	 *            true if it's parsing library.xml from an extension JAR
	 * @return hashtable that contains feature id's and the paths for the source
	 *         files, or null if (1) library.xml is malformed, (2) if
	 *         allowBackwardCompatibility is false, and <target>,
	 *         <configuration>, <platform> or <src> is not specified correctly
	 * @throws Exception
	 */
	private Hashtable<String, ExtensionInfo> parseFeaturesInLibraryXML(
			File libraryXML, boolean allowBackwardCompatibility) throws Exception {
		Library lib = parseLibraryXML(libraryXML);
		
		if (lib == null) {
			return null;
		}		
		
		ArrayList<WidgetFeature> features = lib.getFeatures();
		HashSet<String> paths = new HashSet<String>();		
		Hashtable<String, ExtensionInfo> availableFeatures = new Hashtable<String, ExtensionInfo>();		
		
		if (allowBackwardCompatibility) {
			// have to work for library.xml that doesn't contain configuration and platform elements
			File[] files = _temporaryDirectory.listFiles();
			
			for (File f : files) {
				paths.add(f.getAbsolutePath());
			}				
		} else {
			ExtensionInfo info = new ExtensionInfo(lib, paths);
			boolean extensionIdFound = false;
			
			if (lib.getExtension() != null) {
				Extension extension = lib.getExtension();
				String id = extension.getId();

				if (id != null && !id.isEmpty()) {
					if (_extensionLookupTable.contains(id)) {
						// more than one library.xml contain the same extension id
						Logger.logMessage(LogType.WARNING,
								"VALIDATION_EXTENSION_DEFINED_MORE_THAN_ONCE",
								new String[] { id });
					}
							
					_extensionLookupTable.put(id, info);
					extensionIdFound = true;
				}
			}
			
			if (!extensionIdFound) {
				// not considered an error, this extension might not be used
				// by the app being compiled
				Logger.logMessage(LogType.WARNING,
						"VALIDATION_LIBRARYXML_EXTENSION_ID_NOT_DEFINED",
						new String[] { libraryXML.getAbsolutePath() });
			}			
			
			Configuration config = getTargetConfiguration(lib);
			
			if (config == null) {
                Logger.logMessage(LogType.WARNING, "VALIDATION_LIBRARYXML_NO_CONFIG",
                        new String[] { libraryXML.getAbsolutePath() });
                return null;
			}

			ArrayList<Src> src = config.getSrc();

			if (src == null || src.isEmpty()) {
                Logger.logMessage(LogType.WARNING, "VALIDATION_LIBRARYXML_NO_SRC",
                		new String[] { libraryXML.getAbsolutePath() });
                return null;
			}

			File extensionDir = libraryXML.getParentFile();

			for (Src s : src) {
				String path = s.getPath();
				paths.add(extensionDir.getAbsolutePath() + File.separator
						+ path);
			}
		}

		ExtensionInfo info = new ExtensionInfo(lib, paths);
		
		for (WidgetFeature feature : features) {
			availableFeatures.put(feature.getID(), info);
		}
		
		if (lib.getCompiledJARDependencies() != null) {
			for (Jar j : lib.getCompiledJARDependencies()) {
				String path = j.getPath();
				File temp = new File(path);
				
				if (temp.isAbsolute()) {
					info.addCompiledJARPath(path);
				} else {
					info.addCompiledJARPath(libraryXML.getParentFile()
							.getAbsolutePath()
							+ File.separator + path);
				}
			}
		}
		
		return availableFeatures;
	}

	private static String getExtensionPath() {
		return SessionManager.getInstance().getSourceFolder() + File.separator
				+ EXTENSION_DIRECTORY + File.separator;
	}

	private static String getTempExtensionPath() {
		return SessionManager.getInstance().getSourceFolder()
				+ TEMPORARY_DIRECTORY + File.separator;
	}

	private void copyExtensionPathsToSourceDir(HashSet<String> paths) {
		_curPaths.clear();

		for (String path : paths) {
			File file = new File(path);

			if (file.exists()) {
				try {
					copyFiles(file, new File(getExtensionPath()
							+ file.getName()));
				} catch (IOException e) {
                    Logger.logMessage(LogType.ERROR, "EXCEPTION_IO_COPY_FILES",
                            new String[] {file.getAbsolutePath(), e.getMessage()});
				}
			}
		}
	}

	/**
	 * This method copies file recursively from src to dest
	 */
	private void copyFiles(File src, File dest) throws IOException {
		// Check to ensure that the source is valid
		if (!src.exists()) {
			throw new IOException("copyFiles: Cannot find source: "
					+ src.getAbsolutePath() + ".");
		} else if (!src.canRead()) {
			// check to ensure we have rights to the source
			throw new IOException("copyFiles: No right to read source: "
					+ src.getAbsolutePath() + ".");
		}

		// is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException(
							"copyFiles: Could not create direcotry: "
									+ dest.getAbsolutePath() + ".");
				}
			}

			String list[] = src.list();
			// copy all the files in the list.
			for (String path : list) {
				File dest1 = new File(dest, path);
				File src1 = new File(src, path);
				copyFiles(src1, dest1);
			}
		} else {
			// This was not a directory, just copy the file
			try {
				// open the files for input and output
				FileManager.copyFile(src, dest);
				_curPaths.add(dest.getAbsolutePath());
			} catch (IOException e) { // Error copying file
				IOException wrapper = new IOException(
						"copyFiles: Unable to copy file: "
								+ src.getAbsolutePath() + " to "
								+ dest.getAbsolutePath() + ".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			}
		}
	}
}

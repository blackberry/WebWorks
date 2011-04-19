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

import java.util.ArrayList;

import net.rim.tumbler.config.WidgetFeature;

/**
 * Stores information parsed from library.xml
 */
public class Library {
	private Extension             _extension;
	private String                _entryClass;
	private ArrayList<Platform>      _platforms;
	private ArrayList<Configuration> _configurations;
	private ArrayList<WidgetFeature> _features;	
	private ArrayList<Extension>     _dependencies;
	private ArrayList<Jar>           _compiledJARDependencies;
	
	/**
	 * <extension> element in library.xml<br>
	 * Can be under <library> or <dependencies> 
	 */
	public static class Extension {
		private String _id;
		
		public Extension(String id) {
			_id = id;
		}
		
		public String getId() {
			return _id;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Extension{id: ").append(_id)
					.append("}");
			return buf.toString();
		}
	}
	
	/**
	 * <jar> element in library.xml<br>
	 */
	public static class Jar {
		private String _path;
		
		public Jar(String path) {
			_path = path;
		}
		
		public String getPath() {
			return _path;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Jar{path: ").append(_path)
					.append("}");
			return buf.toString();
		}		
	}
	
	/**
	 * <platform> element in library.xml
	 */
	public static class Platform {
		private String            _value;
		private ArrayList<Target>    _targets;
		
		public Platform(String value) {
			_value = value;
		}
		
		public String getValue() {
			return _value;
		}
		
		public void addTarget(Target target) {
			if (_targets == null) {
				_targets = new ArrayList<Target>();
			}
			
			_targets.add(target);
		}
		
		public ArrayList<Target> getTargets() {
			return _targets;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Platform{value: ").append(
					_value).append(", targets: ").append(_targets).append("}");
			return buf.toString();
		}		
	}
	
	/**
	 * <configuration> element in library.xml
	 */
	public static class Configuration {
		private String            _name;
		private String            _version;
		private ArrayList<Src>       _src;
		
		public Configuration(String name, String version) {
			_name = name;
			_version = version;
		}

		public void addSrc(Src src) {
			if (_src == null) {
				_src = new ArrayList<Src>();
			}

			_src.add(src);
		}
		
		public String getName() {
			return _name;
		}
		
		public String getVersion() {
			return _version;
		}
		
		public ArrayList<Src> getSrc() {
			return _src;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Configuration{name: ")
					.append(_name).append(", version: ").append(_version)
					.append(", src:").append(_src).append("}");
			return buf.toString();
		}		
	}
	
	/**
	 * <src> element in library.xml
	 */
	public static class Src {
		private String            _type;
		private String            _path;
		
		public Src(String type, String path) {
			_type = type;
			_path = path;
		}
		
		public String getType() {
			return _type;
		}
		
		public String getPath() {
			return _path;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Src{type: ").append(_type)
					.append(", path: ").append(_path).append("}");
			return buf.toString();
		}				
	}
	
	/**
	 * <target> element in library.xml
	 */
	public static class Target {
		private String            _version;
		private String            _configName;
		
		public Target(String version, String configName) {
			_version = version;
			_configName = configName;
		}
		
		public String getVersion() {
			return _version;
		}
		
		public String getConfigName() {
			return _configName;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("Target{version: ").append(
					_version).append(", configName: ").append(_configName)
					.append("}");
			return buf.toString();
		}		
	}

	/**
	 * @param e
	 *            <extension> tag found under <library>
	 */
	public void setExtension(Extension e) {
		_extension = e;
	}
	
	/**
	 * @return <extension> tag found under <library>
	 */
	public Extension getExtension() {
		return _extension;
	}
	
	/**
	 * @param entryClass
	 *            entry class of the extension
	 */
	public void setEntryClass(String entryClass) {
		_entryClass = entryClass;
	}
	
	/**
	 * @return entry class of the extension
	 */
	public String getEntryClass() {
		return _entryClass;
	}
	
	/**
	 * @param e
	 *            <extension> found under <dependencies>
	 */
	public void addDependency(Extension e) {
		if (_dependencies == null) {
			_dependencies = new ArrayList<Extension>();
		}

		_dependencies.add(e);
	}
	
	/**
	 * @return <extension> tags found under <dependencies>
	 */
	public ArrayList<Extension> getDependencies() {
		return _dependencies;
	}
	
	/**
	 * @param j
	 *            <jar> found under <dependencies>
	 */
	public void addJarDependency(Jar j) {
		if (_compiledJARDependencies == null) {
			_compiledJARDependencies = new ArrayList<Jar>();
		}
		
		_compiledJARDependencies.add(j);
	}
	
	/**
	 * @return <jar> tags found under <dependencies>
	 */
	public ArrayList<Jar> getCompiledJARDependencies() {
		return _compiledJARDependencies;
	}
	
	/**
	 * @param p
	 *            <platform> tag
	 */
	public void addPlatform(Platform p) {
		if (_platforms == null) {
			_platforms = new ArrayList<Platform>();
		}

		_platforms.add(p);
	}
	
	/**
	 * @return <platform> tags
	 */
	public ArrayList<Platform> getPlatforms() {
		return _platforms;
	}
	
	/**
	 * @param config
	 *            <configuration> tag
	 */
	public void addConfiguration(Configuration config) {
		if (_configurations == null) {
			_configurations = new ArrayList<Configuration>();
		}
		
		_configurations.add(config);
	}
	
	/**
	 * @return <configuration> tags
	 */
	public ArrayList<Configuration> getConfigurations() {
		return _configurations;
	}
	
	/**
	 * @param feature
	 *            <feature> tag
	 */
	public void addFeature(WidgetFeature feature) {
		if (_features == null) {
			_features = new ArrayList<WidgetFeature>();
		}
		
		_features.add(feature);
	}
	
	/**
	 * @return <feature> tags
	 */
	public ArrayList<WidgetFeature> getFeatures() {
		return _features;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("{extension: ")
				.append(_extension).append(", entryClass: ")
				.append(_entryClass).append(", dependencies: ").append(
						_dependencies).append(", platforms: ").append(
						_platforms).append(", configurations: ").append(
						_configurations).append(", features: ").append(
						_features).append("}");
		return buf.toString();
	}
}

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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;

import net.rim.tumbler.config.FeatureManager.ExtensionInfo;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.file.Library.Extension;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;


/**
 * Logic for resolving dependencies for extensions
 */
public class ExtensionDependencyManager {
	private Hashtable<String, ExtensionInfo> _extensionLookupTable;	
	
	private Deque<String>                    _inProgressStack;
	private HashSet<String>                  _resolvedDependencies;	
	
	/**
	 * Constructor
	 * 
	 * @param extensionLookupTable
	 *            map with extension library info and paths indexed by extension
	 *            id, cannot be null
	 */
	public ExtensionDependencyManager(
			Hashtable<String, ExtensionInfo> extensionLookupTable) {
		_extensionLookupTable = extensionLookupTable;
		_inProgressStack = new ArrayDeque<String>();
		_resolvedDependencies = new HashSet<String>();
	}

	private void resolve(HashSet<String> extensions) throws Exception {
		for (String extId : extensions) {
			Library info = null;
			
			if (_extensionLookupTable.get(extId) != null) {
				info = _extensionLookupTable.get(extId).getLibrary();
			}
			
			if (info != null) {
				_inProgressStack.push(extId);
				_resolvedDependencies.add(extId);								
				
				ArrayList<Extension> dependencies = info.getDependencies();
				
				if (dependencies != null && !dependencies.isEmpty()) {
					HashSet<String> deps = new HashSet<String>();
					
					for (Extension e : dependencies) {
						if (!_inProgressStack.contains(e.getId())) {
							deps.add(e.getId());
						} else {
							throw new PackageException("EXCEPTION_CIRCULAR_DEPENDENCY", e.getId());
						}
					}
					
					resolve(deps);
				}
				
				_inProgressStack.pop();
			} else {
				throw new PackageException("EXCEPTION_EXTENSION_NOT_FOUND", extId);
			}			
		}
	}
	
	/**
	 * Resolve dependencies recursively<br>
	 * Base case: extension does not have any dependencies<br>
	 * <br>
	 * 
	 * Push an extension on the stack at the start of the resolve process. The
	 * extension is popped off the stack when all its dependencies have been
	 * resolved.
	 * 
	 * @param extensions
	 * @throws Exception
	 *             when a circular dependency is detected, or if an extension
	 *             listed as a dependency cannot be found in the lookup table
	 */
	public HashSet<String> resolveExtensions(HashSet<String> extensions)
			throws Exception {
		resolve(extensions);
		return _resolvedDependencies;
	}
}

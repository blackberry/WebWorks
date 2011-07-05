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

package blackberry.web.widget.settings;

import java.util.Hashtable;

import blackberry.common.settings.SettingsStoreFactory;

import net.rim.device.api.util.Persistable;


/**
 * Create a unique class under the auto-generated namespace.
 * This permits the data to be deleted once the app is removed from  
 * the persistent store.
 */
class SettingsStore extends Hashtable implements Persistable
{
    
    
}

/**
 * Use a factory to jump across the auto-generated namespace 
 * and the apis that have a static name
 */
public class SettingsStoreFactoryImpl implements SettingsStoreFactory
{
    /**
     * @see blackberry.common.settings.SettingsStoreFactory#createStorableObject()
     */
    public Hashtable createStorableObject() {
        return new SettingsStore();
    }
}
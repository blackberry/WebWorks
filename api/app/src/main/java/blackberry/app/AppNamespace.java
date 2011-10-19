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
package blackberry.app;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.system.Application;
import net.rim.device.api.web.WidgetConfig;

/**
 * blackberry.app namespace
 */
public final class AppNamespace extends Scriptable {

    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_AUTHOREMAIL = "authorEmail";
    public static final String FIELD_AUTHORURL = "authorURL";
    public static final String FIELD_COPYRIGHT = "copyright";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ID = "id";
    public static final String FIELD_ISFOREGROUND = "isForeground";
    public static final String FIELD_LICENSE = "license";
    public static final String FIELD_LICENSEURL = "licenseURL";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_VERSION = "version";

    private Hashtable _fields;
    private WeakReference _weakReferenceBrowserField;

    /**
     * Constructs AppNamespace object.
     * 
     * @param browserField
     *            The {@link BrowserField}
     * @param config
     *            The {@link WidgetConfig}
     */
    public AppNamespace( BrowserField browserField, WidgetConfig config ) {
        _weakReferenceBrowserField = new WeakReference(browserField);;
        _fields = new Hashtable();
        _fields.put( SetHomeScreenNameFunction.NAME, new SetHomeScreenNameFunction() );
        _fields.put( SetHomeScreenIconFunction.NAME, new SetHomeScreenIconFunction( (BrowserField)_weakReferenceBrowserField.get()) );
        _fields.put( RequestForegroundFunction.NAME, new RequestForegroundFunction() );
        _fields.put( RequestBackgroundFunction.NAME, new RequestBackgroundFunction() );
        _fields.put( ExitFunction.NAME, new ExitFunction() );
        _fields.put( ShowBannerIndicatorFunction.NAME, new ShowBannerIndicatorFunction() );
        _fields.put( RemoveBannerIndicatorFunction.NAME, new RemoveBannerIndicatorFunction() );

        String author = config.getAuthor();
        if( author != null ) {
            _fields.put( FIELD_AUTHOR, author );
        }
        String email = config.getAuthorEmail();
        if( email != null ) {
            _fields.put( FIELD_AUTHOREMAIL, email );
        }
        String url = config.getAuthorURL();
        if( url != null ) {
            _fields.put( FIELD_AUTHORURL, url );
        }
        String copyright = config.getCopyright();
        if( copyright != null ) {
            _fields.put( FIELD_COPYRIGHT, copyright );
        }
        String description = config.getDescription();
        if( description != null ) {
            _fields.put( FIELD_DESCRIPTION, description );
        }
        String license = config.getLicense();
        if( license != null ) {
            _fields.put( FIELD_LICENSE, license );
        }
        String licenseUrl = config.getLicenseURL();
        if( licenseUrl != null ) {
            _fields.put( FIELD_LICENSEURL, licenseUrl );
        }
        String name = config.getName();
        if( name != null ) {
            _fields.put( FIELD_NAME, name );
        }
        String version = config.getVersion();
        if( version != null ) {
            _fields.put( FIELD_VERSION, version );
        }
    }

    /**
     * @see net.rim.device.api.script.Scriptable#getField(String)
     */
    public Object getField( String fieldName ) throws Exception {
        if( fieldName.equals( FIELD_ISFOREGROUND ) ) {
            return new Boolean( Application.getApplication().isForeground() );
        } else {
            Object field = _fields.get( fieldName );
            if( field == null ) {
                return super.getField( fieldName );
            }
            return field;
        }
    }
}

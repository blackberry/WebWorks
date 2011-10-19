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

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

import blackberry.app.event.AppEventNamespace;

/**
 * Implementation of blackberry.app extension.
 */
public class AppExtension implements WidgetExtension {

    public static final String FEATURE_APP = "blackberry.app";
    public static final String FEATURE_APP_EVENT = "blackberry.app.event";

    private WidgetConfig _widgetConfig;
    private WeakReference _weakReferenceBrowserField;
    private AppNamespaceHandler _namespaceHandler;
    private String _currentDocUri;

    /**
     * Constructs a AppExtension object.
     */
    public AppExtension() {
        _namespaceHandler = new AppNamespaceHandler();
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { FEATURE_APP, FEATURE_APP_EVENT };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(String, String, Document, ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine ) throws Exception {

        // Reset the feature handler when a new doc is detected
        if( _currentDocUri == null ) {
            _currentDocUri = document.getDocumentURI();
        } else if( document != null && !document.getDocumentURI().equals( _currentDocUri ) ) {
            _namespaceHandler.resetFeatures();
            _currentDocUri = document.getDocumentURI();
        }

        if( feature.equals( FEATURE_APP ) ) {
            _namespaceHandler.setNamespace( new AppNamespace( (BrowserField)_weakReferenceBrowserField.get(), _widgetConfig ) );
            scriptEngine.addExtension( FEATURE_APP, _namespaceHandler );
        } else if( feature.equals( FEATURE_APP_EVENT ) ) {
            _namespaceHandler.setNamespace( new AppEventNamespace() );
            // Add it under the blackberry.app namespace as a field
            scriptEngine.addExtension( FEATURE_APP, _namespaceHandler );
        }
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
        _widgetConfig = widgetConfig;
        _weakReferenceBrowserField = new WeakReference(browserField);
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
        // Reset feature handler when page is done
        _namespaceHandler.resetFeatures();
    }

    private static class AppNamespaceHandler extends Scriptable {

        Scriptable _appNamespace;
        AppEventNamespace _eventNamespace;

        void setNamespace( AppEventNamespace namespace ) {
            _eventNamespace = namespace;
        }

        void setNamespace( AppNamespace namespace ) {
            _appNamespace = namespace;
        }

        public Object getField( String fieldName ) throws Exception {
            if( fieldName.equals( AppEventNamespace.NAME ) ) {
                return _eventNamespace != null ? _eventNamespace : super.getField( fieldName );
            } else if( _appNamespace != null ) {
                return _appNamespace.getField( fieldName );
            }
            return super.getField( fieldName );
        }

        /**
         * Reset the feature hash.
         */
        public void resetFeatures() {
            _appNamespace = null;

            if( _eventNamespace != null ) {
                try {
                    _eventNamespace.unload();
                } catch( Exception e ) {
                }
            }
            _eventNamespace = null;
        }
    }

}

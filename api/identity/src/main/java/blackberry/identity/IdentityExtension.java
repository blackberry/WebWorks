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
package blackberry.identity;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import blackberry.identity.phone.PhoneNamespace;

import org.w3c.dom.Document;

/**
 * This is a main entry class implements WidgetExtension
 * 
 * @author sgolod
 * 
 */
public class IdentityExtension implements WidgetExtension {

    public static final String FEATURE_IDENTITY = "blackberry.identity";
    public static final String FEATURE_IDENTITY_PHONE = "blackberry.identity.phone";

    private IdentityNamespaceHandler _namespaceHandler;
    private String _currentDocUri;

    /**
     * Constructor that initializes the identity namespace handler
     */
    public IdentityExtension() {
        _namespaceHandler = new IdentityNamespaceHandler();
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { FEATURE_IDENTITY, FEATURE_IDENTITY_PHONE };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(java.lang.String, java.lang.String, org.w3c.dom.Document,
     *      net.rim.device.api.script.ScriptEngine)
     */
    public void loadFeature( final String feature, final String version, final Document doc, final ScriptEngine scriptEngine )
            throws Exception {

        // Reset the feature handler when a new doc is detected
        if( _currentDocUri == null ) {
            _currentDocUri = doc.getDocumentURI();
        } else if( doc != null && !doc.getDocumentURI().equals( _currentDocUri ) ) {
            _namespaceHandler.resetFeatures();
            _currentDocUri = doc.getDocumentURI();
        }

        // Load features into the script engine
        if( feature.equals( FEATURE_IDENTITY ) ) {
            IdentityNamespace idNamespace = new IdentityNamespace();
            _namespaceHandler.setNamespace( idNamespace );
            scriptEngine.addExtension( FEATURE_IDENTITY, _namespaceHandler );
        } else if( feature.equals( FEATURE_IDENTITY_PHONE ) ) {
            // Load the phone namespace as a field of identity
            _namespaceHandler.setNamespace( PhoneNamespace.getInstance() );
            scriptEngine.addExtension( FEATURE_IDENTITY, _namespaceHandler );
        }
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(net.rim.device.api.web.WidgetConfig,
     *      net.rim.device.api.browser.field2.BrowserField)
     */
    public void register( final WidgetConfig widgetConfig, final BrowserField browserField ) {
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(org.w3c.dom.Document)
     */
    public void unloadFeatures( final Document doc ) {
    }

    /**
     * Handles features under blackberry.identity
     */
    private static class IdentityNamespaceHandler extends Scriptable {

        Scriptable _identityNamespace;
        Scriptable _phoneNamespace;

        void setNamespace( IdentityNamespace namespace ) {
            _identityNamespace = namespace;
        }

        void setNamespace( PhoneNamespace namespace ) {
            _phoneNamespace = namespace;
        }

        public Object getField( String fieldName ) throws Exception {
            if( fieldName.equals( PhoneNamespace.NAME ) ) {
                return _phoneNamespace != null ? _phoneNamespace : super.getField( fieldName );
            } else if( _identityNamespace != null ) {
                return _identityNamespace.getField( fieldName );
            }
            return super.getField( fieldName );
        }

        /**
         * Reset the feature hash.
         */
        public void resetFeatures() {
            _identityNamespace = null;
            _phoneNamespace = null;
        }
    }

}

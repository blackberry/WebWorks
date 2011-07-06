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
package blackberry.messaging;

import java.util.Hashtable;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

/**
 * Implementation of blackberry.message extension
 */
public class MessagingExtension implements WidgetExtension {

    public static final String FEATURE_NAME = "blackberry.message";
    private static final String _messageFeatureNamePrefix = FEATURE_NAME + ".";
    public static final String FEATURE_NAME_SMS = "sms";

    public static WidgetConfig _widgetConfig = null;
    private static Scriptable _smsNamespace = null;
    private final MessageFeaturesHandler _messageFeaturesHandler;
    private String _currentDocUri;

    /**
     * Default constructor.
     */
    public MessagingExtension() {
        _messageFeaturesHandler = new MessageFeaturesHandler();
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        /**
         * There is a namespace collision between this messaging extension and the sms message extension. Let this extension
         * manage the loading of the sms extension
         */
        return new String[] { FEATURE_NAME, getFormattedFeatureName( "sms" ) };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(String, String, Document, ScriptEngine)
     */
    public void loadFeature( String feature, String version, Document doc, ScriptEngine scriptEngine ) throws Exception {

        // Reset the feature handler when a new doc is detected
        if( _currentDocUri == null ) {
            _currentDocUri = doc.getDocumentURI();
        } else if( doc != null && !doc.getDocumentURI().equals( _currentDocUri ) ) {
            _messageFeaturesHandler.resetFeatures();
            _currentDocUri = doc.getDocumentURI();
        }

        if( feature.equals( FEATURE_NAME ) ) {
            _messageFeaturesHandler.addFeature( feature, new MessagingNamespace() );
        } else if( feature.equals( getFormattedFeatureName( FEATURE_NAME_SMS ) ) ) {
            Class cl = Class.forName( "blackberry.message.sms.SMSNamespace" );
            Scriptable smsNamespace = (Scriptable) cl.newInstance();
            _messageFeaturesHandler.addFeature( feature, smsNamespace );
        }

        scriptEngine.addExtension( FEATURE_NAME, _messageFeaturesHandler );

    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(WidgetConfig, BrowserField)
     */
    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
        MessagingExtension._widgetConfig = widgetConfig;
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(Document)
     */
    public void unloadFeatures( Document doc ) {
        // do nothing
    }

    /**
     * @return the namespace for the message.sms extension
     */
    public static Scriptable getSmsNamespace() {
        return _smsNamespace;
    }

    private static String getFormattedFeatureName( final String featureName ) {
        return _messageFeatureNamePrefix + featureName;

    }

    /**
     * This class would handle name of the features who are the superset of blackberry.message (i.e blackberry.message.sms)
     */
    private static class MessageFeaturesHandler extends Scriptable {

        Hashtable _features = new Hashtable();

        private void addFeature( final String featureName, final Scriptable featureImpl ) {
            _features.put( featureName, featureImpl );
        }

        /**
         * @see net.rim.device.api.script.Scriptable#getField(java.lang.String)
         */
        public Object getField( final String fieldName ) throws Exception {

            Scriptable featureImplementation = (Scriptable) _features.get( getFormattedFeatureName( fieldName ) );

            if( featureImplementation != null ) {
                return featureImplementation;
            } else {
                featureImplementation = (Scriptable) _features.get( FEATURE_NAME );

                if( featureImplementation != null ) {
                    return featureImplementation.getField( fieldName );
                }
            }

            return super.getField( fieldName );
        }

        /**
         * Reset the feature hash.
         */
        public void resetFeatures() {
            _features.clear();
        }
    }

}

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
package blackberry.invoke;

import java.util.Hashtable;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

import blackberry.invoke.addressBookArguments.AddressBookArgumentsConstructor;
import blackberry.invoke.browserArguments.BrowserArgumentsConstructor;
import blackberry.invoke.calendarArguments.CalendarArgumentsConstructor;
import blackberry.invoke.cameraArguments.CameraArgumentsConstructor;
import blackberry.invoke.javaArguments.JavaArgumentsConstructor;
import blackberry.invoke.mapsArguments.MapsArgumentsConstructor;
import blackberry.invoke.memoArguments.MemoArgumentsConstructor;
import blackberry.invoke.messageArguments.MessageArgumentsConstructor;
import blackberry.invoke.phoneArguments.PhoneArgumentsConstructor;
import blackberry.invoke.searchArguments.SearchArgumentsConstructor;
import blackberry.invoke.taskArguments.TaskArgumentsConstructor;

/**
 * This class implements WidgetExtension and serves as an entry point.
 */
public class InvokeExtension implements WidgetExtension {

    private final InvokeFeaturesHandler invokeFeaturesHandler;

    private String _currentDocUri;

    private static final String invokeFeatureNamePrefix = InvokeNamespace.NAME + ".";

    /**
     * Default constructor.
     */
    public InvokeExtension() {
        invokeFeaturesHandler = new InvokeFeaturesHandler();
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { InvokeNamespace.NAME, getFormattedFeatureName( AddressBookArgumentsConstructor.NAME ),
                getFormattedFeatureName( BrowserArgumentsConstructor.NAME ),
                getFormattedFeatureName( CalendarArgumentsConstructor.NAME ),
                getFormattedFeatureName( CameraArgumentsConstructor.NAME ),
                getFormattedFeatureName( JavaArgumentsConstructor.NAME ),
                getFormattedFeatureName( MapsArgumentsConstructor.NAME ),
                getFormattedFeatureName( MemoArgumentsConstructor.NAME ),
                getFormattedFeatureName( PhoneArgumentsConstructor.NAME ),
                getFormattedFeatureName( SearchArgumentsConstructor.NAME ),
                getFormattedFeatureName( TaskArgumentsConstructor.NAME ),
                getFormattedFeatureName( MessageArgumentsConstructor.NAME ) };
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
            invokeFeaturesHandler.resetFeatures();
            _currentDocUri = doc.getDocumentURI();
        }

        if( feature.equals( InvokeNamespace.NAME ) ) {
            invokeFeaturesHandler.addFeature( feature, new InvokeNamespace() );
        } else if( feature.equals( getFormattedFeatureName( AddressBookArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new AddressBookArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( BrowserArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new BrowserArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( CalendarArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new CalendarArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( CameraArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new CameraArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( JavaArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new JavaArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( MapsArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new MapsArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( MemoArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new MemoArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( PhoneArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new PhoneArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( SearchArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new SearchArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( TaskArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new TaskArgumentsConstructor() );
        } else if( feature.equals( getFormattedFeatureName( MessageArgumentsConstructor.NAME ) ) ) {
            invokeFeaturesHandler.addFeature( feature, new MessageArgumentsConstructor() );
        }

        scriptEngine.addExtension( InvokeNamespace.NAME, invokeFeaturesHandler );
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
        // Reset features when page is done
        invokeFeaturesHandler.resetFeatures();
    }

    private static String getFormattedFeatureName( final String featureName ) {
        return invokeFeatureNamePrefix + featureName;

    }

    /**
     * This class would handle name of the features who are the superset of blackberry.invoke (i.e
     * blackberry.invoke.CalendarArguments) What happens in OS 6.x is getField method of blackberry.invoke is called with
     * parameter CalendarArguments.
     */
    private static class InvokeFeaturesHandler extends Scriptable {

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
                featureImplementation = (Scriptable) _features.get( InvokeNamespace.NAME );

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

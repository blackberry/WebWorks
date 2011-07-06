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
package blackberry.web.widget.bf;

import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html2.HTMLDocument;

import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.web.WidgetFeature;

import blackberry.web.widget.device.DeviceInfo;
import blackberry.web.widget.policy.WidgetPolicy;
import blackberry.web.widget.policy.WidgetPolicyFactory;

/**
 *
 */
public class WidgetBrowserFieldListener extends BrowserFieldListener {

    private WidgetConfig _widgetConfig;
    private WidgetPolicy _widgetPolicy;
    private WidgetAccess[] _accessList;

    public WidgetBrowserFieldListener( WidgetConfig wc ) {
        _widgetConfig = wc;
        _widgetPolicy = WidgetPolicyFactory.getPolicy();
        _accessList = _widgetConfig.getAccessList();
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener
     */
    public void documentCreated( BrowserField browserField, ScriptEngine scriptEngine, Document document ) throws Exception {
        // Load features for the current URL of the BrowserField.
        WidgetAccess access = _widgetPolicy.getElement( document.getDocumentURI(), _accessList );
        if( access != null && access.getFeatures() != null ) {
            loadFeatures( access.getFeatures(), document, scriptEngine );
        }

        if( browserField.getScreen() instanceof BrowserFieldScreen ) {
            BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();

            // Push the loading screen at this time so that history.back() will use loading screens
            if( !bfScreen.getPageManager().isFirstLaunch() && !bfScreen.getPageManager().isSuppressingLoadingScreen()
                    && bfScreen.getPageManager().isLoadingScreenRequired( document.getDocumentURI() ) ) {
                bfScreen.getPageManager().showLoadingScreen();
                Thread.yield();
                Thread.sleep( 100 );
            }

            /*
             * Fix the issue where the screen becomes blank after clicking the link in the frame. Only reset the flag(s) for the
             * root document.
             */
            if( browserField.getDocument() == document ) {
                bfScreen.getPageManager().clearFlags();
            }

            // For naviagtion mode, add blackberry.focus namespace to JavaScriptExtension.
            if( bfScreen.getAppNavigationMode() && document instanceof HTMLDocument ) {
                // Add our JS navigation extension to JavaScript Engine.
                if( scriptEngine != null ) {
                    scriptEngine.addExtension( NavigationNamespace.NAME, bfScreen.getNavigationExtension() );
                }
                bfScreen.getNavigationController().reset();
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener
     */
    public void documentUnloading( BrowserField browserField, Document document ) throws Exception {
        if( browserField.getScreen() instanceof BrowserFieldScreen ) {
            BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();

            // For navigation mode, reset the navigation map when the original document is unloading.
            if( bfScreen.getAppNavigationMode() && browserField.getDocument() == null ) {
                bfScreen.getNavigationController().reset();
            }
        }
    }

    // Override other methods ? documentAborted, documentError, documentLoaded, documentProgress.
    // Synchronized to ensure features are loaded properly
    private synchronized void loadFeatures( WidgetFeature[] features, Document doc, ScriptEngine scriptEngine ) {
        int fSize = features.length;
        WidgetFeature feature = null;
        WidgetExtension extension = null;
        for( int i = 0; i < fSize; i++ ) {
            feature = features[ i ];
            extension = _widgetConfig.getExtensionForFeature( feature.getID() );
            if( extension != null ) {
                try {
                    extension.loadFeature( feature.getID(), feature.getVersion(), doc, scriptEngine );
                } catch( Exception x ) {
                    // ignore feature
                }
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener
     */
    public void documentLoaded( BrowserField browserField, Document document ) throws Exception {
        /*
         * Fix the issue "click the link in the frame, the screen becomes blank" Only reset the flag(s) for the root document.
         */
        if( browserField.getDocument() == document ) {
            if( browserField.getScreen() instanceof BrowserFieldScreen ) {
                BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();
            }

            // For navigation mode.
            if( browserField.getScreen() instanceof BrowserFieldScreen ) {
                BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();

                if( bfScreen.getAppNavigationMode() && browserField.getDocument() == document ) {
                    bfScreen.getNavigationController().update();

                    // Add Layout update event listener.
                    if( document instanceof EventTarget ) {
                        EventTarget target = (EventTarget) document;
                        EventListener listener = new UpdateBinsEventListener( browserField );
                        target.addEventListener( "DOMNodeInserted", listener, false );
                        target.addEventListener( "DOMNodeRemoved", listener, false );
                    }
                }
            }

            // Pop the loading screeen if it is displayed.
            if( browserField.getScreen() instanceof BrowserFieldScreen ) {
                BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();
                if( bfScreen.getPageManager().isLoadingScreenDisplayed() ) {
                    bfScreen.getPageManager().hideLoadingScreen();
                }
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener The downloadProgress event will be fired for links to
     *      non-document URIs in our case, it should function both documentCreated handler and documentLoaded handler
     */
    public void downloadProgress( BrowserField browserField, ContentReadEvent event ) throws Exception {
        if( browserField.getScreen() instanceof BrowserFieldScreen ) {
            BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();

            // Clear the flags set during the documentCreated event
            bfScreen.getPageManager().clearFlags();

            // Clear the flags set during the documentCreated event for OS versions prior to 6.0
            if( event.getItemsRead() == event.getItemsToRead() && !DeviceInfo.isBlackBerry6() ) {
                if( bfScreen.getPageManager().isLoadingScreenDisplayed() ) {
                    bfScreen.getPageManager().hideLoadingScreen();
                }
            }
        }
    }

    private static class UpdateBinsEventListener implements EventListener {
        private BrowserField _browserField;

        UpdateBinsEventListener( BrowserField browserField ) {
            super();
            _browserField = browserField;
        }

        public void handleEvent( Event evt ) {
            if( _browserField.getScreen() instanceof BrowserFieldScreen ) {
                BrowserFieldScreen bfScreen = (BrowserFieldScreen) _browserField.getScreen();

                if( bfScreen.getAppNavigationMode() ) {
                    bfScreen.getNavigationController().update();
                }
            }
        }
    }

}

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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.web.WidgetFeature;

import blackberry.core.ScriptableFunctionWrapper;
import blackberry.core.ScriptableWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html2.HTMLDocument;

import blackberry.common.util.JSUtilities;
import blackberry.core.IJSExtension;
import blackberry.web.widget.device.DeviceInfo;
import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.policy.WidgetPolicy;
import blackberry.web.widget.policy.WidgetPolicyFactory;

/**
 *
 */
public class WidgetBrowserFieldListener extends BrowserFieldListener {

    /**
     * A proxy object for the ScriptEngine that will wrap all Scriptable extension in a unique instanced object. This ensures any
     * singleton instances will not be prematurely removed when the browser does the cleanup.
     */
    private static class ProxyScriptEngine implements ScriptEngine {

        private ScriptEngine _scriptEngine;

        /**
         * Wrap the script engine and proxy all the methods to the real object
         * 
         * @param scriptEngine the script engine to proxy
         */
        public ProxyScriptEngine( ScriptEngine scriptEngine ) {
            _scriptEngine = scriptEngine;
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.rim.device.api.script.ScriptEngine#addExtension(java.lang.String, java.lang.Object)
         */
        public void addExtension( String name, Object ext ) throws Exception {
            /* 
             * When the browser cleans up extensions of a previous page. It does not check to see 
             * if the current page is using that extension too. It will unload multiple instances as well.
             */
            if( ext instanceof ScriptableFunction ) {
                _scriptEngine.addExtension( name, new ScriptableFunctionWrapper( (ScriptableFunction) ext ) );
            } else if( ext instanceof Scriptable ) {
                _scriptEngine.addExtension( name, new ScriptableWrapper( (Scriptable) ext ) );
            } else {
                _scriptEngine.addExtension( name, ext );
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see net.rim.device.api.script.ScriptEngine#compileScript(java.lang.String)
         */
        public Object compileScript( String script ) throws IllegalArgumentException {
            return _scriptEngine.compileScript( script );
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.rim.device.api.script.ScriptEngine#executeCompiledScript(java.lang.Object, java.lang.Object)
         */
        public Object executeCompiledScript( Object arg0, Object arg1 ) throws IllegalStateException, IllegalArgumentException,
                RuntimeException {
            return _scriptEngine.executeCompiledScript( arg0, arg1 );
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.rim.device.api.script.ScriptEngine#executeScript(java.lang.String, java.lang.Object)
         */
        public Object executeScript( String arg0, Object arg1 ) throws IllegalStateException, IllegalArgumentException,
                RuntimeException {
            return _scriptEngine.executeScript( arg0, arg1 );
        }
    }

    private WidgetConfig _widgetConfig;
    private WidgetPolicy _widgetPolicy;
    private WidgetAccess[] _accessList;
    private WidgetAccess _prevAccess;
    private ScriptEngine _scriptEngine = null;
    
    public WidgetBrowserFieldListener( WidgetConfig wc ) {
        _widgetConfig = wc;
        _widgetPolicy = WidgetPolicyFactory.getPolicy();
        _accessList = _widgetConfig.getAccessList();
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener
     */
    public void documentCreated( BrowserField browserField, ScriptEngine scriptEngine, Document document ) throws Exception {
        // Clean up extensions that were loaded from previous request
        if( _prevAccess != null ) {
            unloadFeatures( _prevAccess.getFeatures() );
            _prevAccess = null;
        }

        _scriptEngine = null;

        ProxyScriptEngine proxyScriptEngine = null;
        if( scriptEngine != null ) {
            // Clobber the troublesome "blackberry" namespace as it won't free some extensions, but keep a backup
            scriptEngine.executeScript( "(function () { if ( typeof( this.blackberry ) != 'undefined' ) { this.__blackberry__ = this.blackberry; delete this.blackberry; } } )(); ", null );

            // Create proxy for the script engine that will ensure unique namespaces being added as extensions
            proxyScriptEngine = new ProxyScriptEngine( scriptEngine );
            
            // Inject global scripts - must be done before the ScriptableObjects get injected
            injectJavaScript( proxyScriptEngine );
            
            _scriptEngine = proxyScriptEngine;
        }

        // Load features for the current URL of the BrowserField.
        WidgetAccess access = _widgetPolicy.getElement( document.getDocumentURI(), _accessList );
        if( access != null && access.getFeatures() != null ) {
            loadFeatures( access.getFeatures(), document, proxyScriptEngine );
            _prevAccess = access;
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

            // For navigation mode, add blackberry.focus namespace to JavaScriptExtension.
            if( bfScreen.getAppNavigationMode() && document instanceof HTMLDocument ) {
                // Add our JS navigation extension to JavaScript Engine.
                if( proxyScriptEngine != null ) {
                    proxyScriptEngine.addExtension( NavigationNamespace.NAME, bfScreen.getNavigationExtension() );
                    // Load navmode.js into script engine, must be done after blackberry.focus namespace is defined
                    bfScreen.getNavigationJS().loadFeature( null, null, null, scriptEngine );                  
                }
                bfScreen.getNavigationController().reset();
            }

            // Inject HTML5 to gears shim for 5.0 devices\r
            if( scriptEngine != null && DeviceInfo.isBlackBerry5() ) {
                WidgetExtension HTML5Extension = bfScreen.getHTML5Extension();
                HTML5Extension.loadFeature( null, null, null, proxyScriptEngine );
            }
            
            if( scriptEngine != null ) {
                // Restore any clobbered members.  
                // Note: blackberry.network does not show up when enumerated. Add it manually.
                scriptEngine.executeScript( "(function () { if ( typeof( this.__blackberry__ ) != 'undefined' ) { for ( var name in this.__blackberry__ ) { this.blackberry[name] = this.__blackberry__[name]; } this.blackberry.network = this.__blackberry__.network; delete this.__blackberry__; } } )(); ", null );
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
        Object extension = null;
        Hashtable jsExtensionsByFeatureId = new Hashtable();
        // Go through the list of all features, store all JS extension in a map first
        for( int i = 0; i < fSize; i++ ) {
            feature = features[ i ];
            extension = ( (WidgetConfigImpl) _widgetConfig ).getExtensionObjectForFeature( feature.getID() );
            if( extension instanceof IJSExtension ) {
                jsExtensionsByFeatureId.put( feature.getID(), extension );
            }
        }

        // Sort by feature id so that the JS gets loaded in the right order
        if( !jsExtensionsByFeatureId.isEmpty() ) {
            SimpleSortingVector featureIds = new SimpleSortingVector();
            Enumeration keys = jsExtensionsByFeatureId.keys();
            while( keys.hasMoreElements() ) {
                featureIds.addElement( keys.nextElement() );
            }

            featureIds.setSortComparator( JSUtilities.getStringComparator() );
            featureIds.reSort();

            Enumeration sortedFeatureIds = featureIds.elements();
            while( sortedFeatureIds.hasMoreElements() ) {
                IJSExtension jsExtension = (IJSExtension) jsExtensionsByFeatureId.get( sortedFeatureIds.nextElement() );
                jsExtension.loadFeature( feature.getID(), feature.getVersion(), doc, scriptEngine, ( (WidgetConfigImpl) _widgetConfig ).getJSInjectionPaths() );
            }
        }

        // Load widget extensions after all JS extensions are loaded
        for( int i = 0; i < fSize; i++ ) {
            feature = features[ i ];
            extension = ( (WidgetConfigImpl) _widgetConfig ).getExtensionObjectForFeature( feature.getID() );
            if( extension != null && extension instanceof WidgetExtension ) {
                try {
                    ( (WidgetExtension) extension ).loadFeature( feature.getID(), feature.getVersion(), doc, scriptEngine );
                } catch( Exception x ) {
                    // ignore feature
                }
            }
        }       
    }

    private synchronized void unloadFeatures( WidgetFeature[] features ) {
        int fSize = features.length;
        WidgetFeature feature = null;
        Object extension = null;

        for( int i = 0; i < fSize; i++ ) {
            feature = features[ i ];
            extension = ( (WidgetConfigImpl) _widgetConfig ).getExtensionObjectForFeature( feature.getID() );
            if( extension instanceof IJSExtension ) {
                ( (IJSExtension) extension ).unloadFeatures();
            } else if( extension instanceof WidgetExtension ) {
                ( (WidgetExtension) extension ).unloadFeatures( null );
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldListener
     */
    public void documentLoaded( BrowserField browserField, Document document ) throws Exception {
        
        if( _scriptEngine != null ) {
            // Inform the js framework that we are ready to process requests
            try {
                _scriptEngine.executeScript( "try { var event = document.createEvent(\"Event\"); "
                        + " event.initEvent(\"frameworkready\", true, true); " 
                        + " window.dispatchEvent(event); } catch (e) { } ", null );
            } catch( Exception e ) {
            }
        }
        
        /*
         * Fix the issue "click the link in the frame, the screen becomes blank" Only reset the flag(s) for the root document.
         */
        if( browserField.getDocument() == document ) {
            // For navigation mode.
//            if( browserField.getScreen() instanceof BrowserFieldScreen ) {
//                BrowserFieldScreen bfScreen = (BrowserFieldScreen) browserField.getScreen();
//
//                if( bfScreen.getAppNavigationMode() && browserField.getDocument() == document ) {
//                    bfScreen.getNavigationController().update();
//
//                    // Add Layout update event listener.
//                    if( document instanceof EventTarget ) {
//                        EventTarget target = (EventTarget) document;
//                        EventListener listener = new UpdateBinsEventListener( browserField );
//                        target.addEventListener( "DOMNodeInserted", listener, false );
//                        target.addEventListener( "DOMNodeRemoved", listener, false );
//                    }
//                }
//            }

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

    private void injectJavaScript( ScriptEngine scriptEngine ) {
        try {
            if( _widgetConfig instanceof WidgetConfigImpl ) {
                WidgetConfigImpl wConfigImpl = (WidgetConfigImpl) _widgetConfig;

                // shared global JS files
                SimpleSortingVector sharedGlobalJSPaths = wConfigImpl.getSharedGlobalJSInjectionPaths();
                sharedGlobalJSPaths.setSortComparator( JSUtilities.getStringComparator() );
                // sort to ensure JS is loaded in correct order
                sharedGlobalJSPaths.reSort();
                Enumeration sharedGlobalJSPathElems = sharedGlobalJSPaths.elements();
                while( sharedGlobalJSPathElems.hasMoreElements() ) {
                    String jsPath = getValidPath( (String) sharedGlobalJSPathElems.nextElement() );
                    Object compiledScript = scriptEngine.compileScript( readJSContent( jsPath ) );
                    scriptEngine.executeCompiledScript( compiledScript, null );
                }
            }
        } catch( Exception e ) {
            System.out.println( "Error Injection: " + e.getMessage() );
        }
    }

    private String getValidPath( String jsPath ) {
        String SLASH_FWD = "/";

        if( !jsPath.startsWith( SLASH_FWD ) ) {
            return SLASH_FWD + jsPath;
        }

        return jsPath;
    }

    private String readJSContent( String jsURI ) {
        String jsContent = "";
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream( jsURI );
            byte[] data = IOUtilities.streamToBytes( is );
            jsContent = new String( data );
        } catch( Exception e ) {
        } finally {
            try {
                if( is != null ) {
                    is.close();
                    is = null;
                }
            } catch( IOException e ) {
            }
        }
        return jsContent;
    }

//    private static class UpdateBinsEventListener implements EventListener {
//        private WeakReference _browserFieldWeakReference;
//
//        UpdateBinsEventListener( BrowserField browserField ) {
//            super();
//            _browserFieldWeakReference = new WeakReference( browserField );
//        }
//
//        private BrowserField getBrowserField() {
//            Object o = _browserFieldWeakReference.get();
//            if( o instanceof BrowserField ) {
//                return (BrowserField) o;
//            } else {
//                return null;
//            }
//        }
//
//        public void handleEvent( Event evt ) {
//            Screen screen = getBrowserField().getScreen();
//            if( screen instanceof BrowserFieldScreen ) {
//                BrowserFieldScreen bfScreen = (BrowserFieldScreen) screen;
//
//                if( bfScreen.getAppNavigationMode() ) {
//                    bfScreen.getNavigationController().update();
//                }
//            }
//        }
//    }

}

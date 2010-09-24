/*
* Copyright 2010 Research In Motion Limited.
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
package blackberry.web.widget.loadingScreen;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import org.w3c.dom.Document;

import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.container.MainScreen;

import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.loadingScreen.LoadingScreen;
import blackberry.web.widget.loadingScreen.TransitionConstants;
import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.util.WidgetUtil;

public class PageManager {
    
    private WidgetConfigImpl _widgetConfigImpl;
    private UiApplication _app;
    
    private BrowserFieldScreen _screenBrowserField;
    private LoadingScreen _screenLoadingScreen;
    
    private boolean _firstLaunch;
    private boolean _suppressLoadingScreen;
    private boolean _isGoingBackSafe;
    
    private static final String PROTOCOL_LOCAL = "local";
    private static final String PROTOCOL_DATA  = "data";
    private static final String PROTOCOL_FILE  = "file";
    private static final String PROTOCOL_COD   = "cod";
    private static final String PROTOCOL_HTTP  = "http";
    private static final String PROTOCOL_HTTPS = "https";

    public PageManager(UiApplication app, WidgetConfigImpl widgetConfigImpl) {
        _app = app;
        _widgetConfigImpl = widgetConfigImpl;
        
        _firstLaunch = true;
        _suppressLoadingScreen = false;
        _isGoingBackSafe = true;
    }
    
    public boolean isLoadingScreenRequired(String url) {
        String protocol = getProtocol(url);
        
        if (isLocalPageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_LOCAL)) {
            return true;
        }
        
        if (isLocalPageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_DATA)) {
            return true;
        }

        if (isLocalPageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_FILE)) {
            return true;
        }

        if (isLocalPageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_COD)) {
            return true;
        }

        if (isRemotePageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_HTTP)) {
            return true;
        }

        if (isRemotePageLoadRequired() && protocol.equalsIgnoreCase(PROTOCOL_HTTPS)) {
            return true;
        }
        
        return false;
    }
    
    public boolean isFirstPageLoadRequired() {
        return _widgetConfigImpl.getFirstPageLoad();        
    }
        
    private boolean isRemotePageLoadRequired() {
        return _widgetConfigImpl.getRemotePageLoad();
    }

    private boolean isLocalPageLoadRequired() {
        return _widgetConfigImpl.getLocalPageLoad();
    }
    
    public void pushScreens(BrowserFieldScreen screenBF2) {
        _screenBrowserField = screenBF2;
        _screenLoadingScreen = new LoadingScreen(_widgetConfigImpl, this);
        
        _app.pushScreen(_screenBrowserField);
        
        if (isFirstPageLoadRequired()) {
            _app.pushScreen(_screenLoadingScreen);
        }
        
        setTransition();
    }
    
    public boolean isFirstLaunch() {
        return _firstLaunch;
    }
    
    public void clearFirstLaunch() {
        _firstLaunch = false;
    }
    
    public boolean isSuppressingLoadingScreen() {
        return _suppressLoadingScreen;
    }    
    
    public void setSuppressLoadingScreen() {
        _suppressLoadingScreen = true;
    }
    
    public void unsuppressLoadingScreen() {
        _suppressLoadingScreen = false;
    }
    
    public void setGoingBackSafe(boolean value) {
        _isGoingBackSafe = value;
    }
    
    public boolean isGoingBackSafe() {
        return _isGoingBackSafe;
    }

    public void clearFlags() {
        unsuppressLoadingScreen();
        setGoingBackSafe(true);
        clearFirstLaunch();
    }
    
    public boolean isRedirectableNavigation(String protocol) {
        return (protocol.equalsIgnoreCase(PROTOCOL_HTTP) || protocol.equalsIgnoreCase(PROTOCOL_HTTPS));
    }   

    public boolean isLocalTextHtml(String contentType) {
        return (contentType != null && contentType.equalsIgnoreCase("text/html"));
    }   

    private void setTransition() {
        if (_widgetConfigImpl.getTransitionType() != TransitionConstants.TRANSITION_NONE) {

            TransitionContext transitionContext = null;

            switch (_widgetConfigImpl.getTransitionType()) {
                case TransitionConstants.TRANSITION_SLIDEPUSH: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
                    break;
                }
                case TransitionConstants.TRANSITION_SLIDEOVER: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
                    break;
                }
                case TransitionConstants.TRANSITION_FADEIN: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_FADE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
                    break;
                }
                case TransitionConstants.TRANSITION_FADEOUT: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_FADE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
                    break;
                }
                case TransitionConstants.TRANSITION_WIPEIN: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_WIPE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
                    break;
                }
                case TransitionConstants.TRANSITION_WIPEOUT: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_WIPE);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
                    break;
                }
                case TransitionConstants.TRANSITION_ZOOMIN: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
                    break;
                }
                case TransitionConstants.TRANSITION_ZOOMOUT: {
                    transitionContext = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
                    transitionContext.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
                    break;
                }                
            }
    
            if (transitionContext != null) {
                transitionContext.setIntAttribute(TransitionContext.ATTR_DURATION, _widgetConfigImpl.getTransitionDuration());
                transitionContext.setIntAttribute(TransitionContext.ATTR_DIRECTION, _widgetConfigImpl.getTransitionDirection());    
                
                UiEngineInstance engine = Ui.getUiEngineInstance();
                engine.setTransition(_screenBrowserField, _screenLoadingScreen, UiEngineInstance.TRIGGER_PUSH, transitionContext);
            }
        }
    }
    
    public void showLoadingScreen() {
        _app.invokeLater(new Runnable(){ 
            public void run() {
                synchronized(_app.getAppEventLock()){
                    if (_app.getActiveScreen() != _screenLoadingScreen) {
                        _app.pushScreen(_screenLoadingScreen);
                    }
                }
            }
        });
    }
    
    public void hideLoadingScreen() {
        _app.invokeLater(new Runnable(){ 
            public void run() {
                synchronized(_app.getAppEventLock()){
                    /* Removed the condition checking for "active screen" of the application,
                    *  since it's not right if there is a "modal" dialog,
                    *  because the application will release event lock for "modal" dialog,
                    *  in order to proceed next events.
                    *  So we always pop out _screenLoadingScreen no matter what,
                    *  and catch the exception if _screenLoadingScreen was already popped out.
                    */
                    try {
                        _app.popScreen(_screenLoadingScreen);
                    } catch (Exception e) {
                        // Exception is thrown if your screen is not on the stack, which is ok.
                    }
                }
            }
        });
    }
    
    public boolean isLoadingScreenDisplayed() {
        return (_app.getActiveScreen() == _screenLoadingScreen);
    }
    
    public void cancelNewPage() {
        if (_screenBrowserField.getWidgetBrowserField().getHistory().canGoBack()) {
            setSuppressLoadingScreen();
            _screenBrowserField.getWidgetBrowserField().back();
            hideLoadingScreen();
        }
    }
    
    private String getProtocol(String url) {
        if (url != null) {
            int index = url.indexOf(":");
            if (index > 0) {
                return url.substring(0, index).trim();
            }
        }
        return null;
    }
}

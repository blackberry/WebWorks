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
package blackberry.web.widget.bf;

import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.impl.WidgetException;
import blackberry.web.widget.policy.WidgetPolicy;
import blackberry.web.widget.policy.WidgetPolicyFactory;
import blackberry.web.widget.exception.MediaHandledException;

import javax.microedition.io.InputConnection;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldConnectionManager;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.URI;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.browser.Browser;

import javax.microedition.io.HttpConnection;
import java.io.InputStream;


/**
 * 
 */
public class WidgetRequestController extends ProtocolController {
    private WidgetConfig                    _widgetConfig;
    private WidgetPolicy                    _widgetPolicy;
    private boolean                         _hasMultiAccess;
    private BrowserField                    _browserField;
    
    /**
     * Constructor.
     */
    public WidgetRequestController(BrowserField bf, WidgetConfig config) {
        super(bf);
        _widgetConfig = config;
        _widgetPolicy = WidgetPolicyFactory.getPolicy();  
        
        // Set BF handle.
        _browserField = bf;        
        
        // Support for *
        if (_widgetConfig instanceof WidgetConfigImpl) {     
            _hasMultiAccess = ((WidgetConfigImpl) _widgetConfig).allowMultiAccess(); 
        }
        
        
    }
    
    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldController
     */
    public void handleNavigationRequest(BrowserFieldRequest request) throws Exception {
        WidgetAccess access = _widgetPolicy.getElement(request.getURL(), _widgetConfig.getAccessList());
        if (access == null && !_hasMultiAccess) {
            throw new WidgetException(WidgetException.ERROR_WHITELIST_FAIL, request.getURL());
        }
        
        // Determine the MIME type of the url
        String contentType = MIMETypeAssociations.getMIMEType(request.getURL());
        
        // Normalize and strip off parameters.
        String normalizedContentType = MIMETypeAssociations.getNormalizedType(contentType);

        // Determine protocol
        String protocol = request.getProtocol();
              
        // Launch the browser if BF2 cannot handle the mime type        
        if(openWithBrowser(normalizedContentType, protocol, request.getURL())){
            
            invokeBrowser(request.getURL());
                        
                        // Throw a special type of exception that will prevent the history from being updated
                        throw new MediaHandledException();
        }
        else{
            BrowserFieldScreen bfScreen = ((BrowserFieldScreen)_browserField.getScreen());

            bfScreen.getPageManager().setGoingBackSafe(false);
                        
            // Push the loading screeen before sending the new request, so BrowserField screen still contains original page for transition
            if (!bfScreen.getPageManager().isFirstLaunch() && !bfScreen.getPageManager().isSuppressingLoadingScreen() && bfScreen.getPageManager().isLoadingScreenRequired(request.getURL())) {
                bfScreen.getPageManager().showLoadingScreen();
                Thread.yield();
                Thread.sleep(100);
            }
            
            try {
                if (bfScreen.getCacheManager() != null && bfScreen.getCacheManager().isRequestCacheable(request)) {
                    InputConnection ic = null;
                    if (bfScreen.getCacheManager().hasCache(request.getURL()) && !bfScreen.getCacheManager().hasCacheExpired(request.getURL())) {
                        ic = bfScreen.getCacheManager().getCache(request.getURL());
                    } else {
                        ic = _browserField.getConnectionManager().makeRequest(request);
                        if (ic instanceof HttpConnection) {
                            HttpConnection response = (HttpConnection) ic;
                            if (bfScreen.getCacheManager().isResponseCacheable(response)) {
                                ic = bfScreen.getCacheManager().createCache(request.getURL(), response);
                            }
                        }
                    }
                    _browserField.displayContent(ic, request.getURL());
                } else {
                    super.handleNavigationRequest(request);
                }

                if (!bfScreen.getPageManager().isRedirectableNavigation(protocol)) {
                    // The navigation won't redirect
                    bfScreen.getPageManager().clearFlags();

                    if (!bfScreen.getPageManager().isLocalTextHtml(normalizedContentType)) {
                        if (bfScreen.getPageManager().isLoadingScreenDisplayed()) {
                            bfScreen.getPageManager().hideLoadingScreen();
                        }
                    }
                } 
                
            } catch (Exception e) {
                if (bfScreen.getPageManager().isLoadingScreenDisplayed()) {
                    bfScreen.getPageManager().hideLoadingScreen();
                }
                
                bfScreen.getPageManager().clearFlags();
                
                // Rethrow the Exception
                throw e;
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldController
     */    
    public InputConnection handleResourceRequest(BrowserFieldRequest request) throws Exception {
        WidgetAccess access = _widgetPolicy.getElement(request.getURL(), _widgetConfig.getAccessList());
        if (access == null && !_hasMultiAccess) {
            throw new WidgetException(WidgetException.ERROR_WHITELIST_FAIL, request.getURL());
        }
        
        BrowserFieldScreen bfScreen = ((BrowserFieldScreen)_browserField.getScreen());
        if (bfScreen.getCacheManager() != null && bfScreen.getCacheManager().isRequestCacheable(request)) {
            InputConnection ic = null;
            if (bfScreen.getCacheManager().hasCache(request.getURL()) && !bfScreen.getCacheManager().hasCacheExpired(request.getURL())) {
                ic = bfScreen.getCacheManager().getCache(request.getURL());
            } else {
                ic = super.handleResourceRequest(request);
                if (ic instanceof HttpConnection) {
                    HttpConnection response = (HttpConnection) ic;
                    if (bfScreen.getCacheManager().isResponseCacheable(response)) {
                        ic = bfScreen.getCacheManager().createCache(request.getURL(), response);
                    }
                }
            }
            return ic;
        } else {
            return super.handleResourceRequest(request);
        }
    }
    
    // Method to check if the browser needs to be launched to handle the file.
    private boolean openWithBrowser(String mimeType, String protocol, String url)
    {
        if(mimeType != null){
            
            // Determine media type.
            int mediaType = MIMETypeAssociations.getMediaTypeFromMIMEType( mimeType );
            
            // Allow all local media to be handled by BF2.
            // Even if it is not supported by BF2 yet.
            if(protocol.equalsIgnoreCase("local")){                           
                    return false;
            }
            
            // List of types we don't want BF2 to handle.
            if( (mediaType == MIMETypeAssociations.MEDIA_TYPE_AUDIO)
            || (mediaType == MIMETypeAssociations.MEDIA_TYPE_VIDEO)
            || (mediaType == MIMETypeAssociations.MEDIA_TYPE_PLAY_LIST)
            || (mediaType == MIMETypeAssociations.MEDIA_TYPE_APPLICATION)
            || (mediaType == MIMETypeAssociations.MEDIA_TYPE_UNKNOWN) ){
                return true;
            }                        
        }
        
        // If the type was null, check the file extension for .zip or .exe
        // Mark .zip and .exe to be unsupported by BF2 so that the browser will be launched
        // Local files must be opened by BF2 all the time since the browser has no access to those internal files
        if(checkFileExtension(url) && !protocol.equalsIgnoreCase("local")){
            return true;
        }
        else{
            return false;        
        }
    }        
    
    // Checks the file extension for special types.
    private boolean checkFileExtension(String url){       
        
        boolean isMarked = false;
        
        // Check for .zip        
        if(url.endsWith(".zip")){
           isMarked = true;
        }
        // Check for .exe
        else if(url.endsWith(".exe")){
           isMarked = true;
        }
        // Check for .wpd
        else if(url.endsWith(".wpd")){
           isMarked = true;
        }
        
        // Return the value.  False means it is not a special extension
        return isMarked;
    }
    
    
    // Invokes the browser on the given URL    
    private void invokeBrowser(String url){
            BrowserSession bs = null;
            bs = Browser.getDefaultSession();
            bs.displayPage(url);
    }
}

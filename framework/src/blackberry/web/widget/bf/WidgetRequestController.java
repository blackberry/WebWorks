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

import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResponse;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.io.URI;
import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;
import blackberry.common.util.StringUtilities;
import blackberry.core.IJSExtension;
import blackberry.core.JSExtensionRequest;
import blackberry.core.JSExtensionResponse;
import blackberry.web.widget.MemoryMaid;
import blackberry.web.widget.auth.Authenticator;
import blackberry.web.widget.device.DeviceInfo;
import blackberry.web.widget.exception.MediaHandledException;
import blackberry.web.widget.impl.WidgetConfigImpl;
import blackberry.web.widget.impl.WidgetException;
import blackberry.web.widget.policy.WidgetPolicy;
import blackberry.web.widget.policy.WidgetPolicyFactory;
import blackberry.web.widget.bf.HTTPResponseStatus;

/**
 * 
 */
public class WidgetRequestController extends ProtocolController {
    private WidgetConfig _widgetConfig;
    private WidgetPolicy _widgetPolicy;
    private boolean _hasMultiAccess;
    private BrowserField _browserField;

    /**
     * Constructor.
     */
    public WidgetRequestController( BrowserField bf, WidgetConfig config ) {
        super( bf );
        _widgetConfig = config;
        _widgetPolicy = WidgetPolicyFactory.getPolicy();

        // Set BF handle.
        _browserField = bf;

        // Support for *
        if( _widgetConfig instanceof WidgetConfigImpl ) {
            _hasMultiAccess = ( (WidgetConfigImpl) _widgetConfig ).allowMultiAccess();
        }

    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldController
     */

    public void handleNavigationRequest( BrowserFieldRequest request ) throws Exception {
        WidgetAccess access = _widgetPolicy.getElement( request.getURL(), _widgetConfig.getAccessList() );
        if( access == null && !_hasMultiAccess ) {
            if( !DeviceInfo.isBlackBerry6() ) {
                _browserField.getHistory().go( -1 );
            }
            throw new WidgetException( WidgetException.ERROR_WHITELIST_FAIL, request.getURL() );

        }

        BrowserFieldScreen bfScreen = ( (BrowserFieldScreen) _browserField.getScreen() );

        // Launch the browser if BF2 cannot handle the mime type
        if( !openWithBrowser( request ) ) {
            // Determine the MIME type of the url
            String contentType = MIMETypeAssociations.getMIMEType( request.getURL() );

            // Normalize and strip off parameters.
            String normalizedContentType = MIMETypeAssociations.getNormalizedType( contentType );

            // Determine protocol
            String protocol = request.getProtocol();

            bfScreen.getPageManager().setGoingBackSafe( false );
            InputConnection ic = null;
            try {
                if( bfScreen.getCacheManager() != null && bfScreen.getCacheManager().isRequestCacheable( request ) ) {
                    if( bfScreen.getCacheManager().hasCache( request.getURL() )
                            && !bfScreen.getCacheManager().hasCacheExpired( request.getURL() ) ) {
                        ic = bfScreen.getCacheManager().getCache( request.getURL() );
                    } else {
                        ic = _browserField.getConnectionManager().makeRequest( request );
                        if( ic instanceof HttpConnection ) {
                            HttpConnection response = (HttpConnection) ic;
                            if( bfScreen.getCacheManager().isResponseCacheable( response ) ) {
                                ic = bfScreen.getCacheManager().createCache( request.getURL(), response );

                            }
                        }
                    }

                    ic = processAuthentication( ic, request );

                    _browserField.displayContent( ic, request.getURL() );
                } else {

                    // Check whether authentication is required
                    if( isHttpProtocol( request ) ) {
                        // Only HTTP/HTTPS can use the API to receive the response
                        ic = _browserField.getConnectionManager().makeRequest( request );
                        ic = processAuthentication( ic, request );

                        _browserField.displayContent( ic, request.getURL() );
                    } else {
                        super.handleNavigationRequest( request );
                    }

                }

                if( !bfScreen.getPageManager().isRedirectableNavigation( protocol ) ) {
                    // The navigation won't redirect
                    bfScreen.getPageManager().clearFlags();

                    if( !bfScreen.getPageManager().isLocalTextHtml( normalizedContentType ) ) {
                        if( bfScreen.getPageManager().isLoadingScreenDisplayed() ) {
                            bfScreen.getPageManager().hideLoadingScreen();
                        }
                    }
                }

            } catch( Exception e ) {

                bfScreen.getPageManager().hideLoadingScreen();
                bfScreen.getPageManager().clearFlags();

                // Rethrow the Exception
                throw e;
            }
        }
		
        MemoryMaid mm = MemoryMaid.getInstance();
        if( mm != null ) {
            if( mm.isAlive() ) {
                mm.flagGC();
            } else {
                // Start the memory manager after our first page has been loaded
                mm.start();
            }
        }
    }

    /**
     * @see net.rim.device.api.browser.field2.BrowserFieldController
     */
    public InputConnection handleResourceRequest( BrowserFieldRequest request ) throws Exception {

        if( this._browserField == null ) {
            return new HTTPResponseStatus( HTTPResponseStatus.SC_SERVER_ERROR, request ).getResponse();
        }
        if( request.getURL().startsWith( "http://localhost:8472/" ) ) {
            URI requestURI = URI.create( request.getURL() );
            String[] splitPath = StringUtilities.split( requestURI.getPath(), "/" );
            String featureID = "";
            for( int i = 0; i < splitPath.length - 1; i++ ) {
                if( featureID == "" ) {
                    featureID = featureID + splitPath[ i ];
                } else {
                    featureID = featureID + "." + splitPath[ i ];
                }
            }
            Object ext = ( (WidgetConfigImpl) _widgetConfig ).getExtensionObjectForFeature( featureID );
            if( ext != null && ext instanceof IJSExtension ) {
                JSExtensionRequest req = new JSExtensionRequest( request.getURL(), request.getPostData(), request.getHeaders(),
                        ( (WidgetConfigImpl) _widgetConfig ).getFeatureTable() );
                JSExtensionResponse res = new JSExtensionResponse( request.getURL(), null, request.getHeaders() );
                try {
                    ( (IJSExtension) ext ).invoke( req, res );
                    return new BrowserFieldResponse( res.getURL(), res.getPostData(), res.getHeaders() );
                } catch( net.rim.device.api.web.WidgetException e ) {
                    // this block is reached if the method cannot be found within the extension
                    return new HTTPResponseStatus( HTTPResponseStatus.SC_NOT_IMPLEMENTED, request ).getResponse();
                }
            } else {
                if( ext == null ) {
                    return new HTTPResponseStatus( HTTPResponseStatus.SC_NOT_FOUND, request ).getResponse();
                } else if( !( ext instanceof IJSExtension ) ) {
                    return new HTTPResponseStatus( HTTPResponseStatus.SC_NOT_IMPLEMENTED, request ).getResponse();
                }
            }
        }
        WidgetAccess access = _widgetPolicy.getElement( request.getURL(), _widgetConfig.getAccessList() );
        if( access == null && !_hasMultiAccess ) {
            throw new WidgetException( WidgetException.ERROR_WHITELIST_FAIL, request.getURL() );
        }

        // In this event, only rtsp link needs to open browser
        if( request.getProtocol().equalsIgnoreCase( "rtsp" ) ) {
            openWithBrowser( request );
            return null;
        }

        BrowserFieldScreen bfScreen = ( (BrowserFieldScreen) _browserField.getScreen() );
        InputConnection ic = null;
        if( bfScreen.getCacheManager() != null && bfScreen.getCacheManager().isRequestCacheable( request ) ) {
            if( bfScreen.getCacheManager().hasCache( request.getURL() )
                    && !bfScreen.getCacheManager().hasCacheExpired( request.getURL() ) ) {
                ic = bfScreen.getCacheManager().getCache( request.getURL() );
            } else {
                ic = super.handleResourceRequest( request );
                if( ic instanceof HttpConnection ) {
                    HttpConnection response = (HttpConnection) ic;
                    if( bfScreen.getCacheManager().isResponseCacheable( response ) ) {
                        ic = bfScreen.getCacheManager().createCache( request.getURL(), response );
                    }
                }
            }
        } else {
            ic = super.handleResourceRequest( request );
        }
        ic = processAuthentication( ic, request );
        return ic;
    }

    /**
     * Performs authentication checks and requests credentials when needed
     */
    private InputConnection processAuthentication( InputConnection ic, BrowserFieldRequest request ) throws Exception {

        // Check if Basic authentication is required to access the resource
        boolean authenticationIsNeeded = false;
        while( isAuthenticationNeeded( ic, request ) ) {
            authenticationIsNeeded = true;
            BrowserFieldRequest authenticationReq = Authenticator.getAuthenticationRequest( (HttpConnection) ic, request );
            if( authenticationReq == request ) {
                break;
            }
            ic = super.handleResourceRequest( authenticationReq );
        }

        // If credentials are needed and they are correct, set them verified
        if( authenticationIsNeeded && !isHTTPError( ic ) ) {
            Authenticator.verifyCredential( (HttpConnection) ic );
        }

        return ic;
    }

    /**
     * Check if authentication is needed by checking the response code.
     * 
     * @param icResponse
     * @param request
     * @return
     */
    private static boolean isAuthenticationNeeded( InputConnection icResponse, BrowserFieldRequest request ) {
        if( icResponse instanceof HttpConnection && isHttpProtocol( request ) ) {
            HttpConnection hcResponse = (HttpConnection) icResponse;
            try {
                int responseCode = hcResponse.getResponseCode();
                return HttpConnection.HTTP_UNAUTHORIZED == responseCode;
            } catch( IOException e ) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the request uses the HTTP protocol
     * 
     * @param request
     * @return
     */
    private static boolean isHttpProtocol( BrowserFieldRequest request ) {
        String protocol = request.getProtocol();
        return ( protocol.equalsIgnoreCase( "http" ) || protocol.equalsIgnoreCase( "https" ) );
    }

    /**
     * Check the connection for an error response code
     * 
     * @param ic
     * @return
     */
    private static boolean isHTTPError( InputConnection ic ) {
        if( ic instanceof HttpConnection ) {
            try {
                HttpConnection hcResponse = (HttpConnection) ic;
                int responseCode = hcResponse.getResponseCode();
                return ( responseCode >= 400 && responseCode < 600 );
            } catch( IOException e ) {
                return true;
            }
        } else {
            return true;
        }
    }

    // Method to check if the browser needs to be launched to handle the file.
    private boolean openWithBrowser( BrowserFieldRequest request ) throws Exception {
        BrowserFieldScreen bfScreen = ( (BrowserFieldScreen) _browserField.getScreen() );

        // Determine the MIME type of the url
        String contentType = MIMETypeAssociations.getMIMEType( request.getURL() );

        // Normalize and strip off parameters.
        String normalizedContentType = MIMETypeAssociations.getNormalizedType( contentType );

        // Determine protocol
        String protocol = request.getProtocol();

        // Launch the browser if BF2 cannot handle the mime type
        if( openWithBrowser( normalizedContentType, protocol, request.getURL() ) ) {

            invokeBrowser( request.getURL() );

            // Reset the loading screen flags to ensure that the back button is enabled after the invoke
            bfScreen.getPageManager().clearFlags();

            if( DeviceInfo.isBlackBerry6() ) {
                // Throw a special type of exception that will prevent the history from being updated
                throw new MediaHandledException();
            } else {
                // On 5.0 devices we can simply go back once to counter the history updating before this.
                _browserField.getHistory().go( -1 );
            }
            return true;
        } else {
            return false;
        }
    }

    // Method to check if the browser needs to be launched to handle the file.
    private boolean openWithBrowser( String mimeType, String protocol, String url ) {
        // rtsp links should open in the browser.  BF2 does not support the protocol
        if( protocol.equalsIgnoreCase( "rtsp" ) ) {
            return true;
        }

        if( mimeType != null ) {

            // Determine media type.
            int mediaType = MIMETypeAssociations.getMediaTypeFromMIMEType( mimeType );

            // Allow all local media to be handled by BF2.
            // Even if it is not supported by BF2 yet.
            if( protocol.equalsIgnoreCase( "local" ) ) {
                return false;
            }

            // List of types we don't want BF2 to handle.
            if( ( mediaType == MIMETypeAssociations.MEDIA_TYPE_AUDIO ) || ( mediaType == MIMETypeAssociations.MEDIA_TYPE_VIDEO )
                    || ( mediaType == MIMETypeAssociations.MEDIA_TYPE_PLAY_LIST )
                    || ( mediaType == MIMETypeAssociations.MEDIA_TYPE_APPLICATION )
                    || ( mediaType == MIMETypeAssociations.MEDIA_TYPE_UNKNOWN ) ) {
                return true;
            }
        }

        // If the type was null, check the file extension for .zip or .exe
        // Mark .zip and .exe to be unsupported by BF2 so that the browser will
        // be launched
        // Local files must be opened by BF2 all the time since the browser has
        // no access to those internal files
        if( checkFileExtension( url ) && !protocol.equalsIgnoreCase( "local" ) ) {
            return true;
        } else {
            return false;
        }
    }

    // Checks the file extension for special types.
    private boolean checkFileExtension( String url ) {

        boolean isMarked = false;

        // Check for .zip
        if( url.endsWith( ".zip" ) ) {
            isMarked = true;
        }
        // Check for .exe
        else if( url.endsWith( ".exe" ) ) {
            isMarked = true;
        }
        // Check for .wpd
        else if( url.endsWith( ".wpd" ) ) {
            isMarked = true;
        }

        // Return the value. False means it is not a special extension
        return isMarked;
    }

    // Invokes the browser on the given URL
    private void invokeBrowser( String url ) {
        BrowserSession bs = null;
        bs = Browser.getDefaultSession();
        bs.displayPage( url );
    }
}

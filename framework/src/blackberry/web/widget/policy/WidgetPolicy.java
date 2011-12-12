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
package blackberry.web.widget.policy;

import java.util.Hashtable;

import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.web.WidgetAccess;

import blackberry.web.widget.util.WidgetUtil;

/**
 * 
 */
public class WidgetPolicy {

    private Hashtable _authorityCollection;
    private WidgetAccess _localAccess;

    /**
     * Apply widget security model to the requested URI to retrieve the corresponding <access> element from array.
     * http://www.w3.org/TR/2009/WD-widgets-access-20090618/#rfc3987
     * 
     * @param request
     *            The requested URI.
     * @param accessList
     *            WidgetAccess array to loop through to find matching request.
     * @return Return true if the request should be allowed based on WebWorks' config.xml; false otherwise.
     */
    public WidgetAccess getElement( String request, WidgetAccess[] accessList ) {
        try {
            URI requestURI = URI.create( request.trim() );
            // require absolute URI's
            if( requestURI.isAbsolute() ) {

                // Initialize authority collection if it does not yet exist
                initializeAuthCollection( accessList );

                // Start with the full authority path and check if a WidgetAccess set exists for that path
                // If it does not exist, remove the first section of the authority path and try again
                String authString = getAuthorityFromString( request );
                String schemeString = getSchemeFromString( request );

                // Check for an authority string that has an existing key
                // Special case: Allow file protocol to proceed without an authority
                // Special case: Allow local protocol which is always without an authority
                // Special case: Allow data protocol which is always without an authority (let isMatch handle it)
                authString = authorityCheck( schemeString, authString );
                if( authString.equals( "" ) && !( schemeString.equals( "file" ) || schemeString.equals( "local" ) || schemeString.equals( "data" ) ) ) {
                    return null;
                }

                WidgetWebFolderAccess folderAccess;
                WidgetAccess fetchedAccess = null;

                // Retrieve WidgetAccess set for the specified authority
                folderAccess = (WidgetWebFolderAccess) _authorityCollection.get( schemeString + "://" + authString );

                // Special case: no access element was found for a file protocol request.
                // This is added since file protocol was allowed through the above check
                if( schemeString.equals( "file" ) && folderAccess == null ) {
                    return null;
                }
                
                // If no access element is found with local URI, use local access for this request
                if ( schemeString.equals( "local" ) && folderAccess == null ) {
                    return _localAccess;
                }
                
                if(folderAccess != null) {
                    fetchedAccess = folderAccess.getWidgetAccess( requestURI.getPath() + parseNull( requestURI.getQuery() ) );
                }
                if( !isMatch( fetchedAccess, requestURI ) ) {
                    fetchedAccess = folderAccess.getWidgetAccess( requestURI.getPath() + "*" );
                }

                boolean failedToFindAccess = false;
                // Make sure we've got the right one
                while( fetchedAccess == null || !isMatch( fetchedAccess, requestURI ) ) {

                    // There was an auth url that matched, but didnt match the folder structure
                    // Try the next level up
                    authString = authString.substring( authString.indexOf( '.' ) + 1 );

                    // Check for an authority string that has an existing key
                    authString = authorityCheck( schemeString, authString );
                    if( authString.equals( "" ) ) {
                        failedToFindAccess = true;
                        break;
                    }

                    // Retrieve WidgetAccess set for the specified authority
                    folderAccess = (WidgetWebFolderAccess) _authorityCollection.get( schemeString + "://" + authString );

                    // Special case: no access element was found for a file protocol request.
                    // This is added since file protocol was allowed through the above check
                    if( schemeString.equals( "file" ) && folderAccess == null ) {
                        return null;
                    }

                    fetchedAccess = folderAccess.getWidgetAccess( requestURI.getPath() + parseNull( requestURI.getQuery() ) );
                }
                
                if( !failedToFindAccess ) {
                    return fetchedAccess;
                } else if ( isMatch( _localAccess, requestURI ) ) {
                	// If we cannot find a more specific access for this local URI, use local access
                	return _localAccess;
                }
            }
        } catch( MalformedURIException mue ) {
            // invalid request URI - return null
        }
        return null;
    }

    private boolean isMatch( WidgetAccess access, URI toMatchURI ) {
        if( access == null ) {
            return false;
        }

        // Look for local first
        if( WidgetUtil.isLocalURI( toMatchURI ) && access.isLocal() ) {
            // local access always allowed
            return true;
        }
        // Check for data url
        else if( WidgetUtil.isDataURI( toMatchURI ) ) {
            // data urls are allowed
            return true;
        } else if( access.isLocal() ) {
            return false;
        }

        // Based on widgets 1.0 (access control)
        // http://www.w3.org/TR/2009/WD-widgets-access-20090618/#rfc3987
        URI referenceURI = access.getURI();
        boolean allowSub = access.allowSubDomain();

        // Start comparison based on widgets spec.
        // 1. Compare scheme
        if( !referenceURI.getScheme().equalsIgnoreCase( toMatchURI.getScheme() ) ) {
            return false;
        }
        // 2. Compare host - if subdoman is false, host must match exactly
        // (referenceURI MUST HAVE host specified - not null.)
        String refHost = referenceURI.getHost();
        String matchHost = toMatchURI.getHost();
        if( matchHost == null ) {
            return false;
        }
        if( !allowSub && !( refHost.equalsIgnoreCase( matchHost ) ) ) {
            return false;
        }
        // 3. Compare host - if subdomain is true, check for subdomain or match
        if( allowSub && !matchHost.toLowerCase().endsWith( "." + refHost.toLowerCase() ) && !matchHost.equalsIgnoreCase( refHost ) ) {
            return false;
        }
        // 4. Compare port
        String refPort = parseNull( referenceURI.getPort() );
        String toMatchPort = parseNull( toMatchURI.getPort() );
        if( !refPort.equals( toMatchPort ) ) {
            return false;
        }
        // 5. Compare path+query
        String refPath = referenceURI.getPath() + parseNull( referenceURI.getQuery() );
        String toMatchPath = toMatchURI.getPath() + parseNull( toMatchURI.getQuery() );
        if( refPath.endsWith( "*" ) ) {
            refPath = refPath.substring( 0, refPath.length() - 1 );
        }
        if( !toMatchPath.startsWith( refPath ) ) {
            return false;
        }
        return true;
    }

    private String parseNull( String toParse ) {
        return toParse == null ? "" : toParse;
    }

    /**
     * Initalizes the collection of authority urls with their proper WidgetAccess elements
     * 
     * @param accessList
     *            List of WidgetAccess elements to add into the collection
     */
    private void initializeAuthCollection( WidgetAccess[] accessList ) {

        // Initialize collection if it does not yet exist
        if( _authorityCollection == null ) {
            _authorityCollection = new Hashtable();

            // Loop access elements and add them to the authority collection
            for( int i = 0; i < accessList.length; i++ ) {
                WidgetAccess currentAccess = accessList[ i ];
                URI currentURI = currentAccess.getURI();

                // Special case: local access does not go into the collection because it has no URI
                if( currentAccess.isLocal() ) {
                    _localAccess = currentAccess;
                } else {
                    WidgetWebFolderAccess folderAccess;

                    // Check the authority collection to see if the authority item we want already exists
                    if( _authorityCollection.containsKey( currentURI.getScheme() + "://" + currentURI.getAuthority() ) ) {
                        folderAccess = (WidgetWebFolderAccess) _authorityCollection.get( currentURI.getScheme() + "://"
                                + currentURI.getAuthority() );
                    } else {
                        // Create web folder access
                        folderAccess = new WidgetWebFolderAccess();
                    }

                    // Add folder path access to the authority item
                    folderAccess.addWidgetAccess( currentURI.getPath() + parseNull( currentURI.getQuery() ), currentAccess );
                    _authorityCollection.put( currentURI.getScheme() + "://" + currentURI.getAuthority(), folderAccess );

                }
            }
        }

    }

    /**
     * Retrieves the authority portion of a URL string
     * 
     * @param url
     *            URL to parse for authority
     * @return authority URL
     */
    private String getAuthorityFromString( String url ) {
        try {
            URI uriObject = URI.create( url );
            return uriObject.getAuthority();
        } catch( MalformedURIException mue ) {
            // invalid request URI - return null
            return null;
        }
    }

    /**
     * Retrieves the scheme portion of a URL string
     * 
     * @param url
     *            URL to parse for authority
     * @return scheme of the URL
     */
    private String getSchemeFromString( String url ) {
        try {
            URI uriObject = URI.create( url );
            return uriObject.getScheme();
        } catch( MalformedURIException mue ) {
            // invalid request URI - return null
            return null;
        }
    }

    /**
     * Process the given scheme and authority and returns an authority which exists in the authority collection
     * 
     * @param scheme
     *            Scheme of the request url
     * @param authString
     *            Authority of the request url
     * @return Authority which exists in the authority collection
     */
    private String authorityCheck( String scheme, String authString ) {
        if(authString == null) {
            authString = "";
        }

        boolean firstPass = true;
        while( !_authorityCollection.containsKey( scheme + "://" + authString ) ) {

            // If the authority is empty string, then no access element exists for that subdomain
            // Also, if the auth becomes a top level domain and is not found, then stop as well
            // First pass will allow computer names to be used
            if( authString.equals( "" ) || ( ( authString.indexOf( '.' ) == -1 ) && !firstPass ) ) {
                return "";
            }
            authString = authString.substring( authString.indexOf( '.' ) + 1 );

            // Set the flag
            if( firstPass ) {
                firstPass = false;
            }
        }
        return authString;
    }
}

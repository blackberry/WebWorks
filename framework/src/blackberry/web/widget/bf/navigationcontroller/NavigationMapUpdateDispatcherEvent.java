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
package blackberry.web.widget.bf.navigationcontroller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.XYRect;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NodeSelector;
import org.w3c.dom.html2.HTMLHeadElement;
import org.w3c.dom.html2.HTMLIFrameElement;
import org.w3c.dom.html2.HTMLInputElement;
import org.w3c.dom.html2.HTMLMetaElement;
import org.w3c.dom.html2.HTMLSelectElement;

import blackberry.core.threading.DispatchableEvent;

/**
 * NavigationMapUpdateDispatcherEvent
 */
class NavigationMapUpdateDispatcherEvent extends DispatchableEvent {
    
    private final NavigationController _navigationController;
    private boolean                    _pageLoaded;    

    NavigationMapUpdateDispatcherEvent( final NavigationController navigationController, 
            final NavigationController context, boolean pageLoaded ) {
        super( context );
        _navigationController = navigationController;
        _pageLoaded = pageLoaded;
        
    }

    // << DispatchableEvent >>
    protected void dispatch() {
        _navigationController._dom = _navigationController._browserField.getDocument();

        // Reset iframe hashtable
        Hashtable iframeHashtable = _navigationController.getIFrameHashtable();
        iframeHashtable.clear();
    	_navigationController.setIFrameHashtable( iframeHashtable );
        
        if( _pageLoaded ) {
            // Create navigation map
            _navigationController._focusableNodes = populateFocusableNodes( true, _navigationController._dom );
            _navigationController._defaultHoverEffect = isDefaultHoverEffectEnabled( _navigationController._dom );
            // Set first focus
            Node firstFocusNode = findInitialFocusNode();
            if( firstFocusNode == null ) {
                _navigationController.handleInitFocus();
            } else {
                _navigationController.setFocus( firstFocusNode );
            }
        } else {
            // Update navigation map
            _navigationController._focusableNodes = populateFocusableNodes( false, _navigationController._dom );
            if( _navigationController._currentFocusNode != null ) {
                if( !_navigationController.isValidFocusableNode( _navigationController._currentFocusNode ) ) {
                    _navigationController._currentFocusNode = null;
                }
            }
        }
        return;
    }
    
    private Node findInitialFocusNode() {
        if( _navigationController._focusableNodes == null || _navigationController._focusableNodes.size() == 0 ) 
            return null;
        for( int i = 0; i < _navigationController._focusableNodes.size(); i++ ) {
            Node node = (Node) _navigationController._focusableNodes.elementAt( i );
            XYRect nodeRect = _navigationController._widgetFieldManager.getPosition( node );
            if( nodeRect == null || nodeRect.width == 0 || nodeRect.height == 0 ) {
                continue;
            }
            if( _navigationController.isFocusableDisabled( node ) ) {
                continue;
            }
            if( isInitialFocusNode( node ) ) {
                return node;
            }
        }
        return null;
    }
    
    private Vector populateFocusableNodes( boolean firstLoad, final Document targetDom ) {
        Vector focusableNodes = new Vector();
        NodeSelector selector = (NodeSelector) targetDom;
        NodeList nodeList = selector
                .querySelectorAll( "textarea:not([x-blackberry-focusable=false]),a:not([x-blackberry-focusable=false]),input:not([x-blackberry-focusable=false]),select:not([x-blackberry-focusable=false]),button:not([x-blackberry-focusable=false]),[x-blackberry-focusable=true]" );
        for( int i = 0; i < nodeList.getLength(); i++ ) {
            Node node = nodeList.item( i );
            if( node instanceof HTMLSelectElement && ( (HTMLSelectElement) node ).getMultiple() ) {
            } else if( node instanceof HTMLInputElement && ( (HTMLInputElement) node ).getType().equals( "hidden" ) ) {
            } else {

            	// Check for iframes 
                IFrameSearchResult result = checkForIframe(node);
                
                // Append focusable nodes of the iframe if an iframe was found in the search
                if( result.hasIFrameNode() ){
                	Vector iframeFocusableNodes = result.getIFrameFocusableNodes();
                	Node iframeChildNode;
                	int size = iframeFocusableNodes.size();
                	for( int j = 0; j < size ; j++ ){
                		iframeChildNode = (Node)iframeFocusableNodes.elementAt( j );
                		focusableNodes.addElement( iframeChildNode );
                		
                		/* Add the iframe to the child-parent hash.
                		 * Used later to calculate proper XY coordinates */                		
                    	Hashtable iframeHashtable = _navigationController.getIFrameHashtable();
                		iframeHashtable.put( iframeChildNode, result.getTargetNode() );
                		_navigationController.setIFrameHashtable( iframeHashtable );
                	}      
                }
                /* If an iframe was not found, then the target node is just a 
                regular focusable node.  Add it to the list*/
                else{
                	focusableNodes.addElement( result.getTargetNode() );
                }
            }
        }
        return focusableNodes;
    }
    
    
    /**
     * <description>
     * The function checks if the dom element is an iframe or not. 
     * If it is an iframe it runs the selector query to get the node list of 
     * focusable elements recursively.     * 
     * @param node <description>
     * @return IFrameSearchResult containing results of the search
     */
    private IFrameSearchResult checkForIframe( Node node ){
        
    	IFrameSearchResult searchResult;
        if( node instanceof HTMLIFrameElement )
            {
        		HTMLIFrameElement iframeNode = (HTMLIFrameElement) node;  
                Document iframeDom = iframeNode.getContentDocument(); //for now a single banner                
                Vector iframeFocusableNodes = populateFocusableNodes( true, iframeDom );
                
                // Create result container
                searchResult = new IFrameSearchResult( node );
                searchResult.setIFrameFocusableNodes( iframeFocusableNodes );                
            }
            else {
            	searchResult = new IFrameSearchResult( node );
            }
       return searchResult;
    }
    
    
    private boolean isDefaultHoverEffectEnabled( final Document dom ) {
        if( dom == null )
            return true;
        Element head = null;
        Element doc = dom.getDocumentElement();
        if( doc instanceof ElementTraversal ) {
            head = ( (ElementTraversal) doc ).getFirstElementChild();
        }
        if( !( head instanceof HTMLHeadElement ) ) {
            return true;
        }
        for( Node node = head.getFirstChild(); node != null; node = node.getNextSibling() ) {
            if( node instanceof HTMLMetaElement ) {
                HTMLMetaElement meta = (HTMLMetaElement) node;
                String name = meta.getName();
                String content;

                if( name != null && name.equalsIgnoreCase( NavigationController.DEFAULT_HOVER_EFFECT ) ) {
                    content = meta.getContent();
                    if( content != null && content.equalsIgnoreCase( "false" ) ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean isInitialFocusNode( final Node node ) {
        NamedNodeMap nnm = node.getAttributes();
        if( nnm != null ) {
            Node att = nnm.getNamedItem( NavigationController.INITIAL_FOCUS );
            if( ( att instanceof Attr ) && ( (Attr) att ).getValue().equals( "true" ) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Container used to store results of iframe checks. If the target node is
     * an iframe, the focusable nodes within the iframe should be stored.
     */
    private static class IFrameSearchResult {
    	private Node _targetNode;    	
    	private Vector _iframeFocusableNodes;
    	
    	IFrameSearchResult(final Node targetNode){
    		_targetNode = targetNode;    		
    		_iframeFocusableNodes = null;
    	}
    	
    	/**
    	 * Sets the list of focusable nodes within the target iframe node.
    	 * @param focusableNodes
    	 */
    	public void setIFrameFocusableNodes(Vector focusableNodes){
    		_iframeFocusableNodes = focusableNodes;
    	}
    	/**
    	 * Checks if the target node is an iframe.
    	 * @return true when target node is an HTMLIFrameElement
    	 */
    	public boolean hasIFrameNode(){
    		return (_targetNode instanceof HTMLIFrameElement);
    	}
    	
    	/**
    	 * Retrieves target node.
    	 * @return Node used in constructor
    	 */
    	public Node getTargetNode(){
    		return _targetNode;
    	}
    	
    	/**
    	 * Retrieves focusable nodes found within the target node.
    	 * Will be null if the target node is not an iframe.
    	 * @return
    	 */
    	public Vector getIFrameFocusableNodes(){
    		return _iframeFocusableNodes;
    	}
    	
    }
}
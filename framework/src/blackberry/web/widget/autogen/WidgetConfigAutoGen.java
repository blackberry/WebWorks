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
/// 
/// AUTO-GENERATED CLASS FROM WEB COMPONENT PACK - edit with caution
///
package blackberry.web.widget.autogen;

import blackberry.web.widget.impl.WidgetConfigImpl;

import net.rim.device.api.web.WidgetAccess;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetFeature;

import java.util.Hashtable;
import java.util.Vector;
import blackberry.web.widget.loadingScreen.TransitionConstants;

/**
 * see blackberry.web.widget.WidgetConfig
 */
public final class WidgetConfigAutoGen extends WidgetConfigImpl {

    public WidgetConfigAutoGen() {    

        /// EVERYTHING ABOVE THIS LINE IS STATIC AND DOES NOT NEED TO BE CHANGED   
        
        // create access 
        try {
            _accessList.put(                WidgetConfig.WIDGET_LOCAL_DOMAIN,       // access:uri (FEATURE nodes stored at the root level are defaulted with access:uri=local, access:sub-domain=true
                                            new WidgetAccess(
                                                WidgetConfig.WIDGET_LOCAL_DOMAIN,   // access:uri
                                                true,                               // access:sub-domain
                                                new WidgetFeature[]                 // features allowed for this access node
                                                    { new WidgetFeature( 
                                                        "blackberry",        // feature:id
                                                        true,                       // feature:required
                                                        "1.0.0",                    // feature:version
                                                        null), 
                                                      new WidgetFeature(
                                                        "blackberry.pim.Task",
                                                        true,
                                                        "1.0.0",
                                                        null), 
                                                      new WidgetFeature(
                                                        "blackberry.system",
                                                        true,
                                                        "1.0.0",
                                                        null) }) );                 // sub-features
            _accessList.put(                "http://awong-xp2/",                    // access:uri (FEATURE nodes stored at the root level are defaulted with access:uri=local, access:sub-domain=true
                                            new WidgetAccess(
                                                "http://awong-xp2/",      // access:uri
                                                true,                               // access:sub-domain
                                                new WidgetFeature[]                 // features allowed for this access node
                                                    { new WidgetFeature( 
                                                        "blackberry.app",        // feature:id
                                                        true,                       // feature:required
                                                        "1.0.0",                    // feature:version
                                                        null), 
                                                      new WidgetFeature(
                                                        "blackberry.push",
                                                        true,
                                                        "1.0.0",
                                                        null), 
                                                      new WidgetFeature(
                                                        "blackberry.push.Data",
                                                        true,
                                                        "1.0.0",
                                                        null) }) );                 // sub-features
            _accessList.put(                "http://rim.net/",                    // access:uri (FEATURE nodes stored at the root level are defaulted with access:uri=local, access:sub-domain=true
                                            new WidgetAccess(
                                                "http://rim.net/",      // access:uri
                                                true,                               // access:sub-domain
                                                new WidgetFeature[]                 // features allowed for this access node
                                                    { new WidgetFeature( 
                                                        "blackberry.app",        // feature:id
                                                        true,                       // feature:required
                                                        "1.0.0",                    // feature:version
                                                        null),
                                                new WidgetFeature( 
                                                        "blackberry.ui.menu",        // feature:id
                                                        true,                       // feature:required
                                                        "1.0.0",                    // feature:version
                                                        null),                                                        
                                                      new WidgetFeature(
                                                        "blackberry.system",
                                                        true,
                                                        "1.0.0",
                                                        null) }) );                 // sub-features
            /**_accessList.put(                "http://atg05-yyz/",                    // access:uri (FEATURE nodes stored at the root level are defaulted with access:uri=local, access:sub-domain=true
                                            new WidgetAccess(
                                                "http://atg05-yyz/",      // access:uri
                                                true,                               // access:sub-domain
                                                null));**/
        } catch (Exception e) {
            // ignore this element - invalid URI
        }
        _author =                       "Web API Team";
        _authorURL =                    "http://tneil-build-xp/widgets/blackberry/";
        _authorEmail =                  "awong@rim.com";
        _copyright =                    "Copyright 2009 Research In Motion";
        _configXML =                    "test/resources/config.xml";
        //_content =                      "cod://blackberry_web_widget/test/resources/index.html";
        _content =                      "http://eli-xp.rim.net/BBTest/naviWidget_withInputs_customMove.html";
        _foregroundSource =             "http://eli-xp.rim.net/BBTest/naviWidget_withInputs_customMove.html";
        //_content =                      "file:///SDCard/index.html";
        //_content =                      "http://awong-xp2/jamtester/noexist.html";
        _contentType =                  "text/html";                                                                // 'text/html' is default value - only set if necessary
        _contentCharset =               "ISO-8859-1";                                                               // 'ISO-8859-1' is default value - only set if necessary
        //_customHeaders.put(             "RIM-Widget", "rim/widget");
        _description =                  "New test harness for Web API (and Tumbler).";
        _icon =                         "test/resources/icon.png";
        _iconHover =                    "test/resources/icon_hover.png";
        _id =                           "bb-widget-TestWidget-123456789";
        _license =                      "Don't be a pirate.";
        _licenseURL =                   "http://en.wikipedia.org/wiki/Pirate";
        _name =                         "TumblerBuiltWebTestHarness";       
        //_notifications
        //_version                                                                                                  // SET BASED ON VERSION COMPILED INTO COD!       
        
        _widgetNavigationMode =         true;

        //_loadingScreenColor = "#0000FF";
        //_hasMultiAccess = true;
        //_firstPageLoad = true;
        //_remotePageLoad = true;
        //_localPageLoad = true;
        //_transitionType = TransitionConstants.TRANSITION_SLIDEPUSH;
        //_transitionDuration = 1000;
        //_transitionDirection = TransitionConstants.DIRECTION_UP;
        /// EVERYTHING BELOW THIS LINE IS STATIC AND DOES NOT NEED TO BE CHANGED
    }     
}

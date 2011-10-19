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
package blackberry.app;

import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import blackberry.core.ScriptableFunctionBase;

/**
 * Function to remove banner indicator.
 */
public final class RemoveBannerIndicatorFunction extends ScriptableFunctionBase {

    public static final String NAME = "removeBannerIndicator";

    /**
     * @see ScriptableFunctionBase#execute(Object, Object[])
     */
    public Object execute( Object thiz, Object[] args ) {
        ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
        ApplicationIndicator indicator = reg.getApplicationIndicator();
        if( indicator != null ) {
            indicator.setVisible( false );
        }
        return UNDEFINED;
    }
}

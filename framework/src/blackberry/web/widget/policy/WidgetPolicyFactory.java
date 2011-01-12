/*
 * WidgetPolicyFactory.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.policy;

import blackberry.web.widget.policy.WidgetPolicy;

/**
 * 
 */
public class WidgetPolicyFactory {
    
    public static WidgetPolicy getPolicy() {
        return new WidgetPolicy();
    }
    
}

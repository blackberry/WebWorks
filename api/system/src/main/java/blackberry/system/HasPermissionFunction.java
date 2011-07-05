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
package blackberry.system;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import blackberry.core.FunctionSignature;
import blackberry.core.ScriptableFunctionBase;

/**
 * Implementation of blackberry.system.hasPermission function.
 */
public final class HasPermissionFunction extends ScriptableFunctionBase {

    private static final String BLACKBERRY_APP = "blackberry.app";
    private static final String BLACKBERRY_FIND = "blackberry.find";
    private static final String BLACKBERRY_IDENTITY = "blackberry.identity";
    private static final String BLACKBERRY_INVOKE = "blackberry.invoke";
    private static final String BLACKBERRY_IO = "blackberry.io";
    private static final String BLACKBERRY_MESSAGE = "blackberry.message";
    private static final String BLACKBERRY_PIM = "blackberry.pim";
    private static final String BLACKBERRY_PUSH = "blackberry.push";
    private static final String BLACKBERRY_SYSTEM = "blackberry.system";
    private static final String BLACKBERRY_UI = "blackberry.ui";
    private static final String BLACKBERRY_XML = "blackberry.xml";

    /**
     * Implements blackberry.system.hasPermission
     */
    protected Object execute( Object thiz, Object[] args ) throws Exception {
        String request = args[ 0 ].toString();

        switch( hasPermission( request ) ) {
            case ApplicationPermissions.VALUE_ALLOW:
                return new Integer( SystemNamespace.ALLOW_VALUE );
            case ApplicationPermissions.VALUE_DENY:
                return new Integer( SystemNamespace.DENY_VALUE );
            case ApplicationPermissions.VALUE_PROMPT:
                return new Integer( SystemNamespace.PROMPT_VALUE );
            default:
                return new Integer( SystemNamespace.NOTSET_VALUE );
        }
    }

    private int hasPermission( String request ) {
        ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
        if( request.equals( BLACKBERRY_APP ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT );
        } else if( request.equals( BLACKBERRY_FIND ) ) {
            return ApplicationPermissions.VALUE_ALLOW;
        } else if( request.equals( BLACKBERRY_IDENTITY ) ) {
            return ApplicationPermissions.VALUE_ALLOW;
        } else if( request.equals( BLACKBERRY_INVOKE ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION );
        } else if( request.equals( BLACKBERRY_IO ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_FILE_API );
        } else if( request.equals( BLACKBERRY_MESSAGE ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_EMAIL );
        } else if( request.equals( BLACKBERRY_PIM ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_ORGANIZER_DATA );
        } else if( request.equals( BLACKBERRY_PUSH ) ) {
            return apm.getPermission( ApplicationPermissions.PERMISSION_INTERNET );
        } else if( request.equals( BLACKBERRY_SYSTEM ) ) {
            return ApplicationPermissions.VALUE_ALLOW;
        } else if( request.equals( BLACKBERRY_UI ) ) {
            return ApplicationPermissions.VALUE_ALLOW;
        } else if( request.equals( BLACKBERRY_XML ) ) {
            return ApplicationPermissions.VALUE_ALLOW;
        }
        return ApplicationPermissions.VALUE_ALLOW;
    }

    /**
     * @see net.rim.device.api.web.jse.base.ScriptableFunctionBase
     */
    protected FunctionSignature[] getFunctionSignatures() {
        FunctionSignature fs = new FunctionSignature( 1 );
        fs.addParam( String.class, true );
        return new FunctionSignature[] { fs };
    }
}

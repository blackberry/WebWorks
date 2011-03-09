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
package blackberry.identity;

import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.StringUtilities;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.service.ServiceObject;

/**
 * This class contains the implementation for all the functions in blackberry.identity. namespace.
 * 
 * @author sgolod
 * 
 */
public class IdentityFunctions {

    public static final String FUNCTION_GETSERVICELIST = "getServiceList";
    public static final String FUNCTION_GETTRANSPORTlIST = "getTransportList";

    /**
     * Translate cid to a type value.
     * 
     * @param cid
     *            - Type of service It can be one of "CMIME"(Email), "CICAL"(Calendar) or "SYNC"(Contact) (not case sensistive)
     * 
     * @return return the corresponding type value. If cid does not have a corressponding type value recored in the ServiceObject
     *         class, -1 is returned.
     */
    private static int cidToType( final String cid ) {

        if( StringUtilities.strEqualIgnoreCase( cid, "CMIME" ) ) {
            return ServiceObject.TYPE_EMAIL;
        } else if( StringUtilities.strEqualIgnoreCase( cid, "CICAL" ) ) {
            return ServiceObject.TYPE_CALENDAR;
        } else if( StringUtilities.strEqualIgnoreCase( cid, "SYNC" ) ) {
            return ServiceObject.TYPE_CONTACT;
        } else {
            return -1;
        }
    }

    /**
     * Return a list of available email, calendar, and contact services.
     * 
     * @return if no service is available, an empty array will be returned.
     */
    private static ServiceObject[] getServiceList() {
        ServiceObject[] emailServiceList = getServiceList( "CMIME" );
        final ServiceObject[] contactServiceList = getServiceList( "CICAL" );
        final ServiceObject[] calendarServiceList = getServiceList( "SYNC" );

        emailServiceList = getCombinedArray( emailServiceList, contactServiceList );
        emailServiceList = getCombinedArray( emailServiceList, calendarServiceList );

        return emailServiceList;
    }

    private static ServiceObject[] getCombinedArray( final Object[] arr1, final Object[] arr2 ) {
        final ServiceObject[] result = new ServiceObject[ arr1.length + arr2.length ];
        System.arraycopy( arr1, 0, result, 0, arr1.length );
        System.arraycopy( arr2, 0, result, arr1.length, arr2.length );

        return result;
    }

    /**
     * Helper method for finding all the available services with specified cid.
     * 
     * @param cid
     *            indicates a value for which search of services is performed.
     * 
     * @return a list of available services with the specified type. If no service is found, a empty array is returned.
     */
    private static ServiceObject[] getServiceList( final String cid ) {

        final int type = cidToType( cid );
        final ServiceBook sb = ServiceBook.getSB();
        final ServiceRecord[] serviceRecords = sb.findRecordsByCid( cid );
        ServiceObject[] serviceList;

        if( serviceRecords != null ) {

            serviceList = new ServiceObject[ serviceRecords.length ];

            for( int i = 0; i < serviceRecords.length; i++ ) {

                final ServiceRecord serviceRecord = serviceRecords[ i ];
                String emailAddress = null;

                if( type == ServiceObject.TYPE_EMAIL ) {
                    final ServiceConfiguration serviceConfig = new ServiceConfiguration( serviceRecord );
                    emailAddress = serviceConfig.getEmailAddress();
                }

                serviceList[ i ] = new ServiceObject( emailAddress, serviceRecord.getName(), new Integer( type ), serviceRecord
                        .getUid(), serviceRecord.getCid() );

            }
            return serviceList;
        }

        return new ServiceObject[ 0 ];
    }

    /**
     * Implements ScriptableFunctionBase to retrieve a list of services of interest.
     * 
     * @return the list of services implemented as items in the ServiceObject array.
     */
    public static ScriptableFunction createGetServiceListFunction() {
        return new ScriptableFunctionBase() {

            public Object execute( final Object thiz, final Object[] args ) {
                return IdentityFunctions.getServiceList();
            };

        };
    }

}

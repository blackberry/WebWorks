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

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.core.ScriptableFunctionBase;
import blackberry.identity.transport.TransportObject;

/**
 * This class implements request for list of transports.
 * 
 * @author sgolod
 * 
 */
public final class GetTransportListFunction extends ScriptableFunctionBase {

    public static final String NAME = "getTransportList";

    /**
     * Represents TCP Cellular transport also known as Direct TCP
     */
    public static final String TRANSPORT_TCP_CELLULAR = "TCP Cellular";

    /**
     * Represents the Wap 1.0 and Wap 1.1 trasnport types
     */
    public static final String TRANSPORT_WAP = "Wap";

    /**
     * Represents the Wap 2.0 transport type
     */
    public static final String TRANSPORT_WAP2 = "Wap 2.0";

    /**
     * Represents the MDS transport type
     */
    public static final String TRANSPORT_MDS = "MDS";

    /**
     * Represents the Blackberry Internet Service transport type
     */
    public static final String TRANSPORT_BIS_B = "Bis B";

    /**
     * Represents the Blackberry Unite! transport type
     */
    public static final String TRANSPORT_UNITE = "Unite!";

    /**
     * Represents the WIFI transport type
     */
    public static final String TRANSPORT_TCP_WIFI = "TCP Wifi";

    private static final int CONFIG_TYPE_BES = 1;
    private static final String UNITE_NAME = "Unite";

    /**
     * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
     */
    public Object execute( final Object thiz, final Object[] args ) throws Exception {

        final Vector vc = new Vector();

        // Determine if Direct TCP is available
        if( ( RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA ) != 0 ) {
            vc.addElement( new TransportObject( "", TRANSPORT_TCP_CELLULAR, null ) );
        }

        final ServiceBook sb = ServiceBook.getSB();
        final ServiceRecord[] records = sb.getRecords();

        for( int i = 0; i < records.length; i++ ) {
            final ServiceRecord serviceRecord = records[ i ];
            if( serviceRecord.isValid() && !serviceRecord.isDisabled() && determineTransportType( serviceRecord ) != null ) {
                vc.addElement( new TransportObject( serviceRecord.getName(), determineTransportType( serviceRecord ),
                        serviceRecord.getUid() ) );
            }
        }

        final TransportObject[] list = new TransportObject[ vc.size() ];

        for( int i = 0; i < list.length; i++ ) {
            list[ i ] = (TransportObject) vc.elementAt( i );
        }

        return list;
    }

    private static String determineTransportType( final ServiceRecord serviceRecord ) {

        final String cid = serviceRecord.getCid().toLowerCase();
        final String uid = serviceRecord.getUid().toLowerCase();

        // BIS
        if( cid.indexOf( "ippp" ) != -1 && uid.indexOf( "gpmds" ) != -1 ) {
            return TRANSPORT_BIS_B;
        }

        // MDS
        if( cid.indexOf( "ippp" ) != -1 && uid.indexOf( "gpmds" ) == -1 ) {
            return TRANSPORT_MDS;
        }

        // WiFi
        if( cid.indexOf( "wptcp" ) != -1 && uid.indexOf( "wifi" ) != -1 ) {
            return TRANSPORT_TCP_WIFI;
        }

        // Wap1.0
        if( cid.indexOf( "wap" ) != -1 && uid.indexOf( "wap transport" ) != -1 ) {
            return TRANSPORT_WAP;
        }

        // Wap2.0
        if( cid.indexOf( "wptcp" ) != -1 && uid.indexOf( "wap2" ) != -1 && uid.indexOf( "wifi" ) == -1
                && uid.indexOf( "mms" ) == -1 ) {
            return TRANSPORT_WAP2;
        }

        // Unite
        if( getConfigType( serviceRecord ) == CONFIG_TYPE_BES && serviceRecord.getName().equals( UNITE_NAME ) ) {
            return TRANSPORT_UNITE;
        }

        return null;
    }

    /**
     * Gets the config type of a ServiceRecord using getDataInt below
     * 
     * @param record
     *            A ServiceRecord
     * 
     * @return configType of the ServiceRecord
     */
    private static int getConfigType( final ServiceRecord record ) {
        return getDataInt( record, 12 );
    }

    private static int getDataInt( final ServiceRecord record, final int type ) {
        DataBuffer buffer = null;
        buffer = getDataBuffer( record, type );

        if( buffer != null ) {
            try {
                return ConverterUtilities.readInt( buffer );
            } catch( final EOFException e ) {
                return -1;
            }
        }
        return -1;
    }

    private static DataBuffer getDataBuffer( final ServiceRecord record, final int type ) {
        final byte[] data = record.getApplicationData();
        if( data != null ) {
            final DataBuffer buffer = new DataBuffer( data, 0, data.length, true );
            try {
                buffer.readByte();
            } catch( final EOFException e1 ) {
                return null;
            }
            if( ConverterUtilities.findType( buffer, type ) ) {
                return buffer;
            }
        }
        return null;
    }

}

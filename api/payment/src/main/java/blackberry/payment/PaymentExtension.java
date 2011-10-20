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
package blackberry.payment;

import java.util.Vector;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetException;
import blackberry.common.util.JSUtilities;
import blackberry.common.util.json4j.JSONArray;
import blackberry.common.util.json4j.JSONObject;
import blackberry.core.IJSExtension;
import blackberry.core.JSExtensionRequest;
import blackberry.core.JSExtensionResponse;
import blackberry.core.JSExtensionReturnValue;

import net.rimlib.blackberry.api.payment.*;

/**
 * JavaScript extension for blackberry.payment
 */
public class PaymentExtension implements IJSExtension {
    private static Vector SUPPORTED_METHODS;

    public static final String FUNCTION_PURCHASE = "purchase";
    public static final String FUNCTION_GETEXISTINGPURCHASES = "getExistingPurchases";
    public static final String FUNCTION_GETMODE = "getDevelopmentMode";
    public static final String FUNCTION_SETMODE = "setDevelopmentMode";

    public static boolean developmentMode = true;

    private static final String KEY_DG_ID = "digitalGoodID";
    private static final String KEY_DG_SKU = "digitalGoodSKU";
    private static final String KEY_DG_NAME = "digitalGoodName";
    private static final String KEY_METADATA = "metaData";
    private static final String KEY_APP_NAME = "purchaseAppName";
    private static final String KEY_APP_ICON = "purchaseAppIcon";

    private static final String KEY_REFRESH = "allowRefresh";

    private static final int CODE_USER_CANCEL = 1;
    private static final int CODE_SYS_BUSY = 2;
    private static final int CODE_SYS_ERROR = 3;
    private static final int CODE_NOT_FOUND = 4;
    private static final int CODE_ILLEGAL_APP = 5;

    static {
        SUPPORTED_METHODS = new Vector();
        SUPPORTED_METHODS.addElement( FUNCTION_PURCHASE );
        SUPPORTED_METHODS.addElement( FUNCTION_GETEXISTINGPURCHASES );
        SUPPORTED_METHODS.addElement( FUNCTION_GETMODE );
        SUPPORTED_METHODS.addElement( FUNCTION_SETMODE );
    }
    
    private static String[] JS_FILES = { "payment_dispatcher.js", "payment_ns.js" };

    public String[] getFeatureList() {
        return new String[] { "blackberry.payment" };
    }

    /**
     * Implements invoke() of interface IJSExtension. Methods of extension will be called here.
     * 
     * @throws WidgetException
     *             if specified method cannot be recognized
     */
    public void invoke( JSExtensionRequest request, JSExtensionResponse response ) throws WidgetException {
        String method = request.getMethodName();
        Object[] args = request.getArgs();
        String msg = "";
        int code = JSExtensionReturnValue.SUCCESS;
        JSONObject data = new JSONObject();
        JSONArray dataArray = new JSONArray();
        JSONObject returnValue = null;

        PaymentEngine engine = PaymentSystem.getInstance();

        if( engine == null ) {
            throw new IllegalArgumentException(
                    "Sorry, in-app purchases are unavailable. Make sure BlackBerry App World v2.1 or higher is installed on your device." );
        }

        if( !SUPPORTED_METHODS.contains( method ) ) {
            throw new WidgetException( "Undefined method: " + method );
        }

        try {
            if( method.equals( FUNCTION_PURCHASE ) ) {
                String digitalGoodID = (String) request.getArgumentByName( KEY_DG_ID );
                String digitalGoodSKU = (String) request.getArgumentByName( KEY_DG_SKU );
                String digitalGoodName = (String) request.getArgumentByName( KEY_DG_NAME );
                String metaData = (String) request.getArgumentByName( KEY_METADATA );
                String purchaseAppName = (String) request.getArgumentByName( KEY_APP_NAME );
                String purchaseAppIcon = (String) request.getArgumentByName( KEY_APP_ICON );

                PurchaseArgumentsBuilder arguments = new PurchaseArgumentsBuilder().withDigitalGoodId( digitalGoodID )
                        .withDigitalGoodName( digitalGoodName ).withDigitalGoodSku( digitalGoodSKU ).withMetadata( metaData )
                        .withPurchasingAppName( purchaseAppName )
                        .withPurchasingAppIcon( ( purchaseAppIcon != null ? Bitmap.getBitmapResource( purchaseAppIcon ) : null ) );

                // Blocking call: engine.purchase() invokes AppWorld screen.
                Purchase successfulPurchase = engine.purchase( arguments.build() );
                String purchaseJSON = purchaseToJSONString( successfulPurchase );

                data = new JSONObject( purchaseJSON );
                code = JSExtensionReturnValue.SUCCESS;
                msg = "Purchase Successful";
            } else if( method.equals( FUNCTION_GETEXISTINGPURCHASES ) ) {
                String refresh = (String) request.getArgumentByName( KEY_REFRESH );
                // Blocking call: engine.getExistingPurchases() invokes AppWorld screen.
                Purchase[] purchases = engine.getExistingPurchases( parseBoolean( refresh ) );

                if( purchases.length != 0 ) {
                    for( int i = 0; i < purchases.length; i++ ) {
                        String purchaseJSON = "";
                        purchaseJSON += purchaseToJSONString( purchases[ i ] );
                        JSONObject temp = new JSONObject( purchaseJSON );
                        dataArray.add( temp );
                    }
                }
            } else if( method.equals( new String( FUNCTION_GETMODE ) ) ) {
                data.put( "developmentMode", PaymentSystem.getMode() );
                code = JSExtensionReturnValue.SUCCESS;
                msg = "developmentMode set to: " + PaymentSystem.getMode();
            } else if( method.equals( new String( FUNCTION_SETMODE ) ) ) {
                String s = (String) request.getArgumentByName( "developmentMode" );
                if( s.equals( "true" ) ) {
                    PaymentSystem.setMode( true );
                } else {
                    PaymentSystem.setMode( false );
                }
                code = JSExtensionReturnValue.SUCCESS;
                msg = "developmentMode set to: " + s;
            }
        } catch( UserCancelException e ) {
            code = CODE_USER_CANCEL;
            msg = e.getMessage();
        } catch( DigitalGoodNotFoundException e ) {
            code = CODE_NOT_FOUND;
            msg = e.getMessage();
        } catch( PaymentServerException e ) {
            code = CODE_SYS_ERROR;
            msg = e.getMessage();
        } catch( IllegalApplicationException e ) {
            code = CODE_ILLEGAL_APP;
            msg = e.getMessage();
        } catch( PaymentException e ) {
            code = CODE_SYS_ERROR;
            msg = e.getMessage();
        } catch( Exception e ) {
            code = JSExtensionReturnValue.FAIL;
            msg = e.getMessage();
        }

        if( method.equals( FUNCTION_GETEXISTINGPURCHASES ) ) {
            returnValue = new JSExtensionReturnValue( msg, code, dataArray ).getReturnValue();
        } else {
            returnValue = new JSExtensionReturnValue( msg, code, data ).getReturnValue();
        }
        response.setPostData( returnValue.toString().getBytes() );
    }

    private static boolean parseBoolean( String str ) {
        return ( str != null && str.equals( Boolean.TRUE.toString() ) );
    }

    public String purchaseToJSONString( Purchase obj ) {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "{" );
        buffer.append( "\"digitalGoodID\":\"" + obj.getDigitalGoodId() + "\"," );
        buffer.append( "\"digitalGoodSKU\":\"" + obj.getDigitalGoodSku() + "\"," );
        buffer.append( "\"date\":\"" + Long.toString( obj.getDate().getTime() ) + "\"," );
        buffer.append( "\"licenseKey\":\"" + obj.getLicenseKey() + "\"," );
        buffer.append( "\"metaData\":\"" + obj.getMetadata() + "\"," );
        buffer.append( "\"transactionID\":\"" + obj.getTransactionId() + "\"," );
        buffer.append( "}" );
        return buffer.toString();
    }

    public void loadFeature( String feature, String version, Document document, ScriptEngine scriptEngine,
            SimpleSortingVector jsInjectionPaths ) {
        JSUtilities.loadJS( scriptEngine, JS_FILES, jsInjectionPaths );        
    }


    public void register( WidgetConfig widgetConfig, BrowserField browserField ) {
        
    }

    public void unloadFeatures() {
        
    }
}

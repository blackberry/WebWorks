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

import net.rimlib.blackberry.api.payment.PaymentEngine;

/**
 * PaymentEngine singleton and connection mode handler.
 */
public class PaymentSystem {
    private static boolean _developmentMode = false;

    // Get Instance
    public static PaymentEngine getInstance() {
        return SingletonHolder.getInstance();
    }

    public static void setMode( boolean developmentMode ) {
        _developmentMode = developmentMode;
        SingletonHolder.getInstance().setConnectionMode(
                ( _developmentMode ? PaymentEngine.CONNECTION_MODE_LOCAL : PaymentEngine.CONNECTION_MODE_NETWORK ) );
    }

    public static boolean getMode() {
        return _developmentMode;
    }

    // SingletonHolder
    static class SingletonHolder {
        static PaymentEngine instance;

        static {
            instance = PaymentEngine.getInstance();
        }

        static PaymentEngine getInstance() {
            return instance;
        }
    }
}
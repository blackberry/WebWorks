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

/**
 * interface <code>ISystemExtensionConstants</code> contains constants 
 * which are need by SystemExtension
 * @author danlin
 */
public interface ISystemExtensionConstants {
    // Feature name
    public final static String FEATURE_ID = "blackberry.system";
    // System functions
    public final static String SOFTWARE_VERSION = "softwareVersion";
    public final static String SCRIPT_API_VERSION = "scriptApiVersion";
    public final static String MODEL = "model";
    // Methods of SystemExtension
    public final static String FUNCTION_HAS_PERMISSION = "hasPermission";
    public final static String FUNCTION_HAS_CAPABILITY = "hasCapability";
    public final static String FUNCTION_HAS_DATA_COVERAGE = "hasDataCoverage";
    public final static String FUNCTION_IS_MASS_STORAGE_ACTIVE = "isMassStorageActive";
    public final static String FUNCTION_SET_HOME_SCREEN = "setHomeScreenBackground";
    public final static String FUNCTION_GET = "get";

    public final static String ARG_MODULE = "module";
    public final static String ARG_CAPABILITY = "capability";
    public final static String ARG_PICTURE = "picture";

    public final static int UNDEFINED = -10000;
    public final static String ALLOW = "ALLOW";
    public final static String DENY = "DENY";
    public final static String PROMPT = "PROMPT";
    // Capability constant
    public final static String C_INPUT_KEYBOARD_ISSURETYPE = "input.keyboard.issuretype";
    public final static String C_INPUT_TOUCH = "input.touch";
    public final static String C_MEDIA_AUDIO_CAPTURE = "media.audio.capture";
    public final static String C_MEDIA_VIDEO_CAPTURE = "media.video.capture";
    public final static String C_MEDIA_RECORDING = "media.recording";
    public final static String C_LOCATION_MAPS = "location.maps";
    public final static String C_LOCATION_GPS = "location.gps";
    public final static String C_STORAGE_MEMORYCARD = "storage.memorycard";
    public final static String C_NETWORK_BLUETOOTH = "network.bluetooth";
    public final static String C_NETWORK_WLAN = "network.wlan";
    public final static String C_NETWORK_3GPP = "network.3gpp";
    public final static String C_NETWORK_CDMA = "network.cdma";
    public final static String C_NETWORK_IDEN = "network.iden";
    public final static String[] ALL_CAPABILITIES = { C_INPUT_KEYBOARD_ISSURETYPE, C_INPUT_TOUCH, C_MEDIA_AUDIO_CAPTURE,
            C_MEDIA_VIDEO_CAPTURE, C_MEDIA_RECORDING, C_LOCATION_MAPS, C_LOCATION_GPS, C_STORAGE_MEMORYCARD, C_NETWORK_BLUETOOTH,
            C_NETWORK_WLAN, C_NETWORK_3GPP, C_NETWORK_CDMA, C_NETWORK_IDEN };

    // Permission code
    public final static int ALLOW_VALUE = 0;
    public final static int DENY_VALUE = 1;
    public final static int PROMPT_VALUE = 2;
    public final static int NOTSET_VALUE = 3;
    // Permission constant
    public final static String BLACKBERRY_APP = "blackberry.app";
    public final static String BLACKBERRY_APP_EVENT = "blackberry.app.event";
    public final static String BLACKBERRY_AUDIO = "blackberry.audio";
    public final static String BLACKBERRY_AUDIO_PLAYER = "blackberry.audio.Player";
    public final static String BLACKBERRY_FIND = "blackberry.find";
    public final static String BLACKBERRY_FOCUS = "blackberry.focus";
    public final static String BLACKBERRY_IDENTITY = "blackberry.identity";
    public final static String BLACKBERRY_IDENTITY_PHONE = "blackberry.identity.phone";
    public final static String BLACKBERRY_INVOKE = "blackberry.invoke";
    public final static String BLACKBERRY_INVOKE_ADDRESSBOOKARGUMENTS = "blackberry.invoke.AddressBookArguments";
    public final static String BLACKBERRY_INVOKE_BROWSERARGUMENTS = "blackberry.invoke.BrowserArguments";
    public final static String BLACKBERRY_INVOKE_CALENDARARGUMENTS = "blackberry.invoke.CalendarArguments";
    public final static String BLACKBERRY_INVOKE_CAMERAARGUMENTS = "blackberry.invoke.CameraArguments";
    public final static String BLACKBERRY_INVOKE_JAVAARGUMENTS = "blackberry.invoke.JavaArguments";
    public final static String BLACKBERRY_INVOKE_MAPSARGUMENTS = "blackberry.invoke.MapsArguments";
    public final static String BLACKBERRY_INVOKE_MEMOARGUMENTS = "blackberry.invoke.MemoArguments";
    public final static String BLACKBERRY_INVOKE_MESSAGEARGUMENTS = "blackberry.invoke.MessageArguments";
    public final static String BLACKBERRY_INVOKE_PHONEARGUMENTS = "blackberry.invoke.PhoneArguments";
    public final static String BLACKBERRY_INVOKE_SEARCHARGUMENTS = "blackberry.invoke.SearchArguments";
    public final static String BLACKBERRY_INVOKE_TASKARGUMENTS = "blackberry.invoke.TaskArguments";
    public final static String BLACKBERRY_IO_DIR = "blackberry.io.dir";
    public final static String BLACKBERRY_IO_FILE = "blackberry.io.file";
    public final static String BLACKBERRY_MEDIA_CAMERA = "blackberry.media.camera";
    public final static String BLACKBERRY_MEDIA_MICROPHONE = "blackberry.media.microphone";
    public final static String BLACKBERRY_MESSAGE = "blackberry.message";
    public final static String BLACKBERRY_MESSAGE_SMS = "blackberry.message.sms";
    public final static String BLACKBERRY_PAYMENT = "blackberry.payment";
    public final static String BLACKBERRY_PHONE_FIND = "blackberry.phone.Find";
    public final static String BLACKBERRY_PHONE_PHONE = "blackberry.phone.Phone";
    public final static String BLACKBERRY_PHONE_PHONELOGS = "blackberry.phone.PhoneLogs";
    public final static String BLACKBERRY_PIM_ADDRESS = "blackberry.pim.Address";
    public final static String BLACKBERRY_PIM_APPOINTMENT = "blackberry.pim.Appointment";
    public final static String BLACKBERRY_PIM_ATTENDEE = "blackberry.pim.Attendee";
    public final static String BLACKBERRY_PIM_CATEGORY = "blackberry.pim.category";
    public final static String BLACKBERRY_PIM_CONTACT = "blackberry.pim.Contact";
    public final static String BLACKBERRY_PIM_MEMO = "blackberry.pim.Memo";
    public final static String BLACKBERRY_PIM_RECURRENCE = "blackberry.pim.Recurrence";
    public final static String BLACKBERRY_PIM_REMINDER = "blackberry.pim.Reminder";
    public final static String BLACKBERRY_PIM_TASK = "blackberry.pim.Task";
    public final static String BLACKBERRY_PUSH = "blackberry.push";
    public final static String BLACKBERRY_SYSTEM = "blackberry.system";
    public final static String BLACKBERRY_SYSTEM_EVENT = "blackberry.system.event";
    public final static String BLACKBERRY_UI_DIALOG = "blackberry.ui.dialog";
    public final static String BLACKBERRY_UI_MENU = "blackberry.ui.menu";
    public final static String BLACKBERRY_UTILS = "blackberry.utils";
    public final static String BLACKBERRY_WIDGETCACHE = "blackberry.widgetcache";
    public final static String BLACKBERRY_WIDGETCACHE_CACHEINFORMATION = "blackberry.widgetcache.CacheInformation";

    public final static String[] ALL_PERMISSIONS = { BLACKBERRY_APP, BLACKBERRY_APP_EVENT, BLACKBERRY_AUDIO,
            BLACKBERRY_AUDIO_PLAYER, BLACKBERRY_FIND, BLACKBERRY_FOCUS, BLACKBERRY_IDENTITY, BLACKBERRY_IDENTITY_PHONE,
            BLACKBERRY_INVOKE, BLACKBERRY_INVOKE_ADDRESSBOOKARGUMENTS, BLACKBERRY_INVOKE_BROWSERARGUMENTS,
            BLACKBERRY_INVOKE_CALENDARARGUMENTS, BLACKBERRY_INVOKE_CAMERAARGUMENTS, BLACKBERRY_INVOKE_JAVAARGUMENTS,
            BLACKBERRY_INVOKE_MAPSARGUMENTS, BLACKBERRY_INVOKE_MEMOARGUMENTS, BLACKBERRY_INVOKE_MESSAGEARGUMENTS,
            BLACKBERRY_INVOKE_PHONEARGUMENTS, BLACKBERRY_INVOKE_SEARCHARGUMENTS, BLACKBERRY_INVOKE_TASKARGUMENTS,
            BLACKBERRY_IO_DIR, BLACKBERRY_IO_FILE, BLACKBERRY_MEDIA_CAMERA, BLACKBERRY_MEDIA_MICROPHONE, BLACKBERRY_MESSAGE,
            BLACKBERRY_MESSAGE_SMS, BLACKBERRY_PAYMENT, BLACKBERRY_PHONE_FIND, BLACKBERRY_PHONE_PHONE,
            BLACKBERRY_PHONE_PHONELOGS, BLACKBERRY_PIM_ADDRESS, BLACKBERRY_PIM_APPOINTMENT, BLACKBERRY_PIM_ATTENDEE,
            BLACKBERRY_PIM_CATEGORY, BLACKBERRY_PIM_CONTACT, BLACKBERRY_PIM_MEMO, BLACKBERRY_PIM_RECURRENCE,
            BLACKBERRY_PIM_REMINDER, BLACKBERRY_PIM_TASK, BLACKBERRY_PUSH, BLACKBERRY_SYSTEM, BLACKBERRY_SYSTEM_EVENT,
            BLACKBERRY_UI_DIALOG, BLACKBERRY_UI_MENU, BLACKBERRY_UTILS, BLACKBERRY_WIDGETCACHE,
            BLACKBERRY_WIDGETCACHE_CACHEINFORMATION };
}

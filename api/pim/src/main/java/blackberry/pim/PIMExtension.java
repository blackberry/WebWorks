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
package blackberry.pim;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;

import org.w3c.dom.Document;

import blackberry.pim.address.AddressConstructor;
import blackberry.pim.appointment.AppointmentConstructor;
import blackberry.pim.attendee.AttendeeConstructor;
import blackberry.pim.category.CategoryNamespace;
import blackberry.pim.contact.ContactConstructor;
import blackberry.pim.memo.MemoConstructor;
import blackberry.pim.recurrence.RecurrenceConstructor;
import blackberry.pim.reminder.ReminderConstructor;
import blackberry.pim.task.TaskConstructor;

/**
 * This is the main entry class of the PIM extension
 * 
 * @author dmateescu
 */
public class PIMExtension implements WidgetExtension {

    /**
     * @see net.rim.device.api.web.WidgetExtension#getFeatureList()
     */
    public String[] getFeatureList() {
        return new String[] { CategoryNamespace.NAME, RecurrenceConstructor.NAME, ReminderConstructor.NAME,
                AttendeeConstructor.NAME, AppointmentConstructor.NAME, AddressConstructor.NAME, ContactConstructor.NAME,
                MemoConstructor.NAME, TaskConstructor.NAME };
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#loadFeature(java.lang.String, java.lang.String, org.w3c.dom.Document,
     *      net.rim.device.api.script.ScriptEngine)
     */
    public void loadFeature( final String feature, final String version, final Document doc, final ScriptEngine scriptEngine )
            throws Exception {
        Object obj = null;

        if( feature.equals( CategoryNamespace.NAME ) ) {
            obj = new CategoryNamespace();
        } else if( feature.equals( RecurrenceConstructor.NAME ) ) {
            obj = new RecurrenceConstructor();
        } else if( feature.equals( ReminderConstructor.NAME ) ) {
            obj = new ReminderConstructor();
        } else if( feature.equals( AttendeeConstructor.NAME ) ) {
            obj = new AttendeeConstructor();
        } else if( feature.equals( AppointmentConstructor.NAME ) ) {
            obj = new AppointmentConstructor();
        } else if( feature.equals( AddressConstructor.NAME ) ) {
            obj = new AddressConstructor();
        } else if( feature.equals( ContactConstructor.NAME ) ) {
            obj = new ContactConstructor();
        } else if( feature.equals( MemoConstructor.NAME ) ) {
            obj = new MemoConstructor();
        } else if( feature.equals( TaskConstructor.NAME ) ) {
            obj = new TaskConstructor();
        }

        if( obj != null ) {
            scriptEngine.addExtension( feature, obj );
        }
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#register(net.rim.device.api.web.WidgetConfig,
     *      net.rim.device.api.browser.field2.BrowserField)
     */
    public void register( final WidgetConfig widgetConfig, final BrowserField browserField ) {
        // do nothing
    }

    /**
     * @see net.rim.device.api.web.WidgetExtension#unloadFeatures(org.w3c.dom.Document)
     */
    public void unloadFeatures( final Document doc ) {
        // do nothing
    }
}

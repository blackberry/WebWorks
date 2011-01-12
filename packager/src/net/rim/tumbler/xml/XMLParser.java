package net.rim.tumbler.xml;

import net.rim.tumbler.WidgetArchive;
import net.rim.tumbler.config.WidgetConfig;

public interface XMLParser {
    WidgetConfig parseXML(WidgetArchive archive) throws Exception;
}

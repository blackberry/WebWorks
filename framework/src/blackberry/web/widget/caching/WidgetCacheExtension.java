package blackberry.web.widget.caching;

import org.w3c.dom.Document;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import blackberry.web.widget.bf.BrowserFieldScreen;


public class WidgetCacheExtension implements WidgetExtension{

	private BrowserFieldScreen _bFieldScreen;
	
	public String[] getFeatureList() {
		String[] result = new String[1];
		result[0] = "blackberry.widgetcache";
		return result;
	}
	
	public void loadFeature(String feature, String version,Document doc
			, ScriptEngine scriptEngine) throws Exception {
		
		if (feature.equals("blackberry.widgetcache")) {
			scriptEngine.addExtension(WidgetCacheNamespace.NAME,
					_bFieldScreen.getWidgetCacheExtension());
		}
		
	}
	
	public void register(WidgetConfig wConfig, BrowserField bField) {
		_bFieldScreen = (BrowserFieldScreen)(bField.getScreen());
	}
	
	public void unloadFeatures(Document arg0) {
		//DO NOTHING 
	}

}

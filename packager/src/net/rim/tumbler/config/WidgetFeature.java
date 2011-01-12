package net.rim.tumbler.config;

public class WidgetFeature {
    private String _id;
    private boolean _isRequired;
    private String _version;
    private WidgetFeature[] _dependentFeatures;

    public WidgetFeature(String id, boolean isRequired, String version,
            WidgetFeature[] dependentFeatures) {
        _id = id;
        _isRequired = isRequired;
        _version = version;
        _dependentFeatures = dependentFeatures;
    }

    public String getID() {
        return _id;
    }

    public boolean isRequired() {
        return _isRequired;
    }

    public String getVersion() {
        return _version;
    }

    public WidgetFeature[] getDependentFeatures() {
        return _dependentFeatures;
    }
}

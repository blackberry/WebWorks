package net.rim.tumbler.exception;

public class PackageException extends Exception {
    private String          _info;
    
    public PackageException(String id) {
        super(id);
    }
    
    public PackageException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public PackageException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public PackageException(Exception causedBy, String info) {
        super(causedBy);
        _info = info;
    }
    
    public String getInfo() {
        return _info;
    }
}

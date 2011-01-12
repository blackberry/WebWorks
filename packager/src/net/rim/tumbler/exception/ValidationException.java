package net.rim.tumbler.exception;

public class ValidationException extends Exception {
    private String          _info;
    
    public ValidationException(String id) {
        super(id);
    }
    
    public ValidationException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public ValidationException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public String getInfo() {
        return _info;
    }
}

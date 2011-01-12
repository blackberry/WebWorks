package net.rim.tumbler.exception;

public class SessionException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String _info;
	
	public SessionException(String id) {
        super(id);
    }
    
    public SessionException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public SessionException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public SessionException(Exception causedBy, String info) {
        super(causedBy);
        _info = info;
    }
    
    public String getInfo() {
        return _info;
    }

}

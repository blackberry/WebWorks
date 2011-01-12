package net.rim.tumbler.exception;

public class CommandLineException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private String _info;
	
	public CommandLineException(String id) {
        super(id);
    }
    
    public CommandLineException(String id, String info) {
        super(id);
        _info = info;
    }
    
    public CommandLineException(String id, Exception causedBy) {
        super(id, causedBy);
    }
    
    public CommandLineException(Exception causedBy, String info) {
        super(causedBy);
        _info = info;
    }
    
    public String getInfo() {
        return _info;
    }
}

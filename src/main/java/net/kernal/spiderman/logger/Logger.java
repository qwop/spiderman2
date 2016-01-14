package net.kernal.spiderman.logger;

public interface Logger {
	
	public final static byte LEVEL_INFO		= 1;
	public final static byte LEVEL_DEBUG	= 2;
	public final static byte LEVEL_WARN		= 3;
	public final static byte LEVEL_ERROR	= 4;
	public final static byte LEVEL_OFF		= 5;
	
	public void info(String msg);
	public void debug(String msg);
	public void warn(String msg);
	public void error(String err, Throwable e);
	
}

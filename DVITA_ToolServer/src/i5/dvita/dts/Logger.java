package i5.dvita.dts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger 
{
	private static Logger instance = new Logger();
	
	public final static String LOG_FILE_NAME = "log.txt";
	
	private Logger()
	{
	}
	
	public static Logger getInstance()
	{
		return instance;
	}
	
	public synchronized void log(String message, LogType type, String path)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
	    String currentDateFormatted = "[" + dateFormat.format(new Date()) + "]";
	    String logTypeFormatted = "[" + type + "]";
	    String messageFormatted = "[" + message + "]";
	    		 
	    String logMessageFormatted = currentDateFormatted + logTypeFormatted + messageFormatted;
	    
		
		if (null == path)
		{
			System.out.println(logMessageFormatted);
		}
		else
		{			
			this._writeToLog(logMessageFormatted, path + File.separator + Logger.LOG_FILE_NAME);
		}
	}
	
	public synchronized void log(String message, LogType type)
	{
		log(message, type, Configuration.getInstance().getLoggerFolderPath());
	}
	
	private void _writeToLog(String message, String path)
	{
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true))))
		{
		    out.println(message);
		}
		catch (IOException e)
		{
		    // TODO exception handling
		}
	}
	
	public static enum LogType
	{
		GENERAL,
		ERROR,
		NETWORK,
		WARNING
	}
}

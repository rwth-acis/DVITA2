package i5.dvita.dts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSReturn.Status;
import i5.dvita.dts.Logger.LogType;

public class RequestHandlerGetRunningExecutionChainLog
{
	private String _runningExecutionChainUID = null;
	
	public RequestHandlerGetRunningExecutionChainLog(String runningExecutionChainUID)
	{
		this._runningExecutionChainUID = runningExecutionChainUID;
	}
	
	public DTSReturn<ArrayList<String>> process()
	{
		DTSReturn<ArrayList<String>> response = new DTSReturn<ArrayList<String>>();
		ArrayList<String> executionChainLog = null;
		
		File runningExecutionChainsFolder = new File(Configuration.getInstance().getConfigExecutionChainsRunningFolderPath());
		
		for (File runningExecutionChainFolder : runningExecutionChainsFolder.listFiles())
		{
			if (!runningExecutionChainFolder.getName().contains(this._runningExecutionChainUID))
			{
				continue;
			}
			
			File runningExecutionChainLog = new File(runningExecutionChainFolder.getAbsolutePath() + File.separator + Logger.LOG_FILE_NAME);
			
			try
			{
				executionChainLog = (ArrayList<String>) Files.readAllLines(Paths.get(runningExecutionChainLog.getAbsolutePath()), Configuration.ENCODING);
			} 
			catch (IOException e)
			{
				Logger.getInstance().log("can not read running execution chain log file: " + e.getMessage() + e.getCause(), LogType.ERROR);
				response.setMessage("Can't read running execution chain log");
				response.setStatus(Status.ERROR);
				return response;
			}
			
			break;
		}
		
		if (null == executionChainLog)
		{
			Logger.getInstance().log("can not find execution chain " + this._runningExecutionChainUID, LogType.ERROR);
			response.setMessage("Can't read running execution chain log");
			response.setStatus(Status.ERROR);
			response.setReturnValue(new ArrayList<String>());
			return response;
		}
		
		response.setReturnValue(executionChainLog);
		response.setStatus(Status.OKAY);
		return response;
	}
}

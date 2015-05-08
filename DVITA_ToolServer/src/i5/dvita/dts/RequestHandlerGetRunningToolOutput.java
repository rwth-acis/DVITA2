package i5.dvita.dts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSReturn.Status;
import i5.dvita.dts.Logger.LogType;

public class RequestHandlerGetRunningToolOutput
{
	private String _runningExecutionChainUID = null;
	private String _runningToolUID = null;
	
	public RequestHandlerGetRunningToolOutput(DTSGetRunningToolOutput dtsGetRunningToolOutput)
	{
		this._runningExecutionChainUID = dtsGetRunningToolOutput.getRunningExecutionChainUID();
		this._runningToolUID = dtsGetRunningToolOutput.getRunningToolUID();
	}
	
	public DTSReturn<ArrayList<String>> process()
	{
		DTSReturn<ArrayList<String>> response = new DTSReturn<ArrayList<String>>();
		ArrayList<String> toolOutput = null;
		
		File runningExecutionChainsFolder = new File(Configuration.getInstance().getConfigExecutionChainsRunningFolderPath());
		
		for (File runningExecutionChainFolder : runningExecutionChainsFolder.listFiles())
		{
			if (!runningExecutionChainFolder.getName().contains(this._runningExecutionChainUID))
			{
				continue;
			}
			
			File runningToolOutputFile = new File(runningExecutionChainFolder.getAbsolutePath() + File.separator + this._runningToolUID.replaceAll(":", "#") + File.separator + "output");
			
			try
			{
				toolOutput = (ArrayList<String>) Files.readAllLines(Paths.get(runningToolOutputFile.getAbsolutePath()), Configuration.ENCODING);
			} 
			catch (IOException e)
			{
				Logger.getInstance().log("can not read running tool output file: " + e.getMessage() + e.getCause(), LogType.ERROR);
				response.setMessage("Can't read tools output!");
				response.setStatus(Status.ERROR);
				return response;
			}
		}
		
		if (null == toolOutput)
		{
			Logger.getInstance().log("can not find execution chain of running tool, tool: " + this._runningToolUID + ", chain: " + this._runningExecutionChainUID, LogType.ERROR);
			response.setMessage("Can't read tools output!");
			response.setStatus(Status.ERROR);
			return response;
		}
		
		response.setReturnValue(toolOutput);
		response.setStatus(Status.OKAY);
		return response;
	}
}

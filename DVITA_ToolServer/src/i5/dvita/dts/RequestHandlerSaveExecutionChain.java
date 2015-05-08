package i5.dvita.dts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSReturn;
import i5.dvita.dts.Logger.LogType;

import com.thoughtworks.xstream.XStream;

public class RequestHandlerSaveExecutionChain 
{
	private DTSExecutionChain _dtsExecutionChain = null;
	
	public DTSReturn<Void> process()
	{
		DTSReturn<Void> response = new DTSReturn<Void>(null);
		
		String dtsExecutionChainAsXML = null;
		
		response.setMessage("we deed it :O");
		
		File dtsExecutionChainFile = new File
		(
			Configuration.getInstance().getExecutionChainsFolderPath() + File.separator + this._dtsExecutionChain.getName() + ".xml"
		);
		
		if(!dtsExecutionChainFile.exists())
		{
			try 
			{
				XStream xStream = new XStream();
				dtsExecutionChainAsXML = xStream.toXML(this._dtsExecutionChain);
				
				dtsExecutionChainFile.createNewFile();
				PrintWriter out = new PrintWriter(dtsExecutionChainFile);
				out.write(dtsExecutionChainAsXML);
				out.close();
				
				response.setMessage("Execution chain \"" + this._dtsExecutionChain.getName() + "\" successfully saved!");
				response.setStatus(DTSReturn.Status.OKAY);
			}
			catch (IOException e)
			{
				response.setMessage("Failed to save execution chain. See server logs for further details");
				response.setStatus(DTSReturn.Status.ERROR);
				Logger.getInstance().log("Can't save execution chain \"" + this._dtsExecutionChain.getName() + "\": " + e.getMessage(), LogType.ERROR);
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		else
		{
			response.setMessage("An execution chain with that name already exists!");
			response.setStatus(DTSReturn.Status.ERROR);
		}
		
		return response;
	}

	public DTSExecutionChain getDtsExecutionChain()
	{
		return this._dtsExecutionChain;
	}

	public void setDtsExecutionChain(DTSExecutionChain dtsExecutionChain)
	{
		this._dtsExecutionChain = dtsExecutionChain;
	}
}

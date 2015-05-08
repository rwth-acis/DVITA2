package i5.dvita.dts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.thoughtworks.xstream.XStream;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSReturn;

public class RequestHandlerLoadExecutionChain
{
	private String _dtsExecutionChainName = null;
	
	public DTSReturn<DTSExecutionChain> process()
	{
		DTSReturn<DTSExecutionChain> response = new DTSReturn<DTSExecutionChain>(null);
		
		String dtsExecutionChainFilePath = 
				Configuration.getInstance().getExecutionChainsFolderPath() + File.separator + this._dtsExecutionChainName + ".xml";

		File dtsExecutionChainFile = new File(dtsExecutionChainFilePath);
		
		if(dtsExecutionChainFile.exists())
		{
			try
			{
				String dtsExecutionChainAsXML = new String
						(Files.readAllBytes(Paths.get(dtsExecutionChainFilePath)), StandardCharsets.UTF_8);
				XStream xStream = new XStream();
				response.setReturnValue((DTSExecutionChain)xStream.fromXML(dtsExecutionChainAsXML));
				response.setStatus(DTSReturn.Status.OKAY);
			}
			catch (IOException e)
			{
				response.setMessage("Failed to read execution chain. See server logs for further details");
				response.setStatus(DTSReturn.Status.ERROR);
				// TODO log 
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else
		{
			response.setMessage("Execution chain " + this._dtsExecutionChainName + " does not exist!");
			response.setStatus(DTSReturn.Status.ERROR);
		}
		
		return response;
	}

	public String getDtsExecutionChainName()
	{
		return this._dtsExecutionChainName;
	}

	public void setDtsExecutionChainName(String dtsExecutionChainName)
	{
		this._dtsExecutionChainName = dtsExecutionChainName;
	}
}

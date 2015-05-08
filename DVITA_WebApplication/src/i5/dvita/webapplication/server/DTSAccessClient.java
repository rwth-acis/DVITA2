package i5.dvita.webapplication.server;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSRequest;
import i5.dvita.commons.DTSRequest.Request;
import i5.dvita.commons.DTSResponse;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.IDTSAccessClient;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.dbaccess.DBAccess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DTSAccessClient implements IDTSAccessClient
{
	private String _serverId = null;
	private HashMap<String, HashMap<String,String>> _dtsData = null;
	
	public DTSAccessClient()
	{
		this.initializeDTSData();
	}
	
	
	@Override
	public DTSReturn<Void> saveExecutionChain(DTSExecutionChain dtsExecutionChain)
	{
		DTSRequest<DTSExecutionChain> dtsRequest = new DTSRequest<DTSExecutionChain>(Request.SAVE_EXECUTION_CHAIN, dtsExecutionChain);
		DTSResponse<DTSReturn<Void>> dtsResponse = null;		
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		// TODO instanceof checks
		dtsResponse = (DTSResponse<DTSReturn<Void>>) this.doDTSRequest(dtsRequest);
		
		return dtsResponse.getReturnValue();
	}
	
	
	@Override
	public DTSReturn<DTSExecutionChain> loadExecutionChain(String dtsExecutionChainName)
	{
		DTSRequest<String> dtsRequest = new DTSRequest<String>(Request.LOAD_EXECUTION_CHAIN, dtsExecutionChainName);
		DTSResponse<DTSReturn<DTSExecutionChain>> dtsResponse = null;
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		dtsResponse = (DTSResponse<DTSReturn<DTSExecutionChain>>) this.doDTSRequest(dtsRequest);

		return dtsResponse.getReturnValue();
	}
	
	
	@Override
	public DTSReturn<DTSServerInformation> getServerInformation()
	{
		DTSRequest<Void> dtsRequest = new DTSRequest<Void>(Request.GET_SERVER_INFORMATION, null);
		DTSResponse<DTSReturn<DTSServerInformation>> dtsResponse = null;
		DTSServerInformation dtsServerInformation = null;
		DTSReturn<DTSServerInformation> dtsReturn = null;
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		dtsResponse = (DTSResponse<DTSReturn<DTSServerInformation>>) this.doDTSRequest(dtsRequest);
		dtsReturn = dtsResponse.getReturnValue();
		
		dtsServerInformation = dtsReturn.getReturnValue();
		
		dtsServerInformation.setId(this._dtsData.get(this._serverId).get("id"));
		dtsServerInformation.setName(this._dtsData.get(this._serverId).get("titel"));
		dtsServerInformation.setIpport(this._dtsData.get(this._serverId).get("ipAdr") + ":" + this._dtsData.get(this._serverId).get("portAdr"));
		dtsServerInformation.setDescription(this._dtsData.get(this._serverId).get("description"));
		
		return dtsReturn;
	}
	
	
	@Override
	public DTSReturn<Void> invokeExecutionChain(DTSExecutionChainInvoke dtsExecutionChainInvoke)
	{
		DTSRequest<DTSExecutionChainInvoke> dtsRequest = new DTSRequest<DTSExecutionChainInvoke>(Request.INVOKE_EXECUTION_CHAIN, dtsExecutionChainInvoke);
		DTSResponse<DTSReturn<Void>> dtsResponse = null;
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		for (Map.Entry<String, IDTSToolsIO> entry : dtsExecutionChainInvoke.getCustomInputs().entrySet())
		{
			if (entry.getValue() instanceof DTSToolsIOSQL)
			{
				((DTSToolsIOSQL) entry.getValue()).setDatabaseConfigurationAnalysis(
						(new DBAccess()).getConfigurationDataAnalysis(
								Integer.parseInt(entry.getValue().getData().get("analysisId"))));
			}
		}
		
		dtsResponse = (DTSResponse<DTSReturn<Void>>) this.doDTSRequest(dtsRequest);
		
		return dtsResponse.getReturnValue();
	}
	

	@Override
	public DTSReturn<ArrayList<String>> getRunningExecutionChainLog(String runningExecutionChainUID)
	{
		DTSRequest<String> dtsRequest = new DTSRequest<String>(Request.GET_RUNNING_EXECUTION_CHAIN_LOG, runningExecutionChainUID);
		DTSResponse<DTSReturn<ArrayList<String>>> dtsResponse = null;
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		dtsResponse = (DTSResponse<DTSReturn<ArrayList<String>>>) this.doDTSRequest(dtsRequest);
		
		return dtsResponse.getReturnValue();
	}
	
	
	@Override
	public DTSReturn<ArrayList<String>> getRunningToolOutput(DTSGetRunningToolOutput dtsGetRunningToolOutput)
	{
		DTSRequest<DTSGetRunningToolOutput> dtsRequest = new DTSRequest<DTSGetRunningToolOutput>(Request.GET_RUNNING_TOOL_OUTPUT, dtsGetRunningToolOutput);
		DTSResponse<DTSReturn<ArrayList<String>>> dtsResponse = null;
		
		if (null == this._serverId)
		{
			// client has to call selectServer(serverId) first!
			return null;
		}
		
		dtsResponse = (DTSResponse<DTSReturn<ArrayList<String>>>) this.doDTSRequest(dtsRequest);
		
		return dtsResponse.getReturnValue();
	}

	@Override
	public IDTSAccessClient selectServer(String serverId)
	{
		this._serverId = serverId;
		return this;
	}
	
	
	private void initializeDTSData()
	{
		DBAccess dbaccess = new DBAccess();
		this._dtsData = dbaccess.getConfigurationToolserver();
	}
	
	private DTSResponse doDTSRequest(DTSRequest dtsRequest)
	{
		DTSResponse dtsResponse = null;
		
		try
		{
			Socket dtsSocket = new Socket(this._dtsData.get(this._serverId).get("ipAdr"), Integer.parseInt(this._dtsData.get(this._serverId).get("portAdr")));
        	ObjectInputStream in = new ObjectInputStream(dtsSocket.getInputStream());
        	ObjectOutputStream out = new ObjectOutputStream(dtsSocket.getOutputStream());
        	
        	out.writeObject(dtsRequest);
        	
        	try
        	{
        		dtsResponse = (DTSResponse) in.readObject();
			}
        	catch (ClassNotFoundException e)
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    
        	out.close();
        	in.close();
        	dtsSocket.close();
		}
		catch (UnknownHostException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return dtsResponse;
	}
}

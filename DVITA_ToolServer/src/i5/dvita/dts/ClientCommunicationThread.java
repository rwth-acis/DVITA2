package i5.dvita.dts;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSRequest;
import i5.dvita.commons.DTSRequest.Request;
import i5.dvita.commons.DTSResponse;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ClientCommunicationThread extends Thread
{
	private Socket _socket = null;

    public ClientCommunicationThread(Socket socket)
    {
        super("ClientCommunicationThread");
        this._socket = socket;
    } 
    
    public void run()
    {
    	DTSAccessServer dtsAccess = new DTSAccessServer();
    	
    	ObjectOutputStream outObjectStream = null;
    	ObjectInputStream inObjectStream = null;
    	Object request = null;
    	Object response = null;
    	
		try
		{
	        // create connection streams
			outObjectStream = new ObjectOutputStream(this._socket.getOutputStream());
	        inObjectStream = new ObjectInputStream(this._socket.getInputStream());
	        
	        // receive request object
	        try 
	        {
	        	request = inObjectStream.readObject();
			}
	        catch (ClassNotFoundException e)
	        {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        // process request object
	        if (request instanceof DTSRequest)
	        {
	        	DTSRequest dtsRequest = (DTSRequest) request;
	        	
	        	if (dtsRequest.getRequestType().equals(Request.GET_SERVER_INFORMATION))
	        	{
	        		response = new DTSResponse<DTSReturn<DTSServerInformation>>(dtsAccess.getServerInformation());
	        	}
	        	else if (dtsRequest.getRequestType().equals(Request.SAVE_EXECUTION_CHAIN))
	        	{
	        		if (dtsRequest.getParameter() instanceof DTSExecutionChain)
	        		{
	        			response = new DTSResponse<DTSReturn<Void>>(dtsAccess.saveExecutionChain((DTSExecutionChain) dtsRequest.getParameter()));
	        		}
	        		else
	        		{
	        			// TODO exception handling
	        		}
	        	}
	        	else if (dtsRequest.getRequestType().equals(Request.LOAD_EXECUTION_CHAIN))
	        	{
	        		if (dtsRequest.getParameter() instanceof String)
	        		{
	        			response = new DTSResponse<DTSReturn<DTSExecutionChain>>(dtsAccess.loadExecutionChain((String) dtsRequest.getParameter()));
	        		}
	        	}
	        	else if (dtsRequest.getRequestType().equals(Request.INVOKE_EXECUTION_CHAIN))
	        	{
	        		if (dtsRequest.getParameter() instanceof DTSExecutionChainInvoke)
	        		{
	        			response = new DTSResponse<DTSReturn<Void>>(dtsAccess.invokeExecutionChain((DTSExecutionChainInvoke) dtsRequest.getParameter()));
	        		}
	        	}
	        	else if (dtsRequest.getRequestType().equals(Request.GET_RUNNING_TOOL_OUTPUT))
	        	{
	        		if (dtsRequest.getParameter() instanceof DTSGetRunningToolOutput)
	        		{
	        			response = new DTSResponse<DTSReturn<ArrayList<String>>>(dtsAccess.getRunningToolOutput((DTSGetRunningToolOutput) dtsRequest.getParameter()));
	        		}
	        	}
	        	else if (dtsRequest.getRequestType().equals(Request.GET_RUNNING_EXECUTION_CHAIN_LOG))
	        	{
	        		if (dtsRequest.getParameter() instanceof String)
	        		{
	        			response = new DTSResponse<DTSReturn<ArrayList<String>>>(dtsAccess.getRunningExecutionChainLog((String) dtsRequest.getParameter()));
	        		}
	        	}
	        	else
	        	{
					// TODO exception handling
				}
	        }
	        
	        // send request object response
	        outObjectStream.writeObject(response);
	        
	        // close connection streams
	        inObjectStream.close();
	        outObjectStream.close();
        	
	        // close connection
        	this._socket.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

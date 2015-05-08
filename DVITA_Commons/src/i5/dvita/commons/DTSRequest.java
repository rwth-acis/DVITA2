package i5.dvita.commons;

import java.io.Serializable;

public class DTSRequest<T> implements Serializable 
{
	private static final long serialVersionUID = 2546629159311347843L;
	
	private Request _requestType;
	private T _parameter;	
	
	public DTSRequest(Request type, T param)
	{
		this._requestType = type;
		this._parameter = param;
	}
	
	public T getParameter()
	{
		return this._parameter;
	}

	public void setParameter(T parameter)
	{
		this._parameter = parameter;
	}

	public Request getRequestType()
	{
		return this._requestType;
	}

	public void setRequestType(Request requestType)
	{
		this._requestType = requestType;
	}

	public static enum Request
	{
		GET_SERVER_INFORMATION,
		SAVE_EXECUTION_CHAIN,
		LOAD_EXECUTION_CHAIN,
		INVOKE_EXECUTION_CHAIN,
		GET_RUNNING_TOOL_OUTPUT,
		GET_RUNNING_EXECUTION_CHAIN_LOG
	}
}

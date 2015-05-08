package i5.dvita.commons;

import java.io.Serializable;

public class DTSGetRunningToolOutput implements Serializable
{
	private static final long serialVersionUID = 7078606031434028604L;
	
	private String _runningExecutionChainUID = null;
	private String _runningToolUID = null;
	
	public String getRunningExecutionChainUID()
	{
		return this._runningExecutionChainUID;
	}
	
	public void setRunningExecutionChainUID(String runningExecutionChainUID)
	{
		this._runningExecutionChainUID = runningExecutionChainUID;
	}
	
	public String getRunningToolUID()
	{
		return this._runningToolUID;
	}
	
	public void setRunningToolUID(String runningToolUID)
	{
		this._runningToolUID = runningToolUID;
	}

}

package i5.dvita.commons;

import java.util.ArrayList;

public interface IDTSAccess 
{
	public DTSReturn<DTSServerInformation> getServerInformation();
	public DTSReturn<Void> saveExecutionChain(DTSExecutionChain dtsExecutionChain);
	public DTSReturn<DTSExecutionChain> loadExecutionChain(String dtsExecutionChainName);
	public DTSReturn<Void> invokeExecutionChain(DTSExecutionChainInvoke dtsExecutionChainInvoke);
	public DTSReturn<ArrayList<String>> getRunningToolOutput(DTSGetRunningToolOutput dtsGetRunningToolOutput);
	public DTSReturn<ArrayList<String>> getRunningExecutionChainLog(String runningExecutionChainUID);
}

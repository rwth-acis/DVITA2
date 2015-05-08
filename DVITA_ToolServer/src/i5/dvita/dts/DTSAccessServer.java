package i5.dvita.dts;

import java.util.ArrayList;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.IDTSAccess;

public class DTSAccessServer implements IDTSAccess
{
	@Override
	public DTSReturn<DTSServerInformation> getServerInformation()
	{
		RequestHandlerServerInformation handlerServerInformation = new RequestHandlerServerInformation();
		return handlerServerInformation.process();
	}

	@Override
	public DTSReturn<Void> saveExecutionChain(DTSExecutionChain dtsExecutionChain)
	{
		RequestHandlerSaveExecutionChain handlerSaveExecutionChain = new RequestHandlerSaveExecutionChain();
		handlerSaveExecutionChain.setDtsExecutionChain(dtsExecutionChain);
		return handlerSaveExecutionChain.process();
	}

	@Override
	public DTSReturn<DTSExecutionChain> loadExecutionChain(String dtsExecutionChainName)
	{
		RequestHandlerLoadExecutionChain handlerLoadExecutionChain = new RequestHandlerLoadExecutionChain();
		handlerLoadExecutionChain.setDtsExecutionChainName(dtsExecutionChainName);
		return handlerLoadExecutionChain.process();
	}

	@Override
	public DTSReturn<Void> invokeExecutionChain(DTSExecutionChainInvoke dtsExecutionChainInvoke)
	{
		RequestHandlerInvokeExecutionChain handlerInvokeExecutionChain = new RequestHandlerInvokeExecutionChain(dtsExecutionChainInvoke);
		return handlerInvokeExecutionChain.process();
	}

	@Override
	public DTSReturn<ArrayList<String>> getRunningToolOutput(DTSGetRunningToolOutput dtsGetRunningToolOutput)
	{
		RequestHandlerGetRunningToolOutput handlerGetRunningToolOutput = new RequestHandlerGetRunningToolOutput(dtsGetRunningToolOutput);
		return handlerGetRunningToolOutput.process();
	}

	@Override
	public DTSReturn<ArrayList<String>> getRunningExecutionChainLog(String runningExecutionChainUID)
	{
		RequestHandlerGetRunningExecutionChainLog handlerGetRunningExecutionChainLog = new RequestHandlerGetRunningExecutionChainLog(runningExecutionChainUID);
		return handlerGetRunningExecutionChainLog.process();
	}
}

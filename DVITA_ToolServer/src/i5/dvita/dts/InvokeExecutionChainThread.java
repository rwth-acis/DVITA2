package i5.dvita.dts;

import i5.dvita.commons.DTSExecutionChainConnection;
import i5.dvita.commons.DTSExecutionChainRunningInformation;
import i5.dvita.commons.DTSToolsIOFile;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.dts.Logger.LogType;
import i5.dvita.dts.RequestHandlerInvokeExecutionChain.DTSToolEC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InvokeExecutionChainThread extends Thread
{
	private HashMap<String, DTSToolEC> _dtsToolsEC = null;
	private List<DTSExecutionChainConnection> _dtsExecutionChainConnections = null;
	private DTSExecutionChainRunningInformation _runningInfo = null;
	
	public InvokeExecutionChainThread(HashMap<String, DTSToolEC> dtsToolsEC, List<DTSExecutionChainConnection> dtsExecutionChainConnections, DTSExecutionChainRunningInformation runningInfo)
	{
		super("InvokeExecutionChainThread");
		this._dtsToolsEC = dtsToolsEC;
		this._dtsExecutionChainConnections = dtsExecutionChainConnections;
		this._runningInfo = runningInfo;
		RequestHandlerInvokeExecutionChain.update(this._runningInfo);
	}
	
	@Override
	public void run()
	{
		do
		{
			// iterate DTSToolsEC
			for (Map.Entry<String, DTSToolEC> dtsToolEC : this._dtsToolsEC.entrySet())
			{
				// skip finished tools
				if (dtsToolEC.getValue().isExecutionFinished())
				{
					continue;
				}
				
				// skip tools with missing inputs
				if (!dtsToolEC.getValue().isInputReady())
				{
					continue;
				}
				
				Logger.getInstance().log
					("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": Tool " + dtsToolEC.getValue().getDtsTool().getToolName() + " is ready, executing now!", LogType.GENERAL, this._runningInfo.getAbsolutePath());
				this._runningInfo.getRunningTool(dtsToolEC.getValue().getUid()).setStatus(DTSExecutionChainRunningInformation.STATUS_RUNNING);
				RequestHandlerInvokeExecutionChain.update(this._runningInfo);
				dtsToolEC.getValue().execute(this._runningInfo);
				
				if (this._runningInfo.getRunningTool(dtsToolEC.getValue().getUid()).equals(DTSExecutionChainRunningInformation.STATUS_ERROR))
				{
					Logger.getInstance().log
						("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": Tool " + dtsToolEC.getValue().getDtsTool().getToolName() + " failed", LogType.ERROR, this._runningInfo.getAbsolutePath());
					this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
					RequestHandlerInvokeExecutionChain.update(this._runningInfo);
					break;
				}
				else
				{
					dtsToolEC.getValue().setExecutionIsFinished(true);
					this._runningInfo.getRunningTool(dtsToolEC.getValue().getUid()).setStatus(DTSExecutionChainRunningInformation.STATUS_FINISHED);
					RequestHandlerInvokeExecutionChain.update(this._runningInfo);
				}
				
				Logger.getInstance().log
					("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": Tool " + dtsToolEC.getValue().getDtsTool().getToolName() + " finished, now I/O", LogType.GENERAL, this._runningInfo.getAbsolutePath());
				
				// iterate connections to find the targets of this tools outputs
				for (DTSExecutionChainConnection dtsExecutionChainConnection : this._dtsExecutionChainConnections)
				{
					if (!dtsExecutionChainConnection.getOutputToolUID().toString().equals(dtsToolEC.getKey()))
					{
						continue;
					}
					
					// output DTSToolIO (source)
					IDTSToolsIO dtsToolsIOOutput = dtsToolEC.getValue().getDtsTool().getOutputs().get(dtsExecutionChainConnection.getOutputDTSIOString());
					
					// input DTSToolEC
					DTSToolEC dtsToolECInput = this._dtsToolsEC.get(dtsExecutionChainConnection.getInputToolUID().toString());
					
					// input DTSToolIO (target)
					IDTSToolsIO dtsToolsIOInput = dtsToolECInput.getDtsTool().getInputs().get(dtsExecutionChainConnection.getInputDTSIOString());
					
					if (dtsToolsIOOutput instanceof DTSToolsIOFile)
					{
						try
						{
							String targetFilePath = dtsToolECInput.getAbsoluteFolderPath() + File.separator + dtsToolsIOInput.getData().get("filename");		
							Logger.getInstance().log
								("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": I/O file, copy " + dtsToolsIOOutput.getData().get("absoluteFilePath") + "(" + dtsToolEC.getValue().getDtsTool().getToolName() + ") to " + targetFilePath + "(" + dtsToolECInput.getDtsTool().getToolName() + ")", LogType.GENERAL, this._runningInfo.getAbsolutePath());
							Files.copy(Paths.get(dtsToolsIOOutput.getData().get("absoluteFilePath")), Paths.get(targetFilePath));
							dtsToolsIOInput.getData().put("absoluteFilePath", targetFilePath);
						}
						catch (IOException e)
						{
							Logger.getInstance().log
								("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": I/O file copy fail: " + e.getMessage() + e.getCause(), LogType.ERROR, this._runningInfo.getAbsolutePath());
							this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
							RequestHandlerInvokeExecutionChain.update(this._runningInfo);
							break;
						}
					}
					else if (dtsToolsIOOutput instanceof DTSToolsIOSQL)
					{
						try
						{
							Logger.getInstance().log
								("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": I/O SQL, transfer " + ((DTSToolsIOSQL) dtsToolsIOOutput).getDatabaseConfigurationAnalysis().metaTitle + "(" + dtsToolEC.getValue().getDtsTool().getToolName() + ") to tool " + dtsToolECInput.getDtsTool().getToolName(), LogType.GENERAL, this._runningInfo.getAbsolutePath());
							((DTSToolsIOSQL) dtsToolsIOInput).setDatabaseConfigurationAnalysis(((DTSToolsIOSQL) dtsToolsIOOutput).getDatabaseConfigurationAnalysis());
						} 
						catch (NullPointerException e)
						{
							Logger.getInstance().log
								("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": I/O SQL transfer failed: " + e.getMessage() + e.getCause(), LogType.ERROR, this._runningInfo.getAbsolutePath());
							this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
							RequestHandlerInvokeExecutionChain.update(this._runningInfo);
							break;
						}
					}
					else
					{
						Logger.getInstance().log
							("Execution chain " + this._runningInfo.getRunningExecutionChainName() + ": I/O unknown: " + dtsToolsIOOutput.toString(), LogType.ERROR, this._runningInfo.getAbsolutePath());
					}
				}
				
				if (this._runningInfo.getStatus().equals(DTSExecutionChainRunningInformation.STATUS_ERROR))
				{
					break;
				}
			}
		} while (!this.isFinished() && !this._runningInfo.getStatus().equals(DTSExecutionChainRunningInformation.STATUS_ERROR));
		
		if (!this._runningInfo.getStatus().equals(DTSExecutionChainRunningInformation.STATUS_ERROR))
		{
			Logger.getInstance().log
				("Execution chain " + this._runningInfo.getRunningExecutionChainName() + " finished!", LogType.GENERAL, this._runningInfo.getAbsolutePath());
			this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_FINISHED);
			RequestHandlerInvokeExecutionChain.update(this._runningInfo);
		}
		else
		{
			Logger.getInstance().log
				("Execution chain " + this._runningInfo.getRunningExecutionChainName() + " aborted", LogType.ERROR, this._runningInfo.getAbsolutePath());
		}
	}
	
	private Boolean isFinished()
	{	
		for (Map.Entry<String, DTSToolEC> dtsToolEC : this._dtsToolsEC.entrySet())
		{
			if (!dtsToolEC.getValue().isExecutionFinished())
			{
				return false;
			}
		}
		
		return true;
	}	
}

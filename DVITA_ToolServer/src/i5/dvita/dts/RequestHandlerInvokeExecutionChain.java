package i5.dvita.dts;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;

import i5.dvita.commons.DTSExecutionChainConnection;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSExecutionChainRunningInformation;
import i5.dvita.commons.DTSExecutionChainUniqueId;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DTSReturn.Status;
import i5.dvita.commons.DTSTool;
import i5.dvita.commons.DTSToolsIOFile;
import i5.dvita.commons.DTSToolsIOString;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.dts.Logger.LogType;

public class RequestHandlerInvokeExecutionChain
{
	private DTSExecutionChainInvoke _dtsExecutionChainInvoke = null;
	private HashMap<String, DTSToolEC> _dtsToolsEC = new HashMap<String, DTSToolEC>();
	private File _executionChainFolder = null;
	private DTSServerInformation _dtsServerInformation = null;
	private DTSExecutionChainRunningInformation _runningInfo = null;
	
	public RequestHandlerInvokeExecutionChain(DTSExecutionChainInvoke dtsExecutionChainInvoke)
	{
		String id = UUID.randomUUID().toString().replaceAll("-", "");
		this._dtsExecutionChainInvoke = dtsExecutionChainInvoke;
		this._dtsServerInformation = (new RequestHandlerServerInformation()).process().getReturnValue();
		this._executionChainFolder = new File
		(
				Configuration.getInstance().getConfigExecutionChainsRunningFolderPath()
				+ File.separator
				+ this._dtsExecutionChainInvoke.getTemplateExecutionChainName()
				+ "#"
				+ id
		);
		
		this._runningInfo = new DTSExecutionChainRunningInformation();
		this._runningInfo.setAbsolutePath(this._executionChainFolder.getAbsolutePath());
		this._runningInfo.setTemplateExecutionChainName(this._dtsExecutionChainInvoke.getTemplateExecutionChainName());
		this._runningInfo.setRunningExecutionChainName(this._dtsExecutionChainInvoke.getRunningExecutionChainName());
		this._runningInfo.setId(id);
		this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_RUNNING);
	}

	public DTSReturn<Void> process()
	{
		DTSReturn<Void> response = new DTSReturn<Void>(null);
		
		_executionChainFolder.mkdirs();
		
		if (!_executionChainFolder.isDirectory())
		{
			response.setMessage("Invoke Execution Chain error: can not create execution chain folder");
			response.setStatus(Status.ERROR);
			return response;
		}
		
		List<DTSExecutionChainConnection> dtsExecutionChainConnections =
				_dtsServerInformation.getExecutionChains().get(this._dtsExecutionChainInvoke.getTemplateExecutionChainName()).getConnections();
		
		// iterate connections
		for (DTSExecutionChainConnection dtsExecutionChainConnection : dtsExecutionChainConnections)
		{
			// input part
			if (!dtsExecutionChainConnection.getInputToolUID().getType().equals("tool"))
			{
				// TODO error, logging?!
				response.setMessage("Invoke Execution Chain error: invalid chain connection");
				response.setStatus(Status.ERROR);
				return response;
			}

			DTSExecutionChainUniqueId dtsExecutionChainUIDInputTool = dtsExecutionChainConnection.getInputToolUID();
			
			if(!this._dtsToolsEC.containsKey(dtsExecutionChainUIDInputTool.toString()))
			{
				String msg = this.initializeDTSToolsEC(dtsExecutionChainUIDInputTool);
				
				if (null != msg)
				{
					response.setMessage(msg);
					response.setStatus(Status.ERROR);
					this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
					update(this._runningInfo);
					return response;
				}
			}
			
			// output part, can also be customInput
			if (dtsExecutionChainConnection.getOutputToolUID().getType().equals("customInput"))
			{
				// carry out connection customInput -> tool
				// transfer output data into input
				// data given by Invoke parameter
				IDTSToolsIO dtsToolOutput = 
						this._dtsExecutionChainInvoke.getCustomInputs().get(dtsExecutionChainConnection.getOutputToolUID().toString());
				IDTSToolsIO dtsToolInput = 
						this._dtsToolsEC.get(dtsExecutionChainConnection.getInputToolUID().toString())
							.getDtsTool().getInputs().get(dtsExecutionChainConnection.getInputDTSIOString());
				
				// TODO more error handling?! (are input/output types matching, is output not null, matches customInput type with DTSToolIO class,...
				if (dtsToolOutput instanceof DTSToolsIOString)
				{
					dtsToolInput.setData(dtsToolOutput.getData());
				}
				else if (dtsToolOutput instanceof DTSToolsIOSQL)
				{
					((DTSToolsIOSQL) dtsToolInput).setDatabaseConfigurationAnalysis
					(
						((DTSToolsIOSQL) dtsToolOutput).getDatabaseConfigurationAnalysis()
					);
				}
				else
				{
					// TODO error, logging?!
					response.setMessage("Invoke Execution Chain error: invalid customInput");
					response.setStatus(Status.ERROR);
					return response;
				}
				
			}
			else if (dtsExecutionChainConnection.getOutputToolUID().getType().equals("tool"))
			{
				DTSExecutionChainUniqueId dtsExecutionChainUIDOutputTool = dtsExecutionChainConnection.getOutputToolUID();
				
				if(!this._dtsToolsEC.containsKey(dtsExecutionChainUIDOutputTool.toString()))
				{
					String msg = this.initializeDTSToolsEC(dtsExecutionChainUIDOutputTool);
					
					if (null != msg)
					{
						response.setMessage(msg);
						response.setStatus(Status.ERROR);
						return response;
					}
				}
			}
			else
			{
				// TODO error, logging?!
				response.setMessage("Invoke Execution Chain error: invalid chain connection");
				response.setStatus(Status.ERROR);
				return response;
			}
		}
		
		// initialization complete. send response to finish the request
		// the chain execution itself is done in another thread
		new InvokeExecutionChainThread(this._dtsToolsEC, dtsExecutionChainConnections, this._runningInfo).start();
		
		response.setStatus(Status.OKAY);
		response.setMessage("Execution Chain successfully started!");
		
		return response;
	}
	
	public static void update(DTSExecutionChainRunningInformation runningInfo)
	{
		XStream xStream = new XStream();
		String runningInfoAsXML = xStream.toXML(runningInfo);
		
		File runningInfoXMLFile = new File(runningInfo.getAbsolutePath() + File.separator + "runningInfo.xml");
		
		try
		{
			runningInfoXMLFile.createNewFile();
		} 
		catch (IOException e)
		{
			Logger.getInstance().log
			(
				"Can not create execution chain running information xml: " + e.getMessage() + e.getCause(),
				LogType.ERROR,
				runningInfo.getAbsolutePath()
			);
		}
		
		try
		{
			PrintWriter out = new PrintWriter(runningInfoXMLFile);
			out.write(runningInfoAsXML);
			out.close();
		} 
		catch (FileNotFoundException e)
		{
			Logger.getInstance().log
			(
				"Can not write to execution chain running information xml: " + e.getMessage() + e.getCause(),
				LogType.ERROR,
				runningInfo.getAbsolutePath()
			);
		}
	}

	public DTSExecutionChainInvoke getDtsExecutionChainInvoke()
	{
		return this._dtsExecutionChainInvoke;
	}

	public void setDtsExecutionChainInvoke(DTSExecutionChainInvoke dtsExecutionChainInvoke)
	{
		this._dtsExecutionChainInvoke = dtsExecutionChainInvoke;
	}
	
	public String initializeDTSToolsEC(DTSExecutionChainUniqueId dtsExecutionChainUniqueId)
	{
		DTSToolEC dtsToolEC = new DTSToolEC(_dtsServerInformation.getDTSTools().get(dtsExecutionChainUniqueId.getId()));
		
		this._dtsToolsEC.put
		(
			dtsExecutionChainUniqueId.toString(),
			dtsToolEC
		);
		
		// create tool folder within this execution chain folder and copy data
		dtsExecutionChainUniqueId.setSeperator("#");
		File dtsECToolFolder = new File
				(_executionChainFolder.getAbsolutePath() + File.separator + dtsExecutionChainUniqueId.toString());
		dtsECToolFolder.mkdirs();
		if (!dtsECToolFolder.isDirectory())
		{
			String message = "Invoke execution chain error: can not create execution tool folder";
			Logger.getInstance().log(message, LogType.ERROR, this._runningInfo.getAbsolutePath());
			return message;
		}
		dtsToolEC.setUid(dtsExecutionChainUniqueId.toString());
		this._runningInfo.addRunningTool(dtsExecutionChainUniqueId.toString());
		dtsExecutionChainUniqueId.setSeperator(":");
		
		File dtsToolDataFolder = new File
				(Configuration.getInstance().getToolCollectionFolderPath() + File.separator + dtsToolEC.getDtsTool().getToolName() + File.separator + Configuration.TOOL_DATA_FOLDER_NAME);
		if (!dtsToolDataFolder.isDirectory())
		{
			String message = "Invoke Execution Chain error: can not access execution tool data folder";
			Logger.getInstance().log(message, LogType.ERROR, this._runningInfo.getAbsolutePath());
			return message;
		}
		
		this.copyToolFolderData(dtsToolDataFolder.getAbsolutePath(), dtsECToolFolder.getAbsolutePath());
		
		dtsToolEC.setAbsoluteFolderPath(dtsECToolFolder.getAbsolutePath());
		
		return null;
	}
	
	private void copyToolFolderData(String source, String destination)
	{
		File srcDir = new File(source);
		File dstSubFolder = null;
		
		for (File dtsToolDataFile : srcDir.listFiles())
		{
			if (dtsToolDataFile.isDirectory())
			{
				dstSubFolder = new File(destination + File.separator + dtsToolDataFile.getName());
				dstSubFolder.mkdirs();
				copyToolFolderData(dtsToolDataFile.getAbsolutePath(), dstSubFolder.getAbsolutePath());
			}
			else
			{
				try
				{
					Files.copy(Paths.get(dtsToolDataFile.getAbsolutePath()), Paths.get(destination + File.separator + dtsToolDataFile.getName()));
				} 
				catch (IOException e)
				{
					Logger.getInstance().log
					(
						"Can not copy tool data to execution chain running folder: " + e.getMessage() + e.getCause(),
						LogType.ERROR,
						this._runningInfo.getAbsolutePath()
					);
					this._runningInfo.setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
					update(this._runningInfo);
				}
			}
		}
	}
	
	public class DTSToolEC
	{
		private DTSTool _dtsTool = null;
		private String _absoluteFolderPath = null;
		private Boolean _executionIsFinished = false;
		private String _uid = null;

		public DTSToolEC(DTSTool dtsTool)
		{
			this._dtsTool = dtsTool;
		}
		
		public void execute(DTSExecutionChainRunningInformation runningInfo)
		{
			Process process = null;
			Integer executionReturnValue = null;
	
			Logger.getInstance().log
				("Tool execution starts: " + this._dtsTool.getToolName(), LogType.GENERAL,runningInfo.getAbsolutePath());
			
			String executionCommand = this._dtsTool.getCommand();
			
			executionCommand = this.replaceParametersWithValues(executionCommand);
			this.replaceToolsIOParameters();
			
			Logger.getInstance().log
				("Tool execution command: " + executionCommand, LogType.GENERAL,runningInfo.getAbsolutePath());
			
			try
			{
				ProcessBuilder processBuilder = null;
				
				if (Configuration.getInstance().getOperatingSystem().equalsIgnoreCase(Configuration.OPERATING_SYSTEM_WINDOWS))
				{
					processBuilder = new ProcessBuilder("cmd", "/c", executionCommand);
				}
				else if (Configuration.getInstance().getOperatingSystem().equalsIgnoreCase(Configuration.OPERATING_SYSTEM_LINUX))
				{
					processBuilder = new ProcessBuilder("bash", "-c", executionCommand);
				}

				if (null == processBuilder)
				{
					Logger.getInstance().log("Can not create ProcessBuilder: unknown OS", Logger.LogType.ERROR);
					System.exit(-1);
				}
				
				processBuilder.directory(new File(_absoluteFolderPath));
				processBuilder.redirectErrorStream(true);
				
				if (!this.hasIPC())
				{
					processBuilder.redirectOutput(new File(_absoluteFolderPath+File.separator+"output"));
				}
				
				process = processBuilder.start();
				
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + " started!", LogType.GENERAL,runningInfo.getAbsolutePath());

				if (this.hasIPC())
				{
					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + " uses IPC", LogType.GENERAL,runningInfo.getAbsolutePath());
					
					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC create output stream", LogType.GENERAL,runningInfo.getAbsolutePath());
					ObjectOutputStream outputStream = new ObjectOutputStream(process.getOutputStream());

					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC create input stream", LogType.GENERAL,runningInfo.getAbsolutePath());
					ObjectInputStream inputStream = new ObjectInputStream(process.getInputStream());
					
					Object inputObject = null;
					
					// send input data
					HashMap<String, DatabaseConfigurationAnalysis> dataIPC = new HashMap<String, DatabaseConfigurationAnalysis>();
								
					for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getInputs().entrySet())
					{
						if(entry.getValue().isIPC())
						{
							if (entry.getValue() instanceof DTSToolsIOSQL)
							{
								Logger.getInstance().log
									("Tool " + this._dtsTool.getToolName() + ": IPC send SQL data, db: " + ((DTSToolsIOSQL) entry.getValue()).getDatabaseConfigurationAnalysis().metaTitle, LogType.GENERAL, runningInfo.getAbsolutePath());
								dataIPC.put(entry.getValue().getId(), ((DTSToolsIOSQL) entry.getValue()).getDatabaseConfigurationAnalysis());
							}		
						}
					}

					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC send data...", LogType.GENERAL,runningInfo.getAbsolutePath());
					outputStream.writeObject(dataIPC);
					
					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC send data complete, " + dataIPC.size() + " objects sent", LogType.GENERAL,runningInfo.getAbsolutePath());

					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC output stream closed", LogType.GENERAL,runningInfo.getAbsolutePath());
					outputStream.close();
					
					// receive output
					FileWriter outputWriter = new FileWriter(_absoluteFolderPath+File.separator+"output");
					while (true)
					{
						try
						{
							inputObject = inputStream.readObject();
						} 
						catch (EOFException e)
						{
							Logger.getInstance().log
								("Tool " + this._dtsTool.getToolName() + ": IPC EOF", LogType.GENERAL,runningInfo.getAbsolutePath());
							break;
						}
						catch (StreamCorruptedException e) 
						{
							// tool sends invalid data. since we don't know what the tool is doing with it's StdOut,
							// we cancel receiving data from the tool and just wait for it to finish
							// all inputs are passed through to the outputs
							((DTSToolsIOSQL) this._dtsTool.getOutputs().get("outputSQL")).setDatabaseConfigurationAnalysis(((DTSToolsIOSQL) this._dtsTool.getInputs().get("inputSQL")).getDatabaseConfigurationAnalysis());
							Logger.getInstance().log
								("Tool " + this._dtsTool.getToolName() + ": IPC stream corrupted. switch to data-pass-through state", LogType.WARNING,runningInfo.getAbsolutePath());
							break;
						}
						
						if (inputObject instanceof String)
						{
							outputWriter.write((String) inputObject);
							outputWriter.write('\n');
							outputWriter.flush();
						}
						else if (inputObject instanceof HashMap<?,?>)
						{					
							for (Map.Entry<?, ?> entry : ((HashMap<?,?>) inputObject).entrySet())
							{
								if (entry.getValue() instanceof DatabaseConfigurationAnalysis)
								{
									try
									{
										((DTSToolsIOSQL) this._dtsTool.getOutputs().get(entry.getKey())).setDatabaseConfigurationAnalysis((DatabaseConfigurationAnalysis) entry.getValue());
										Logger.getInstance().log
											("Tool " + this._dtsTool.getToolName() + ": IPC received SQL: " + ((DatabaseConfigurationAnalysis) entry.getValue()).metaTitle, LogType.GENERAL,runningInfo.getAbsolutePath());
									} 
									catch (Exception e)
									{
										Logger.getInstance().log
											("Tool " + this._dtsTool.getToolName() + ": IPC failure while receiving SQL " + e.getMessage() + e.getCause(), LogType.ERROR, runningInfo.getAbsolutePath());
										runningInfo.getRunningTools().get(this._uid.replaceAll(":", "#")).setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
										update(runningInfo);
									}
								}
								else
								{
									Logger.getInstance().log
										("Tool " + this._dtsTool.getToolName() + ": IPC unknown data", LogType.WARNING, runningInfo.getAbsolutePath());
								}
							}
						}
						else
						{
							Logger.getInstance().log
								("Tool " + this._dtsTool.getToolName() + ": IPC unknown data", LogType.WARNING, runningInfo.getAbsolutePath());
						}
					}
					
					outputWriter.close();
					
					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": IPC close input stream", LogType.GENERAL, runningInfo.getAbsolutePath());
					inputStream.close();
				}
				
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + ": running...", LogType.GENERAL, runningInfo.getAbsolutePath());
				executionReturnValue = process.waitFor();
				
				if (0 != executionReturnValue)
				{
					Logger.getInstance().log
						("Tool " + this._dtsTool.getToolName() + ": returned with exit code " + executionReturnValue, LogType.WARNING, runningInfo.getAbsolutePath());
				}
			} 
			catch (IOException e)
			{
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + ": execution IO error: " + e.getMessage() + e.getCause(), LogType.ERROR, runningInfo.getAbsolutePath());
				runningInfo.getRunningTools().get(this._uid.replaceAll(":", "#")).setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
				update(runningInfo);
				
				e.printStackTrace();
				process.destroy();				
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ClassNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if (null == executionReturnValue)
			{
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + ": execution failed", LogType.ERROR, runningInfo.getAbsolutePath());
				runningInfo.getRunningTools().get(this._uid.replaceAll(":", "#")).setStatus(DTSExecutionChainRunningInformation.STATUS_ERROR);
				update(runningInfo);

			}
			else 
			{
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + ": finished! checking for outputs...", LogType.GENERAL, runningInfo.getAbsolutePath());
				
				for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getOutputs().entrySet())
				{
					if (entry.getValue() instanceof DTSToolsIOFile)
					{
						Logger.getInstance().log
							("Tool " + this._dtsTool.getToolName() + ": file output: " + entry.getValue().getData().get("filename"), LogType.GENERAL, runningInfo.getAbsolutePath());
						entry.getValue().getData().put("absoluteFilePath", this.getAbsoluteFolderPath() + File.separator + entry.getValue().getData().get("filename"));
					}
				}
				
				Logger.getInstance().log
					("Tool " + this._dtsTool.getToolName() + ": output done", LogType.GENERAL, runningInfo.getAbsolutePath());
			}
		}
		
		public DTSTool getDtsTool()
		{
			return this._dtsTool;
		}
		
		public Boolean isInputReady()
		{
			Boolean inputReady = true;
			for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getInputs().entrySet())
			{
				if(!entry.getValue().isDataAvailable())
				{
					inputReady = false;
					break;
				}
			}
			
			return inputReady;
		}
		
		private Boolean hasIPC()
		{
			for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getInputs().entrySet())
			{
				if(entry.getValue().isIPC())
				{
					return true;
				}
			}
			
			for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getOutputs().entrySet())
			{
				if(entry.getValue().isIPC())
				{
					return true;
				}
			}
			
			return false;
		}
		
		
		private void replaceToolsIOParameters()
		{
			for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getInputs().entrySet())
			{
				for (String parameter : entry.getValue().getParameters())
				{
					String inputId = parameter.replaceAll("%", "");
					entry.getValue().replaceParameterWithValue(parameter, this._dtsTool.getInputs().get(inputId).getData().get("text"));
				}
			}
			
			for (Map.Entry<String, IDTSToolsIO> entry : this._dtsTool.getOutputs().entrySet())
			{
				for (String parameter : entry.getValue().getParameters())
				{
					String inputId = parameter.replaceAll("%", "");
					entry.getValue().replaceParameterWithValue(parameter, this._dtsTool.getInputs().get(inputId).getData().get("text"));
				}
			}
		}
		
		private String replaceParametersWithValues(String command)
		{
			for (String parameter : this._dtsTool.getParameters())
			{
				String inputId = parameter.replaceAll("%", "");
				command = command.replaceAll(parameter, this._dtsTool.getInputs().get(inputId).getData().get("text"));
			}
			
			return command;
		}

		public String getAbsoluteFolderPath()
		{
			return this._absoluteFolderPath;
		}

		public void setAbsoluteFolderPath(String absoluteFolderPath)
		{
			this._absoluteFolderPath = absoluteFolderPath;
		}

		public Boolean getExecutionIsFinished()
		{
			return _executionIsFinished;
		}

		public void setExecutionIsFinished(Boolean executionIsFinished)
		{
			this._executionIsFinished = executionIsFinished;
		}
		
		public Boolean isExecutionFinished()
		{
			return this.getExecutionIsFinished();
		}

		public String getUid()
		{
			return _uid;
		}

		public void setUid(String uid)
		{
			this._uid = uid;
		}
	}
}

package i5.dvita.dts;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration
{
	private static Configuration instance = new Configuration();
	
	private final static Integer CONFIG_DEFAULT_SERVER_PORT = 23572;
	private final static String CONFIG_DEFAULT_FILE_NAME = "dtsconfig.txt";
	private final static String CONFIG_DEFAULT_TOOL_COLLECTION_FOLDER_PATH = "dtstools";
	private final static String CONFIG_DEFAULT_EXECUTION_CHAINS_FOLDER_PATH = "executionChains";
	private final static String CONFIG_DEFAULT_EXECUTION_CHAINS_RUNNING_FOLDER_PATH = "executionChainsRunning";
	
	public final static String TOOL_MANIFEST_FILE_NAME = "manifest.xml";
	public final static String TOOL_DATA_FOLDER_NAME = "data";
	
	public final static String OPERATING_SYSTEM_WINDOWS = "WINDOWS";
	public final static String OPERATING_SYSTEM_LINUX = "LINUX";
	
	private InetAddress _configNetworkIP = null;
	private Integer _configNetworkPort = CONFIG_DEFAULT_SERVER_PORT;
	private Integer _configNetworkPort2 = 23572;
	private String _configToolCollectionFolderPath = CONFIG_DEFAULT_TOOL_COLLECTION_FOLDER_PATH;
	private String _configLoggerFolderPath = null;
	private String _configExecutionChainsFolderPath = CONFIG_DEFAULT_EXECUTION_CHAINS_FOLDER_PATH;
	private String _configExecutionChainsRunningFolderPath = CONFIG_DEFAULT_EXECUTION_CHAINS_RUNNING_FOLDER_PATH;
	private String _operatingSystem = null;
		
	public final static Charset ENCODING = StandardCharsets.UTF_8;
	
	private Configuration()
	{
		if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
		{
			setOperatingSystem(Configuration.OPERATING_SYSTEM_WINDOWS);
		}
		else if (System.getProperty("os.name").toUpperCase().contains("LINUX"))
		{
			setOperatingSystem(Configuration.OPERATING_SYSTEM_LINUX);
		}
		else
		{
			System.out.println("Error on configuration: Can not determine operating system");
			System.exit(-1);
		}
	}
	
	public static Configuration getInstance()
	{
		return instance;
	}
	
	public Boolean readConfigFile(String[] args)
	{
		// args is the user entered parameter input for the DTS
		// do all the error stuff for wrong server startup
		File configFile = null;
		List<String> configFileLines = null;
		
		if (1 == args.length)
		{
			configFile = new File(args[0]);
		}
		
		if (0 == args.length)
		{
			configFile = new File(CONFIG_DEFAULT_FILE_NAME);
		}
		
		if ((1 < args.length))
		{
			System.out.println("Error on startup: parameter has to be path to server configuration file.");
			return false;
		}

		if((1 == args.length) && (!configFile.exists() || configFile.isDirectory()))
		{
			System.out.println("Error on startup: given configuration file does not exist or is a directory");
			return false;
		}
		
		if(!configFile.exists() || configFile.isDirectory())
		{
			System.out.println("No configuration file found, using default settings");
			
			// at least we need a tool folder!
			File toolFolder = new File(CONFIG_DEFAULT_TOOL_COLLECTION_FOLDER_PATH);
			
			if(!toolFolder.isDirectory())
			{
				System.out.println("Error on startup: you need a tool folder.");
				return false;
			}
			
			// ... and a executionChain folder!
			File executionChainsFolder = new File(this._configExecutionChainsFolderPath);
			
			if(!executionChainsFolder.isDirectory())
			{
				executionChainsFolder.mkdirs();
				
				if(!executionChainsFolder.isDirectory())
				{
					System.out.println("Error on startup: can not create executionChains folder.");
					return false;
				}
			}
		}
		else
		{
			try
			{
				configFileLines = Files.readAllLines(Paths.get(configFile.getPath()), ENCODING);
			}
			catch (IOException e)
			{			
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (String setting : configFileLines)
			{
				List<String> settingExploded = new ArrayList<String>(Arrays.asList(setting.split("=")));
				
				if (2 != settingExploded.size())
				{
					// invalid line, skip
					continue;
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("PORT"))
				{
					this._configNetworkPort2 = Integer.parseInt(settingExploded.get(1));
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("IP"))
				{
					try
					{
						this._configNetworkIP = InetAddress.getByName(settingExploded.get(1));
					}
					catch (UnknownHostException e)
					{					
						System.out.println("Error on startup: IP given in configuration file is invalid.");
						System.out.println(e.getMessage());
						return false;
					}
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("TOOLFOLDER"))
				{
					File toolFolder = new File(settingExploded.get(1));
					
					if(!toolFolder.isDirectory())
					{
						System.out.println("Error on startup: invalid tool folder given in configuration file");
						return false;
					}						

					this._configToolCollectionFolderPath = settingExploded.get(1);
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("EXECUTIONCHAINSFOLDER"))
				{
					String executionChainsFolderString = settingExploded.get(1);
					
					if (executionChainsFolderString.substring(executionChainsFolderString.length()-1).equals("/")
						|| executionChainsFolderString.substring(executionChainsFolderString.length()-1).equals("\\"))
					{
						executionChainsFolderString = executionChainsFolderString.substring(0, executionChainsFolderString.length()-2);
					}
					
					new File(executionChainsFolderString).mkdirs();
					
					File executionChainsFolder = new File(executionChainsFolderString);
					
					if(executionChainsFolder.isDirectory())
					{
						this._configExecutionChainsFolderPath = executionChainsFolderString;
					}
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("ECRUNNINGFOLDER"))
				{
					String executionChainsRunningFolderString = settingExploded.get(1);
					
					if (executionChainsRunningFolderString.substring(executionChainsRunningFolderString.length()-1).equals("/")
						|| executionChainsRunningFolderString.substring(executionChainsRunningFolderString.length()-1).equals("\\"))
					{
						executionChainsRunningFolderString = executionChainsRunningFolderString.substring(0, executionChainsRunningFolderString.length()-2);
					}
					
					new File(executionChainsRunningFolderString).mkdirs();
					
					File executionChainsRunningFolder = new File(executionChainsRunningFolderString);
					
					if(executionChainsRunningFolder.isDirectory())
					{
						this._configExecutionChainsRunningFolderPath = executionChainsRunningFolderString;
					}
				}
				
				if (settingExploded.get(0).equalsIgnoreCase("LOGFOLDER"))
				{
					String logFolderString = settingExploded.get(1);
					
					if (logFolderString.substring(logFolderString.length()-1).equals("/")
						|| logFolderString.substring(logFolderString.length()-1).equals("\\"))
					{
						logFolderString = logFolderString.substring(0, logFolderString.length()-2);
					}
					
					new File(logFolderString).mkdirs();
					
					File logFolder = new File(logFolderString);
					
					if(logFolder.isDirectory())
					{
						this._configLoggerFolderPath = logFolderString;
					}						
				}
			}
			
			File toolFolder = new File(this._configToolCollectionFolderPath);

			if(!toolFolder.isDirectory())
			{
				System.out.println("Error on startup: you need a tool folder.");
				return false;
			}
			
			File executionChainsFolder = new File(this._configExecutionChainsFolderPath);

			if(!executionChainsFolder.isDirectory())
			{
				executionChainsFolder.mkdirs();
				
				if(!executionChainsFolder.isDirectory())
				{
					System.out.println("Error on startup: can not create executionChains folder.");
					return false;
				}
			}
			
			System.out.println("Configuration file successfully readed");
		}

		return true;
	}
	
	public Integer getPort()
	{
		return this._configNetworkPort2;
	}
	
	public InetAddress getIP()
	{
		return this._configNetworkIP;
	}
	
	public String getToolCollectionFolderPath()
	{
		return this._configToolCollectionFolderPath;
	}
	
	public String getLoggerFolderPath()
	{
		return this._configLoggerFolderPath;
	}
	
	public String getExecutionChainsFolderPath()
	{
		return this._configExecutionChainsFolderPath;
	}

	public String getConfigExecutionChainsRunningFolderPath()
	{
		return this._configExecutionChainsRunningFolderPath;
	}

	public void setConfigExecutionChainsRunningFolderPath(String configExecutionChainsRunningFolderPath)
	{
		this._configExecutionChainsRunningFolderPath = configExecutionChainsRunningFolderPath;
	}

	public String getOperatingSystem() 
	{
		return _operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) 
	{
		this._operatingSystem = operatingSystem;
	}
}

package i5.dvita.dts;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainRunningInformation;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DTSTool;
import i5.dvita.commons.DTSToolsIOFile;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.DTSToolsIOString;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.dts.Logger.LogType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.thoughtworks.xstream.XStream;


public class RequestHandlerServerInformation
{
	public DTSReturn<DTSServerInformation> process()
	{
		DTSServerInformation dtsServerInformation = new DTSServerInformation();
		dtsServerInformation.setDTSTools(this._scanToolCollectionFolder());
		dtsServerInformation.setExecutionChains(this._scanExecutionChainsFolder());
		dtsServerInformation.setExecutionChainsRunningInfo(this._scanExecutionChainsRunningFolder());
		dtsServerInformation.setMessageBoxLines(this._messageBoxData());
		
		return new DTSReturn<DTSServerInformation>(dtsServerInformation);
	}

	private List<String> _messageBoxData()
	{
		List<String> logLines = new ArrayList<String>();
		File runningExecutionChainsFolder = new File(Configuration.getInstance().getConfigExecutionChainsRunningFolderPath());
		
		for (File runningExecutionChainFolder : runningExecutionChainsFolder.listFiles())
		{
			List<String> folderNameSplit = Arrays.asList(runningExecutionChainFolder.getName().split("#"));
			
			if ( 2 != folderNameSplit.size())
			{
				Logger.getInstance().log("can not read running execution chain log file to build up message box data", LogType.ERROR);
				return new ArrayList<String>();
			}
			
			ArrayList<String> runningExecutionChainLog =
					(new RequestHandlerGetRunningExecutionChainLog(folderNameSplit.get(1))).process().getReturnValue();
			logLines.addAll(runningExecutionChainLog);
		}
		
		Collections.sort(logLines);
		
		if (20 < logLines.size())
		{
			logLines = new ArrayList<String>(logLines.subList(logLines.size()-21, logLines.size()-1));
		}
		
		return logLines;
	}
	
	
	private HashMap<String,DTSExecutionChainRunningInformation> _scanExecutionChainsRunningFolder()
	{
		File[] dtsExecutionChainsRunningFiles = new File(Configuration.getInstance().getConfigExecutionChainsRunningFolderPath()).listFiles();
		HashMap<String,DTSExecutionChainRunningInformation> runningInfos = new HashMap<String,DTSExecutionChainRunningInformation>();
		DTSExecutionChainRunningInformation runningInfo = null;
		
		for (File dtsExecutionChainRunningFile : dtsExecutionChainsRunningFiles)
		{
			if (dtsExecutionChainRunningFile.isDirectory())
			{
				try
				{
					String dtsExecutionChainRunningAsXML = new String
							(Files.readAllBytes(Paths.get(dtsExecutionChainRunningFile.getAbsolutePath() + File.separator + "runningInfo.xml")), StandardCharsets.UTF_8);
					XStream xStream = new XStream();
					runningInfo = (DTSExecutionChainRunningInformation)xStream.fromXML(dtsExecutionChainRunningAsXML);
					runningInfos.put(runningInfo.getId(), runningInfo);
				} 
				catch (IOException e)
				{
					Logger.getInstance().log("can not read execution chain running info: " + e.getMessage(), LogType.ERROR);
				}
			}
		}
		
		return runningInfos;
	}
	
	private HashMap<String,DTSExecutionChain> _scanExecutionChainsFolder()
	{
		File[] dtsExecutionChainsFiles = new File(Configuration.getInstance().getExecutionChainsFolderPath()).listFiles();
		HashMap<String,DTSExecutionChain> dtsExecutionChains = new HashMap<String,DTSExecutionChain>();
		DTSExecutionChain dtsExecutionChain = null;
		
		for (File dtsExecutionChainFile : dtsExecutionChainsFiles)
		{
			try
			{
				String dtsExecutionChainAsXML = new String
						(Files.readAllBytes(Paths.get(dtsExecutionChainFile.getAbsolutePath())), StandardCharsets.UTF_8);
				XStream xStream = new XStream();
				dtsExecutionChain = (DTSExecutionChain)xStream.fromXML(dtsExecutionChainAsXML);
				dtsExecutionChains.put(dtsExecutionChain.getName(), dtsExecutionChain);
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return dtsExecutionChains;
	}
	
	
	private HashMap<String,DTSTool> _scanToolCollectionFolder()
	{
		File[] toolCollectionFolder = new File(Configuration.getInstance().getToolCollectionFolderPath()).listFiles();
		DTSTool dvitaTool = null;
		HashMap<String,DTSTool> dvitaTools = new HashMap<String,DTSTool>();
		
		for (File toolFolder : toolCollectionFolder)
		{
			if (!toolFolder.isDirectory())
			{
				System.out.println("Scanning tool collection folder: invalid tool folder " + toolFolder.getName());
				continue;
			}
			
			if (null == (dvitaTool = this._processToolFolder(toolFolder)))
			{
				continue;
			}
			else
			{
				dvitaTools.put(dvitaTool.getId(), dvitaTool);
			}
		}
		
		return dvitaTools;
	}
	
	
	private DTSTool _processToolFolder(File toolFolder)
	{
		File[] toolFolderFiles = toolFolder.listFiles();
		DTSTool dvitaTool = null;
		
		for (File toolFolderFile : toolFolderFiles)
		{
			if (toolFolderFile.getName().equals(Configuration.TOOL_MANIFEST_FILE_NAME))
			{
				dvitaTool = this._processManifest(toolFolderFile);
			}
		}
		
		return dvitaTool;
	}
	
	private synchronized DTSTool _processManifest(File manifestPath)
	{
		DTSTool dvitaTool = new DTSTool();
		List<String> parameters = new ArrayList<String>();
		
		try
		{
			Document doc = new SAXBuilder().build(manifestPath.getAbsolutePath());
			Element XMLElementRoot = doc.getRootElement();
			
			dvitaTool.setId(XMLElementRoot.getAttributeValue("id"));
			
			if (null == dvitaTool.getId())
			{
				XMLElementRoot.setAttribute("id", UUID.randomUUID().toString().replaceAll("-", ""));
				dvitaTool.setId(XMLElementRoot.getAttributeValue("id"));
				XMLOutputter XMLOut = new XMLOutputter();
				XMLOut.output(doc, new FileWriter(manifestPath, false));
			}
			
			dvitaTool.setToolName(manifestPath.getParentFile().getName());
			dvitaTool.setCommand(XMLElementRoot.getChildText("command"));
			
			Pattern pattern = Pattern.compile("%.*?%");
			Matcher matcher = pattern.matcher(dvitaTool.getCommand());
			
			while (matcher.find())
			{
				parameters.add(matcher.group());
			}
			
			dvitaTool.setParameters(parameters);
			
			List<Element> XMLToolInputs = XMLElementRoot.getChild("inputDescription").getChildren();

			for (Element XMLElementInput : XMLToolInputs)
			{
				IDTSToolsIO dtsToolsIO = null;
				
				if (XMLElementInput.getAttributeValue("type").equals("file"))
				{
					dtsToolsIO = new DTSToolsIOFile();
					HashMap<String, String> data = new HashMap<String, String>();
					
					List<String> parametersTools = new ArrayList<String>();
					matcher = pattern.matcher(XMLElementInput.getChildText("filename"));
					while (matcher.find())
					{
						parametersTools.add(matcher.group());
					}
					dtsToolsIO.setParameters(parametersTools);
					
					data.put("filename", XMLElementInput.getChildText("filename"));
					data.put("text", XMLElementInput.getChildText("filename"));
					dtsToolsIO.setData(data);
				}
				else if (XMLElementInput.getAttributeValue("type").equals("sql"))
				{
					dtsToolsIO = new DTSToolsIOSQL();
				} 
				else if (XMLElementInput.getAttributeValue("type").equals("string"))
				{
					dtsToolsIO = new DTSToolsIOString();
				}
				
				dtsToolsIO.setId(XMLElementInput.getAttributeValue("id"));
				dtsToolsIO.setName(XMLElementInput.getChildText("name"));
				dtsToolsIO.setDescription(XMLElementInput.getChildText("description"));

				dvitaTool.addInput(dtsToolsIO.getId(), dtsToolsIO);
			}
				
			List<Element> XMLToolOutputs = XMLElementRoot.getChild("outputDescription").getChildren();

			for (Element XMLElementOutput : XMLToolOutputs)
			{
				IDTSToolsIO dtsToolsIO = null;
				
				if (XMLElementOutput.getAttributeValue("type").equals("file"))
				{
					dtsToolsIO = new DTSToolsIOFile();
					HashMap<String, String> data = new HashMap<String, String>();
					
					List<String> parametersTools = new ArrayList<String>();
					matcher = pattern.matcher(XMLElementOutput.getChildText("filename"));
					while (matcher.find())
					{
						parametersTools.add(matcher.group());
					}
					dtsToolsIO.setParameters(parametersTools);
					
					data.put("filename", XMLElementOutput.getChildText("filename"));
					data.put("text", XMLElementOutput.getChildText("filename"));
					dtsToolsIO.setData(data);
				}
				else if (XMLElementOutput.getAttributeValue("type").equals("sql"))
				{
					dtsToolsIO = new DTSToolsIOSQL();
				} 
				else if (XMLElementOutput.getAttributeValue("type").equals("string"))
				{
					dtsToolsIO = new DTSToolsIOString();

				}
				
				dtsToolsIO.setId(XMLElementOutput.getAttributeValue("id"));
				dtsToolsIO.setName(XMLElementOutput.getChildText("name"));
				dtsToolsIO.setDescription(XMLElementOutput.getChildText("description"));

				dvitaTool.addOutput(dtsToolsIO.getId(), dtsToolsIO);
			}
		}
		catch (JDOMException e)
		{
			Logger.getInstance().log(e.getMessage(), LogType.ERROR);
		}
		catch (IOException e)
		{
			Logger.getInstance().log(e.getMessage(), LogType.ERROR);
		}
		
		return dvitaTool;
	}
}

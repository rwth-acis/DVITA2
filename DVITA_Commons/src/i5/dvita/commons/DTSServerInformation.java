package i5.dvita.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class DTSServerInformation implements Serializable
{
	private static final long serialVersionUID = -1867849002377571842L;
	
	private String _id = null;
	private String _name = null;
	private String _ipport = null;
	private String _description = null;
	
	private HashMap<String,DTSTool> _dtsTools = null;
	private HashMap<String, DTSExecutionChain> _executionChains = null;
	
	private HashMap<String, DTSExecutionChainRunningInformation> _executionChainsRunningInfo = null;
	
	private List<String> _messageBoxLines = null;
	
	public DTSServerInformation()
	{
		this._dtsTools = new HashMap<String,DTSTool>();
	}
	
	public void setDTSTools(HashMap<String,DTSTool> dtsTools)
	{
		this._dtsTools = dtsTools;
	}

	public HashMap<String,DTSTool> getDTSTools()
	{
		return this._dtsTools;
	}

	public HashMap<String, DTSExecutionChain> getExecutionChains()
	{
		return this._executionChains;
	}

	public void setExecutionChains(HashMap<String, DTSExecutionChain> _executionChains)
	{
		this._executionChains = _executionChains;
	}

	public String getId()
	{
		return _id;
	}

	public void setId(String id)
	{
		this._id = id;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public String getIpport()
	{
		return _ipport;
	}

	public void setIpport(String ipport)
	{
		this._ipport = ipport;
	}

	public String getDescription()
	{
		return _description;
	}

	public void setDescription(String description)
	{
		this._description = description;
	}

	public HashMap<String, DTSExecutionChainRunningInformation> getExecutionChainsRunningInfo()
	{
		return _executionChainsRunningInfo;
	}

	public void setExecutionChainsRunningInfo(HashMap<String, DTSExecutionChainRunningInformation> executionChainsRunningInfo)
	{
		this._executionChainsRunningInfo = executionChainsRunningInfo;
	}

	public List<String> getMessageBoxLines()
	{
		return _messageBoxLines;
	}

	public void setMessageBoxLines(List<String> messageBoxLines)
	{
		this._messageBoxLines = messageBoxLines;
	}
}

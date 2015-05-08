package i5.dvita.commons;

import java.io.Serializable;
import java.util.HashMap;

public class DTSExecutionChainInvoke implements Serializable
{
	private static final long serialVersionUID = 371060499459998695L;
	private String _templateExecutionChainName = null;
	private String _runningExecutionChainName = null;
	private HashMap<String, IDTSToolsIO> _customInputs = new HashMap<String, IDTSToolsIO>();
	
	public String getTemplateExecutionChainName()
	{
		return this._templateExecutionChainName;
	}

	public void setTemplateExecutionChainName(String templateExecutionChainName)
	{
		this._templateExecutionChainName = templateExecutionChainName;
	}

	public String getRunningExecutionChainName()
	{
		return this._runningExecutionChainName;
	}

	public void setRunningExecutionChainName(String runningExecutionChainName)
	{
		this._runningExecutionChainName = runningExecutionChainName;
	}

	public HashMap<String, IDTSToolsIO> getCustomInputs()
	{
		return this._customInputs;
	}

	public void setCustomInputs(HashMap<String, IDTSToolsIO> customInputs)
	{
		this._customInputs = customInputs;
	}
	
	public void addCustomInput(String UID, IDTSToolsIO customInput)
	{
		this._customInputs.put(UID, customInput);
	}
}

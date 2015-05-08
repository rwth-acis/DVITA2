package i5.dvita.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DTSTool implements Serializable
{
	private static final long serialVersionUID = -3900388150524944678L;
	
	private String _id = null;
	private String _toolName = null;
	private String _command = null;
	
	private List<String> _parameters = new ArrayList<String>();
	
	private HashMap<String, IDTSToolsIO> _inputs = new HashMap<String, IDTSToolsIO>();
	private HashMap<String, IDTSToolsIO> _outputs = new HashMap<String, IDTSToolsIO>();
	
	public String getId()
	{
		return this._id;
	}
	public void setId(String id)
	{
		this._id = id;
	}
	public String getToolName()
	{
		return this._toolName;
	}
	public void setToolName(String toolName)
	{
		this._toolName = toolName;
	}
	public String getCommand()
	{
		return this._command;
	}
	public void setCommand(String command)
	{
		this._command = command;
	}
	public HashMap<String, IDTSToolsIO> getInputs()
	{
		return this._inputs;
	}
	public void setInputs(HashMap<String, IDTSToolsIO> inputs)
	{
		this._inputs = inputs;
	}
	public HashMap<String, IDTSToolsIO> getOutputs()
	{
		return this._outputs;
	}
	public void setOutputs(HashMap<String, IDTSToolsIO> outputs)
	{
		this._outputs = outputs;
	}
	public void addInput(String id, IDTSToolsIO input)
	{
		this._inputs.put(id, input);
	}
	public void addOutput(String id, IDTSToolsIO output)
	{
		this._outputs.put(id, output);
	}
	public List<String> getParameters()
	{
		return this._parameters;
	}
	public void setParameters(List<String> parameters)
	{
		this._parameters = parameters;
	}	
}

package i5.dvita.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public abstract class DTSToolsIO implements IDTSToolsIO,Serializable
{
	private String _id = null;
	private String _name = null;
	private String _description = null;
	private HashMap<String, String> _data = null;
	private List<String> _parameters = new ArrayList<String>();
	
	public DTSToolsIO()
	{
	}
	public String getId()
	{
		return this._id;
	}
	public void setId(String id)
	{
		this._id = id;
	}
	public String getName()
	{
		return this._name;
	}
	public void setName(String name)
	{
		this._name = name;
	}
	public String getDescription()
	{
		return this._description;
	}
	public void setDescription(String description)
	{
		this._description = description;
	}
	public HashMap<String, String> getData()
	{
		return this._data;
	}
	public void setData(HashMap<String, String> data)
	{
		this._data = data;
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

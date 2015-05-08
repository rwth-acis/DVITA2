package i5.dvita.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class DTSExecutionChain implements Serializable
{
	private static final long serialVersionUID = 5069845153380621872L;
	
	private String _name = null;
	private String _description = null;	
	
	private List<DTSExecutionChainConnection> _connections = null;
	private HashMap<String,WorkbenchElementData> _workbenchElementsData = null;
	
	public DTSExecutionChain()
	{
		this._workbenchElementsData = new HashMap<String,WorkbenchElementData>();
	}
	
	public void addWorkbenchElement(String guiId, int left, int top)
	{
		WorkbenchElementData workbenchElementData = new WorkbenchElementData();
		workbenchElementData.setLeft(left);
		workbenchElementData.setTop(top);
		
		this._workbenchElementsData.put(guiId, workbenchElementData);
	}
	
	public HashMap<String,WorkbenchElementData> getWorkbenchElements()
	{
		return this._workbenchElementsData;
	}
	
	public List<DTSExecutionChainConnection> getConnections()
	{
		return this._connections;
	}

	public void setConnections(List<DTSExecutionChainConnection> connections)
	{
		this._connections = connections;
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

	public static class WorkbenchElementData implements Serializable
	{
		private static final long serialVersionUID = -6937176730657839290L;
		
		private Integer _left;
		private Integer _top;
		
		public Integer getTop()
		{
			return this._top;
		}
		
		public void setTop(Integer top)
		{
			this._top = top;
		}
		
		public Integer getLeft()
		{
			return this._left;
		}
		
		public void setLeft(Integer left)
		{
			this._left = left;
		}
	}
}

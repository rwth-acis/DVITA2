package i5.dvita.commons;

import java.io.Serializable;
import java.util.HashMap;

public class DTSExecutionChainRunningInformation implements Serializable
{
	private static final long serialVersionUID = -1261946362849133989L;

	public static final String STATUS_READY = "READY";
	public static final String STATUS_RUNNING = "RUNNING";
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_NOTREADY = "NOTREADY";
	public static final String STATUS_FINISHED = "FINISHED";

	private String _id = null;
	private String _templateExecutionChainName = null;
	private String _runningExecutionChainName = null;
	
	private String _timeElapsedString = null;
	private String _size = null;
	private String _status = null;
	
	private HashMap<String, DTSToolRunningInformation> _runningTools = new HashMap<String, DTSToolRunningInformation>();
	
	private String _absolutePath = null;
	
	// for GWT
	public DTSExecutionChainRunningInformation()
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

	public String getTimeElapsedString()
	{
		return this._timeElapsedString;
	}

	public void setTimeElapsedString(String timeElapsedString)
	{
		this._timeElapsedString = timeElapsedString;
	}

	public String getSize()
	{
		return this._size;
	}

	public void setSize(String size)
	{
		this._size = size;
	}

	public HashMap<String, DTSToolRunningInformation> getRunningTools()
	{
		return this._runningTools;
	}

	public void setRunningTools(HashMap<String, DTSToolRunningInformation> runningTools)
	{
		this._runningTools = runningTools;
	}

	public void addRunningTool(String uid)
	{
		DTSToolRunningInformation dtsToolRunningInformation = new DTSToolRunningInformation();
		dtsToolRunningInformation.setStatus(STATUS_NOTREADY);
		this._runningTools.put(uid.replaceAll(":", "#"), dtsToolRunningInformation);
	}
	
	public DTSToolRunningInformation getRunningTool(String uid)
	{
		return this._runningTools.get(uid.replaceAll(":", "#"));
	}
	
	public String getAbsolutePath()
	{
		return _absolutePath;
	}

	public void setAbsolutePath(String absolutePath)
	{
		this._absolutePath = absolutePath;
	}


	public String getStatus()
	{
		return _status;
	}

	public void setStatus(String _status)
	{
		this._status = _status;
	}


	public static class DTSToolRunningInformation implements Serializable
	{
		private static final long serialVersionUID = -5750599090179650538L;
		
		private String _uid = null;
		private String _status = null;
		
		public String getUid()
		{
			return this._uid;
		}
		
		public void setUid(String uid)
		{
			this._uid = uid;
		}

		public String getStatus()
		{
			return _status;
		}

		public void setStatus(String status)
		{
			this._status = status;
		}
	}
}



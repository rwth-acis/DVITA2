package i5.dvita.commons;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class DTSExecutionChainUniqueId implements Serializable
{
	private static final long serialVersionUID = -182599189845638793L;

	private String _type = null;
	private String _id = null;
	private String _randomId = null;
	private String _seperator = ":";
	
	
	public DTSExecutionChainUniqueId()
	{
		this._seperator = ":";
	}
	
	public String getType()
	{
		return this._type;
	}
	public void setType(String type)
	{
		this._type = type;
	}
	public String getId()
	{
		return this._id;
	}
	public void setId(String id)
	{
		this._id = id;
	}
	public String getRandomId()
	{
		return this._randomId;
	}
	public void setRandomId(String randomId)
	{
		this._randomId = randomId;
	}
	public String getSeperator()
	{
		return this._seperator;
	}
	public void setSeperator(String seperator)
	{
		this._seperator = seperator;
	}
	
	@Override
	public String toString()
	{
		// TODO fix die ganze seperator scheisse
		if (null == this._seperator)
		{
			this._seperator = ":";
		}
		return this._type + this._seperator + this._id + this._seperator + this._randomId;
	}
	
	public static DTSExecutionChainUniqueId fromString(String UID)
	{
		String seperator = ":";
		
		if (UID.contains("#"))
		{
			seperator = "#";
		}

		DTSExecutionChainUniqueId dtsExecutionChainUniqueId = new DTSExecutionChainUniqueId();
		List<String> uidSplit = Arrays.asList(UID.split(seperator));
		
		dtsExecutionChainUniqueId.setType(uidSplit.get(0));
		dtsExecutionChainUniqueId.setId(uidSplit.get(1));
		dtsExecutionChainUniqueId.setRandomId(uidSplit.get(2));
		
		return dtsExecutionChainUniqueId;
	}
}

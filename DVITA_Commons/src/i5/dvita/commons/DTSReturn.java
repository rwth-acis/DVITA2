package i5.dvita.commons;

import java.io.Serializable;

public class DTSReturn<T> implements Serializable
{
	private static final long serialVersionUID = -7334701018341951710L;

	private T _returnValue;
	private String _message;
	private Status _status = null;
	
	public DTSReturn(T value)
	{
		this._returnValue = value;
	}
	
	// GWT compability
	public DTSReturn()
	{
		
	}
	
	public T getReturnValue()
	{
		return this._returnValue;
	}
	
	public void setReturnValue(T returnValue)
	{
		this._returnValue = returnValue;
	}
	
	public Status getStatus()
	{
		return this._status;
	}
	
	public void setStatus(Status status)
	{
		this._status = status;
	}
	
	public String getMessage()
	{
		return this._message;
	}
	
	public void setMessage(String message)
	{
		this._message = message;
	}
	
	public static enum Status
	{
		OKAY, // :)
		ERROR
	}
	
}

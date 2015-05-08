package i5.dvita.commons;

import java.io.Serializable;

public class DTSResponse<T> implements Serializable
{
	private static final long serialVersionUID = -1382458704803761714L;
	
	private T _returnValue;
	
	public DTSResponse(T value)
	{
		this._returnValue = value;
	}
	
	public void setReturnValue(T returnValue)
	{
		this._returnValue = returnValue;
	}
	
	public T getReturnValue()
	{
		return this._returnValue;
	}
}

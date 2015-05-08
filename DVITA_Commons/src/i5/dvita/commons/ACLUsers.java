package i5.dvita.commons;

import java.io.Serializable;

public class ACLUsers implements Serializable
{
	private static final long serialVersionUID = 4634011092433958948L;
	
	public static final String OPERATION_READ = "READ";
	
	private Integer _userId = null;
	private String _operation = null;
	private Integer _target = null;
	
	public String getOperation()
	{
		return this._operation;
	}
	public void setOperation(String operation)
	{
		this._operation = operation;
	}
	public Integer getTarget()
	{
		return this._target;
	}
	public void setTarget(Integer target)
	{
		this._target = target;
	}
	public Integer getUserId()
	{
		return _userId;
	}
	public void setUserId(Integer userId)
	{
		this._userId = userId;
	}
	
}

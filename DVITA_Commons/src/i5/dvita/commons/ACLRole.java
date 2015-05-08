package i5.dvita.commons;

import java.io.Serializable;

public class ACLRole implements Serializable
{
	private static final long serialVersionUID = -7070543973858683484L;
	
	private String _role = null;
	private String _operation = null;
	
	public String getRole()
	{
		return this._role;
	}
	public void setRole(String role)
	{
		this._role = role;
	}
	public String getOperation()
	{
		return this._operation;
	}
	public void setOperation(String operation)
	{
		this._operation = operation;
	}
	
}

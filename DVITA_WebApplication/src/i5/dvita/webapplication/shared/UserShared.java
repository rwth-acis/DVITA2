package i5.dvita.webapplication.shared;

import java.io.Serializable;
import java.util.List;

public class UserShared implements Serializable
{
	private static final long serialVersionUID = 450693648098563086L;
	
	private String _name = null;
	private String _email = null;
	private List<String> _operations = null;
	
	public String getName()
	{
		return this._name;
	}
	public void setName(String name)
	{
		this._name = name;
	}
	public String getEmail()
	{
		return this._email;
	}
	public void setEmail(String email)
	{
		this._email = email;
	}
	public List<String> getOperations()
	{
		return _operations;
	}
	public void setOperations(List<String> operations)
	{
		this._operations = operations;
	}
}

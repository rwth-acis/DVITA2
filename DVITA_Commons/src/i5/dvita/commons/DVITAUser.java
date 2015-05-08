package i5.dvita.commons;

import java.io.Serializable;

public class DVITAUser implements Serializable
{
	private static final long serialVersionUID = 9057056072989057660L;
	
	private Integer _id = null;
	private String _name = null;
	private String _email = null;
	private String _role = null;
	private String _ident = null;
	
	public Integer getId()
	{
		return this._id;
	}
	public void setId(Integer id)
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
	public String getEmail()
	{
		return this._email;
	}
	public void setEmail(String email)
	{
		this._email = email;
	}
	public String getRole()
	{
		return this._role;
	}
	public void setRole(String role)
	{
		this._role = role;
	}
	public String getIdent()
	{
		return this._ident;
	}
	public void setIdent(String ident)
	{
		this._ident = ident;
	}


}

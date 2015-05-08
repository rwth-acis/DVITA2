package i5.dvita.webapplication.shared;

import java.io.Serializable;

public class SetupServiceAuthResult implements Serializable
{
	private static final long serialVersionUID = 3427270050482842288L;
	
	private String _redirectURL = null;
	private String _authUID = null;
//	private String _discovery = null;
	
	public String getRedirectURL()
	{
		return this._redirectURL;
	}
	
	public void setRedirectURL(String redirectURL)
	{
		this._redirectURL = redirectURL;
	}

	public String getAuthUID()
	{
		return _authUID;
	}

	public void setAuthUID(String authUID)
	{
		this._authUID = authUID;
	}
	
//	public String getDiscovery()
//	{
//		return this._discovery;
//	}
//	
//	public void setDiscovery(String discovery)
//	{
//		this._discovery = discovery;
//	}
}

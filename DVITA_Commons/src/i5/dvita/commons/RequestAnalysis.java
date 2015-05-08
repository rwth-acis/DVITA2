package i5.dvita.commons;

import java.io.Serializable;

public class RequestAnalysis implements Serializable
{
	private static final long serialVersionUID = 4751922487452483162L;
	
	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_RUNNING = "RUNNING";
	public static final String STATUS_FINISHED = "FINISHED";
	public static final String STATUS_REJECTED = "REJECTED";
	
	private Integer _id = null;
	private Integer _userId = null;
	private String _url = null;
	private String _status = null;
	private Integer _analysisId = null;
	private DVITAUser _user = null;
	
	public Integer getId()
	{
		return this._id;
	}
	public void setId(Integer id)
	{
		this._id = id;
	}
	public Integer getUserId()
	{
		return this._userId;
	}
	public void setUserId(Integer userId)
	{
		this._userId = userId;
	}
	public String getUrl()
	{
		return this._url;
	}
	public void setUrl(String url)
	{
		this._url = url;
	}
	public String getStatus()
	{
		return this._status;
	}
	public void setStatus(String status)
	{
		this._status = status;
	}
	public DVITAUser getUser()
	{
		return _user;
	}
	public void setUser(DVITAUser user)
	{
		this._user = user;
	}
	public Integer getAnalysisId()
	{
		return _analysisId;
	}
	public void setAnalysisId(Integer analysisId)
	{
		this._analysisId = analysisId;
	}

}

package i5.dvita.commons;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.commons.DatabaseConnection;

import java.io.Serializable;
import java.util.HashMap;

public class DatabaseRepresentationData implements Serializable
{
	private static final long serialVersionUID = 3926918019425049097L;
	
	private HashMap<Integer, DatabaseConfigurationAnalysis> _analysisRows = null;
	private HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> _analysisRepresentationRows = null;
	private HashMap<Integer, DatabaseConfigurationRawdata> _rawdataRows = null;
	private HashMap<Integer, DatabaseConnection> _connectionsRows = null;
	

	public HashMap<Integer, DatabaseConfigurationAnalysis> getAnalysisRows()
	{
		return this._analysisRows;
	}


	public void setAnalysisRows(HashMap<Integer, DatabaseConfigurationAnalysis> analysisRows)
	{
		this._analysisRows = analysisRows;
	}


	public HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> getAnalysisRepresentationRows()
	{
		return this._analysisRepresentationRows;
	}


	public void setAnalysisRepresentationRows(HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> analysisRepresentationRows)
	{
		this._analysisRepresentationRows = analysisRepresentationRows;
	}


	public HashMap<Integer, DatabaseConfigurationRawdata> getRawdataRows()
	{
		return this._rawdataRows;
	}


	public void setRawdataRows(HashMap<Integer, DatabaseConfigurationRawdata> rawdataRows)
	{
		this._rawdataRows = rawdataRows;
	}


	public HashMap<Integer, DatabaseConnection> getConnectionsRows()
	{
		return this._connectionsRows;
	}


	public void setConnectionsRows(HashMap<Integer, DatabaseConnection> connectionsRows)
	{
		this._connectionsRows = connectionsRows;
	}


	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}


	public static class DatabaseConfigurationAnalysisRepresentation implements Serializable
	{
		private static final long serialVersionUID = -7389884537023063040L;
		
		private Integer _id = null;
		private Integer _analysisId = null;
		private String _titleAnalysisOverwrite = null;
		private String _descriptionAnalysisOverwrite = null;
		private String _titleRawdataOverwrite = null;
		private String _descriptionRawdataOverwrite = null;
		
		public Integer getId()
		{
			return this._id;
		}
		
		public void setId(Integer id)
		{
			this._id = id;
		}
		
		public Integer getAnalysisId()
		{
			return _analysisId;
		}

		public void setAnalysisId(Integer analysisId)
		{
			this._analysisId = analysisId;
		}
		
		public String getTitleAnalysisOverwrite()
		{
			return this._titleAnalysisOverwrite;
		}
		
		public void setTitleAnalysisOverwrite(String titleAnalysisOverwrite)
		{
			this._titleAnalysisOverwrite = titleAnalysisOverwrite;
		}
		
		public String getDescriptionAnalysisOverwrite()
		{
			return this._descriptionAnalysisOverwrite;
		}
		
		public void setDescriptionAnalysisOverwrite(String descriptionAnalysisOverwrite)
		{
			this._descriptionAnalysisOverwrite = descriptionAnalysisOverwrite;
		}
		
		public String getTitleRawdataOverwrite()
		{
			return this._titleRawdataOverwrite;
		}
		
		public void setTitleRawdataOverwrite(String titleRawdataOverwrite)
		{
			this._titleRawdataOverwrite = titleRawdataOverwrite;
		}
		
		public String getDescriptionRawdataOverwrite()
		{
			return this._descriptionRawdataOverwrite;
		}
		
		public void setDescriptionRawdataOverwrite(String descriptionRawdataOverwrite)
		{
			this._descriptionRawdataOverwrite = descriptionRawdataOverwrite;
		}
		
		
	}
}

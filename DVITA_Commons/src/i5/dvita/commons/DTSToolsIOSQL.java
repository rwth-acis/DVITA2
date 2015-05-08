package i5.dvita.commons;

public class DTSToolsIOSQL extends DTSToolsIO
{
	private static final long serialVersionUID = 5298199305017203477L;
	
	private DatabaseConfigurationAnalysis _databaseConfigurationAnalysis = null;

	public DTSToolsIOSQL()
	{
	}

	@Override
	public Boolean isDataAvailable()
	{
		return (null != this.getDatabaseConfigurationAnalysis());
	}

	@Override
	public void replaceParameterWithValue(String parameter, String value) {	}

	public DatabaseConfigurationAnalysis getDatabaseConfigurationAnalysis()
	{
		return this._databaseConfigurationAnalysis;
	}

	public void setDatabaseConfigurationAnalysis(DatabaseConfigurationAnalysis databaseConfigurationAnalysis)
	{
		this._databaseConfigurationAnalysis = databaseConfigurationAnalysis;
	}

	@Override
	public Boolean isIPC()
	{
		return true;
	}
}

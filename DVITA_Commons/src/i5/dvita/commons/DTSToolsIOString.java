package i5.dvita.commons;

public class DTSToolsIOString extends DTSToolsIO
{
	private static final long serialVersionUID = 1056368144681167889L;
	
	public DTSToolsIOString()
	{
	}

	@Override
	public Boolean isDataAvailable()
	{
		return this.getData().containsKey("text");
	}

	@Override
	public void replaceParameterWithValue(String parameter, String value) {	}

	@Override
	public Boolean isIPC()
	{
		return false;
	}
}

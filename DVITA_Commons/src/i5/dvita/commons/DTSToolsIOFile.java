package i5.dvita.commons;

public class DTSToolsIOFile extends DTSToolsIO
{
	private static final long serialVersionUID = -7663394368372092462L;
	


	public DTSToolsIOFile()
	{
	}

	@Override
	public Boolean isDataAvailable()
	{
		return this.getData().containsKey("absoluteFilePath");
	}

	@Override
	public void replaceParameterWithValue(String parameter, String value)
	{
		String filename = this.getData().get("filename");
		filename = filename.replaceAll(parameter, value);
		this.getData().put("filename", filename);
		this.getData().put("text", filename);
	}

	@Override
	public Boolean isIPC()
	{
		return false;
	}

}

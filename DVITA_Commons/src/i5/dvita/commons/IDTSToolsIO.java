package i5.dvita.commons;

import java.util.HashMap;
import java.util.List;

public interface IDTSToolsIO
{
	public String getId();
	public void setId(String id);
	public String getName();
	public void setName(String name);
	public String getDescription();
	public void setDescription(String description);
	public HashMap<String, String> getData();
	public void setData(HashMap<String, String> value);
	public Boolean isDataAvailable();
	public List<String> getParameters();
	public void setParameters(List<String> parameters);
	public void replaceParameterWithValue(String parameter, String value);
	public Boolean isIPC();
}

package i5.dvita.commons;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class DTSExecutionChainConnection implements Serializable
{
	private static final long serialVersionUID = -7424115632550178669L;
	
	private DTSExecutionChainUniqueId _outputToolUID = null;
	private String _outputDTSIOString = null;
	private DTSExecutionChainUniqueId _inputToolUID = null;
	private String _inputDTSIOString = null;
	
	public DTSExecutionChainUniqueId getOutputToolUID()
	{
		return this._outputToolUID;
	}
	public void setOutputToolUID(DTSExecutionChainUniqueId outputToolUID)
	{
		this._outputToolUID = outputToolUID;
	}
	public String getOutputDTSIOString()
	{
		return _outputDTSIOString;
	}
	public void setOutputDTSIOString(String _outputDTSIOString)
	{
		this._outputDTSIOString = _outputDTSIOString;
	}
	public DTSExecutionChainUniqueId getInputToolUID()
	{
		return this._inputToolUID;
	}
	public void setInputToolUID(DTSExecutionChainUniqueId inputToolUID)
	{
		this._inputToolUID = inputToolUID;
	}
	public String getInputDTSIOString()
	{
		return _inputDTSIOString;
	}
	public void setInputDTSIOString(String inputDTSIOString)
	{
		this._inputDTSIOString = inputDTSIOString;
	}
	@Override
	public String toString()
	{
		return this._outputToolUID.toString() + "#" + this._outputDTSIOString + "#" + this._inputToolUID.toString() + "#" + this._inputDTSIOString;
	}
	public static DTSExecutionChainConnection fromString(String connectionString)
	{
		DTSExecutionChainConnection dtsExecutionChainConnection = new DTSExecutionChainConnection();
		List<String> connectionStringSplit = Arrays.asList(connectionString.split("#"));
		
		dtsExecutionChainConnection.setOutputToolUID(DTSExecutionChainUniqueId.fromString(connectionStringSplit.get(0)));
		dtsExecutionChainConnection.setOutputDTSIOString(connectionStringSplit.get(1));
		dtsExecutionChainConnection.setInputToolUID(DTSExecutionChainUniqueId.fromString(connectionStringSplit.get(2)));
		dtsExecutionChainConnection.setInputDTSIOString(connectionStringSplit.get(3));
		
		return dtsExecutionChainConnection;
	}
}

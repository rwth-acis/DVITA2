package i5.dvita.tools.commons;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToolsIPC
{
	private List<String> _inputIds = null;
	private ObjectOutputStream _outputStream = null;
	private ObjectInputStream _inputStream = null;
	private FileWriter _wDebug = null;
	
	public ToolsIPC(String inputId)
	{
		this._inputIds = new ArrayList<String>();
		this._inputIds.add(inputId);
		this.createStreams();
	}

	public ToolsIPC(List<String> inputIds)
	{
		this._inputIds = inputIds;
		this.createStreams();
	}
	
	private void createStreams()
	{
		try
		{
			this._wDebug = new FileWriter("debugIPCoutput.txt", true);
		} 
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			this._outputStream = new ObjectOutputStream(System.out);
		} 
		catch (IOException e)
		{
			try
			{
				this._wDebug.write("error create output stream");this._wDebug.write('\n');this._wDebug.flush();
			} 
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try
		{
			this._inputStream = new ObjectInputStream(System.in);
		}
		catch (IOException e)
		{
			try
			{
				this._wDebug.write("error create input stream");this._wDebug.write('\n');this._wDebug.flush();
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<?, ?> read()
	{
		Object inputObject = null;

		try
		{
			inputObject = this._inputStream.readObject();
		}
		catch (ClassNotFoundException e)
		{
			try
			{
				this._wDebug.write("error readobject classnotfound");this._wDebug.write('\n');this._wDebug.flush();
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			try
			{
				this._wDebug.write("error read object ioexc");this._wDebug.write('\n');this._wDebug.flush();
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!(inputObject instanceof HashMap<?, ?>))
	    {
			try
			{
				this._wDebug.write("error not hashmap");this._wDebug.write('\n');this._wDebug.flush();
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	// TODO error
			System.out.println("error not hashmap");
	    	System.exit(1);
	    }

		for (String inputId : this._inputIds)
		{
			if (!((HashMap<?, ?>) inputObject).containsKey(inputId))
			{
				try
				{
					this._wDebug.write("error missing ipc data");this._wDebug.write('\n');this._wDebug.flush();
				} catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// TODO error
				System.out.println("error missing IPC data");
		    	System.exit(1);
			}
		}
		
		return (HashMap<?, ?>) inputObject;
	}
	
	public void write(int outputData)
	{
		this.write(Integer.toString(outputData));
	}
	
	public void write(Object outputData)
	{
		try
		{
			this._outputStream.writeObject(outputData);
			this._wDebug.write("out:" + outputData.toString());this._wDebug.write('\n');this._wDebug.flush();
		} 
		catch (IOException e)
		{
			try
			{
				this._wDebug.write("error write object");this._wDebug.write('\n');this._wDebug.flush();
			} 
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}

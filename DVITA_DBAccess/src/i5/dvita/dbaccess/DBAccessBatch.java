package i5.dvita.dbaccess;

import i5.dvita.commons.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DBAccessBatch extends DBAccess
{
	private PreparedStatement _batchQueryStatement = null;
	
	public DBAccessBatch(DatabaseConnection db)
	{
		super(db);
	}
	
	public void setQuery(String batchQuery)
	{
		try
		{
			this._batchQueryStatement = this._createConnection().prepareStatement(batchQuery);
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addValues(HashMap<Integer, Object> values)
	{
		for (Map.Entry<Integer, Object> entry : values.entrySet())
		{
			try
			{
				Object value = entry.getValue();
				
				if (value instanceof String)
				{
					this._batchQueryStatement.setString(entry.getKey(), (String) entry.getValue());
				}
				else if (value instanceof Integer)
				{
					this._batchQueryStatement.setInt(entry.getKey(), (Integer) entry.getValue());
				}
				else if (value instanceof Double)
				{
					this._batchQueryStatement.setDouble(entry.getKey(), (Double) entry.getValue());
				}
				else if (value instanceof Timestamp) 
				{
					this._batchQueryStatement.setTimestamp(entry.getKey(), (Timestamp) entry.getValue());
				}
			} 
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try
		{
			this._batchQueryStatement.addBatch();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void execute()
	{
		try
		{
			this._batchQueryStatement.executeBatch();
			
			if (null != _statement)
			{
				_statement.close();
			}
			
			if (null != _connection)
			{
				_connection.close();
			}
			
			if (null != this._batchQueryStatement)
			{
				this._batchQueryStatement.close();
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package i5.dvita.dbaccess;

import i5.dvita.commons.ACLRole;
import i5.dvita.commons.ACLUsers;
import i5.dvita.commons.DVITAUser;
import i5.dvita.commons.DatabaseCollectionSetup;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.commons.DatabaseConfigurationAnalysis.Granularity;
import i5.dvita.commons.DatabaseRepresentationData.DatabaseConfigurationAnalysisRepresentation;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.commons.DatabaseConnection;
import i5.dvita.commons.DatabaseRepresentationData;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBAccess 
{
	/** The database on which a DBAccess instance is working on */
	protected DatabaseConnection _db;
	
	private ArrayList<String> _sqlColumns = new ArrayList<String>();
	private ArrayList<String> _sqlTables = new ArrayList<String>();
	private String _sqlWhere = "";
	private String _sqlGroup = "";
	private String _sqlOrder = "";
	private String _sqlLimit = "";
	
	// IMPORTANT: dvita_config.txt must be accessible at the deployed app path 
	public static String CONFIG_FILE_PATH = "this is set by ConfigReader class during startup";
	
	public final static Charset CONFIG_FILE_ENCODING = StandardCharsets.UTF_8;
	
	public static final String SQL_DATABASE_CONNECTION_TABLE_NAME = "database_connections";
	public static final String SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME = "configurations_analysis_representation";
	public static final String SQL_DATABASE_ANALYSIS_TABLE_NAME = "configurations_analysis";
	public static final String SQL_DATABASE_RAWDATA_TABLE_NAME = "configurations_rawdata";
	public static final String SQL_DATABASE_USERS_TABLE_NAME = "users";
	public static final String SQL_DATABASE_ACL_ROLES_TABLE_NAME = "acl_roles";
	public static final String SQL_DATABASE_ACL_USERS_TABLE_NAME = "acl_users";
	public static final String SQL_DATABASE_ANALYSIS_REQUESTS_TABLE_NAME = "analysis_requests";
	
	protected Connection _connection;
	protected Statement _statement;
	
	// we need this for getRecordsByRows to save the actual cursor from the ResultSet
	private ResultSet _limitedResultSetCursor = null;
	
	/*
	 * 
	 * constructor for config database
	 * 
	 */
	
	public DBAccess()
	{
		try
		{
			Set<String> savedConfigParameter = new HashSet<String>();

			List<String> configFileLines = Files.readAllLines(Paths.get(CONFIG_FILE_PATH), CONFIG_FILE_ENCODING);
			DatabaseConnection db = new DatabaseConnection();
			
			for (String configLine : configFileLines)
			{
				List<String> configLineExploded = new ArrayList<String>(Arrays.asList(configLine.split("=")));
				
				if (2 != configLineExploded.size())
				{
					// invalid line, skip
					continue;
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("server"))
				{
					db.server = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("port"))
				{
					db.port = Integer.parseInt(configLineExploded.get(1));
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("databasename"))
				{
					db.databasename = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("user"))
				{
					db.user = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("password"))
				{
					db.passwort = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("type"))
				{
					db.type = Integer.parseInt(configLineExploded.get(1));
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("tablePrefix"))
				{
					db.tablePrefix = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("schema"))
				{
					db.schema = configLineExploded.get(1);
					savedConfigParameter.add(configLineExploded.get(0));
				}
			}
			
			if (null == db.tablePrefix)
			{
				db.tablePrefix = "";
			}
			
			if (null == db.schema)
			{
				db.schema = "";
			}
			
			_db = db;
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 * 
	 * constructor with given database (analysis or raw)
	 * 
	 */
	
	public DBAccess(DatabaseConnection db)
	{
		_db = db;
	}
	
	public DatabaseConnection getDb()
	{
		return this._db;
	}
	
	public void addColumn(String sqlColumn)
	{
		_sqlColumns.add(sqlColumn);
	}
	
	public void addTable(String sqlTable)
	{
		_sqlTables.add(sqlTable);
	}
	
	public void setWhere(String sqlWhere)
	{
		_sqlWhere = sqlWhere;
	}
	
	public void setGroup(String sqlGroup)
	{
		_sqlGroup = sqlGroup;
	}
	
	public void setOrder(String sqlOrder)
	{
		_sqlOrder = sqlOrder;
	}
	
	public void setLimit(String sqlLimit)
	{
		_sqlLimit = sqlLimit;
	}

	
	public HashMap<String,ArrayList<String>> getRecordsByColumns()
	{
		HashMap<String,ArrayList<String>> sqlRecords = new HashMap<String,ArrayList<String>>();
		String[] sqlColumnExploded;
		ResultSet sqlResult = _executeQuery(_builtQuery());
		
		// initialize, for each column an empty ArrayList<String>
		for (String sqlColumn : _sqlColumns)
		{
			sqlColumnExploded = sqlColumn.split(" ");
			
			if (1 == sqlColumnExploded.length)
			{
				// column name identical to the column name in result set
				sqlRecords.put(sqlColumn, new ArrayList<String>());
			}
			else if (2 == sqlColumnExploded.length && sqlColumnExploded[0].equals("DISTINCT"))
			{
				// using "DISTINCT columnName"
				sqlRecords.put(sqlColumnExploded[1], new ArrayList<String>());
			}
			else if (sqlColumnExploded[sqlColumnExploded.length-2].equals("AS"))
			{
				// using alias "columnName AS identifier"
				sqlRecords.put(sqlColumnExploded[sqlColumnExploded.length-1], new ArrayList<String>());
			}
			else
			{
				// TODO ERROR
			}
		}
		
		try
		{
			while(sqlResult.next())
			{	
				for (String sqlColumn : _sqlColumns)
				{
					sqlColumnExploded = sqlColumn.split(" ");
					
					if (1 == sqlColumnExploded.length)
					{
						// column name identical to the column name in result set
						sqlRecords.get(sqlColumn).add(sqlResult.getString(sqlColumn));
					}
					else if (2 == sqlColumnExploded.length && sqlColumnExploded[0].equals("DISTINCT"))
					{
						// using "DISTINCT columnName"
						sqlRecords.get(sqlColumnExploded[1]).add(sqlResult.getString(sqlColumnExploded[1]));
					}
					else if (sqlColumnExploded[sqlColumnExploded.length-2].equals("AS"))
					{
						// using alias "databasecolumn AS identifier"
						sqlRecords.get(sqlColumnExploded[sqlColumnExploded.length-1]).add(sqlResult.getString(sqlColumnExploded[sqlColumnExploded.length-1]));
					}
					else
					{
						// TODO ERROR
					}
				}
			}
			
			sqlResult.close();
			_statement.close();
			_connection.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sqlRecords;
	}
	
	
	public ArrayList<HashMap<String,String>> getRecordsByRows()
	{
		ArrayList<HashMap<String,String>> sqlRecords = new ArrayList<HashMap<String,String>>();
		String[] sqlColumnExploded;
		ResultSet sqlResult = _executeQuery(_builtQuery());
		
		try
		{
			while(sqlResult.next())
			{
				HashMap<String,String> mapDatabaseColumnsToValues = new HashMap<String,String>();
				
				for (String sqlColumn : _sqlColumns)
				{
					sqlColumnExploded = sqlColumn.split(" ");
					
					if (1 == sqlColumnExploded.length)
					{
						// column name identical to the column name in result set
						mapDatabaseColumnsToValues.put(sqlColumn, sqlResult.getString(sqlColumn));
					}
					else if (2 == sqlColumnExploded.length && sqlColumnExploded[0].equalsIgnoreCase("DISTINCT"))
					{
						// using "DISTINCT columnName"
						mapDatabaseColumnsToValues.put(sqlColumnExploded[1], sqlResult.getString(sqlColumnExploded[1]));
					}
					//else if (3 == sqlColumnExploded.length)
					else if (sqlColumnExploded[sqlColumnExploded.length-2].equalsIgnoreCase("AS"))
					{
						// using alias "databasecolumn AS identifier"
						mapDatabaseColumnsToValues.put(sqlColumnExploded[sqlColumnExploded.length-1], sqlResult.getString(sqlColumnExploded[sqlColumnExploded.length-1]));				
					}
					else
					{
						// TODO ERROR
					}
				}
				
				sqlRecords.add(mapDatabaseColumnsToValues);
			}
			
			sqlResult.close();
			_statement.close();
			_connection.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sqlRecords;
	}
	
	public ArrayList<HashMap<String,String>> getRecordsByRowsLimit(Integer limit)
	{
		ArrayList<HashMap<String,String>> sqlRecords = new ArrayList<HashMap<String,String>>();
		String[] sqlColumnExploded;

		limit++;
		
		if (null == this._limitedResultSetCursor)
		{
			this._limitedResultSetCursor = _executeQuery(_builtQuery());
		}
		
		Integer rowCount = 1;
		
		try
		{
			if (this._limitedResultSetCursor.isClosed())
			{
				this._limitedResultSetCursor.close();
				_statement.close();
				_connection.close();
				return sqlRecords;
			}
			
			while(this._limitedResultSetCursor.next())
			{
				HashMap<String,String> mapDatabaseColumnsToValues = new HashMap<String,String>();
				
				for (String sqlColumn : _sqlColumns)
				{
					sqlColumnExploded = sqlColumn.split(" ");
					
					if (1 == sqlColumnExploded.length)
					{
						// column name identical to the column name in result set
						mapDatabaseColumnsToValues.put(sqlColumn, this._limitedResultSetCursor.getString(sqlColumn));
					}
					else if (2 == sqlColumnExploded.length && sqlColumnExploded[0].equalsIgnoreCase("DISTINCT"))
					{
						// using "DISTINCT columnName"
						mapDatabaseColumnsToValues.put(sqlColumnExploded[1], this._limitedResultSetCursor.getString(sqlColumnExploded[1]));
					}
					else if (sqlColumnExploded[sqlColumnExploded.length-2].equalsIgnoreCase("AS"))
					{
						// using alias "databasecolumn AS identifier"
						mapDatabaseColumnsToValues.put(sqlColumnExploded[sqlColumnExploded.length-1], this._limitedResultSetCursor.getString(sqlColumnExploded[sqlColumnExploded.length-1]));				
					}
					else
					{
						// TODO ERROR
					}
				}
				
				sqlRecords.add(mapDatabaseColumnsToValues);
				rowCount++;
				
				if (0 == (rowCount % limit))
				{
					break;
				}
			}
			
			if (0 != (rowCount % limit ))
			{
				this._limitedResultSetCursor.close();
				_statement.close();
				_connection.close();
				return sqlRecords;
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sqlRecords;
	}
	
	
	private String _builtQuery()
	{
		String sqlQuery;
		
		// construct query
		// SELECT
		sqlQuery = "SELECT ";
		for (String sqlColumn : _sqlColumns)
		{
			sqlQuery += sqlColumn + ",";
		}
		sqlQuery = sqlQuery.substring(0,sqlQuery.length()-1);
		
		// FROM
		sqlQuery += " FROM ";
		for (String sqlTable : _sqlTables)
		{
			sqlQuery += sqlTable + ",";
		}
		sqlQuery = sqlQuery.substring(0,sqlQuery.length()-1);
		
		// WHERE
		if ((!_sqlWhere.equals("")) && (!_sqlWhere.equalsIgnoreCase("null")) && (null != _sqlWhere))
		{
			sqlQuery += " WHERE " + _sqlWhere;
		}
		
		if ("" != _sqlGroup)
		{
			sqlQuery += " GROUP BY " + _sqlGroup;
		}
		
		// ORDER
		if ("" != _sqlOrder)
		{
			sqlQuery += " ORDER BY " + _sqlOrder;
		}
		
		// LIMIT
		if ("" != _sqlLimit)
		{
			if (2 == _db.type)
			{
				sqlQuery += " FETCH FIRST " + _sqlLimit + " ROWS ONLY";
			}
			else
			{
				sqlQuery += " LIMIT " + _sqlLimit;
			}
		}
		
		return sqlQuery;
	}
	
	public void doQueryUpdate(String sqlQuery)
	{
		_executeUpdate(sqlQuery);
		
		try
		{
			if (null != _statement)
			{
				_statement.close();
			}
			
			if (null != _connection)
			{
				_connection.close();
			}
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public HashMap<String, HashMap<String,String>> getConfigurationToolserver()
	{
		HashMap<String, HashMap<String,String>> toolserverData = new HashMap<String, HashMap<String,String>>();
		
		ResultSet sqlResult = _executeQuery("SELECT * FROM toolservers ORDER BY id ASC");
		
		try
		{
			while (sqlResult.next())
			{
				HashMap<String,String> toolserver = new HashMap<String,String>();
				toolserver.put("id", sqlResult.getString("id"));
				toolserver.put("ipAdr", sqlResult.getString("ipAdr"));
				toolserver.put("portAdr", sqlResult.getString("portAdr"));
				toolserver.put("titel", sqlResult.getString("titel"));
				toolserver.put("description", sqlResult.getString("description"));
				toolserverData.put(sqlResult.getString("id"), toolserver);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return toolserverData;
	}
	
	public void setDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData)
	{
		if (null != databaseRepresentationData.getAnalysisRows())
		{
			DatabaseConfigurationAnalysis databaseConfigurationAnalysis = null;
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
			for (Map.Entry<Integer, DatabaseConfigurationAnalysis> entry : databaseRepresentationData.getAnalysisRows().entrySet())
			{
				databaseConfigurationAnalysis = databaseRepresentationData.getAnalysisRows().get(entry.getKey());
				Integer gran = 1;
				switch (databaseConfigurationAnalysis.gran) 
				{
					case YEARLY: gran = 1; break;
					case MONTHLY: gran = 2; break;
					case WEEKLY: gran = 3; break;
					case DAYLY: gran = 4; break;
					default: gran = 1; break;
				}
				this.doQueryUpdate
				(
					"UPDATE " + tableName + " "
					+ "SET" + " "
					+ "dvita_configurations_rawdata_id = " + databaseConfigurationAnalysis.databaseConfigurationRawdata.id + ","
					+ "dvita_database_connections_id = " + databaseConfigurationAnalysis.connectionId + ","
					+ "granularity = " + gran + "," 
					+ "rangeStart = \"" + databaseConfigurationAnalysis.rangeStart + "\","
					+ "rangeEnd = \"" + databaseConfigurationAnalysis.rangeEnd + "\","
					+ "numberTopics = " + databaseConfigurationAnalysis.NumberTopics + ","
					+ "meta_title = \"" + databaseConfigurationAnalysis.metaTitle + "\","
					+ "meta_description = \"" + databaseConfigurationAnalysis.metaDescription + "\","
					+ "tablePrefix = \"" + databaseConfigurationAnalysis.tablePrefix + "\","
					+ "status = " + databaseConfigurationAnalysis.status + " "
					+ "WHERE id = " + databaseConfigurationAnalysis.id
				);
			}
		}
		
		if (null != databaseRepresentationData.getRawdataRows())
		{
			DatabaseConfigurationRawdata databaseConfigurationRawdata = null;
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
			for (Map.Entry<Integer, DatabaseConfigurationRawdata> entry : databaseRepresentationData.getRawdataRows().entrySet())
			{
				databaseConfigurationRawdata = databaseRepresentationData.getRawdataRows().get(entry.getKey());
				
				if ((null == databaseConfigurationRawdata.whereClause) || (databaseConfigurationRawdata.whereClause.equalsIgnoreCase("null")))
				{
					databaseConfigurationRawdata.whereClause = "";
				}
				
				this.doQueryUpdate
				(
					"UPDATE " + tableName + " "
					+ "SET" + " "
					+ "dvita_database_connections_id = " + databaseConfigurationRawdata.connectionId + ","
					+ "columnNameID = \"" + databaseConfigurationRawdata.columnNameID + "\"," 
					+ "columnNameDate = \"" + databaseConfigurationRawdata.columnNameDate + "\","
					+ "columnNameContent = \"" + databaseConfigurationRawdata.columnNameContent + "\","
					+ "columnNameTitle = \"" + databaseConfigurationRawdata.columnNameTitle + "\","
					+ "columnNameURL = \"" + databaseConfigurationRawdata.columnNameURL + "\","
					+ "from_clause = \"" + databaseConfigurationRawdata.fromClause + "\","
					+ "where_clause = \"" + databaseConfigurationRawdata.whereClause + "\","
					+ "meta_title = \"" + databaseConfigurationRawdata.metaTitle + "\","
					+ "meta_description = \"" + databaseConfigurationRawdata.metaDescription + "\","
					+ "tablePrefix = \"" + databaseConfigurationRawdata.tablePrefix + "\" "
					+ "WHERE id = " + databaseConfigurationRawdata.id
				);
			}
		}
		
		if (null != databaseRepresentationData.getConnectionsRows())
		{
			DatabaseConnection databaseConnection = null;
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_CONNECTION_TABLE_NAME;
			for (Map.Entry<Integer, DatabaseConnection> entry : databaseRepresentationData.getConnectionsRows().entrySet())
			{
				databaseConnection = databaseRepresentationData.getConnectionsRows().get(entry.getKey());
				
				this.doQueryUpdate
				(
					"UPDATE " + tableName + " "
					+ "SET" + " "
					+ "name = \"" + databaseConnection.name + "\"," 
					+ "description = \"" + databaseConnection.description + "\","
					+ "server = \"" + databaseConnection.server + "\","
					+ "port = " + databaseConnection.port + ","
					+ "databasename = \"" + databaseConnection.databasename + "\","
					// TODO change column name schema
					+ "`schema` = \"" + databaseConnection.schema + "\","
					+ "type = " + databaseConnection.type + ","
					+ "user = \"" + databaseConnection.user + "\","
					+ "password = \"" + databaseConnection.passwort + "\" "
					+ "WHERE id = " + databaseConnection.id
				);
			}
		}
		//problem
		if (null != databaseRepresentationData.getAnalysisRepresentationRows())
		{
			DatabaseConfigurationAnalysisRepresentation databaseConfigurationAnalysisRepresentation = null;
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME;
			for (Map.Entry<Integer, DatabaseConfigurationAnalysisRepresentation> entry : databaseRepresentationData.getAnalysisRepresentationRows().entrySet())
			{
				databaseConfigurationAnalysisRepresentation = databaseRepresentationData.getAnalysisRepresentationRows().get(entry.getKey());
				//Integer Id= (null != databaseConfigurationAnalysisRepresentation.getId()) ? databaseConfigurationAnalysisRepresentation.getId() : 0;
				Integer analysisId = (null != databaseConfigurationAnalysisRepresentation.getAnalysisId()) ? databaseConfigurationAnalysisRepresentation.getAnalysisId() : 0;
				String titleAnalysisOverwrite = (null != databaseConfigurationAnalysisRepresentation.getTitleAnalysisOverwrite()) ? databaseConfigurationAnalysisRepresentation.getTitleAnalysisOverwrite() : "";
				String descriptionAnalysisOverwrite = (null != databaseConfigurationAnalysisRepresentation.getDescriptionAnalysisOverwrite()) ? databaseConfigurationAnalysisRepresentation.getDescriptionAnalysisOverwrite() : "";
				String titleRawdataOverwrite = (null != databaseConfigurationAnalysisRepresentation.getTitleRawdataOverwrite()) ? databaseConfigurationAnalysisRepresentation.getTitleRawdataOverwrite() : "";
				String descriptionRawdataOverwrite = (null != databaseConfigurationAnalysisRepresentation.getDescriptionRawdataOverwrite()) ? databaseConfigurationAnalysisRepresentation.getDescriptionRawdataOverwrite() : "";
				System.out.println("inside"+analysisId+" "+titleAnalysisOverwrite+" "+titleRawdataOverwrite);
				
				System.out.println("KEY IS"+databaseConfigurationAnalysisRepresentation.getId());
				this.doQueryUpdate
				(
					"UPDATE " + tableName + " "
					+ "SET" + " "
					+ "dvita_configurations_analysis_id = " + analysisId + "," 
					+ "title_analysis_overwrite = \"" + titleAnalysisOverwrite + "\","
					+ "description_analysis_overwrite = \"" + descriptionAnalysisOverwrite + "\","
					+ "title_rawdata_overwrite = \"" + titleRawdataOverwrite + "\","
					+ "description_rawdata_overwrite = \"" + descriptionRawdataOverwrite + "\" "+" "+"WHERE id="+databaseConfigurationAnalysisRepresentation.getId()
				);
				System.out.println("done update");
			}
		}
	}
	
	public void removeDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData)
	{
		if (null != databaseRepresentationData.getAnalysisRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
			
			for (Map.Entry<Integer, DatabaseConfigurationAnalysis> entry : databaseRepresentationData.getAnalysisRows().entrySet())
			{
				this.doQueryUpdate("DELETE FROM " + tableName + " WHERE id=" + entry.getKey());
			}
		}
		
		if (null != databaseRepresentationData.getRawdataRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
			
			for (Map.Entry<Integer, DatabaseConfigurationRawdata> entry : databaseRepresentationData.getRawdataRows().entrySet())
			{
				this.doQueryUpdate("DELETE FROM " + tableName + " WHERE id=" + entry.getKey());
			}
		}
		
		if (null != databaseRepresentationData.getConnectionsRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_CONNECTION_TABLE_NAME;
			
			for (Map.Entry<Integer, DatabaseConnection> entry : databaseRepresentationData.getConnectionsRows().entrySet())
			{
				this.doQueryUpdate("DELETE FROM " + tableName + " WHERE id=" + entry.getKey());
			}
		}
		
		if (null != databaseRepresentationData.getAnalysisRepresentationRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME;
			
			for (Map.Entry<Integer, DatabaseConfigurationAnalysisRepresentation> entry : databaseRepresentationData.getAnalysisRepresentationRows().entrySet())
			{
				this.doQueryUpdate("DELETE FROM " + tableName + " WHERE id=" + entry.getKey());
			}
		}
	}
	
	public void addDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData)
	{
		if (null != databaseRepresentationData.getAnalysisRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
			this.doQueryUpdate("INSERT INTO " + tableName + " () VALUES ()");
			this.setDatabaseRepresentationData(databaseRepresentationData);
		}
		
		if (null != databaseRepresentationData.getRawdataRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
			this.doQueryUpdate("INSERT INTO " + tableName + " () VALUES ()");
			this.setDatabaseRepresentationData(databaseRepresentationData);
		}
		
		if (null != databaseRepresentationData.getConnectionsRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_CONNECTION_TABLE_NAME;
			this.doQueryUpdate("INSERT INTO " + tableName + " () VALUES ()");
			this.setDatabaseRepresentationData(databaseRepresentationData);
		}
		
		if (null != databaseRepresentationData.getAnalysisRepresentationRows())
		{
			String tableName = this._db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME;
			this.doQueryUpdate("INSERT INTO " + tableName + " () VALUES ()");
			this.setDatabaseRepresentationData(databaseRepresentationData);
		}
	}
	
	public DatabaseRepresentationData getDatabaseRepresentationData()
	{
		DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
		
		HashMap<Integer, DatabaseConfigurationAnalysis> analysisRows = new HashMap<Integer, DatabaseConfigurationAnalysis>();
		HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> analysisRepresentationRows = new HashMap<Integer, DatabaseConfigurationAnalysisRepresentation>();
		HashMap<Integer, DatabaseConfigurationRawdata> rawdataRows = new HashMap<Integer, DatabaseConfigurationRawdata>();
		HashMap<Integer, DatabaseConnection> connectionsRows = new HashMap<Integer, DatabaseConnection>();
		
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = null;
		DatabaseConfigurationAnalysisRepresentation databaseConfigurationAnalysisRepresentation = null;
		DatabaseConfigurationRawdata databaseConfigurationRawdata = null;
		DatabaseConnection databaseConnection = null;
		
		String analysisTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
		String analysisRepresentationTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME;
		String rawdataTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
		String connectionTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_CONNECTION_TABLE_NAME;
		
		// analysis
		ResultSet sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + analysisTableName + " " +
			"ORDER BY id ASC"
		);
		
		try
		{
			while (sqlResult.next())
			{
				databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
				databaseConfigurationAnalysis.id = sqlResult.getInt("id");
				databaseConfigurationAnalysis.databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
				databaseConfigurationAnalysis.databaseConfigurationRawdata.id = sqlResult.getInt("dvita_configurations_rawdata_id");
				databaseConfigurationAnalysis.connectionId = sqlResult.getInt("dvita_database_connections_id");
				switch (sqlResult.getInt("granularity")) 
				{
					case 1: databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
					case 2: databaseConfigurationAnalysis.gran = Granularity.MONTHLY; break;
					case 3: databaseConfigurationAnalysis.gran = Granularity.WEEKLY; break;
					case 4: databaseConfigurationAnalysis.gran = Granularity.DAYLY; break;
					case 5: databaseConfigurationAnalysis.gran = Granularity.QUARTERYEAR; break;
					case 6: databaseConfigurationAnalysis.gran = Granularity.HALFYEAR; break;
					case 7: databaseConfigurationAnalysis.gran = Granularity.FIVEYEARS; break;
					case 8: databaseConfigurationAnalysis.gran = Granularity.DECADE; break;
					default: 
						System.out.println("Unknown granularity value");
						databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
				}
				databaseConfigurationAnalysis.rangeStart = sqlResult.getTimestamp("rangeStart");
				databaseConfigurationAnalysis.rangeEnd = sqlResult.getTimestamp("rangeEnd");
				databaseConfigurationAnalysis.NumberTopics = sqlResult.getInt("numberTopics");	
				databaseConfigurationAnalysis.metaTitle = sqlResult.getString("meta_title");
				databaseConfigurationAnalysis.metaDescription = sqlResult.getString("meta_description"); 
				databaseConfigurationAnalysis.tablePrefix = sqlResult.getString("tablePrefix"); 
				databaseConfigurationAnalysis.status = sqlResult.getInt("status"); 
				
				analysisRows.put(databaseConfigurationAnalysis.id, databaseConfigurationAnalysis);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Database representation got and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		databaseRepresentationData.setAnalysisRows(analysisRows);
		
		// analysis representation
		sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + analysisRepresentationTableName + " " +
			"ORDER BY id ASC"
		);
		
		try
		{
			while (sqlResult.next())
			{
				databaseConfigurationAnalysisRepresentation = new DatabaseConfigurationAnalysisRepresentation();
				databaseConfigurationAnalysisRepresentation.setId(sqlResult.getInt("id"));
				databaseConfigurationAnalysisRepresentation.setAnalysisId(sqlResult.getInt("dvita_configurations_analysis_id"));
				databaseConfigurationAnalysisRepresentation.setTitleAnalysisOverwrite(sqlResult.getString("title_analysis_overwrite"));
				databaseConfigurationAnalysisRepresentation.setDescriptionAnalysisOverwrite(sqlResult.getString("description_analysis_overwrite"));
				databaseConfigurationAnalysisRepresentation.setTitleRawdataOverwrite(sqlResult.getString("title_rawdata_overwrite"));
				databaseConfigurationAnalysisRepresentation.setDescriptionRawdataOverwrite(sqlResult.getString("description_rawdata_overwrite"));

				analysisRepresentationRows.put(databaseConfigurationAnalysisRepresentation.getId(), databaseConfigurationAnalysisRepresentation);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Analysis representation got and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		databaseRepresentationData.setAnalysisRepresentationRows(analysisRepresentationRows);
				
		// rawdata
		sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + rawdataTableName + " " +
			"ORDER BY id ASC"
		);
		
		try
		{
			while (sqlResult.next())
			{
				databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
				databaseConfigurationRawdata.id = sqlResult.getInt("id");
				databaseConfigurationRawdata.connectionId = sqlResult.getInt("dvita_database_connections_id");
				databaseConfigurationRawdata.columnNameID = sqlResult.getString("columnNameID");
				databaseConfigurationRawdata.columnNameDate = sqlResult.getString("columnNameDate");
				databaseConfigurationRawdata.columnNameContent = sqlResult.getString("columnNameContent");
				databaseConfigurationRawdata.columnNameTitle = sqlResult.getString("columnNameTitle");
				databaseConfigurationRawdata.columnNameURL = sqlResult.getString("columnNameURL");
				databaseConfigurationRawdata.fromClause = sqlResult.getString("from_clause");
				databaseConfigurationRawdata.whereClause = sqlResult.getString("where_clause");
				databaseConfigurationRawdata.metaTitle = sqlResult.getString("meta_title");
				databaseConfigurationRawdata.metaDescription = sqlResult.getString("meta_description");
				databaseConfigurationRawdata.tablePrefix = sqlResult.getString("tablePrefix");

				rawdataRows.put(databaseConfigurationRawdata.id, databaseConfigurationRawdata);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Configuration raw data got and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		databaseRepresentationData.setRawdataRows(rawdataRows);
		
		// connection data
		sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + connectionTableName + " " +
			"ORDER BY id ASC"
		);
		
		try
		{
			while (sqlResult.next())
			{
				databaseConnection = new DatabaseConnection();
				databaseConnection.id = sqlResult.getInt("id");
				databaseConnection.name = sqlResult.getString("name");
				databaseConnection.description = sqlResult.getString("description");
				databaseConnection.server = sqlResult.getString("server");
				databaseConnection.port = sqlResult.getInt("port");
				databaseConnection.databasename = sqlResult.getString("databasename");
				databaseConnection.schema = sqlResult.getString("schema");
				databaseConnection.type = sqlResult.getInt("type");
				databaseConnection.user = sqlResult.getString("user");
				databaseConnection.passwort = sqlResult.getString("password");
				
				connectionsRows.put(databaseConnection.id, databaseConnection);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Database info got and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		databaseRepresentationData.setConnectionsRows(connectionsRows);
		
		return databaseRepresentationData;
	}
	
	public ArrayList<DatabaseCollectionSetup> getConfigurationAnalysisRepresentation(Integer userId)
	{
		ArrayList<DatabaseCollectionSetup> setupData = new ArrayList<DatabaseCollectionSetup>();
		
		String analysisRepresentationTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REPRESENTATION_TABLE_NAME;
		String analysisTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
		String rawdataTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
		String analysisRequestsTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REQUESTS_TABLE_NAME;
		String analysisACLUsersTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ACL_USERS_TABLE_NAME;
		
		ResultSet sqlResult = _executeQuery
		(
			"SELECT analysisConf.id" + "," +
			"analysisRep.title_analysis_overwrite" + "," +
			"analysisRep.description_analysis_overwrite" + "," +
			"analysisRep.title_rawdata_overwrite" + "," +
			"analysisRep.description_rawdata_overwrite" + "," +
			"analysisConf.meta_title" + "," +
			"analysisConf.meta_description" + "," +
			"analysisConf.numberTopics" + "," +
			"rawdataConf.id" + "," +
			"rawdataConf.meta_title" + "," +
			"rawdataConf.meta_description" + " " +
			"FROM " + analysisRepresentationTableName + " AS analysisRep " +
			"INNER JOIN " + analysisTableName + " AS analysisConf " +
			"ON analysisRep.dvita_configurations_analysis_id = analysisConf.id " +
			"INNER JOIN " + rawdataTableName + " AS rawdataConf " +
			"ON analysisConf.dvita_configurations_rawdata_id = rawdataConf.id " +
			"ORDER BY rawdataConf.id ASC"
		);
		
		Integer lastRawdataId = 0;
		DatabaseCollectionSetup databaseCollectionSetup = new DatabaseCollectionSetup();
		DatabaseConfigurationRawdata databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
		
		try 
		{
			while (sqlResult.next())
			{
				databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
				databaseConfigurationAnalysis.id = sqlResult.getInt("analysisConf.id");
				databaseConfigurationAnalysis.NumberTopics = sqlResult.getInt("analysisConf.numberTopics");
				
				if (null != sqlResult.getString("analysisRep.title_analysis_overwrite"))
				{
					databaseConfigurationAnalysis.metaTitle = sqlResult.getString("analysisRep.title_analysis_overwrite");
				}
				else
				{
					databaseConfigurationAnalysis.metaTitle = sqlResult.getString("analysisConf.meta_title");
				}
				
				if (null != sqlResult.getString("analysisRep.description_analysis_overwrite"))
				{
					databaseConfigurationAnalysis.metaDescription = sqlResult.getString("analysisRep.description_analysis_overwrite");
					
				}
				else
				{
					databaseConfigurationAnalysis.metaDescription = sqlResult.getString("analysisConf.meta_description"); 
				}
				
				if ((0 == lastRawdataId) || lastRawdataId != sqlResult.getInt("rawdataConf.id"))
				{
					databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
					
					databaseConfigurationRawdata.id = sqlResult.getInt("rawdataConf.id");
					
					if (null != sqlResult.getString("analysisRep.title_rawdata_overwrite"))
					{
						databaseConfigurationRawdata.metaTitle = sqlResult.getString("analysisRep.title_rawdata_overwrite");
					}
					else
					{
						databaseConfigurationRawdata.metaTitle = sqlResult.getString("rawdataConf.meta_title");
					}
					
					if (null != sqlResult.getString("analysisRep.description_analysis_overwrite"))
					{
						databaseConfigurationRawdata.metaDescription = sqlResult.getString("analysisRep.description_rawdata_overwrite");
						
					}
					else
					{
						databaseConfigurationRawdata.metaDescription = sqlResult.getString("rawdataConf.meta_description"); 
					}
					
					if (0 != lastRawdataId)
					{
						setupData.add(databaseCollectionSetup);
						databaseCollectionSetup = new DatabaseCollectionSetup();
					}
					
					databaseCollectionSetup.databaseConfigurationRawdata = databaseConfigurationRawdata;
					databaseCollectionSetup.databaseConfigurationAnalyses.add(databaseConfigurationAnalysis);
					lastRawdataId = sqlResult.getInt("rawdataConf.id");
				}
				else
				{
					databaseCollectionSetup.databaseConfigurationAnalyses.add(databaseConfigurationAnalysis);
				}
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			setupData.add(databaseCollectionSetup);
			System.out.println("configuration analysis got and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// additional the analysis done by requests
		if (null != userId)
		{
			ArrayList<Integer> readableRequests = new ArrayList<>();
			String userReadableAnalyses = "";
			
			if (0 != userId)
			{
				ResultSet sqlResultACLUsers = _executeQuery
				(
					"SELECT target_id " + 
					"FROM " + analysisACLUsersTableName + " " +
					"WHERE user_id=" + userId + " AND operation=\"READ\""
				);
				
				try
				{
					while (sqlResultACLUsers.next())
					{
						readableRequests.add(sqlResultACLUsers.getInt("target_id"));
					}
					sqlResult.close();
					_statement.close();
					_connection.close();
					System.out.println("ACL users got and closed");
				} 
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (0 < readableRequests.size())
				{
					Integer[] userReadableAnalysesArray = new Integer[readableRequests.size()];
					userReadableAnalyses = " AND analysisConf.id IN(" +  HelperTools.implode(readableRequests.toArray(userReadableAnalysesArray)) + ")";
				}
				else
				{
					return setupData;
				}
			}
			
			ResultSet sqlResultAnalysisRequests = _executeQuery
			(
				"SELECT analysisConf.id" + "," +
				"analysisConf.meta_title" + "," +
				"analysisConf.meta_description" + "," +
				"analysisConf.numberTopics" + "," +
				"rawdataConf.id" + "," +
				"rawdataConf.meta_title" + "," +
				"rawdataConf.meta_description" + " " +
				"FROM " + analysisRequestsTableName + " AS analysisRequest " +
				"INNER JOIN " + analysisTableName + " AS analysisConf " +
				"ON analysisRequest.analysis_id = analysisConf.id " +
				"INNER JOIN " + rawdataTableName + " AS rawdataConf " +
				"ON analysisConf.dvita_configurations_rawdata_id = rawdataConf.id " +
				"WHERE analysisRequest.status = \"" + RequestAnalysis.STATUS_FINISHED + "\" " + userReadableAnalyses + " " +
				"ORDER BY rawdataConf.id ASC"
			);
			
			try
			{
				while (sqlResultAnalysisRequests.next())
				{
					DatabaseCollectionSetup databaseCollectionSetupUserRequest = null;
					
					// find rawdata entry
					for (DatabaseCollectionSetup collectionSetup : setupData)
					{
						if (collectionSetup.databaseConfigurationRawdata.id.equals(sqlResultAnalysisRequests.getInt("rawdataConf.id")))
						{
							databaseCollectionSetupUserRequest = collectionSetup;
							break;
						}
					}
					
					if (null == databaseCollectionSetupUserRequest)
					{
						databaseCollectionSetupUserRequest = new DatabaseCollectionSetup();
						
						DatabaseConfigurationRawdata databaseConfigurationRawdataUserRequest = new DatabaseConfigurationRawdata();
						databaseConfigurationRawdataUserRequest.id = sqlResultAnalysisRequests.getInt("rawdataConf.id");
						databaseConfigurationRawdataUserRequest.metaTitle = sqlResultAnalysisRequests.getString("rawdataConf.meta_title");
						databaseConfigurationRawdataUserRequest.metaDescription = sqlResultAnalysisRequests.getString("rawdataConf.meta_description");
						
						databaseCollectionSetupUserRequest.databaseConfigurationRawdata = databaseConfigurationRawdataUserRequest;
						setupData.add(databaseCollectionSetupUserRequest);
					}
					
					databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
					databaseConfigurationAnalysis.id = sqlResultAnalysisRequests.getInt("analysisConf.id");
					databaseConfigurationAnalysis.NumberTopics = sqlResultAnalysisRequests.getInt("analysisConf.numberTopics");
					databaseConfigurationAnalysis.metaTitle = sqlResultAnalysisRequests.getString("analysisConf.meta_title");
					databaseConfigurationAnalysis.metaDescription = sqlResultAnalysisRequests.getString("analysisConf.meta_description");
					
					databaseCollectionSetupUserRequest.databaseConfigurationAnalyses.add(databaseConfigurationAnalysis);
				}
				sqlResult.close();
				_statement.close();
				_connection.close();
				System.out.println("Result analysis got and closed");
			} 
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return setupData;
	}
	
	public HashMap<String,DatabaseConfigurationAnalysis> getConfigurationAnalysisMap()
	{
		HashMap<String,DatabaseConfigurationAnalysis> databaseConfigurationAnalysisList = new HashMap<String,DatabaseConfigurationAnalysis>();
		
		String analysisConfTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
		String rawdataConfTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
		
		ResultSet sqlResult = _executeQuery
		(
			"SELECT " + 
				"analysisConf.id" + "," +
				"analysisConf.meta_title" + "," +
				"analysisConf.meta_description" + "," +
				"rawdataConf.id" + "," +
				"rawdataConf.meta_title" + "," +
				"rawdataConf.meta_description" + " " +
			"FROM " + analysisConfTableName + " AS analysisConf " + 
			"INNER JOIN " + rawdataConfTableName + " AS rawdataConf " +
			"ON analysisConf.dvita_configurations_rawdata_id = rawdataConf.id"
		);
		
		try
		{
			while (sqlResult.next())
			{
				DatabaseConfigurationAnalysis databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
				databaseConfigurationAnalysis.databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
				
				// analysis configuration data
				databaseConfigurationAnalysis.id = sqlResult.getInt("analysisConf.id");
				databaseConfigurationAnalysis.metaTitle = sqlResult.getString("analysisConf.meta_title");
				databaseConfigurationAnalysis.metaDescription = sqlResult.getString("analysisConf.meta_description"); 
				
				// corresponding rawdata configuration data
				databaseConfigurationAnalysis.databaseConfigurationRawdata.id = sqlResult.getInt("rawdataConf.id");
				databaseConfigurationAnalysis.databaseConfigurationRawdata.metaTitle = sqlResult.getString("rawdataConf.meta_title"); 
				databaseConfigurationAnalysis.databaseConfigurationRawdata.metaDescription = sqlResult.getString("rawdataConf.meta_description");
				
				databaseConfigurationAnalysisList.put(databaseConfigurationAnalysis.id.toString(), databaseConfigurationAnalysis);			
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("analysis and configuration raw data got and closed");
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return databaseConfigurationAnalysisList;
	}
	
	public DatabaseConfigurationAnalysis getConfigurationDataAnalysis(int analysisId)
	{
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = null;
		
		String analysisConfTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_TABLE_NAME;
		String rawdataConfTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_RAWDATA_TABLE_NAME;
		String connectionsTableName = _db.tablePrefix + DBAccess.SQL_DATABASE_CONNECTION_TABLE_NAME;
		
		// TODO replace * with actual needed fields (giving them aliases
		ResultSet sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + analysisConfTableName + " analysisConf " + 
			"INNER JOIN " + connectionsTableName + " analysisConnection " +
			"ON analysisConf.dvita_database_connections_id = analysisConnection.id " +
			"INNER JOIN " + rawdataConfTableName + " rawdataConf " +
			"ON analysisConf.dvita_configurations_rawdata_id = rawdataConf.id " +
			"INNER JOIN " + connectionsTableName + " rawdataConnection " +
			"ON rawdataConf.dvita_database_connections_id = rawdataConnection.id " +
			"WHERE analysisConf.id=" + analysisId
		);

		try
		{
			databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
			databaseConfigurationAnalysis.databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
			
			sqlResult.next();
			
			// analysis configuration data
			databaseConfigurationAnalysis.id = sqlResult.getInt("analysisConf.id");
			databaseConfigurationAnalysis.rangeStart = sqlResult.getTimestamp("analysisConf.rangeStart");
			databaseConfigurationAnalysis.rangeEnd = sqlResult.getTimestamp("analysisConf.rangeEnd");
			databaseConfigurationAnalysis.NumberTopics = sqlResult.getInt("analysisConf.numberTopics");
			databaseConfigurationAnalysis.metaTitle = sqlResult.getString("analysisConf.meta_title");
			databaseConfigurationAnalysis.metaDescription = sqlResult.getString("analysisConf.meta_description"); 
			databaseConfigurationAnalysis.tablePrefix = sqlResult.getString("analysisConf.tablePrefix");
			databaseConfigurationAnalysis.status = sqlResult.getInt("analysisConf.status");
			// TODO extern granularity method
			switch( sqlResult.getInt("granularity"))
			{
				case 1: databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
				case 2: databaseConfigurationAnalysis.gran = Granularity.MONTHLY; break;
				case 3: databaseConfigurationAnalysis.gran = Granularity.WEEKLY; break;
				case 4: databaseConfigurationAnalysis.gran = Granularity.DAYLY; break;
				default: databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
			}
			databaseConfigurationAnalysis.server = sqlResult.getString("analysisConnection.server");
			databaseConfigurationAnalysis.port = sqlResult.getInt("analysisConnection.port");
			databaseConfigurationAnalysis.user = sqlResult.getString("analysisConnection.user");
			databaseConfigurationAnalysis.passwort = sqlResult.getString("analysisConnection.password");
			databaseConfigurationAnalysis.databasename = sqlResult.getString("analysisConnection.databasename");
			databaseConfigurationAnalysis.schema = sqlResult.getString("analysisConnection.schema");
			databaseConfigurationAnalysis.type = sqlResult.getInt("analysisConnection.type");
			
			// corresponding rawdata configuration data
			databaseConfigurationAnalysis.databaseConfigurationRawdata.id = sqlResult.getInt("rawdataConf.id");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameID = sqlResult.getString("rawdataConf.columnNameID"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameContent = sqlResult.getString("rawdataConf.columnNameContent");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameDate = sqlResult.getString("rawdataConf.columnNameDate");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameURL = sqlResult.getString("rawdataConf.columnNameURL");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameTitle = sqlResult.getString("rawdataConf.columnNameTitle"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.fromClause = sqlResult.getString("rawdataConf.from_clause"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause = sqlResult.getString("rawdataConf.where_clause"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix = sqlResult.getString("rawdataConf.tablePrefix"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.metaTitle = sqlResult.getString("rawdataConf.meta_title"); 
			databaseConfigurationAnalysis.databaseConfigurationRawdata.metaDescription = sqlResult.getString("rawdataConf.meta_description");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.server = sqlResult.getString("rawdataConnection.server");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.port = sqlResult.getInt("rawdataConnection.port");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.user = sqlResult.getString("rawdataConnection.user");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.passwort = sqlResult.getString("rawdataConnection.password");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.databasename = sqlResult.getString("rawdataConnection.databasename");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.schema = sqlResult.getString("rawdataConnection.schema");
			databaseConfigurationAnalysis.databaseConfigurationRawdata.type = sqlResult.getInt("rawdataConnection.type");
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Database configuration got and closed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return databaseConfigurationAnalysis;
	}
	
	public Integer getUserByIdent(String ident)
	{
		ResultSet sqlResult = _executeQuery("SELECT id FROM " + _db.tablePrefix + DBAccess.SQL_DATABASE_USERS_TABLE_NAME + " WHERE ident=\"" + ident + "\"");
				
		try
		{
			while (sqlResult.next())
			{
				return sqlResult.getInt("id");
			}
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void addUser(DVITAUser dvitaUser)
	{
		_executeUpdate("INSERT INTO " + _db.tablePrefix + DBAccess.SQL_DATABASE_USERS_TABLE_NAME
				+ " SET name=\""+dvitaUser.getName() + "\","
				+ "email=\""+dvitaUser.getEmail()+"\","
				+ "role=\""+dvitaUser.getRole()+"\","
				+ "ident=\""+dvitaUser.getIdent()+"\"");
	}
	
	public HashMap<Integer, ACLRole> getACLRoles()
	{
		HashMap<Integer, ACLRole> aclRoles = new HashMap<Integer, ACLRole>();
		
		ResultSet sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + _db.tablePrefix + DBAccess.SQL_DATABASE_ACL_ROLES_TABLE_NAME + " " +
			"ORDER BY id ASC"
		);
				
		try
		{
			while (sqlResult.next())
			{
				ACLRole aclRole = new ACLRole();
				aclRole.setRole(sqlResult.getString("role"));
				aclRole.setOperation(sqlResult.getString("operation"));
				
				aclRoles.put(sqlResult.getInt("id"), aclRole);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("AcL roles got  and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aclRoles;
	}
	
	public HashMap<Integer, DVITAUser> getUsers()
	{
		HashMap<Integer, DVITAUser> users = new HashMap<Integer, DVITAUser>();
		
		ResultSet sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + _db.tablePrefix + DBAccess.SQL_DATABASE_USERS_TABLE_NAME + " " +
			"ORDER BY id ASC"
		);
				
		try
		{
			while (sqlResult.next())
			{
				DVITAUser dvitaUser = new DVITAUser();
				dvitaUser.setId(sqlResult.getInt("id"));
				dvitaUser.setName(sqlResult.getString("name"));
				dvitaUser.setEmail(sqlResult.getString("email"));
				dvitaUser.setRole(sqlResult.getString("role"));
				dvitaUser.setIdent(sqlResult.getString("ident"));
			
				users.put(sqlResult.getInt("id"), dvitaUser);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Database users got  and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return users;
	}
	
	public void saveRequestAnalysis(RequestAnalysis requestAnalysis)
	{
		if (null == requestAnalysis.getId())
		{
			_executeUpdate("INSERT INTO " + _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REQUESTS_TABLE_NAME
					+ " SET user_id="+requestAnalysis.getUserId() + ","
					+ "url=\""+requestAnalysis.getUrl()+"\","
					+ "status=\""+requestAnalysis.getStatus()+"\"");
		}
		else
		{
			_executeUpdate
			(
				"UPDATE "  + _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REQUESTS_TABLE_NAME + " "
				+ "SET" + " "
				+ "status = \"" + requestAnalysis.getStatus() + "\","
				+ "analysis_id = " + requestAnalysis.getAnalysisId() + " "
				+ "WHERE id = " + requestAnalysis.getId()
			);
		}
	}
	
	public void saveAclUsers(ACLUsers aclUsers)
	{
		_executeUpdate("INSERT INTO " + _db.tablePrefix + DBAccess.SQL_DATABASE_ACL_USERS_TABLE_NAME
				+ " SET user_id="+aclUsers.getUserId() + ","
				+ "operation=\""+aclUsers.getOperation()+"\","
				+ "target_id="+aclUsers.getTarget());
	}
	
	public HashMap<Integer, RequestAnalysis> getRequestAnalysis()
	{
		HashMap<Integer, RequestAnalysis> requestAnalyses = new HashMap<Integer, RequestAnalysis>();
		
		ResultSet sqlResult = _executeQuery
		(
			"SELECT * " + 
			"FROM " + _db.tablePrefix + DBAccess.SQL_DATABASE_ANALYSIS_REQUESTS_TABLE_NAME + " request " +
			"INNER JOIN " + _db.tablePrefix + DBAccess.SQL_DATABASE_USERS_TABLE_NAME + " user " +
			"ON request.user_id = user.id " +
			"ORDER BY request.id ASC"
		);

		try
		{
			while (sqlResult.next())
			{
				RequestAnalysis requestAnalysis = new RequestAnalysis();
				requestAnalysis.setId(sqlResult.getInt("request.id"));
				requestAnalysis.setStatus(sqlResult.getString("request.status"));
				requestAnalysis.setUrl(sqlResult.getString("request.url"));
				requestAnalysis.setAnalysisId(sqlResult.getInt("request.analysis_id"));
				requestAnalysis.setUserId(sqlResult.getInt("request.user_id"));
				
				DVITAUser dvitaUser = new DVITAUser();
				dvitaUser.setId(sqlResult.getInt("user.id"));
				dvitaUser.setName(sqlResult.getString("user.name"));
				dvitaUser.setEmail(sqlResult.getString("user.email"));
				dvitaUser.setRole(sqlResult.getString("user.role"));
				dvitaUser.setIdent(sqlResult.getString("user.ident"));
				
				requestAnalysis.setUser(dvitaUser);
			
				requestAnalyses.put(sqlResult.getInt("request.id"), requestAnalysis);
			}
			sqlResult.close();
			_statement.close();
			_connection.close();
			System.out.println("Analyses request got  and closed");
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return requestAnalyses;
	}
	
	private void _executeUpdate(String sqlQuery)
	{
		Connection connection = _createConnection();
		Statement statement = null;
		
		try
		{
			statement = connection.createStatement();
			statement.executeUpdate(sqlQuery);
			statement.close();
			connection.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ResultSet _executeQuery(String sqlQuery)
	{
		ResultSet sqlResult = null;
		
		Connection connection = _createConnection();
		Statement statement = null;
		
		try
		{
			statement = connection.createStatement();
			sqlResult = statement.executeQuery(sqlQuery);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// for later closing
		_statement = statement;
		_connection = connection;
		
		return sqlResult;
	}
	
	protected Connection _createConnection()
	{
		Connection connection = null;
		
		switch (_db.type)
		{
		case 0: break;
		case 1:
		 	try 
		 	{
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection
				(
						"jdbc:mysql://"+_db.server+":"+_db.port+"/"+_db.databasename,
						_db.user,
						_db.passwort
				);
			} 
		 	catch (ClassNotFoundException e1)
		 	{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		 	catch (SQLException e1) 
		 	{
		 		// TODO Auto-generated catch block
		 		e1.printStackTrace();
		 	}
		 	
		 	break;
		case 2: 
			try 
			{
				Class.forName("com.ibm.db2.jcc.DB2Driver");
				connection = DriverManager.getConnection
				(
					"jdbc:db2://" + _db.server + ":" + _db.port + "/" + _db.databasename + ":currentSchema=" + _db.schema + ";",
					_db.user,
					_db.passwort
				);
			} 
			catch (SQLException e)
			{
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}

			break;
		}
		
		return connection;
	}
	
}

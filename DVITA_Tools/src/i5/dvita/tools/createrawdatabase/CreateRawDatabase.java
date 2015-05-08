package i5.dvita.tools.createrawdatabase;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.tools.commons.ToolsIPC;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CreateRawDatabase
{
	final static String MANIFEST_INPUT_SQL_ID = "inputSQL";
	final static String MANIFEST_OUTPUT_SQL_ID = "outputSQL";
	
	public static void main(String[] args) throws Exception
	{
		// IPC input
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = null;

		ToolsIPC toolsIPC = new ToolsIPC(MANIFEST_INPUT_SQL_ID);
		Object inputData = toolsIPC.read().get(MANIFEST_INPUT_SQL_ID);
		
		if (inputData instanceof DatabaseConfigurationAnalysis)
		{
			databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) inputData;
		}
		else
		{
			// TODO error
			toolsIPC.write("error wrong datatype");
	    	System.exit(1);
		}
		
		DBAccess dbAccess = new DBAccess(databaseConfigurationAnalysis.databaseConfigurationRawdata);

		if (1 != args.length)
		{
			toolsIPC.write("Usage: CreateRawDatabase <tableName>");
			System.exit(1);
		}
		
		String tableName = args[0];
		
		String sqlQuery = 
				"CREATE TABLE `" + tableName + "`("
				+ "id integer NOT NULL AUTO_INCREMENT,"
				+ "date varchar(20) NOT NULL,"
				+ "content TEXT,"
				+ "title varchar(255) NOT NULL,"
				+ "url varchar(255),"
				+ "PRIMARY KEY (id))";
		
		dbAccess.doQueryUpdate(sqlQuery);
		
		Path pathInput = Paths.get("input-description.csv");
		
		for (String line : Files.readAllLines(pathInput, StandardCharsets.UTF_8))
		{
			String[] el = line.split("\t"); 
			
			String id = el[0];
			String title = el[1];
			String content = el[3];
			String url = el[4];
			String year = el[5];
			
			year = year.replaceAll("\\D+", "");
			
			if (2 == year.length())
			{
				if (Integer.parseInt(year) < 20)
				{
					year = "20" + year;
				}
				else
				{
					year = "19" + year;
				}
			}
			
			sqlQuery = 
					"INSERT INTO `" + tableName + "` SET "+
		    		"`title`=\""+title.replaceAll("\"", "'")+
		    		"\",`content`=\""+content.replaceAll("\"", "'")+
		    		"\",`url`=\""+url+
		    		"\",`date`=\"" + year + "-01-01\"";
			
			dbAccess.doQueryUpdate(sqlQuery);
			
			toolsIPC.write(id+" added");
		}
		
		// IPC output
		HashMap<String, DatabaseConfigurationAnalysis> outputData = new HashMap<String, DatabaseConfigurationAnalysis>();
		outputData.put(MANIFEST_OUTPUT_SQL_ID, databaseConfigurationAnalysis);
		toolsIPC.write(outputData);
	}
}

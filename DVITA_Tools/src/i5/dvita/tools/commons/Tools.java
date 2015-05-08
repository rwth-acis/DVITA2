package i5.dvita.tools.commons;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Tools
{
	public static Double[] getCurrent(int topic, DatabaseConfigurationAnalysis databaseConfigurationAnalysis, int timesteps)
	{
		Double[] TopicLists= new Double[timesteps];
		//ArrayList<Integer> TopicID= new ArrayList<Integer>();

		for(int j= 0; j<timesteps; j++)
		{
			// bestimme dokumente pro zeitintervall
			// ist nötig für den average
			// (wir können unten NICHT den SQL avg nehmen, da wir Dokumente
			// mit zu geringen TopicPropoertions gepruned haben, das sind aber
			// für jedes Topic andere Dokumente)
			DBAccess dbAccess = new DBAccess(databaseConfigurationAnalysis);
			dbAccess.addColumn("count(DISTINCT DOCID) AS numberDocs");
			dbAccess.addTable(databaseConfigurationAnalysis.tablePrefix+ "_BELONGTO");
			dbAccess.setWhere("INTERVALID ="+j);
			double numberDocs = Double.parseDouble((dbAccess.getRecordsByRows().get(0).get("numberDocs")));
				
				// average oder sum??? evtl. sum umschalten
				// wenn zu jedem zeitpunkt sehr unterschiedeliche anzahl dokumente sind
				// dann koentte average besser sein
				// ACHTUNG: hier dennoch sum. average kommt gleich
				// liegt daran, dass wir pro topic unterschiedliche anzahl docs haben
				// daher ist der average nicht der normale average

			Double y = 0.0;
			
			dbAccess = new DBAccess(databaseConfigurationAnalysis);
			dbAccess.addColumn("SUM(TOPICPROPORTION) AS y");
			dbAccess.addTable(databaseConfigurationAnalysis.tablePrefix+"_BELONGTO");
			dbAccess.setWhere("TOPICID= "+topic+" AND  INTERVALID ="+j);

			if (!(null == dbAccess.getRecordsByRows().get(0).get("y")))
			{
				y = Double.parseDouble(dbAccess.getRecordsByRows().get(0).get("y"));
			}
			TopicLists[j]=y/numberDocs; // nun ist der average korrekt berechnet!!!
				//System.out.println("einträge für " +j+"von Topic"+topic+ "ist :"+ TopicLists[j]);
	
			
			
		}

		return TopicLists;
	}

	
	public static void DropTableIfExists(DatabaseConfigurationAnalysis databaseConfigurationAnalysis, String table)
	{
		DBAccess dbAccess = new DBAccess(databaseConfigurationAnalysis);
    	
		// mysql
		if(databaseConfigurationAnalysis.type==1)
		{
			dbAccess.doQueryUpdate("DROP TABLE IF EXISTS " + table);
			return;
		}
		
		// db2
		if(databaseConfigurationAnalysis.type==2)
		{
			dbAccess.doQueryUpdate("if(exists(select 1 from syscat.tables where tabschema = '" + databaseConfigurationAnalysis.schema + "' and tabname = '" + table + "')) then drop table MYSCHEMA.MYTABLE;end if;");
		}
	}
		

	public static void copyFolder(File src, File dest)
		throws IOException{
	
		if(src.isDirectory()){
	
			//if directory not exists, create it
			if(!dest.exists()){
			   dest.mkdir();
			   //System.out.println("Directory copied from " 
	             //             + src + "  to " + dest);
			}
	
			//list all the directory contents
			String files[] = src.list();
	
			for (String file : files) {
			   //construct the src and dest file structure
			   File srcFile = new File(src, file);
			   File destFile = new File(dest, file);
			   //recursive copy
			   copyFolder(srcFile,destFile);
			   //System.out.println("File copied from " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
			} 
	
		}else{
			//if file, then copy it
			//Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
		        OutputStream out = new FileOutputStream(dest); 
	
		        byte[] buffer = new byte[1024];
	
		        int length;
		        //copy the file content in bytes 
		        while ((length = in.read(buffer)) > 0){
		    	   out.write(buffer, 0, length);
		        }
	
		        in.close();
		        out.close();
		        //System.out.println("File copied from " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
		}
	}
}

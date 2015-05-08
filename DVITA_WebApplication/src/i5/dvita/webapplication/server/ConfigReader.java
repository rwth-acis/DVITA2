package i5.dvita.webapplication.server;


import i5.dvita.dbaccess.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ConfigReader  implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent e) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		// NOTE: this sets the server side file path of the dvita_config.txt file
		// it must reside under the dvita app folder otherwise you will see a 500 error on startup
		try {
			DBAccess.CONFIG_FILE_PATH =e.getServletContext().getRealPath("dvita_config.txt");
			
		} catch (Exception e1) {			
			e1.printStackTrace();
		}
		
	}	
}

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import i5.dvita.dbaccess.ConfigRawdataShared;
//import i5.dvita.dbaccess.ConfigTopicminingShared;
//import i5.dvita.dbaccess.ConfigTopicminingShared.Granularity;
//
//public class ConfigReader {
//	
//	static String schema = ConnectionManager.schema;
//	
//	
//	// liest aus den entsprechenden Konfigurationstabellen die Daten aus und speichert
//	// diese in die entsprechenden Java Objekte
//	
//	public static ConfigTopicminingShared readConfigTopicmining(int analysisID, Statement statement) throws SQLException {
//		ConfigTopicminingShared info2 = new ConfigTopicminingShared();
//		String sqlquery2="SELECT * FROM "+ConnectionManager.schema+"config_topicmining WHERE id="+analysisID;
//		System.out.println("ConfigReader->readConfigTopicmining: "+sqlquery2);
//		ResultSet sq2 = statement.executeQuery(sqlquery2);
//
//		
//		while(sq2.next()) {
//			info2.NumberTopics=sq2.getInt("numberTopics");;
//			 info2.tablePrefix = sq2.getString("tablePrefix");
//			 info2.metaDescription = sq2.getString("meta_description"); 
//			 info2.id = sq2.getInt("id");
//			 info2.metaTitle = sq2.getString("meta_title"); 
//			 info2.rawdataID = sq2.getInt("rawdataID");
//			 info2.rangeEnd = sq2.getTimestamp("rangeEnd");
//			 info2.rangeStart = sq2.getTimestamp("rangeStart");
//			 
//			 // Zuordnung der granularity aus DB zu den Granularity aus der Menge{Yearly, Monthly ,...}
//			 
//			 switch( sq2.getInt("granularity")) {
//			 	case 1: info2.gran = Granularity.YEARLY; break;
//			 	case 2: info2.gran = Granularity.MONTHLY; break;
//			 	case 3: info2.gran = Granularity.WEEKLY; break;
//			 	case 4: info2.gran = Granularity.DAYLY; break;
//			 	default: info2.gran = Granularity.YEARLY; break;
//			 }
//			 
//			
//		}
//
//		return info2;
//	}
//	
//	public static ConfigRawdataShared readConfigRawdata(int rawdataID, Statement statement) throws SQLException {
//
//			ConfigRawdataShared info = new ConfigRawdataShared();
//			
//			String sqlquery="SELECT * FROM "+ConnectionManager.schema+"config_rawdata WHERE id="+rawdataID;
//			System.out.println("ConfigReader->readConfigRawdata: "+sqlquery);
//			ResultSet sql = statement.executeQuery(sqlquery);
//			
//			sql.next();
//				
//				
//				 info.columnNameContent = sql.getString("columnNameContent");
//				 info.columnNameDate = sql.getString("columnNameDate");
//				 info.columnNameURL = sql.getString("columnNameURL");
//				 info.columnNameTitle = sql.getString("columnNameTitle"); 
//				 info.fromClause = sql.getString("from"); 
//				 info.whereClause = sql.getString("where"); 
//				 info.columnNameID = sql.getString("columnNameID"); 
//				 info.tablePrefix = sql.getString("tablePrefix"); 
//				 info.rawdataID = sql.getInt("id");
//				 info.metaTitle = sql.getString("meta_title"); 
//				 info.metaDescription = sql.getString("meta_description"); 
//				 Integer remote = sql.getInt("connectionID");
//				 if(remote == null || remote == 0) {
//					 info.dataOnHostServer = true;
//				 } else {
//					 info.dataOnHostServer = false;
//					 // dann nun auch die remote connection daten holen
//					 String sqlquery3="SELECT * FROM "+ConnectionManager.schema+"config_connection WHERE id="+remote;
//					 System.out.println("ConfigReader->readConfigRawdata: "+sqlquery3);
//					 ResultSet sq3 = statement.executeQuery(sqlquery3);
//					 sq3.next();
//					 info.type = sq3.getInt("type");
//					 info.server = sq3.getString("server");
//					 info.port = sq3.getInt("port");
//					 info.databasename = sq3.getString("databasename");
//					 
//					 
//					 
//					 // für diese ID ist es in der Datenbank gespeichert
//					 String user = sq3.getString("user");
//					 if(user != null && user != "null") {
//						 info.user = user;
//						 info.passwort = sq3.getString("password");
//					 }
//					 
//					 // für diese ID im code gespeochert
//					 if(remote==2) {
//						 info.user = "aercsro";
//						 info.passwort = "roddick12";
//					 } else if(remote==3) {
//						 info.user = "lfrontierro";
//						 info.passwort = "T-Map-read";
//					 }
//				 } 
//			
//	
//			return info;
//
//		
//	}
//
//
//}

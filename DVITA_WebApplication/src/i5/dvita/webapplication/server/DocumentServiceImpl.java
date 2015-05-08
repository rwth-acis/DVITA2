package i5.dvita.webapplication.server;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.SerializablePair;
import i5.dvita.webapplication.client.DocumentService;
import i5.dvita.webapplication.shared.DocumentData;
import i5.dvita.webapplication.shared.DocumentInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server side implementation of the RPC service.
 * @param <wordsOfTopics>
 */
@SuppressWarnings("serial")
public class DocumentServiceImpl extends RemoteServiceServlet implements DocumentService
{
	public DocumentInfo[] documentSearch(String words)
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		DocumentInfo[] documentSearch = null ;
		
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		ArrayList<Integer> wordIDList= new ArrayList<Integer>();

		String[] wordsExploded = words.split(" ");
		
		for (Integer i = 0; i < wordsExploded.length; i++)
		{
			Stemmer s = new Stemmer();
			s.add(wordsExploded[i].toCharArray(),wordsExploded[i].length());
			s.stem();
			String StemWordOfSearchedItem= s.toString();			

			DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
			dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_WORDS");
			dbaccess.addColumn("ID");
			dbaccess.setWhere("STEMNAME='"+StemWordOfSearchedItem+"'");
			sqlResultAsColumn = dbaccess.getRecordsByColumns().get("ID");
			
			if (0 < sqlResultAsColumn.size())
			{
				for (String sqlResult : sqlResultAsColumn)
				{
					wordIDList.add(Integer.parseInt(sqlResult));
				}
			}
			
			String listOfWordIDs;	
			
			if(wordIDList.isEmpty())
			{
				return new DocumentInfo[0];
			}
			
			listOfWordIDs = wordIDList.get(0)+"";
			
			for(i=1; i<wordIDList.size(); i++)
			{
				listOfWordIDs += ","+wordIDList.get(i);
			}
			
			
			ArrayList<Integer> docIDList= new ArrayList<Integer>();
			ArrayList<Integer> wordsQuantityInDoc= new ArrayList<Integer>();

			dbaccess = new DBAccess(databaseConfigurationAnalysis);
			dbaccess.addTable("(SELECT DOCID, count(*) as c, sum(QUANTITY) as q from "+databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix+"_CONTAINS where WORDID in ("+listOfWordIDs+")  group by DOCID ORDER BY count(*) desc, sum(QUANTITY) DESC LIMIT 10) t1");
			dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix+"_BELONGTO t2");
			dbaccess.addColumn("distinct t1.DOCID as DOCID");
			dbaccess.addColumn("t2.INTERVALID as TOPICTIME");
			dbaccess.addColumn("t1.c");
			dbaccess.addColumn("t1.q");
			dbaccess.setWhere("t1.DOCID=t2.DOCID");
			dbaccess.setOrder("t1.c desc, t1.q desc");
			sqlResultAsRows = dbaccess.getRecordsByRows();
			
			for (HashMap<String,String> row : sqlResultAsRows)
			{
				docIDList.add(Integer.parseInt(row.get("DOCID")));
				wordsQuantityInDoc.add(Integer.parseInt(row.get("TOPICTIME")));
			}
			
			Integer[]idOfDocs=new Integer[docIDList.size()];
			idOfDocs=docIDList.toArray(idOfDocs);
			Integer[]quantityOfWordInDoc=new Integer[wordsQuantityInDoc.size()];
			quantityOfWordInDoc=wordsQuantityInDoc.toArray(quantityOfWordInDoc);
			
			String where = "";
			if(databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause != null && databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause.length()>0)
			{
				where = databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause + " AND ";
			}
			
			documentSearch = new DocumentInfo[idOfDocs.length];
			
			for(i=0;i<idOfDocs.length;i++)
			{
				dbaccess = new DBAccess(databaseConfigurationAnalysis.databaseConfigurationRawdata);
				dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.fromClause);
				dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameTitle+" as Title");
				dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameDate+" as Datum");
				dbaccess.setWhere(where+""+databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameID+"="+idOfDocs[i]+"");
				sqlResultAsRows = dbaccess.getRecordsByRows();
				
				documentSearch[i] = new DocumentInfo();
			
				for (HashMap<String,String> row : sqlResultAsRows)
				{
					documentSearch[i].docTitle = row.get("Title");
					documentSearch[i].docDate = row.get("Datum");
				}
			}	
			
			Integer[][]docsAndQuantity=new Integer[idOfDocs.length][quantityOfWordInDoc.length];
			
			for(int j=0;j<idOfDocs.length;j++)
			{
				docsAndQuantity[j][0]=idOfDocs[j];
				documentSearch[j].docID=idOfDocs[j];
				documentSearch[j].intervalID=quantityOfWordInDoc[j];

				HashMap<Integer, Double> res = getTopicProportions___(idOfDocs[j]);
				documentSearch[j].topicIDs = new Integer[res.size()];
				documentSearch[j].topicProportions = new Double[res.size()];	
				
				Integer counter = 0;
				for (Map.Entry<Integer,Double> entry : res.entrySet())
				{
					documentSearch[j].topicIDs[counter] = entry.getKey();
					documentSearch[j].topicProportions[counter] = entry.getValue();
					counter++;
				}
				
			}
		}
		
		
		
		return documentSearch;
	}
	
	
	public DocumentInfo[] relatedDocuments (int input,int limit,int topictime) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		DocumentInfo[] relatedDocuments;
		
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		ArrayList<String> sqlResultAsColumnDocumentIds = new ArrayList<String>();
		HashMap<Integer,Double> topicProportions = new HashMap<Integer,Double>();
		String sqlWhere = "";
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
		dbaccess.addColumn("MAX(TOPICID) AS x");
		dbaccess.addColumn("MIN(TOPICID) AS y");
		sqlResultAsRows = dbaccess.getRecordsByRows();
		Integer x = Integer.parseInt(sqlResultAsRows.get(0).get("x"));
		Integer y = Integer.parseInt(sqlResultAsRows.get(0).get("y"));
		
		if (input < y || input > x)
		{
			return null;
		} 
		else
		{			
			dbaccess = new DBAccess(databaseConfigurationAnalysis);
			dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
			dbaccess.addColumn("DOCID");
			dbaccess.setWhere("TOPICID= " + input + " AND INTERVALID= " + topictime);
			dbaccess.setOrder("TOPICPROPORTION DESC");
			dbaccess.setLimit(String.valueOf(limit));
			sqlResultAsColumnDocumentIds = dbaccess.getRecordsByColumns().get("DOCID");
			
			relatedDocuments = new DocumentInfo[sqlResultAsColumnDocumentIds.size()];

			for (Integer i = 0; i < sqlResultAsColumnDocumentIds.size(); i++)
			{
				//speichere die Objekte eines Dokumentes ja in dem erzeugten Arry für Dokumente =>   Doc2, Doc 4 , Doc 8
				//  Objekte:                                                                        Titel: PC,  Gen,   Handy
				//	Objekte:	
				relatedDocuments[i] = new DocumentInfo();
				relatedDocuments[i].docID = Integer.parseInt(sqlResultAsColumnDocumentIds.get(i));
				
				//gehe die einzelnen DocID's gespeichert in DocID[i] durch und suche nach deren Titel	
				dbaccess = new DBAccess(databaseConfigurationAnalysis.databaseConfigurationRawdata);
				dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.fromClause);
				dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameTitle+" AS Title");
				dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameDate+" AS Date");
				
				if (null != databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause && 0 < databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause.length())
				{
					sqlWhere = databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause + " AND ";
				}
				
				dbaccess.setWhere(sqlWhere + databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameID + "=" + relatedDocuments[i].docID);
				sqlResultAsRows = dbaccess.getRecordsByRows();
				
				if (0 < sqlResultAsRows.size())
				{
					relatedDocuments[i].docTitle = sqlResultAsRows.get(0).get("Title");
					relatedDocuments[i].docDate = sqlResultAsRows.get(0).get("Date");
				}
				
				topicProportions = getTopicProportions___(relatedDocuments[i].docID);
				
				relatedDocuments[i].topicIDs = new Integer[topicProportions.size()];
				relatedDocuments[i].topicProportions = new Double[topicProportions.size()];			
				
				Integer mapCounter = 0;
				Set<Integer> keySet = topicProportions.keySet();
				for (Integer topicId : keySet)
				{
					relatedDocuments[i].topicIDs[mapCounter] = topicId;
					relatedDocuments[i].topicProportions[mapCounter] = topicProportions.get(topicId);
					mapCounter++;
				}
				
				relatedDocuments[i].intervalID = topictime;
			}
		}
		
		return relatedDocuments;
	}
	
	
	private HashMap<Integer,Double> getTopicProportions___(Integer documentId)
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		HashMap<Integer,Double> topicProportions = new HashMap<Integer,Double>();
		
		if (null == documentId || 0 == documentId)
		{
			return topicProportions;
		}
		
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
		dbaccess.addColumn("TOPICID");
		dbaccess.addColumn("TOPICPROPORTION");
		dbaccess.setWhere("DOCID="+documentId);
		dbaccess.setOrder("TOPICPROPORTION");
		sqlResultAsRows = dbaccess.getRecordsByRows();
		
		for (HashMap<String,String> mapTopicIdToTopicProportion : sqlResultAsRows)
		{
			topicProportions.put(Integer.parseInt(mapTopicIdToTopicProportion.get("TOPICID")), Double.parseDouble(mapTopicIdToTopicProportion.get("TOPICPROPORTION")));
		}
		
		return topicProportions;
	}
	
	
	protected SerializablePair<Integer[], Double[]> getTopicProportions(PreparedStatement ps, int docid) throws SQLException
	{
		//save the topic-proportions for visualising its result in a pei chart!!
		ps.setInt(1,docid);
		ResultSet sql3 = ps.executeQuery();

		ArrayList<Double> TOPICPROPORTION=new ArrayList<Double>();
		ArrayList<Integer> IDsTOPIC= new ArrayList<Integer>();

		while(sql3.next())
		{
			IDsTOPIC.add(sql3.getInt("TOPICID"));
			TOPICPROPORTION.add(sql3.getDouble("TOPICPROPORTION")); 
		}
		
		Integer [] TOPICIDs= new Integer[IDsTOPIC.size()];
		TOPICIDs = IDsTOPIC.toArray(TOPICIDs);
		Double [] TOPICsPROPORTION= new Double[ TOPICPROPORTION.size()];
		TOPICsPROPORTION = TOPICPROPORTION.toArray(TOPICsPROPORTION);

		SerializablePair<Integer[],Double[]> p = new SerializablePair<Integer[],Double[]>();
		p.first = TOPICIDs;
		p.second = TOPICsPROPORTION;
		return p;
	}


	public DocumentData getDocumentData(int docid) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		DocumentData documentData = new DocumentData();
		
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		String sqlWhere = "";
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis.databaseConfigurationRawdata);
		dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.fromClause);
		dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameContent + " AS Text");
		dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameTitle + " AS Title");
		dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameDate + " AS Datum");
		
		if (null != databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameURL)
		{
			dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameURL + " AS URL");
		}
		
		if (null != databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause && 0 < databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause.length())
		{
			sqlWhere = databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause + " AND ";
		}
		
		dbaccess.setWhere(sqlWhere + databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameID + "=" + docid);
		
		sqlResultAsRows = dbaccess.getRecordsByRows();
		documentData.title = sqlResultAsRows.get(0).get("Title");
		documentData.content = sqlResultAsRows.get(0).get("Text");
		documentData.date = sqlResultAsRows.get(0).get("Datum");
		
		if (sqlResultAsRows.get(0).containsKey("URL"))
		{
			documentData.url = dbaccess.getRecordsByRows().get(0).get("URL");
		}
		
		return documentData;
	}


	
	// liefert zum gegeben docID die 10 ähnlichsten Dokumente
	// aus dem Zeitpunkt topictime!!!
	// das dokument selbst muss nicht zu topictime vorliegen, aber die anderen dokumente
	
	public DocumentInfo[] similarDocuments(int docid, int topictime) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		DocumentInfo[] similarDocuments;
		
		ArrayList<String> sqlResultAsColumnDocumentIds = new ArrayList<String>();
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		HashMap<Integer,Double> topicProportions = new HashMap<Integer,Double>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_SIMILARDOCS");
		dbaccess.addColumn("DOCIDDESTINATION");
		dbaccess.setWhere("INTERVALID="+topictime+" AND DOCIDSOURCE="+docid);
		dbaccess.setOrder("position ASC");
		sqlResultAsColumnDocumentIds = dbaccess.getRecordsByColumns().get("DOCIDDESTINATION");
		
		similarDocuments = new DocumentInfo[sqlResultAsColumnDocumentIds.size()];
		
		for (Integer i = 0; i < sqlResultAsColumnDocumentIds.size(); i++)
		{
			similarDocuments[i] = new DocumentInfo();
			similarDocuments[i].docID = Integer.parseInt(sqlResultAsColumnDocumentIds.get(i));
			
			topicProportions = getTopicProportions___(similarDocuments[i].docID);
			
			similarDocuments[i].topicIDs = new Integer[topicProportions.size()];
			similarDocuments[i].topicProportions = new Double[topicProportions.size()];			
			
			Integer mapCounter = 0;
			Set<Integer> keySet = topicProportions.keySet();
			for (Integer topicId : keySet)
			{
				similarDocuments[i].topicIDs[mapCounter] = topicId;
				similarDocuments[i].topicProportions[mapCounter] = topicProportions.get(topicId);
				mapCounter++;
			}
			
			String where = "";
			
			if(null != databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause && 0 < databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause.length())
			{
				where = databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause + " AND ";
			}
			
			dbaccess = new DBAccess(databaseConfigurationAnalysis.databaseConfigurationRawdata);
			dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.fromClause);
			dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameDate+" AS Datum");
			dbaccess.addColumn(databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameTitle+" AS Title");
			dbaccess.setWhere(where+" "+databaseConfigurationAnalysis.databaseConfigurationRawdata.columnNameID+"="+similarDocuments[i].docID+"");
			sqlResultAsRows = dbaccess.getRecordsByRows();
			
			if (0 < sqlResultAsRows.size())
			{
				similarDocuments[i].docTitle=sqlResultAsRows.get(0).get("Title");
				similarDocuments[i].docDate=sqlResultAsRows.get(0).get("Datum");
				similarDocuments[i].intervalID=topictime;
			}
		}
		
		return similarDocuments;
	}
}

package i5.dvita.webapplication.server;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.HelperTools;
import i5.dvita.webapplication.client.WordService;
import i5.dvita.webapplication.shared.WordData;
import i5.dvita.webapplication.shared.WordEvolutionData;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;


import org.apache.commons.lang3.ArrayUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 

@SuppressWarnings("serial")
public class WordServiceImpl extends RemoteServiceServlet implements WordService
{
	@Override
	public WordEvolutionData getWordEvolution(Integer[] wordsIds, int topicID) throws IllegalArgumentException 
	{
		WordEvolutionData wordEvolutionData = new WordEvolutionData();
		wordEvolutionData.wordIDs = wordsIds;
		
		if(wordsIds.length == 0)
			return wordEvolutionData;
		
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		HashMap<String,ArrayList<String>> result = new HashMap<String,ArrayList<String>>();
		ArrayList<HashMap<String,String>> result2 = new ArrayList<HashMap<String,String>>();
	
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix+ "_TOPICINTERVALS");
		dbaccess.addColumn("intervalStart");
		dbaccess.addColumn("ID");
		result = dbaccess.getRecordsByColumns();
		
		String[] TopicTime = new String[result.get("intervalStart").size()];
		TopicTime = result.get("intervalStart").toArray(TopicTime);
	
		String [] TimeId = new String[result.get("ID").size()];		
		TimeId = result.get("ID").toArray(TimeId);
		
		dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix+ "_DESCRIBEDBY");
		dbaccess.addColumn("WORDID");
		dbaccess.addColumn("INTERVALID");
		dbaccess.addColumn("PROBABILITY");
		dbaccess.setWhere("WORDID IN ("+HelperTools.implode(wordsIds)+") AND INTERVALID IN ("+HelperTools.implode(TimeId)+") AND TOPICID="+topicID);
		result2 = dbaccess.getRecordsByRows();
		
		//MD
		Double[][]wordsCount = new Double [wordsIds.length][TimeId.length];
		for (HashMap<String,String> resultRow : result2)
		{
			wordsCount[ArrayUtils.indexOf(wordsIds, Integer.parseInt(resultRow.get("WORDID")))]
					[Integer.parseInt(resultRow.get("INTERVALID"))] 
							= Double.parseDouble(resultRow.get("PROBABILITY"));			
		}
		/*
		Integer c1 = 0;
		Integer c2 = Integer.parseInt(result2.get(0).get("WORDID"));
		Double[][]wordsCount = new Double [wordsIds.length][TimeId.length];
		for (HashMap<String,String> resultRow : result2)
		{
			if (c2 != Integer.parseInt(resultRow.get("WORDID"))) {
				c2 = Integer.parseInt(resultRow.get("WORDID")); 
				c1++; 
			}
			
			if (c1 >= wordsIds.length)
			{
				break;
			}
			
			wordsCount[c1][Integer.parseInt(resultRow.get("INTERVALID"))] = Double.parseDouble(resultRow.get("PROBABILITY"));
		}*/
		
		wordEvolutionData.relevanceAtTime=wordsCount;
		wordEvolutionData.intervalStartDate=TopicTime;
		
		return wordEvolutionData;
	}
	
	
	@Override
	public WordData bestWords(int topic, int Number, long topictime) 
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		WordData wordsOfTopics = new WordData(); 
		
		ArrayList<String> wordIdsAsStrings = new ArrayList<String>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix+ "_DESCRIBEDBY");
		dbaccess.addColumn("WORDID");
		dbaccess.setWhere("TOPICID="+topic+" AND INTERVALID="+topictime);
		dbaccess.setOrder("PROBABILITY DESC");
		dbaccess.setLimit(String.valueOf(Number));
		wordIdsAsStrings = dbaccess.getRecordsByColumns().get("WORDID");
		Number = wordIdsAsStrings.size();
		
		Integer[] wordIds = new Integer[Number];
		Integer c = 0;
		for (String wordIdAsString : wordIdsAsStrings)
		{
			wordIds[c++] = Integer.parseInt(wordIdAsString);
		}
		
		dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_WORDS");
		dbaccess.addColumn("NAME");
		dbaccess.setWhere("ID IN("+HelperTools.implode(wordIds)+")");

		String[] wordNames = new String[Number];
		wordNames = dbaccess.getRecordsByColumns().get("NAME").toArray(wordNames);
		
		wordsOfTopics.wordIds = wordIds;
		wordsOfTopics.words = wordNames;
		
		return wordsOfTopics;
	}
}

package i5.dvita.webapplication.server;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.HelperTools;
import i5.dvita.webapplication.client.TopicService;
import i5.dvita.webapplication.shared.ThemeRiverData;
import i5.dvita.webapplication.shared.TopicLabels;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class TopicServiceImpl extends RemoteServiceServlet implements TopicService
{ 
	static int nrWords = 4;
	
	HashMap<Integer,String> mappingWordIDtoWordNAME = new  HashMap<Integer,String>();

	public TopicLabels getTopicList() throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		TopicLabels topicLabels = new TopicLabels();
		
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		ArrayList<HashMap<String,String>> sqlResultAsRows = new ArrayList<HashMap<String,String>>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_DESCRIBEDBY");
		dbaccess.addColumn("DISTINCT TOPICID");
		sqlResultAsColumn = dbaccess.getRecordsByColumns().get("TOPICID");
		
		Integer[]TopicIds = new Integer[sqlResultAsColumn.size()];
		Integer c = 0;
		for (String TopicId : sqlResultAsColumn)
		{
			TopicIds[c++] = Integer.parseInt(TopicId);
		}

		Integer[][]WORDIDS = new Integer [TopicIds.length][nrWords];
		String[][]WORDS = new String [TopicIds.length][nrWords];
		Integer[] allWordIDS = new Integer[TopicIds.length*nrWords];
		
		Integer globalWordCounter = 0;
		
		for(int i=0; i <TopicIds.length; i++ )
		{
			dbaccess = new DBAccess(databaseConfigurationAnalysis);
			dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_DESCRIBEDBY");
			dbaccess.addColumn("WORDID");
			dbaccess.addColumn("SUM(PROBABILITY) AS A");
			dbaccess.setWhere("TOPICID = "+TopicIds[i]);
			dbaccess.setGroup("WORDID");
			dbaccess.setOrder("A DESC");
			dbaccess.setLimit(String.valueOf(nrWords));
			sqlResultAsColumn = dbaccess.getRecordsByColumns().get("WORDID");
			
			int j=0;
			for (String wordId : sqlResultAsColumn)
			{
				WORDIDS[i][j]= Integer.parseInt(wordId);
				allWordIDS[globalWordCounter++] = WORDIDS[i][j];
				j++;
			}
		}
		
		dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_WORDS");
		dbaccess.addColumn("ID");
		dbaccess.addColumn("NAME");
		dbaccess.setWhere("ID IN("+HelperTools.implode(allWordIDS)+")");
		sqlResultAsRows = dbaccess.getRecordsByRows();
		
		for (HashMap<String,String> sqlRow : sqlResultAsRows)
		{
			mappingWordIDtoWordNAME.put(Integer.parseInt(sqlRow.get("ID")), sqlRow.get("NAME"));
		}

		for(int i=0; i <TopicIds.length; i++ )
		{
			for(int j=0; j <nrWords; j++ )
			{
				WORDS[i][j] = mappingWordIDtoWordNAME.get(WORDIDS[i][j]);
			}
		}

			
		topicLabels.wordIDs = WORDIDS;
		topicLabels.words = WORDS;
		topicLabels.topicIDs = TopicIds ;
		
		return topicLabels;
	}
	

	public Integer[] topicRanking(int buttonTyp) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		Integer[] topicRanking = new Integer[databaseConfigurationAnalysis.NumberTopics];
		
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_TRANKING");
		dbaccess.addColumn("TOPICID");
		dbaccess.setWhere("RANKTYPE="+buttonTyp);
		dbaccess.setOrder("RANK ASC");
		sqlResultAsColumn = dbaccess.getRecordsByColumns().get("TOPICID");
		
		Integer c = 0;
		for (String TopicId : sqlResultAsColumn)
		{
			topicRanking[c++] = Integer.parseInt(TopicId);
		}
		
		return topicRanking;
	}
	
	
	public Integer[] topicSearch(String textitem) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		Integer[] topicRanking;
		
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		
		DBAccess dbaccess;
		
		String[] multipleWords = textitem.split(" ");
		ArrayList<Integer> TopicList= new ArrayList<Integer>();
		ArrayList<Integer> wordIDList= new ArrayList<Integer>();

		for(int i=0;i<multipleWords.length;i++)
		{
			Stemmer s = new Stemmer();
			s.add(multipleWords[i].toCharArray(),multipleWords[i].length());
			s.stem();
			String StemWordOfSearchedItem= s.toString();

			dbaccess = new DBAccess(databaseConfigurationAnalysis);
			dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_WORDS");
			dbaccess.addColumn("ID");
			dbaccess.setWhere("STEMNAME='"+StemWordOfSearchedItem+"'");
			sqlResultAsColumn = dbaccess.getRecordsByColumns().get("ID");
			
			for (String id : sqlResultAsColumn)
			{
				wordIDList.add(Integer.parseInt(id));
			}
		}
		
		String listOfWordIDs;
		
		if(wordIDList.size() > 0)
		{
			listOfWordIDs = wordIDList.get(0)+"";
			
			for(int i=1; i<wordIDList.size(); i++)
			{
				listOfWordIDs += ","+wordIDList.get(i);
			}
		}
		else
		{
			return new Integer[0];
		}
		
		dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_DESCRIBEDBY t");
		dbaccess.addColumn("t.TOPICID AS ID");
		dbaccess.setWhere("t.WORDID in ("+listOfWordIDs+")");
		dbaccess.setGroup("t.TOPICID");
		dbaccess.setOrder("count(*) DESC, sum(t.PROBABILITY) DESC");
		sqlResultAsColumn = dbaccess.getRecordsByColumns().get("ID");
		
		for (String id : sqlResultAsColumn)
		{
			TopicList.add(Integer.parseInt(id));
		}

		topicRanking = new Integer[TopicList.size()];
		topicRanking = TopicList.toArray(topicRanking);
		
		return topicRanking;
	}

	
	public String [][] getTimeintervals()throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		String[][] timeIntervals;
		
		ArrayList<HashMap<String,String>> sqlResultAsRow = new ArrayList<HashMap<String,String>>();
		
		ArrayList<String> startOfTime= new ArrayList<String>();
		ArrayList<String> endOfTime= new ArrayList<String>();
		ArrayList<Integer> idOfTime= new ArrayList<Integer>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_TOPICINTERVALS");
		dbaccess.addColumn("ID");
		dbaccess.addColumn("intervalStart");
		dbaccess.addColumn("intervalEND");
		dbaccess.setOrder("ID");
		sqlResultAsRow = dbaccess.getRecordsByRows();
		
		for (HashMap<String,String> resultRow : sqlResultAsRow)
		{
			startOfTime.add(resultRow.get("intervalStart"));
			endOfTime.add(resultRow.get("intervalEND"));
			idOfTime.add(Integer.parseInt(resultRow.get("ID")));
		}			

		String[]startOfTimeIntervall= new String [startOfTime.size()];
		String[]endOfTimeIntervall= new String [endOfTime.size()];

		startOfTimeIntervall=startOfTime.toArray(startOfTimeIntervall);
		endOfTimeIntervall=endOfTime.toArray(endOfTimeIntervall);

		timeIntervals = new String[startOfTimeIntervall.length][endOfTimeIntervall.length];

		int i=0;
		int k=1;	
		for(int j=0;j<startOfTimeIntervall.length;j++)
		{
			timeIntervals[j][i]=startOfTimeIntervall[j];	
			timeIntervals[j][k]=endOfTimeIntervall[j];	
		}
		
		return timeIntervals;
	}


	public ThemeRiverData [] getTopicCurrent(Integer [] Topics) throws IllegalArgumentException
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		ThemeRiverData[] topicCurrent;
		
		ArrayList<HashMap<String,String>> sqlResultAsRow = new ArrayList<HashMap<String,String>>();
		HashMap<String,ArrayList<String>> sqlResultAsColumns = new HashMap<String,ArrayList<String>>();
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		
		ArrayList<String> IntervalStartentries= new ArrayList<String>();
		ArrayList<Integer> IntervalIDentries= new ArrayList<Integer>();
		
		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_TOPICINTERVALS");
		dbaccess.addColumn("ID");
		dbaccess.addColumn("intervalStart");
		dbaccess.setOrder("ID ASC");
		sqlResultAsRow = dbaccess.getRecordsByRows();
		
		for (HashMap<String,String> resultRow : sqlResultAsRow)
		{
			IntervalStartentries.add(resultRow.get("intervalStart")); 	
			IntervalIDentries.add(Integer.parseInt(resultRow.get("ID")));
		}
		
		String[] TopicTime = new String[IntervalStartentries.size()];
		TopicTime = IntervalStartentries.toArray(TopicTime);
		Integer [] TimeId = new Integer[IntervalIDentries.size()];
		TimeId = IntervalIDentries.toArray(TimeId);
		
		Integer[] WORDIDS = new Integer[nrWords];
		topicCurrent = new ThemeRiverData[Topics.length];
		
		for(int i=0; i <Topics.length; i++ )
		{
			//für jedes topic
			String[][]WORDS = new String[TimeId.length][nrWords];

			for (int k=0; k<TimeId.length;k++)
			{
				// Zu jeder Zeit
				dbaccess = new DBAccess(databaseConfigurationAnalysis);
				dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_DESCRIBEDBY");
				dbaccess.addColumn("WORDID");
				dbaccess.setWhere("TOPICID=" + Topics[i] +" AND INTERVALID="+TimeId[k]);
				dbaccess.setOrder("PROBABILITY DESC");
				dbaccess.setLimit(String.valueOf(nrWords));
				sqlResultAsColumns = dbaccess.getRecordsByColumns();
				
				int j = 0;
				boolean wordIsCached = true;
				// TODO fix den ganzen columns vs column shice, bessere fehlerkorrektur!
				if (0 < sqlResultAsColumns.size())
				{
					sqlResultAsColumn = sqlResultAsColumns.get("WORDID");
					for (String resultColumn : sqlResultAsColumn)
					{
						WORDIDS[j]=(Integer.parseInt(resultColumn));
						if (!mappingWordIDtoWordNAME.containsKey(WORDIDS[j]))
						{
							wordIsCached = false;
						}
						j++;
					}
				}

				if (wordIsCached)
				{
					for(j=0;j<nrWords;j++)
					{
						WORDS[k][j] = mappingWordIDtoWordNAME.get(WORDIDS[j]);
					}
				}
				else
				{
					dbaccess = new DBAccess(databaseConfigurationAnalysis);
					dbaccess.addTable(databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_WORDS");
					dbaccess.addColumn("NAME");
					dbaccess.setWhere("ID IN("+HelperTools.implode(WORDIDS)+")");
					sqlResultAsColumn = dbaccess.getRecordsByColumns().get("NAME");
					
					j=0;
					for (String resultColumn : sqlResultAsColumn)
					{
						WORDS[k][j]=resultColumn;
						mappingWordIDtoWordNAME.put(WORDIDS[j], WORDS[k][j]);
						j++;
					}
				}				
			}
			
			Double[] TopicLists = getCurrent(Topics[i],TimeId.length);

			topicCurrent[i] = new ThemeRiverData(); 
			topicCurrent[i].topicID = Topics[i];
			topicCurrent[i].relevanceAtTime = TopicLists;
			topicCurrent[i].wordsAtTime = WORDS;
		}
		
		return topicCurrent;
	}

	
	private Double[] getCurrent(int topic, int timesteps)
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) httpSession.getAttribute("databaseConfigurationAnalysis");
		
		Double[] current = new Double[timesteps];
		
		ArrayList<String> sqlResultAsColumn = new ArrayList<String>();
		ArrayList<String> sqlResultAsColumn2 = new ArrayList<String>();
		
		Integer[] counter = new Integer[timesteps];
	
		for(int k = 0; k < timesteps; k++)
		{
			counter[k] = k;
		}

		DBAccess dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
		dbaccess.addColumn("count(DISTINCT DOCID) AS numberDocs");
		dbaccess.setWhere("INTERVALID IN("+HelperTools.implode(counter)+")");
		dbaccess.setGroup("INTERVALID");
		sqlResultAsColumn = dbaccess.getRecordsByColumns().get("numberDocs");
		
		dbaccess = new DBAccess(databaseConfigurationAnalysis);
		dbaccess.addTable(databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
		dbaccess.addColumn("SUM(TOPICPROPORTION) AS y");
		dbaccess.setWhere("TOPICID=" + topic + " AND INTERVALID IN("+HelperTools.implode(counter)+")");
		dbaccess.setGroup("INTERVALID");
		sqlResultAsColumn2 = dbaccess.getRecordsByColumns().get("y");
		
		for(int j = 0; j < timesteps; j++)
		{
			current[j]=Double.parseDouble(sqlResultAsColumn2.get(j))/Double.parseDouble(sqlResultAsColumn.get(j)); // nun ist der average korrekt berechnet!!!
		}
		
		return current;
	}
}


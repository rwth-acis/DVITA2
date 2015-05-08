package i5.dvita.tools.preprocessing;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.DBAccessBatch;
import i5.dvita.tools.commons.Timer;
import i5.dvita.tools.commons.Tools;
import i5.dvita.tools.commons.ToolsIPC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Preprocessing 
{
	int minCountProDoc = 2; // pro dok soll das wort mindestens so oft vorkommen
	 
	ArrayList< HashMap<String,Integer> > wordID2RealWorldQuantityMap = new ArrayList<HashMap<String,Integer>>();	
	private DatabaseConfigurationAnalysis _databaseConfigurationAnalysis = null;
	private ToolsIPC _toolsIPC = null;
	
	final static String MANIFEST_INPUT_SQL_ID = "inputSQL";
	final static String MANIFEST_OUTPUT_SQL_ID = "outputSQL";
	
	public Preprocessing() throws Exception
	{
		this._toolsIPC = new ToolsIPC(MANIFEST_INPUT_SQL_ID);
		Object inputData = null;

		try
		{
			inputData = this._toolsIPC.read().get(MANIFEST_INPUT_SQL_ID);
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (inputData instanceof DatabaseConfigurationAnalysis)
		{
			this._databaseConfigurationAnalysis = (DatabaseConfigurationAnalysis) inputData;
		}
		else
		{
			// TODO error
			this._toolsIPC.write("error wrong datatype");
	    	System.exit(1);
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			Preprocessing d = new Preprocessing();
			d.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception
	{
		boolean overwriteTable = true; 		
		
		Timer stemmingEtc = new Timer();
		Timer writeWords = new Timer();
		Timer writeRealWords = new Timer();

		Map<String, Integer> mapID = new HashMap<String, Integer>();
		HashSet<Integer> savedWords = new HashSet<Integer>();

		int WortID = 0;
		
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = this.getDatabaseConfigurationAnalysis();
		DatabaseConfigurationRawdata databaseConfigurationRawdata = databaseConfigurationAnalysis.databaseConfigurationRawdata;
        
		// Create a table for saving Words
        if(overwriteTable)
        {
        	Tools.DropTableIfExists(databaseConfigurationAnalysis, databaseConfigurationAnalysis.tablePrefix+"_WORDS");
        }
		
		String analysisDatabasePrefix = databaseConfigurationRawdata.tablePrefix;
		
		DBAccess dbAccessAnalysis = new DBAccess(databaseConfigurationAnalysis);
		dbAccessAnalysis.doQueryUpdate("CREATE TABLE "+analysisDatabasePrefix+"_WORDS(STEMNAME VARCHAR (225) NOT NULL, NAME VARCHAR (225), ID INTEGER NOT NULL, PRIMARY KEY (ID))");

		// Create a table for Contains:
		if(overwriteTable)
		{
			Tools.DropTableIfExists(databaseConfigurationAnalysis, databaseConfigurationAnalysis.tablePrefix+"_CONTAINS");	
		}

		dbAccessAnalysis.doQueryUpdate("CREATE TABLE "+analysisDatabasePrefix+"_CONTAINS(WORDID INTEGER NOT NULL, DOCID INTEGER NOT NULL, QUANTITY INTEGER NOT NULL, PRIMARY KEY(WORDID, DOCID))");
		
		// weitere indexe (neben den primary key index) erzeugen
		dbAccessAnalysis.doQueryUpdate("CREATE INDEX "+analysisDatabasePrefix+"_A ON "+analysisDatabasePrefix+"_CONTAINS (WORDID)");
		dbAccessAnalysis.doQueryUpdate("CREATE INDEX "+analysisDatabasePrefix+"_B ON "+analysisDatabasePrefix+"_CONTAINS (DOCID)");
		
		ArrayList<String> stopWrds = this.readStopwords();

		DBAccess dbAccessRawdata = new DBAccess(databaseConfigurationRawdata);
		dbAccessRawdata.addTable(databaseConfigurationRawdata.fromClause);
		dbAccessRawdata.addColumn(databaseConfigurationRawdata.columnNameID + " as ID");
		dbAccessRawdata.addColumn(databaseConfigurationRawdata.columnNameContent + " as Text");
		dbAccessRawdata.addColumn(databaseConfigurationRawdata.columnNameTitle + " as Titel");
		if ((null != databaseConfigurationRawdata.whereClause) 
				&& (databaseConfigurationRawdata.whereClause.length() > 0))
		{
			dbAccessRawdata.setWhere(databaseConfigurationRawdata.whereClause);
		}

		//gehe durch alle dokumente:
		
		ArrayList<HashMap<String,String>> rows = dbAccessRawdata.getRecordsByRowsLimit(1000);
		Integer count = 1;
		
		while (0 < rows.size())
		{
			for (HashMap<String, String> row : rows)
			{
				int ID = Integer.parseInt(row.get("ID"));
				String Text = row.get("Text");
				if(Text == null) continue;
				String t = Text.toLowerCase();
				
				// nutze text und titel wird wort extraction
				String Titel = row.get("Titel");
				if(Titel != null) {
					t+= " "+Titel.toLowerCase();
				}
				
				stemmingEtc.start();
				
				// preprocessing : delete all punctuation mark from the text like
				// .,"!?... and replace the with a blank
				t = t.replaceAll("[^a-z]", " ");

				String[] individualWords = t.split(Pattern.quote(" "));
				
				Map<String, Integer> mapQuantity = new HashMap<String, Integer>();

				
				Stemmer s = new Stemmer();

				// for-loops to remove stopwords from the text
				for (int i = 0; i < individualWords.length; i++)
				{
					String currentWord = individualWords[i];

					// delete words from docs with a length shorter than 2
					if (individualWords[i].length() <= 2)
					{
						//oldTerm = ""; // für bigrams wichtig!!!
						continue;
					}
					
					if(stopWrds.contains(individualWords[i]))
					{
						//oldTerm = ""; // für bigrams wichtig!!!
						continue;
					}
			
					
					// hallo katze hund the maus hund
					// oldterm="";
					// oldterm="hallo";
					//->biram hallo katze
					// oldterm = "katze"
					// -> bigram katze hund

					// Words in text that are saved in "stemword" :

					char[] word = currentWord.toCharArray();
					s.add(word, word.length);
					s.stem();

					String stemword = s.toString();
					// Quantity of words in each Docs, calculated by
					// mapQuantity:

					// Bilde Wort auf eine Zahl ab durch Hash mapQuantity:
					if (mapQuantity.get(stemword) == null)
					{
							mapQuantity.put(stemword, 1);
					}
					else
					{
						int Quantity = mapQuantity.get(stemword);
						Quantity++;
						mapQuantity.put(stemword, Quantity);
					}

					// Refers a WordID to each new word that is recognized in a
					// doc
					Integer stemwordID = mapID.get(stemword);
					if (stemwordID == null)
					{
						mapID.put(stemword, WortID);
						stemwordID = WortID;
						WortID += 1;
		
						wordID2RealWorldQuantityMap.add(new HashMap<String,Integer>());
					}

					if(mapQuantity.get(stemword)==minCountProDoc)
					{
						if(!savedWords.contains(stemwordID)) 
						{
							String sqlinsert = "INSERT INTO " + analysisDatabasePrefix + "_WORDS(STEMNAME, ID ) VALUES('"+ stemword + "'," + stemwordID + ")";
							dbAccessAnalysis.doQueryUpdate(sqlinsert);
							
							savedWords.add(stemwordID);
						}
					}
						
						
					//In  tabelle Word bis jetzt :  nur gestemmte wort, nun das original wort: 
					//wörter im dokument: computer, computer, copmute, computers
					// alle werden gestemt zu zu compt
					// compt -> wordID ist 3
					// wordID2RealWorldQuantityMap[3] ist eine Map die abbildet
					//	computer -> 2x
					// compute -> 1x
					// computers -> 1x


					HashMap<String,Integer> map = wordID2RealWorldQuantityMap.get(stemwordID);
					Integer c = map.get(currentWord);
					
					if(c==null)
					{
						map.put(currentWord,1);
					}
					else
					{
						map.put(currentWord,c+1);
					}
				}
				
				stemmingEtc.pause();
				writeWords.start();

				this._toolsIPC.write(ID);

				Set<String> Words = mapQuantity.keySet();
				insertWords(dbAccessAnalysis, analysisDatabasePrefix, Words,mapQuantity,ID,mapID);
				writeWords.pause();
			}
			
			this._toolsIPC.write(count*1000 + " done!");
			count++;
			
			rows = dbAccessRawdata.getRecordsByRowsLimit(1000);
		}
				
		writeRealWords.start();
		
		// schreibe real world representatives in die datei
		writeRealWorldRepresentatives(analysisDatabasePrefix, wordID2RealWorldQuantityMap.size());
		writeRealWords.pause();
		
		long a = stemmingEtc.getTime();
		long b = writeWords.getTime();
		long c = writeRealWords.getTime();
		
		this._toolsIPC.write("stemingEtc " + a);
		this._toolsIPC.write("writeWords " + b);
		this._toolsIPC.write("writeRealWords " + c);
		
		// IPC output
		HashMap<String, DatabaseConfigurationAnalysis> outputData = new HashMap<String, DatabaseConfigurationAnalysis>();
		outputData.put(MANIFEST_OUTPUT_SQL_ID, databaseConfigurationAnalysis);
		this._toolsIPC.write(outputData);
		
	}

	private  ArrayList<String> readStopwords()
	{		
		DBAccess dbAccessAnalysis = new DBAccess(this.getDatabaseConfigurationAnalysis());
		dbAccessAnalysis.addTable("stopwords");
		dbAccessAnalysis.addColumn("Name");
		return dbAccessAnalysis.getRecordsByColumns().get("Name");
	}

	private  void writeRealWorldRepresentatives(String analysisDatabasePrefix, int size)
	{
		this._toolsIPC.write("starte realworld word writing");

		DBAccessBatch dbAccessBatch = new DBAccessBatch(this._databaseConfigurationAnalysis);
		dbAccessBatch.setQuery("UPDATE "+analysisDatabasePrefix+"_WORDS SET Name = ? WHERE ID = ?");
		HashMap<Integer, Object> values = null;
		
		for(int i=0; i<size; i++)
		{
			//wörter im dokument: computer, computer, copmute, computers
			// alle werden gestemt zu zu compt
			// compt -> wordID ist 3
			// wordID2RealWorldQuantityMap.get(3) ist eine Map die abbildet
			//	computer -> 2x
			// compute -> 1x
			// computers -> 1x
		
			HashMap<String, Integer> map = wordID2RealWorldQuantityMap.get(i);
			String real = "";
			Integer best = -1;
			
			for(String key : map.keySet())
			{
				if(map.get(key)>best)
				{
					best = map.get(key);
					real = key;
				}
			}
			
			//  nun steht in real="copmuter" und in best=2

			values = new HashMap<Integer, Object>();
			values.put(1, real);
			values.put(2, Integer.toString(i));
			dbAccessBatch.addValues(values);
		}

		dbAccessBatch.execute();
		
	}
	
	/**
	 * Die Wörter+ gestemmte Wörter mit entsprechnde IDs in die Tabelle _Contain einfügen
	 * @param words words sind alle gestemmten wörter die in Document x vorkommen
	 * @param mapQuantity mapQuantity bildet gestemmtes wort auf aunzahl ab
	 * @param ID ist die DocID
	 * @param mapID  wort auf Id abbilden
	 */
	
	private  void insertWords(DBAccess dbAccessAnalysis, String analysisDatabasePrefix, Set<String> words,
			Map<String, Integer> mapQuantity, int ID, Map<String, Integer> mapID) {

		
		if(words.isEmpty()) return;
		
		String sqlcontain = "INSERT INTO "+analysisDatabasePrefix+"_CONTAINS(WORDID,DOCID, QUANTITY) VALUES";
		boolean first = true;
		boolean atLeastOne = false;
		
		for (String word : words) 
		{
			int Quantity = mapQuantity.get(word);
			
			if(Quantity < minCountProDoc) { continue; }
			
			atLeastOne = true;
			int WID = mapID.get(word);
			//wordID2OverallCount.set(WID,wordID2OverallCount.get(WID)+Quantity); // overall count hochzählen
			
			if(!first) { sqlcontain += ", ("+ WID + "," + ID + "," + Quantity + ")"; }
			else { first = false; sqlcontain += " ("+ WID + "," + ID + "," + Quantity + ")"; }
		}
		
		if(!atLeastOne) return;

		dbAccessAnalysis.doQueryUpdate(sqlcontain);		
	}
	
	
	public DatabaseConfigurationAnalysis getDatabaseConfigurationAnalysis()
	{
		return this._databaseConfigurationAnalysis;
	}

	public void setDatabaseConfigurationAnalysis(DatabaseConfigurationAnalysis databaseConfigurationAnalysis)
	{
		this._databaseConfigurationAnalysis = databaseConfigurationAnalysis;
	}


	// WAR NUR ZUM TEST
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(HashMap<String, Integer> bigramToCount) {
	        this.base = bigramToCount;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}

}

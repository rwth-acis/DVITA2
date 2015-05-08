package i5.dvita.tools.DynamicLDA;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseConfigurationAnalysis.Granularity;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.DBAccessBatch;
import i5.dvita.tools.commons.Timer;
import i5.dvita.tools.commons.Tools;
import i5.dvita.tools.commons.ToolsIPC;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class DynamicLDA
{
	int sequenceMinIter = 1000; // min und max iterationen
	int sequenceMaxIter = 1000; // für den dynamic LDA
	int maxEMIter = 100; // iterationen für den initialen (static LDA)
	
//	String schema = ConnectionManager.schema;

	boolean linux = false;
	
// default runpath wenn unten nichts anderes angegeben
//	public String runPath = System.getProperty("user.dir") + "\\DLDA";
//	public String runPath = "C:\\Thesis\\DLDA-64BITWIN";
	public String runPath = "DLDA";

	// ignoriere ein Topic für ein bestimmtes Dokument wenn dessen Anteil zu klein ist
	double minTopicProportion = 0.01; 

	// ignoreire ein Wort für ein bestimmtes Topic wenn dessen Anteil zu klein ist
	// (es ist relativ gesehen bzgl. des "besten" wortes aus dem topic)
	// d.h. wenn das beste wort von topic x eine probability von 0.3 hat, dann werden
	// z.b. alle woerter mit probability kleiner 0.003 entfernt (für dieses topic)
	double minRelativeWordProbability = 0.01; 

	// nur wörter mit hinreichend hoher gesamtzahl in datenbank berücksichtigt werden(von blei)
	int minWordCountOverall = 25; // blei hatte 25

	//  nur dokumente mit hinreichend hoher wortzahl betrachten
	// (anzahl wörter im dokument mindestens)
	int minWordsPerDocument = 10;
	
//	ConfigTopicminingShared info2;
//	ConfigRawdataShared info;

	// speichert alle frequent words bzw deren ID (vorder einträge)
	// und bildet diese auf die neuen IDs ab für das topic mining (zweite einträge)
	// dadurch sind beim topic mining nur aufsteigende IDs vorhanden und keine fehldenen einträge
	private HashMap<Integer, Integer> frequentWordsMap;
	// bildet die WordIDs von LDA auf die realen WordIDs ab
	// da die WordIDs von LDA aufsteigend sind kann man einfach array nehmen
	ArrayList<Integer> inverseFrequentWordsMap = new ArrayList<Integer>();


	private boolean overwriteTable;
	private ArrayList<Integer> fileDocID2DatabaseDocID = new ArrayList<Integer>();
	ArrayList<Integer> fileDocID2DatabaseIntervalID = new ArrayList<Integer>();

	final static String MANIFEST_INPUT_SQL_ID = "inputSQL";
	private static final String MANIFEST_OUTPUT_SQL_ID = "outputSQL";
	
	private ToolsIPC _toolsIPC = null;
	
	private DatabaseConfigurationAnalysis _databaseConfigurationAnalysis = null;

	public DynamicLDA() 
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
	
		if (System.getProperty("os.name").toUpperCase().contains("LINUX"))
		{
			linux = true;
		}
	}

	void writeTopicProportions(double[] probs, int DOCID, int intervalID) throws SQLException 
	{
		int nrTopics = probs.length;
		
		DBAccessBatch dbAccessBatch = new DBAccessBatch(this._databaseConfigurationAnalysis);
		dbAccessBatch.setQuery("INSERT INTO " + this._databaseConfigurationAnalysis.tablePrefix + "_BELONGTO(TOPICPROPORTION, DOCID, TOPICID, INTERVALID ) VALUES("
					+ "?" +	"," + DOCID + "," + "?" + "," + intervalID + ")");
		HashMap<Integer, Object> values = null;
		
		for(int t=0; t<nrTopics; t++)
		{
			if(probs[t] < this.minTopicProportion)
			{
				continue;
			}

			values = new HashMap<Integer, Object>();
			values.put(1, probs[t]);
			values.put(2, t);
			dbAccessBatch.addValues(values);
		}
		
		dbAccessBatch.execute();
	}

	void writeWordProb(double[][] probOfwordXAtTimeY, int TopicID) throws SQLException 
	{
		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		String head = "INSERT INTO " + this._databaseConfigurationAnalysis.tablePrefix + "_DESCRIBEDBY (PROBABILITY, WORDID, TOPICID, INTERVALID) VALUES";
		String values = "";

		// speichere immer 200 wörter gleichzeitig
		// damit man nicht für jedes einzelen wort die datenabnk verbinden muss
		int count = 0;
		int max = 200;


		// BESTIMME WIE OFT EIN WORT MINDESTENS AUFTRETEN MUSS (IN DIESEM TOPIC)
		// DAMIT ES SPÄTER GESPEICHERT WIRD

		// bestimme zu jeden zeitpunkt das "beste" wort bzw. dessen probability
		double[] highestProbAtTimeY = new double[probOfwordXAtTimeY[0].length];
		for(int wordID=0; wordID<probOfwordXAtTimeY.length; wordID++) {
			for(int intervalID=0; intervalID<probOfwordXAtTimeY[0].length; intervalID++) {
				if(probOfwordXAtTimeY[wordID][intervalID]>highestProbAtTimeY[intervalID]) {
					highestProbAtTimeY[intervalID] = probOfwordXAtTimeY[wordID][intervalID];
				}
			}
		}


		// ENDE DER BESTIMMUNG


		for(int wordID=0; wordID<probOfwordXAtTimeY.length; wordID++) {
			for(int intervalID=0; intervalID<probOfwordXAtTimeY[0].length; intervalID++) {

				if(probOfwordXAtTimeY[wordID][intervalID]<this.minRelativeWordProbability*highestProbAtTimeY[intervalID]) continue;

				if(count == 0) {
					values += " (" +(probOfwordXAtTimeY[wordID][intervalID])+ "," + inverseFrequentWordsMap.get(wordID) + "," +TopicID+ "," + intervalID + ")";

				} else {
					values += ", (" +(probOfwordXAtTimeY[wordID][intervalID])+ "," + inverseFrequentWordsMap.get(wordID) + "," +TopicID+ "," + intervalID + ")";

				}

				count++;

				if(count == max) {
					dbAccess.doQueryUpdate(head + values);
					count = 0;
					values = "";
				}
			}
		}

		if(count > 0) {
			dbAccess.doQueryUpdate(head + values);
		}

	}


	// schreibe die Informationen über die Dokumente (d.h. welche Wörter treten wie oft im jweiligen
	// Dokument auf) in die dateian data-mult.dat und data-seq.dat
	//
	// zusätzlich werden auch die Maps für die WordsIDs, DokumentIDs und IntervalIDs weggeschrieben
	int writeDocuments(File workingPath, String tmpData, String tmpDirectory) throws Exception {
		// sqlquery die den minimalen und maximalen monat/jahr in der Datenbank ausgibt
		//String sqlquerydate = "SELECT MONTH(min) as minMonth, YEAR(min) as minYear, MONTH(max) as maxMonth, YEAR(max) as maxYear FROM (SELECT MIN(Date) as min, MAX(Date) as max FROM `rawdata`) newTable";
		// sie vergibt den Intervallen eine ID und ein startDate (inklusive!!!) und endDate (exklusive)
		// anhand dieser Intervallen werden nacher die jeweiligen Dokumente des Zeitstempels ausgewählt!!



		fileDocID2DatabaseDocID = new ArrayList<Integer>();
		fileDocID2DatabaseIntervalID.clear();

		Log docFile = new Log(workingPath.getAbsolutePath() + File.separator + tmpData+"-mult.dat",true,false,true);

		LinkedList<Integer> docsPerTimeStamp = new LinkedList<Integer>();

		this._toolsIPC.write("Step1");

		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		dbAccess.addColumn("ID");
		dbAccess.addColumn("intervalStart");
		dbAccess.addColumn("intervalEnd");
		dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix + "_TOPICINTERVALS");
		dbAccess.setOrder("intervalStart ASC");
		
		for (HashMap<String,String> row : dbAccess.getRecordsByRows())
		{
			this._toolsIPC.write("Step2");


			int intervalID = Integer.parseInt(row.get("ID"));
			String intervalStart = row.get("intervalStart");
			String intervalEnd = row.get("intervalEnd");


			// hole nur die DokumentIDs die SOWOHL in contains sind ALS AUCH im gültigen Intervallbereich
			String where = "";
			
			if(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause != null && this._databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause.length()>0)
			{
				where = this._databaseConfigurationAnalysis.databaseConfigurationRawdata.whereClause + " AND ";
			}
			
			// FIXME: this causes a SQL string too long/complex exception if we go beyond ~2000 words per time slice.
			// therefore during preprocessing we need to add an additional table x_docinfos(docid,date) with 
			// mapping from doc to date

			// bestimme documentIDS die im zeitintervall-bereich liegen
			boolean noDocs = false;
			noDocs = true;
			DatabaseConfigurationRawdata databaseConfigurationRawdata = this._databaseConfigurationAnalysis.databaseConfigurationRawdata;
			dbAccess = new DBAccess(databaseConfigurationRawdata);
			dbAccess.addColumn(databaseConfigurationRawdata.columnNameID + " AS DOCID");
			dbAccess.addTable(databaseConfigurationRawdata.fromClause);
			dbAccess.setWhere(where+""+databaseConfigurationRawdata.columnNameDate+">='"+ intervalStart+"' AND "+databaseConfigurationRawdata.columnNameDate+"<'" + intervalEnd+"'");
			String out = "(";
			for (HashMap<String,String> row2 : dbAccess.getRecordsByRows())
			{
				out += row2.get("DOCID") + ",";
				noDocs = false;
			}
			out = out.substring(0,out.length()-1) + ")";
			
			dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
			dbAccess.addColumn("DISTINCT DOCID");
			dbAccess.addTable(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_CONTAINS");
			dbAccess.setWhere("DOCID in " + out);

			int docsInThisTimestamp = 0;

			if(!noDocs) 
			{
				for (HashMap<String,String> row3 : dbAccess.getRecordsByRows())
				{

					int DOCID= Integer.parseInt(row3.get("DOCID"));
					// bestimme wörter (und deren anzhal) für das aktuelle dokument
					DBAccess dbAccess2 = new DBAccess(this._databaseConfigurationAnalysis);
					dbAccess2.addColumn("QUANTITY");
					dbAccess2.addColumn("WORDID");
					dbAccess2.addTable(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix + "_CONTAINS");
					dbAccess2.setWhere("DOCID = " + DOCID);
					dbAccess2.setOrder("WORDID ASC");

					String output = "";
					int distinctWordsPerDoc = 0;
					int overallWordsInDoc = 0;
					for (HashMap<String,String> row4 : dbAccess2.getRecordsByRows())
					{
						// gehe durch alle wörter
						int wordid = Integer.parseInt(row4.get("WORDID"));
						if(this.frequentWordsMap.get(wordid)==null) { 
							// kein frequent word!! nächstes nehmen!
							continue;
						}
						int quantity = Integer.parseInt(row4.get("QUANTITY"));
						// seineWordID:quantity
						output += " " + frequentWordsMap.get(wordid)+ ":" + quantity;
						distinctWordsPerDoc++;
						overallWordsInDoc += quantity;
					}

					if(overallWordsInDoc<this.minWordsPerDocument) continue;

					docsInThisTimestamp++;
					// write to file
					// distinctWordsPerDoc + output + "\n"
					// schreieb die entsprechende Zeile in die Datei
					docFile.log(distinctWordsPerDoc + output + "\n");
					fileDocID2DatabaseDocID.add(DOCID);
					fileDocID2DatabaseIntervalID.add(intervalID);


				}
			
			}
			
			this._toolsIPC.write("  " + intervalStart+" - "+intervalEnd);
			this._toolsIPC.write("  docsInThisTimestamp " + docsInThisTimestamp);
			
			if( docsInThisTimestamp==0)
			{
				this._toolsIPC.write(intervalStart+" - "+intervalEnd);
				System.exit(-1);
			}
			docsPerTimeStamp.add(docsInThisTimestamp);
			
		}

		docFile.close();

		// schreibe die Anzahl Dokumente per Timestamp in die Sequence Datei
		int nrTimesteps = docsPerTimeStamp.size();

		Log countFile = new Log(workingPath.getAbsolutePath()+ File.separator +tmpData+"-seq.dat",true,true,true);
		countFile.log(nrTimesteps + "\n");

		for(Integer count : docsPerTimeStamp) {
			countFile.log(count + "\n");
		}
		countFile.close();
		
		
		// ******** die InverseWOrdID speichern
		Log wordIDFile = new Log(workingPath.getAbsolutePath()+ File.separator +tmpDirectory+"/wordIDs.dat",true,false,true);

		// [Zur Kontrolle das nicht alles durcheinandergeht wird in die erste Zeile
		// die TopicMining ID geschrieben und in die zweite Zeile die Datebase ID]
		wordIDFile.log(this._databaseConfigurationAnalysis.id + "\n");
		wordIDFile.log(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.id + "\n");
		
		// nun kommen die eigentliche WortIDs
		for(int i=0; i<this.inverseFrequentWordsMap.size(); i++) {
			// seine ID (sind aufsteigened) und unsere ID
			wordIDFile.log(i + " " + inverseFrequentWordsMap.get(i) + "\n");
		}
		wordIDFile.close();

		// ********* die dokumentIDs speichern
		// (auch gleichzeitig deren interval ID)
		Log docIDFile = new Log(workingPath.getAbsolutePath()+ File.separator +tmpDirectory+File.separator+"docIDs.dat",true,false,true);
		
		
		// [Zur Kontrolle das nicht alles durcheinandergeht wird in die erste Zeile
		// die TopicMining ID geschrieben und in die zweite Zeile die Datebase ID]
		docIDFile.log(this._databaseConfigurationAnalysis.id + "\n");
		docIDFile.log(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.id + "\n");
		
		// nun die nrTimesteps
		docIDFile.log(nrTimesteps + "\n");
		
		// nun die eigentlichen DokIDs und Intervalle
		for(int i=0; i<fileDocID2DatabaseDocID.size(); i++) {
			// seine docID (sind aufsteigened) und unsere docID + unsere intervalID
			docIDFile.log(i + " " + fileDocID2DatabaseDocID.get(i) + " " + fileDocID2DatabaseIntervalID.get(i) + "\n");
		}
		docIDFile.close();

		
		

		return nrTimesteps;

	}

	void resumeLDA(String tmpDirectory, boolean ignoreID) throws SQLException {
		if(!loadWordIDsFile(tmpDirectory,ignoreID)) {
			this._toolsIPC.write("error A");
			System.exit(-1);
		}
		int nrTimesteps = loadDocIDsFile(tmpDirectory,ignoreID);
		
		if(nrTimesteps == -1) {
			this._toolsIPC.write("error B");
			System.exit(-1);
		}
		
		int nrWords = inverseFrequentWordsMap.size();	
		
		long runtimePostLDA = runPostLDA(tmpDirectory,nrTimesteps,nrWords);
		this._toolsIPC.write("runtime post LDA: " + runtimePostLDA);
	}


	private int loadDocIDsFile(String tmpDirectory, boolean ignoreID)
	{
		int nrTimesteps = -1;
		
		try
		{
			// Open the file
			FileInputStream fstream = new FileInputStream(runPath + File.separator + tmpDirectory + File.separator + "docIDs.dat");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

//			String firstLine = br.readLine();
//			// muss die topicMining ID sein
//			if(Integer.parseInt(firstLine) != this._databaseConfigurationAnalysis.id) 
//			{
//				this._toolsIPC.write("ACHTUNG: TopicMiningID ist anders!!!");
//				if(!ignoreID)System.exit(-1);
//			
//				// kopiere dann die interval tabelle
//				{
//				DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (new DBAccess()).getConfigurationDataAnalysis(Integer.parseInt(firstLine));
//				
//				if(overwriteTable) 
//				{
//					Tools.DropTableIfExists(databaseConfigurationAnalysis, databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS");
//				}
//
//				DBAccess dbAccess = new DBAccess(databaseConfigurationAnalysis);
//				dbAccess.doQueryUpdate("CREATE TABLE "+databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS AS (SELECT ID, intervalStart, intervalEnd FROM "+databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS);");
//				}
//			}

			String secondLine = br.readLine();
			// muss die database ID sein
			if(Integer.parseInt(secondLine) != this._databaseConfigurationAnalysis.databaseConfigurationRawdata.id) {
				this._toolsIPC.write("error 2");
				System.exit(-1);
			}
			
			String thirdLine = br.readLine();
			// ist die Anzahl der TimeSteps
			nrTimesteps = Integer.parseInt(thirdLine);

			// wenn alles korrekt lese die DocIDs und Interval IDs ein
			this.fileDocID2DatabaseDocID.clear();
			this.fileDocID2DatabaseIntervalID.clear();
			int count = 0;
			String strLine;
			strLine = br.readLine();
			while(strLine != null) {
				String[] pair = strLine.split(" ");
				if(Integer.parseInt(pair[0]) != count) {
					// IDs von LDA sind aufsteigend
					this._toolsIPC.write("error 3");
					System.exit(-1);
				}
				
				fileDocID2DatabaseDocID.add(Integer.parseInt(pair[1]));
				fileDocID2DatabaseIntervalID.add(Integer.parseInt(pair[2]));
				
				count++;
				strLine = br.readLine();
			}
			

			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			return -1;
		}

		return nrTimesteps;
		
	}

	private boolean loadWordIDsFile(String tmpDirectory, boolean ignoreID) {

		try {



			// Open the file
			FileInputStream fstream = new FileInputStream(runPath + File.separator + tmpDirectory + "/wordIDs.dat");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String firstLine = br.readLine();
			// muss die topicMining ID sein
			if(Integer.parseInt(firstLine) != this._databaseConfigurationAnalysis.id) {
				this._toolsIPC.write("ACHTUNG TopicMINING ID is anders!!!!");
				if(!ignoreID) System.exit(-1);
			}

			String secondLine = br.readLine();
			// muss die database ID sein
			if(Integer.parseInt(secondLine) != this._databaseConfigurationAnalysis.databaseConfigurationRawdata.id) {
				this._toolsIPC.write("error 2");
				System.exit(-1);
			}

			// wenn alles korrekt lese die WordIDs ein
			this.inverseFrequentWordsMap.clear();
			int count = 0;
			String strLine;
			strLine = br.readLine();
			while(strLine != null) {
				String[] pair = strLine.split(" ");
				if(Integer.parseInt(pair[0]) != count) {
					// IDs von LDA sind aufsteigend
					this._toolsIPC.write("error 3");
					System.exit(-1);
				}
				
				inverseFrequentWordsMap.add(Integer.parseInt(pair[1]));
				
				count++;
				strLine = br.readLine();
			}
			

			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			return false;
		}
		
		return true;
		
		
	}

	void readResult() 
	{
		int nrTopics = this._databaseConfigurationAnalysis.NumberTopics;

		try 
		{
			Timer lda = new Timer();
			Timer preLDA = new Timer();

			preLDA.start();

			createTopicIntervalTable();

			Path runpathp = FileSystems.getDefault().getPath(runPath);
			// Pfad wo die Ergebnisse gespeichert werden
			Path p2 = java.nio.file.Files.createTempDirectory(runpathp,"LDA");
			File workingPath = new File(runPath);
			String tmpData = p2.getFileName()+"/data";
			String tmpDirectory = p2.getFileName().toString();

			
			determineFrequentWords();
			int nrWords = inverseFrequentWordsMap.size();	
			int nrTimesteps = writeDocuments(workingPath,tmpData,tmpDirectory);
			fileDocID2DatabaseDocID.size();

			preLDA.pause();
			
			int curr = 0;
			
			// moeglichkeit zum zwischenspeichern der ergebnisse nach weniger itereationen
			HashSet<Integer> listeItertations = new HashSet<Integer>();
			
			try 
			{
				String line;
				
				lda.start();
				
				Process p = null;
				if(linux) 
				{
					String runCommend = runPath + "/startup.sh -ntopics " + nrTopics + " -mode fit -rng_seed 0 -initialize_lda true -corpus_prefix " + runPath + File.separator + tmpData + " -outname " + runPath + File.separator +p2.getFileName()+" -top_chain_var 0.005 -alpha 0.01 -lda_sequence_min_iter "+this.sequenceMinIter+" -lda_sequence_max_iter "+this.sequenceMaxIter+" -lda_max_em_iter "+this.maxEMIter;
					this._toolsIPC.write("runcmd: " + runCommend);
					this._toolsIPC.write("wrkpth: " + workingPath);
					// export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:~/usr/lib
					p = Runtime.getRuntime().exec(runCommend,null);			
				} 
				else 
				{
					// starte den Topic Mining Algroithms (den C++ Code von Blei)
					String runCommend = runPath + "\\main.exe -ntopics " + nrTopics + " -mode fit -rng_seed 0 -initialize_lda true -corpus_prefix " + runPath + File.separator + tmpData + " -outname " + runPath + File.separator +p2.getFileName()+" -top_chain_var 0.005 -alpha 0.01 -lda_sequence_min_iter "+this.sequenceMinIter+" -lda_sequence_max_iter "+this.sequenceMaxIter+" -lda_max_em_iter "+this.maxEMIter;
					this._toolsIPC.write(runCommend);
					p = Runtime.getRuntime().exec(runCommend);
				}
				
				BufferedReader bri = new BufferedReader
						(new InputStreamReader(p.getInputStream()));
				BufferedReader bre = new BufferedReader
						(new InputStreamReader(p.getErrorStream()));
				while ((line = bre.readLine()) != null) {
					if(line.contains("EM iter")) {
						this._toolsIPC.write("iteration " + curr);
						
						if(listeItertations.contains(curr)) {
							Tools.copyFolder(new File(runPath + File.separator + tmpDirectory),new File(runPath + File.separator + tmpDirectory+"-"+curr));
						}
						curr++;
						
					}
				}
				bre.close();
				while ((line = bri.readLine()) != null) {
				}
				bri.close();


				p.waitFor();
				this._toolsIPC.write("LDA Done.");
				lda.pause();
			}
			catch (Exception err) {
				err.printStackTrace();
			}

			

			long runtimePostLDA = runPostLDA(tmpDirectory,nrTimesteps,nrWords);
			
			Log l = new Log("runtime"+this._databaseConfigurationAnalysis.id+".txt",true,true,true);
			l.log("pre LDA " + preLDA.getTime());
			l.log("LDA " + lda.getTime());
			l.log("post LDA " + runtimePostLDA);
			l.close();
						
			// IPC output
			HashMap<String, DatabaseConfigurationAnalysis> outputData = new HashMap<String, DatabaseConfigurationAnalysis>();
			outputData.put(MANIFEST_OUTPUT_SQL_ID, this._databaseConfigurationAnalysis);
			this._toolsIPC.write(outputData);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private long runPostLDA(String tmpDirectory, int nrTimesteps, int nrWords) throws SQLException {
		
		String path = runPath + File.separator + tmpDirectory + File.separator;

		if(overwriteTable) {
			Tools.DropTableIfExists(this._databaseConfigurationAnalysis, this._databaseConfigurationAnalysis.tablePrefix+"_DESCRIBEDBY");
			Tools.DropTableIfExists(this._databaseConfigurationAnalysis, this._databaseConfigurationAnalysis.tablePrefix+"_BELONGTO");

		}
		
		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);

		// erstmal die Tabllen erstellen
		dbAccess.doQueryUpdate("CREATE TABLE "+this._databaseConfigurationAnalysis.tablePrefix+"_DESCRIBEDBY(PROBABILITY DOUBLE NOT NULL,  WORDID INTEGER NOT NULL, TOPICID INTEGER NOT NULL, INTERVALID INTEGER NOT NULL, PRIMARY KEY(WORDID, TOPICID, INTERVALID))");
		dbAccess.doQueryUpdate("CREATE TABLE "+this._databaseConfigurationAnalysis.tablePrefix+"_BELONGTO(TOPICPROPORTION FLOAT NOT NULL, DOCID INTEGER NOT NULL, TOPICID INTEGER NOT NULL, INTERVALID INTEGER NOT NULL, PRIMARY KEY(DOCID, TOPICID) )");
		
		// weitere indexe (neben den primary key index) erzeugen
		dbAccess.doQueryUpdate("CREATE INDEX `"+this._databaseConfigurationAnalysis.tablePrefix+"_C` ON "+this._databaseConfigurationAnalysis.tablePrefix+"_DESCRIBEDBY (TOPICID, INTERVALID)");
		dbAccess.doQueryUpdate("CREATE INDEX `"+this._databaseConfigurationAnalysis.tablePrefix+"_D` ON "+this._databaseConfigurationAnalysis.tablePrefix+"_BELONGTO (TOPICID, INTERVALID)");
		
		// wenn linux dann ist der pfad noch einen tiefer
//		if(linux) 
//		{
//		 Here, also in Windows this path was the right one. Just out-commented for documentation
			path += "lda-seq" + File.separator;
//		}

		Timer postLDA = new Timer();
		postLDA.start();

		// zunächst für jedes Dokument die Topic Proportions auslesen
		// in gam steht für jedes dokument die topic porpotration
		String topicDistributionFile = path+"gam.dat";
		ArrayList<double[]> allTopicProportions = new ArrayList<double[]>();


		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(topicDistributionFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			int nrDocs = fileDocID2DatabaseDocID.size();

			// lies für jedes daokument sein topic porportions
			for(int i=0; i<nrDocs; i++) {
				int realDocID = fileDocID2DatabaseDocID.get(i);
				double[] topicProportions = new double[this._databaseConfigurationAnalysis.NumberTopics];
				double sum = 0;
				for(int j=0; j<this._databaseConfigurationAnalysis.NumberTopics; j++) {
					strLine = br.readLine(); // lies nächste zeile aus datei
					topicProportions[j] = Double.parseDouble(strLine); // unnormalisiert!!
					sum += topicProportions[j];
				} 
				for(int j=0; j<this._databaseConfigurationAnalysis.NumberTopics; j++) {
					topicProportions[j] /= sum;
				} 

				allTopicProportions.add(topicProportions);
				int interval = fileDocID2DatabaseIntervalID.get(i);
				writeTopicProportions(topicProportions,realDocID,interval);
			}

			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}


		// nun für jedes Topic seine Wortverteilungen

		for(int ttopic=0; ttopic<this._databaseConfigurationAnalysis.NumberTopics; ttopic++) {
			DecimalFormat df = new DecimalFormat("000");
			// jede datei stellt ein topic  dar
			// in der datei ist zu jedem wort und jedem zeitpunkt die anzahl/wahrschenlihckeit dass es auftreitt
			String wordDistributionFile = path+"topic-" + df.format(ttopic) + "-var-e-log-prob.dat";

			double[][] probOfwordXAtTimeY = new double[nrWords][nrTimesteps]; // nur für dieses eine Topic!!!

			try{
				// Open the file that is the first 
				// command line parameter
				FileInputStream fstream = new FileInputStream(wordDistributionFile);
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;

				// ACHTUNG: in der Datei wird für jedes wort zunächst über alle zeitpunkte gegangen
				for(int w=0; w<nrWords; w++) {

					for(int ti=0; ti<nrTimesteps; ti++) {
						strLine = br.readLine();
						probOfwordXAtTimeY[w][ti] = Math.exp(Double.parseDouble(strLine)); // es waren log werte gespeichert
					}
				}

				writeWordProb(probOfwordXAtTimeY,ttopic);


				//Close the input stream
				in.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage() + " bei file " + wordDistributionFile);
			}

		}
		
		postLDA.pause();
		
		return postLDA.stop();
		
	}

	public static Timestamp addOne(Timestamp date, Granularity g) {
		Timestamp calculatedDate = null;

		if (date != null) {
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			switch(g)  {
				case MONTHLY: calendar.add(Calendar.MONTH, 1); break;
				case YEARLY: calendar.add(Calendar.YEAR, 1); break;
				case WEEKLY: calendar.add(Calendar.WEEK_OF_YEAR,1); break;
				case DAYLY: calendar.add(Calendar.DAY_OF_MONTH, 1); break;
				case QUARTERYEAR: calendar.add(Calendar.MONTH, 3); break;
				case HALFYEAR: calendar.add(Calendar.MONTH, 6); break;
				case FIVEYEARS: calendar.add(Calendar.YEAR, 5); break;
				case DECADE: calendar.add(Calendar.YEAR, 1); break;		
			}
			calculatedDate = new Timestamp(calendar.getTime().getTime());
		}

		return calculatedDate;
	}




	private void createTopicIntervalTable() throws SQLException
	{
		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		if(overwriteTable)
		{
			Tools.DropTableIfExists(this._databaseConfigurationAnalysis, this._databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS");
		}

		dbAccess.doQueryUpdate("CREATE TABLE "+this._databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS(ID INTEGER NOT NULL, intervalStart TIMESTAMP NOT NULL, intervalEnd TIMESTAMP NOT NULL, PRIMARY KEY(ID))");

		Timestamp old = null;
		Timestamp current = this._databaseConfigurationAnalysis.rangeStart;

		int id = 0;
		
		DBAccessBatch dbAccessBatch = new DBAccessBatch(this._databaseConfigurationAnalysis);
		dbAccessBatch.setQuery("INSERT INTO "+this._databaseConfigurationAnalysis.tablePrefix+"_TOPICINTERVALS(ID, intervalStart, intervalEnd) VALUES(?,?,?)");
		HashMap<Integer, Object> values = null;
		
		while(current.before(this._databaseConfigurationAnalysis.rangeEnd))
		{
			if(old != null)
			{
				this._toolsIPC.write(old + " - " + current);
				values = new HashMap<Integer, Object>();
				values.put(1, id);
				values.put(2, old);
				values.put(3, current);
				dbAccessBatch.addValues(values);
				id++;
			}

			old = current;
			current = addOne(current,this._databaseConfigurationAnalysis.gran);
		}

		values = new HashMap<Integer, Object>();
		values.put(1, id);
		values.put(2, old);
		values.put(3, this._databaseConfigurationAnalysis.rangeEnd);
		dbAccessBatch.addValues(values);
		
		dbAccessBatch.execute();
	}

	private void determineFrequentWords() {

			DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
			dbAccess.addColumn("WORDID");
			dbAccess.addTable(this._databaseConfigurationAnalysis.databaseConfigurationRawdata.tablePrefix+"_CONTAINS");
			dbAccess.setGroup("WORDID HAVING sum(quantity)>="+this.minWordCountOverall);

			frequentWordsMap = new HashMap<Integer,Integer>();
			inverseFrequentWordsMap = new ArrayList<Integer>();
			int newID = 0;

			for (HashMap<String, String> row : dbAccess.getRecordsByRows())
			{
				// deine WordIDs -> seine WordIDs
				frequentWordsMap.put(Integer.parseInt(row.get("WORDID")),newID);
				// seine WordID -> deine WordID
				inverseFrequentWordsMap.add(Integer.parseInt(row.get("WORDID")));
				newID++;
			}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		/////////////////////////////////////////////////////////////////

		//aus den 3 tabellen alles eingelesen!!!
		///////////////////////////////////////////////////////////////

			//args = new String[2];
			//args = new String[3]; // wenn 3, dann wird altes ergebnis eingelesen
			//args[0]= "1111";
			//args[1]= ""; // so wird es in ./DLDA ausgeführt
			
			// Auf der lokale Festplatte und nicht auf H:\ da zu langsam! 
			 //args[1]="C:/DLDA";
			 
			// wird nur genutzt, wenn 3 argumente angegeben
			//args[2] = "LDA4840017260647270770-2"; // LDA ergebnis auslesen (wenn z.b. vorher abgebrochen)

			DynamicLDA d = new DynamicLDA();
			
			d.overwriteTable = true;

			d.readResult();

		}
}


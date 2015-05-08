package i5.dvita.tools.similardocumentcomputation;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.tools.commons.Timer;
import i5.dvita.tools.commons.Tools;
import i5.dvita.tools.commons.ToolsIPC;
import i5.dvita.tools.commons.Tupel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SimilarDocumentComputation
{
	boolean overwriteTable = true;

	private DatabaseConfigurationAnalysis _databaseConfigurationAnalysis = null;
	private ToolsIPC _toolsIPC = null;
	
	final static String MANIFEST_INPUT_SQL_ID = "inputSQL";
	final static String MANIFEST_OUTPUT_SQL_ID = "outputSQL";
	
	public SimilarDocumentComputation()
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
		
		HashMap<Integer,Double[]> docID2TopicProportions = new HashMap<Integer,Double[]>();
		HashMap<Integer,HashSet<Integer>> intervalID2Documents = new HashMap<Integer,HashSet<Integer>>();

		if(overwriteTable)
		{
			Tools.DropTableIfExists(this._databaseConfigurationAnalysis, this._databaseConfigurationAnalysis.tablePrefix+"_SIMILARDOCS");
		}

		// erstmal die Tabllen erstellen
		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		dbAccess.doQueryUpdate("CREATE TABLE "+this._databaseConfigurationAnalysis.tablePrefix+"_SIMILARDOCS(DOCIDSOURCE INTEGER NOT NULL, DOCIDDESTINATION INTEGER NOT NULL, INTERVALID INTEGER NOT NULL, POSITION INTEGER NOT NULL, PRIMARY KEY(DOCIDSOURCE, DOCIDDESTINATION))");
		dbAccess.doQueryUpdate("CREATE INDEX `"+this._databaseConfigurationAnalysis.tablePrefix+"_E` ON "+this._databaseConfigurationAnalysis.tablePrefix+"_SIMILARDOCS (DOCIDSOURCE, INTERVALID)");


		Timer t2 = new Timer();
		t2.start();

		
		Timer t = new Timer();
		t.start();
		
		// gehe durch alle dokumente
		dbAccess.addColumn("DISTINCT DOCID");
		dbAccess.addColumn("INTERVALID");
		dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix + "_BELONGTO");
		
		for (HashMap<String, String> row : dbAccess.getRecordsByRows())
		{
			int docid = Integer.parseInt(row.get("DOCID"));
			int myintervalID = Integer.parseInt(row.get("INTERVALID"));

			this._toolsIPC.write(docid);
			
			int nrTopics = this._databaseConfigurationAnalysis.NumberTopics;

			/*
			   SHOULD BE THIS:
			 	for(int intervalTime = Math.max(myintervalID - info2.similarDocsTimeShift, 0); 
					intervalTime <= Math.min(myintervalID + info2.similarDocsTimeShift, maxIntervalID); 
					intervalTime++) 
			 */

			for(int intervalTime = Math.max(myintervalID - this._databaseConfigurationAnalysis.similarDocsTimeShift, 0); 
				intervalTime <= myintervalID + this._databaseConfigurationAnalysis.similarDocsTimeShift; 
				intervalTime++) {

				//<> heißt ungleich
				HashSet<Integer> DocList= intervalID2Documents.get(intervalTime);
				
				if(DocList == null) {
						
					t.pause();
					
					DocList = new HashSet<Integer>();
				
				
					dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
					dbAccess.addColumn("DISTINCT DOCID");
					dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix+ "_BELONGTO");
					dbAccess.setWhere("INTERVALID="+intervalTime);
					
					for (HashMap<String, String> row2 : dbAccess.getRecordsByRows())
					{
						DocList.add(Integer.parseInt(row2.get("DOCID")));
						//this._toolsIPC.write("DOCLIST"+DocList);
					}
					
					intervalID2Documents.put(intervalTime,DocList);
				
					t.start();
				}

				/**
				 * betrachte erst das angeklickte Dokument! 
				 *  speichere in TopicproportionOfResearchedDocid die id von topics
				 *  wo der angeklickte Doc in diesen Topics(Topicproportion) auftritt
				 */
				Double[] TopicproportionOfResearchedDocid= docID2TopicProportions.get(docid);

				if(TopicproportionOfResearchedDocid==null) {
					
					t.pause();
					
					TopicproportionOfResearchedDocid = new Double[nrTopics];
					for(int i=0; i<nrTopics; i++) {
						TopicproportionOfResearchedDocid[i] = 0.0;
					}
					dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
					dbAccess.addColumn("TOPICPROPORTION");
					dbAccess.addColumn("TOPICID");
					dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix+ "_BELONGTO");
					dbAccess.setWhere("DOCID="+docid);
					
					for (HashMap<String, String> row3 : dbAccess.getRecordsByRows())
					{
						TopicproportionOfResearchedDocid[Integer.parseInt(row3.get("TOPICID"))]=Double.parseDouble(row3.get("TOPICPROPORTION"));

					}
					
					docID2TopicProportions.put(docid, TopicproportionOfResearchedDocid);
				
					t.start();
				}

				//ZU Jedem Doc muss die 10 ähnlichsten bereit gespeichert sein



				//Klasse Tupel unter Server!
				ArrayList<Tupel> tupel=new ArrayList<Tupel>();

				for (int otherDocId : DocList){
					
					if(otherDocId == docid) continue; // nicht mit sich selbst vergleichen
					
					
					Double[] TopicproportionsOtherDoc= docID2TopicProportions.get(otherDocId);
					
					if(TopicproportionsOtherDoc==null) {
						
						t.pause();
						
						TopicproportionsOtherDoc = new Double[nrTopics];
						for(int i=0; i<nrTopics; i++) {
							TopicproportionsOtherDoc[i] = 0.0;
						}
						/**
						 * Hier speichere in  TopicproportionsOtherDoc die ids von topics
						 * wo die verglichenen Docs in diesen Topics(Topicproportion) auftreten
						 */
						dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
						dbAccess.addColumn("TOPICPROPORTION");
						dbAccess.addColumn("TOPICID");
						dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix+ "_BELONGTO");
						dbAccess.setWhere("DOCID="+otherDocId);
						for (HashMap<String, String> row4 : dbAccess.getRecordsByRows())
						{
							TopicproportionsOtherDoc[Integer.parseInt(row4.get("TOPICID"))]=Double.parseDouble(row4.get("TOPICPROPORTION"));
							//numberNonZeroTopics++;
						}
						
						docID2TopicProportions.put(otherDocId, TopicproportionsOtherDoc);
					
						t.start();
					}
					

					DocumentInfo docinfo = new DocumentInfo();

					docinfo.docID = otherDocId;
					// kopiere die oben bestimmten Porpoertions in das Array für die Ausgabe
					// damit übertragung schneller geht, speichere nur die nicht 0 topics
					docinfo.topicProportions = new Double[nrTopics];
					docinfo.topicIDs = new Integer[nrTopics];
					int k=0;
					for(int i=0; i<nrTopics; i++) {
						docinfo.topicProportions[k]=TopicproportionsOtherDoc[i];
						docinfo.topicIDs[k] = i;
						k++;
					}
					
					/**
					 * Für die Suche nach ähnlichen Dokumenten zu dem angeklickten Dokument: 
					 */
					Double tmp=JensenShanonDivergenz.JSD(TopicproportionOfResearchedDocid, TopicproportionsOtherDoc);
					tupel.add(new Tupel(tmp,docinfo));
				}

				java.util.Collections.sort(tupel);

				
				int maxSimilar = Math.min(this._databaseConfigurationAnalysis.similarDocsCount,tupel.size());
				
				t.pause();
				
				// nehme nur die x-besten als ausgabe
				for(int i=0; i<maxSimilar; i++) {
					DocumentInfo result = tupel.get(i).theDoc;
			

	
					///////////////////////////INSERT IN TABLE////////////////////////////////////////////////////////////////////

					dbAccess.doQueryUpdate("INSERT INTO "+this._databaseConfigurationAnalysis.tablePrefix+"_SIMILARDOCS(DOCIDSOURCE, DOCIDDESTINATION, INTERVALID, POSITION) VALUES("+
									docid + ","+ result.docID+ "," + intervalTime+ ","+(i+1)+")");
					


	
				}
				
				t.start();
				
			}
		}


		long runtime = t.stop();
		long runtime2 = t2.stop();
		
		this._toolsIPC.write("runtime without database connection " + runtime);
		this._toolsIPC.write("overall runtime " + runtime2);


		// IPC output
		HashMap<String, DatabaseConfigurationAnalysis> outputData = new HashMap<String, DatabaseConfigurationAnalysis>();
		outputData.put(MANIFEST_OUTPUT_SQL_ID, this._databaseConfigurationAnalysis);
		this._toolsIPC.write(outputData);
	}

	public static void main(String[] args)
	{
			new SimilarDocumentComputation();
	}

}

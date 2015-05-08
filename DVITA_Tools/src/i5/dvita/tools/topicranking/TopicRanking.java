package i5.dvita.tools.topicranking;

import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.dbaccess.DBAccessBatch;
import i5.dvita.tools.commons.Tools;
import i5.dvita.tools.commons.ToolsIPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class TopicRanking
{
	boolean overwriteTable = true;
	private DatabaseConfigurationAnalysis _databaseConfigurationAnalysis = null;

	private ToolsIPC _toolsIPC = null;
	
	final static String MANIFEST_INPUT_SQL_ID = "inputSQL";
	final static String MANIFEST_OUTPUT_SQL_ID = "outputSQL";
	
	public Integer[] topicSorting(int buttonTyp) throws IllegalArgumentException
	{

		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		dbAccess.addColumn("count(*) AS timesteps");
		dbAccess.addTable(this._databaseConfigurationAnalysis.tablePrefix+ "_TOPICINTERVALS");
		int timesteps = Integer.parseInt(dbAccess.getRecordsByRows().get(0).get("timesteps"));
		
		ArrayList<Tupel2> list= new ArrayList<Tupel2>();
		
		for(int topicid=0; topicid<this._databaseConfigurationAnalysis.NumberTopics; topicid++)
		{
			Double[] topicCurrent = Tools.getCurrent(topicid,this._databaseConfigurationAnalysis,timesteps);
		
			if(buttonTyp==1){
				double sum=0;
				double add=0;
				double var;
				for (int i=0; i < topicCurrent.length-1; i++){
					sum= sum+topicCurrent[i];
				}
				double Mean= sum/topicCurrent.length;
				
				for (int i=0; i < topicCurrent.length-1; i++){
				add=add+Math.pow(topicCurrent[i]-Mean, 2);
				
				}
				var=(1.0/(topicCurrent.length-1.0))*add;
				
				Tupel2 t = new Tupel2(-var,topicid);
				list.add(t);
				
			}
			else if(buttonTyp==2){
				
				double g=0;
				for (int i=0; i < topicCurrent.length-1; i++){
					if(topicCurrent[i+1]-topicCurrent[i]<0 ){
						g= g+(topicCurrent[i+1]-topicCurrent[i]);
					}
				}
				Tupel2 t= new Tupel2(g, topicid);
				list.add(t);
			}
			
			else if(buttonTyp==3){
				double g=0;
				for (int i=0; i < topicCurrent.length-1; i++){
					if(topicCurrent[i+1]-topicCurrent[i]>0 ){
						g= g+topicCurrent[i+1]-topicCurrent[i];
					}
				}
				Tupel2 t= new Tupel2(-g, topicid);
				list.add(t);
			}
			else if(buttonTyp==4){
				// mean (here  equal to sum)
				double sum = 0;
				for(double val : topicCurrent) {
					sum += val;
				}
				// negative value to have large sums at the front
				Tupel2 t = new Tupel2(-sum,topicid);
				list.add(t);
				
			}
			else if(buttonTyp==5){
				double g=0;
				double time=0;
				double lambda = 0.1; // decay mit 0.1
				for (int i=topicCurrent.length-1; i > 0; i--){
					g += Math.exp(-lambda*time)*(topicCurrent[i]-topicCurrent[i-1]);
					time++;
				}
				Tupel2 t= new Tupel2(-g, topicid);
				list.add(t);
			}
		
		}
		
		Collections.sort(list);

		Integer[]TopicIds = new Integer[list.size()];
		
		for(int i=0; i<list.size(); i++) {
			this._toolsIPC.write(list.get(i).value + " " + list.get(i).ID);
			TopicIds[i] = list.get(i).ID;
		}
		
		//return meinRiver;
		return TopicIds;
	}

	
	public TopicRanking()
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

		DBAccess dbAccess = new DBAccess(this._databaseConfigurationAnalysis);
		
		if(overwriteTable)
		{
			Tools.DropTableIfExists(this._databaseConfigurationAnalysis, this._databaseConfigurationAnalysis.tablePrefix+"_TRANKING");
		}

		// erstmal die Tabllen erstellen
		dbAccess.doQueryUpdate("CREATE TABLE " + this._databaseConfigurationAnalysis.tablePrefix + "_TRANKING(RANKTYPE INTEGER NOT NULL, TOPICID INTEGER NOT NULL, RANK INTEGER NOT NULL, PRIMARY KEY(RANKTYPE, TOPICID))");
		
		DBAccessBatch dbAccessBatch = new DBAccessBatch(this._databaseConfigurationAnalysis);
		dbAccessBatch.setQuery("INSERT INTO " + this._databaseConfigurationAnalysis.tablePrefix + "_TRANKING(RANKTYPE, TOPICID, RANK) VALUES(?,?,?)");
		HashMap<Integer, Object> values = null;
		
		for(int sorttype=1; sorttype<=5; sorttype++)
		{
		
			Integer[] order = topicSorting(sorttype);
		
			for(int position=0; position<order.length; position++)
			{
				values = new HashMap<Integer, Object>();
				values.put(1, sorttype);
				values.put(2, order[position]);
				values.put(3, position);
				dbAccessBatch.addValues(values);
			}

			dbAccessBatch.execute();
		}
		
		// IPC output
		HashMap<String, DatabaseConfigurationAnalysis> outputData = new HashMap<String, DatabaseConfigurationAnalysis>();
		outputData.put(MANIFEST_OUTPUT_SQL_ID, this._databaseConfigurationAnalysis);
		this._toolsIPC.write(outputData);
	}

	public static void main(String[] args)
	{
		new TopicRanking();
	}

}

package i5.dvita.webapplication.server;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseRepresentationData;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.webapplication.client.ControlPanelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControlPanelServiceImpl extends RemoteServiceServlet implements ControlPanelService
{
	@Override
	public void removeDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException
	{
		DBAccess dbAccess = new DBAccess();
		dbAccess.removeDatabaseRepresentationData(databaseRepresentationData);
	}

	@Override
	public void addDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException
	{
		DBAccess dbAccess = new DBAccess();
		dbAccess.addDatabaseRepresentationData(databaseRepresentationData);
	}
	
	@Override
	public void setDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException
	{
		DBAccess dbAccess = new DBAccess();
		dbAccess.setDatabaseRepresentationData(databaseRepresentationData);
	}
	
	@Override
	public DatabaseRepresentationData getDatabaseRepresentationData() throws IllegalArgumentException
	{
		DBAccess dbAccess = new DBAccess();
		return dbAccess.getDatabaseRepresentationData();
	}
	
	
	@Override
	public HashMap<String,DatabaseConfigurationAnalysis> getConfigurationDataAnalysisList()
	{
		DBAccess dbAccess = new DBAccess();
		return dbAccess.getConfigurationAnalysisMap();
	}
	
	
	@Override
	public HashMap<String,DTSServerInformation> getServerInformation() throws IllegalArgumentException
	{
		DTSAccessClient dtsAccessClient = new DTSAccessClient();
		DBAccess dbaccess = new DBAccess();
		
		HashMap<String,DTSServerInformation> serverInformation = new HashMap<String,DTSServerInformation>();
		HashMap<String, HashMap<String,String>> dtsData = dbaccess.getConfigurationToolserver();
		
		for (Map.Entry<String, HashMap<String,String>> toolserver : dtsData.entrySet())
		{
			DTSServerInformation dtsServerInformation = dtsAccessClient.selectServer(toolserver.getKey()).getServerInformation().getReturnValue();
			serverInformation.put(toolserver.getKey(), dtsServerInformation);
		}
		
		return serverInformation;
	}

	
	@Override
	public DTSReturn<Void> saveExecutionChain(String dtsId, DTSExecutionChain dtsExecutionChain) throws IllegalArgumentException
	{
		DTSAccessClient dtsAccessClient = new DTSAccessClient();
		DTSReturn<Void> dtsSaveExecutionChain = dtsAccessClient.selectServer(dtsId).saveExecutionChain(dtsExecutionChain);
		
		return dtsSaveExecutionChain;
	}

	
	@Override
	public DTSReturn<Void> invokeExecutionChain(String dtsId, DTSExecutionChainInvoke dtsExecutionChainInvoke) throws IllegalArgumentException
	{
		DTSAccessClient dtsAccessClient = new DTSAccessClient();
		
		// for SQL inputs we need to fill the DTSToolIO data with the server connection data
		for (Map.Entry<String, IDTSToolsIO> entry : dtsExecutionChainInvoke.getCustomInputs().entrySet())
		{
			if (entry.getValue() instanceof DTSToolsIOSQL)
			{
				DTSToolsIOSQL dtsToolsIOSQL = (DTSToolsIOSQL) entry.getValue();
				DatabaseConfigurationAnalysis databaseConfigurationAnalysis = (new DBAccess()).getConfigurationDataAnalysis(Integer.parseInt(dtsToolsIOSQL.getData().get("analysisId")));
				dtsToolsIOSQL.setDatabaseConfigurationAnalysis(databaseConfigurationAnalysis);
			}
		}
		
		DTSReturn<Void> dtsInvokeExecutionChain = dtsAccessClient.selectServer(dtsId).invokeExecutionChain(dtsExecutionChainInvoke);
		
		return dtsInvokeExecutionChain;
	}
	
	
	@Override
	public DTSReturn<ArrayList<String>> getRunningToolOutput(String dtsId, DTSGetRunningToolOutput dtsGetRunningToolOutput)
	{		
		return (new DTSAccessClient()).selectServer(dtsId).getRunningToolOutput(dtsGetRunningToolOutput);
	}


	@Override
	public DTSReturn<ArrayList<String>> getRunningExecutionChainLog(String dtsId, String runningExectionChainUID) throws IllegalArgumentException
	{
		return (new DTSAccessClient()).selectServer(dtsId).getRunningExecutionChainLog(runningExectionChainUID);
	}
}

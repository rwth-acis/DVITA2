package i5.dvita.webapplication.client;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseRepresentationData;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControlPanelServiceAsync 
{
	void getServerInformation(AsyncCallback<HashMap<String,DTSServerInformation>> callback);
	void saveExecutionChain(String dtsId, DTSExecutionChain dtsExecutionChain, AsyncCallback<DTSReturn<Void>> callback);
	void invokeExecutionChain(String dtsId, DTSExecutionChainInvoke dtsExecutionChainInvoke, AsyncCallback<DTSReturn<Void>> callback);
	void getConfigurationDataAnalysisList(AsyncCallback<HashMap<String,DatabaseConfigurationAnalysis>> callback);
	void getRunningToolOutput(String dtsId, DTSGetRunningToolOutput dtsGetRunningToolOutput, AsyncCallback<DTSReturn<ArrayList<String>>> callback);
	void getRunningExecutionChainLog(String dtsId, String runningExecutionChainUID, AsyncCallback<DTSReturn<ArrayList<String>>> callback);
	void getDatabaseRepresentationData(AsyncCallback<DatabaseRepresentationData> callback);
	void setDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData, AsyncCallback<Void> callback);
	void addDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData,AsyncCallback<Void> callback);
	void removeDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData,AsyncCallback<Void> callback);
}

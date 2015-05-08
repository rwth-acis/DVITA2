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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controlPanel")
public interface ControlPanelService extends RemoteService
{
	public HashMap<String,DTSServerInformation> getServerInformation() throws IllegalArgumentException;
	public DTSReturn<Void> saveExecutionChain(String dtsId, DTSExecutionChain dtsExecutionChain) throws IllegalArgumentException;
	public DTSReturn<Void> invokeExecutionChain(String dtsId, DTSExecutionChainInvoke dtsExecutionChainInvoke) throws IllegalArgumentException;
	public HashMap<String,DatabaseConfigurationAnalysis> getConfigurationDataAnalysisList() throws IllegalArgumentException;
	public DTSReturn<ArrayList<String>> getRunningToolOutput(String dtsId, DTSGetRunningToolOutput dtsGetRunningToolOutput) throws IllegalArgumentException;
	public DTSReturn<ArrayList<String>> getRunningExecutionChainLog(String dtsId, String runningExectionChainUID) throws IllegalArgumentException;
	public DatabaseRepresentationData getDatabaseRepresentationData()  throws IllegalArgumentException;
	public void setDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException;
	public void removeDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException;
	public void addDatabaseRepresentationData(DatabaseRepresentationData databaseRepresentationData) throws IllegalArgumentException;
}

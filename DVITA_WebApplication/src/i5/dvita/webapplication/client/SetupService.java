package i5.dvita.webapplication.client;

import i5.dvita.commons.DatabaseCollectionSetup;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.webapplication.shared.SetupServiceAuthResult;
import i5.dvita.webapplication.shared.UserShared;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */

@RemoteServiceRelativePath("setup")
public interface SetupService extends RemoteService
{
	ArrayList<DatabaseCollectionSetup> getSetupInformation();
	public DatabaseConfigurationAnalysis setUpSession(int analysisID, String user, String pw);
	public SetupServiceAuthResult openIdAuth(String userString);
	public String openIdVerify(String responseURL, String disco);
	public UserShared getUser();
	public void saveRequestAnalysis(RequestAnalysis requestAnalysis);
	public HashMap<Integer, RequestAnalysis> getRequestAnalysis();
	public String shareRequestedAnalysis(Integer analysisId, String eMail);
	public void logout();
}


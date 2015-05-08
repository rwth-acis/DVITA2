package i5.dvita.webapplication.client;

import i5.dvita.commons.DatabaseCollectionSetup;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.webapplication.shared.SetupServiceAuthResult;
import i5.dvita.webapplication.shared.UserShared;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface SetupServiceAsync
{
	void getSetupInformation(AsyncCallback<ArrayList<DatabaseCollectionSetup>> asyncCallback);
	void setUpSession(int analysisID, String user, String pw, AsyncCallback<DatabaseConfigurationAnalysis> callback);
	void openIdAuth(String userString, AsyncCallback<SetupServiceAuthResult> callback);
	void openIdVerify(String responseURL, String disco, AsyncCallback<String> callback);
	void getUser(AsyncCallback<UserShared> callback);
	void saveRequestAnalysis(RequestAnalysis requestAnalysis, AsyncCallback<Void> callback);
	void getRequestAnalysis(AsyncCallback<HashMap<Integer, RequestAnalysis>> callback);
	void shareRequestedAnalysis(Integer analysisId, String eMail, AsyncCallback<String> callback);
	void logout(AsyncCallback<Void> callback);
}

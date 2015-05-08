package i5.dvita.webapplication.server;

import i5.dvita.commons.ACL;
import i5.dvita.commons.ACLUsers;
import i5.dvita.commons.DVITAUser;
import i5.dvita.commons.DatabaseCollectionSetup;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.dbaccess.DBAccess;
import i5.dvita.webapplication.client.SetupService;
import i5.dvita.webapplication.shared.SetupServiceAuthResult;
import i5.dvita.webapplication.shared.UserShared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ibm.db2.jcc.am.bo;

@SuppressWarnings("serial")
public class SetupServiceImpl extends RemoteServiceServlet implements SetupService
{
	public DatabaseConfigurationAnalysis setUpSession(int analysisId, String username, String passwort)
	{
		DBAccess dbaccess = new DBAccess();
		DatabaseConfigurationAnalysis databaseConfigurationAnalysis = dbaccess.getConfigurationDataAnalysis(analysisId);
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		httpSession.setAttribute("databaseConfigurationAnalysis", databaseConfigurationAnalysis);
		
		return databaseConfigurationAnalysis;
	}
	
	public ArrayList<DatabaseCollectionSetup> getSetupInformation()
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		Integer userId = (Integer) httpSession.getAttribute("userId");
		
		DBAccess dbAccess = new DBAccess();
		
		if (null == userId)
		{
			return dbAccess.getConfigurationAnalysisRepresentation(null); 
		}
		
		DVITAUser dvitaUser = dbAccess.getUsers().get(userId);
		
		if (ACLHandler.getOperations(dvitaUser.getRole()).contains(ACL.OPERATION_CONTROL_PANEL))
		{
			userId = 0;
		}
		
		return dbAccess.getConfigurationAnalysisRepresentation(userId); 
	}
	
	public SetupServiceAuthResult openIdAuth(String userString)
	{
		SetupServiceAuthResult setupServiceAuthResult = new SetupServiceAuthResult();
		
		ConsumerManager manager = new ConsumerManager();
		
		String returnURL = this.getOpenIDReturnUrl();
		
		 // perform discovery on the user-supplied identifier
	    @SuppressWarnings("rawtypes")
		List discoveries = null;
		try
		{
			discoveries = manager.discover(userString);
		} 
		catch (DiscoveryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    // attempt to associate with the OpenID provider
	    // and retrieve one service endpoint for authentication
	    DiscoveryInformation discovered = manager.associate(discoveries);

	    // obtain a AuthRequest message to be sent to the OpenID provider
	    AuthRequest authReq = null;
	    try
		{
	    	authReq = manager.authenticate(discovered, returnURL);
		} 
	    catch (MessageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    catch (ConsumerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
//	    String discoString = null;
//
//	    try
//	    {
//	        ByteArrayOutputStream bo = new ByteArrayOutputStream();
//	        ObjectOutputStream so = new ObjectOutputStream(bo);
//	        so.writeObject(discovered);
//	        so.flush();
//	        discoString = bo.toString();
//	    } 
//	    catch (Exception e)
//	    {
//	        System.out.println(e);
//	        System.exit(1);
//	    }
	    
	    FetchRequest fetch = FetchRequest.createFetchRequest();
    	try
		{
			fetch.addAttribute("FirstName", "http://schema.openid.net/namePerson/first", true);
			fetch.addAttribute("LastName", "http://schema.openid.net/namePerson/last", true);
	    	fetch.addAttribute("Email", "http://schema.openid.net/contact/email", true);
		} 
    	catch (MessageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// wants up to three email addresses
    	fetch.setCount("Email", 3);
		try
		{
			authReq.addExtension(fetch);
		} 
		catch (MessageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    
		String authUID = UUID.randomUUID().toString().replace("-", "");
		
	    ConsumerManagerHandler consumerManagerHandler = ConsumerManagerHandler.getInstance();
	    consumerManagerHandler.setManager(authUID, manager);
	    consumerManagerHandler.setDiscovery(authUID, discovered);
	    
	    setupServiceAuthResult.setRedirectURL(authReq.getDestinationUrl(true));
	    setupServiceAuthResult.setAuthUID(authUID);
	    (new DBAccess()).doQueryUpdate("INSERT INTO error SET errorcol=\"authUID: "+authUID+"\"");
//	    setupServiceAuthResult.setDiscovery(discoString);
	    
	    return setupServiceAuthResult;
	}


	@Override
	public String openIdVerify(String responseURL, String disco)
	{
//DBAccess dbAccess = new DBAccess();
//		
//
//ArrayList<DatabaseCollectionSetup> a = dbAccess.getConfigurationAnalysisRepresentation(null); 
//
//			return Integer.toString(a.size());
		
		DBAccess dbAccess = new DBAccess();
		
		ParameterList openidResp = null;
		
		// create responseQuery
		String responseQuery = responseURL.substring(responseURL.indexOf('?')+1);
		dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+responseQuery+"\"");
		
		if (responseQuery.substring(responseQuery.length()-5, responseQuery.length()).equalsIgnoreCase("#main"))
		{
			responseQuery = responseQuery.substring(0, responseQuery.length()-5);
		}
		else if (responseQuery.endsWith("#"))
		{
			responseQuery = responseQuery.substring(0, responseQuery.length()-1);
		}
		
		dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+responseQuery+"\"");
		
		// create responseReturnTo
		String responseReturnTo =  responseURL.substring(0, responseURL.indexOf('?'));

		// TODO HACK
		responseReturnTo = getOpenIDReturnUrl();
		
		dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+responseReturnTo+"\"");
		try
		{
			openidResp = ParameterList.createFromQueryString(responseQuery);
		} 
		catch (MessageException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+e1.getMessage()+"\"");
		}

//		DiscoveryInformation discoveryInformation = null;

		dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\"disco: "+disco+"\"");
//		try
//		{
//		     byte b[] = disco.getBytes("UTF8");
//		     dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\"b: "+b.toString()+"\"");
//		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
//		     dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\"bi: "+bi.available()+"\"");
//		     ObjectInputStream si = new ObjectInputStream(bi);
//		     discoveryInformation = (DiscoveryInformation) si.readObject();
//		}
//		catch (Exception e)
//		{
//		     e.printStackTrace();
//		     dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+e.getMessage()+"\"");
//		}

	    // verify the response
	    VerificationResult verification = null;
//	    String userManagerId = discoveryInformation.getClaimedIdentifier().toString();

	    ConsumerManagerHandler consumerManagerHandler = ConsumerManagerHandler.getInstance();
//	    ConsumerManager manager = consumerManagerHandler.getManager(userManagerId);
	    ConsumerManager manager = consumerManagerHandler.getManager(disco);
	    DiscoveryInformation discoveryInformation = consumerManagerHandler.getDiscovery(disco);
	    
		try
		{
			verification = manager.verify(responseReturnTo, openidResp, discoveryInformation);
		} 
		catch (MessageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+e.getMessage()+"\"");
		} 
		catch (DiscoveryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+e.getMessage()+"\"");
		} 
		catch (AssociationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbAccess.doQueryUpdate("INSERT INTO error SET errorcol=\""+e.getMessage()+"\"");
		}

	    // examine the verification result and extract the verified identifier
	    Identifier verified = verification.getVerifiedId();

	    if (verified != null)
        {
            AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
            {
                FetchResponse fetchResp;
				try
				{
					fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
					@SuppressWarnings("rawtypes")
					List emails = fetchResp.getAttributeValues("Email");
	                String fristNameString = fetchResp.getAttributeValue("FirstName");
	                String lastNameString = fetchResp.getAttributeValue("LastName");
	                String email = (String) emails.get(0);
              
	                dbAccess = new DBAccess();
	                Integer userId = dbAccess.getUserByIdent(authSuccess.getIdentity());
	                
	                if (null == userId)
	                {
	                	DVITAUser dvitaUser = new DVITAUser();
	                	dvitaUser.setName(fristNameString + " " + lastNameString);
	                	dvitaUser.setIdent(authSuccess.getIdentity());
	                	dvitaUser.setEmail(email);
	                	dvitaUser.setRole(ACL.ROLE_USER);
	                	dbAccess.addUser(dvitaUser);
	                	userId = dbAccess.getUserByIdent(authSuccess.getIdentity());
	                }
	                
	                HttpSession httpSession = getThreadLocalRequest().getSession(true);
                	httpSession.setAttribute("userId", userId);
				} 
				catch (MessageException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }

            consumerManagerHandler.removeManager(disco);
            consumerManagerHandler.removeDiscovery(disco);
            
            return null;  // success
        }
	    else
	    {
	        return "Can't log in";
	    }
	}

	@Override
	public UserShared getUser()
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		Integer userId = (Integer) httpSession.getAttribute("userId");
		
		DBAccess dbAccess = new DBAccess();
		UserShared userShared = new UserShared();
		
		if (null == userId)
		{
			userShared.setName("Guest");
			userShared.setOperations(ACLHandler.getOperations(ACL.ROLE_GUEST));
		}
		else
		{
			DVITAUser dvitaUser = dbAccess.getUsers().get(userId);
			userShared.setName(dvitaUser.getName());
			userShared.setOperations(ACLHandler.getOperations(dvitaUser.getRole()));
		}
		
		return userShared;
	}

	@Override
	public void saveRequestAnalysis(RequestAnalysis requestAnalysis)
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		Integer userId = (Integer) httpSession.getAttribute("userId");
		
		DBAccess dbAccess = new DBAccess();
		
		if (null == requestAnalysis.getUserId())
		{
			requestAnalysis.setUserId(userId);
		}
		
		dbAccess.saveRequestAnalysis(requestAnalysis);
		
		if (requestAnalysis.getStatus().equalsIgnoreCase(RequestAnalysis.STATUS_FINISHED))
		{
			ACLUsers aclUsers = new ACLUsers();
			aclUsers.setUserId(requestAnalysis.getUserId());
			aclUsers.setOperation(ACLUsers.OPERATION_READ);
			aclUsers.setTarget(requestAnalysis.getAnalysisId());
			
			dbAccess.saveAclUsers(aclUsers);
		}
	}

	@Override
	public HashMap<Integer, RequestAnalysis> getRequestAnalysis()
	{	
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		Integer userId = (Integer) httpSession.getAttribute("userId");
		
		if (null == userId)
		{
			return null;
		}
		
		DBAccess dbAccess = new DBAccess();
		DVITAUser dvitaUser = dbAccess.getUsers().get(userId);
		
		HashMap<Integer, RequestAnalysis> requestAnalysis = dbAccess.getRequestAnalysis();
		List<String> operations = ACLHandler.getOperations(dvitaUser.getRole());
		
		if (operations.contains(ACL.OPERATION_INVOKE_ANALYSIS))
		{
			return requestAnalysis;
		}
		else if (operations.contains(ACL.OPERATION_REQUEST_ANALYSIS))
		{
			HashMap<Integer, RequestAnalysis> requestAnalysisUser = new HashMap<Integer, RequestAnalysis>();
			
			for (Map.Entry<Integer, RequestAnalysis> entry : requestAnalysis.entrySet())
			{
				if (userId == entry.getValue().getUserId())
				{
					entry.getValue().setUser(null);
					requestAnalysisUser.put(entry.getKey(), entry.getValue());
				}
			}
			
			return requestAnalysisUser;
		}
		
		return null;
	}

	@Override
	public String shareRequestedAnalysis(Integer analysisId, String eMail)
	{
		DBAccess dbAccess = new DBAccess();
		dbAccess.addColumn("id");
		dbAccess.addTable(dbAccess.getDb().tablePrefix + DBAccess.SQL_DATABASE_USERS_TABLE_NAME);
		dbAccess.setWhere("email=\"" + eMail + "\"");
		
		ArrayList<HashMap<String,String>> result = dbAccess.getRecordsByRows();
		
		if (0 == result.size())
		{
			return "No user is registered with that eMail";
		}
		
		if (1 < result.size())
		{
			return "Multiple users are registered with that eMail!";
		}
		
		dbAccess.doQueryUpdate("INSERT INTO " + dbAccess.getDb().tablePrefix + DBAccess.SQL_DATABASE_ACL_USERS_TABLE_NAME + " SET user_id="+result.get(0).get("id") + ",operation=\"READ\",target_id="+analysisId);
		
		return null;
	}

	@Override
	public void logout()
	{
		HttpSession httpSession = getThreadLocalRequest().getSession(true);
		httpSession.removeAttribute("userId");
	}
	
	private String getOpenIDReturnUrl()
	{
		String openIDreturnUrl = "";
		
		try
		{
			List<String> configFileLines = Files.readAllLines(Paths.get(DBAccess.CONFIG_FILE_PATH), DBAccess.CONFIG_FILE_ENCODING);
			
			for (String configLine : configFileLines)
			{
				List<String> configLineExploded = new ArrayList<String>(Arrays.asList(configLine.split("=")));
				
				if (2 != configLineExploded.size())
				{
					// invalid line, skip
					continue;
				}
				
				if (configLineExploded.get(0).equalsIgnoreCase("openIDreturnUrl"))
				{
					return configLineExploded.get(1);
				}
			}
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return openIDreturnUrl;
	}
}

